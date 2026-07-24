/* 설치형 앱 등록만 담당하며, 오프라인 캐시와 요청 가로채기를 사용하지 않는다. */
self.addEventListener('install', () => {
    self.skipWaiting();
});

self.addEventListener('activate', (event) => {
    event.waitUntil(self.clients.claim());
});
