import {get, post, put} from '../common/api.js';
import {
    focusDateTimeField,
    initializeDateTimeFields,
    readDateTimeValue,
    setDateTimeDisabled,
    setDateTimeValue,
} from '../common/date-time-field.js';
import {debounce, element, lookup} from '../common/dom.js';
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
const MAX_INLINE_IMAGE_BYTES = 10 * 1024 * 1024;
const INLINE_IMAGE_TYPES = new Set(['image/jpeg', 'image/png', 'image/webp']);

const form = lookup('[data-notice-form]');
const noticeId = form.dataset.noticeId ? Number(form.dataset.noticeId) : null;
const titleInput = lookup('[data-notice-title]');
const bodyInput = lookup('[data-notice-body]');
const attachmentInput = lookup('[data-notice-files]');
const imageInput = lookup('[data-notice-image-files]');
const preview = lookup('[data-notice-preview]');
const previewStatus = lookup('[data-preview-status]');
const errorTarget = lookup('[data-notice-error]');
const uploadErrorTarget = lookup('[data-notice-upload-error]');
const attachmentList = lookup('[data-notice-file-list]');
const targetSelect = lookup('[data-notice-target]');
const teamSelect = lookup('[data-notice-team]');
const teamWrap = lookup('[data-notice-team-wrap]');
const importantInput = lookup('[data-notice-important]');
const scheduleEnabledInput = lookup('[data-notice-schedule-enabled]');
const scheduleWrap = lookup('[data-notice-schedule-wrap]');
const scheduleError = lookup('[data-notice-schedule-error]');
const writePanel = lookup('[data-notice-panel="write"]');
const previewPanel = lookup('[data-notice-panel="preview"]');
let attachments = [];
let pendingAttachmentFiles = [];
let teams = [];
let submitting = false;
let activeTab = 'write';

function setMessage(target, message = '') {
    target.textContent = message;
    target.classList.toggle('hidden', !message);
}

function setError(message = '') {
    setMessage(errorTarget, message);
}

function setUploadError(message = '') {
    setMessage(uploadErrorTarget, message);
}

function hasDroppedFiles(event) {
    return Array.from(event.dataTransfer?.types || []).includes('Files');
}

function attachmentIds() {
    return attachments.map((attachment) => attachment.storedFileId);
}

function bodyReferencesAttachment(storedFileId) {
    const expression = new RegExp(`attachment://${storedFileId}(?![0-9])`);
    return expression.test(bodyInput.value);
}

function renderAttachments() {
    attachmentList.replaceChildren();
    attachments.forEach((file) => {
        const row = element('li', 'flex min-h-11 items-center gap-3 rounded-md border bg-card px-3 py-2');
        const name = element('span', 'min-w-0 flex-1 truncate text-sm font-semibold', file.originalName);
        row.appendChild(name);
        if (isInlineImage(file)) {
            row.appendChild(element('span', 'shrink-0 text-xs text-muted-foreground', '본문 이미지'));
        }
        const remove = element('button',
            'min-h-9 shrink-0 rounded-md px-2 text-xs font-bold text-destructive hover:bg-destructive-soft',
            '제거');
        remove.type = 'button';
        remove.dataset.removeAttachmentId = String(file.storedFileId);
        remove.setAttribute('aria-label', `${file.originalName} 첨부 제거`);
        row.appendChild(remove);
        attachmentList.appendChild(row);
    });
    pendingAttachmentFiles.forEach((file, index) => {
        const row = element('li',
            'flex min-h-11 items-center gap-3 rounded-md border border-dashed bg-secondary/40 px-3 py-2');
        row.appendChild(element('span', 'min-w-0 flex-1 truncate text-sm font-semibold', file.name));
        row.appendChild(element('span', 'shrink-0 text-xs text-muted-foreground', '업로드 대기'));
        const remove = element('button',
            'min-h-9 shrink-0 rounded-md px-2 text-xs font-bold text-destructive hover:bg-destructive-soft',
            '제거');
        remove.type = 'button';
        remove.dataset.removePendingAttachmentIndex = String(index);
        remove.setAttribute('aria-label', `${file.name} 첨부 선택 취소`);
        row.appendChild(remove);
        attachmentList.appendChild(row);
    });
    attachmentList.classList.toggle('hidden', attachments.length === 0
        && pendingAttachmentFiles.length === 0);
}

