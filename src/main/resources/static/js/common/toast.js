const TOAST_ID = 'toast';
const TOAST_VISIBLE_MS = 2400;
const TOAST_BASE_CLASS = 'fixed bottom-8 left-1/2 z-50 -translate-x-1/2 rounded-lg bg-sidebar px-5 py-3 text-sm font-bold text-white shadow-lg transition duration-300';

let hideTimerId = null;

function lookupToastElement() {
    const existing = document.getElementById(TOAST_ID);
    if (existing) {
        return existing;
    }
    const toast = document.createElement('div');
    toast.id = TOAST_ID;
    toast.className = `${TOAST_BASE_CLASS} pointer-events-none translate-y-4 opacity-0`;
    toast.setAttribute('role', 'status');
    document.body.appendChild(toast);
    return toast;
}

export function showToast(message) {
    const toast = lookupToastElement();
    toast.textContent = message;
    toast.classList.remove('translate-y-4', 'opacity-0');
    if (hideTimerId !== null) {
        clearTimeout(hideTimerId);
    }
    hideTimerId = setTimeout(() => {
        toast.classList.add('translate-y-4', 'opacity-0');
        hideTimerId = null;
    }, TOAST_VISIBLE_MS);
}

// 리다이렉트 후 flash 메시지 자동 표시 — layout이 addFlashAttribute("toast", ...)를 data 속성으로 싣는다
const flash = document.querySelector('[data-flash-toast]');
if (flash) {
    showToast(flash.dataset.flashToast);
}
