import {ApiError, get, post, put} from '../common/api.js';
import {all, bindPageActions, element, lookup, readValue} from '../common/dom.js';
import {openModal} from '../common/modal.js';
import {currentUserRole} from '../common/session.js';
import {showToast} from '../common/toast.js';
import {badge, closeActionModal} from '../common/view.js';

const ACTIONS = Object.freeze({
    PAY: 'fee-pay',
    UNPAY: 'fee-unpay',
    CREATE_OPEN: 'fee-create-open',
    EDIT_OPEN: 'fee-edit-open',
    SAVE: 'fee-save',
    OPEN: 'fee-open',
    CLOSE: 'fee-close',
    CANCEL_OPEN: 'fee-cancel-open',
    CANCEL: 'fee-cancel',
    HISTORY_OPEN: 'fee-history-open',
});
const STATUS_LABELS = Object.freeze({
    DRAFT: ['초안', 'neutral'],
    OPEN: ['부과 중', 'info'],
    CLOSED: ['마감', 'success'],
    PAID: ['납부 완료', 'success'],
    UNPAID: ['미납', 'warning'],
    EXEMPT: ['면제', 'info'],
    CANCELLED: ['취소', 'neutral'],
});

let feeItems = [];
let selectedItem = null;
let charges = [];
let editingItemId = null;

function errorMessage(error) {
    if (error instanceof ApiError && error.fieldErrors.length > 0) {
        return error.fieldErrors[0].reason;
    }
    return error.message || '요청을 처리하지 못했습니다.';
}

function money(value) {
    return Number(value || 0).toLocaleString('ko-KR');
}

function date(value) {
    return value ? value.slice(0, 10) : '—';
}

function dateTime(value) {
    if (!value) {
        return '—';
    }
    return new Intl.DateTimeFormat('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
    }).format(new Date(value));
}

function setError(selector, message) {
    const node = lookup(selector);
    node.textContent = message || '';
    node.classList.toggle('hidden', !message);
}

function statusBadge(status) {
    const [label, tone] = STATUS_LABELS[status] || [status, 'neutral'];
    return badge(label, tone);
}

function setMyState(title, message, retry = false) {
    const state = lookup('[data-my-fee-state]');
    state.hidden = false;
    lookup('[data-my-fee-state-title]', state).textContent = title;
    lookup('[data-my-fee-state-message]', state).textContent = message;
    lookup('[data-my-fee-retry]', state).classList.toggle('hidden', !retry);
}

function renderMyFees(items, summary) {
    all('[data-my-fee-list] tr:not([data-my-fee-state])')
            .forEach((row) => row.remove());
    lookup('[data-stat-value="my-total"]').textContent = money(summary.totalAmount);
    lookup('[data-stat-value="my-paid"]').textContent = money(summary.paidAmount);
    lookup('[data-stat-value="my-unpaid"]').textContent = money(summary.unpaidAmount);
    if (items.length === 0) {
        setMyState('부과된 회비가 없습니다', '현재 확인할 회비 항목이 없습니다.');
        return;
    }
    lookup('[data-my-fee-state]').hidden = true;
    const template = lookup('[data-my-fee-row-template]');
    items.forEach((item) => {
        const row = template.content.firstElementChild.cloneNode(true);
        lookup('[data-my-fee-name]', row).textContent = item.itemName;
        lookup('[data-my-fee-term]', row).textContent =
                `${item.referenceYear} ${item.referenceTermCode || ''}`.trim();
        lookup('[data-my-fee-amount]', row).textContent = `${money(item.chargedAmount)}원`;
        lookup('[data-my-fee-status]', row).appendChild(statusBadge(item.status));
        lookup('[data-my-fee-paid-date]', row).textContent = date(item.paidDttm);
        lookup('[data-my-fee-due-date]', row).textContent = date(item.dueDate);
        lookup('[data-my-fee-list]').appendChild(row);
    });
}

async function loadMyFees() {
    setMyState('회비 내역을 불러오는 중입니다', '잠시만 기다려 주세요.');
    try {
        const [items, summary] = await Promise.all([
            get('/api/fees/mine'),
            get('/api/fees/mine/summary'),
        ]);
        renderMyFees(items, summary);
    } catch (error) {
        setMyState('회비 내역을 불러오지 못했습니다', errorMessage(error), true);
    }
}

function createTab(item) {
    const template = lookup('[data-fee-tab-template]');
    const tab = template.content.firstElementChild.cloneNode(true);
    tab.dataset.feeItemId = item.feeItemId;
    tab.textContent = item.name;
    return tab;
}

