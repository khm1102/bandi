import {get, post} from '../common/api.js';
import {showToast} from '../common/toast.js';
import {bindPageActions, element, lookup, readValue} from '../common/dom.js';
import {badge} from '../common/view.js';
import qrcode from '../vendor/qrcode-generator.js';

const ACTIONS = Object.freeze({
    COPY_LOOKUP_TOKEN: 'copy-lookup-token',
    DOWNLOAD_ENTRY_QR: 'download-entry-qr',
});

const ROUND_STATUS_LABELS = Object.freeze({
    SCHEDULED: '예정',
    RESERVATION_OPEN: '신청 가능',
    RESERVATION_CLOSED: '신청 마감',
    ENTRY_OPEN: '입장 진행',
    ENDED: '종료',
    CANCELLED: '취소',
});

const slug = decodeURIComponent(window.location.pathname.split('/').filter(Boolean).at(-1) || '');
const state = {
    page: null,
    rounds: [],
    policy: null,
    selectedRoundId: null,
    seats: [],
    selectedSeatIds: new Set(),
    createdLookupToken: null,
    entryQrUrl: null,
};

function formatDateTime(value) {
    if (!value) {
        return '-';
    }
    return new Intl.DateTimeFormat('ko-KR', {
        year: 'numeric',
        month: 'long',
        day: 'numeric',
        weekday: 'short',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
    }).format(new Date(value));
}

function selectedRound() {
    return state.rounds.find((round) => round.performanceRoundId === state.selectedRoundId) || null;
}

function roundButton(round) {
    const open = round.status === 'RESERVATION_OPEN';
    const selected = round.performanceRoundId === state.selectedRoundId;
    const button = element('button', `min-h-20 rounded-lg border p-4 text-left transition-colors ${selected ? 'border-primary bg-accent' : 'bg-card'} ${open ? 'hover:border-primary' : 'cursor-not-allowed opacity-60'}`);
    button.type = 'button';
    button.disabled = !open;
    button.dataset.roundId = String(round.performanceRoundId);
    button.setAttribute('aria-pressed', String(selected));
    const heading = element('span', 'flex flex-wrap items-center gap-2');
    heading.appendChild(element('strong', 'text-sm font-black', `${round.roundNo}회차`));
    heading.appendChild(badge(ROUND_STATUS_LABELS[round.status] || round.status,
            open ? 'success' : round.status === 'CANCELLED' ? 'danger' : 'neutral'));
    button.appendChild(heading);
    button.appendChild(element('span', 'mt-2 block text-xs text-muted-foreground', formatDateTime(round.startDttm)));
    if (round.accessibilities.length > 0) {
        const support = element('span', 'mt-2 flex flex-wrap gap-1');
        round.accessibilities.forEach((item) => support.appendChild(badge(item.title, 'info')));
        button.appendChild(support);
    }
    return button;
}

function renderRounds() {
    const host = lookup('[data-round-list]');
    host.replaceChildren();
    if (state.rounds.length === 0) {
        host.appendChild(element('p', 'text-sm text-muted-foreground sm:col-span-2', '공개된 공연 회차가 없습니다.'));
        return;
    }
    state.rounds.forEach((round) => host.appendChild(roundButton(round)));
}

function seatButton(seat, fallbackIndex) {
    const selected = state.selectedSeatIds.has(seat.performanceRoundSeatId);
    const button = element('button', `flex size-11 items-center justify-center rounded-md border text-xs font-extrabold transition-colors focus-visible:ring-2 focus-visible:ring-ring ${selected ? 'border-primary bg-primary text-primary-foreground' : 'bg-card text-muted-foreground hover:border-primary hover:bg-accent hover:text-accent-foreground'}`, seat.seatLabel);
    button.type = 'button';
    button.dataset.seatId = String(seat.performanceRoundSeatId);
    button.setAttribute('aria-pressed', String(selected));
    button.setAttribute('aria-label', `${seat.seatLabel} 좌석${seat.accessibilityCode ? `, ${seat.accessibilityCode}` : ''}`);
    const displayRow = Number.isInteger(seat.displayRow) ? seat.displayRow + 1 : 1;
    const displayColumn = Number.isInteger(seat.displayColumn) ? seat.displayColumn + 1 : fallbackIndex + 1;
    button.style.gridRow = String(displayRow);
    button.style.gridColumn = String(displayColumn);
    return button;
}

