// ==UserScript==
// @name         Krabot Characteristics Extractor
// @namespace    krabot
// @version      __PROJECT_VERSION__
// @description  Extract character name, playerId and PP and POST to backend only when changed
// @match        *://www.kraland.org/jouer/plateau
// @match        *://www.kraland.org/profil/interface
// @grant        GM_xmlhttpRequest
// @connect      __BACKEND_HOST__
// @connect      __BACKEND_HOST_NO_PORT__
// @connect      127.0.0.1
// ==/UserScript==

(function () {
    'use strict';

    const BACKEND_URL = '__BACKEND_URL__';
    const API_KEY_STORAGE = 'krabot_api_key';

    function getApiKey() {
        return localStorage.getItem(API_KEY_STORAGE);
    }

    function getSnapshotKey() {
        const k = getApiKey();
        return 'krabot_snapshot_' + (k ? k : 'default');
    }

    // Snapshot storage with TTL (1 hour)
    const SNAPSHOT_TTL_MS = 60 * 60 * 1000; // 1 hour

    function loadSnapshot() {
        const key = getSnapshotKey();
        try {
            const raw = localStorage.getItem(key);
            if (!raw) return null;
            let parsed;
            try {
                parsed = JSON.parse(raw);
            } catch (e) {
                // invalid JSON -> remove the stored item
                try {
                    localStorage.removeItem(key);
                } catch (_) {
                }
                return null;
            }
            // Expecting stored shape: { data: {...}, savedAt: 123456789 }
            if (!parsed || typeof parsed !== 'object' || !parsed.hasOwnProperty('savedAt') || parsed.data === undefined) {
                // incompatible shape -> remove it
                try {
                    localStorage.removeItem(key);
                } catch (_) {
                }
                return null;
            }
            const savedAt = Number(parsed.savedAt) || 0;
            const age = Date.now() - savedAt;
            if (age <= SNAPSHOT_TTL_MS && savedAt > 0) return parsed.data;
            // expired or invalid savedAt -> remove and return null
            try {
                localStorage.removeItem(key);
            } catch (_) {
            }
            return null;
        } catch (e) {
            // unexpected error -> attempt to remove and return null
            try {
                localStorage.removeItem(getSnapshotKey());
            } catch (_) {
            }
            return null;
        }
    }

    function saveSnapshot(snapshot) {
        try {
            const payload = {data: snapshot, savedAt: Date.now()};
            localStorage.setItem(getSnapshotKey(), JSON.stringify(payload));
        } catch (e) {
            console.error('Failed to save snapshot', e);
        }
    }

    // Utility: extract last numeric id from an href string
    function extractIdFromHref(href) {
        if (!href) return null;
        try {
            const all = href.toString().match(/([0-9]+)/g);
            if (!all || !all.length) return null;
            return all[all.length - 1];
        } catch (e) {
            return null;
        }
    }

    function extractPlateau() {
        let name = null;
        try {
            const titleEl = document.querySelector('#col-left .list-group .list-group-item.active');
            name = titleEl.innerText.replace('×','').trim();
            console.info('[Krabot] name extracted from plateau: ' + name);
        } catch (e) {
            name = null;
            console.error('[Krabot] failed to extract name from plateau', e);
        }

        let playerId = null;
        try {
            const anchor = document.querySelector('.dashboard a[href*="communaute/membres"]');
            const href = anchor && anchor.getAttribute('href');
            playerId = extractIdFromHref(href);
            console.info('[Krabot] playerId extracted from plateau: ' + playerId);
        } catch (e) {
            playerId = null;
            console.error('[Krabot] failed to extract playerId from plateau', e);
        }

        let pp = null;
        try {
            // Try to find an anchor whose tooltip contains "Puissance Politique" (data-original-title or title)
            const ppAnchor = document.querySelector('a[data-original-title*="Puissance Politique"], a[title*="Puissance Politique"]');
            const dataTitle = ppAnchor && (ppAnchor.getAttribute('data-original-title') || ppAnchor.getAttribute('title'));
            const m = dataTitle.match(/Puissance\s+Politique[\s:\u00A0\-\u2011\u2013\u2014]*([0-9]{1,5})/i);

            pp = parseInt(m[1].replace(/\D/g, ''), 10);
            console.info('[Krabot] PP extracted from plateau: ' + pp);
        } catch (e) {
            pp = null;

            try {
                const labels = Array.from(document.querySelectorAll('.mobile-gauge-compact-label'));
                for (const lbl of labels) {
                    const txt = (lbl.textContent || '').trim().toUpperCase();
                    if (txt === 'PP' || txt.startsWith('PP') || txt.includes('PUISSANCE')) {
                        const firstSibling = lbl.nextElementSibling; // usually the .mobile-gauge-compact-bar
                        const secondSibling = firstSibling && firstSibling.nextElementSibling ? firstSibling.nextElementSibling : null;
                        if (secondSibling && secondSibling.classList && secondSibling.classList.contains('mobile-gauge-compact-value')) {
                            const v = (secondSibling.textContent || '').trim().replace(/\D/g, '');
                            if (v.length) {
                                pp = parseInt(v, 10);
                                console.info('[Krabot] PP extracted from plateau (mobile-gauge fallback): ' + pp);
                            }
                        }
                        break;
                    }
                }
            } catch (ee) {
                console.error('[Krabot] failed to extract PP from plateau (tooltip)', e);
                console.error('[Krabot] failed to extract PP from plateau (mobile-gauge fallback)', ee);
            }
        }

        return {playerId, name, pp};
    }


    function shouldSend(data) {
        if (!data || !data.playerId) return false;
        const snap = loadSnapshot();
        if (!snap) return true;
        return snap.playerId !== data.playerId || snap.name !== data.name || snap.pp !== data.pp;
    }

    function send(data) {
        const apiKey = getApiKey();
        const url = BACKEND_URL + '/krabot/characteristics/uploadCharacteristics' + (apiKey ? '?apiKey=' + encodeURIComponent(apiKey) : '');
        // Only send if pp is a number; otherwise avoid sending to prevent server treating null as 0
        if (typeof data.pp !== 'number') {
            console.warn('[Krabot] send aborted: pp is missing or not a number', data.pp);
            return;
        }
        const payload = {playerId: data.playerId, name: data.name, pp: data.pp};
        GM_xmlhttpRequest({
            method: 'POST',
            url: url,
            headers: {'Content-Type': 'application/json'},
            data: JSON.stringify(payload),
            onload(resp) {
                console.log('[Krabot] characteristics sent', resp.status);
            },
            onerror(err) {
                console.error('[Krabot] failed to send characteristics', err);
            }
        });
    }

    function trySend(data) {
        if (!data || !data.playerId) {
            console.warn('Krabot: Unable to find playerId', data);
            return;
        }
        // sanitize name and reject obvious false positives
        const bannedNames = ['Krabot - Caractéristiques', 'Krabot - Caracteristiques', '(no name)'];
        const name = (data.name || '').toString().trim();
        if (!name || bannedNames.includes(name)) {
            console.warn('Krabot: refusing to send empty or invalid name', name);
            return;
        }
        // do not attempt to send if pp is missing (avoid server-side conversion of null -> 0)
        if (typeof data.pp !== 'number') {
            console.log('[Krabot] pp not available — send skipped');
            return;
        }
        if (shouldSend(data)) {
            send(data);
            try {
                saveSnapshot({playerId: data.playerId, name: data.name, pp: data.pp});
            } catch (e) {
            }
            console.log('[Krabot] snapshot updated');
        } else {
            console.log('[Krabot] no change detected, send ignored');
        }
    }

    // Expose debug helpers (both extractors) and keep a convenience .extract that chooses by pathname
    try {
        if (typeof window !== 'undefined') {
            window.__krabot = window.__krabot || {};
            window.__krabot.extractPlateau = extractPlateau;
            window.__krabot.extractInterface = extractInterface;
            window.__krabot.trySend = trySend;
            window.__krabot.getSnapshotKey = getSnapshotKey;
            // convenience: choose extractor based on current path
            window.__krabot.extract = function () {
                if (location.pathname.includes('/jouer/plateau')) return extractPlateau();
                if (location.pathname.includes('/profil/interface')) return extractInterface();
                return extractPlateau();
            };
        }
    } catch (e) {
    }

    if (location.pathname.includes('/jouer/plateau')) {
        try {
            const d = extractPlateau();
            const snap = loadSnapshot();
            // send if no snapshot or if the detected values differ from the snapshot
            if (!snap) {
                console.log('[Krabot] no snapshot found — sending initial data', d);
                trySend(d);
            } else if (snap.playerId !== d.playerId || snap.name !== d.name || snap.pp !== d.pp) {
                console.log('[Krabot] change detected vs snapshot — updating', {from: snap, to: d});
                trySend(d);
            } else {
                console.log('[Krabot] snapshot up-to-date — no initial send');
            }
        } catch (e) {
            console.error('[Krabot] failed to perform initial plateau send', e);
        }
    }

    if (location.pathname.includes('/profil/interface')) {
        (function initPanelSimple() {
            // if panel exists, ensure there's only one Refresh button and observe for late injectors
            const existing = document.querySelector('.krabot-panel');
            if (existing) {
                try {
                    const btns = Array.from(existing.querySelectorAll('button')).filter(b => /Rafraîchir/i.test((b.innerText || '').trim()));
                    if (btns.length > 1) btns.slice(1).forEach(b => b.remove());
                    const ob = new MutationObserver(() => {
                        const bs = Array.from(existing.querySelectorAll('button')).filter(b => /Rafraîchir/i.test((b.innerText || '').trim()));
                        if (bs.length > 1) bs.slice(1).forEach(b => b.remove());
                    });
                    ob.observe(existing, {childList: true, subtree: true});
                } catch (e) { /* ignore */
                }
                return;
            }

            // create panel and append once ready
            const tryCreate = () => {
                if (document.querySelector('.krabot-panel')) return true;
                const template = document.querySelector('.panel') || document.querySelector('.card');
                const colLeft = document.querySelector('div.col-left');
                const target = colLeft || document.querySelector('div.col-md-3.sidebar') || document.querySelector('#content') || document.body;
                if (!target) return false;

                const panel = document.createElement('div');
                panel.className = (template ? template.className : 'panel panel-default') + ' krabot-panel';
                const heading = document.createElement('div');
                heading.className = (template && (template.querySelector('.panel-heading') || template.querySelector('.card-header')) ? (template.querySelector('.panel-heading') || template.querySelector('.card-header')).className : 'panel-heading');
                heading.innerHTML = '<h3 class="panel-title"><i class="fa fa-list"></i> Krabot - Caractéristiques</h3>';
                panel.appendChild(heading);
                const body = document.createElement('div');
                body.className = (template && (template.querySelector('.panel-body') || template.querySelector('.card-body')) ? (template.querySelector('.panel-body') || template.querySelector('.card-body')).className : 'panel-body');
                panel.appendChild(body);

                // header refresh
                const right = document.createElement('span');
                right.className = 'pull-right clickable';
                const refreshBtn = document.createElement('button');
                refreshBtn.className = 'btn btn-default btn-xs';
                refreshBtn.style.marginLeft = '6px';
                refreshBtn.innerText = 'Rafraîchir';
                right.appendChild(refreshBtn);
                heading.appendChild(right);

                // body content
                const apiInput = document.createElement('input');
                apiInput.className = 'form-control';
                apiInput.placeholder = 'apiKey UUID (optionnel)';
                apiInput.value = localStorage.getItem(API_KEY_STORAGE) || '';
                const saveBtn = document.createElement('button');
                saveBtn.className = 'btn btn-default btn-xs';
                saveBtn.style.marginTop = '6px';
                saveBtn.innerText = 'Save apiKey';
                const inlineFeedback = document.createElement('div');
                inlineFeedback.style.marginTop = '6px';
                inlineFeedback.style.fontSize = '12px';
                const listGroup = document.createElement('div');
                listGroup.className = 'list-group';
                listGroup.style.marginTop = '8px';
                listGroup.style.maxHeight = '400px';
                listGroup.style.overflow = 'auto';
                body.appendChild(apiInput);
                body.appendChild(saveBtn);
                body.appendChild(inlineFeedback);
                body.appendChild(listGroup);

                // append safely (avoid forms)
                if (target.tagName === 'FORM') document.body.appendChild(panel); else target.appendChild(panel);

                // utility sanitizeName (same logic)
                const sanitizeName = (n) => {
                    if (!n) return '(no name)';
                    let s = String(n).replace(/\u00A0/g, ' ').trim();
                    // remove trailing separators/dashes/# followed by at least 2 digits
                    s = s.replace(/[\s\u00A0\p{Pd}#]*\d{2,}\s*$/u, '');
                    return s.trim();
                };

                // helpers
                const buildUrl = (path, params) => {
                    const apiKey = localStorage.getItem(API_KEY_STORAGE);
                    const u = new URL((BACKEND_URL || '') + path);
                    if (apiKey) u.searchParams.set('apiKey', apiKey);
                    if (params) Object.keys(params).forEach(k => {
                        if (params[k] != null) u.searchParams.set(k, params[k]);
                    });
                    return u.toString();
                };
                const gmGet = (url) => new Promise((resolve, reject) => {
                    GM_xmlhttpRequest({
                        method: 'GET', url, onload(resp) {
                            try {
                                if (resp.status >= 200 && resp.status < 300) return resolve(resp.responseText ? JSON.parse(resp.responseText) : null);
                                return reject({status: resp.status, body: resp.responseText});
                            } catch (e) {
                                return reject({status: resp.status || 0, body: 'invalid-json'});
                            }
                        }, onerror(err) {
                            return reject({status: 0, body: err});
                        }
                    });
                });

                // behaviors
                saveBtn.onclick = () => {
                    localStorage.setItem(API_KEY_STORAGE, apiInput.value);
                    inlineFeedback.style.color = 'green';
                    inlineFeedback.innerText = 'apiKey saved';
                    setTimeout(() => inlineFeedback.innerText = '', 2000);
                };
                const loadUsers = async () => {
                    listGroup.innerHTML = '';
                    try {
                        const users = await gmGet(buildUrl('/krabot/characteristics/getUsers'));
                        if (!users || users.length === 0) {
                            listGroup.innerHTML = '<div class="list-group-item">Aucun utilisateur enregistré.</div>';
                            return;
                        }
                        users.forEach(u => {
                            const name = sanitizeName(u.name);
                            const pp = (typeof u.pp !== 'undefined' && u.pp != null) ? u.pp : 'n/a';
                            const item = document.createElement('div');
                            item.className = 'list-group-item';
                            item.style.cursor = 'default';
                            item.innerText = `${name} — PP: ${pp}`;
                            // no onclick detail behavior — list is informational only
                            listGroup.appendChild(item);
                        });
                    } catch (e) {
                        // Provide a more informative, sanitized and truncated error message in the panel
                        try {
                            console.error('[Krabot] loadUsers error', e);
                        } catch (_) {
                        }
                        const status = (e && (e.status || e.statusCode)) ? (e.status || e.statusCode) : 0;
                        let body = (e && e.body) ? e.body : (typeof e === 'string' ? e : null);
                        try {
                            if (!body && e && e.responseText) body = e.responseText;
                        } catch (_) {
                        }
                        try {
                            if (body && typeof body !== 'string') body = JSON.stringify(body);
                        } catch (_) {
                            body = String(body);
                        }
                        body = body ? String(body).replace(/</g, '&lt;').slice(0, 300) : 'no-body';
                        listGroup.innerHTML = `<div class="list-group-item text-danger">Erreur lors du chargement (status: ${status}) — ${body}</div>`;
                    }
                };
                refreshBtn.onclick = () => loadUsers();

                // remove duplicate refreshes and observe
                try {
                    const btns = Array.from(panel.querySelectorAll('button')).filter(b => /Rafraîchir/i.test((b.innerText || '').trim()));
                    if (btns.length > 1) btns.slice(1).forEach(b => b.remove());
                } catch (e) {
                }
                try {
                    const obs = new MutationObserver(() => {
                        try {
                            const bs = Array.from(panel.querySelectorAll('button')).filter(b => /Rafraîchir/i.test((b.innerText || '').trim()));
                            if (bs.length > 1) bs.slice(1).forEach(b => b.remove());
                        } catch (e) {
                        }
                    });
                    obs.observe(panel, {childList: true, subtree: true});
                } catch (e) {
                }

                // initial load
                setTimeout(() => loadUsers(), 60);
                return true;
            };

            // try a few times to attach the panel while DOM stabilizes
            let attempts = 0;
            const max = 30;
            const iv = setInterval(() => {
                if (tryCreate() || ++attempts >= max) clearInterval(iv);
            }, 300);
        })();
    }

    // periodic refresh: re-extract and send if changed (call the explicit extractor depending on page)
    setInterval(() => {
        try {
            if (isMobileDevice()) {
                // skip periodic extraction/send on mobile
                return;
            }
            let d = null;
            if (location.pathname.includes('/jouer/plateau')) {
                d = extractPlateau();
            } else if (location.pathname.includes('/profil/interface')) {
                d = extractInterface();
            } else {
                return; // not a page we care about
            }
            if (d && d.playerId) {
                trySend(d);
            } else {
                console.warn('Krabot: No playerId found during periodic refresh');
            }
        } catch (e) {
            console.error('Krabot: Error during periodic refresh', e);
        }
    }, 30000); // 30 seconds
})();