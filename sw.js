// Offline cache for the installable web app: network first (always get the newest version), cache as fallback
const CACHE='spike-legends-v22';
const FILES=['./','./index.html','./manifest.webmanifest','./icons/icon-192.png','./icons/icon-512.png','./icons/icon-180.png','./icons/icon-32.png','./icons/icon-maskable-512.png'];
self.addEventListener('install',e=>{ e.waitUntil(caches.open(CACHE).then(c=>Promise.all(FILES.map(f=>fetch(f,{cache:'reload'}).then(r=>c.put(f,r)).catch(()=>{}))))); self.skipWaiting(); });
self.addEventListener('activate',e=>{ e.waitUntil(caches.keys().then(ks=>Promise.all(ks.filter(k=>k!==CACHE).map(k=>caches.delete(k))))); self.clients.claim(); });
self.addEventListener('fetch',e=>{ if(e.request.method!=='GET') return;
  e.respondWith(fetch(e.request,{cache:'no-cache'}).then(r=>{ if(r&&r.ok&&new URL(e.request.url).origin===location.origin){ const cp=r.clone(); caches.open(CACHE).then(c=>c.put(e.request,cp)); } return r; }).catch(()=>caches.match(e.request).then(r=>r||caches.match('./index.html')))); });
