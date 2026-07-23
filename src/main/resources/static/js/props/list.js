import {get, patch, post, put} from '../common/api.js';
import {openModal} from '../common/modal.js';
import {showToast} from '../common/toast.js';
import {all, appendCell, bindPageActions, debounce, element, lookup, readValue} from '../common/dom.js';
import {currentUserRole} from '../common/session.js';
import {activateFilterChip, badge, closeActionModal} from '../common/view.js';

const ACTIONS = Object.freeze({
    DETAIL: 'asset-detail',
    EDIT: 'asset-edit',
    SAVE: 'asset-save',
    STATUS_SAVE: 'asset-status-save',
    PHOTO: 'asset-photo',
    UNIT_OPEN: 'asset-unit-open',
    UNIT_EDIT: 'asset-unit-edit',
    UNIT_SAVE: 'asset-unit-save',
});

const CATEGORY_LABELS = Object.freeze({
    PROP: '소품',
    COSTUME: '의상',
    LIGHTING: '조명 장비',
    AUDIO: '음향 장비',
    VIDEO: '영상 장비',
    EQUIPMENT: '기타 장비',
});

const STATUS_LABELS = Object.freeze({
    AVAILABLE: '사용 가능',
    IN_USE: '사용 중',
    LOANED: '외부 대여',
    REPAIR: '수리 중',
    LOST: '분실',
    DISPOSED: '폐기',
});

const ACTION_LABELS = Object.freeze({
    REGISTER: '등록',
    ADJUST: '수량 조정',
    MOVE: '위치 이동',
    LOAN: '사용 등록',
    RETURN: '반납',
    REPAIR: '수리',
    DAMAGE: '손상',
    LOST: '분실',
    DISPOSE: '폐기',
});

const canAdmin = currentUserRole === 'admin';
let items = [];
let members = [];
let editingItem = null;
let selectedItemId = null;

function statusTone(status) {
    if (status === 'AVAILABLE') {
        return 'success';
    }
    if (status === 'IN_USE' || status === 'LOANED') {
        return 'warning';
    }
    return 'danger';
}

function categoryLabel(categoryCode) {
    return CATEGORY_LABELS[categoryCode] || categoryCode;
}

function statusLabel(status) {
    return STATUS_LABELS[status] || status;
}

function memberName(memberId) {
    return members.find((member) => member.memberId === memberId)?.name
            || `부원 #${memberId}`;
}

function formatDateTime(value) {
    if (!value) {
        return '-';
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return value;
    }
    return new Intl.DateTimeFormat('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
    }).format(date);
}

function actionButton(label, action, variant = 'outline') {
    const tones = {
        outline: 'border bg-card hover:bg-secondary',
        primary: 'bg-primary text-primary-foreground hover:bg-primary-strong hover:text-white',
        danger: 'border border-destructive/30 bg-card text-destructive hover:bg-destructive-soft',
    };
    const button = element('button', `inline-flex min-h-11 items-center justify-center rounded-md px-3 text-xs font-bold transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring ${tones[variant]}`, label);
    button.type = 'button';
    button.dataset.pageAction = action;
    return button;
}

function emptyRow(message) {
    const row = element('tr');
    const cell = appendCell(row, message, 'py-11 text-center text-muted-foreground');
    cell.colSpan = 6;
    return row;
}

function buildItemRow(item) {
    const row = element('tr');
    row.dataset.assetRow = '';
    row.dataset.itemId = String(item.assetItemId);
    row.dataset.status = item.status;
    row.dataset.searchText = `${item.name} ${item.categoryCode} ${categoryLabel(item.categoryCode)} ${item.storageLocation}`.toLowerCase();

    const nameCell = appendCell(row, '', 'min-w-48');
    const nameGroup = element('div', 'flex items-center gap-3');
    const photoMark = element('span', 'flex size-9 shrink-0 items-center justify-center rounded-md bg-accent text-xs font-black text-accent-foreground', item.photoFileId ? '사진' : '품목');
    const nameText = element('div', 'min-w-0');
    nameText.appendChild(element('strong', 'block truncate text-sm', item.name));
    nameText.appendChild(element('span', 'mt-0.5 block text-xs text-muted-foreground', item.trackingType === 'INDIVIDUAL' ? '개별 관리' : '수량 관리'));
    nameGroup.append(photoMark, nameText);
    nameCell.appendChild(nameGroup);

    const categoryCell = appendCell(row, '');
    categoryCell.appendChild(badge(categoryLabel(item.categoryCode)));
    appendCell(row, `${item.totalQuantity}개`, 'tabular-nums');
    appendCell(row, item.storageLocation);
    const statusCell = appendCell(row, '');
    statusCell.appendChild(badge(statusLabel(item.status), statusTone(item.status)));
    const actionsCell = appendCell(row, '', 'min-w-52 text-right');
    actionsCell.appendChild(lookup('[data-asset-actions-template]').content.cloneNode(true));
    return row;
}

