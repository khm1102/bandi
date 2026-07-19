import {ApiError, get, patch} from '../common/api.js';
import {bindPageActions, element, lookup, readValue} from '../common/dom.js';
import {openSheet, closeSheet} from '../common/sheet.js';
import {showToast} from '../common/toast.js';
import {activateFilterChip, badge} from '../common/view.js';

const ACTIONS = Object.freeze({
    EXPORT: 'reservation-export',
    MORE: 'reservation-more',
    RETRY: 'reservation-retry',
    METRICS_RETRY: 'reservation-metrics-retry',
    FILTER_RESET: 'reservation-filter-reset',
    DETAIL_OPEN: 'reservation-detail-open',
    CANCEL_OPEN: 'reservation-cancel-open',
    CANCEL_CLOSE: 'reservation-cancel-close',
    CANCEL_SAVE: 'reservation-cancel-save',
});
const PAGE_SIZE = 25;
const STATUS_LABELS = Object.freeze({
    CONFIRMED: '확정',
    PARTIALLY_CANCELLED: '일부 취소',
    CANCELLED: '취소',
});
const ROUND_STATUS = Object.freeze({
    SCHEDULED: ['예정', 'neutral'],
    RESERVATION_OPEN: ['신청 중', 'info'],
    RESERVATION_CLOSED: ['신청 마감', 'warning'],
    ENTRY_OPEN: ['입장 중', 'success'],
    ENDED: ['종료', 'neutral'],
    CANCELLED: ['취소', 'danger'],
});

let projects = [];
let rounds = [];
let reservations = [];
let metrics = null;
let offset = 0;
let reachedEnd = false;
let loadingList = false;
let listRequestId = 0;
let exporting = false;
let detailReservation = null;
let sortKey = null;
let sortAscending = true;

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
        return '로그인이 만료됐어요. 다시 로그인해야 처리할 수 있어요. 화면의 목록과 필터는 그대로 남아 있어요.';
    }
    return error.message || '요청을 처리하지 못했어요. 잠시 후 다시 시도해 주세요.';
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
    const roundId = Number(readValue('reservationRound'));
    return rounds.find((round) => round.performanceRoundId === roundId) || null;
}

function selectedFilter() {
    const chip = lookup('[data-filter-group="reservation-status"][aria-pressed="true"]');
    return chip?.dataset.filterValue || 'ALL';
}

function setHidden(selector, hidden) {
    lookup(selector)?.classList.toggle('hidden', hidden);
}

function renderRoundStatus() {
    const host = lookup('[data-round-status]');
    host.replaceChildren();
    const round = selectedRound();
    if (!round) {
        host.appendChild(badge('회차 없음'));
        return;
    }
    const [label, tone] = ROUND_STATUS[round.status] || [round.status, 'neutral'];
    host.appendChild(badge(label, tone));
}

function renderMetrics() {
    const emptyValue = selectedRound() ? '—' : '0';
    lookup('[data-summary-count]').textContent = metrics
            ? String(metrics.reservationCount) : emptyValue;
    lookup('[data-summary-seats]').textContent = metrics
            ? String(metrics.reservedSeatCount) : emptyValue;
    lookup('[data-summary-checked]').textContent = metrics
            ? String(metrics.checkedInSeatCount) : emptyValue;
    lookup('[data-summary-rate]').textContent = metrics
            ? String(Math.round(Number(metrics.entryRate))) : emptyValue;
}

function setMetricsError(message = '') {
    const area = lookup('[data-metrics-error]');
    area.querySelector('[data-metrics-error-message]').textContent = message;
    area.classList.toggle('hidden', !message);
}

function validSeats(reservation) {
    return reservation.seats.filter((seat) => seat.status !== 'CANCELLED');
}

function sortedReservations() {
    if (!sortKey) {
        return reservations;
    }
    const copied = [...reservations];
    copied.sort((left, right) => {
        const a = String(left[sortKey] ?? '');
        const b = String(right[sortKey] ?? '');
        return sortAscending ? a.localeCompare(b, 'ko') : b.localeCompare(a, 'ko');
    });
    return copied;
}

function renderSortMarks() {
    document.querySelectorAll('[data-sort-mark]').forEach((mark) => {
        mark.textContent = mark.dataset.sortMark === sortKey ? (sortAscending ? '↑' : '↓') : '';
    });
}

