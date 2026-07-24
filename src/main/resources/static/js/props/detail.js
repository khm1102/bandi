import {del, get, post, put} from '../common/api.js';
import {closeModal, openModal} from '../common/modal.js';
import {showToast} from '../common/toast.js';

const CATEGORY_LABELS = {PROP: '소품', COSTUME: '의상', LIGHTING: '조명 장비', AUDIO: '음향 장비', VIDEO: '영상 장비', EQUIPMENT: '기타 장비'};
const STATUS_LABELS = {AVAILABLE: '사용 가능', IN_USE: '사용 중', LOANED: '대여 중', REPAIR: '수리 중', LOST: '분실', DISPOSED: '폐기'};
const TRACKING_LABELS = {QUANTITY: '수량형', INDIVIDUAL: '개별 관리'};
const OWNER_LABELS = {CLUB: '동아리 소유', MEMBER: '부원 소유', EXTERNAL: '외부 소유'};
const HISTORY_ACTION_LABELS = {REGISTER: '등록', ADJUST: '수량 조정', MOVE: '위치 변경', LOAN: '대여', RETURN: '반납', REPAIR: '수리', DAMAGE: '파손', LOST: '분실', DISPOSE: '폐기', DELETE: '삭제', RESTORE: '복구'};

const root = document.querySelector('[data-asset-detail-root]');

