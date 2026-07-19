import {get} from '../common/api.js';
import {all, element, lookup} from '../common/dom.js';
import {badge} from '../common/view.js';

const ACTIVE_CLASSES = ['border-primary', 'bg-accent'];
const CAST_TYPE_LABELS = Object.freeze({
    PRIMARY: '주 출연',
    ALTERNATE: '얼터네이트',
    UNDERSTUDY: '언더스터디',
});

const root = lookup('[data-performance-page]');
const slug = root?.dataset.performanceSlug || '';

function profileImageUrl(cast) {
    const profile = cast.profile;
    if (!profile?.profileFileId) {
        return null;
    }
    return `/api/public-performances/profiles/${profile.publicProfileId}/files/${profile.profileFileId}`;
}

function castCard(cast) {
    const card = element('article', 'flex min-h-24 gap-3 rounded-md border border-sidebar-border bg-sidebar-accent p-3');
    const imageUrl = profileImageUrl(cast);
    if (imageUrl) {
        const image = element('img', 'size-20 shrink-0 rounded-md object-cover');
        image.src = imageUrl;
        image.alt = `${cast.profile.publicName || '출연자'} 프로필`;
        image.loading = 'lazy';
        card.appendChild(image);
    } else {
        const initial = (cast.profile?.publicName || '?').slice(0, 1);
        card.appendChild(element('span', 'flex size-20 shrink-0 items-center justify-center rounded-md bg-sidebar text-2xl font-black text-primary', initial));
    }
    const content = element('div', 'min-w-0 flex-1');
    content.appendChild(element('span', 'block text-xs font-bold text-primary', cast.characterName));
    content.appendChild(element('strong', 'mt-1 block text-sm font-black text-white', cast.profile?.publicName || '출연자 비공개'));
    const type = CAST_TYPE_LABELS[cast.castType];
    if (type && cast.castType !== 'PRIMARY') {
        content.appendChild(badge(type, 'neutral'));
    }
    card.appendChild(content);
    return card;
}

function renderRoundCasts(casts) {
    const host = lookup('[data-round-cast-list]');
    host.replaceChildren();
    if (casts.length === 0) {
        host.appendChild(element('p', 'text-sm text-sidebar-foreground sm:col-span-2', '이 회차의 공개 캐스팅 정보가 아직 없습니다.'));
    } else {
        casts.forEach((cast) => host.appendChild(castCard(cast)));
    }
    lookup('[data-round-cast-panel]').setAttribute('aria-busy', 'false');
}

function setSelectedRound(button) {
    all('[data-round-select]').forEach((candidate) => {
        candidate.classList.remove(...ACTIVE_CLASSES);
        candidate.setAttribute('aria-selected', 'false');
        candidate.tabIndex = -1;
    });
    button.classList.add(...ACTIVE_CLASSES);
    button.setAttribute('aria-selected', 'true');
    button.tabIndex = 0;
    const reservationLink = lookup('[data-round-reservation-link]');
    if (reservationLink) {
        reservationLink.href = `/reserve/${encodeURIComponent(slug)}?round=${encodeURIComponent(button.dataset.roundId)}`;
    }
}

async function loadRoundCasts(button) {
    setSelectedRound(button);
    const panel = lookup('[data-round-cast-panel]');
    const host = lookup('[data-round-cast-list]');
    panel.setAttribute('aria-busy', 'true');
    host.replaceChildren(element('p', 'text-sm text-sidebar-foreground', '회차별 캐스팅을 불러오는 중입니다.'));
    try {
        const casts = await get(`/api/public-performances/${encodeURIComponent(slug)}/rounds/${button.dataset.roundId}/casts`);
        if (button.getAttribute('aria-selected') === 'true') {
            renderRoundCasts(casts);
        }
    } catch (error) {
        host.replaceChildren(element('p', 'text-sm text-warning sm:col-span-2', '회차별 캐스팅을 불러오지 못했습니다.'));
        panel.setAttribute('aria-busy', 'false');
    }
}

function moveRoundFocus(current, direction) {
    const buttons = all('[data-round-select]');
    const index = buttons.indexOf(current);
    const next = buttons[(index + direction + buttons.length) % buttons.length];
    next.focus();
    loadRoundCasts(next);
}

document.addEventListener('click', (event) => {
    const button = event.target.closest('[data-round-select]');
    if (button) {
        loadRoundCasts(button);
    }
});

document.addEventListener('keydown', (event) => {
    const button = event.target.closest('[data-round-select]');
    if (!button || !['ArrowDown', 'ArrowUp', 'ArrowLeft', 'ArrowRight'].includes(event.key)) {
        return;
    }
    event.preventDefault();
    moveRoundFocus(button, ['ArrowDown', 'ArrowRight'].includes(event.key) ? 1 : -1);
});

const firstRound = lookup('[data-round-select]');
if (firstRound && slug) {
    loadRoundCasts(firstRound);
}
