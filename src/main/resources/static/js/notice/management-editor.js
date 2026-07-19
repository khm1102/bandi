import {get, post, put} from '../common/api.js';
import {bindPageActions, element, lookup, readValue} from '../common/dom.js';
import {showToast} from '../common/toast.js';

const root = lookup('[data-notice-editor]');
let noticeId = Number(root.dataset.noticeId) || null;
let attachments = [];
let dirty = false;

function updateSaveState(message, saved = false) {
    lookup('[data-notice-save-state]').textContent = message;
    const dot = lookup('[data-notice-save-dot]');
    dot.classList.toggle('bg-warning', !saved);
    dot.classList.toggle('bg-success', saved);
}

function markDirty() {
    dirty = true;
    updateSaveState('저장하지 않은 변경이 있어요');
}

function markSaved() {
    dirty = false;
    updateSaveState('모든 변경을 저장했어요', true);
    lookup('[data-notice-save-time]').textContent =
            `${new Intl.DateTimeFormat('ko-KR', {hour: '2-digit', minute: '2-digit'}).format(new Date())} 저장`;
}

function renderAttachments() {
    const list = lookup('[data-notice-attachments]');
    list.replaceChildren();
    attachments.forEach((attachment) => {
        const item = element('li', 'flex items-center gap-2 rounded-md bg-secondary px-3 py-2 text-xs');
        item.appendChild(element('span', 'min-w-0 flex-1 truncate',
                attachment.originalName || attachment.originalFileName));
        const remove = element('button', 'min-h-11 shrink-0 font-black text-destructive', '제거');
        remove.type = 'button';
        remove.addEventListener('click', () => {
            attachments = attachments.filter((candidate) => candidate.id !== attachment.id);
            renderAttachments();
            markDirty();
        });
        item.appendChild(remove);
        list.appendChild(item);
    });
}

async function upload(file) {
    const formData = new FormData();
    formData.append('file', file);
    const privateFile = await post('/api/files/private', formData, {query: {domain: 'public-notice'}});
    const publicFile = await post(`/api/files/${privateFile.id}/public-promotions`, {domain: 'public-notice'});
    return {id: publicFile.id, originalName: file.name};
}

async function loadDetail() {
    if (!noticeId) {
        updateSaveState('아직 저장하지 않은 새 공시예요');
        return;
    }
    const notice = await get(`/api/admin/public-notices/${noticeId}`);
    document.getElementById('noticeTitle').value = notice.title;
    document.getElementById('noticeBody').value = notice.body;
    const category = document.getElementById('noticeCategory');
    if (!Array.from(category.options).some((option) =>
            option.value === notice.categoryCode)) {
        const option = element('option', '', notice.categoryCode);
        option.value = notice.categoryCode;
        category.appendChild(option);
    }
    category.value = notice.categoryCode;
    document.getElementById('noticePinned').checked = notice.pinned;
    attachments = notice.attachments.map((file) => ({...file, id: file.storedFileId}));
    renderAttachments();
    dirty = false;
    updateSaveState('저장된 초안을 불러왔어요', true);
}

function renderSelectedFiles() {
    const region = lookup('[data-notice-selected-files]');
    const files = Array.from(document.getElementById('noticeFiles').files);
    region.classList.toggle('hidden', files.length === 0);
    region.textContent = files.length === 0 ? ''
        : `저장할 파일 ${files.length}개 · ${files.map((file) => file.name).join(', ')}`;
}

function setError(message) {
    const region = lookup('[data-notice-editor-error]');
    region.textContent = message || '';
    region.classList.toggle('hidden', !message);
}

async function save(trigger) {
    const form = lookup('[data-notice-editor-form]');
    if (!form.reportValidity()) return;
    trigger.disabled = true;
    setError('');
    try {
        const files = Array.from(document.getElementById('noticeFiles').files);
        for (const file of files) {
            attachments.push(await upload(file));
        }
        document.getElementById('noticeFiles').value = '';
        renderSelectedFiles();
        renderAttachments();
        const body = {
            categoryCode: readValue('noticeCategory'), title: readValue('noticeTitle'),
            body: readValue('noticeBody'),
            pinned: document.getElementById('noticePinned').checked,
            attachmentFileIds: attachments.map((file) => file.id),
        };
        if (noticeId) {
            await put(`/api/admin/public-notices/${noticeId}`, body);
            markSaved();
            showToast('공시 초안을 저장했어요.');
        } else {
            const created = await post('/api/admin/public-notices', body);
            noticeId = created.publicNoticeId;
            dirty = false;
            window.location.assign(`/notice-management/${noticeId}/edit?saved=1`);
        }
    } catch (error) {
        setError(error.message || '공시를 저장하지 못했습니다.');
        renderAttachments();
    } finally { trigger.disabled = false; }
}

bindPageActions({'notice-draft-save': save});
lookup('[data-notice-editor-form]').addEventListener('input', markDirty);
lookup('[data-notice-editor-form]').addEventListener('change', markDirty);
document.getElementById('noticeFiles').addEventListener('change', () => {
    renderSelectedFiles();
    markDirty();
});
window.addEventListener('beforeunload', (event) => {
    if (!dirty) {
        return;
    }
    event.preventDefault();
    event.returnValue = '';
});
document.querySelector('a[href="/notice-management"]')?.addEventListener('click', (event) => {
    if (!dirty || window.confirm('저장하지 않은 변경이 있어요. 목록으로 나갈까요?')) {
        return;
    }
    event.preventDefault();
});
loadDetail().catch((error) => {
    setError(error.message || '공시를 불러오지 못했습니다.');
    showToast(error.message || '공시를 불러오지 못했습니다.');
});
