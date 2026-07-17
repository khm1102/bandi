import {openModal} from '../common/modal.js';
import {showToast} from '../common/toast.js';
import {all, appendCell, bindPageActions, debounce, element, lookup, readValue} from '../common/dom.js';
import {currentUserRole} from '../common/session.js';
import {activateFilterChip, badge, closeActionModal, today} from '../common/view.js';

const ACTIONS = Object.freeze({
    ADD: 'prop-add',
    EDIT: 'prop-edit',
    SAVE_EDIT: 'prop-edit-save',
    DELETE: 'prop-delete',
    ADD_BORROW: 'borrow-add',
    RETURN_BORROW: 'borrow-return'
});

let editingPropRow = null;

function readValidInteger(id, message) {
    const input = document.getElementById(id);
    input.setCustomValidity('');
    const value = input.valueAsNumber;
    if (!Number.isInteger(value) || !input.checkValidity()) {
        input.setCustomValidity(message);
        input.reportValidity();
        input.focus();
        showToast(message);
        return null;
    }
    return value;
}

function updatePropSummary() {
    const rows = all('[data-prop-row]');
    const used = rows.reduce((total, row) => total + Number(row.cells[3].textContent), 0);
    const stored = rows.reduce(
        (total, row) => total + Number(row.cells[2].textContent) - Number(row.cells[3].textContent),
        0
    );
    lookup('[data-stat-value="prop-total"]').textContent = String(rows.length);
    lookup('[data-stat-value="prop-used"]').textContent = String(used);
    lookup('[data-stat-value="prop-stored"]').textContent = String(stored);
}

function updateBorrowSummary() {
    const list = lookup('[data-borrow-list]');
    if (!list) {
        return;
    }
    const pending = all('[data-page-action="borrow-return"]', list).length;
    lookup('[data-stat-value="borrow-pending"]').textContent = String(pending);
    lookup('[data-borrow-pending]').textContent = String(pending);
}

function filterProps() {
    const selected = lookup('[data-filter-group="prop"][aria-pressed="true"]');
    const category = selected ? selected.dataset.filterValue : '전체';
    const query = lookup('[data-prop-search]').value.trim().toLowerCase();
    all('[data-prop-row]').forEach((row) => {
        const categoryMatched = category === '전체' || row.dataset.category === category;
        const queryMatched = !query || row.textContent.toLowerCase().includes(query);
        row.hidden = !(categoryMatched && queryMatched);
    });
}

function statusBadge(status) {
    const tone = status === '정상' ? 'success' : status === '사용중' ? 'warning' : 'danger';
    return badge(status, tone);
}

function buildPropRow(name, category, total, used, location) {
    const row = element('tr');
    row.dataset.propRow = '';
    row.dataset.category = category;
    appendCell(row, name, 'font-bold');
    const categoryCell = appendCell(row, '');
    categoryCell.appendChild(badge(category));
    appendCell(row, String(total));
    appendCell(row, String(used), 'font-bold text-warning');
    appendCell(row, location);
    const statusCell = appendCell(row, '');
    statusCell.appendChild(statusBadge('정상'));
    if (currentUserRole !== 'member') {
        const actionCell = appendCell(row, '', 'text-right');
        const actions = lookup('[data-prop-actions-template]').content.cloneNode(true);
        actionCell.appendChild(actions);
    }
    return row;
}

function addProp(trigger) {
    const name = readValue('ppName');
    if (!name) {
        showToast('품목명을 입력해 주세요');
        return;
    }
    const total = readValidInteger('ppTotal', '총수량은 1 이상의 정수로 입력해 주세요');
    if (total === null) {
        return;
    }
    const row = buildPropRow(
        name,
        readValue('ppCat'),
        total,
        0,
        readValue('ppLoc') || '미지정'
    );
    lookup('[data-prop-list]').prepend(row);
    closeActionModal(trigger);
    filterProps();
    updatePropSummary();
    showToast('품목을 등록했어요');
}

