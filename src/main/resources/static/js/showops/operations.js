import {get, post} from '../common/api.js';
import {openModal} from '../common/modal.js';
import {showToast} from '../common/toast.js';
import {bindPageActions, element, lookup, readValue} from '../common/dom.js';
import {badge, closeActionModal} from '../common/view.js';

const ACTIONS = Object.freeze({
    CHECK_IN: 'entry-check-in',
    CANCEL_OPEN: 'entry-cancel-open',
    CANCEL_SAVE: 'entry-cancel-save',
});

const ROUND_STATUS = Object.freeze({
    SCHEDULED: ['예정', 'neutral'],
    RESERVATION_OPEN: ['신청 진행', 'info'],
    RESERVATION_CLOSED: ['신청 마감', 'warning'],
    ENTRY_OPEN: ['입장 진행', 'success'],
    ENDED: ['종료', 'neutral'],
    CANCELLED: ['취소', 'danger'],
});

const RESERVATION_STATUS = Object.freeze({
    CONFIRMED: ['확정', 'success'],
    PARTIALLY_CANCELLED: ['일부 취소', 'warning'],
    CANCELLED: ['취소', 'danger'],
});

let projects = [];
let rounds = [];
let currentReservation = null;
let currentEntryToken = null;
let lookupMethod = null;

function formatDateTime(value) {
    if (!value) {
        return '-';
    }
    return new Intl.DateTimeFormat('ko-KR', {
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
    }).format(new Date(value));
}

function roundLabel(round) {
    return `${round.roundNo}회차 · ${formatDateTime(round.startDttm)}`;
}

function populateSelect(select, values, valueKey, labelBuilder, emptyLabel) {
    select.replaceChildren();
    if (values.length === 0) {
        const option = element('option', '', emptyLabel);
        option.value = '';
        select.appendChild(option);
        select.disabled = true;
        return;
    }
    select.disabled = false;
    values.forEach((value) => {
        const option = element('option', '', labelBuilder(value));
        option.value = String(value[valueKey]);
        select.appendChild(option);
    });
}

function selectedRound() {
    const roundId = Number(readValue('entryRound'));
    return rounds.find((round) => round.performanceRoundId === roundId) || null;
}

function entryIsOpen() {
    return selectedRound()?.status === 'ENTRY_OPEN';
}

function setLookupFormsEnabled(enabled) {
    ['entryToken', 'entryReservationNo', 'entryApplicantName'].forEach((id) => {
        document.getElementById(id).disabled = !enabled;
    });
    ['[data-token-form] button[type="submit"]',
        '[data-manual-form] button[type="submit"]'].forEach((selector) => {
        lookup(selector).disabled = !enabled;
    });
}

function renderRoundStatus() {
    const round = selectedRound();
    const statusHost = lookup('[data-round-status]');
    statusHost.replaceChildren();
    if (!round) {
        statusHost.appendChild(badge('선택 안 됨'));
        lookup('[data-entry-guidance]').textContent = '입장할 회차를 선택해 주세요.';
        setLookupFormsEnabled(false);
        return;
    }
    const [label, tone] = ROUND_STATUS[round.status] || [round.status, 'neutral'];
    statusHost.appendChild(badge(label, tone));
    const open = round.status === 'ENTRY_OPEN';
    lookup('[data-entry-guidance]').textContent = open
            ? '현재 회차의 관람객 입장을 처리할 수 있습니다.'
            : '입장 진행 상태인 회차에서만 관람객 조회와 처리가 가능합니다.';
    setLookupFormsEnabled(open);
}

function resetMetrics() {
    ['entry-reservation-count', 'entry-reserved-seat-count',
        'entry-checked-seat-count', 'entry-pending-seat-count'].forEach((hook) => {
        lookup(`[data-stat-value="${hook}"]`).textContent = '0';
    });
    lookup('[data-stat-delta="entry-rate"]').textContent = '입장률 0%';
}

