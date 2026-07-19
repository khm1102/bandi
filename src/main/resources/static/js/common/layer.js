const FOCUSABLE_SELECTOR = 'a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])';

const layers = [];

function isRendered(element) {
    return element.getClientRects().length > 0;
}

export function focusables(container) {
    return Array.from(container.querySelectorAll(FOCUSABLE_SELECTOR)).filter(isRendered);
}

export function pushLayer(element, close) {
    removeLayer(element);
    layers.push({element, close});
}

export function removeLayer(element) {
    const index = layers.findIndex((layer) => layer.element === element);
    if (index >= 0) {
        layers.splice(index, 1);
    }
}

export function isTopLayer(element) {
    return layers.length > 0 && layers[layers.length - 1].element === element;
}

document.addEventListener('keydown', (event) => {
    const top = layers[layers.length - 1];
    if (!top) {
        return;
    }
    if (event.key === 'Escape') {
        event.preventDefault();
        top.close();
        return;
    }
    if (event.key !== 'Tab') {
        return;
    }
    const items = focusables(top.element);
    if (items.length === 0) {
        event.preventDefault();
        return;
    }
    const first = items[0];
    const last = items[items.length - 1];
    const outside = !top.element.contains(document.activeElement);
    if (event.shiftKey && (outside || document.activeElement === first)) {
        event.preventDefault();
        last.focus();
        return;
    }
    if (!event.shiftKey && (outside || document.activeElement === last)) {
        event.preventDefault();
        first.focus();
    }
});