function renderItems() {
    const list = lookup('[data-asset-list]');
    const selected = lookup('[data-filter-group="asset-status"][aria-pressed="true"]');
    const status = selected?.dataset.filterValue || 'ALL';
    const query = lookup('[data-asset-search]').value.trim().toLowerCase();
    const visibleItems = items.filter((item) => {
        const searchText = `${item.name} ${item.categoryCode} ${categoryLabel(item.categoryCode)} ${item.storageLocation}`.toLowerCase();
        return (status === 'ALL' || item.status === status)
                && (!query || searchText.includes(query));
    });
    list.replaceChildren();
    if (visibleItems.length === 0) {
        list.appendChild(emptyRow(items.length === 0
                ? '등록된 품목이 없습니다.'
                : '검색 조건에 맞는 품목이 없습니다.'));
    } else {
        list.append(...visibleItems.map(buildItemRow));
    }
    lookup('[data-asset-region]').setAttribute('aria-busy', 'false');
}

function updateSummary() {
    const totalQuantity = items.reduce((total, item) => total + item.totalQuantity, 0);
    const attention = items.filter((item) => ['REPAIR', 'LOST', 'DISPOSED'].includes(item.status)).length;
    lookup('[data-stat-value="asset-total"]').textContent = String(items.length);
    lookup('[data-stat-value="asset-quantity"]').textContent = String(totalQuantity);
    lookup('[data-stat-value="asset-attention"]').textContent = String(attention);
}

function populateSelect(select, values, valueKey, labelBuilder) {
    select.replaceChildren();
    values.forEach((value) => {
        const option = element('option', '', labelBuilder(value));
        option.value = String(value[valueKey]);
        select.appendChild(option);
    });
}

async function loadReferences() {
    members = [];
    if (canAdmin) {
        let page = 0;
        let response;
        do {
            response = await get('/api/members', {status: 'ACTIVE', page, pageSize: 100});
            members.push(...response.items);
            page += 1;
        } while (response.hasNext);
    }
    const ownerSelect = document.getElementById('assetOwnerMember');
    if (ownerSelect) {
        populateSelect(ownerSelect, members, 'memberId', (member) => member.name);
    }
}

async function loadItems() {
    lookup('[data-asset-region]').setAttribute('aria-busy', 'true');
    items = await get('/api/assets');
    updateSummary();
    renderItems();
}

function updateOwnerFields() {
    const ownerType = readValue('assetOwnerType');
    const memberField = lookup('[data-owner-member-field]');
    const externalField = lookup('[data-owner-external-field]');
    memberField.hidden = ownerType !== 'MEMBER';
    externalField.hidden = ownerType !== 'EXTERNAL';
    document.getElementById('assetOwnerMember').required = ownerType === 'MEMBER';
    document.getElementById('assetExternalOwner').required = ownerType === 'EXTERNAL';
}

function resetItemForm() {
    const form = lookup('[data-asset-form]');
    form.reset();
    document.getElementById('assetId').value = '';
    document.getElementById('assetQuantity').value = '1';
    document.getElementById('assetTrackingType').disabled = false;
    document.getElementById('assetPhoto').value = '';
    editingItem = null;
    updateOwnerFields();
    document.getElementById('assetModalTitle').textContent = '품목 등록';
}

function openEdit(trigger) {
    const itemId = Number(trigger.closest('[data-asset-row]').dataset.itemId);
    editingItem = items.find((item) => item.assetItemId === itemId);
    document.getElementById('assetId').value = String(itemId);
    document.getElementById('assetName').value = editingItem.name;
    const categorySelect = document.getElementById('assetCategory');
    if (!Array.from(categorySelect.options)
            .some((option) => option.value === editingItem.categoryCode)) {
        const option = element('option', '', editingItem.categoryCode);
        option.value = editingItem.categoryCode;
        categorySelect.appendChild(option);
    }
    categorySelect.value = editingItem.categoryCode;
    document.getElementById('assetTrackingType').value = editingItem.trackingType;
    document.getElementById('assetTrackingType').disabled = true;
    document.getElementById('assetQuantity').value = String(editingItem.totalQuantity);
    document.getElementById('assetLocation').value = editingItem.storageLocation;
    document.getElementById('assetOwnerType').value = editingItem.ownerType;
    document.getElementById('assetOwnerMember').value = editingItem.ownerMemberId || '';
    document.getElementById('assetExternalOwner').value = editingItem.externalOwnerName || '';
    document.getElementById('assetNote').value = editingItem.note || '';
    document.getElementById('assetPhoto').value = '';
    updateOwnerFields();
    document.getElementById('assetModalTitle').textContent = '품목 수정';
    openModal('assetModal');
}

