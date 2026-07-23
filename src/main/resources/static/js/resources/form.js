import {get, post, put} from '../common/api.js';
import {mountSafeHtml} from '../common/safe-html.js';

const form = document.querySelector('[data-resource-form]');
const titleInput = document.querySelector('[data-resource-title]');
const bodyInput = document.querySelector('[data-resource-body]');
const fileInput = document.querySelector('[data-resource-files]');
const fileList = document.querySelector('[data-resource-file-list]');
const preview = document.querySelector('[data-resource-preview]');
const errorBox = document.querySelector('[data-resource-error]');
const resourceId = Number(window.location.pathname.match(/^\/resources\/(\d+)\/edit$/)?.[1]) || null;
let attachments = [];
let previewTimer;

function showError(message = '') {
    errorBox.textContent = message;
    errorBox.classList.toggle('hidden', !message);
}

function fileId(response) {
    return response.id ?? response.storedFileId;
}

function renderFiles() {
    fileList.replaceChildren();
    attachments.forEach((file) => {
        const item = document.createElement('li');
        item.className = 'flex items-center justify-between gap-3 rounded-md border bg-card px-3 py-2 text-sm';
        const name = document.createElement('span');
        name.className = 'min-w-0 truncate';
        name.textContent = file.originalName;
        const remove = document.createElement('button');
        remove.type = 'button';
        remove.className = 'min-h-11 shrink-0 text-sm font-bold text-destructive';
        remove.textContent = '제거';
        remove.addEventListener('click', () => {
            attachments = attachments.filter((candidate) => candidate.storedFileId !== file.storedFileId);
            renderFiles();
        });
        item.append(name, remove);
        fileList.append(item);
    });
}

async function upload(files) {
    for (const file of files) {
        const data = new FormData();
        data.append('file', file);
        const response = await post('/api/files/private?domain=resource', data);
        const storedFileId = fileId(response);
        attachments.push({storedFileId, originalName: file.name, contentType: file.type || ''});
        if (file.type.startsWith('image/')) {
            insertMarkdown(`![${file.name}](attachment://${storedFileId})`);
        }
    }
    renderFiles();
}

function insertMarkdown(value) {
    const start = bodyInput.selectionStart;
    const end = bodyInput.selectionEnd;
    const current = bodyInput.value;
    bodyInput.value = `${current.slice(0, start)}${value}${current.slice(end)}`;
    bodyInput.selectionStart = bodyInput.selectionEnd = start + value.length;
    bodyInput.focus();
}

const markdownActions = {
    bold: () => insertMarkdown('**굵은 텍스트**'),
    heading: () => insertMarkdown('# 제목'),
    list: () => insertMarkdown('- 목록 항목'),
    quote: () => insertMarkdown('> 인용문'),
    code: () => insertMarkdown('`코드`'),
    link: () => insertMarkdown('[링크 설명](https://example.com)'),
    table: () => insertMarkdown('| 항목 | 내용 |\n| --- | --- |\n|  |  |'),
    image: () => fileInput.click(),
};

async function refreshPreview() {
    if (!bodyInput.value.trim()) {
        preview.replaceChildren();
        return;
    }
    const response = await post('/api/resources/markdown-preview', {bodyMarkdown: bodyInput.value});
    mountSafeHtml(preview, response.html);
}

function selectTab(tab) {
    document.querySelectorAll('[data-resource-tab]').forEach((button) => {
        button.classList.toggle('bg-card', button.dataset.resourceTab === tab);
    });
    document.querySelector('[data-resource-panel="write"]').classList.toggle('hidden', tab !== 'write');
    document.querySelector('[data-resource-panel="preview"]').classList.toggle('hidden', tab !== 'preview');
    if (tab === 'preview') {
        refreshPreview().catch((error) => showError(error.message));
    }
}

async function loadEdit() {
    if (!resourceId) {
        return;
    }
    const resource = await get(`/api/resources/${resourceId}`);
    if (!resource.canManage) {
        window.location.replace(`/resources/${resourceId}`);
        return;
    }
    document.title = '자료 수정';
    document.querySelector('[data-page-title]')?.replaceChildren('자료 수정');
    titleInput.value = resource.title;
    bodyInput.value = resource.bodyMarkdown;
    attachments = resource.files.map((file) => ({
        storedFileId: file.storedFileId,
        originalName: file.originalName,
        contentType: file.contentType,
    }));
    renderFiles();
}

form.addEventListener('submit', async (event) => {
    event.preventDefault();
    showError();
    const submitter = event.submitter;
    submitter.disabled = true;
    try {
        const payload = {
            title: titleInput.value.trim(),
            bodyMarkdown: bodyInput.value.trim(),
            attachmentFileIds: attachments.map((file) => file.storedFileId),
        };
        if (resourceId) {
            await put(`/api/resources/${resourceId}`, payload);
            window.location.assign(`/resources/${resourceId}`);
            return;
        }
        const response = await post('/api/resources', payload);
        const created = response?.resourceId || response?.id;
        window.location.assign(created ? `/resources/${created}` : '/resources');
    } catch (error) {
        showError(error.message || '자료를 저장하지 못했습니다. 입력한 내용은 유지돼요.');
    } finally {
        submitter.disabled = false;
    }
});

document.querySelectorAll('[data-resource-tab]').forEach((button) => {
    button.addEventListener('click', () => selectTab(button.dataset.resourceTab));
});
document.querySelectorAll('[data-markdown]').forEach((button) => {
    button.addEventListener('click', () => markdownActions[button.dataset.markdown]?.());
});
fileInput.addEventListener('change', async () => {
    try {
        await upload(fileInput.files);
        fileInput.value = '';
    } catch (error) {
        showError(error.message || '파일을 올리지 못했습니다.');
    }
});
bodyInput.addEventListener('dragover', (event) => {
    event.preventDefault();
    bodyInput.classList.add('border-primary');
});
bodyInput.addEventListener('dragleave', () => bodyInput.classList.remove('border-primary'));
bodyInput.addEventListener('drop', async (event) => {
    event.preventDefault();
    bodyInput.classList.remove('border-primary');
    try {
        await upload(event.dataTransfer.files);
    } catch (error) {
        showError(error.message || '파일을 올리지 못했습니다.');
    }
});
bodyInput.addEventListener('input', () => {
    window.clearTimeout(previewTimer);
    previewTimer = window.setTimeout(() => {
        if (!document.querySelector('[data-resource-panel="preview"]').classList.contains('hidden')) {
            refreshPreview().catch((error) => showError(error.message));
        }
    }, 400);
});
loadEdit().catch((error) => showError(error.message || '자료를 불러오지 못했습니다.'));
