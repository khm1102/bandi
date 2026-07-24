import {get, post} from '../common/api.js';
import {element, lookup} from '../common/dom.js';
import {currentUserRole} from '../common/session.js';
import {badge} from '../common/view.js';

const STATUS_META = Object.freeze({
    SUBMITTED: ['검수 대기', 'info'], TEAM_APPROVED: ['팀장 승인', 'info'],
    REVISION_REQUESTED: ['보완 요청', 'warning'], APPROVED: ['최종 승인', 'success'],
    ARCHIVED: ['보관', 'neutral'],
});
let detail;

function formatDateTime(value) {
    return value ? new Intl.DateTimeFormat('ko-KR', {year: 'numeric', month: 'short',
        day: 'numeric', hour: '2-digit', minute: '2-digit', hour12: false}).format(new Date(value)) : '—';
}
function statusBadge(status) {
    const [label, tone] = STATUS_META[status] || [status || '미정', 'neutral'];
    return badge(label, tone);
}
function state(title, message, retry = false) {
    const root = lookup('[data-review-detail-state]');
    root.classList.remove('hidden');
    lookup('[data-review-detail-content]').classList.add('hidden');
    lookup('[data-review-detail-state-title]', root).textContent = title;
    lookup('[data-review-detail-state-message]', root).textContent = message;
    lookup('[data-review-detail-retry]', root).classList.toggle('hidden', !retry);
}
function recordId() {
    return Number(lookup('[data-review-detail]').dataset.activityRecordId);
}
function renderFiles(record) {
    const root = lookup('[data-review-files]');
    root.replaceChildren();
    const files = record.currentFiles || [];
    lookup('[data-review-files-empty]').classList.toggle('hidden', files.length > 0);
    files.forEach((file) => {
        const card = lookup('[data-review-file-template]').content.firstElementChild.cloneNode(true);
        lookup('[data-review-file-name]', card).textContent = file.originalName;
        const href = `/api/activity-reviews/${record.activityRecordId}/files/${file.storedFileId}/download`;
        const download = lookup('[data-review-file-download]', card);
        download.href = href;
        if (file.fileRole === 'DOCUMENT') {
            const preview = lookup('[data-review-file-preview]', card);
            preview.replaceChildren(element('span', 'px-4 text-center text-sm font-bold text-info', 'HWPX 활동 내역서'));
        } else {
            const image = lookup('[data-review-file-image]', card);
            image.src = href;
            image.alt = `${record.title} 활동 사진`;
            image.addEventListener('error', () => image.replaceWith(element('span', 'px-4 text-center text-xs text-muted-foreground', '사진을 불러오지 못했습니다')), {once: true});
        }
        root.append(card);
    });
}
function renderHistory(record) {
    const root = lookup('[data-review-history]');
    root.replaceChildren();
    const histories = record.reviewHistories || [];
    if (histories.length === 0) {
        root.append(element('p', 'text-xs text-muted-foreground', '아직 검수 이력이 없습니다.'));
        return;
    }
    histories.forEach((history) => {
        const item = element('article', 'rounded-md border px-3 py-2.5');
        const statuses = element('div', 'flex flex-wrap items-center gap-2');
        statuses.append(statusBadge(history.previousStatus), element('span', 'text-xs text-muted-foreground', '→'), statusBadge(history.newStatus));
        item.append(statuses, element('p', 'mt-2 text-xs', history.comment || '상태 변경'),
            element('p', 'mt-1 text-xs text-muted-foreground', `${history.reviewedByName || '처리자 미상'} · ${formatDateTime(history.reviewedDttm)}`));
        root.append(item);
    });
}
function renderRevisions(record) {
    const root = lookup('[data-review-revisions]');
    root.replaceChildren();
    const revisions = record.revisions || [];
    if (revisions.length === 0) {
        root.append(element('p', 'text-xs text-muted-foreground', '제출 이력이 없습니다.'));
        return;
    }
    revisions.forEach((revision) => {
        const item = element('article', 'rounded-md border px-3 py-2.5');
        item.append(element('b', 'text-xs', `v${revision.revisionNo} · ${revision.title}`),
            element('p', 'mt-1 text-xs text-muted-foreground', `${revision.changedByName || '작성자 미상'} · ${formatDateTime(revision.changedDttm)}`),
            element('p', 'mt-2 text-xs', revision.changeReason || '최초 제출'));
        root.append(item);
    });
}
function actionButton(label, action, tone = 'primary') {
    const button = element('button', `min-h-11 rounded-md px-4 text-sm font-bold ${tone === 'danger' ? 'bg-destructive text-destructive-foreground' : 'bg-primary text-primary-foreground'}`, label);
    button.type = 'button';
    button.dataset.reviewAction = action;
    if (action === 'archive') {
        button.dataset.confirm = '이 기록을 보관할까요? 보관 후에도 승인 기록과 HWPX 다운로드는 유지됩니다.';
        button.dataset.confirmAction = '기록 보관';
    }
    return button;
}
function renderActions(record) {
    const panel = lookup('[data-review-action-panel]');
    const actions = lookup('[data-review-actions]');
    const comment = lookup('[data-review-comment]');
    const commentLabel = lookup('[data-review-comment-label]');
    actions.replaceChildren();
    comment.value = '';
    comment.classList.add('hidden');
    commentLabel.classList.add('hidden');
    panel.classList.add('hidden');
    if (currentUserRole === 'leader' && record.status === 'SUBMITTED') {
        panel.classList.remove('hidden');
        lookup('[data-review-action-title]').textContent = '팀장 1차 검수';
        lookup('[data-review-action-description]').textContent = '내용이 맞으면 팀장 승인을, 보완이 필요하면 사유를 남겨 주세요.';
        comment.classList.remove('hidden');
        commentLabel.classList.remove('hidden');
        comment.placeholder = '보완 요청일 때만 사유를 입력해 주세요.';
        actions.append(actionButton('팀장 승인', 'team-approve'), actionButton('보완 요청', 'revision', 'danger'));
    }
    if (currentUserRole === 'admin' && record.status === 'TEAM_APPROVED') {
        panel.classList.remove('hidden');
        lookup('[data-review-action-title]').textContent = '최종 승인';
        lookup('[data-review-action-description]').textContent = '팀장 검수가 끝난 기록입니다. 최종 승인하거나 보완을 요청할 수 있어요.';
        comment.classList.remove('hidden');
        commentLabel.classList.remove('hidden');
        comment.placeholder = '보완 요청일 때만 사유를 입력해 주세요.';
        actions.append(actionButton('최종 승인', 'final-approve'), actionButton('보완 요청', 'revision', 'danger'));
    }
    if (currentUserRole === 'admin' && record.status === 'SUBMITTED') {
        panel.classList.remove('hidden');
        lookup('[data-review-action-title]').textContent = '긴급 최종 승인';
        lookup('[data-review-action-description]').textContent = '팀장 검수를 건너뛰는 승인입니다. 긴급 승인 사유를 반드시 남겨야 합니다.';
        comment.classList.remove('hidden');
        commentLabel.classList.remove('hidden');
        comment.placeholder = '긴급 최종 승인 사유를 입력해 주세요.';
        actions.append(actionButton('긴급 최종 승인', 'final-approve'), actionButton('보완 요청', 'revision', 'danger'));
    }
    if (currentUserRole === 'admin' && record.status === 'APPROVED') {
        panel.classList.remove('hidden');
        lookup('[data-review-action-title]').textContent = '기록 보관';
        lookup('[data-review-action-description]').textContent = '보관 후에도 승인 기록 목록과 HWPX 다운로드는 유지됩니다.';
        actions.append(actionButton('기록 보관', 'archive', 'danger'));
    }
}
function render(record) {
    detail = record;
    const badges = lookup('[data-review-detail-badges]');
    badges.replaceChildren(badge(record.reportDocument ? '한글 내역서' : '간단 기록', 'neutral'), statusBadge(record.status));
    lookup('[data-review-detail-title]').textContent = record.title;
    lookup('[data-review-detail-meta]').textContent = `${record.teamName || '팀 미정'} · ${record.createdByName || '작성자 미상'} 작성 · ${formatDateTime(record.activityDttm)} · 참여 ${record.participantCount}명`;
    lookup('[data-review-detail-body]').textContent = record.body;
    renderFiles(record); renderHistory(record); renderRevisions(record); renderActions(record);
}
async function load() {
    state('활동 기록을 불러오는 중입니다', '잠시만 기다려 주세요.');
    try {
        const record = await get(`/api/activity-reviews/${recordId()}`);
        render(record);
        lookup('[data-review-detail-state]').classList.add('hidden');
        lookup('[data-review-detail-content]').classList.remove('hidden');
    } catch (error) { state('활동 기록을 불러오지 못했습니다', error.message, true); }
}
async function act(button) {
    const action = button.dataset.reviewAction;
    const comment = lookup('[data-review-comment]').value.trim();
    const errorTarget = lookup('[data-review-action-error]');
    errorTarget.classList.add('hidden');
    if ((action === 'revision' || (action === 'final-approve' && detail.status === 'SUBMITTED')) && !comment) {
        errorTarget.textContent = action === 'revision' ? '보완 사유를 입력해 주세요.' : '긴급 승인 사유를 입력해 주세요.';
        errorTarget.classList.remove('hidden');
        lookup('[data-review-comment]').focus();
        return;
    }
    button.disabled = true;
    try {
        if (action === 'team-approve') await post(`/api/activity-reviews/${recordId()}/team-approve`);
        if (action === 'final-approve') await post(`/api/activity-reviews/${recordId()}/final-approve`, {emergencyReason: detail.status === 'SUBMITTED' ? comment : null});
        if (action === 'revision') await post(`/api/activity-reviews/${recordId()}/revision-request`, {comment});
        if (action === 'archive') await post(`/api/activity-reviews/${recordId()}/archive`);
        await load();
    } catch (error) {
        errorTarget.textContent = error.message;
        errorTarget.classList.remove('hidden');
    } finally {
        button.disabled = false;
    }
}
document.addEventListener('click', (event) => {
    const action = event.target.closest('[data-review-action]');
    if (action) act(action);
    if (event.target.closest('[data-page-action="activity-review-detail-retry"]')) load();
});
load();
