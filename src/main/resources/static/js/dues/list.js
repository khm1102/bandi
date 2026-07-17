import {showToast} from '../common/toast.js';
import {all, bindPageActions, element, lookup, readValue} from '../common/dom.js';
import {currentUserRole} from '../common/session.js';
import {badge, closeActionModal, today} from '../common/view.js';

const ACTIONS = Object.freeze({
    PAY: 'fee-pay',
    MARK_UNPAID: 'fee-unpay',
    ADD: 'fee-add',
    DELETE: 'fee-delete'
});
const feeStates = new WeakMap();

function parseFeeState(tab) {
    const paidDates = new Map();
    tab.dataset.paidRows.split(',').filter(Boolean).forEach((entry) => {
        const [rowIndex, date] = entry.split(':');
        paidDates.set(Number(rowIndex), date);
    });
    return all('[data-fee-row]').map((row, index) => ({
        paid: paidDates.has(index),
        date: paidDates.get(index) || '—'
    }));
}

function collectFeeState() {
    return all('[data-fee-row]').map((row) => ({
        paid: lookup('[data-fee-status]', row).textContent.trim() === '납부 완료',
        date: lookup('[data-fee-date]', row).textContent.trim()
    }));
}

function updateFeeSummary(tab, feeState) {
    const amount = Number(tab?.dataset.amount || 0);
    const paidCount = feeState.filter((payment) => payment.paid).length;
    lookup('[data-stat-value="fee-amount"]').textContent = amount.toLocaleString('ko-KR');
    lookup('[data-stat-value="fee-paid"]').textContent = String(paidCount);
    lookup('[data-stat-value="fee-unpaid"]').textContent = String(feeState.length - paidCount);
    lookup('[data-stat-value="fee-collected"]').textContent = (amount * paidCount)
        .toLocaleString('ko-KR');
}

function renderFeeState(tab) {
    const feeState = feeStates.get(tab) || parseFeeState(tab);
    feeStates.set(tab, feeState);
    all('[data-fee-row]').forEach((row, index) => {
        const payment = feeState[index];
        const tone = payment.paid ? 'success' : 'warning';
        const statusBadge = badge(payment.paid ? '납부 완료' : '미납', tone);
        lookup('[data-fee-status]', row).replaceChildren(statusBadge);
        const dateCell = lookup('[data-fee-date]', row);
        dateCell.textContent = payment.date;
        dateCell.classList.toggle('text-muted-foreground', !payment.paid);
        lookup('[data-fee-person]', row).checked = false;
    });
    lookup('[data-fee-all]').checked = false;
    updateFeeSummary(tab, feeState);
}

function selectFeeTab(tab, announce) {
    const currentTab = lookup('[data-fee-tab][aria-selected="true"]');
    if (currentTab && currentTab !== tab) {
        feeStates.set(currentTab, collectFeeState());
    }
    all('[data-fee-tab]').forEach((candidate) => {
        const selected = candidate === tab;
        candidate.classList.toggle('border', selected);
        candidate.classList.toggle('bg-card', selected);
        candidate.classList.toggle('text-foreground', selected);
        candidate.classList.toggle('text-muted-foreground', !selected);
        candidate.setAttribute('aria-selected', String(selected));
        candidate.tabIndex = selected ? 0 : -1;
    });
    renderFeeState(tab);
    if (announce) {
        showToast(`${tab.textContent.trim()} 현황을 불러왔어요`);
    }
}

function changeFeeStatus(paid) {
    const selectedTab = lookup('[data-fee-tab][aria-selected="true"]');
    if (!selectedTab) {
        showToast('먼저 회비 항목을 추가해 주세요');
        return;
    }
    const selectedRows = all('[data-fee-row]')
        .filter((row) => lookup('[data-fee-person]', row).checked);
    if (selectedRows.length === 0) {
        showToast('부원을 선택해 주세요');
        return;
    }
    selectedRows.forEach((row) => {
        const tone = paid ? 'success' : 'warning';
        lookup('[data-fee-status]', row).replaceChildren(badge(paid ? '납부 완료' : '미납', tone));
        const dateCell = lookup('[data-fee-date]', row);
        dateCell.textContent = paid ? today() : '—';
        dateCell.classList.toggle('text-muted-foreground', !paid);
        lookup('[data-fee-person]', row).checked = false;
    });
    lookup('[data-fee-all]').checked = false;
    const feeState = collectFeeState();
    feeStates.set(selectedTab, feeState);
    updateFeeSummary(selectedTab, feeState);
    showToast(`${selectedRows.length}명을 ${paid ? '납부' : '미납'} 처리했어요`);
}

function addFee(trigger) {
    const name = readValue('feeName');
    if (!name) {
        showToast('항목명을 입력해 주세요');
        return;
    }
    const classes = 'min-h-11 rounded-md border bg-card px-3 text-xs font-bold text-foreground';
    const tab = element('button', classes, name);
    tab.type = 'button';
    tab.role = 'tab';
    tab.setAttribute('aria-selected', 'true');
    tab.dataset.feeTab = '';
    tab.dataset.amount = readValue('feeAmt') || '0';
    tab.dataset.paidRows = '';
    const container = lookup('[role="tablist"]');
    if (!container) {
        showToast('회비 항목 영역을 찾을 수 없어요');
        return;
    }
    container.appendChild(tab);
    selectFeeTab(tab, false);
    closeActionModal(trigger);
    showToast('회비 항목을 추가했어요');
}

function deleteFee() {
    const selectedTab = lookup('[data-fee-tab][aria-selected="true"]');
    if (!selectedTab) {
        showToast('삭제할 회비 항목이 없어요');
        return;
    }
    const nextTab = selectedTab.nextElementSibling || selectedTab.previousElementSibling;
    const name = selectedTab.textContent.trim();
    selectedTab.remove();
    if (nextTab?.matches('[data-fee-tab]')) {
        selectFeeTab(nextTab, false);
        nextTab.focus();
    } else {
        updateFeeSummary(null, []);
    }
    showToast(`${name} 항목을 삭제했어요`);
}

if (currentUserRole === 'admin') {
    const allCheckbox = lookup('[data-fee-all]');
    allCheckbox.addEventListener('change', () => {
        all('[data-fee-person]').forEach((checkbox) => {
            checkbox.checked = allCheckbox.checked;
        });
    });
    const tabList = lookup('[role="tablist"]');
    tabList.addEventListener('click', (event) => {
        const tab = event.target.closest('[data-fee-tab]');
        if (tab) {
            selectFeeTab(tab, true);
        }
    });
    tabList.addEventListener('keydown', (event) => {
        if (!['ArrowLeft', 'ArrowRight'].includes(event.key)) {
            return;
        }
        const tabs = all('[data-fee-tab]', tabList);
        const currentIndex = tabs.indexOf(document.activeElement);
        if (currentIndex < 0) {
            return;
        }
        event.preventDefault();
        const direction = event.key === 'ArrowRight' ? 1 : -1;
        const nextTab = tabs[(currentIndex + direction + tabs.length) % tabs.length];
        selectFeeTab(nextTab, false);
        nextTab.focus();
    });
    const initialTab = lookup('[data-fee-tab][aria-selected="true"]');
    if (initialTab) {
        selectFeeTab(initialTab, false);
    } else {
        updateFeeSummary(null, []);
    }
}

bindPageActions({
    [ACTIONS.PAY]: () => changeFeeStatus(true),
    [ACTIONS.MARK_UNPAID]: () => changeFeeStatus(false),
    [ACTIONS.ADD]: addFee,
    [ACTIONS.DELETE]: deleteFee
});
