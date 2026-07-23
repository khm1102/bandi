import {get, post, put} from '../common/api.js';
import {bindPageActions, debounce, element, lookup} from '../common/dom.js';
import {renderPagination, readPageFromUrl, setUrlPage, writeUrl, normalizePage} from '../common/pagination.js';
import {activateFilterChip} from '../common/view.js';
import {openModal, closeModal} from '../common/modal.js';
import {showToast} from '../common/toast.js';

const PAGE_SIZE = 20;
const labels = {SCRIPT: '대본', MINUTES: '회의록', PROMOTION: '홍보물', VIDEO: '영상', OTHER: '기타'};
const list = lookup('[data-resource-list]');
const state = lookup('[data-resource-state]');
const pagination = lookup('[data-pagination]');
const searchInput = lookup('[data-resource-search]');
let editingId = null;
let loginMember = null;
let requestGeneration = 0;

function error(message = '') {
    const target = lookup('[data-resource-error]');
    target.textContent = message;
    target.classList.toggle('hidden', !message);
}

function manage(resource) {
    return document.body.dataset.userRole === 'admin'
        || (document.body.dataset.userRole === 'leader' && resource.teamId === loginMember?.teamId);
}

function readUrlState() {
    const params = new URLSearchParams(window.location.search);
    return {
        params,
        query: params.get('q') || '',
        category: params.get('category') || 'ALL',
        scope: params.get('scope') || 'ANY',
        page: readPageFromUrl(params),
    };
}

function activateChip(group, value) {
    const chip = document.querySelector(`[data-filter-group="${group}"][data-filter-value="${value}"]`);
    if (chip) {
        activateFilterChip(chip);
    }
}

function syncControls(urlState) {
    searchInput.value = urlState.query;
    activateChip('resource-category', urlState.category);
    activateChip('resource-scope', urlState.scope);
}

function setState(title, message, options = {}) {
    lookup('[data-resource-state-title]', state).textContent = title;
    lookup('[data-resource-state-message]', state).textContent = message;
    lookup('[data-resource-reset]', state).classList.toggle('hidden', !options.reset);
    lookup('[data-resource-retry]', state).classList.toggle('hidden', !options.retry);
    state.hidden = false;
}

function appendResource(item) {
    const row = lookup('[data-resource-row-template]').content.firstElementChild.cloneNode(true);
    row.dataset.resourceId = item.resourceId;
    lookup('[data-resource-title]', row).textContent = item.title;
    const scope = item.targetScope === 'TEAM' ? item.teamName || '팀 자료' : '전체 공용';
    lookup('[data-resource-meta]', row).textContent = `${scope} · ${item.updatedByName || '수정자 정보 없음'}`;
    lookup('[data-resource-category]', row).textContent = labels[item.categoryCode] || item.categoryCode;
    lookup('[data-resource-version]', row).textContent = item.currentRevisionNo ? `v${item.currentRevisionNo}` : '—';
    lookup('[data-resource-date]', row).textContent = item.updatedDttm
        ? new Date(item.updatedDttm).toLocaleDateString('ko-KR') : '—';
    const actions = lookup('[data-resource-actions]', row);
    const download = element('button', 'min-h-11 rounded-md border px-3 text-xs font-bold', '다운로드');
    download.type = 'button';
    download.dataset.pageAction = 'resource-download';
    actions.appendChild(download);
    if (manage(item)) {
        [['수정', 'resource-edit'], ['이력', 'resource-history']].forEach(([label, action]) => {
            const button = element('button', 'min-h-11 rounded-md border px-3 text-xs font-bold', label);
            button.type = 'button';
            button.dataset.pageAction = action;
            actions.appendChild(button);
        });
        const archive = element('button', 'min-h-11 rounded-md border border-destructive/30 px-3 text-xs font-bold text-destructive', '보관');
        archive.type = 'button';
        archive.dataset.pageAction = 'resource-archive';
        archive.dataset.confirm = '이 자료를 보관할까요? 보관 후 일반 목록에서 숨겨집니다.';
        archive.dataset.confirmAction = '자료 보관';
        actions.appendChild(archive);
    }
    list.appendChild(row);
}

function clearRows() {
    list.querySelectorAll('tr:not([data-resource-state])').forEach((row) => row.remove());
}

function hasFilter(urlState) {
    return Boolean(urlState.query || urlState.category !== 'ALL' || urlState.scope !== 'ANY');
}

