const ALLOWED_TAGS = new Set(['A', 'BLOCKQUOTE', 'BR', 'CODE', 'DEL', 'EM', 'H1', 'H2',
    'H3', 'H4', 'H5', 'H6', 'HR', 'LI', 'OL', 'P', 'PRE', 'STRONG', 'TABLE', 'TBODY',
    'TD', 'TH', 'THEAD', 'TR', 'UL']);

function cloneSafeNode(node) {
    if (node.nodeType === Node.TEXT_NODE) return document.createTextNode(node.textContent);
    if (node.nodeType !== Node.ELEMENT_NODE || !ALLOWED_TAGS.has(node.tagName)) return null;
    const copy = document.createElement(node.tagName.toLowerCase());
    if (node.tagName === 'A') {
        const href = node.getAttribute('href');
        if (href) copy.setAttribute('href', href);
        copy.setAttribute('target', '_blank');
        copy.setAttribute('rel', 'noopener noreferrer');
    }
    node.childNodes.forEach((child) => {
        const safeChild = cloneSafeNode(child);
        if (safeChild) copy.appendChild(safeChild);
    });
    return copy;
}

export function mountSafeHtml(container, html) {
    const documentFragment = new DOMParser().parseFromString(html || '', 'text/html');
    const nodes = [];
    documentFragment.body.childNodes.forEach((node) => {
        const safeNode = cloneSafeNode(node);
        if (safeNode) nodes.push(safeNode);
    });
    container.replaceChildren(...nodes);
}