function activateTab(itemId) {
    selectedItem = feeItems.find((item) => item.feeItemId === Number(itemId));
    if (!selectedItem) {
        return;
    }
    all('[data-fee-tab]').forEach((tab) => {
        const selected = Number(tab.dataset.feeItemId) === selectedItem.feeItemId;
        tab.setAttribute('aria-selected', String(selected));
        tab.tabIndex = selected ? 0 : -1;
        tab.classList.toggle('border', selected);
        tab.classList.toggle('bg-card', selected);
        tab.classList.toggle('text-foreground', selected);
        tab.classList.toggle('text-muted-foreground', !selected);
    });
    renderItemContext();
    loadCharges();
}

function renderItemContext() {
    lookup('[data-fee-item-name]').textContent = selectedItem.name;
    lookup('[data-fee-item-description]').textContent =
            selectedItem.description || '등록된 설명이 없습니다.';
    lookup('[data-fee-item-due]').textContent = `${date(selectedItem.dueDate)}까지`;
    const status = lookup('[data-fee-item-status]');
    status.replaceChildren(statusBadge(selectedItem.status));
    lookup('[data-page-action="fee-edit-open"]').classList.toggle('hidden',
            selectedItem.status !== 'DRAFT');
    lookup('[data-page-action="fee-open"]').classList.toggle('hidden',
            selectedItem.status !== 'DRAFT');
    lookup('[data-page-action="fee-close"]').classList.toggle('hidden',
            selectedItem.status !== 'OPEN');
    lookup('[data-page-action="fee-cancel-open"]').classList.remove('hidden');
    lookup('[data-fee-process-controls]').classList.toggle('hidden',
            selectedItem.status === 'DRAFT');
}

function renderItems() {
    const tabs = lookup('[data-fee-tabs]');
    tabs.replaceChildren();
    const available = feeItems.filter((item) => item.status !== 'CANCELLED');
    lookup('[data-fee-empty]').classList.toggle('hidden', available.length > 0);
    lookup('[data-fee-workspace]').classList.toggle('hidden', available.length === 0);
    if (available.length === 0) {
        selectedItem = null;
        ['fee-edit-open', 'fee-open', 'fee-close', 'fee-cancel-open']
                .forEach((action) => lookup(`[data-page-action="${action}"]`)
                        .classList.add('hidden'));
        return;
    }
    available.forEach((item) => tabs.appendChild(createTab(item)));
    activateTab(selectedItem && available.some((item) =>
        item.feeItemId === selectedItem.feeItemId)
        ? selectedItem.feeItemId : available[0].feeItemId);
}

function setChargeState(title, message) {
    const state = lookup('[data-fee-charge-state]');
    state.hidden = false;
    lookup('[data-fee-charge-state-title]', state).textContent = title;
    lookup('[data-fee-charge-state-message]', state).textContent = message;
}

function renderSummary() {
    lookup('[data-stat-value="fee-amount"]').textContent = money(selectedItem.amount);
    lookup('[data-stat-value="fee-paid"]').textContent =
            charges.filter((charge) => charge.status === 'PAID').length;
    lookup('[data-stat-value="fee-unpaid"]').textContent =
            charges.filter((charge) => charge.status === 'UNPAID').length;
    lookup('[data-stat-value="fee-collected"]').textContent = money(
            charges.filter((charge) => charge.status === 'PAID')
                    .reduce((sum, charge) => sum + charge.chargedAmount, 0));
}

function renderCharges() {
    all('[data-fee-charge-row]').forEach((row) => row.remove());
    renderSummary();
    if (charges.length === 0) {
        setChargeState('부과된 멤버가 없습니다', '이 항목의 부과 대상을 확인해 주세요.');
        return;
    }
    lookup('[data-fee-charge-state]').hidden = true;
    const template = lookup('[data-fee-charge-row-template]');
    charges.forEach((charge) => {
        const row = template.content.firstElementChild.cloneNode(true);
        row.dataset.feeChargeId = charge.feeChargeId;
        const checkbox = lookup('[data-fee-person]', row);
        checkbox.setAttribute('aria-label', `${charge.memberName} 선택`);
        lookup('[data-fee-avatar]', row).textContent = Array.from(charge.memberName)[0] || '?';
        lookup('[data-fee-member-name]', row).textContent = charge.memberName;
        lookup('[data-fee-charged-amount]', row).textContent = `${money(charge.chargedAmount)}원`;
        lookup('[data-fee-status]', row).appendChild(statusBadge(charge.status));
        lookup('[data-fee-date]', row).textContent = date(charge.paidDttm);
        const history = lookup('[data-page-action="fee-history-open"]', row);
        history.dataset.feeChargeId = charge.feeChargeId;
        history.dataset.memberName = charge.memberName;
        history.setAttribute('aria-label', `${charge.memberName} 수납 상태 변경 이력`);
        lookup('[data-fee-charge-list]').appendChild(row);
    });
    lookup('[data-fee-all]').checked = false;
}

