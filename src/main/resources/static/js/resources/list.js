import {get, post, put} from '../common/api.js';
import {lookup, element} from '../common/dom.js';
import {activateFilterChip, bindPageActions} from '../common/view.js';
import {openModal, closeModal} from '../common/modal.js';
import {showToast} from '../common/toast.js';

let resources = [];
let editingId = null;
let loginMember = null;

const labels = {SCRIPT: '대본', MINUTES: '회의록', PROMOTION: '홍보물', VIDEO: '영상', OTHER: '기타'};
function error(message = '') { const target = lookup('[data-resource-error]'); target.textContent = message; target.classList.toggle('hidden', !message); }
function manage(resource) { return document.body.dataset.userRole === 'admin' || (document.body.dataset.userRole === 'leader' && resource.teamId === loginMember?.teamId); }
function showListState(message) {
    const row = document.createElement('tr');
    const cell = element('td', 'px-5 py-11 text-center text-sm text-muted-foreground', message);
    cell.colSpan = 5;
    row.appendChild(cell);
    lookup('[data-resource-list]').appendChild(row);
}
function render() {
    const selected = lookup('[data-filter-group="resource"][aria-pressed="true"]').dataset.filterValue;
    const query = lookup('[data-resource-search]').value.trim().toLowerCase();
    const list = lookup('[data-resource-list]'); list.replaceChildren();
    resources.filter((item) => (selected === 'ALL' || item.categoryCode === selected) && `${item.title} ${item.updatedByName || ''}`.toLowerCase().includes(query)).forEach((item) => {
        const row = lookup('[data-resource-row-template]').content.firstElementChild.cloneNode(true); row.dataset.resourceId = item.resourceId;
        lookup('[data-resource-title]', row).textContent = item.title;
        lookup('[data-resource-meta]', row).textContent = item.updatedByName || '수정자 정보 없음';
        lookup('[data-resource-category]', row).textContent = labels[item.categoryCode] || item.categoryCode;
        lookup('[data-resource-version]', row).textContent = item.currentRevisionNo ? `v${item.currentRevisionNo}` : '—';
        lookup('[data-resource-date]', row).textContent = item.updatedDttm ? new Date(item.updatedDttm).toLocaleDateString('ko-KR') : '—';
        const actions = lookup('[data-resource-actions]', row);
        const download = element('button', 'min-h-11 rounded-md border px-3 text-xs font-bold', '다운로드'); download.type = 'button'; download.dataset.pageAction = 'resource-download'; actions.appendChild(download);
        if (manage(item)) {
            const edit = element('button', 'min-h-11 rounded-md border px-3 text-xs font-bold', '수정'); edit.type = 'button'; edit.dataset.pageAction = 'resource-edit'; actions.appendChild(edit);
            const history = element('button', 'min-h-11 rounded-md border px-3 text-xs font-bold', '이력'); history.type = 'button'; history.dataset.pageAction = 'resource-history'; actions.appendChild(history);
            const archive = element('button', 'min-h-11 rounded-md border border-destructive/30 px-3 text-xs font-bold text-destructive', '보관'); archive.type = 'button'; archive.dataset.pageAction = 'resource-archive'; archive.dataset.confirm = '이 자료를 보관할까요? 보관 후 일반 목록에서 숨겨집니다.'; archive.dataset.confirmAction = '자료 보관'; actions.appendChild(archive);
        }
        list.appendChild(row);
    });
    if (!list.children.length) showListState('조건에 맞는 자료가 없습니다.');
}
async function load() { try { resources = await get('/api/resources', {pageSize: 100}); render(); } catch (exception) { const list = lookup('[data-resource-list]'); list.replaceChildren(); showListState(exception.message); } }
function updateTeamField() { const admin = document.body.dataset.userRole === 'admin'; const isTeam = document.getElementById('resourceTarget').value === 'TEAM'; lookup('[data-resource-team-wrap]').classList.toggle('hidden', !admin || !isTeam); }
function selectedTeamId() { return document.body.dataset.userRole === 'admin' ? Number(document.getElementById('resourceTeam').value) || null : loginMember?.teamId; }
function openCreate(trigger) { editingId = null; document.getElementById('resourceFormModalTitle').textContent = '자료 업로드'; ['resourceTitle', 'resourceDescription', 'resourceFile'].forEach((id) => document.getElementById(id).value = ''); document.getElementById('resourcePinned').checked = false; updateTeamField(); error(); openModal('resourceFormModal', trigger); }
async function openEdit(trigger) { const id = trigger.closest('[data-resource-row]').dataset.resourceId; const detail = await get(`/api/resource-management/${id}`); editingId = id; document.getElementById('resourceFormModalTitle').textContent = '자료 수정'; document.getElementById('resourceTitle').value = detail.title; document.getElementById('resourceCategory').value = detail.categoryCode; document.getElementById('resourceTarget').value = detail.targetScope; if (detail.teamId) document.getElementById('resourceTeam').value = String(detail.teamId); document.getElementById('resourceDescription').value = detail.description; document.getElementById('resourcePinned').checked = detail.pinned; document.getElementById('resourceFile').value = ''; updateTeamField(); error(); openModal('resourceFormModal', trigger); }
async function save(trigger) { trigger.disabled = true; try { const targetScope = document.getElementById('resourceTarget').value; const body = {targetScope, teamId: targetScope === 'TEAM' ? selectedTeamId() : null, categoryCode: document.getElementById('resourceCategory').value, title: document.getElementById('resourceTitle').value.trim(), description: document.getElementById('resourceDescription').value.trim(), pinned: document.getElementById('resourcePinned').checked, storedFileIds: []}; const file = document.getElementById('resourceFile').files[0]; if (file) { const form = new FormData(); form.append('file', file); const uploaded = await post('/api/files/private?domain=resource', form); body.storedFileIds = [uploaded.id]; } if (editingId) { await put(`/api/resource-management/${editingId}`, body); if (body.storedFileIds.length) await post(`/api/resource-management/${editingId}/revisions`, {storedFileIds: body.storedFileIds}); } else { if (!body.storedFileIds.length) throw new Error('처음 등록하는 자료는 파일을 선택해 주세요.'); const created = await post('/api/resource-management', body); await post(`/api/resource-management/${created.resourceId}/publish`); } closeModal(document.getElementById('resourceFormModal')); await load(); showToast('자료를 저장했습니다.'); } catch (exception) { error(exception.message); } finally { trigger.disabled = false; } }
async function download(trigger) { const id = trigger.closest('[data-resource-row]').dataset.resourceId; const detail = await get(`/api/resources/${id}`); if (!detail.files.length) { showToast('다운로드할 파일이 없습니다.'); return; } window.location.assign(`/api/resources/${id}/files/${detail.files[0].storedFileId}/download`); }
async function history(trigger) { const id = trigger.closest('[data-resource-row]').dataset.resourceId; const detail = await get(`/api/resource-management/${id}`); const list = lookup('[data-resource-history-list]'); list.replaceChildren(); detail.revisions.forEach((revision) => { const section = element('section', 'rounded-md border p-3'); section.appendChild(element('b', 'text-sm', `버전 ${revision.revisionNo}`)); revision.files.forEach((file) => { const button = element('button', 'mt-2 block min-h-11 text-left text-sm font-bold text-accent-foreground', file.originalName); button.type = 'button'; button.addEventListener('click', () => window.location.assign(`/api/resources/${id}/files/${file.storedFileId}/download`)); section.appendChild(button); }); list.appendChild(section); }); if (!list.children.length) list.appendChild(element('p', 'text-sm text-muted-foreground', '등록된 파일 이력이 없습니다.')); openModal('resourceHistoryModal', trigger); }
async function archive(trigger) { const id = trigger.closest('[data-resource-row]').dataset.resourceId; await post(`/api/resource-management/${id}/archive`); await load(); showToast('자료를 보관했습니다.'); }
lookup('[data-resource-search]').addEventListener('input', render);
document.addEventListener('click', (event) => { const chip = event.target.closest('[data-filter-group="resource"]'); if (chip) { activateFilterChip(chip); render(); } });
bindPageActions({'resource-create': openCreate, 'resource-save': save, 'resource-edit': openEdit, 'resource-download': download, 'resource-history': history, 'resource-archive': archive});
document.getElementById('resourceTarget').addEventListener('change', updateTeamField);
async function initialize() { loginMember = await get('/api/members/me'); const teams = await get('/api/members/reference/teams'); const select = document.getElementById('resourceTeam'); teams.forEach((team) => { const option = document.createElement('option'); option.value = String(team.teamId); option.textContent = team.name; select.appendChild(option); }); await load(); }
initialize().catch((exception) => { const list = lookup('[data-resource-list]'); list.replaceChildren(); showListState(exception.message); });
