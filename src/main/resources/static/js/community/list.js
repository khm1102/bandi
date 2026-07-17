import {showToast} from '../common/toast.js';
import {all, bindPageActions, element, lookup, readValue} from '../common/dom.js';
import {currentUserRole, memberProfiles} from '../common/session.js';
import {activateFilterChip, badge, closeActionModal} from '../common/view.js';

const ACTIONS = Object.freeze({
    LIKE: 'post-like',
    ADD: 'post-add',
    NOTICE: 'post-notice'
});

function buildPostCard(category, title, body, pinned) {
    const card = element('div', pinned
        ? 'rounded-lg border border-primary/40 bg-card p-5 ring-2 ring-ring/10'
        : 'rounded-lg border bg-card p-5');
    card.dataset.postCard = '';
    card.dataset.category = category;
    const header = element('div', 'mb-2 flex items-center gap-2.5');
    const avatarClasses = 'flex size-7 items-center justify-center rounded-full bg-primary '
        + 'text-xs font-black text-primary-foreground';
    header.appendChild(element('span', avatarClasses, memberProfiles[currentUserRole][1]));
    const author = element('div', 'min-w-0 flex-1');
    author.append(
        element('b', 'block text-sm', memberProfiles[currentUserRole][0]),
        element('span', 'text-xs text-muted-foreground', '방금')
    );
    header.append(author, badge(pinned ? '공지' : category, pinned ? 'accent' : 'neutral'));
    const footer = element('div', 'mt-3 flex items-center gap-4 text-xs font-bold text-muted-foreground');
    const like = element('button', '', '좋아요 ');
    like.type = 'button';
    like.dataset.pageAction = ACTIONS.LIKE;
    const count = element('span', '', '0');
    count.dataset.likeCount = '';
    like.appendChild(count);
    footer.append(like, element('span', '', '댓글 0'));
    card.append(
        header,
        element('p', 'text-sm font-extrabold', title),
        element('p', 'mt-1 text-sm text-muted-foreground', body),
        footer
    );
    return card;
}

function addPost(trigger, notice) {
    const title = readValue(notice ? 'npTitle' : 'poTitle');
    if (!title) {
        showToast('제목을 입력해 주세요');
        return;
    }
    const category = notice ? '공지' : readValue('poCat');
    const body = readValue(notice ? 'npBody' : 'poBody');
    lookup('[data-post-list]').prepend(buildPostCard(category, title, body, notice));
    closeActionModal(trigger);
    showToast(notice ? '공지를 등록했어요. 상단에 고정됩니다' : '글을 등록했어요');
}

document.addEventListener('click', (event) => {
    const filter = event.target.closest('[data-filter-group="post"]');
    if (!filter) {
        return;
    }
    activateFilterChip(filter);
    const category = filter.dataset.filterValue;
    all('[data-post-card]').forEach((card) => {
        card.hidden = category !== '전체' && card.dataset.category !== category;
    });
});

bindPageActions({
    [ACTIONS.LIKE]: (trigger) => {
        const count = lookup('[data-like-count]', trigger);
        count.textContent = String(Number(count.textContent) + 1);
    },
    [ACTIONS.ADD]: (trigger) => addPost(trigger, false),
    [ACTIONS.NOTICE]: (trigger) => addPost(trigger, true)
});
