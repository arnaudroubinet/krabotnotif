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
            const lines = container.innerText.split(/\n+/).map(s => s.trim()).filter(Boolean);
            for (const l of lines) {
                // look for a line starting with letter and containing a space (first and last name)
                if (/^[A-Za-zÀ-ÖØ-öø-ÿ].+ .+$/.test(l) && l.length < 60) { name = l; break; }
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
        if (shouldSend(data)) { send(data); try { saveSnapshot({ playerId: data.playerId, name: data.name, pp: data.pp }); } catch(e) {} console.log('[Krabot] snapshot updated'); } else { console.log('[Krabot] no change detected, send ignored'); }
    }

    // Expose debug helpers
    try { if (typeof window !== 'undefined') { window.__krabot = window.__krabot || {}; window.__krabot.extract = extract; window.__krabot.trySend = trySend; window.__krabot.getSnapshotKey = getSnapshotKey; } } catch(e) {}

    if (location.pathname.includes('/jouer/plateau')) { const btn = document.createElement('button'); btn.innerText = 'Send characteristics'; btn.style.position = 'fixed'; btn.style.bottom = '10px'; btn.style.right = '10px'; btn.style.zIndex = 9999; btn.onclick = () => { const d = extract(); console.log('extracted', d); trySend(d); }; document.body.appendChild(btn); }

    if (location.pathname.includes('/profil/interface')) { const container = document.createElement('div'); container.style.position = 'fixed'; container.style.bottom = '10px'; container.style.left = '10px'; container.style.zIndex = 9999; container.style.background = 'white'; container.style.padding='8px'; const input = document.createElement('input'); input.placeholder = 'apiKey UUID'; input.value = localStorage.getItem(API_KEY_STORAGE) || ''; const save = document.createElement('button'); save.innerText = 'Save apiKey'; save.onclick = () => { localStorage.setItem(API_KEY_STORAGE, input.value); alert('Saved'); }; container.appendChild(input); container.appendChild(save); document.body.appendChild(container); }

})();
