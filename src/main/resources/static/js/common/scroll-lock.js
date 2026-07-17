const scrollLockOwners = new Set();

function syncBodyScroll() {
    document.body.classList.toggle('overflow-hidden', scrollLockOwners.size > 0);
}

export function lockBodyScroll(owner) {
    if (!owner) {
        return;
    }
    scrollLockOwners.add(owner);
    syncBodyScroll();
}

export function unlockBodyScroll(owner) {
    scrollLockOwners.delete(owner);
    syncBodyScroll();
}
