import {get, post, put} from '../common/api.js';
import {bindPageActions, element, lookup, readValue} from '../common/dom.js';
import {showToast} from '../common/toast.js';

const root = lookup('[data-notice-editor]');
const noticeId = Number(root.dataset.noticeId) || null;
let attachments = [];

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
    if (!noticeId) return;
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
        for (const file of files) attachments.push(await upload(file));
        const body = {
            categoryCode: readValue('noticeCategory'), title: readValue('noticeTitle'),
            body: readValue('noticeBody'),
            pinned: document.getElementById('noticePinned').checked,
            attachmentFileIds: attachments.map((file) => file.id),
        };
        if (noticeId) await put(`/api/admin/public-notices/${noticeId}`, body);
        else await post('/api/admin/public-notices', body);
        window.location.assign('/notice-management');
    } catch (error) {
        setError(error.message || '공시를 저장하지 못했습니다.');
        renderAttachments();
    } finally { trigger.disabled = false; }
}

bindPageActions({'notice-draft-save': save});
loadDetail().catch((error) => {
    setError(error.message || '공시를 불러오지 못했습니다.');
    showToast(error.message || '공시를 불러오지 못했습니다.');
});