function buildRow(reservation) {
    const row = element('button', 'w-full rounded-lg border bg-card p-4 text-left transition-colors hover:bg-secondary focus-visible:ring-2 focus-visible:ring-ring lg:grid lg:min-h-14 lg:grid-cols-[7rem_minmax(9rem,1.2fr)_minmax(10rem,1fr)_7rem_4rem] lg:items-center lg:gap-3 lg:rounded-none lg:border-x-0 lg:border-b lg:border-t-0 lg:px-4 lg:py-2');
    row.type = 'button';
    row.dataset.pageAction = ACTIONS.DETAIL_OPEN;
    row.dataset.reservationId = String(reservation.reservationId);
    row.setAttribute('aria-haspopup', 'dialog');

    const no = element('span', 'font-mono text-xs font-bold', reservation.reservationNo);
    const name = element('span', 'text-sm font-bold', reservation.applicantName);
    const phone = element('span', 'text-xs text-muted-foreground tabular-nums', reservation.phone || '-');
    const person = element('span', 'hidden min-w-0 lg:block');
    person.append(name, phone);
    const seats = validSeats(reservation);
    const seatText = seats.length > 0
            ? `${seats.map((seat) => seat.seatLabel).join(' ')} · ${seats.length}석`
            : '유효 좌석 없음';
    const seatSpan = element('span', 'text-xs text-muted-foreground', seatText);
    const statusCell = element('span', 'hidden lg:block');
    statusCell.appendChild(badge(STATUS_LABELS[reservation.status] || reservation.status, statusTone(reservation.status)));
    const openHint = element('span', 'hidden text-xs font-bold text-accent-foreground lg:block', '상세');

    const mobileTop = element('span', 'flex flex-wrap items-center gap-2 lg:hidden');
    mobileTop.append(no.cloneNode(true), name.cloneNode(true));
    const mobileBottom = element('span', 'mt-1.5 flex flex-wrap items-center gap-2 lg:hidden');
    mobileBottom.append(seatSpan.cloneNode(true), badge(STATUS_LABELS[reservation.status] || reservation.status, statusTone(reservation.status)));
    [no, seatSpan, openHint].forEach((node) => node.classList.add('hidden', 'lg:block'));
    row.append(mobileTop, mobileBottom, no, person, seatSpan, statusCell, openHint);
    return row;
}

function renderList() {
    const list = lookup('[data-reservation-list]');
    list.replaceChildren();
    const filterActive = selectedFilter() !== 'ALL';
    const hasContext = projects.length > 0 && Boolean(selectedRound());
    setHidden('[data-list-error]', true);
    if (!hasContext) {
        showEmpty(projects.length === 0 ? '공연 프로젝트가 없어요' : '이 공연에는 회차가 없어요',
                '공연 운영 설정에서 프로젝트와 회차를 먼저 만들어야 신청을 받을 수 있어요.', {management: true});
        setHidden('[data-page-action="reservation-more"]', true);
        setHidden('[data-list-end]', true);
        return;
    }
    if (reservations.length === 0) {
        if (filterActive) {
            showEmpty('이 상태의 신청이 없어요', '필터를 초기화하면 전체 신청을 볼 수 있어요.', {filter: true});
        } else {
            showEmpty('아직 관람 신청이 없어요', '신청 기간이 열려 있는지 회차 상태를 확인해 보세요.', {});
        }
        setHidden('[data-page-action="reservation-more"]', true);
        setHidden('[data-list-end]', true);
        return;
    }
    setHidden('[data-list-empty]', true);
    list.append(...sortedReservations().map(buildRow));
    setHidden('[data-page-action="reservation-more"]', reachedEnd);
    setHidden('[data-list-end]', !reachedEnd);
    renderSortMarks();
}

function showEmpty(title, message, {filter = false, management = false}) {
    setHidden('[data-list-empty]', false);
    lookup('[data-list-empty-title]').textContent = title;
    lookup('[data-list-empty-message]').textContent = message;
    const resetButton = lookup(`[data-page-action="${ACTIONS.FILTER_RESET}"]`);
    resetButton?.classList.toggle('hidden', !filter);
    const managementLink = lookup('[data-empty-management-link]');
    managementLink?.classList.toggle('hidden', !management);
    managementLink?.classList.toggle('inline-flex', management);
}

