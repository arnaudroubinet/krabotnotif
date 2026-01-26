// ==UserScript==
// @name         Krabot Characteristics Extractor
// @namespace    krabot
// @version      ${project.version}
// @description  Extract character name, playerId and PP and POST to backend only when changed
// @match        *://www.kraland.org/jouer/plateau
// @match        *://www.kraland.org/profil/interface
// @grant        GM_xmlhttpRequest
// @connect      __BACKEND_HOST__
// @connect      __BACKEND_HOST_NO_PORT__
// @connect      127.0.0.1
// ==/UserScript==

(function() {
    'use strict';

    const BACKEND_URL = '__BACKEND_URL__';
    const API_KEY_STORAGE = 'krabot_api_key';

    function getApiKey() { return localStorage.getItem(API_KEY_STORAGE); }
    function getSnapshotKey() { const k = getApiKey(); return 'krabot_snapshot_' + (k ? k : 'default'); }

    // Snapshot storage with TTL (1 hour)
    const SNAPSHOT_TTL_MS = 60 * 60 * 1000; // 1 hour

    // Detect mobile devices (userAgent OR small viewport)
    function isMobileDevice() {
        try {
            const ua = (navigator && navigator.userAgent) ? navigator.userAgent : '';
            const uaMobile = /Mobi|Android|iPhone|iPad|iPod|Mobile/i.test(ua);
            const smallViewport = (window.matchMedia && window.matchMedia('(max-width:767px)').matches) || false;
            return !!(uaMobile || smallViewport);
        } catch (e) { return false; }
    }

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
                try { localStorage.removeItem(key); } catch (_) {}
                return null;
            }
            // Expecting stored shape: { data: {...}, savedAt: 123456789 }
            if (!parsed || typeof parsed !== 'object' || !parsed.hasOwnProperty('savedAt') || parsed.data === undefined) {
                // incompatible shape -> remove it
                try { localStorage.removeItem(key); } catch (_) {}
                return null;
            }
            const savedAt = Number(parsed.savedAt) || 0;
            const age = Date.now() - savedAt;
            if (age <= SNAPSHOT_TTL_MS && savedAt > 0) return parsed.data;
            // expired or invalid savedAt -> remove and return null
            try { localStorage.removeItem(key); } catch (_) {}
            return null;
        } catch (e) {
            // unexpected error -> attempt to remove and return null
            try { localStorage.removeItem(getSnapshotKey()); } catch (_) {}
            return null;
        }
    }

    function saveSnapshot(snapshot) {
        try {
            const payload = { data: snapshot, savedAt: Date.now() };
            localStorage.setItem(getSnapshotKey(), JSON.stringify(payload));
        } catch(e) { console.error('Failed to save snapshot', e); }
    }

    function extractPlateau() {
        // Simple extraction for plateau: name, playerId, pp (desktop-only)
        let name = '';

        // name: read from list-group header (active span or first available)
        try {
            const span = document.querySelector('div.list-group span.list-group-item.active')
                || document.querySelector('div.list-group span.list-group-item')
                || document.querySelector('div.list-group span');
            if (span && (span.innerText || '').trim()) {
                name = (span.innerText || '').replace(/\u00A0/g, ' ').trim().replace(/^[^A-Za-zÀ-ÖØ-öø-ÿ]+/, '').replace(/\s+/g, ' ');
            }
        } catch (e) { /* ignore */ }

        // playerId: prefer community member anchors, otherwise profile anchors; take last numeric group
        let playerId = null;
        try {
            const member = Array.from(document.querySelectorAll('a[href*="communaute/membres/"]'));
            for (const a of member) {
                const href = a.getAttribute('href') || '';
                if (!href || href.includes('/edit')) continue;
                const nums = (href.match(/([0-9]+)/g) || []);
                if (nums.length) { playerId = nums[nums.length - 1]; break; }
            }
            if (!playerId) {
                const profiles = Array.from(document.querySelectorAll('a[href*="/profil/"]'));
                for (const a of profiles) {
                    const href = a.getAttribute('href') || '';
                    const nums = (href.match(/([0-9]+)/g) || []);
                    if (nums.length) { playerId = nums[nums.length - 1]; break; }
                }
            }
        } catch (e) { /* ignore */ }

        // PP: find the anchor whose title starts with 'Puissance Politique' and extract last number from its visible text
        let pp = null;
        try {
            const anchors = Array.from(document.querySelectorAll('a[title]'));
            const target = anchors.find(a => { const t = (a.getAttribute('title')||'').replace(/\u00A0/g,' ').trim(); return /^Puissance\s+Politique\b/i.test(t); });
            if (target) {
                const text = (target.innerText || '').replace(/\u00A0/g,' ').replace(/\s+/g,' ').trim();
                const nums = text.match(/([0-9]+)/g);
                if (nums && nums.length) {
                    pp = parseInt(nums[nums.length - 1], 10);
                    if (Number.isNaN(pp)) pp = null;
                }
            }
        } catch (e) { /* ignore */ }

        return { name, playerId, pp };
    }

    function extractInterface() {
        // Simple extractor for /profil/interface: name, playerId, pp (desktop-only)
        let name = '';

        // name from list-group header
        try {
            const span = document.querySelector('div.list-group span.list-group-item.active')
                || document.querySelector('div.list-group span.list-group-item')
                || document.querySelector('div.list-group span');
            if (span && (span.innerText || '').trim()) {
                name = (span.innerText || '').replace(/\u00A0/g, ' ').trim().replace(/^[^A-Za-zÀ-ÖØ-öø-ÿ]+/, '').replace(/\s+/g, ' ');
            }
        } catch (e) { /* ignore */ }

        // playerId: prefer profile anchors on interface page, take last numeric group
        let playerId = null;
        try {
            const profileAnchors = Array.from(document.querySelectorAll('a[href*="/profil/"]'));
            for (const a of profileAnchors) {
                const href = a.getAttribute('href') || '';
                const nums = (href.match(/([0-9]+)/g) || []);
                if (nums.length) { playerId = nums[nums.length - 1]; break; }
            }
        } catch (e) { /* ignore */ }

        // PP: find anchor with title starting 'Puissance Politique' and extract last visible number
        let pp = null;
        try {
            const anchors = Array.from(document.querySelectorAll('a[title]'));
            const target = anchors.find(a => { const t = (a.getAttribute('title')||'').replace(/\u00A0/g,' ').trim(); return /^Puissance\s+Politique\b/i.test(t); });
            if (target) {
                const text = (target.innerText || '').replace(/\u00A0/g,' ').replace(/\s+/g,' ').trim();
                const nums = text.match(/([0-9]+)/g);
                if (nums && nums.length) {
                    pp = parseInt(nums[nums.length - 1], 10);
                    if (Number.isNaN(pp)) pp = null;
                }
            }
        } catch (e) { /* ignore */ }

        return { name, playerId, pp };
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
        GM_xmlhttpRequest({ method: 'POST', url: url, headers: {'Content-Type': 'application/json'}, data: JSON.stringify({playerId: data.playerId, name: data.name, pp: data.pp}), onload(resp) { console.log('[Krabot] characteristics sent', resp.status); }, onerror(err) { console.error('[Krabot] failed to send characteristics', err); } });
    }

    function trySend(data) {
        if (!data || !data.playerId) { console.warn('Krabot: Unable to find playerId', data); return; }
        // sanitize name and reject obvious false positives
        const bannedNames = ['Krabot - Caractéristiques', 'Krabot - Caracteristiques', '(no name)'];
        const name = (data.name || '').toString().trim();
        if (!name || bannedNames.includes(name)) { console.warn('Krabot: refusing to send empty or invalid name', name); return; }
        if (shouldSend(data)) { send(data); try { saveSnapshot({ playerId: data.playerId, name: data.name, pp: data.pp }); } catch(e) {} console.log('[Krabot] snapshot updated'); } else { console.log('[Krabot] no change detected, send ignored'); }
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
            window.__krabot.extract = function() {
                if (location.pathname.includes('/jouer/plateau')) return extractPlateau();
                if (location.pathname.includes('/profil/interface')) return extractInterface();
                return extractPlateau();
            };
        }
    } catch(e) {}

    // On plateau page: perform one immediate extract and send only when necessary (skip on mobile)
    if (location.pathname.includes('/jouer/plateau')) {
        if (isMobileDevice()) {
            console.log('[Krabot] mobile detected — skipping automatic extraction/send on plateau');
        } else {
            try {
                const d = extractPlateau();
                const snap = loadSnapshot();
                // send if no snapshot or if the detected values differ from the snapshot
                if (!snap) {
                    console.log('[Krabot] no snapshot found — sending initial data', d);
                    trySend(d);
                } else if (snap.playerId !== d.playerId || snap.name !== d.name || snap.pp !== d.pp) {
                    console.log('[Krabot] change detected vs snapshot — updating', { from: snap, to: d });
                    trySend(d);
                } else {
                    console.log('[Krabot] snapshot up-to-date — no initial send');
                }
            } catch (e) { console.error('[Krabot] failed to perform initial plateau send', e); }
        }
    }

    if (location.pathname.includes('/profil/interface')) {
        (function initPanelSimple(){
            // if panel exists, ensure there's only one Refresh button and observe for late injectors
            const existing = document.querySelector('.krabot-panel');
            if (existing) {
                try {
                    const btns = Array.from(existing.querySelectorAll('button')).filter(b => /Rafraîchir/i.test((b.innerText||'').trim()));
                    if (btns.length > 1) btns.slice(1).forEach(b => b.remove());
                    const ob = new MutationObserver(() => {
                        const bs = Array.from(existing.querySelectorAll('button')).filter(b => /Rafraîchir/i.test((b.innerText||'').trim()));
                        if (bs.length > 1) bs.slice(1).forEach(b => b.remove());
                    });
                    ob.observe(existing, { childList: true, subtree: true });
                } catch(e) { /* ignore */ }
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
                const heading = document.createElement('div'); heading.className = (template && (template.querySelector('.panel-heading')||template.querySelector('.card-header')) ? (template.querySelector('.panel-heading')||template.querySelector('.card-header')).className : 'panel-heading');
                heading.innerHTML = '<h3 class="panel-title"><i class="fa fa-list"></i> Krabot - Caractéristiques</h3>';
                panel.appendChild(heading);
                const body = document.createElement('div'); body.className = (template && (template.querySelector('.panel-body')||template.querySelector('.card-body')) ? (template.querySelector('.panel-body')||template.querySelector('.card-body')).className : 'panel-body');
                panel.appendChild(body);

                // header refresh
                const right = document.createElement('span'); right.className = 'pull-right clickable';
                const refreshBtn = document.createElement('button'); refreshBtn.className = 'btn btn-default btn-xs'; refreshBtn.style.marginLeft = '6px'; refreshBtn.innerText = 'Rafraîchir';
                right.appendChild(refreshBtn); heading.appendChild(right);

                // body content
                const apiInput = document.createElement('input'); apiInput.className = 'form-control'; apiInput.placeholder = 'apiKey UUID (optionnel)'; apiInput.value = localStorage.getItem(API_KEY_STORAGE) || '';
                const saveBtn = document.createElement('button'); saveBtn.className = 'btn btn-default btn-xs'; saveBtn.style.marginTop = '6px'; saveBtn.innerText = 'Save apiKey';
                const inlineFeedback = document.createElement('div'); inlineFeedback.style.marginTop = '6px'; inlineFeedback.style.fontSize = '12px';
                const listGroup = document.createElement('div'); listGroup.className = 'list-group'; listGroup.style.marginTop = '8px'; listGroup.style.maxHeight = '400px'; listGroup.style.overflow = 'auto';
                body.appendChild(apiInput); body.appendChild(saveBtn); body.appendChild(inlineFeedback); body.appendChild(listGroup);

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
                const buildUrl = (path, params) => { const apiKey = localStorage.getItem(API_KEY_STORAGE); const u = new URL((BACKEND_URL || '') + path); if (apiKey) u.searchParams.set('apiKey', apiKey); if (params) Object.keys(params).forEach(k => { if (params[k] != null) u.searchParams.set(k, params[k]); }); return u.toString(); };
                const gmGet = (url) => new Promise((resolve, reject) => { GM_xmlhttpRequest({ method:'GET', url, onload(resp){ try{ if (resp.status>=200 && resp.status<300) return resolve(resp.responseText ? JSON.parse(resp.responseText) : null); return reject({status:resp.status, body:resp.responseText}); }catch(e){ return reject({status:resp.status||0, body:'invalid-json'}); } }, onerror(err){ return reject({status:0, body:err}); } }); });

                // behaviors
                saveBtn.onclick = () => { localStorage.setItem(API_KEY_STORAGE, apiInput.value); inlineFeedback.style.color='green'; inlineFeedback.innerText='apiKey saved'; setTimeout(()=> inlineFeedback.innerText='',2000); };
                const loadUsers = async () => {
                    listGroup.innerHTML='';
                    try {
                        const users = await gmGet(buildUrl('/krabot/characteristics/getUsers'));
                        if (!users || users.length===0) { listGroup.innerHTML = '<div class="list-group-item">Aucun utilisateur enregistré.</div>'; return; }
                        users.forEach(u => {
                            const name = sanitizeName(u.name);
                            const pp = (typeof u.pp !== 'undefined' && u.pp!=null) ? u.pp : 'n/a';
                            const item = document.createElement('div');
                            item.className='list-group-item';
                            item.style.cursor='default';
                            item.innerText = `${name} — PP: ${pp}`;
                            // no onclick detail behavior — list is informational only
                            listGroup.appendChild(item);
                        });
                    } catch(e) {
                        // Provide a more informative, sanitized and truncated error message in the panel
                        try { console.error('[Krabot] loadUsers error', e); } catch(_) {}
                        const status = (e && (e.status || e.statusCode)) ? (e.status || e.statusCode) : 0;
                        let body = (e && e.body) ? e.body : (typeof e === 'string' ? e : null);
                        try { if (!body && e && e.responseText) body = e.responseText; } catch(_) {}
                        try { if (body && typeof body !== 'string') body = JSON.stringify(body); } catch(_) { body = String(body); }
                        body = body ? String(body).replace(/</g,'&lt;').slice(0,300) : 'no-body';
                        listGroup.innerHTML = `<div class="list-group-item text-danger">Erreur lors du chargement (status: ${status}) — ${body}</div>`;
                    }
                };
                refreshBtn.onclick = () => loadUsers();

                // remove duplicate refreshes and observe
                try { const btns = Array.from(panel.querySelectorAll('button')).filter(b=>/Rafraîchir/i.test((b.innerText||'').trim())); if (btns.length>1) btns.slice(1).forEach(b=>b.remove()); } catch(e){}
                try { const obs = new MutationObserver(()=>{ try{ const bs=Array.from(panel.querySelectorAll('button')).filter(b=>/Rafraîchir/i.test((b.innerText||'').trim())); if(bs.length>1) bs.slice(1).forEach(b=>b.remove()); }catch(e){} }); obs.observe(panel, {childList:true, subtree:true}); } catch(e){}

                // initial load
                setTimeout(() => loadUsers(), 60);
                return true;
            };

            // try a few times to attach the panel while DOM stabilizes
            let attempts = 0; const max = 30; const iv = setInterval(()=>{ if (tryCreate() || ++attempts>=max) clearInterval(iv); }, 300);
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
        } catch(e) {
            console.error('Krabot: Error during periodic refresh', e);
        }
    }, 30000); // 30 seconds
 })();