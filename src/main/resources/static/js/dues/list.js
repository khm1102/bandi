import {ApiError, get, post} from '../common/api.js';
import {all, bindPageActions, element, lookup, readValue} from '../common/dom.js';
import {openModal} from '../common/modal.js';
import {currentUserRole} from '../common/session.js';
import {showToast} from '../common/toast.js';
import {badge, closeActionModal} from '../common/view.js';

const ACTIONS = Object.freeze({
    PAY: 'fee-pay',
    UNPAY: 'fee-unpay',
    ADD: 'fee-add',
    CANCEL_OPEN: 'fee-cancel-open',
    CANCEL: 'fee-cancel',
});
const STATUS_LABELS = Object.freeze({
    PAID: ['납부 완료', 'success'],
    UNPAID: ['미납', 'warning'],
    EXEMPT: ['면제', 'info'],
    CANCELLED: ['취소', 'neutral'],
});

let feeItems = [];
let selectedItem = null;
let charges = [];
let pendingCreatedFeeItemId = null;

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
    all('[data-fee-tab]').forEach((tab) => {
        const selected = Number(tab.dataset.feeItemId) === selectedItem.feeItemId;
        tab.setAttribute('aria-selected', String(selected));
        tab.tabIndex = selected ? 0 : -1;
        tab.classList.toggle('border', selected);
        tab.classList.toggle('bg-card', selected);
        tab.classList.toggle('text-foreground', selected);
        tab.classList.toggle('text-muted-foreground', !selected);
    });
    loadCharges();
}

function renderItems() {
    const tabs = lookup('[data-fee-tabs]');
    tabs.replaceChildren();
    const available = feeItems.filter((item) => item.status !== 'CANCELLED');
    lookup('[data-fee-empty]').classList.toggle('hidden', available.length > 0);
    lookup('[data-fee-workspace]').classList.toggle('hidden', available.length === 0);
    if (available.length === 0) {
        selectedItem = null;
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

async function addFee(trigger) {
    const dueDate = readValue('feeDue');
    const amount = Number(readValue('feeAmt'));
    setError('[data-fee-form-error]', '');
    trigger.disabled = true;
    try {
        if (!pendingCreatedFeeItemId) {
            const created = await post('/api/fee-management', {
                name: readValue('feeName'),
                description: readValue('feeDescription'),
                referenceYear: dueDate ? Number(dueDate.slice(0, 4)) : 0,
                referenceTermCode: readValue('feeTerm'),
                amount,
                dueDate,
            });
            pendingCreatedFeeItemId = created.feeItemId;
        }
        await post(`/api/fee-management/${pendingCreatedFeeItemId}/open`, {
            selectedMemberIds: [],
        });
        pendingCreatedFeeItemId = null;
        closeActionModal(trigger);
        showToast('회비 항목을 만들고 전체 활성 멤버에게 부과했습니다.');
        await loadItems();
    } catch (error) {
        setError('[data-fee-form-error]', errorMessage(error));
    } finally {
        trigger.disabled = false;
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
        [ACTIONS.ADD]: addFee,
        [ACTIONS.CANCEL_OPEN]: openCancel,
        [ACTIONS.CANCEL]: cancelFee,
    });
    loadItems();
} else {
    lookup('[data-my-fee-retry]').addEventListener('click', loadMyFees);
    loadMyFees();
}