function setListLoading(loading) {
    loadingList = loading;
    lookup('[data-reservation-region]').setAttribute('aria-busy', String(loading));
    setHidden('[data-list-loading]', !loading);
    lookup('[data-list-loading]')?.classList.toggle('flex', loading);
    const more = lookup(`[data-page-action="${ACTIONS.MORE}"]`);
    if (more) {
        more.disabled = loading;
    }
}

function requestContext() {
    return {
        roundId: Number(readValue('reservationRound')),
        projectId: Number(readValue('reservationProject')),
        status: selectedFilter(),
    };
}

async function fetchPage(nextOffset, context = requestContext()) {
    return get(`/api/reservation-management/rounds/${context.roundId}/reservations`, {
        projectId: context.projectId,
        status: context.status === 'ALL' ? null : context.status,
        offset: nextOffset,
        limit: PAGE_SIZE,
    });
}

async function fetchMetrics(context = requestContext()) {
    return get(`/api/reservation-management/rounds/${context.roundId}/metrics`, {
        projectId: context.projectId,
    });
}

async function loadReservations({append = false} = {}) {
    if (loadingList && append) {
        return;
    }
    const context = requestContext();
    const requestId = ++listRequestId;
    if (!append) {
        offset = 0;
        reachedEnd = false;
        reservations = [];
        metrics = null;
        renderMetrics();
        setMetricsError();
        lookup('[data-reservation-list]').replaceChildren();
        setHidden('[data-list-empty]', true);
        setHidden('[data-list-error]', true);
    }
    if (!context.projectId || !context.roundId) {
        metrics = null;
        renderMetrics();
        renderList();
        setListLoading(false);
        return;
    }
    setListLoading(true);
    try {
        const [pageResult, metricsResult] = await Promise.allSettled([
            fetchPage(offset, context),
            append ? Promise.resolve(metrics)
                : fetchMetrics(context),
        ]);
        if (requestId !== listRequestId) {
            return;
        }
        if (metricsResult.status === 'fulfilled') {
            metrics = metricsResult.value;
            setMetricsError();
        } else if (!append) {
            metrics = null;
            setMetricsError('신청 요약을 불러오지 못했어요. 신청 목록은 계속 확인할 수 있어요.');
        }
        if (pageResult.status === 'rejected') {
            throw pageResult.reason;
        }
        const page = pageResult.value;
        reservations = append ? [...reservations, ...page] : page;
        offset += page.length;
        reachedEnd = page.length < PAGE_SIZE;
        renderMetrics();
        renderList();
    } catch (error) {
        if (requestId !== listRequestId) {
            return;
        }
        renderMetrics();
        if (reservations.length === 0) {
            lookup('[data-reservation-list]').replaceChildren();
            setHidden('[data-list-empty]', true);
        }
        setHidden('[data-list-error]', false);
        showToast(describeError(error));
    } finally {
        if (requestId === listRequestId) {
            setListLoading(false);
        }
    }
}

async function retryMetrics(trigger) {
    const context = requestContext();
    if (!context.projectId || !context.roundId) {
        return;
    }
    trigger.disabled = true;
    try {
        metrics = await fetchMetrics(context);
        renderMetrics();
        setMetricsError();
    } catch (error) {
        setMetricsError(describeError(error));
    } finally {
        trigger.disabled = false;
    }
}

async function loadRounds() {
    const projectId = Number(readValue('reservationProject'));
    rounds = projectId
            ? await get(`/api/performance-management/projects/${projectId}/rounds`)
            : [];
    populateSelect(document.getElementById('reservationRound'), rounds,
            'performanceRoundId', roundLabel, '등록된 회차가 없어요');
    renderRoundStatus();
}

async function loadReferences() {
    projects = await get('/api/performance-management/projects', {limit: 100});
    projects = projects.filter((project) => project.status !== 'CANCELLED');
    populateSelect(document.getElementById('reservationProject'), projects,
            'performanceProjectId',
            (project) => `${project.academicYear} ${project.termCode === 'FIRST' ? '1학기' : '2학기'} · ${project.title}`,
            '등록된 공연 프로젝트가 없어요');
    await loadRounds();
    await loadReservations();
}

// ---------- 상세 sheet ----------

