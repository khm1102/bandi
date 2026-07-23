import {get, post, put} from '../common/api.js';
import {element, lookup} from '../common/dom.js';
import {mountSafeHtml} from '../common/safe-html.js';

const ACTIONS = {
    BOLD: 'bold',
    ITALIC: 'italic',
    HEADING_ONE: 'heading-one',
    HEADING_TWO: 'heading-two',
    LIST: 'list',
    QUOTE: 'quote',
    CODE: 'code',
    LINK: 'link',
    TABLE: 'table',
    IMAGE: 'image',
};

const form = lookup('[data-resource-form]');
const titleInput = lookup('[data-resource-title]');
const bodyInput = lookup('[data-resource-body]');
const fileInput = lookup('[data-resource-files]');
const imageInput = lookup('[data-resource-image-files]');
const fileList = lookup('[data-resource-file-list]');
const preview = lookup('[data-resource-preview]');
const previewStatus = lookup('[data-resource-preview-status]');
const errorBox = lookup('[data-resource-error]');
const uploadErrorBox = lookup('[data-resource-upload-error]');
const writePanel = lookup('[data-resource-panel="write"]');
const previewPanel = lookup('[data-resource-panel="preview"]');
const resourceId = Number(window.location.pathname.match(
    /^\/resources\/(\d+)\/edit$/)?.[1]) || null;
let attachments = [];
let previewTimer;
let submitting = false;

function setMessage(target, message = '') {
    target.textContent = message;
    target.classList.toggle('hidden', !message);
}

function showError(message = '') {
    setMessage(errorBox, message);
}

function showUploadError(message = '') {
    setMessage(uploadErrorBox, message);
}

function attachmentIds() {
    return attachments.map((file) => file.storedFileId);
}

function isImage(file) {
    return file.contentType?.startsWith('image/');
}

function renderFiles() {
    fileList.replaceChildren();
    attachments.forEach((file) => {
        const item = element('li',
            'flex min-h-11 items-center gap-3 rounded-md border bg-card px-3 py-2');
        item.appendChild(element('span', 'min-w-0 flex-1 truncate text-sm font-semibold',
            file.originalName));
        if (isImage(file)) {
            item.appendChild(element('span', 'shrink-0 text-xs text-muted-foreground',
                '본문 이미지'));
        }
        const remove = element('button',
            'min-h-9 shrink-0 rounded-md px-2 text-xs font-bold text-destructive hover:bg-destructive-soft',
            '제거');
        remove.type = 'button';
        remove.dataset.removeResourceFileId = String(file.storedFileId);
        remove.setAttribute('aria-label', `${file.originalName} 첨부 제거`);
        item.appendChild(remove);
        fileList.appendChild(item);
    });
    fileList.classList.toggle('hidden', attachments.length === 0);
}

function insertMarkdown(value) {
    const start = bodyInput.selectionStart;
    const end = bodyInput.selectionEnd;
    bodyInput.setRangeText(value, start, end, 'end');
    bodyInput.focus();
    bodyInput.dispatchEvent(new Event('input', {bubbles: true}));
}

function markdownInsertion(action, selectedText) {
    const text = selectedText || '텍스트';
    switch (action) {
        case ACTIONS.BOLD:
            return `**${text}**`;
        case ACTIONS.ITALIC:
            return `*${text}*`;
        case ACTIONS.HEADING_ONE:
            return `# ${selectedText || '제목'}`;
        case ACTIONS.HEADING_TWO:
            return `## ${selectedText || '제목'}`;
        case ACTIONS.LIST:
            return `- ${selectedText || '목록 항목'}`;
        case ACTIONS.QUOTE:
            return `> ${selectedText || '인용할 내용'}`;
        case ACTIONS.CODE:
            return selectedText.includes('\n')
                ? `\`\`\`\n${selectedText}\n\`\`\`` : `\`${text}\``;
        case ACTIONS.LINK:
            return `[${text}](https://)`;
        case ACTIONS.TABLE:
            return '| 항목 | 내용 |\n| --- | --- |\n|  |  |';
        default:
            return text;
    }
}

function insertFormattedMarkdown(action) {
    if (action === ACTIONS.IMAGE) {
        imageInput.click();
        return;
    }
    const selectedText = bodyInput.value.slice(bodyInput.selectionStart,
        bodyInput.selectionEnd);
    insertMarkdown(markdownInsertion(action, selectedText));
}

async function upload(files) {
    showUploadError();
    for (const file of files) {
        const data = new FormData();
        data.append('file', file);
        const response = await post('/api/files/private?domain=resource', data);
        const storedFileId = response.id ?? response.storedFileId;
        if (!storedFileId) {
            throw new Error('첨부 파일 정보를 확인하지 못했어요. 다시 시도해 주세요.');
        }
        const uploaded = {
            storedFileId,
            originalName: file.name,
            contentType: file.type || 'application/octet-stream',
        };
        attachments.push(uploaded);
        if (isImage(uploaded)) {
            insertMarkdown(`![${file.name}](attachment://${storedFileId})`);
        }
    }
    renderFiles();
}

