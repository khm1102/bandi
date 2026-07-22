import {get, post, put} from '../common/api.js';
import {lookup, element} from '../common/dom.js';
import {mountSafeHtml} from '../common/safe-html.js';

const form = lookup('[data-notice-form]');
const noticeId = form.dataset.noticeId || null;
let attachments = [];
let teams = [];

function setError(message = '') {
    const target = lookup('[data-notice-error]');
    target.textContent = message;
    target.classList.toggle('hidden', !message);
}

function renderAttachments() {
    const list = lookup('[data-notice-file-list]');
    list.replaceChildren();
    attachments.forEach((file) => {
        const row = element('li', 'flex min-h-11 items-center gap-2 rounded-md border px-3');
        row.appendChild(element('span', 'min-w-0 flex-1 truncate', file.originalName));
        const remove = element('button', 'rounded-md px-2 text-xs font-bold text-destructive', '제거');
        remove.type = 'button';
        remove.addEventListener('click', () => { attachments = attachments.filter((item) => item.storedFileId !== file.storedFileId); renderAttachments(); });
        row.appendChild(remove); list.appendChild(row);
    });
}

function updateTeamField() {
    lookup('[data-notice-team-wrap]').classList.toggle('hidden', lookup('[data-notice-target]').value !== 'TEAM');
}

async function refreshPreview() {
    const status = lookup('[data-preview-status]');
    const markdown = lookup('[data-notice-body]').value;
    if (!markdown.trim()) {
        mountSafeHtml(lookup('[data-notice-preview]'), '<p>내용을 입력하면 미리보기가 표시됩니다.</p>');
        status.textContent = '';
        return;
    }
    status.textContent = '미리보기를 만드는 중…';
    try {
        const response = await post('/api/internal-notices/markdown-preview', {bodyMarkdown: markdown});
        mountSafeHtml(lookup('[data-notice-preview]'), response.html.value);
        status.textContent = '미리보기를 업데이트했습니다.';
    } catch (error) { status.textContent = '미리보기를 만들지 못했습니다. 내용을 그대로 유지합니다.'; }
}

async function uploadFiles() {
    const files = [...lookup('[data-notice-files]').files];
    for (const file of files) {
        const body = new FormData(); body.append('file', file);
        const created = await post('/api/files/private?domain=notice', body);
        attachments.push({storedFileId: created.id, originalName: file.name});
    }
    lookup('[data-notice-files]').value = '';
    renderAttachments();
}

function requestBody() {
    return {targetScope: lookup('[data-notice-target]').value,
        teamId: lookup('[data-notice-target]').value === 'TEAM' ? Number(lookup('[data-notice-team]').value) : null,
        title: lookup('[data-notice-title]').value.trim(), body: lookup('[data-notice-body]').value,
        important: lookup('[data-notice-important]').checked,
        attachmentFileIds: attachments.map((file) => file.storedFileId)};
}

async function save(publish) {
    setError();
    try {
        await uploadFiles();
        const body = requestBody();
        let id = noticeId;
        if (id) await put(`/api/internal-notice-management/${id}`, body);
        else { const created = await post('/api/internal-notice-management', body); id = created.internalNoticeId; }
        const scheduledAt = lookup('[data-notice-publish-at]').value;
        if (publish) {
            await post(`/api/internal-notice-management/${id}/publish`, {publishStartDttm: scheduledAt ? `${scheduledAt}:00` : null, publishEndDttm: null});
        }
        window.location.assign(publish && !scheduledAt ? `/notices/${id}` : `/notices/${id}/edit`);
    } catch (error) { setError(error.message); }
}

async function initialize() {
    teams = await get('/api/members/reference/teams');
    const member = await get('/api/members/me');
    const select = lookup('[data-notice-team]');
    teams.filter((team) => document.body.dataset.userRole === 'admin' || team.teamId === member.teamId)
        .forEach((team) => { const option = document.createElement('option'); option.value = String(team.teamId); option.textContent = team.name; select.appendChild(option); });
    if (noticeId) {
        const notice = await get(`/api/internal-notice-management/${noticeId}`);
        lookup('[data-notice-target]').value = notice.targetScope;
        if (notice.teamId) select.value = String(notice.teamId);
        lookup('[data-notice-title]').value = notice.title;
        lookup('[data-notice-body]').value = notice.bodyMarkdown;
        lookup('[data-notice-important]').checked = notice.important;
        attachments = notice.attachments; renderAttachments();
    }
    updateTeamField(); refreshPreview();
}

lookup('[data-notice-target]').addEventListener('change', updateTeamField);
lookup('[data-preview-refresh]').addEventListener('click', refreshPreview);
form.addEventListener('submit', (event) => { event.preventDefault(); save(event.submitter?.dataset.pageAction === 'publish-notice'); });
initialize().catch((error) => setError(error.message));
