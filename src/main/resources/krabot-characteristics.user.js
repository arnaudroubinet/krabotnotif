// ==UserScript==
// @name         Krabot Characteristics Extractor
// @namespace    krabot
// @version      1
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

    function loadSnapshot() {
        try { const raw = localStorage.getItem(getSnapshotKey()); return raw ? JSON.parse(raw) : null; } catch (e) { return null; }
    }

    function saveSnapshot(snapshot) {
        try { localStorage.setItem(getSnapshotKey(), JSON.stringify(snapshot)); } catch(e) { console.error('Failed to save snapshot', e); }
    }

    function extract() {
        const container = document.querySelector('div.col-md-3.sidebar') || document.querySelector('#content') || document.body;
        let name = '';
        if (container) {
            // Temporarily hide our injected panel so its heading won't be picked up by heuristics
            const panel = document.querySelector('.krabot-panel');
            const prevVis = panel ? panel.style.visibility : null;
            if (panel) panel.style.visibility = 'hidden';
            try {
                const lines = container.innerText.split(/\n+/).map(s => s.trim()).filter(Boolean);
                for (const l of lines) {
                    // look for a line starting with letter and containing a space (first and last name)
                    if (/^[A-Za-zÀ-ÖØ-öø-ÿ].+ .+$/.test(l) && l.length < 60) { name = l; break; }
                }
            } finally {
                if (panel) panel.style.visibility = prevVis || '';
            }
        }

        let playerId = null;
        const memberAnchors = Array.from(document.querySelectorAll('a[href*="communaute/membres/"]'));
        for (const a of memberAnchors) {
            const href = a.getAttribute('href') || '';
            if (!href || href.includes('/edit')) continue;
            try {
                const dashMatches = Array.from(href.matchAll(/-([0-9]+)/g)).map(m => m[1]);
                if (dashMatches.length) {
                    const candidate = dashMatches.slice().reverse().find(s => s.length >= 3) || dashMatches[dashMatches.length - 1];
                    if (candidate) { playerId = candidate; break; }
                }
            } catch(e) { /* ignore */ }
            const all = href.match(/([0-9]+)/g);
            if (all && all.length) { playerId = all[all.length - 1]; break; }
        }
        if (!playerId) {
            const profileAnchors = Array.from(document.querySelectorAll('a[href*="/profil/"]'));
            for (const a of profileAnchors) { const href = a.getAttribute('href') || ''; const all = href.match(/([0-9]+)/g); if (all && all.length) { playerId = all[all.length - 1]; break; } }
        }

        let pp = null;
        const ppCandidates = Array.from(document.querySelectorAll('*')).filter(e => (e.innerText || '').includes('PP'));
        for (const c of ppCandidates) { const mm = (c.innerText || '').match(/([0-9]+)/); if (mm) { pp = parseInt(mm[1], 10); break; } }

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

    // Expose debug helpers
    try { if (typeof window !== 'undefined') { window.__krabot = window.__krabot || {}; window.__krabot.extract = extract; window.__krabot.trySend = trySend; window.__krabot.getSnapshotKey = getSnapshotKey; } } catch(e) {}

    if (location.pathname.includes('/jouer/plateau')) { const btn = document.createElement('button'); btn.innerText = 'Send characteristics'; btn.style.position = 'fixed'; btn.style.bottom = '10px'; btn.style.right = '10px'; btn.style.zIndex = 9999; btn.onclick = () => { const d = extract(); console.log('extracted', d); trySend(d); }; document.body.appendChild(btn); }

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
                    } catch(e) { listGroup.innerHTML = '<div class="list-group-item text-danger">Erreur lors du chargement</div>'; }
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

    // initial extraction and send when script loads
    const initialData = extract();
    console.log('initial extract', initialData);

    // periodic refresh: re-extract and send if changed
    setInterval(() => {
        try {
            const d = extract();
            if (d.playerId) {
                trySend(d);
            } else {
                console.warn('Krabot: No playerId found during periodic refresh');
            }
        } catch(e) {
            console.error('Krabot: Error during periodic refresh', e);
        }
    }, 30000); // 30 seconds
})();