function focusList() {
    const first = list.querySelector('[data-resource-row] button, [data-resource-row] a');
    if (first) {
        first.focus({preventScroll: true});
    }
    list.scrollIntoView({behavior: 'smooth', block: 'start'});
}

async function load(options = {}) {
    const urlState = readUrlState();
    syncControls(urlState);
    const generation = ++requestGeneration;
    setState('자료를 불러오는 중입니다', '잠시만 기다려 주세요.');
    try {
        const response = await get('/api/resources', {
            keyword: urlState.query,
            categoryCode: urlState.category === 'ALL' ? null : urlState.category,
            targetScope: urlState.scope === 'ANY' ? null : urlState.scope,
            page: urlState.page,
            pageSize: PAGE_SIZE,
        });
        if (generation !== requestGeneration) {
            return;
        }
        const normalized = normalizePage(response, urlState.page);
        if (normalized !== urlState.page) {
            setUrlPage(urlState.params, normalized);
            writeUrl(urlState.params, false);
            await load(options);
            return;
        }
        clearRows();
        response.items.forEach(appendResource);
        state.hidden = response.items.length > 0;
        if (response.totalElements > 0) {
            renderPagination(pagination, response, changePage);
        } else {
            pagination.classList.add('hidden');
            const filtered = hasFilter(urlState);
            setState(filtered ? '조건에 맞는 자료가 없습니다' : '아직 등록된 자료가 없습니다',
                filtered ? '검색어나 필터를 초기화해 보세요.' : '자료가 등록되면 여기에 표시됩니다.',
                {reset: filtered});
        }
        if (response.items.length > 0 && options.focus) {
            focusList();
        }
    } catch (exception) {
        if (generation === requestGeneration) {
            clearRows();
            setState('자료를 불러오지 못했습니다', exception.message || '잠시 후 다시 시도해 주세요.', {retry: true});
        }
    }
}

function replaceFilters(changes) {
    const urlState = readUrlState();
    Object.entries(changes).forEach(([key, value]) => {
        if (!value || value === 'ALL' || value === 'ANY') {
            urlState.params.delete(key);
        } else {
            urlState.params.set(key, value);
        }
    });
    setUrlPage(urlState.params, 0);
    writeUrl(urlState.params, false);
    load();
}

function changePage(page) {
    const urlState = readUrlState();
    setUrlPage(urlState.params, page);
    writeUrl(urlState.params, true);
    load({focus: true});
}

function updateTeamField() {
    const admin = document.body.dataset.userRole === 'admin';
    const isTeam = document.getElementById('resourceTarget').value === 'TEAM';
    lookup('[data-resource-team-wrap]').classList.toggle('hidden', !admin || !isTeam);
}

function selectedTeamId() {
    return document.body.dataset.userRole === 'admin'
        ? Number(document.getElementById('resourceTeam').value) || null : loginMember?.teamId;
}

function openCreate(trigger) {
    editingId = null;
    document.getElementById('resourceFormModalTitle').textContent = '자료 업로드';
    ['resourceTitle', 'resourceDescription', 'resourceFile'].forEach((id) => {
        document.getElementById(id).value = '';
    });
    document.getElementById('resourcePinned').checked = false;
    updateTeamField();
    error();
    openModal('resourceFormModal', trigger);
}

async function openEdit(trigger) {
    const id = trigger.closest('[data-resource-row]').dataset.resourceId;
    const detail = await get(`/api/resource-management/${id}`);
    editingId = id;
    document.getElementById('resourceFormModalTitle').textContent = '자료 수정';
    document.getElementById('resourceTitle').value = detail.title;
    document.getElementById('resourceCategory').value = detail.categoryCode;
    document.getElementById('resourceTarget').value = detail.targetScope;
    if (detail.teamId) {
        document.getElementById('resourceTeam').value = String(detail.teamId);
    }
    document.getElementById('resourceDescription').value = detail.description;
    document.getElementById('resourcePinned').checked = detail.pinned;
    document.getElementById('resourceFile').value = '';
    updateTeamField();
    error();
    openModal('resourceFormModal', trigger);
}

