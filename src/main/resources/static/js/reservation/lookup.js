import {post} from '../common/api.js';
import {closeSheetOf, openSheet} from '../common/sheet.js';
import {showToast} from '../common/toast.js';
import {bindPageActions, element, lookup, readValue} from '../common/dom.js';
import {badge} from '../common/view.js';

const ACTIONS = Object.freeze({
    CANCEL_OPEN: 'reservation-cancel-open',
    CANCEL_SAVE: 'reservation-cancel-save',
    RESET: 'reservation-lookup-reset',
});

const STATUS_LABELS = Object.freeze({
    CONFIRMED: ['신청 확정', 'success'],
    PARTIALLY_CANCELLED: ['일부 취소', 'warning'],
    CANCELLED: ['신청 취소', 'danger'],
});

let currentLookupToken = null;
let currentReservation = null;

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

function seatCard(seat) {
    const cancelled = seat.status === 'CANCELLED';
    const checkedIn = Boolean(seat.checkedInDttm);
    const card = element('div', 'flex min-h-16 items-center gap-3 rounded-md border p-3');
    card.appendChild(element('strong', 'text-sm font-black', seat.seatLabel));
    const state = cancelled
            ? badge('취소', 'danger')
            : checkedIn ? badge('입장 완료', 'success') : badge('미입장', 'warning');
    card.appendChild(state);
    if (checkedIn) {
        card.appendChild(element('span', 'ml-auto text-xs text-muted-foreground', formatDateTime(seat.checkedInDttm)));
    }
    return card;
}

function renderReservation(reservation) {
    currentReservation = reservation;
    lookup('[data-lookup-entry]').classList.add('hidden');
    lookup('[data-lookup-empty]').classList.add('hidden');
    lookup('[data-lookup-result]').classList.remove('hidden');
    lookup('[data-lookup-reservation-no]').textContent = reservation.reservationNo;
    lookup('[data-lookup-performance-title]').textContent = reservation.performanceTitle;
    lookup('[data-lookup-round]').textContent = `${reservation.roundNo}회차 · ${formatDateTime(reservation.startDttm)}`;
    lookup('[data-lookup-name]').textContent = reservation.applicantName || '개인정보 파기됨';
    lookup('[data-lookup-phone]').textContent = reservation.phone || '개인정보 파기됨';
    lookup('[data-lookup-place]').textContent = reservation.place;
    const [statusLabel, tone] = STATUS_LABELS[reservation.status] || [reservation.status, 'neutral'];
    lookup('[data-lookup-status]').replaceChildren(badge(statusLabel, tone));
    const seatHost = lookup('[data-lookup-seats]');
    seatHost.replaceChildren();
    reservation.seats.forEach((seat) => seatHost.appendChild(seatCard(seat)));
    if (reservation.seats.length === 0) {
        seatHost.appendChild(element('p', 'text-sm text-muted-foreground', '신청 좌석 정보가 없습니다.'));
    }
    lookup('[data-lookup-performance-link]').href = `/performances/${encodeURIComponent(reservation.performanceSlug)}`;
    const cancelButton = lookup(`[data-page-action="${ACTIONS.CANCEL_OPEN}"]`);
    cancelButton.classList.toggle('hidden', !reservation.cancelable);
}

function showFeedback(message, tone = 'danger') {
    const feedback = lookup('[data-lookup-feedback]');
    feedback.textContent = message;
    feedback.className = tone === 'success'
            ? 'mt-3 rounded-md border border-success bg-success-soft px-3 py-2.5 text-sm text-success'
            : 'mt-3 rounded-md border border-destructive bg-destructive-soft px-3 py-2.5 text-sm text-destructive';
}

function showLookupError(message) {
    currentReservation = null;
    currentLookupToken = null;
    lookup('[data-lookup-result]').classList.add('hidden');
    lookup('[data-lookup-entry]').classList.remove('hidden');
    showFeedback(`${message} 입력한 토큰은 유지했어요. 다시 확인해 주세요.`);
}

async function lookupReservation(trigger) {
    const form = lookup('[data-lookup-form]');
    if (!form.reportValidity()) {
        return;
    }
    const token = readValue('lookupToken');
    trigger.disabled = true;
    lookup('[data-lookup-feedback]').classList.add('hidden');
    try {
        const reservation = await post('/api/public-reservations/lookup', {
            lookupToken: token,
        });
        currentLookupToken = token;
        document.getElementById('lookupToken').value = '';
        renderReservation(reservation);
        showToast('관람 신청을 확인했어요.');
    } catch (error) {
        showLookupError(error.message || '관람 신청을 조회하지 못했어요.');
    } finally {
        trigger.disabled = false;
    }
}

function openCancel(trigger) {
    if (!currentReservation || !currentLookupToken) {
        showFeedback('다시 조회한 뒤 취소를 시작해 주세요.');
        return;
    }
    document.getElementById('publicCancelReason').value = '';
    const activeSeats = currentReservation.seats.filter((seat) => seat.status !== 'CANCELLED');
    lookup('[data-cancel-reservation-summary]').textContent =
            `${currentReservation.performanceTitle} · ${currentReservation.roundNo}회차 · ${activeSeats.length}석`;
    openSheet('publicReservationCancelSheet', trigger);
}

async function cancelReservation(trigger) {
    const form = lookup('[data-public-cancel-form]');
    if (!form.reportValidity() || !currentLookupToken || !currentReservation) {
        return;
    }
    trigger.disabled = true;
    try {
        await post('/api/public-reservations/cancel', {
            lookupToken: currentLookupToken,
            reason: readValue('publicCancelReason'),
        });
        closeSheetOf(trigger);
        const reservation = await post('/api/public-reservations/lookup', {
            lookupToken: currentLookupToken,
        });
        renderReservation(reservation);
        currentLookupToken = null;
        showToast('관람 신청을 취소했어요.');
    } catch (error) {
        showToast(error.message || '관람 신청을 취소하지 못했습니다.');
    } finally {
        trigger.disabled = false;
    }
}

function resetLookup() {
    currentLookupToken = null;
    currentReservation = null;
    lookup('[data-lookup-result]').classList.add('hidden');
    lookup('[data-lookup-empty]').classList.remove('hidden');
    lookup('[data-lookup-entry]').classList.remove('hidden');
    lookup('[data-lookup-feedback]').classList.add('hidden');
    const input = document.getElementById('lookupToken');
    input.value = '';
    input.focus();
}

lookup('[data-lookup-form]').addEventListener('submit', (event) => {
    event.preventDefault();
    lookupReservation(event.submitter || lookup('[data-lookup-form] button[type="submit"]'));
});

bindPageActions({
    [ACTIONS.CANCEL_OPEN]: openCancel,
    [ACTIONS.CANCEL_SAVE]: cancelReservation,
    [ACTIONS.RESET]: resetLookup,
});

window.addEventListener('pagehide', () => {
    currentLookupToken = null;
    currentReservation = null;
    document.getElementById('lookupToken').value = '';
});