function renderMetrics(metrics) {
    lookup('[data-stat-value="entry-reservation-count"]').textContent = String(metrics.reservationCount);
    lookup('[data-stat-value="entry-reserved-seat-count"]').textContent = String(metrics.reservedSeatCount);
    lookup('[data-stat-value="entry-checked-seat-count"]').textContent = String(metrics.checkedInSeatCount);
    lookup('[data-stat-value="entry-pending-seat-count"]').textContent = String(metrics.notCheckedInSeatCount);
    lookup('[data-stat-delta="entry-rate"]').textContent = `입장률 ${Math.round(Number(metrics.entryRate))}%`;
}

async function loadMetrics() {
    const projectId = Number(readValue('entryProject'));
    const roundId = Number(readValue('entryRound'));
    if (!projectId || !roundId) {
        resetMetrics();
        return;
    }
    const metrics = await get(`/api/reservation-management/rounds/${roundId}/metrics`, {
        projectId,
    });
    renderMetrics(metrics);
}

function clearSensitiveState() {
    currentEntryToken = null;
    document.getElementById('entryToken').value = '';
}

function clearEntryResult(message = 'QR 또는 신청 정보로 관람 신청을 조회해 주세요.') {
    currentReservation = null;
    lookupMethod = null;
    clearSensitiveState();
    lookup('[data-entry-empty]').textContent = message;
    lookup('[data-entry-empty]').classList.remove('hidden');
    lookup('[data-entry-detail]').classList.add('hidden');
    lookup('[data-reservation-status]').classList.add('hidden');
}

function selectedSeatIds() {
    return Array.from(document.querySelectorAll('[data-entry-seat-select]:checked'))
            .map((input) => Number(input.value));
}

function updateSelectionSummary() {
    const selected = selectedSeatIds();
    lookup('[data-entry-selection-summary]').textContent = `선택된 좌석 ${selected.length}개`;
    lookup(`[data-page-action="${ACTIONS.CHECK_IN}"]`).disabled = selected.length === 0;
}

function cancelButton(seat) {
    const button = element('button', 'inline-flex min-h-11 items-center justify-center rounded-md border bg-card px-3 text-xs font-bold text-destructive transition-colors hover:bg-destructive-soft focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring', '입장 취소');
    button.type = 'button';
    button.dataset.pageAction = ACTIONS.CANCEL_OPEN;
    button.dataset.reservationSeatId = String(seat.reservationSeatId);
    button.dataset.seatLabel = seat.seatLabel;
    return button;
}

function buildSeatControl(seat) {
    const wrapper = element('div', 'flex min-h-14 items-center gap-3 rounded-md border p-3');
    const cancelled = seat.status === 'CANCELLED';
    const checkedIn = Boolean(seat.checkedInDttm);
    if (!cancelled && !checkedIn) {
        const input = element('input', 'size-5 shrink-0 accent-primary');
        input.type = 'checkbox';
        input.value = String(seat.reservationSeatId);
        input.dataset.entrySeatSelect = '';
        input.id = `entrySeat${seat.reservationSeatId}`;
        input.addEventListener('change', updateSelectionSummary);
        wrapper.appendChild(input);
    } else {
        wrapper.appendChild(element('span', 'size-5 shrink-0'));
    }
    const content = element('div', 'min-w-0 flex-1');
    const label = element(cancelled || checkedIn ? 'strong' : 'label', 'block text-sm font-extrabold', seat.seatLabel);
    if (!cancelled && !checkedIn) {
        label.htmlFor = `entrySeat${seat.reservationSeatId}`;
    }
    content.appendChild(label);
    if (cancelled) {
        content.appendChild(element('span', 'mt-0.5 block text-xs text-destructive', '취소된 좌석'));
    } else if (checkedIn) {
        content.appendChild(element('span', 'mt-0.5 block text-xs text-success', `입장 ${formatDateTime(seat.checkedInDttm)}`));
    } else {
        content.appendChild(element('span', 'mt-0.5 block text-xs text-muted-foreground', '미입장'));
    }
    wrapper.appendChild(content);
    if (checkedIn && !cancelled) {
        wrapper.appendChild(cancelButton(seat));
    }
    return wrapper;
}

