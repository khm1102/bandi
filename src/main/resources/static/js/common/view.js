import {all, element} from './dom.js';
import {closeModal} from './modal.js';

const CHIP_ACTIVE = ['border-sidebar', 'bg-sidebar', 'text-white'];
const CHIP_INACTIVE = ['bg-card', 'text-muted-foreground', 'hover:border-sidebar-muted'];

export function closeActionModal(trigger) {
    closeModal(trigger.closest('[data-modal-back]'));
}

export function activateFilterChip(button) {
    const group = button.dataset.filterGroup;
    if (!group) {
        return;
    }
    all('[data-filter-group]').filter((candidate) => candidate.dataset.filterGroup === group).forEach((candidate) => {
        candidate.classList.remove(...CHIP_ACTIVE);
        candidate.classList.add(...CHIP_INACTIVE);
        candidate.setAttribute('aria-pressed', 'false');
    });
    button.classList.remove(...CHIP_INACTIVE);
    button.classList.add(...CHIP_ACTIVE);
    button.setAttribute('aria-pressed', 'true');
}

export function badge(text, tone = 'neutral') {
    const tones = {
        accent: 'bg-accent text-accent-foreground',
        success: 'bg-success-soft text-success',
        warning: 'bg-warning-soft text-warning',
        danger: 'bg-destructive-soft text-destructive',
        info: 'bg-info-soft text-info',
        neutral: 'bg-secondary text-muted-foreground'
    };
    const classes = 'inline-flex items-center rounded-full px-2.5 py-1 text-xs font-extrabold';
    return element('span', `${classes} ${tones[tone]}`, text);
}

export function today() {
    const now = new Date();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    return `${month}/${day}`;
}