function isInlineImage(file) {
    return file.sizeBytes <= MAX_INLINE_IMAGE_BYTES && INLINE_IMAGE_TYPES.has(file.contentType);
}

function updateTeamField() {
    teamWrap.classList.toggle('hidden', targetSelect.value !== 'TEAM');
}

function updateTargetLabel() {
    const teamOption = targetSelect.querySelector('option[value="TEAM"]');
    const team = teams.find((item) => item.teamId === Number(teamSelect.value));
    if (teamOption && team) {
        teamOption.textContent = `${team.name} 멤버`;
    }
}

function updateScheduleField() {
    const enabled = scheduleEnabledInput.checked;
    scheduleWrap.classList.toggle('hidden', !enabled);
    setDateTimeDisabled('noticePublishAt', !enabled);
    setMessage(scheduleError);
}

function followsMinuteStep(value, step) {
    return value && Number(value.slice(14, 16)) % step === 0;
}

function setActiveTab(nextTab) {
    activeTab = nextTab;
    const writing = nextTab === 'write';
    writePanel.classList.toggle('hidden', !writing);
    previewPanel.classList.toggle('hidden', writing);
    document.querySelectorAll('[data-notice-tab]').forEach((tab) => {
        const selected = tab.dataset.noticeTab === nextTab;
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
            return selectedText.includes('\n') ? `\`\`\`\n${selectedText}\n\`\`\`` : `\`${text}\``;
        case ACTIONS.LINK:
            return `[${text}](https://)`;
        case ACTIONS.TABLE:
            return '| 항목 | 내용 |\n| --- | --- |\n|  |  |';
        default:
            return text;
    }
}

function insertMarkdown(action) {
    const start = bodyInput.selectionStart;
    const end = bodyInput.selectionEnd;
    const selectedText = bodyInput.value.slice(start, end);
    const inserted = markdownInsertion(action, selectedText);
    bodyInput.setRangeText(inserted, start, end, 'end');
    bodyInput.focus();
    bodyInput.dispatchEvent(new Event('input', {bubbles: true}));
}

async function refreshPreview() {
    const markdown = bodyInput.value;
    if (!markdown.trim()) {
        preview.replaceChildren(element('p', 'text-muted-foreground',
            '내용을 입력하면 여기에 미리보기가 표시돼요.'));
        previewStatus.textContent = '';
        return;
    }
    previewStatus.textContent = '미리보기를 만드는 중이에요…';
    try {
        const response = await post('/api/internal-notice-management/markdown-preview', {
            internalNoticeId: noticeId,
            bodyMarkdown: markdown,
            attachmentFileIds: attachmentIds(),
        });
        mountSafeHtml(preview, response.html.value);
        previewStatus.textContent = '현재 내용으로 업데이트했어요.';
    } catch (error) {
        previewStatus.textContent =
            '미리보기를 만들지 못했어요. 작성한 내용은 그대로 유지돼요.';
    }
}

function appendAttachment(file) {
    if (attachments.some((attachment) => attachment.storedFileId === file.storedFileId)) {
        return;
    }
    attachments.push(file);
}

async function uploadAttachments() {
    while (pendingAttachmentFiles.length > 0) {
        const file = pendingAttachmentFiles[0];
        const body = new FormData();
        body.append('file', file);
        const created = await post('/api/files/private?domain=notice', body);
        appendAttachment({storedFileId: created.id, originalName: file.name,
            contentType: file.type || 'application/octet-stream', sizeBytes: file.size});
        pendingAttachmentFiles.shift();
        renderAttachments();
    }
}

async function uploadInlineImages(files) {
    setUploadError();
    for (const file of files) {
        if (!INLINE_IMAGE_TYPES.has(file.type) || file.size > MAX_INLINE_IMAGE_BYTES) {
            throw new Error('본문 이미지는 JPG, PNG, WebP 파일만 10MiB 이하로 첨부할 수 있어요.');
        }
        const body = new FormData();
        body.append('file', file);
        const uploaded = await post('/api/internal-notice-management/images', body);
        appendAttachment(uploaded);
        const start = bodyInput.selectionStart;
        bodyInput.setRangeText(`![${uploaded.originalName}](attachment://${uploaded.storedFileId})`,
                start, bodyInput.selectionEnd, 'end');
    }
    renderAttachments();
    bodyInput.focus();
    bodyInput.dispatchEvent(new Event('input', {bubbles: true}));
}

function requestBody() {
    const targetScope = targetSelect.value;
    return {
        targetScope,
        teamId: targetScope === 'TEAM' ? Number(teamSelect.value) : null,
        title: titleInput.value.trim(),
        body: bodyInput.value,
        important: importantInput.checked,
        attachmentFileIds: attachmentIds(),
    };
}

function setSubmitting(value) {
    submitting = value;
    form.querySelectorAll('button[type="submit"]').forEach((button) => {
        button.disabled = value;
    });
}

async function save(publish) {
    if (submitting) {
        return;
    }
    setError();
    setUploadError();
    const scheduledAt = scheduleEnabledInput.checked
        ? readDateTimeValue('noticePublishAt') : '';
    if (publish && scheduleEnabledInput.checked && !scheduledAt) {
        setMessage(scheduleError, '예약 게시 시각을 선택해 주세요.');
        focusDateTimeField('noticePublishAt');
        return;
    }
    if (publish && scheduledAt && new Date(scheduledAt) <= new Date()) {
        setMessage(scheduleError, '예약 게시 시각은 현재보다 뒤로 선택해 주세요.');
        focusDateTimeField('noticePublishAt');
        return;
    }
    if (publish && scheduledAt && !followsMinuteStep(scheduledAt, 5)) {
        setMessage(scheduleError, '예약 게시 시각은 5분 단위로 입력해 주세요.');
        focusDateTimeField('noticePublishAt');
        return;
    }
    setSubmitting(true);
    try {
        await uploadAttachments();
        const body = requestBody();
        let savedNoticeId = noticeId;
        if (savedNoticeId) {
            await put(`/api/internal-notice-management/${savedNoticeId}`, body);
        } else {
            const created = await post('/api/internal-notice-management', body);
            savedNoticeId = created.internalNoticeId;
        }
        if (publish) {
            await post(`/api/internal-notice-management/${savedNoticeId}/publish`, {
                publishStartDttm: scheduledAt ? `${scheduledAt}:00` : null,
                publishEndDttm: null,
            });
        }
        window.location.assign(publish && !scheduledAt
            ? `/notices/${savedNoticeId}` : `/notices/${savedNoticeId}/edit`);
    } catch (error) {
        setError(error.message
            || '저장하지 못했어요. 입력한 내용과 첨부 목록은 유지돼요. 다시 시도해 주세요.');
        setSubmitting(false);
    }
}

async function initialize() {
    teams = await get('/api/members/reference/teams');
    const member = await get('/api/members/me');
    teams.filter((team) => document.body.dataset.userRole === 'admin' || team.teamId === member.teamId)
        .forEach((team) => {
            const option = document.createElement('option');
            option.value = String(team.teamId);
            option.textContent = team.name;
            teamSelect.appendChild(option);
        });
    teamSelect.value = String(member.teamId);
    if (noticeId) {
        const notice = await get(`/api/internal-notice-management/${noticeId}`);
        targetSelect.value = notice.targetScope;
        if (notice.teamId) {
            teamSelect.value = String(notice.teamId);
        }
        titleInput.value = notice.title;
        bodyInput.value = notice.bodyMarkdown;
        importantInput.checked = notice.important;
        scheduleEnabledInput.checked = notice.status === 'SCHEDULED';
        setDateTimeValue('noticePublishAt',
                scheduleEnabledInput.checked && notice.publishStartDttm
                    ? notice.publishStartDttm.slice(0, 16) : '');
        attachments = notice.attachments;
    }
    renderAttachments();
    updateTeamField();
    updateTargetLabel();
    updateScheduleField();
}

form.addEventListener('submit', (event) => {
    event.preventDefault();
    save(event.submitter?.dataset.pageAction === 'publish-notice');
});
form.addEventListener('click', (event) => {
    const toolbarButton = event.target.closest('[data-markdown-action]');
    if (toolbarButton) {
        const action = toolbarButton.dataset.markdownAction;
        if (action === ACTIONS.IMAGE) {
            imageInput.click();
        } else {
            insertMarkdown(action);
        }
        return;
    }
    const remove = event.target.closest('[data-remove-attachment-id]');
    if (remove) {
        const storedFileId = Number(remove.dataset.removeAttachmentId);
        if (bodyReferencesAttachment(storedFileId)) {
            setUploadError(
                '본문에서 이 이미지를 먼저 제거한 뒤 첨부 목록에서 제거해 주세요.');
            return;
        }
        attachments = attachments.filter((attachment) => attachment.storedFileId !== storedFileId);
        renderAttachments();
        return;
    }
    const pendingRemove = event.target.closest('[data-remove-pending-attachment-index]');
    if (pendingRemove) {
        pendingAttachmentFiles.splice(
            Number(pendingRemove.dataset.removePendingAttachmentIndex), 1);
        renderAttachments();
    }
});
document.querySelectorAll('[data-notice-tab]').forEach((tab) => {
    tab.addEventListener('click', () => setActiveTab(tab.dataset.noticeTab));
});
targetSelect.addEventListener('change', () => {
    updateTeamField();
    updateTargetLabel();
});
teamSelect.addEventListener('change', updateTargetLabel);
scheduleEnabledInput.addEventListener('change', updateScheduleField);
attachmentInput.addEventListener('change', () => {
    pendingAttachmentFiles = pendingAttachmentFiles.concat([...attachmentInput.files]);
    attachmentInput.value = '';
    renderAttachments();
});
imageInput.addEventListener('change', () => {
    const files = [...imageInput.files];
    imageInput.value = '';
    uploadInlineImages(files).catch((error) => {
        setUploadError(error.message
            || '이미지를 올리지 못했어요. 파일을 확인한 뒤 다시 시도해 주세요.');
    });
});
bodyInput.addEventListener('dragover', (event) => {
    if (!hasDroppedFiles(event)) {
        return;
    }
    event.preventDefault();
    bodyInput.classList.add('border-ring', 'bg-accent/40');
});
bodyInput.addEventListener('dragleave', () => {
    bodyInput.classList.remove('border-ring', 'bg-accent/40');
});
bodyInput.addEventListener('drop', (event) => {
    if (!hasDroppedFiles(event)) {
        return;
    }
    event.preventDefault();
    bodyInput.classList.remove('border-ring', 'bg-accent/40');
    bodyInput.focus();
    uploadInlineImages([...event.dataTransfer.files]).catch((error) => {
        setUploadError(error.message
            || '이미지를 올리지 못했어요. 파일을 확인한 뒤 다시 시도해 주세요.');
    });
});
bodyInput.addEventListener('input', debounce(() => {
    if (activeTab === 'preview') {
        refreshPreview();
    }
}));
document.addEventListener('keydown', (event) => {
    if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === 's') {
        event.preventDefault();
        save(false);
    }
});
initializeDateTimeFields();
initialize().catch((error) => setError(error.message));
