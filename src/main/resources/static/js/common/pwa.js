const serviceWorkerSupported = 'serviceWorker' in navigator;

async function registerServiceWorker() {
    if (!serviceWorkerSupported || !window.isSecureContext) {
        return;
    }
    try {
        const serviceWorkerUrl = new URL('../../service-worker.js', import.meta.url);
        const scopeUrl = new URL('../../', import.meta.url);
        await navigator.serviceWorker.register(serviceWorkerUrl, {scope: scopeUrl.pathname});
    } catch {
        // PWA 설치 지원 실패는 서비스 이용을 막지 않는다.
    }
}

registerServiceWorker();