function renderSelectedSeats() {
    const host = lookup('[data-selected-seats]');
    host.replaceChildren();
    const selected = state.seats.filter((seat) => state.selectedSeatIds.has(
            seat.performanceRoundSeatId));
    if (selected.length === 0) {
        host.appendChild(element('span', 'text-xs text-muted-foreground', '아직 선택한 좌석이 없습니다.'));
    } else {
        selected.forEach((seat) => host.appendChild(badge(seat.seatLabel, 'accent')));
    }
    const round = selectedRound();
    lookup('[data-reservation-summary]').textContent = round
            ? `${round.roundNo}회차 · ${formatDateTime(round.startDttm)} · ${selected.length}석`
            : '회차와 좌석을 선택해 주세요.';
}

function renderSeats() {
    const host = lookup('[data-seat-map]');
    host.replaceChildren();
    if (state.seats.length === 0) {
        host.style.gridTemplateColumns = 'minmax(16rem, 1fr)';
        host.appendChild(element('p', 'py-8 text-center text-sm text-muted-foreground', '현재 신청 가능한 좌석이 없습니다.'));
    } else {
        const maxColumn = Math.max(...state.seats.map((seat, index) =>
            Number.isInteger(seat.displayColumn) ? seat.displayColumn + 1 : index + 1));
        host.style.gridTemplateColumns = `repeat(${maxColumn}, 2.75rem)`;
        state.seats.forEach((seat, index) => host.appendChild(seatButton(seat, index)));
    }
    lookup('[data-seat-remaining]').replaceChildren(badge(`신청 가능 ${state.seats.length}석`, state.seats.length > 0 ? 'success' : 'warning'));
    const round = selectedRound();
    lookup('[data-seat-guidance]').textContent = round
            ? `${formatDateTime(round.startDttm)} · 좌석을 눌러 선택하세요.`
            : '회차를 선택해 주세요.';
    renderSelectedSeats();
}

async function loadSeats(roundId) {
    state.selectedRoundId = roundId;
    state.selectedSeatIds.clear();
    renderRounds();
    lookup('[data-seat-map]').setAttribute('aria-busy', 'true');
    const seats = await get(`/api/public-reservations/${encodeURIComponent(slug)}/rounds/${roundId}/seats`);
    if (state.selectedRoundId !== roundId) {
        return;
    }
    state.seats = seats;
    renderSeats();
    lookup('[data-seat-map]').setAttribute('aria-busy', 'false');
}

function toggleSeat(button) {
    const seatId = Number(button.dataset.seatId);
    if (state.selectedSeatIds.has(seatId)) {
        state.selectedSeatIds.delete(seatId);
    } else {
        state.selectedSeatIds.add(seatId);
    }
    renderSeats();
}

function showFeedback(message, success = false) {
    const feedback = lookup('[data-reservation-feedback]');
    feedback.textContent = message;
    feedback.className = `rounded-md border px-3 py-2.5 text-sm ${success ? 'border-success bg-success-soft text-success' : 'border-destructive bg-destructive-soft text-destructive'}`;
}

function createEntryQr(entryToken) {
    const qr = qrcode(0, 'M');
    qr.addData(entryToken, 'Byte');
    qr.make();
    return qr.createDataURL(8, 8);
}

function renderCompletion(response) {
    state.createdLookupToken = response.lookupToken;
    state.entryQrUrl = createEntryQr(response.entryToken);
    response.entryToken = null;
    response.lookupToken = null;
    lookup('[data-created-reservation-no]').textContent = response.reservationNo;
    lookup('[data-created-lookup-token]').textContent = state.createdLookupToken;
    lookup('[data-entry-qr]').src = state.entryQrUrl;
    lookup('[data-reservation-content]').classList.add('hidden');
    lookup('[data-reservation-complete]').classList.remove('hidden');
    lookup('[data-reservation-complete]').scrollIntoView({behavior: 'smooth', block: 'start'});
}

