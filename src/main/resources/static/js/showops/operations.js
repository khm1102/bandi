import {ApiError, get, post} from '../common/api.js';
import {bindPageActions, element, lookup, readValue} from '../common/dom.js';
import {openSheet, closeSheetOf} from '../common/sheet.js';
import {showToast} from '../common/toast.js';
import {badge} from '../common/view.js';

const ACTIONS = Object.freeze({
    CHECK_IN: 'entry-check-in',
    SELECT_ALL: 'entry-select-all',
    NEXT_GUEST: 'entry-next-guest',
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
const ROUND_BLOCK_REASON = Object.freeze({
    SCHEDULED: '이 회차는 아직 입장 시작 전이에요.',
    RESERVATION_OPEN: '이 회차는 아직 신청을 받는 중이라 입장 처리를 시작할 수 없어요.',
    RESERVATION_CLOSED: '신청은 마감됐지만 입장은 아직 열리지 않았어요.',
    ENDED: '이미 종료된 회차예요.',
    CANCELLED: '취소된 회차라 입장 처리를 할 수 없어요.',
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
        month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', hour12: false,
    }).format(new Date(value));
}

function describeError(error) {
    if (error instanceof ApiError && error.status === 401) {
        return '로그인이 만료됐어요. 다시 로그인해야 처리할 수 있어요.';
    }
    if (error instanceof ApiError && error.status === 409) {
        return '다른 기기에서 먼저 처리됐을 수 있어요. 현재 상태를 다시 조회해 주세요.';
    }
    return error.message || '요청을 처리하지 못했어요. 잠시 후 다시 시도해 주세요.';
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

function selectedProject() {
    const projectId = Number(readValue('entryProject'));
    return projects.find((project) => project.performanceProjectId === projectId) || null;
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

function focusTokenInput() {
    const input = document.getElementById('entryToken');
    if (!input.disabled) {
        input.focus();
    }
}

// ---------- 컨텍스트 표시 ----------

function renderContextSummary() {
    const summary = lookup('[data-context-summary]');
    const project = selectedProject();
    const round = selectedRound();
    if (!project || !round) {
        summary.textContent = '공연과 회차를 선택해 주세요';
        return;
    }
    summary.textContent = `${project.title} · ${roundLabel(round)}`;
}

function renderRoundStatus() {
    const round = selectedRound();
    const statusHost = lookup('[data-round-status]');
    statusHost.replaceChildren();
    const guidance = lookup('[data-entry-guidance]');
    renderContextSummary();
    if (!round) {
        statusHost.appendChild(badge('선택 안 됨'));
        guidance.textContent = '입장할 회차를 선택해 주세요.';
        setLookupFormsEnabled(false);
        renderBlocked('회차를 선택하면 QR 조회를 시작할 수 있어요.');
        return;
    }
    const [label, tone] = ROUND_STATUS[round.status] || [round.status, 'neutral'];
    statusHost.appendChild(badge(label, tone));
    const open = round.status === 'ENTRY_OPEN';
    setLookupFormsEnabled(open);
    if (open) {
        guidance.textContent = '입장 진행 중이에요. QR 입력에 포커스를 두고 스캔해 주세요.';
        lookup('[data-context-details]').open = false;
        clearEntryResult();
        focusTokenInput();
        return;
    }
    const reason = ROUND_BLOCK_REASON[round.status] || '이 회차에서는 입장 처리를 할 수 없어요.';
    guidance.textContent = `${reason} 회차 상태는 공연 운영 설정에서 바꿀 수 있어요.`;
    renderBlocked(`${reason} 입장 진행(ENTRY_OPEN 전환)은 공연 운영 설정에서 처리해요.`);
}

function renderMetricsLine(metrics) {
    const host = lookup('[data-context-metrics]');
    if (!metrics) {
        host.textContent = '';
        return;
    }
    host.textContent = `입장 ${metrics.checkedInSeatCount} · 미입장 ${metrics.notCheckedInSeatCount}`;
}

async function loadMetrics() {
    const projectId = Number(readValue('entryProject'));
    const roundId = Number(readValue('entryRound'));
    if (!projectId || !roundId) {
        renderMetricsLine(null);
        return;
    }
    renderMetricsLine(await get(`/api/reservation-management/rounds/${roundId}/metrics`, {projectId}));
}

// ---------- 결과 영역 ----------

function feedback(tone, title, message, actions = []) {
    const host = lookup('[data-entry-feedback]');
    const tones = {
        success: ['border-success/30 bg-success-soft', 'text-success', 'M20 6L9 17l-5-5'],
        warning: ['border-warning/30 bg-warning-soft', 'text-warning', 'M12 9v4M12 17h.01M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z'],
        error: ['border-destructive/30 bg-destructive-soft', 'text-destructive', 'M12 9v4M12 17h.01M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z'],
        info: ['bg-card', 'text-info', 'M12 16v-4M12 8h.01M22 12A10 10 0 1 1 2 12a10 10 0 0 1 20 0z'],
    };
    const [boxClass, iconClass, iconPath] = tones[tone] || tones.info;
    host.className = `rounded-lg border p-4 ${boxClass}`;
    host.replaceChildren();
    const head = element('div', 'flex items-start gap-2.5');
    const icon = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
    icon.setAttribute('viewBox', '0 0 24 24');
    icon.setAttribute('fill', 'none');
    icon.setAttribute('stroke', 'currentColor');
    icon.setAttribute('stroke-width', '2');
    icon.setAttribute('stroke-linecap', 'round');
    icon.setAttribute('stroke-linejoin', 'round');
    icon.setAttribute('aria-hidden', 'true');
    icon.classList.add('mt-0.5', 'size-5', 'shrink-0', iconClass);
    const path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
    path.setAttribute('d', iconPath);
    icon.appendChild(path);
    const text = element('div', 'min-w-0');
    text.append(element('b', `block text-sm font-bold ${iconClass}`, title),
            element('p', 'mt-0.5 text-sm text-foreground', message));
    head.append(icon, text);
    host.appendChild(head);
    if (actions.length > 0) {
        const bar = element('div', 'mt-3 flex flex-wrap gap-2');
        actions.forEach(({label, action, primary}) => {
            const button = element('button', primary
                ? 'inline-flex min-h-11 items-center justify-center rounded-md bg-primary px-4 text-sm font-bold text-primary-foreground transition-colors hover:bg-primary-strong focus-visible:ring-2 focus-visible:ring-ring'
                : 'inline-flex min-h-11 items-center justify-center rounded-md border bg-card px-4 text-sm font-bold transition-colors hover:bg-secondary focus-visible:ring-2 focus-visible:ring-ring', label);
            button.type = 'button';
            button.dataset.pageAction = action;
            bar.appendChild(button);
        });
        host.appendChild(bar);
    }
    host.classList.remove('hidden');
}

function hideFeedback() {
    const host = lookup('[data-entry-feedback]');
    host.classList.add('hidden');
    host.replaceChildren();
}

function renderBlocked(message) {
    clearSensitiveState();
    currentReservation = null;
    lookupMethod = null;
    hideFeedback();
    lookup('[data-entry-detail]').classList.add('hidden');
    const empty = lookup('[data-entry-empty]');
    empty.textContent = message;
    empty.classList.remove('hidden');
}

function clearSensitiveState() {
    currentEntryToken = null;
    document.getElementById('entryToken').value = '';
}

function clearPersonalData() {
    ['[data-entry-reservation-no]', '[data-entry-applicant-name]', '[data-entry-phone]'].forEach((selector) => {
        lookup(selector).textContent = '';
    });
    lookup('[data-entry-seats]').replaceChildren();
    document.getElementById('entryReservationNo').value = '';
    document.getElementById('entryApplicantName').value = '';
}

function clearEntryResult(message = 'QR 또는 신청 정보로 관람 신청을 조회해 주세요.') {
    currentReservation = null;
    lookupMethod = null;
    clearSensitiveState();
    clearPersonalData();
    hideFeedback();
    const empty = lookup('[data-entry-empty]');
    empty.textContent = message;
    empty.classList.remove('hidden');
    lookup('[data-entry-detail]').classList.add('hidden');
}

function selectableSeatInputs() {
    return Array.from(document.querySelectorAll('[data-entry-seat-select]'));
}

function selectedSeatIds() {
    return selectableSeatInputs().filter((input) => input.checked)
            .map((input) => Number(input.value));
}

function selectedSeatLabels() {
    return selectableSeatInputs().filter((input) => input.checked)
            .map((input) => input.dataset.seatLabel);
}

function updateSelectionSummary() {
    const ids = selectedSeatIds();
    const labels = selectedSeatLabels();
    const summary = lookup('[data-entry-selection-summary]');
    summary.textContent = ids.length === 0
            ? '선택한 좌석이 없어요'
            : `선택: ${labels.join(', ')} (${ids.length}석)`;
    lookup(`[data-page-action="${ACTIONS.CHECK_IN}"]`).disabled = ids.length === 0;
    const selectAll = lookup(`[data-page-action="${ACTIONS.SELECT_ALL}"]`);
    if (selectAll) {
        selectAll.disabled = selectableSeatInputs().length === 0;
    }
}

function cancelButton(seat) {
    const button = element('button', 'inline-flex min-h-11 items-center justify-center rounded-md border px-3 text-xs font-bold text-destructive transition-colors hover:bg-destructive-soft focus-visible:ring-2 focus-visible:ring-ring', '입장 취소');
    button.type = 'button';
    button.dataset.pageAction = ACTIONS.CANCEL_OPEN;
    button.dataset.reservationSeatId = String(seat.reservationSeatId);
    button.dataset.seatLabel = seat.seatLabel;
    button.dataset.checkedInDttm = seat.checkedInDttm || '';
    return button;
}

function buildSeatControl(seat) {
    const cancelled = seat.status === 'CANCELLED';
    const checkedIn = Boolean(seat.checkedInDttm);
    const wrapper = element('div', `flex min-h-14 items-center gap-3 rounded-md border p-3 ${cancelled ? 'bg-secondary' : checkedIn ? 'bg-success-soft/50' : 'bg-card'}`);
    if (!cancelled && !checkedIn) {
        const input = element('input', 'size-5 shrink-0 accent-primary');
        input.type = 'checkbox';
        input.value = String(seat.reservationSeatId);
        input.dataset.entrySeatSelect = '';
        input.dataset.seatLabel = seat.seatLabel;
        input.id = `entrySeat${seat.reservationSeatId}`;
        input.addEventListener('change', updateSelectionSummary);
        wrapper.appendChild(input);
    } else {
        const icon = element('span', `flex size-5 shrink-0 items-center justify-center rounded-full text-xs font-bold ${cancelled ? 'bg-destructive-soft text-destructive' : 'bg-success text-white'}`, cancelled ? '×' : '✓');
        icon.setAttribute('aria-hidden', 'true');
        wrapper.appendChild(icon);
    }
    const content = element('div', 'min-w-0 flex-1');
    const label = element(cancelled || checkedIn ? 'strong' : 'label', 'block text-sm font-bold', seat.seatLabel);
    if (!cancelled && !checkedIn) {
        label.htmlFor = `entrySeat${seat.reservationSeatId}`;
    }
    content.appendChild(label);
    if (cancelled) {
        content.appendChild(element('span', 'mt-0.5 block text-xs text-destructive', '취소된 좌석이에요'));
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

function renderReservation(reservation, {moveFocus = true} = {}) {
    currentReservation = reservation;
    lookup('[data-entry-empty]').classList.add('hidden');
    lookup('[data-entry-detail]').classList.remove('hidden');
    lookup('[data-entry-reservation-no]').textContent = reservation.reservationNo;
    lookup('[data-entry-applicant-name]').textContent = reservation.applicantName;
    lookup('[data-entry-phone]').textContent = reservation.phone;
    const statusHost = lookup('[data-reservation-status]');
    const [label, tone] = RESERVATION_STATUS[reservation.status] || [reservation.status, 'neutral'];
    statusHost.replaceChildren(badge(label, tone));
    const seatHost = lookup('[data-entry-seats]');
    seatHost.replaceChildren();
    reservation.seats.forEach((seat) => seatHost.appendChild(buildSeatControl(seat)));
    if (reservation.seats.length === 0) {
        seatHost.appendChild(element('p', 'text-sm text-muted-foreground', '신청된 좌석이 없어요.'));
    }
    updateSelectionSummary();

    const total = reservation.seats.filter((seat) => seat.status !== 'CANCELLED');
    const checkedIn = total.filter((seat) => seat.checkedInDttm);
    if (reservation.status === 'CANCELLED') {
        feedback('error', '취소된 신청이에요', '입장 처리할 수 없어요. 관람객에게 신청 상태를 안내해 주세요.');
    } else if (total.length > 0 && checkedIn.length === total.length) {
        feedback('warning', '모든 좌석이 이미 입장했어요',
                `좌석 ${total.length}석이 모두 입장 완료 상태예요. 중복 입장인지 확인해 주세요.`,
                [{label: '다음 관람객 받기', action: ACTIONS.NEXT_GUEST, primary: true}]);
    } else if (checkedIn.length > 0) {
        feedback('info', '일부 좌석이 이미 입장했어요',
                `입장 ${checkedIn.length}석 · 미입장 ${total.length - checkedIn.length}석이에요. 지금 도착한 인원의 좌석만 선택해 주세요.`);
    } else {
        hideFeedback();
    }
    if (moveFocus) {
        lookup('[data-entry-result-title]').focus();
    }
}

// ---------- 조회·처리 ----------

async function withBusy(trigger, task) {
    trigger.disabled = true;
    try {
        await task();
    } catch (error) {
        feedback('error', '처리하지 못했어요', describeError(error));
    } finally {
        trigger.disabled = false;
    }
}

function validateEntryRound() {
    if (!entryIsOpen()) {
        feedback('error', '입장 진행 중인 회차가 아니에요', '입장 진행 상태인 회차를 선택해 주세요.');
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
    document.getElementById('entryToken').value = '';
    trigger.disabled = true;
    try {
        const reservation = await post(`/api/reservation-management/rounds/${readValue('entryRound')}/entry/lookup`, {
            entryToken: token,
        });
        currentEntryToken = token;
        lookupMethod = 'TOKEN';
        renderReservation(reservation);
    } catch (error) {
        clearEntryResult('일치하는 관람 신청을 찾지 못했어요.');
        feedback('error', '신청을 찾지 못했어요',
                `${describeError(error)} 이 회차의 QR이 아니거나 토큰이 일치하지 않을 수 있어요. 입력값은 안전하게 지웠어요.`);
        focusTokenInput();
    } finally {
        trigger.disabled = false;
    }
}

async function lookupManually(trigger) {
    const form = lookup('[data-manual-form]');
    if (!form.reportValidity() || !validateEntryRound()) {
        return;
    }
    const reservationNo = readValue('entryReservationNo');
    const applicantName = readValue('entryApplicantName');
    trigger.disabled = true;
    try {
        const reservation = await post(`/api/reservation-management/rounds/${readValue('entryRound')}/entry/search`, {
            reservationNo,
            applicantName,
        });
        lookupMethod = 'MANUAL';
        renderReservation(reservation);
    } catch (error) {
        clearEntryResult('일치하는 관람 신청을 찾지 못했어요.');
        feedback('error', '신청을 찾지 못했어요',
                `${describeError(error)} 신청번호와 이름을 다시 확인해 주세요.`);
        document.getElementById('entryReservationNo').focus();
    } finally {
        trigger.disabled = false;
    }
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
    renderReservation(currentReservation, {moveFocus: false});
}

async function checkIn(trigger) {
    if (!currentReservation || !validateEntryRound()) {
        return;
    }
    const reservationSeatIds = selectedSeatIds();
    const seatLabels = selectedSeatLabels();
    if (reservationSeatIds.length === 0) {
        feedback('warning', '좌석을 선택해 주세요', '입장 처리할 미입장 좌석을 먼저 선택해야 해요.');
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
        await refreshCurrentReservation();
        await loadMetrics();
        feedback('success', `${reservationSeatIds.length}석 입장을 처리했어요`,
                `${seatLabels.join(', ')} 좌석이 입장 완료됐어요.`,
                [{label: '다음 관람객 받기', action: ACTIONS.NEXT_GUEST, primary: true}]);
        lookup('[data-entry-feedback]').querySelector('button')?.focus();
    });
}

function selectAllPending() {
    selectableSeatInputs().forEach((input) => {
        input.checked = true;
    });
    updateSelectionSummary();
}

function nextGuest() {
    clearEntryResult();
    focusTokenInput();
}

// ---------- 입장 취소 ----------

function openCancel(trigger) {
    document.getElementById('entryCancelSeatId').value = trigger.dataset.reservationSeatId;
    document.getElementById('entryCancelReason').value = '';
    lookup('[data-entry-cancel-error]').classList.add('hidden');
    lookup('[data-entry-cancel-seat]').textContent = `${trigger.dataset.seatLabel} 좌석`;
    lookup('[data-entry-cancel-time]').textContent = trigger.dataset.checkedInDttm
            ? `입장 처리 시각: ${formatDateTime(trigger.dataset.checkedInDttm)}`
            : '';
    openSheet('entryCancelSheet', trigger);
}

async function cancelEntry(trigger) {
    const form = lookup('[data-entry-cancel-form]');
    if (!form.reportValidity() || !currentReservation || !validateEntryRound()) {
        return;
    }
    trigger.disabled = true;
    try {
        await post(`/api/reservation-management/rounds/${readValue('entryRound')}/entry/check-in-cancellations`, {
            reservationSeatId: Number(readValue('entryCancelSeatId')),
            reason: readValue('entryCancelReason'),
        });
        closeSheetOf(trigger);
        await refreshCurrentReservation();
        await loadMetrics();
        feedback('success', '좌석 입장을 취소했어요', '좌석이 다시 미입장 상태로 돌아갔어요.');
    } catch (error) {
        const errorArea = lookup('[data-entry-cancel-error]');
        errorArea.textContent = describeError(error);
        errorArea.classList.remove('hidden');
    } finally {
        trigger.disabled = false;
    }
}

// ---------- 로딩 ----------

async function loadRounds() {
    const projectId = Number(readValue('entryProject'));
    rounds = projectId
            ? await get(`/api/performance-management/projects/${projectId}/rounds`)
            : [];
    populateSelect(document.getElementById('entryRound'), rounds,
            'performanceRoundId', roundLabel, '등록된 회차가 없어요');
    renderRoundStatus();
    try {
        await loadMetrics();
    } catch (error) {
        renderMetricsLine(null);
    }
}

async function loadReferences() {
    projects = await get('/api/performance-management/projects', {limit: 100});
    projects = projects.filter((project) => project.status !== 'CANCELLED');
    populateSelect(document.getElementById('entryProject'), projects,
            'performanceProjectId',
            (project) => `${project.academicYear} ${project.termCode === 'FIRST' ? '1학기' : '2학기'} · ${project.title}`,
            '등록된 공연 프로젝트가 없어요');
    await loadRounds();
}

// ---------- 이벤트 ----------

document.getElementById('entryProject').addEventListener('change', async () => {
    try {
        await loadRounds();
    } catch (error) {
        showToast(describeError(error));
    }
});

document.getElementById('entryRound').addEventListener('change', async () => {
    renderRoundStatus();
    try {
        await loadMetrics();
    } catch (error) {
        renderMetricsLine(null);
        showToast(describeError(error));
    }
});

lookup('[data-token-form]').addEventListener('submit', (event) => {
    event.preventDefault();
    const trigger = event.submitter || lookup('[data-token-form] button[type="submit"]');
    lookupByToken(trigger);
});
lookup('[data-manual-form]').addEventListener('submit', (event) => {
    event.preventDefault();
    const trigger = event.submitter || lookup('[data-manual-form] button[type="submit"]');
    lookupManually(trigger);
});

bindPageActions({
    [ACTIONS.CHECK_IN]: checkIn,
    [ACTIONS.SELECT_ALL]: selectAllPending,
    [ACTIONS.NEXT_GUEST]: nextGuest,
    [ACTIONS.CANCEL_OPEN]: openCancel,
    [ACTIONS.CANCEL_SAVE]: cancelEntry,
});

clearEntryResult();
setLookupFormsEnabled(false);
loadReferences().catch((error) => {
    renderMetricsLine(null);
    clearEntryResult('공연 정보를 불러오지 못했어요. 잠시 후 다시 시도해 주세요.');
    feedback('error', '공연 정보를 불러오지 못했어요', describeError(error));
});