if (root) {
    const assetId = Number(window.location.pathname.match(/^\/props\/(\d+)$/)?.[1]);
    const canAdmin = root.dataset.canAdmin === 'true';
    const loaded = root.querySelector('[data-asset-detail-loaded]');
    const loading = root.querySelector('[data-asset-detail-loading]');
    const error = root.querySelector('[data-asset-detail-error]');
    const errorMessage = root.querySelector('[data-asset-detail-error-message]');
    const photoArea = root.querySelector('[data-asset-photo-area]');
    const badgeArea = root.querySelector('[data-asset-badges]');
    const name = root.querySelector('[data-asset-name]');
    const category = root.querySelector('[data-asset-category]');
    const facts = root.querySelector('[data-asset-facts]');
    const unitsSection = root.querySelector('[data-asset-units-section]');
    const unitList = root.querySelector('[data-asset-unit-list]');
    const unitCardList = root.querySelector('[data-asset-unit-card-list]');
    const unitEmpty = root.querySelector('[data-asset-unit-empty]');
    const historyList = root.querySelector('[data-asset-history-list]');
    const historyEmpty = root.querySelector('[data-asset-history-empty]');
    const adminActions = root.querySelector('[data-asset-detail-admin-actions]');
    const editLink = root.querySelector('[data-asset-edit-link]');
    const deleteButton = root.querySelector('[data-asset-delete]');
    const unitCreateButton = root.querySelector('[data-asset-unit-create]');
    const createModal = document.getElementById('assetUnitCreateModal');
    const editModal = document.getElementById('assetUnitEditModal');
    let currentAsset = null;
    let selectedUnit = null;

    function createText(tag, className, value) {
        const element = document.createElement(tag);
        element.className = className;
        element.textContent = value;
        return element;
    }

    function statusBadge(status) {
        return createText('span', 'inline-flex rounded-full bg-secondary px-2.5 py-1 text-xs font-bold', STATUS_LABELS[status] || status);
    }

    function showError(message) {
        loaded.classList.add('hidden');
        loading.classList.add('hidden');
        errorMessage.textContent = message;
        error.classList.remove('hidden');
        root.setAttribute('aria-busy', 'false');
    }

    function formatDateTime(value) {
        if (!value) {
            return '기록 없음';
        }
        const date = new Date(value);
        return Number.isNaN(date.getTime()) ? value.replace('T', ' ') : date.toLocaleString('ko-KR', {dateStyle: 'medium', timeStyle: 'short'});
    }

    function renderPhoto(asset) {
        photoArea.replaceChildren();
        if (!asset.photoFileId) {
            photoArea.append(createText('span', 'text-sm font-bold text-muted-foreground', '등록된 사진이 없습니다.'));
            return;
        }
        const image = document.createElement('img');
        image.src = `/api/assets/${asset.assetItemId}/photo/download`;
        image.alt = `${asset.name} 사진`;
        image.className = 'max-h-96 w-full rounded-lg object-contain';
        image.addEventListener('error', () => {
            photoArea.replaceChildren(createText('span', 'text-sm font-bold text-muted-foreground', '사진을 표시하지 못했습니다.'));
        }, {once: true});
        photoArea.append(image);
    }

    function appendFact(label, value) {
        const wrapper = document.createElement('div');
        wrapper.append(createText('dt', 'text-xs font-bold text-muted-foreground', label));
        wrapper.append(createText('dd', 'mt-1 text-sm font-bold', value || '-'));
        facts.append(wrapper);
    }

    function renderAsset(asset) {
        currentAsset = asset;
        document.title = `${asset.name} · 소품·장비`;
        renderPhoto(asset);
        badgeArea.replaceChildren(statusBadge(asset.status), createText('span', 'inline-flex rounded-full bg-secondary px-2.5 py-1 text-xs font-bold text-muted-foreground', TRACKING_LABELS[asset.trackingType] || asset.trackingType));
        name.textContent = asset.name;
        category.textContent = CATEGORY_LABELS[asset.categoryCode] || asset.categoryCode;
        facts.replaceChildren();
        appendFact('보관 위치', asset.storageLocation);
        appendFact('수량', `${asset.totalQuantity}개`);
        appendFact('소유 구분', OWNER_LABELS[asset.ownerType] || asset.ownerType);
        appendFact('소유자', asset.externalOwnerName || (asset.ownerMemberId ? '등록 멤버' : '-'));
        appendFact('비고', asset.note || '-');
        if (canAdmin) {
            editLink.href = `/props/${asset.assetItemId}/edit`;
            deleteButton.dataset.confirm = `“${asset.name}” 품목을 삭제할까요? 일반 목록에서 숨겨지고 변경 이력은 보존됩니다.`;
            adminActions.classList.remove('hidden');
        }
        const individual = asset.trackingType === 'INDIVIDUAL';
        unitsSection.classList.toggle('hidden', !individual);
        if (individual && canAdmin) {
            unitCreateButton.classList.remove('hidden');
        }
    }

    function renderUnits(units) {
        unitList.replaceChildren();
        unitCardList.replaceChildren();
        unitEmpty.classList.toggle('hidden', units.length > 0);
        units.forEach((unit) => {
            const row = document.createElement('tr');
            row.className = 'border-b last:border-b-0';
            row.append(createCell(unit.managementNo, 'px-3 py-3 text-sm font-bold'));
            const statusCell = document.createElement('td');
            statusCell.className = 'px-3 py-3';
            statusCell.append(statusBadge(unit.status));
            row.append(statusCell);
            row.append(createCell(unit.storageLocation, 'px-3 py-3 text-sm text-muted-foreground'));
            const actions = document.createElement('td');
            actions.className = 'px-3 py-3 text-right';
            if (canAdmin) {
                const edit = document.createElement('button');
                edit.type = 'button';
                edit.className = 'min-h-11 rounded-md border bg-card px-3 text-sm font-bold hover:bg-secondary';
                edit.textContent = '수정';
                edit.dataset.assetUnitEdit = String(unit.assetUnitId);
                actions.append(edit);
            }
            row.append(actions);
            unitList.append(row);

            const card = document.createElement('article');
            card.className = 'rounded-lg border bg-card p-4';
            const heading = createText('h3', 'text-sm font-extrabold', unit.managementNo);
            const detail = createText('p', 'mt-1 text-sm text-muted-foreground', unit.storageLocation || '보관 위치 없음');
            const footer = document.createElement('div');
            footer.className = 'mt-4 flex items-center justify-between gap-3';
            footer.append(statusBadge(unit.status));
            if (canAdmin) {
                const edit = document.createElement('button');
                edit.type = 'button';
                edit.className = 'min-h-11 rounded-md border bg-card px-3 text-sm font-bold hover:bg-secondary';
                edit.textContent = '수정';
                edit.dataset.assetUnitEdit = String(unit.assetUnitId);
                footer.append(edit);
            }
            card.append(heading, detail, footer);
            unitCardList.append(card);
        });
    }

    function createCell(value, className) {
        return createText('td', className, value || '-');
    }

    function renderHistories(histories) {
        historyList.replaceChildren();
        historyEmpty.classList.toggle('hidden', histories.length > 0);
        histories.forEach((history) => {
            const item = document.createElement('li');
            item.className = 'flex flex-wrap items-baseline justify-between gap-x-4 gap-y-1 border-b pb-3 last:border-b-0 last:pb-0';
            const label = createText('strong', 'text-sm', HISTORY_ACTION_LABELS[history.action] || history.action);
            const description = createText('span', 'ml-2 text-sm text-muted-foreground', history.note || '변경 내용이 기록되었습니다.');
            const left = document.createElement('div');
            left.append(label, description);
            item.append(left, createText('time', 'text-xs text-muted-foreground', formatDateTime(history.changedDttm)));
            historyList.append(item);
        });
    }

    async function load() {
        if (!assetId) {
            showError('올바르지 않은 품목 주소입니다.');
            return;
        }
        root.setAttribute('aria-busy', 'true');
        error.classList.add('hidden');
        loading.classList.remove('hidden');
        try {
            const asset = await get(`/api/assets/${assetId}`);
            renderAsset(asset);
            const requests = [get(`/api/assets/${assetId}/histories`)];
            if (asset.trackingType === 'INDIVIDUAL') {
                requests.push(get(`/api/assets/${assetId}/units`));
            }
            const results = await Promise.all(requests);
            renderHistories(results[0] || []);
            if (asset.trackingType === 'INDIVIDUAL') {
                renderUnits(results[1] || []);
            }
            loading.classList.add('hidden');
            loaded.classList.remove('hidden');
            root.setAttribute('aria-busy', 'false');
        } catch (requestError) {
            showError(requestError.message || '잠시 후 다시 시도해 주세요.');
        }
    }

    function setModalError(modal, selector, message = '') {
        const box = modal?.querySelector(selector);
        if (!box) {
            return;
        }
        box.textContent = message;
        box.classList.toggle('hidden', !message);
    }

    async function createUnit() {
        const managementNo = createModal.querySelector('[data-asset-unit-management-no]');
        const storageLocation = createModal.querySelector('[data-asset-unit-storage-location]');
        setModalError(createModal, '[data-asset-unit-create-error]');
        try {
            await post(`/api/assets/${assetId}/units`, {managementNo: managementNo.value.trim(), storageLocation: storageLocation.value.trim()});
            closeModal(createModal);
            showToast('개별 장비를 추가했어요.');
            await load();
        } catch (requestError) {
            setModalError(createModal, '[data-asset-unit-create-error]', requestError.message || '개별 장비를 추가하지 못했어요.');
        }
    }

    function openUnitEdit(unit) {
        selectedUnit = unit;
        editModal.querySelector('[data-asset-unit-status]').value = unit.status;
        editModal.querySelector('[data-asset-unit-edit-storage-location]').value = unit.storageLocation;
        editModal.querySelector('[data-asset-unit-note]').value = '';
        setModalError(editModal, '[data-asset-unit-edit-error]');
        openModal('assetUnitEditModal', document.activeElement);
    }

    async function updateUnit() {
        if (!selectedUnit) {
            return;
        }
        const status = editModal.querySelector('[data-asset-unit-status]').value;
        const storageLocation = editModal.querySelector('[data-asset-unit-edit-storage-location]').value.trim();
        const note = editModal.querySelector('[data-asset-unit-note]').value.trim();
        setModalError(editModal, '[data-asset-unit-edit-error]');
        try {
            await put(`/api/assets/units/${selectedUnit.assetUnitId}`, {status, storageLocation, note: note || null});
            closeModal(editModal);
            showToast('개별 장비 정보를 수정했어요.');
            await load();
        } catch (requestError) {
            setModalError(editModal, '[data-asset-unit-edit-error]', requestError.message || '개별 장비를 수정하지 못했어요.');
        }
    }

    root.addEventListener('click', async (event) => {
        if (event.target.closest('[data-asset-detail-retry]')) {
            await load();
            return;
        }
        if (event.target.closest('[data-asset-delete]')) {
            try {
                await del(`/api/assets/${assetId}`);
                window.location.assign('/props?deleted=true');
            } catch (requestError) {
                showToast(requestError.message || '품목을 삭제하지 못했어요.', 'error');
            }
            return;
        }
        const unitEdit = event.target.closest('[data-asset-unit-edit]');
        if (unitEdit) {
            const units = await get(`/api/assets/${assetId}/units`);
            const unit = units.find((item) => item.assetUnitId === Number(unitEdit.dataset.assetUnitEdit));
            if (unit) {
                openUnitEdit(unit);
            }
        }
    });

    document.addEventListener('click', (event) => {
        if (event.target.closest('[data-page-action="save-asset-unit"]')) {
            createUnit();
        }
        if (event.target.closest('[data-page-action="update-asset-unit"]')) {
            updateUnit();
        }
    });

    load();
}