function renderReservation(reservation) {
    currentReservation = reservation;
    lookup('[data-entry-empty]').classList.add('hidden');
    lookup('[data-entry-detail]').classList.remove('hidden');
    lookup('[data-entry-reservation-no]').textContent = reservation.reservationNo;
    lookup('[data-entry-applicant-name]').textContent = reservation.applicantName;
    lookup('[data-entry-phone]').textContent = reservation.phone;
    const statusHost = lookup('[data-reservation-status]');
    const [label, tone] = RESERVATION_STATUS[reservation.status] || [reservation.status, 'neutral'];
    statusHost.replaceChildren(badge(label, tone));
    statusHost.classList.remove('hidden');
    const seatHost = lookup('[data-entry-seats]');
    seatHost.replaceChildren();
    reservation.seats.forEach((seat) => seatHost.appendChild(buildSeatControl(seat)));
    if (reservation.seats.length === 0) {
        seatHost.appendChild(element('p', 'text-sm text-muted-foreground', '신청된 좌석이 없습니다.'));
    }
    updateSelectionSummary();
}

async function withBusy(trigger, task) {
    trigger.disabled = true;
    try {
        await task();
    } catch (error) {
        showToast(error.message || '요청을 처리하지 못했습니다.');
    } finally {
        trigger.disabled = false;
    }
}

function validateEntryRound() {
    if (!entryIsOpen()) {
        showToast('입장 진행 상태인 회차를 선택해 주세요.');
        return false;
    }
    return true;
}

async function lookupByToken(trigger) {
    const form = lookup('[data-token-form]');
    if (!form.reportValidity() || !validateEntryRound()) {
        return;
    }
    const token = readValue('entryToken');
    clearEntryResult('일치하는 관람 신청을 찾지 못했습니다. 다시 조회해 주세요.');
    await withBusy(trigger, async () => {
        const reservation = await post(`/api/reservation-management/rounds/${readValue('entryRound')}/entry/lookup`, {
            entryToken: token,
        });
        currentEntryToken = token;
        lookupMethod = 'TOKEN';
        document.getElementById('entryToken').value = '';
        renderReservation(reservation);
        showToast('관람 신청을 확인했습니다.');
    });
}

async function lookupManually(trigger) {
    const form = lookup('[data-manual-form]');
    if (!form.reportValidity() || !validateEntryRound()) {
        return;
    }
    const reservationNo = readValue('entryReservationNo');
    const applicantName = readValue('entryApplicantName');
    clearEntryResult('일치하는 관람 신청을 찾지 못했습니다. 다시 조회해 주세요.');
    await withBusy(trigger, async () => {
        const reservation = await post(`/api/reservation-management/rounds/${readValue('entryRound')}/entry/search`, {
            reservationNo,
            applicantName,
        });
        lookupMethod = 'MANUAL';
        renderReservation(reservation);
        showToast('관람 신청을 확인했습니다.');
    });
}

async function checkIn(trigger) {
    if (!currentReservation || !validateEntryRound()) {
        return;
    }
    const reservationSeatIds = selectedSeatIds();
    if (reservationSeatIds.length === 0) {
        showToast('입장 처리할 좌석을 선택해 주세요.');
        return;
    }
    await withBusy(trigger, async () => {
        const roundId = readValue('entryRound');
        if (lookupMethod === 'TOKEN') {
            await post(`/api/reservation-management/rounds/${roundId}/entry/check-ins`, {
                entryToken: currentEntryToken,
                reservationSeatIds,
            });
        } else {
            await post(`/api/reservation-management/rounds/${roundId}/entry/manual-check-ins`, {
                reservationNo: currentReservation.reservationNo,
                applicantName: currentReservation.applicantName,
                reservationSeatIds,
            });
        }
        const processedCount = reservationSeatIds.length;
        await refreshCurrentReservation();
        await loadMetrics();
        showToast(`${processedCount}개 좌석을 입장 처리했습니다.`);
    });
}

