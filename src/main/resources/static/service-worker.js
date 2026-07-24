/* 설치형 앱을 위한 네트워크 전용 서비스 워커다. 오프라인 캐시를 저장하지 않는다. */
self.addEventListener('install', () => {
    self.skipWaiting();
});

self.addEventListener('activate', (event) => {
    event.waitUntil(self.clients.claim());
});

self.addEventListener('fetch', (event) => {
    const requestUrl = new URL(event.request.url);
    if (event.request.method !== 'GET' || requestUrl.origin !== self.location.origin) {
        return;
    }
    event.respondWith(fetch(event.request));
});
