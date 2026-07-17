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
    const row = buildPropRow(
        name,
        readValue('ppCat'),
        Number(readValue('ppTotal')) || 1,
        0,
        readValue('ppLoc') || '미지정'
    );
    lookup('[data-prop-list]').prepend(row);
    closeActionModal(trigger);
    filterProps();
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
    const total = Math.max(0, Number(readValue('epTotal')) || 0);
    const used = Math.min(total, Math.max(0, Number(readValue('epUse')) || 0));
    editingPropRow.cells[0].textContent = readValue('epName') || editingPropRow.cells[0].textContent;
    editingPropRow.cells[2].textContent = String(total);
    editingPropRow.cells[3].textContent = String(used);
    editingPropRow.cells[4].textContent = readValue('epLoc') || '미지정';
    editingPropRow.cells[5].replaceChildren(statusBadge(readValue('epStatus')));
    editingPropRow = null;
    closeActionModal(trigger);
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
        showToast('품목을 삭제했어요');
    },
    [ACTIONS.ADD_BORROW]: addBorrow,
    [ACTIONS.RETURN_BORROW]: (trigger) => {
        const row = trigger.closest('tr');
        lookup('[data-borrow-status]', row).replaceChildren(badge('반납 완료', 'success'));
        trigger.remove();
        showToast('반납 완료로 기록했어요');
    }
});