async function refreshCurrentReservation() {
    const roundId = readValue('entryRound');
    if (lookupMethod === 'TOKEN') {
        currentReservation = await post(`/api/reservation-management/rounds/${roundId}/entry/lookup`, {
            entryToken: currentEntryToken,
        });
    } else {
        currentReservation = await post(`/api/reservation-management/rounds/${roundId}/entry/search`, {
            reservationNo: currentReservation.reservationNo,
            applicantName: currentReservation.applicantName,
        });
    }
    renderReservation(currentReservation);
}

function openCancel(trigger) {
    document.getElementById('entryCancelSeatId').value = trigger.dataset.reservationSeatId;
    document.getElementById('entryCancelReason').value = '';
    lookup('[data-entry-cancel-seat]').textContent = trigger.dataset.seatLabel;
    openModal('entryCancelModal');
}

async function cancelEntry(trigger) {
    const form = lookup('[data-entry-cancel-form]');
    if (!form.reportValidity() || !currentReservation || !validateEntryRound()) {
        return;
    }
    await withBusy(trigger, async () => {
        await post(`/api/reservation-management/rounds/${readValue('entryRound')}/entry/check-in-cancellations`, {
            reservationSeatId: Number(readValue('entryCancelSeatId')),
            reason: readValue('entryCancelReason'),
        });
        closeActionModal(trigger);
        await refreshCurrentReservation();
        await loadMetrics();
        showToast('좌석 입장을 취소했습니다.');
    });
}

async function loadRounds() {
    const projectId = Number(readValue('entryProject'));
    rounds = projectId
            ? await get(`/api/performance-management/projects/${projectId}/rounds`)
            : [];
    populateSelect(document.getElementById('entryRound'), rounds,
            'performanceRoundId', roundLabel, '등록된 회차가 없습니다');
    renderRoundStatus();
    clearEntryResult();
    await loadMetrics();
}

async function loadReferences() {
    projects = await get('/api/performance-management/projects', {limit: 100});
    projects = projects.filter((project) => project.status !== 'CANCELLED');
    populateSelect(document.getElementById('entryProject'), projects,
            'performanceProjectId',
            (project) => `${project.academicYear} ${project.termCode} · ${project.title}`,
            '등록된 공연 프로젝트가 없습니다');
    await loadRounds();
}

document.getElementById('entryProject').addEventListener('change', async () => {
    try {
        await loadRounds();
    } catch (error) {
        showToast(error.message || '공연 정보를 불러오지 못했습니다.');
    }
});

document.getElementById('entryRound').addEventListener('change', async () => {
    clearEntryResult();
    renderRoundStatus();
    try {
        await loadMetrics();
    } catch (error) {
        resetMetrics();
        showToast(error.message || '입장 지표를 불러오지 못했습니다.');
    }
});

lookup('[data-token-form]').addEventListener('submit', (event) => {
    event.preventDefault();
    const trigger = event.submitter
            || lookup('[data-token-form] button[type="submit"]');
    lookupByToken(trigger).catch((error) => {
        showToast(error.message || '관람 신청을 확인하지 못했습니다.');
    });
});
lookup('[data-manual-form]').addEventListener('submit', (event) => {
    event.preventDefault();
    const trigger = event.submitter
            || lookup('[data-manual-form] button[type="submit"]');
    lookupManually(trigger).catch((error) => {
        showToast(error.message || '관람 신청을 확인하지 못했습니다.');
    });
});

bindPageActions({
    [ACTIONS.CHECK_IN]: checkIn,
    [ACTIONS.CANCEL_OPEN]: openCancel,
    [ACTIONS.CANCEL_SAVE]: cancelEntry,
});

clearEntryResult();
setLookupFormsEnabled(false);
loadReferences().catch((error) => {
    resetMetrics();
    clearEntryResult('공연 정보를 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.');
    showToast(error.message || '공연 정보를 불러오지 못했습니다.');
});