async function loadCharges() {
    setChargeState('부과 명단을 불러오는 중입니다', '잠시만 기다려 주세요.');
    try {
        charges = await get(`/api/fee-management/${selectedItem.feeItemId}/charges`);
        renderCharges();
    } catch (error) {
        setChargeState('부과 명단을 불러오지 못했습니다', errorMessage(error));
    }
}

async function loadItems() {
    try {
        feeItems = await get('/api/fee-management');
        renderItems();
    } catch (error) {
        lookup('[data-fee-workspace]').classList.add('hidden');
        lookup('[data-fee-empty]').classList.remove('hidden');
        showToast(errorMessage(error));
    }
}

async function processCharges(status) {
    const ids = all('[data-fee-charge-row]').filter((row) =>
        lookup('[data-fee-person]', row).checked).map((row) =>
        Number(row.dataset.feeChargeId));
    if (ids.length === 0) {
        showToast('처리할 멤버를 선택해 주세요.');
        return;
    }
    try {
        await post(`/api/fee-management/${selectedItem.feeItemId}/charges/process`, {
            feeChargeIds: ids,
            status,
            reason: '운영 화면 일괄 처리',
        });
        showToast(`${ids.length}명의 수납 상태를 변경했습니다.`);
        await loadCharges();
    } catch (error) {
        showToast(errorMessage(error));
    }
}

function feeItemPayload() {
    const dueDate = readValue('feeDue');
    return {
        name: readValue('feeName'),
        description: readValue('feeDescription'),
        referenceYear: dueDate ? Number(dueDate.slice(0, 4)) : 0,
        referenceTermCode: readValue('feeTerm'),
        amount: Number(readValue('feeAmt')),
        dueDate,
    };
}

function fillFeeForm(item) {
    document.getElementById('feeName').value = item?.name || '';
    document.getElementById('feeDescription').value = item?.description || '';
    document.getElementById('feeAmt').value = item?.amount || '';
    document.getElementById('feeDue').value = item?.dueDate || '';
    document.getElementById('feeTerm').value = item?.referenceTermCode || '';
}

function openFeeForm(trigger, item) {
    editingItemId = item?.feeItemId || null;
    fillFeeForm(item);
    setError('[data-fee-form-error]', '');
    document.getElementById('feeModalTitle').textContent =
            item ? '회비 초안 수정' : '회비 초안 추가';
    document.getElementById('feeModalDescription').textContent = item
        ? '부과를 시작하기 전까지 항목 정보를 수정할 수 있습니다.'
        : '초안으로 저장한 뒤 내용을 확인하고 부과를 시작합니다.';
    lookup('[data-fee-submit-label]').textContent = item ? '수정 저장' : '초안 저장';
    openModal('feeModal', trigger);
}

function openCreateForm(trigger) {
    openFeeForm(trigger, null);
}

function openEditForm(trigger) {
    if (!selectedItem || selectedItem.status !== 'DRAFT') {
        showToast('초안 상태의 회비만 수정할 수 있습니다.');
        return;
    }
    openFeeForm(trigger, selectedItem);
}

async function saveFee(trigger) {
    const targetId = editingItemId;
    trigger.disabled = true;
    try {
        if (targetId) {
            await put(`/api/fee-management/${targetId}`, feeItemPayload());
        } else {
            const created = await post('/api/fee-management', feeItemPayload());
            selectedItem = {feeItemId: created.feeItemId};
        }
        closeActionModal(trigger);
        showToast(targetId ? '회비 초안을 수정했습니다.' : '회비 초안을 저장했습니다.');
        editingItemId = null;
        await loadItems();
    } catch (error) {
        setError('[data-fee-form-error]', errorMessage(error));
    } finally {
        trigger.disabled = false;
    }
}