function openPropEdit(trigger) {
    editingPropRow = trigger.closest('[data-prop-row]');
    const cells = editingPropRow.cells;
    document.getElementById('epName').value = cells[0].textContent.trim();
    document.getElementById('epTotal').value = cells[2].textContent.trim();
    document.getElementById('epUse').value = cells[3].textContent.trim();
    document.getElementById('epLoc').value = cells[4].textContent.trim();
    document.getElementById('epStatus').value = cells[5].textContent.trim();
    openModal('editPropModal');
}

function savePropEdit(trigger) {
    if (!editingPropRow) {
        return;
    }
    const total = readValidInteger('epTotal', '총수량은 0 이상의 정수로 입력해 주세요');
    const used = readValidInteger('epUse', '사용중 수량은 0 이상의 정수로 입력해 주세요');
    if (total === null || used === null) {
        return;
    }
    if (used > total) {
        const usedInput = document.getElementById('epUse');
        usedInput.setCustomValidity('사용중 수량은 총수량보다 클 수 없습니다.');
        usedInput.reportValidity();
        usedInput.focus();
        showToast('사용중 수량은 총수량보다 클 수 없습니다');
        return;
    }
    editingPropRow.cells[0].textContent = readValue('epName') || editingPropRow.cells[0].textContent;
    editingPropRow.cells[2].textContent = String(total);
    editingPropRow.cells[3].textContent = String(used);
    editingPropRow.cells[4].textContent = readValue('epLoc') || '미지정';
    editingPropRow.cells[5].replaceChildren(statusBadge(readValue('epStatus')));
    editingPropRow = null;
    closeActionModal(trigger);
    updatePropSummary();
    showToast('품목 정보를 수정했어요');
}

function addBorrow(trigger) {
    const itemName = readValue('bwItem');
    if (!itemName) {
        showToast('물품명을 입력해 주세요');
        return;
    }
    const owner = readValue('bwOwner');
    const matched = owner.match(/^(.+) \((.+)\)$/);
    const row = element('tr');
    appendCell(row, itemName, 'font-bold');
    appendCell(row, matched ? matched[1] : owner);
    appendCell(row, matched ? matched[2] : '');
    appendCell(row, today());
    appendCell(row, readValue('bwDue') || '미정');
    const statusCell = appendCell(row, '');
    statusCell.dataset.borrowStatus = '';
    statusCell.appendChild(badge('대기', 'warning'));
    const actionCell = appendCell(row, '', 'text-right');
    const returnAction = lookup('[data-borrow-return-template]').content.cloneNode(true);
    actionCell.appendChild(returnAction);
    lookup('[data-borrow-list]').prepend(row);
    closeActionModal(trigger);
    updateBorrowSummary();
    showToast('빌린 물품을 기록했어요');
}

lookup('[data-prop-search]').addEventListener('input', debounce(filterProps));
document.addEventListener('click', (event) => {
    const filter = event.target.closest('[data-filter-group="prop"]');
    if (!filter) {
        return;
    }
    activateFilterChip(filter);
    filterProps();
});

bindPageActions({
    [ACTIONS.ADD]: addProp,
    [ACTIONS.EDIT]: openPropEdit,
    [ACTIONS.SAVE_EDIT]: savePropEdit,
    [ACTIONS.DELETE]: (trigger) => {
        trigger.closest('[data-prop-row]').remove();
        updatePropSummary();
        showToast('품목을 삭제했어요');
    },
    [ACTIONS.ADD_BORROW]: addBorrow,
    [ACTIONS.RETURN_BORROW]: (trigger) => {
        const row = trigger.closest('tr');
        lookup('[data-borrow-status]', row).replaceChildren(badge('반납 완료', 'success'));
        trigger.remove();
        updateBorrowSummary();
        showToast('반납 완료로 기록했어요');
    }
});

updatePropSummary();
updateBorrowSummary();
