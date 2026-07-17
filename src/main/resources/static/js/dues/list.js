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

function selectFeeTab(tab, announce) {
    all('[data-fee-tab]').forEach((candidate) => {
        const selected = candidate === tab;
        candidate.classList.toggle('border', selected);
        candidate.classList.toggle('bg-card', selected);
        candidate.classList.toggle('text-foreground', selected);
        candidate.classList.toggle('text-muted-foreground', !selected);
        candidate.setAttribute('aria-selected', String(selected));
        candidate.tabIndex = selected ? 0 : -1;
    });
    if (announce) {
        showToast(`${tab.textContent.trim()} 현황을 불러왔어요`);
    }
}

function changeFeeStatus(paid) {
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
    const container = lookup('[data-fee-tab]').parentElement;
    all('[data-fee-tab]').forEach((candidate) => {
        candidate.classList.remove('border', 'bg-card', 'text-foreground');
        candidate.classList.add('text-muted-foreground');
    });
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
}

bindPageActions({
    [ACTIONS.PAY]: () => changeFeeStatus(true),
    [ACTIONS.MARK_UNPAID]: () => changeFeeStatus(false),
    [ACTIONS.ADD]: addFee,
    [ACTIONS.DELETE]: deleteFee
});