function seatItem(seat) {
    const item = element('li', 'flex min-h-11 items-center gap-3 rounded-md border px-3 py-2');
    const cancelled = seat.status === 'CANCELLED';
    const checkedIn = Boolean(seat.checkedInDttm);
    const icon = element('span', `flex size-5 shrink-0 items-center justify-center rounded-full text-xs font-bold ${cancelled ? 'bg-destructive-soft text-destructive' : checkedIn ? 'bg-success-soft text-success' : 'bg-secondary text-muted-foreground'}`, cancelled ? '×' : checkedIn ? '✓' : '·');
    icon.setAttribute('aria-hidden', 'true');
    const text = element('span', 'min-w-0 flex-1');
    text.append(element('b', 'block text-sm font-bold', seat.seatLabel),
            element('span', 'block text-xs text-muted-foreground',
                    cancelled ? '취소된 좌석' : checkedIn ? `입장 ${formatDateTime(seat.checkedInDttm)}` : '미입장'));
    item.append(icon, text);
    return item;
}

function openDetail(trigger) {
    detailReservation = reservations.find((candidate) =>
        String(candidate.reservationId) === trigger.dataset.reservationId);
    if (!detailReservation) {
        return;
    }
    lookup('[data-detail-no]').textContent = detailReservation.reservationNo;
    lookup('[data-detail-name]').textContent = detailReservation.applicantName;
    lookup('[data-detail-phone]').textContent = detailReservation.phone;
    lookup('[data-detail-status]').replaceChildren(
            badge(STATUS_LABELS[detailReservation.status] || detailReservation.status,
                    statusTone(detailReservation.status)));
    const seats = lookup('[data-detail-seats]');
    seats.replaceChildren(...detailReservation.seats.map(seatItem));
    if (detailReservation.seats.length === 0) {
        seats.appendChild(element('li', 'text-sm text-muted-foreground', '신청된 좌석이 없어요.'));
    }
    const cancelable = detailReservation.cancelable && detailReservation.status !== 'CANCELLED';
    lookup(`[data-page-action="${ACTIONS.CANCEL_OPEN}"]`).classList.toggle('hidden', !cancelable);
    setHidden('[data-detail-not-cancelable]', cancelable);
    setHidden('[data-detail-cancel-open-area]', false);
    setHidden('[data-detail-cancel-area]', true);
    setHidden('[data-detail-cancel-success]', true);
    document.getElementById('cancelReason').value = '';
    lookup('[data-cancel-error]').classList.add('hidden');
    openSheet('reservationDetailSheet', trigger);
}

function openCancelArea() {
    const seats = validSeats(detailReservation);
    lookup('[data-cancel-summary]').textContent =
            `${detailReservation.reservationNo} · ${detailReservation.applicantName} · ${seats.map((seat) => seat.seatLabel).join(', ') || '좌석 없음'} · 현재 ${STATUS_LABELS[detailReservation.status] || detailReservation.status}`;
    setHidden('[data-detail-cancel-open-area]', true);
    setHidden('[data-detail-cancel-area]', false);
    document.getElementById('cancelReason').focus();
}

function closeCancelArea() {
    setHidden('[data-detail-cancel-area]', true);
    setHidden('[data-detail-cancel-open-area]', false);
}

async function cancelReservation(trigger) {
    const reason = readValue('cancelReason');
    const errorArea = lookup('[data-cancel-error]');
    if (!reason) {
        errorArea.textContent = '취소 사유를 입력해 주세요. 사유는 운영 기록에 남아요.';
        errorArea.classList.remove('hidden');
        document.getElementById('cancelReason').focus();
        return;
    }
    trigger.disabled = true;
    try {
        await patch(`/api/reservation-management/reservations/${detailReservation.reservationId}/cancel`, {
            performanceProjectId: Number(readValue('reservationProject')),
            reason,
        });
        setHidden('[data-detail-cancel-area]', true);
        setHidden('[data-detail-cancel-success]', false);
        await loadReservations();
    } catch (error) {
        errorArea.textContent = describeError(error);
        errorArea.classList.remove('hidden');
    } finally {
        trigger.disabled = false;
    }
}

// ---------- CSV ----------

function csvCell(value) {
    let text = String(value ?? '');
    if (/^[=+\-@]/.test(text)) {
        text = `'${text}`;
    }
    return `"${text.replaceAll('"', '""')}"`;
}