async function uploadPhoto(file) {
    if (!file) {
        return editingItem?.photoFileId || null;
    }
    if (!file.type.startsWith('image/')) {
        throw new Error('품목 사진은 이미지 파일만 선택해 주세요.');
    }
    const formData = new FormData();
    formData.append('file', file);
    const uploaded = await post('/api/files/private', formData, {
        query: {domain: 'asset'},
    });
    return uploaded.id;
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

async function saveItem(trigger) {
    const form = lookup('[data-asset-form]');
    if (!form.reportValidity()) {
        return;
    }
    await withBusy(trigger, async () => {
        const photoFileId = await uploadPhoto(document.getElementById('assetPhoto').files[0]);
        const ownerType = readValue('assetOwnerType');
        const common = {
            name: readValue('assetName'),
            categoryCode: readValue('assetCategory'),
            ownerType,
            ownerMemberId: ownerType === 'MEMBER' ? Number(readValue('assetOwnerMember')) : null,
            externalOwnerName: ownerType === 'EXTERNAL' ? readValue('assetExternalOwner') : null,
            totalQuantity: Number(readValue('assetQuantity')),
            storageLocation: readValue('assetLocation'),
            photoFileId,
            note: readValue('assetNote') || null,
        };
        if (editingItem) {
            await put(`/api/assets/${editingItem.assetItemId}`, common);
        } else {
            await post('/api/assets', {
                ...common,
                trackingType: readValue('assetTrackingType'),
            });
        }
        closeActionModal(trigger);
        await loadItems();
        showToast(editingItem ? '품목 정보를 수정했습니다.' : '품목을 등록했습니다.');
        resetItemForm();
    });
}

function detailField(label, value) {
    const group = element('div');
    group.appendChild(element('dt', 'text-xs font-extrabold text-muted-foreground', label));
    group.appendChild(element('dd', 'mt-1 text-sm font-bold', value || '-'));
    return group;
}

function sectionHeading(title) {
    return element('h3', 'text-sm font-extrabold', title);
}

function buildHistoryList(histories) {
    const section = element('section', 'flex flex-col gap-3');
    section.appendChild(sectionHeading('변경 이력'));
    if (histories.length === 0) {
        section.appendChild(element('p', 'rounded-lg bg-secondary px-4 py-3 text-sm text-muted-foreground', '변경 이력이 없습니다.'));
        return section;
    }
    const list = element('ol', 'divide-y rounded-lg border');
    histories.forEach((history) => {
        const row = element('li', 'flex items-start gap-3 px-4 py-3');
        row.appendChild(badge(ACTION_LABELS[history.action] || history.action));
        const body = element('div', 'min-w-0 flex-1');
        body.appendChild(element('strong', 'block text-sm', history.note || `${history.quantity}개 변경`));
        body.appendChild(element('span', 'mt-1 block text-xs text-muted-foreground', `${memberName(history.changedByMemberId)} · ${formatDateTime(history.changedDttm)}`));
        row.appendChild(body);
        list.appendChild(row);
    });
    section.appendChild(list);
    return section;
}

function buildUnitList(item, units) {
    const section = element('section', 'flex flex-col gap-3');
    const heading = element('div', 'flex items-center gap-3');
    heading.appendChild(sectionHeading('개별 장비'));
    if (canAdmin) {
        const addButton = actionButton('장비 등록', ACTIONS.UNIT_OPEN);
        addButton.dataset.itemId = String(item.assetItemId);
        addButton.classList.add('ml-auto');
        heading.appendChild(addButton);
    }
    section.appendChild(heading);
    if (units.length === 0) {
        section.appendChild(element('p', 'rounded-lg bg-secondary px-4 py-3 text-sm text-muted-foreground', '등록된 개별 장비가 없습니다.'));
        return section;
    }
    const list = element('ul', 'divide-y rounded-lg border');
    units.forEach((unit) => {
        const row = element('li', 'flex flex-wrap items-center gap-3 px-4 py-3');
        const body = element('div', 'min-w-0 flex-1');
        body.appendChild(element('strong', 'block text-sm', unit.managementNo));
        body.appendChild(element('span', 'mt-1 block text-xs text-muted-foreground', unit.storageLocation));
        row.append(body, badge(statusLabel(unit.status), statusTone(unit.status)));
        if (canAdmin && unit.status !== 'IN_USE') {
            const editButton = actionButton('수정', ACTIONS.UNIT_EDIT);
            editButton.dataset.unitId = String(unit.assetUnitId);
            editButton.dataset.itemId = String(item.assetItemId);
            row.appendChild(editButton);
        } else if (canAdmin) {
            row.appendChild(element('span', 'text-xs font-bold text-muted-foreground', '반납 후 수정'));
        }
        list.appendChild(row);
    });
    section.appendChild(list);
    return section;
}

function buildAdminStatus(item) {
    const section = element('section', 'rounded-lg border bg-secondary p-4');
    section.appendChild(sectionHeading('품목 상태 변경'));
    const controls = element('div', 'mt-3 flex flex-col gap-2 md:flex-row');
    const select = element('select', 'h-11 flex-1 rounded-md border border-input bg-card px-3 text-base md:text-sm');
    select.dataset.assetStatusSelect = '';
    Object.entries(STATUS_LABELS).forEach(([value, label]) => {
        const option = element('option', '', label);
        option.value = value;
        option.selected = item.status === value;
        select.appendChild(option);
    });
    const note = element('input', 'h-11 flex-[2] rounded-md border border-input bg-card px-3 text-base md:text-sm');
    note.dataset.assetStatusNote = '';
    note.placeholder = '변경 사유';
    const button = actionButton('상태 저장', ACTIONS.STATUS_SAVE, 'primary');
    button.dataset.itemId = String(item.assetItemId);
    button.dataset.currentStatus = item.status;
    controls.append(select, note, button);
    section.appendChild(controls);
    return section;
}

async function renderDetail(itemId) {
    const detail = lookup('[data-asset-detail]');
    detail.replaceChildren(element('p', 'py-8 text-center text-sm text-muted-foreground', '상세 정보를 불러오는 중입니다.'));
    const item = items.find((candidate) => candidate.assetItemId === itemId);
    const requests = [
        get(`/api/assets/${itemId}/histories`),
        item.trackingType === 'INDIVIDUAL'
                ? get(`/api/assets/${itemId}/units`)
                : Promise.resolve([]),
    ];
    const [histories, units] = await Promise.all(requests);
    detail.replaceChildren();
    const header = element('div', 'flex flex-wrap items-start gap-3');
    const title = element('div', 'min-w-0 flex-1');
    title.appendChild(element('strong', 'block text-lg font-extrabold', item.name));
    title.appendChild(element('span', 'mt-1 block text-xs text-muted-foreground', categoryLabel(item.categoryCode)));
    header.append(title, badge(statusLabel(item.status), statusTone(item.status)));
    if (item.photoFileId) {
        const photoButton = actionButton('사진 보기', ACTIONS.PHOTO);
        photoButton.dataset.itemId = String(itemId);
        header.appendChild(photoButton);
    }
    detail.appendChild(header);
    const fields = element('dl', 'grid grid-cols-2 gap-4 rounded-lg bg-secondary p-4');
    fields.append(
        detailField('총수량', `${item.totalQuantity}개`),
        detailField('관리 방식', item.trackingType === 'INDIVIDUAL' ? '개별 관리' : '수량 관리'),
        detailField('보관 위치', item.storageLocation),
        detailField('소유 구분', item.ownerType === 'CLUB' ? '동아리' : item.ownerType === 'MEMBER' ? memberName(item.ownerMemberId) : item.externalOwnerName),
    );
    detail.appendChild(fields);
    if (item.note) {
        detail.appendChild(element('p', 'rounded-lg border px-4 py-3 text-sm leading-relaxed', item.note));
    }
    if (canAdmin) {
        detail.appendChild(buildAdminStatus(item));
    }
    if (item.trackingType === 'INDIVIDUAL') {
        detail.appendChild(buildUnitList(item, units));
    }
    detail.appendChild(buildHistoryList(histories));
}

async function openDetail(trigger) {
    selectedItemId = Number(trigger.closest('[data-asset-row]')?.dataset.itemId
            || trigger.dataset.itemId);
    openModal('assetDetailModal');
    try {
        await renderDetail(selectedItemId);
    } catch (error) {
        lookup('[data-asset-detail]').replaceChildren(element('p', 'rounded-lg bg-destructive-soft px-4 py-3 text-sm text-destructive', error.message || '상세 정보를 불러오지 못했습니다.'));
    }
}

async function changeStatus(trigger) {
    const detail = lookup('[data-asset-detail]');
    const status = lookup('[data-asset-status-select]', detail).value;
    if (status === trigger.dataset.currentStatus) {
        showToast('변경할 상태를 선택해 주세요.');
        return;
    }
    await withBusy(trigger, async () => {
        await patch(`/api/assets/${trigger.dataset.itemId}/status`, {
            status,
            note: lookup('[data-asset-status-note]', detail).value.trim() || null,
        });
        await loadItems();
        await renderDetail(Number(trigger.dataset.itemId));
        showToast('품목 상태를 변경했습니다.');
    });
}

function openUnit(trigger, editing = false) {
    const itemId = Number(trigger.dataset.itemId);
    document.getElementById('unitItemId').value = String(itemId);
    document.getElementById('unitId').value = editing ? trigger.dataset.unitId : '';
    document.getElementById('unitManagementNo').disabled = editing;
    lookup('[data-unit-status-field]').hidden = !editing;
    lookup('[data-unit-note-field]').hidden = !editing;
    document.getElementById('unitNote').value = '';
    if (editing) {
        get(`/api/assets/${itemId}/units`).then((units) => {
            const unit = units.find((candidate) => String(candidate.assetUnitId) === trigger.dataset.unitId);
            document.getElementById('unitManagementNo').value = unit.managementNo;
            document.getElementById('unitLocation').value = unit.storageLocation;
            document.getElementById('unitStatus').value = unit.status;
        }).catch((error) => showToast(error.message));
    } else {
        lookup('[data-unit-form]').reset();
        document.getElementById('unitItemId').value = String(itemId);
    }
    document.getElementById('assetUnitModalTitle').textContent = editing ? '개별 장비 수정' : '개별 장비 등록';
    openModal('assetUnitModal');
}

async function saveUnit(trigger) {
    const form = lookup('[data-unit-form]');
    if (!form.reportValidity()) {
        return;
    }
    const itemId = Number(readValue('unitItemId'));
    const unitId = readValue('unitId');
    await withBusy(trigger, async () => {
        if (unitId) {
            await put(`/api/assets/units/${unitId}`, {
                status: readValue('unitStatus'),
                storageLocation: readValue('unitLocation'),
                note: readValue('unitNote') || null,
            });
        } else {
            await post(`/api/assets/${itemId}/units`, {
                managementNo: readValue('unitManagementNo'),
                storageLocation: readValue('unitLocation'),
            });
        }
        closeActionModal(trigger);
        await loadItems();
        await renderDetail(itemId);
        showToast(unitId ? '개별 장비를 수정했습니다.' : '개별 장비를 등록했습니다.');
    });
}

function openPhoto(trigger) {
    window.open(`/api/assets/${trigger.dataset.itemId}/photo/download`, '_blank', 'noopener');
}

lookup('[data-asset-search]').addEventListener('input', debounce(renderItems));
document.addEventListener('click', (event) => {
    const filter = event.target.closest('[data-filter-group="asset-status"]');
    if (filter) {
        activateFilterChip(filter);
        renderItems();
    }
});
document.getElementById('assetOwnerType')?.addEventListener('change', updateOwnerFields);
document.querySelector('[data-open-modal="assetModal"]')?.addEventListener('click', resetItemForm);

bindPageActions({
    [ACTIONS.DETAIL]: openDetail,
    [ACTIONS.EDIT]: openEdit,
    [ACTIONS.SAVE]: saveItem,
    [ACTIONS.STATUS_SAVE]: changeStatus,
    [ACTIONS.PHOTO]: openPhoto,
    [ACTIONS.UNIT_OPEN]: (trigger) => openUnit(trigger, false),
    [ACTIONS.UNIT_EDIT]: (trigger) => openUnit(trigger, true),
    [ACTIONS.UNIT_SAVE]: saveUnit,
});

Promise.all([loadReferences(), loadItems()]).catch((error) => {
    lookup('[data-asset-region]').setAttribute('aria-busy', 'false');
    lookup('[data-asset-list]').replaceChildren(emptyRow('품목을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.'));
    showToast(error.message || '소품·장비 정보를 불러오지 못했습니다.');
});
