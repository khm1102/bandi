import {get, patch} from '../common/api.js';
import {openModal} from '../common/modal.js';
import {showToast} from '../common/toast.js';
import {appendCell, bindPageActions, element, lookup, readValue} from '../common/dom.js';
import {activateFilterChip, badge, closeActionModal} from '../common/view.js';

const ACTIONS = Object.freeze({
    EXPORT: 'reservation-export',
    CANCEL_OPEN: 'reservation-cancel-open',
    CANCEL_SAVE: 'reservation-cancel-save',
});

const STATUS_LABELS = Object.freeze({
    CONFIRMED: '확정',
    PARTIALLY_CANCELLED: '일부 취소',
    CANCELLED: '취소',
});

let projects = [];
let rounds = [];
let reservations = [];
let metrics = null;

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

function statusTone(status) {
    if (status === 'CONFIRMED') {
        return 'success';
    }
    if (status === 'PARTIALLY_CANCELLED') {
        return 'warning';
    }
    return 'danger';
}

function populateSelect(select, values, valueKey, labelBuilder) {
    select.replaceChildren();
    values.forEach((value) => {
        const option = element('option', '', labelBuilder(value));
        option.value = String(value[valueKey]);
        select.appendChild(option);
    });
}

function emptyRow(message) {
    const row = element('tr');
    const cell = appendCell(row, message, 'py-11 text-center text-muted-foreground');
    cell.colSpan = 6;
    return row;
}

function actionButton(label, action) {
    const button = element('button', 'inline-flex min-h-11 items-center justify-center rounded-md border bg-card px-3 text-xs font-bold text-destructive transition-colors hover:bg-destructive-soft focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring', label);
    button.type = 'button';
    button.dataset.pageAction = action;
    return button;
}

function buildReservationRow(reservation) {
    const row = element('tr');
    appendCell(row, reservation.reservationNo, 'font-mono text-xs font-bold');
    const nameCell = appendCell(row, '');
    const person = element('div', 'flex items-center gap-2');
    person.appendChild(element('span', 'flex size-7 shrink-0 items-center justify-center rounded-full bg-info text-xs font-black text-white', reservation.applicantName.slice(0, 1)));
    person.appendChild(element('strong', 'text-sm', reservation.applicantName));
    nameCell.appendChild(person);
    appendCell(row, reservation.phone, 'text-muted-foreground');
    const seatsCell = appendCell(row, '');
    const seats = reservation.seats.filter((seat) => seat.status !== 'CANCELLED');
    const seatGroup = element('div', 'flex min-w-36 flex-wrap gap-1');
    seats.forEach((seat) => {
        const label = seat.checkedInDttm ? `${seat.seatLabel} · 입장` : seat.seatLabel;
        seatGroup.appendChild(badge(label, seat.checkedInDttm ? 'success' : 'info'));
    });
    if (seats.length === 0) {
        seatGroup.appendChild(element('span', 'text-xs text-muted-foreground', '유효 좌석 없음'));
    }
    seatsCell.appendChild(seatGroup);
    const statusCell = appendCell(row, '');
    statusCell.appendChild(badge(STATUS_LABELS[reservation.status] || reservation.status,
            statusTone(reservation.status)));
    const actionCell = appendCell(row, '', 'text-right');
    if (reservation.cancelable && reservation.status !== 'CANCELLED') {
        const button = actionButton('신청 취소', ACTIONS.CANCEL_OPEN);
        button.dataset.reservationId = String(reservation.reservationId);
        actionCell.appendChild(button);
    }
    return row;
}

function renderReservations() {
    const list = lookup('[data-reservation-list]');
    list.replaceChildren();
    if (reservations.length === 0) {
        list.appendChild(emptyRow('이 회차에 해당하는 관람 신청이 없습니다.'));
    } else {
        list.append(...reservations.map(buildReservationRow));
    }
    lookup('[data-reservation-region]').setAttribute('aria-busy', 'false');
}

function renderMetrics() {
    const values = metrics || {
        reservationCount: 0,
        reservedSeatCount: 0,
        checkedInSeatCount: 0,
        entryRate: 0,
    };
    lookup('[data-stat-value="reservation-count"]').textContent = String(values.reservationCount);
    lookup('[data-stat-value="reserved-seat-count"]').textContent = String(values.reservedSeatCount);
    lookup('[data-stat-value="checked-seat-count"]').textContent = String(values.checkedInSeatCount);
    lookup('[data-stat-value="entry-rate"]').textContent = String(Math.round(Number(values.entryRate)));
}

async function loadRounds() {
    const projectId = Number(readValue('reservationProject'));
    rounds = projectId
            ? await get(`/api/performance-management/projects/${projectId}/rounds`)
            : [];
    populateSelect(document.getElementById('reservationRound'), rounds,
            'performanceRoundId', roundLabel);
}

