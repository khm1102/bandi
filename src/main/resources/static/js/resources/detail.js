import {del, get} from '../common/api.js';
import {mountSafeHtml} from '../common/safe-html.js';
import {initializeShareActions} from '../common/share.js';

const resourceId = Number(window.location.pathname.match(/^\/resources\/(\d+)$/)?.[1]);
const root = document.querySelector('[data-resource-detail]');

function formatDate(value) {
    return new Intl.DateTimeFormat('ko-KR', {
        year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit',
    }).format(new Date(value));
}

function linkCard(preview) {
    const card = document.createElement('a');
    card.href = preview.normalizedUrl;
    card.target = '_blank';
    card.rel = 'noopener noreferrer';
    card.className = 'block rounded-md border p-4 hover:bg-secondary';
    const domain = document.createElement('p');
    domain.className = 'text-xs text-muted-foreground';
    domain.textContent = preview.domain;
    const title = document.createElement('p');
    title.className = 'mt-1 font-bold';
    title.textContent = preview.title || preview.normalizedUrl;
    const description = document.createElement('p');
    description.className = 'mt-1 text-sm text-muted-foreground';
    description.textContent = preview.description || '링크 열기';
    if (preview.previewImageFileId) {
        const image = document.createElement('img');
        image.className = 'mt-3 max-h-56 w-full rounded object-cover';
        image.src = `/api/resources/${resourceId}/link-previews/${preview.previewImageFileId}/inline`;
        image.alt = '';
        image.addEventListener('error', () => image.remove(), {once: true});
        card.append(domain, title, description, image);
        return card;
    }
    card.append(domain, title, description);
    return card;
}

function render(resource) {
    document.querySelector('[data-resource-title]').textContent = resource.title;
    document.querySelector('[data-resource-meta]').textContent = `${resource.createdByName} 작성 · ${formatDate(resource.updatedDttm)} 수정`;
    mountSafeHtml(document.querySelector('[data-resource-body]'), resource.bodyHtml.value ?? resource.bodyHtml);

    const files = document.querySelector('[data-resource-files]');
    files.replaceChildren();
    resource.files.forEach((file) => {
        const item = document.createElement('li');
        const link = document.createElement('a');
        link.className = 'inline-flex min-h-11 items-center rounded-md border px-3 text-sm font-bold hover:bg-secondary';
        link.href = `/api/resources/${resource.resourceId}/files/${file.storedFileId}/download`;
        link.textContent = file.originalName;
        item.append(link);
        files.append(item);
    });

    if (resource.linkPreviews.length) {
        const section = document.createElement('section');
        section.className = 'mt-7 border-t pt-5';
        const heading = document.createElement('h2');
        heading.className = 'text-sm font-extrabold';
        heading.textContent = '링크 미리보기';
        const cards = document.createElement('div');
        cards.className = 'mt-3 grid gap-3';
        resource.linkPreviews.forEach((preview) => cards.append(linkCard(preview)));
        section.append(heading, cards);
        document.querySelector('[data-resource-body]').after(section);
    }

    if (resource.canManage) {
        const edit = document.querySelector('[data-resource-edit]');
        edit.href = `/resources/${resource.resourceId}/edit`;
        edit.className = 'inline-flex min-h-11 items-center rounded-md border px-4 text-sm font-bold hover:bg-secondary';
        edit.textContent = '자료 수정';
        const remove = document.querySelector('[data-resource-delete]');
        remove.className = 'inline-flex min-h-11 items-center rounded-md bg-destructive px-4 text-sm font-bold text-destructive-foreground';
        remove.textContent = '자료 삭제';
        remove.dataset.confirm = `“${resource.title}” 자료를 삭제할까요? 목록과 상세, 첨부 다운로드에서 숨겨집니다.`;
        remove.addEventListener('click', async () => {
            remove.disabled = true;
            try {
                await del(`/api/resources/${resource.resourceId}`);
                window.location.assign('/resources');
            } finally {
                remove.disabled = false;
            }
        });
    }
    const shareButton = document.querySelector('[data-share-button]');
    shareButton.classList.remove('hidden');
    shareButton.dataset.shareCanIssue = String(resource.canIssuePublicShare);
    shareButton.dataset.shareEnabled = String(resource.shareEnabled);
    shareButton.dataset.shareTitle = resource.title;
    shareButton.dataset.shareInternalUrl = `/resources/${resource.resourceId}`;
    shareButton.dataset.shareIssueUrl = `/api/resources/${resource.resourceId}/share-link`;
    const revokeButton = document.querySelector('[data-share-revoke]');
    revokeButton.dataset.shareRevokeUrl = `/api/resources/${resource.resourceId}/share-link`;
    revokeButton.classList.toggle('hidden', !resource.canIssuePublicShare || !resource.shareEnabled);
    initializeShareActions(root);
}

get(`/api/resources/${resourceId}`).then(render).catch((error) => {
    root.replaceChildren();
    const message = document.createElement('p');
    message.className = 'text-sm text-destructive';
    message.textContent = error.message || '자료를 불러오지 못했습니다.';
    root.append(message);
});
