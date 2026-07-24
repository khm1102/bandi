async function registerServiceWorker() {
    if (!('serviceWorker' in navigator) || !window.isSecureContext) {
        return;
    }
    try {
        await navigator.serviceWorker.register('/service-worker.js', {scope: '/'});
    } catch {
        // 설치형 앱 등록 실패는 일반 웹 사용을 막지 않는다.
    }
}

window.addEventListener('load', registerServiceWorker, {once: true});