async function collectAllPages(context) {
    const collected = [];
    const seenIds = new Set();
    let cursor = 0;
    const progress = lookup('[data-export-progress]');
    while (true) {
        progress.textContent = `${collected.length}건 수집 중이에요…`;
        const page = await fetchPage(cursor, context);
        if (page.length > 0 && page.every((item) => seenIds.has(item.reservationId))) {
            throw new Error('같은 신청 목록이 반복되어 내보내기를 중단했어요. 잠시 후 다시 시도해 주세요.');
        }
        page.forEach((item) => seenIds.add(item.reservationId));
        collected.push(...page);
        cursor += page.length;
        if (page.length < PAGE_SIZE) {
            return collected;
        }
    }
}

async function exportReservations(trigger) {
    if (exporting) {
        return;
    }
    if (!selectedRound()) {
        showToast('먼저 회차를 선택해 주세요.');
        return;
    }
    exporting = true;
    trigger.disabled = true;
    const context = requestContext();
    const progress = lookup('[data-export-progress]');
    try {
        const all = await collectAllPages(context);
        if (all.length === 0) {
            progress.textContent = '내보낼 신청이 없어요.';
            return;
        }
        const rows = all.map((reservation) => [
            reservation.reservationNo,
            reservation.applicantName,
            reservation.phone,
            validSeats(reservation).map((seat) => seat.seatLabel).join(' '),
            validSeats(reservation).length,
            STATUS_LABELS[reservation.status] || reservation.status,
        ].map(csvCell).join(','));
        const header = ['신청번호', '관람객명', '연락처', '좌석', '유효좌석수', '상태'].map(csvCell).join(',');
        const csv = [header, ...rows].join('\r\n');
        const url = URL.createObjectURL(new Blob([`\u{FEFF}${csv}`], {type: 'text/csv;charset=utf-8'}));
        const anchor = element('a');
        anchor.href = url;
        anchor.download = `bandi_reservations_round_${context.roundId}.csv`;
        anchor.click();
        URL.revokeObjectURL(url);
        progress.textContent = `${all.length}건을 CSV로 내보냈어요.`;
    } catch (error) {
        progress.textContent = '내보내기에 실패했어요. 파일은 만들어지지 않았어요. 다시 시도해 주세요.';
        showToast(describeError(error));
    } finally {
        exporting = false;
        trigger.disabled = false;
    }
}

// ---------- 이벤트 ----------

function resetFilter() {
    const allChip = lookup('[data-filter-group="reservation-status"][data-filter-value="ALL"]');
    if (allChip) {
        activateFilterChip(allChip);
    }
    loadReservations().catch((error) => showToast(describeError(error)));
}

document.getElementById('reservationProject').addEventListener('change', async () => {
    try {
        await loadRounds();
        await loadReservations();
    } catch (error) {
        showToast(describeError(error));
    }
});
document.getElementById('reservationRound').addEventListener('change', () => {
    renderRoundStatus();
    loadReservations().catch((error) => showToast(describeError(error)));
});
document.addEventListener('click', (event) => {
    const filter = event.target.closest('[data-filter-group="reservation-status"]');
    if (filter) {
        activateFilterChip(filter);
        loadReservations().catch((error) => showToast(describeError(error)));
        return;
    }
    const sortButton = event.target.closest('[data-sort-key]');
    if (sortButton) {
        const key = sortButton.dataset.sortKey;
        sortAscending = sortKey === key ? !sortAscending : true;
        sortKey = key;
        renderList();
    }
});

bindPageActions({
    [ACTIONS.EXPORT]: exportReservations,
    [ACTIONS.MORE]: () => loadReservations({append: true}),
    [ACTIONS.RETRY]: () => loadReservations(),
    [ACTIONS.METRICS_RETRY]: retryMetrics,
    [ACTIONS.FILTER_RESET]: resetFilter,
    [ACTIONS.DETAIL_OPEN]: openDetail,
    [ACTIONS.CANCEL_OPEN]: openCancelArea,
    [ACTIONS.CANCEL_CLOSE]: closeCancelArea,
    [ACTIONS.CANCEL_SAVE]: cancelReservation,
});

loadReferences().catch((error) => {
    lookup('[data-reservation-region]').setAttribute('aria-busy', 'false');
    setHidden('[data-list-error]', false);
    showToast(describeError(error));
});