async function save(trigger) {
    trigger.disabled = true;
    try {
        const targetScope = document.getElementById('resourceTarget').value;
        const body = {
            targetScope,
            teamId: targetScope === 'TEAM' ? selectedTeamId() : null,
            categoryCode: document.getElementById('resourceCategory').value,
            title: document.getElementById('resourceTitle').value.trim(),
            description: document.getElementById('resourceDescription').value.trim(),
            pinned: document.getElementById('resourcePinned').checked,
            storedFileIds: [],
        };
        const file = document.getElementById('resourceFile').files[0];
        if (file) {
            const form = new FormData();
            form.append('file', file);
            const uploaded = await post('/api/files/private?domain=resource', form);
            body.storedFileIds = [uploaded.id];
        }
        if (editingId) {
            await put(`/api/resource-management/${editingId}`, body);
            if (body.storedFileIds.length) {
                await post(`/api/resource-management/${editingId}/revisions`, {storedFileIds: body.storedFileIds});
            }
        } else {
            if (!body.storedFileIds.length) {
                throw new Error('처음 등록하는 자료는 파일을 선택해 주세요.');
            }
            const created = await post('/api/resource-management', body);
            await post(`/api/resource-management/${created.resourceId}/publish`);
        }
        closeModal(document.getElementById('resourceFormModal'));
        await load();
        showToast('자료를 저장했습니다.');
    } catch (exception) {
        error(exception.message);
    } finally {
        trigger.disabled = false;
    }
}

async function download(trigger) {
    const id = trigger.closest('[data-resource-row]').dataset.resourceId;
    const detail = await get(`/api/resources/${id}`);
    if (!detail.files.length) {
        showToast('다운로드할 파일이 없습니다.');
        return;
    }
    window.location.assign(`/api/resources/${id}/files/${detail.files[0].storedFileId}/download`);
}

async function history(trigger) {
    const id = trigger.closest('[data-resource-row]').dataset.resourceId;
    const detail = await get(`/api/resource-management/${id}`);
    const historyList = lookup('[data-resource-history-list]');
    historyList.replaceChildren();
    detail.revisions.forEach((revision) => {
        const section = element('section', 'rounded-md border p-3');
        section.appendChild(element('b', 'text-sm', `버전 ${revision.revisionNo}`));
        revision.files.forEach((file) => {
            const button = element('button', 'mt-2 block min-h-11 text-left text-sm font-bold text-accent-foreground', file.originalName);
            button.type = 'button';
            button.addEventListener('click', () => window.location.assign(`/api/resources/${id}/files/${file.storedFileId}/download`));
            section.appendChild(button);
        });
        historyList.appendChild(section);
    });
    if (!historyList.children.length) {
        historyList.appendChild(element('p', 'text-sm text-muted-foreground', '등록된 파일 이력이 없습니다.'));
    }
    openModal('resourceHistoryModal', trigger);
}

async function archive(trigger) {
    const id = trigger.closest('[data-resource-row]').dataset.resourceId;
    await post(`/api/resource-management/${id}/archive`);
    await load();
    showToast('자료를 보관했습니다.');
}

searchInput.addEventListener('input', debounce(() => replaceFilters({q: searchInput.value.trim()}), 300));
document.addEventListener('click', (event) => {
    const chip = event.target.closest('[data-filter-group="resource-category"], [data-filter-group="resource-scope"]');
    if (!chip) {
        return;
    }
    activateFilterChip(chip);
    const key = chip.dataset.filterGroup === 'resource-category' ? 'category' : 'scope';
    replaceFilters({[key]: chip.dataset.filterValue});
});
lookup('[data-resource-reset]').addEventListener('click', () => {
    writeUrl(new URLSearchParams(), false);
    load();
});
lookup('[data-resource-retry]').addEventListener('click', () => load());
window.addEventListener('popstate', () => load({focus: true}));
bindPageActions({'resource-create': openCreate, 'resource-save': save, 'resource-edit': openEdit, 'resource-download': download, 'resource-history': history, 'resource-archive': archive});
document.getElementById('resourceTarget').addEventListener('change', updateTeamField);

async function initialize() {
    const [member, teams] = await Promise.all([
        get('/api/members/me'),
        get('/api/members/reference/teams'),
    ]);
    loginMember = member;
    const select = document.getElementById('resourceTeam');
    teams.forEach((team) => {
        const option = element('option', '', team.name);
        option.value = String(team.teamId);
        select.appendChild(option);
    });
    await load();
}

initialize().catch((exception) => setState('자료실을 준비하지 못했습니다', exception.message, {retry: true}));