async function submitReservation(trigger) {
    const form = lookup('[data-reservation-form]');
    if (!form.reportValidity()) {
        return;
    }
    if (!state.selectedRoundId || state.selectedSeatIds.size === 0) {
        showFeedback('공연 회차와 좌석을 선택해 주세요.');
        document.getElementById('seatSelection').scrollIntoView({behavior: 'smooth'});
        return;
    }
    trigger.disabled = true;
    try {
        const response = await post(`/api/public-reservations/${encodeURIComponent(slug)}`, {
            performanceRoundId: state.selectedRoundId,
            performanceRoundSeatIds: Array.from(state.selectedSeatIds),
            applicantName: readValue('guestName'),
            phone: readValue('guestPhone'),
            privacyPolicyVersionId: state.policy.policyDocumentVersionId,
        });
        renderCompletion(response);
        showToast('관람 신청이 완료되었습니다.');
    } catch (error) {
        showFeedback(error.message || '관람 신청을 완료하지 못했습니다.');
        try {
            await loadSeats(state.selectedRoundId);
        } catch (seatError) {
            showToast('좌석 현황을 새로 불러오지 못했습니다.');
        }
    } finally {
        trigger.disabled = false;
    }
}

async function copyLookupToken() {
    if (!state.createdLookupToken) {
        return;
    }
    try {
        await navigator.clipboard.writeText(state.createdLookupToken);
        showToast('조회 토큰을 복사했습니다.');
    } catch (error) {
        showToast('복사하지 못했습니다. 조회 토큰을 직접 선택해 주세요.');
    }
}

function downloadEntryQr() {
    if (!state.entryQrUrl) {
        return;
    }
    const anchor = element('a');
    anchor.href = state.entryQrUrl;
    anchor.download = `bandi-entry-${lookup('[data-created-reservation-no]').textContent}.gif`;
    anchor.click();
}

function chooseInitialRound() {
    const requested = Number(new URLSearchParams(window.location.search).get('round'));
    const openRounds = state.rounds.filter((round) => round.status === 'RESERVATION_OPEN');
    return openRounds.find((round) => round.performanceRoundId === requested)
            || openRounds[0]
            || null;
}

function renderPage() {
    document.title = `${state.page.projectTitle} 관람 신청 - bandi`;
    lookup('[data-performance-title]').textContent = state.page.projectTitle;
    lookup('[data-performance-description]').textContent = `${state.page.shortDescription} · ${state.page.place}`;
    lookup('[data-performance-link]').href = `/performances/${encodeURIComponent(slug)}`;
    lookup('[data-policy-body]').textContent = state.policy.body;
    lookup('[data-reservation-page]').setAttribute('aria-busy', 'false');
    lookup('[data-reservation-content]').classList.remove('hidden');
    renderRounds();
}

function showLoadError(error) {
    lookup('[data-reservation-page]').setAttribute('aria-busy', 'false');
    lookup('[data-reservation-error-message]').textContent = error.message || '잠시 후 다시 시도해 주세요.';
    lookup('[data-reservation-error]').classList.remove('hidden');
}

document.addEventListener('click', (event) => {
    const round = event.target.closest('[data-round-id]');
    if (round) {
        loadSeats(Number(round.dataset.roundId)).catch(showLoadError);
        return;
    }
    const seat = event.target.closest('[data-seat-id]');
    if (seat) {
        toggleSeat(seat);
    }
});

lookup('[data-reservation-form]').addEventListener('submit', (event) => {
    event.preventDefault();
    submitReservation(event.submitter || lookup('[data-reservation-form] button[type="submit"]'));
});

bindPageActions({
    [ACTIONS.COPY_LOOKUP_TOKEN]: copyLookupToken,
    [ACTIONS.DOWNLOAD_ENTRY_QR]: downloadEntryQr,
});

Promise.all([
    get(`/api/public-performances/${encodeURIComponent(slug)}`),
    get(`/api/public-performances/${encodeURIComponent(slug)}/rounds`),
    get('/api/public-policies/reservation-privacy'),
]).then(async ([page, rounds, policy]) => {
    state.page = page;
    state.rounds = rounds;
    state.policy = policy;
    renderPage();
    const initialRound = chooseInitialRound();
    if (initialRound) {
        await loadSeats(initialRound.performanceRoundId);
    } else {
        renderSeats();
        showFeedback('현재 관람 신청 가능한 회차가 없습니다.');
    }
}).catch(showLoadError);
