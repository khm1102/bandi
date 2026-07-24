import {get, post} from '../common/api.js';
import {normalizePage, readPageFromUrl, renderPagination, setUrlPage, writeUrl} from '../common/pagination.js';
import {showToast} from '../common/toast.js';

const PAGE_SIZE = 20;
const CATEGORY_LABELS = {
    PROP: '소품',
    COSTUME: '의상',
    LIGHTING: '조명 장비',
    AUDIO: '음향 장비',
    VIDEO: '영상 장비',
    EQUIPMENT: '기타 장비',
};
const STATUS_LABELS = {
    AVAILABLE: '사용 가능',
    IN_USE: '사용 중',
    LOANED: '대여 중',
    REPAIR: '수리 중',
    LOST: '분실',
    DISPOSED: '폐기',
};
const TRACKING_LABELS = {
    QUANTITY: '수량형',
    INDIVIDUAL: '개별 관리',
};

const root = document.querySelector('[data-asset-list-root]');
const region = document.querySelector('[data-asset-region]');

if (root && region) {
    const searchInput = root.querySelector('[data-asset-search]');
    const categorySelect = root.querySelector('[data-asset-category]');
    const trackingTypeSelect = root.querySelector('[data-asset-tracking-type]');
    const statusSelect = root.querySelector('[data-asset-status]');
    const deletedCheckbox = root.querySelector('[data-asset-deleted]');
    const tableList = region.querySelector('[data-asset-table-list]');
    const cardList = region.querySelector('[data-asset-card-list]');
    const summary = region.querySelector('[data-asset-summary]');
    const empty = region.querySelector('[data-asset-empty]');
    const emptyTitle = region.querySelector('[data-asset-empty-title]');
    const emptyDescription = region.querySelector('[data-asset-empty-description]');
    const error = region.querySelector('[data-asset-error]');
    const pagination = region.querySelector('[data-pagination]');
    const canAdmin = root.dataset.canAdmin === 'true';
    let requestGeneration = 0;
    let searchTimerId = null;

    function queryFromUrl() {
        const params = new URLSearchParams(window.location.search);
        return {
            params,
            keyword: params.get('q') || '',
            categoryCode: params.get('category') || '',
            trackingType: params.get('trackingType') || '',
            status: params.get('status') || '',
            deleted: canAdmin && params.get('deleted') === 'true',
            page: readPageFromUrl(params),
        };
    }

    function syncControls(query) {
        searchInput.value = query.keyword;
        categorySelect.value = query.categoryCode;
        trackingTypeSelect.value = query.trackingType;
        statusSelect.value = query.status;
        if (deletedCheckbox) {
            deletedCheckbox.checked = query.deleted;
        }
    }

    function updateFilterUrl() {
        const params = new URLSearchParams();
        if (searchInput.value.trim()) {
            params.set('q', searchInput.value.trim());
        }
        if (categorySelect.value) {
            params.set('category', categorySelect.value);
        }
        if (trackingTypeSelect.value) {
            params.set('trackingType', trackingTypeSelect.value);
        }
        if (statusSelect.value) {
            params.set('status', statusSelect.value);
        }
        if (canAdmin && deletedCheckbox?.checked) {
            params.set('deleted', 'true');
        }
        writeUrl(params, false);
    }

    function createButton(label, href, className = 'border bg-card hover:bg-secondary') {
        const link = document.createElement('a');
        link.href = href;
        link.className = `inline-flex min-h-11 items-center justify-center rounded-md px-3 text-sm font-bold ${className}`;
        link.textContent = label;
        return link;
    }

    function createStatusBadge(status, deleted) {
        const badge = document.createElement('span');
        badge.className = deleted
            ? 'inline-flex rounded-full bg-muted px-2.5 py-1 text-xs font-bold text-muted-foreground'
            : 'inline-flex rounded-full bg-secondary px-2.5 py-1 text-xs font-bold text-foreground';
        badge.textContent = deleted ? '삭제됨' : (STATUS_LABELS[status] || status);
        return badge;
    }

    function createThumbnail(item, sizeClass) {
        const wrapper = document.createElement('div');
        wrapper.className = `${sizeClass} shrink-0 overflow-hidden rounded-md border bg-secondary`;
        if (!item.photoFileId) {
            const fallback = document.createElement('span');
            fallback.className = 'flex h-full w-full items-center justify-center text-lg text-muted-foreground';
            fallback.textContent = '▣';
            wrapper.append(fallback);
            return wrapper;
        }
        const image = document.createElement('img');
        image.src = `/api/assets/${item.assetItemId}/photo/download`;
        image.alt = '';
        image.className = 'h-full w-full object-cover';
        image.addEventListener('error', () => {
            image.remove();
            const fallback = document.createElement('span');
            fallback.className = 'flex h-full w-full items-center justify-center text-lg text-muted-foreground';
            fallback.textContent = '▣';
            wrapper.append(fallback);
        }, {once: true});
        wrapper.append(image);
        return wrapper;
    }

    function createManageActions(item, deleted) {
        const actions = document.createElement('div');
        actions.className = 'flex flex-wrap items-center justify-end gap-2';
        if (deleted) {
            const restore = document.createElement('button');
            restore.type = 'button';
            restore.className = 'inline-flex min-h-11 items-center justify-center rounded-md border bg-card px-3 text-sm font-bold hover:bg-secondary';
            restore.textContent = '복구';
            restore.dataset.assetAction = 'restore';
            restore.dataset.assetItemId = String(item.assetItemId);
            restore.dataset.confirm = `“${item.name}” 품목을 복구할까요? 일반 목록에 다시 표시됩니다.`;
            restore.dataset.confirmAction = '복구';
            actions.append(restore);
            return actions;
        }
        actions.append(createButton('상세', `/props/${item.assetItemId}`));
        if (canAdmin) {
            actions.append(createButton('수정', `/props/${item.assetItemId}/edit`));
        }
        return actions;
    }

    function renderTable(items, deleted) {
        tableList.replaceChildren();
        items.forEach((item) => {
            const row = document.createElement('tr');
            row.className = 'border-b last:border-b-0';
            const photoCell = document.createElement('td');
            photoCell.className = 'px-4 py-3';
            photoCell.append(createThumbnail(item, 'size-14'));
            row.append(photoCell);

            const nameCell = document.createElement('td');
            nameCell.className = 'px-4 py-3';
            const name = document.createElement('a');
            name.href = `/props/${item.assetItemId}`;
            name.className = 'font-bold hover:underline';
            name.textContent = item.name;
            nameCell.append(name);
            row.append(nameCell);

            const categoryCell = document.createElement('td');
            categoryCell.className = 'px-4 py-3 text-sm text-muted-foreground';
            categoryCell.textContent = CATEGORY_LABELS[item.categoryCode] || item.categoryCode;
            row.append(categoryCell);

            const trackingCell = document.createElement('td');
            trackingCell.className = 'px-4 py-3 text-sm';
            trackingCell.textContent = `${TRACKING_LABELS[item.trackingType] || item.trackingType} · ${item.totalQuantity}개`;
            row.append(trackingCell);

            const locationCell = document.createElement('td');
            locationCell.className = 'truncate px-4 py-3 text-sm text-muted-foreground';
            locationCell.title = item.storageLocation;
            locationCell.textContent = item.storageLocation;
            row.append(locationCell);

            const statusCell = document.createElement('td');
            statusCell.className = 'px-4 py-3';
            statusCell.append(createStatusBadge(item.status, deleted));
            row.append(statusCell);

            const actionCell = document.createElement('td');
            actionCell.className = 'px-4 py-3';
            actionCell.append(createManageActions(item, deleted));
            row.append(actionCell);
            tableList.append(row);
        });
    }

    function renderCards(items, deleted) {
        cardList.replaceChildren();
        items.forEach((item) => {
            const card = document.createElement('article');
            card.className = 'rounded-xl border bg-card p-4';
            const heading = document.createElement('div');
            heading.className = 'flex gap-3';
            heading.append(createThumbnail(item, 'size-20'));
            const information = document.createElement('div');
            information.className = 'min-w-0 flex-1';
            const name = document.createElement('a');
            name.href = `/props/${item.assetItemId}`;
            name.className = 'block truncate font-extrabold';
            name.textContent = item.name;
            const metadata = document.createElement('p');
            metadata.className = 'mt-1 text-sm text-muted-foreground';
            metadata.textContent = `${CATEGORY_LABELS[item.categoryCode] || item.categoryCode} · ${item.totalQuantity}개`;
            const location = document.createElement('p');
            location.className = 'mt-1 truncate text-sm text-muted-foreground';
            location.textContent = item.storageLocation;
            information.append(name, metadata, location);
            heading.append(information);
            card.append(heading);
            const footer = document.createElement('div');
            footer.className = 'mt-4 flex items-center justify-between gap-3';
            footer.append(createStatusBadge(item.status, deleted));
            footer.append(createManageActions(item, deleted));
            card.append(footer);
            cardList.append(card);
        });
    }

    function showEmpty(query) {
        emptyTitle.textContent = query.keyword || query.categoryCode || query.trackingType || query.status
            ? '조건에 맞는 품목이 없습니다.' : (query.deleted ? '삭제된 품목이 없습니다.' : '등록된 품목이 없습니다.');
        emptyDescription.textContent = query.keyword || query.categoryCode || query.trackingType || query.status
            ? '검색어나 필터를 바꿔 보세요.' : '품목이 등록되면 이곳에서 사진과 함께 확인할 수 있어요.';
        empty.querySelector('[data-asset-reset-filter]').hidden = !(query.keyword || query.categoryCode || query.trackingType || query.status);
        empty.classList.remove('hidden');
    }

    function clearStates() {
        empty.classList.add('hidden');
        error.classList.add('hidden');
        tableList.replaceChildren();
        cardList.replaceChildren();
        pagination.classList.add('hidden');
    }

    async function load() {
        const query = queryFromUrl();
        syncControls(query);
        clearStates();
        region.setAttribute('aria-busy', 'true');
        const generation = ++requestGeneration;
        try {
            const response = await get('/api/assets', {
                keyword: query.keyword,
                categoryCode: query.categoryCode,
                trackingType: query.trackingType,
                status: query.status,
                deleted: query.deleted,
                page: query.page,
                pageSize: PAGE_SIZE,
            });
            if (generation !== requestGeneration) {
                return;
            }
            const normalizedPage = normalizePage(response, query.page);
            if (normalizedPage !== query.page) {
                setUrlPage(query.params, normalizedPage);
                writeUrl(query.params, false);
                await load();
                return;
            }
            summary.textContent = `총 ${response.totalElements.toLocaleString('ko-KR')}건`;
            if (response.items.length === 0) {
                showEmpty(query);
                return;
            }
            renderTable(response.items, query.deleted);
            renderCards(response.items, query.deleted);
            renderPagination(pagination, response, changePage);
        } catch (requestError) {
            if (generation !== requestGeneration) {
                return;
            }
            error.classList.remove('hidden');
            summary.textContent = '';
        } finally {
            if (generation === requestGeneration) {
                region.setAttribute('aria-busy', 'false');
            }
        }
    }

    function changePage(page) {
        const query = queryFromUrl();
        setUrlPage(query.params, page);
        writeUrl(query.params, true);
        load();
    }

    function changeFilters() {
        updateFilterUrl();
        load();
    }

    searchInput.addEventListener('input', () => {
        clearTimeout(searchTimerId);
        searchTimerId = window.setTimeout(changeFilters, 300);
    });
    [categorySelect, trackingTypeSelect, statusSelect, deletedCheckbox]
        .filter(Boolean)
        .forEach((control) => control.addEventListener('change', changeFilters));
    region.querySelector('[data-asset-reset-filter]').addEventListener('click', () => {
        searchInput.value = '';
        categorySelect.value = '';
        trackingTypeSelect.value = '';
        statusSelect.value = '';
        if (deletedCheckbox) {
            deletedCheckbox.checked = false;
        }
        updateFilterUrl();
        load();
    });
    region.querySelector('[data-asset-retry]').addEventListener('click', load);
    region.addEventListener('click', async (event) => {
        const action = event.target.closest('[data-asset-action]');
        if (!action || action.dataset.assetAction !== 'restore') {
            return;
        }
        try {
            await post(`/api/assets/${action.dataset.assetItemId}/restore`);
            showToast('품목을 복구했어요.');
            load();
        } catch (requestError) {
            showToast(requestError.message);
        }
    });
    window.addEventListener('popstate', load);
    load();
}