async function openFee() {
    if (!selectedItem || selectedItem.status !== 'DRAFT') {
        showToast('부과를 시작할 회비 초안을 선택해 주세요.');
        return;
    }
    try {
        await post(`/api/fee-management/${selectedItem.feeItemId}/open`, {
            selectedMemberIds: [],
        });
        showToast('전체 활성 멤버에게 회비를 부과했습니다.');
        await loadItems();
    } catch (error) {
        showToast(errorMessage(error));
    }
}

async function closeFee() {
    if (!selectedItem || selectedItem.status !== 'OPEN') {
        showToast('부과 중인 회비 항목을 선택해 주세요.');
        return;
    }
    try {
        await post(`/api/fee-management/${selectedItem.feeItemId}/close`);
        showToast('회비 부과를 마감했습니다.');
        await loadItems();
    } catch (error) {
        showToast(errorMessage(error));
    }
}

function openCancel(trigger) {
    if (!selectedItem) {
        showToast('취소할 회비 항목이 없습니다.');
        return;
    }
    document.getElementById('feeCancelReason').value = '';
    setError('[data-fee-cancel-error]', '');
    openModal('feeCancelModal', trigger);
}

async function cancelFee(trigger) {
    trigger.disabled = true;
    try {
        await post(`/api/fee-management/${selectedItem.feeItemId}/cancel`, {
            reason: readValue('feeCancelReason'),
        });
        closeActionModal(trigger);
        showToast('회비 항목을 취소했습니다.');
        await loadItems();
    } catch (error) {
        setError('[data-fee-cancel-error]', errorMessage(error));
    } finally {
        trigger.disabled = false;
    }
}

async function showChargeHistory(trigger) {
    const region = lookup('[data-fee-history]');
    lookup('[data-fee-history-member]').textContent = trigger.dataset.memberName;
    region.replaceChildren(element('p',
            'py-8 text-center text-sm text-muted-foreground',
            '변경 이력을 불러오는 중입니다.'));
    openModal('feeHistoryModal', trigger);
    try {
        const histories = await get(
                `/api/fee-management/charges/${trigger.dataset.feeChargeId}/histories`);
        if (histories.length === 0) {
            region.replaceChildren(element('p',
                    'rounded-md bg-secondary px-4 py-3 text-sm text-muted-foreground',
                    '수납 상태 변경 이력이 없습니다.'));
            return;
        }
        region.replaceChildren();
        histories.forEach((history) => {
            const card = element('article', 'rounded-lg border px-4 py-3');
            const head = element('div', 'flex flex-wrap items-center gap-2');
            head.append(statusBadge(history.previousStatus),
                    element('span', 'text-xs text-muted-foreground', '→'),
                    statusBadge(history.newStatus));
            card.append(head, element('p',
                    'mt-2 text-xs text-muted-foreground',
                    `${history.changedByName || '알 수 없음'} · ${dateTime(history.changedDttm)}`));
            if (history.reason) {
                card.appendChild(element('p', 'mt-2 text-sm', history.reason));
            }
            region.appendChild(card);
        });
    } catch (error) {
        region.replaceChildren(element('p',
                'rounded-md bg-destructive-soft px-4 py-3 text-sm text-destructive',
                errorMessage(error)));
    }
}

if (currentUserRole === 'admin') {
    lookup('[data-fee-tabs]').addEventListener('click', (event) => {
        const tab = event.target.closest('[data-fee-tab]');
        if (tab) {
            activateTab(tab.dataset.feeItemId);
        }
    });
    lookup('[data-fee-all]').addEventListener('change', (event) => {
        all('[data-fee-person]').forEach((checkbox) => {
            checkbox.checked = event.target.checked;
        });
    });
    bindPageActions({
        [ACTIONS.PAY]: () => processCharges('PAID'),
        [ACTIONS.UNPAY]: () => processCharges('UNPAID'),
        [ACTIONS.CREATE_OPEN]: openCreateForm,
        [ACTIONS.EDIT_OPEN]: openEditForm,
        [ACTIONS.SAVE]: saveFee,
        [ACTIONS.OPEN]: openFee,
        [ACTIONS.CLOSE]: closeFee,
        [ACTIONS.CANCEL_OPEN]: openCancel,
        [ACTIONS.CANCEL]: cancelFee,
        [ACTIONS.HISTORY_OPEN]: showChargeHistory,
    });
    loadItems();
} else {
    lookup('[data-my-fee-retry]').addEventListener('click', loadMyFees);
    loadMyFees();
}