async function refreshPreview() {
    if (!bodyInput.value.trim()) {
        preview.replaceChildren(element('p', 'text-muted-foreground',
            '내용을 입력하면 여기에 미리보기가 표시돼요.'));
        previewStatus.textContent = '';
        return;
    }
    previewStatus.textContent = '미리보기를 만드는 중이에요…';
    try {
        const response = await post('/api/resources/markdown-preview', {
            bodyMarkdown: bodyInput.value,
        });
        mountSafeHtml(preview, response.html);
        previewStatus.textContent = '현재 내용으로 업데이트했어요.';
    } catch (error) {
        previewStatus.textContent =
            '미리보기를 만들지 못했어요. 작성한 내용은 그대로 유지돼요.';
    }
}

function setActiveTab(nextTab) {
    const writing = nextTab === 'write';
    writePanel.classList.toggle('hidden', !writing);
    previewPanel.classList.toggle('hidden', writing);
    document.querySelectorAll('[data-resource-tab]').forEach((tab) => {
        const selected = tab.dataset.resourceTab === nextTab;
        tab.setAttribute('aria-selected', String(selected));
        tab.classList.toggle('bg-secondary', selected);
        tab.classList.toggle('text-muted-foreground', !selected);
    });
    if (writing) {
        bodyInput.focus();
        return;
    }
    refreshPreview().then(() => previewPanel.focus());
}

function setSubmitting(value) {
    submitting = value;
    form.querySelectorAll('button[type="submit"]').forEach((button) => {
        button.disabled = value;
    });
}

async function save() {
    if (submitting) {
        return;
    }
    showError();
    showUploadError();
    setSubmitting(true);
    try {
        const payload = {
            title: titleInput.value.trim(),
            bodyMarkdown: bodyInput.value.trim(),
            attachmentFileIds: attachmentIds(),
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
        showError(error.message
            || '자료를 저장하지 못했어요. 입력한 내용과 첨부 목록은 유지돼요. 다시 시도해 주세요.');
        setSubmitting(false);
    }
}

async function loadEdit() {
    if (!resourceId) {
        renderFiles();
        return;
    }
    const resource = await get(`/api/resources/${resourceId}`);
    if (!resource.canManage) {
        window.location.replace(`/resources/${resourceId}`);
        return;
    }
    document.title = '자료 수정';
    titleInput.value = resource.title;
    bodyInput.value = resource.bodyMarkdown;
    attachments = resource.files.map((file) => ({
        storedFileId: file.storedFileId,
        originalName: file.originalName,
        contentType: file.contentType,
    }));
    renderFiles();
}

form.addEventListener('submit', (event) => {
    event.preventDefault();
    save();
});

form.addEventListener('click', (event) => {
    const tab = event.target.closest('[data-resource-tab]');
    if (tab) {
        setActiveTab(tab.dataset.resourceTab);
        return;
    }
    const format = event.target.closest('[data-resource-markdown-action]');
    if (format) {
        insertFormattedMarkdown(format.dataset.resourceMarkdownAction);
        return;
    }
    const remove = event.target.closest('[data-remove-resource-file-id]');
    if (remove) {
        attachments = attachments.filter((file) => file.storedFileId
            !== Number(remove.dataset.removeResourceFileId));
        renderFiles();
    }
});

fileInput.addEventListener('change', async () => {
    try {
        await upload(fileInput.files);
        fileInput.value = '';
    } catch (error) {
        showUploadError(error.message || '파일을 올리지 못했어요. 다시 시도해 주세요.');
    }
});

imageInput.addEventListener('change', async () => {
    try {
        await upload(imageInput.files);
        imageInput.value = '';
    } catch (error) {
        showUploadError(error.message || '이미지를 올리지 못했어요. 다시 시도해 주세요.');
    }
});

bodyInput.addEventListener('dragover', (event) => {
    event.preventDefault();
    bodyInput.classList.add('border-primary');
});

bodyInput.addEventListener('dragleave', () => {
    bodyInput.classList.remove('border-primary');
});

bodyInput.addEventListener('drop', async (event) => {
    event.preventDefault();
    bodyInput.classList.remove('border-primary');
    try {
        await upload(event.dataTransfer.files);
    } catch (error) {
        showUploadError(error.message || '파일을 올리지 못했어요. 다시 시도해 주세요.');
    }
});

bodyInput.addEventListener('input', () => {
    window.clearTimeout(previewTimer);
    previewTimer = window.setTimeout(() => {
        if (!previewPanel.classList.contains('hidden')) {
            refreshPreview();
        }
    }, 400);
});

loadEdit().catch((error) => {
    showError(error.message || '자료를 불러오지 못했어요. 다시 시도해 주세요.');
});
