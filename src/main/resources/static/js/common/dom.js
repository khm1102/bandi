export function all(selector, root = document) {
    return Array.from(root.querySelectorAll(selector));
}

export function lookup(selector, root = document) {
    return root.querySelector(selector);
}

export function readValue(id) {
    const input = document.getElementById(id);
    return input ? input.value.trim() : '';
}

export function element(tagName, className, text) {
    const node = document.createElement(tagName);
    if (className) {
        node.className = className;
    }
    if (text !== undefined) {
        node.textContent = text;
    }
    return node;
}

export function appendCell(row, text, className = '') {
    const cell = element('td', className, text);
    row.appendChild(cell);
    return cell;
}

export function debounce(handler, delay = 300) {
    let timeoutId;
    return (...args) => {
        window.clearTimeout(timeoutId);
        timeoutId = window.setTimeout(() => handler(...args), delay);
    };
}

export function bindPageActions(actions, root = document) {
    root.addEventListener('click', (event) => {
        const trigger = event.target.closest('[data-page-action]');
        if (!trigger || !root.contains(trigger)) {
            return;
        }
        const handler = actions[trigger.dataset.pageAction];
        if (handler) {
            handler(trigger);
        }
    });
}