async function loadReservations() {
    const projectId = Number(readValue('reservationProject'));
    const roundId = Number(readValue('reservationRound'));
    if (!projectId || !roundId) {
        reservations = [];
        metrics = null;
        renderMetrics();
        renderReservations();
        return;
    }
    const selected = lookup('[data-filter-group="reservation-status"][aria-pressed="true"]');
    const status = selected?.dataset.filterValue || 'ALL';
    lookup('[data-reservation-region]').setAttribute('aria-busy', 'true');
    [reservations, metrics] = await Promise.all([
        get(`/api/reservation-management/rounds/${roundId}/reservations`, {
            projectId,
            status: status === 'ALL' ? null : status,
            offset: 0,
            limit: 100,
        }),
        get(`/api/reservation-management/rounds/${roundId}/metrics`, {
            projectId,
        }),
    ]);
    renderMetrics();
    renderReservations();
}

async function loadReferences() {
    projects = await get('/api/performance-management/projects', {limit: 100});
    projects = projects.filter((project) => project.status !== 'CANCELLED');
    populateSelect(document.getElementById('reservationProject'), projects,
            'performanceProjectId',
            (project) => `${project.academicYear} ${project.termCode} · ${project.title}`);
    await loadRounds();
    await loadReservations();
}

function csvCell(value) {
    return `"${String(value ?? '').replaceAll('"', '""')}"`;
}

function exportReservations() {
    if (reservations.length === 0) {
        showToast('내보낼 신청 명단이 없습니다.');
        return;
    }
    const rows = reservations.map((reservation) => [
        reservation.reservationNo,
        reservation.applicantName,
        reservation.phone,
        reservation.seats.filter((seat) => seat.status !== 'CANCELLED')
                .map((seat) => seat.seatLabel).join(' '),
        reservation.seats.filter((seat) => seat.status !== 'CANCELLED').length,
        STATUS_LABELS[reservation.status] || reservation.status,
    ].map(csvCell).join(','));
    const csv = ['신청번호,관람객명,연락처,좌석,유효좌석수,상태', ...rows].join('\n');
    const url = URL.createObjectURL(new Blob([`\uFEFF${csv}`], {
        type: 'text/csv;charset=utf-8',
    }));
    const anchor = element('a');
    anchor.href = url;
    anchor.download = `bandi_reservations_round_${readValue('reservationRound')}.csv`;
    anchor.click();
    URL.revokeObjectURL(url);
    showToast('현재 조회한 명단을 CSV로 내보냈습니다.');
}

function openCancel(trigger) {
    const reservation = reservations.find((candidate) => String(candidate.reservationId) === trigger.dataset.reservationId);
    document.getElementById('cancelReservationId').value = trigger.dataset.reservationId;
    document.getElementById('cancelReason').value = '';
    lookup('[data-cancel-reservation]').textContent = `${reservation.reservationNo} · ${reservation.applicantName}`;
    openModal('reservationCancelModal');
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

async function cancelReservation(trigger) {
    const form = lookup('[data-cancel-form]');
    if (!form.reportValidity()) {
        return;
    }
    await withBusy(trigger, async () => {
        await patch(`/api/reservation-management/reservations/${readValue('cancelReservationId')}/cancel`, {
            performanceProjectId: Number(readValue('reservationProject')),
            reason: readValue('cancelReason'),
        });
        closeActionModal(trigger);
        await loadReservations();
        showToast('관람 신청을 취소했습니다.');
    });
}

document.getElementById('reservationProject').addEventListener('change', async () => {
    try {
        await loadRounds();
        await loadReservations();
    } catch (error) {
        showToast(error.message || '공연 정보를 불러오지 못했습니다.');
    }
});
document.getElementById('reservationRound').addEventListener('change', () => {
    loadReservations().catch((error) => showToast(error.message));
});
document.addEventListener('click', (event) => {
    const filter = event.target.closest('[data-filter-group="reservation-status"]');
    if (filter) {
        activateFilterChip(filter);
        loadReservations().catch((error) => showToast(error.message));
    }
});

bindPageActions({
    [ACTIONS.EXPORT]: exportReservations,
    [ACTIONS.CANCEL_OPEN]: openCancel,
    [ACTIONS.CANCEL_SAVE]: cancelReservation,
});

loadReferences().catch((error) => {
    lookup('[data-reservation-region]').setAttribute('aria-busy', 'false');
    lookup('[data-reservation-list]').replaceChildren(emptyRow('신청 명단을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.'));
    showToast(error.message || '신청 명단을 불러오지 못했습니다.');
});
