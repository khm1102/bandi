import {del, get, patch, post, put} from '../common/api.js';
import {all, bindPageActions, element, lookup, readValue} from '../common/dom.js';
import {openModal} from '../common/modal.js';
import {closeSheetOf, openSheet} from '../common/sheet.js';
import {showToast} from '../common/toast.js';
import {badge} from '../common/view.js';

const ACTIONS = Object.freeze({
    PROFILE_CREATE: 'profile-create-open', PROFILE_EDIT: 'profile-edit-open',
    PROFILE_SAVE: 'profile-save', PROFILE_VISIBILITY: 'profile-visibility-change',
    CONSENT_OPEN: 'profile-consent-open', CONSENT_SAVE: 'profile-consent-save',
    CONSENT_REVOKE: 'profile-consent-revoke',
    POLICY_OPEN: 'profile-policy-open', POLICY_SAVE: 'profile-policy-save',
    CHARACTER_CREATE: 'character-create-open', CHARACTER_EDIT: 'character-edit-open',
    CHARACTER_SAVE: 'character-save', CHARACTER_DELETE: 'character-delete',
    CAST_CREATE: 'cast-create-open', CAST_EDIT: 'cast-edit-open', CAST_SAVE: 'cast-save',
    CAST_DELETE: 'cast-delete', CAST_HISTORY: 'cast-history-open',
    ROUND_CAST_CREATE: 'round-cast-create-open', ROUND_CAST_EDIT: 'round-cast-edit-open',
    ROUND_CAST_SAVE: 'round-cast-save', ROUND_CAST_DELETE: 'round-cast-delete',
    CREDIT_CREATE: 'credit-create-open', CREDIT_EDIT: 'credit-edit-open',
    CREDIT_SAVE: 'credit-save', CREDIT_DELETE: 'credit-delete',
    MEDIA_CREATE: 'media-create-open', MEDIA_EDIT: 'media-edit-open',
    MEDIA_SAVE: 'media-save', MEDIA_PUBLISHED: 'media-published-change',
    MEDIA_DELETE: 'media-delete',
});
const VISIBILITY_LABELS = {DRAFT: '초안', PUBLISHED: '게시', ARCHIVED: '보관'};
const IMPORTANCE_LABELS = {LEAD: '주연', SUPPORT: '조연', ENSEMBLE: '앙상블'};
const CAST_TYPE_LABELS = {PRIMARY: '주 캐스팅', ALTERNATE: '대체', UNDERSTUDY: '언더스터디'};
const MEDIA_TYPE_LABELS = {
    POSTER: '포스터·키아트', PROFILE: '프로필', REHEARSAL: '연습',
    BEHIND: '비하인드', STAGE: '무대', VIDEO: '영상',
};
const CONSENT_LABELS = {NAME: '공개 이름', PHOTO: '사진', BIO: '소개', SOCIAL: 'SNS'};
const HISTORY_ACTION_LABELS = {ASSIGN: '배정', CHANGE: '변경', REMOVE: '해제'};
const HISTORY_SCOPE_LABELS = {PROJECT: '작품', ROUND: '회차'};
const CONTENT_TABS = new Set(['profiles', 'casting', 'rounds', 'credits']);

let projects = [];
let profiles = [];
let members = [];
let policyDocuments = [];
let policyVersions = [];
let profileConsents = new Map();
let characters = [];
let casts = [];
let rounds = [];
let roundCasts = [];
let credits = [];
let mediaItems = [];
let editingProfile = null;
let consentingProfile = null;
let editingCharacter = null;
let editingCast = null;
let editingRoundCast = null;
let editingCredit = null;
let editingMedia = null;

function project() {
    const id = Number(readValue('contentProjectSelect'));
    return projects.find((item) => item.performanceProjectId === id) || null;
}

function selectedRound() {
    const id = Number(readValue('roundCastRoundSelect'));
    return rounds.find((item) => item.performanceRoundId === id) || null;
}

function profileById(id) {
    return profiles.find((item) => item.publicProfileId === id) || null;
}

function characterById(id) {
    return characters.find((item) => item.performanceCharacterId === id) || null;
}

function appendOption(select, value, label) {
    const option = element('option', '', label);
    option.value = String(value);
    select.appendChild(option);
}

function actionButton(label, action, id, tone = 'outline') {
    const style = tone === 'danger'
            ? 'border-destructive/30 text-destructive hover:bg-destructive-soft'
            : tone === 'primary'
                ? 'border-primary bg-primary text-primary-foreground hover:bg-primary-strong'
                : 'hover:bg-secondary';
    const button = element('button',
            `min-h-11 rounded-md border bg-card px-3 text-xs font-bold transition-colors ${style}`,
            label);
    button.type = 'button';
    button.dataset.pageAction = action;
    if (id !== undefined) button.dataset.targetId = String(id);
    return button;
}

function emptyState(message) {
    return element('p', 'px-5 py-10 text-center text-sm text-muted-foreground', message);
}

function formatDateTime(value) {
    return value ? value.replace('T', ' ').slice(0, 16) : '-';
}

function currentLocalDateTime() {
    const now = new Date();
    const local = new Date(now.getTime() - now.getTimezoneOffset() * 60_000);
    return local.toISOString().slice(0, 16);
}

function currentConsent(profileId, scope) {
    return (profileConsents.get(profileId) || [])
            .filter((item) => item.consentScope === scope)
            .sort((left, right) => left.agreedDttm.localeCompare(right.agreedDttm))
            .at(-1) || null;
}

function hasConsent(profileId, scope) {
    const consent = currentConsent(profileId, scope);
    return Boolean(consent?.agreed && !consent.revokedDttm);
}

function availableProfiles() {
    return profiles.filter((profile) => profile.visibilityStatus === 'PUBLISHED'
            && hasConsent(profile.publicProfileId, 'NAME'));
}

function setSelectItems(id, items, valueKey, label) {
    const select = document.getElementById(id);
    select.replaceChildren();
    items.forEach((item) => appendOption(select, item[valueKey], label(item)));
}

function requireProject() {
    if (project()) {
        return true;
    }
    showToast('먼저 공연 프로젝트를 선택해 주세요.');
    return false;
}

function announce(message) {
    lookup('[data-content-status]').textContent = message;
}

function selectContentTab(name, options = {}) {
    const selectedName = CONTENT_TABS.has(name) ? name : 'profiles';
    all('[data-content-tab]').forEach((tab) => {
        const active = tab.dataset.contentTab === selectedName;
        tab.setAttribute('aria-selected', String(active));
        tab.tabIndex = active ? 0 : -1;
        tab.classList.toggle('border-primary', active);
        tab.classList.toggle('border-transparent', !active);
        tab.classList.toggle('text-foreground', active);
        tab.classList.toggle('text-muted-foreground', !active);
    });
    all('[data-content-panel]').forEach((panel) => {
        panel.classList.toggle('hidden', panel.dataset.contentPanel !== selectedName);
    });
    if (options.updateHash !== false) {
        history.replaceState(null, '', `#${selectedName}`);
    }
    if (options.focus) {
        lookup(`[data-content-tab="${selectedName}"]`).focus();
    }
}

function renderSummary() {
    lookup('[data-summary-profiles]').textContent = String(profiles.filter(
            (profile) => profile.visibilityStatus === 'PUBLISHED').length);
    lookup('[data-summary-characters]').textContent = String(characters.length);
    lookup('[data-summary-casts]').textContent = String(casts.length);
    lookup('[data-summary-media]').textContent = String(mediaItems.filter(
            (media) => media.published).length);
}

function setNextAction(kind, title, description, label, tabName = null, targetId = null) {
    lookup('[data-content-next-title]').textContent = title;
    lookup('[data-content-next-description]').textContent = description;
    const button = lookup('[data-content-next-action]');
    button.textContent = label;
    button.dataset.nextAction = kind;
    if (tabName) {
        button.dataset.nextTab = tabName;
    } else {
        delete button.dataset.nextTab;
    }
    if (targetId) {
        button.dataset.targetId = String(targetId);
    } else {
        delete button.dataset.targetId;
    }
}

function renderNextAction() {
    const selected = project();
    if (!selected) {
        setNextAction('project', '먼저 공연 프로젝트를 만들어 주세요',
                '공연 콘텐츠는 학기별 공연 프로젝트에 연결돼요.', '공연 운영 설정으로 이동');
        return;
    }
    if (!profiles.length) {
        setNextAction(ACTIONS.PROFILE_CREATE, '첫 공개 프로필을 등록해 주세요',
                '배우와 제작진의 공개 이름을 먼저 준비해야 캐스팅을 연결할 수 있어요.',
                '공개 프로필 추가', 'profiles');
        return;
    }
    const profileWithoutNameConsent = profiles.find((profile) =>
        profile.visibilityStatus !== 'ARCHIVED' && !hasConsent(profile.publicProfileId, 'NAME'));
    if (profileWithoutNameConsent) {
        setNextAction(ACTIONS.CONSENT_OPEN, `${profileWithoutNameConsent.publicName}님의 공개 동의가 필요해요`,
                '공개 이름 동의를 기록해야 프로필을 게시하고 캐스팅에 배정할 수 있어요.',
                '동의 기록하기', 'profiles', profileWithoutNameConsent.publicProfileId);
        return;
    }
    const draftProfile = profiles.find((profile) => profile.visibilityStatus === 'DRAFT');
    if (draftProfile) {
        setNextAction('review-profile', `${draftProfile.publicName} 프로필을 게시할지 확인해 주세요`,
                '사진과 소개, 공개 동의 범위를 검토한 뒤 목록에서 게시할 수 있어요.',
                '프로필 검토', 'profiles', draftProfile.publicProfileId);
        return;
    }
    if (!characters.length) {
        setNextAction(ACTIONS.CHARACTER_CREATE, '첫 등장인물을 등록해 주세요',
                '배역을 만든 뒤 공개 프로필을 작품 캐스팅에 연결할 수 있어요.',
                '등장인물 추가', 'casting');
        return;
    }
    const unassignedCharacter = characters.find((character) => !casts.some((cast) =>
        cast.performanceCharacterId === character.performanceCharacterId));
    if (unassignedCharacter) {
        setNextAction(ACTIONS.CAST_CREATE, `${unassignedCharacter.name} 배역의 배우를 정해 주세요`,
                '등장인물마다 작품 전체의 기본 캐스팅을 연결해요.',
                '작품 캐스팅 배정', 'casting');
        return;
    }
    const currentRound = selectedRound();
    if (currentRound && !roundCasts.length) {
        setNextAction(ACTIONS.ROUND_CAST_CREATE, `${currentRound.roundNo}회차 캐스팅을 확정해 주세요`,
                '회차별 실제 출연자가 작품 기본 캐스팅과 다를 수 있어요.',
                '회차 캐스팅 배정', 'rounds', currentRound.performanceRoundId);
        return;
    }
    if (!credits.length) {
        setNextAction(ACTIONS.CREDIT_CREATE, '제작진 크레딧을 등록해 주세요',
                '연출, 무대, 조명 등 관객에게 공개할 제작진을 순서대로 기록해요.',
                '크레딧 추가', 'credits');
        return;
    }
    if (!mediaItems.length) {
        setNextAction(ACTIONS.MEDIA_CREATE, '공연을 소개할 사진을 추가해 주세요',
                '포스터나 연습 사진에 대체 텍스트와 제작 크레딧을 함께 기록해요.',
                '미디어 추가', 'credits');
        return;
    }
    const unpublishedMedia = mediaItems.find((media) => !media.published);
    if (unpublishedMedia) {
        setNextAction('review-media', `${unpublishedMedia.title}의 게시 여부를 확인해 주세요`,
                '설명과 대체 텍스트를 검토한 뒤 목록에서 게시할 수 있어요.',
                '미디어 검토', 'credits', unpublishedMedia.performanceMediaId);
        return;
    }
    setNextAction('public-page', '관객에게 보여줄 콘텐츠가 준비됐어요',
            '외부 공연 페이지의 공개 상태와 관람 안내를 마지막으로 확인해 주세요.',
            '외부 공개 설정으로 이동');
}

async function withBusy(trigger, task) {
    trigger.disabled = true;
    try {
        await task();
    } catch (error) {
        showToast(error.message || '요청을 처리하지 못했습니다.');
    } finally {
        trigger.disabled = false;
    }
}

async function uploadPublicFile(file, imageOnly = false) {
    if (!file) return null;
    if (imageOnly && !file.type.startsWith('image/')) {
        throw new Error('이미지 파일만 업로드할 수 있습니다.');
    }
    const data = new FormData();
    data.append('file', file);
    const privateFile = await post('/api/files/private', data,
            {query: {domain: 'performance'}});
    const promoted = await post(`/api/files/${privateFile.id}/public-promotions`,
            {domain: 'performance'});
    return promoted.id;
}

function renderProjectSelect() {
    const select = document.getElementById('contentProjectSelect');
    const previous = select.value;
    select.replaceChildren();
    projects.filter((item) => item.status !== 'CANCELLED').forEach((item) =>
        appendOption(select, item.performanceProjectId,
                `${item.academicYear} ${item.termCode} · ${item.title}`));
    if (previous && Array.from(select.options).some((option) => option.value === previous)) {
        select.value = previous;
    }
    lookup('[data-content-project-state]').textContent = project()
            ? `${project().title}의 공개 콘텐츠를 편집하고 있어요.`
            : '등록된 공연 프로젝트가 없어요.';
    [ACTIONS.CHARACTER_CREATE, ACTIONS.CAST_CREATE, ACTIONS.CAST_HISTORY,
        ACTIONS.ROUND_CAST_CREATE, ACTIONS.CREDIT_CREATE, ACTIONS.MEDIA_CREATE]
            .forEach((action) => {
                const button = lookup(`[data-page-action="${action}"]`);
                if (button) button.disabled = !project();
            });
}

function renderProfiles() {
    const region = lookup('[data-profile-list]');
    region.replaceChildren();
    if (!profiles.length) {
        region.appendChild(emptyState('등록된 공개 프로필이 없습니다.'));
        return;
    }
    const memberMap = new Map(members.map((item) => [item.memberId, item]));
    profiles.forEach((profile) => {
        const row = element('article', 'py-4');
        row.dataset.profileId = String(profile.publicProfileId);
        const head = element('div', 'flex flex-wrap items-center gap-2');
        head.append(element('h3', 'text-sm font-black', profile.publicName),
                badge(VISIBILITY_LABELS[profile.visibilityStatus],
                        profile.visibilityStatus === 'PUBLISHED' ? 'success' : 'neutral'));
        const source = profile.memberId ? memberMap.get(profile.memberId)?.name || `멤버 #${profile.memberId}` : '외부 참여자';
        head.appendChild(element('span', 'text-xs text-muted-foreground', source));
        const actions = element('div', 'ml-auto flex flex-wrap gap-1');
        if (profile.visibilityStatus !== 'ARCHIVED') {
            actions.append(actionButton('수정', ACTIONS.PROFILE_EDIT, profile.publicProfileId),
                    actionButton('동의', ACTIONS.CONSENT_OPEN, profile.publicProfileId));
            if (profile.visibilityStatus === 'DRAFT') {
                const publish = actionButton('게시', ACTIONS.PROFILE_VISIBILITY,
                        profile.publicProfileId, 'primary');
                publish.dataset.visibility = 'PUBLISHED';
                actions.appendChild(publish);
            } else {
                const draft = actionButton('초안 전환', ACTIONS.PROFILE_VISIBILITY,
                        profile.publicProfileId);
                draft.dataset.visibility = 'DRAFT';
                const archive = actionButton('보관', ACTIONS.PROFILE_VISIBILITY,
                        profile.publicProfileId, 'danger');
                archive.dataset.visibility = 'ARCHIVED';
                archive.dataset.confirm = '보관한 공개 프로필은 다시 수정할 수 없습니다. 계속할까요?';
                archive.dataset.confirmAction = '프로필 보관';
                actions.append(draft, archive);
            }
        }
        head.appendChild(actions);
        const scopes = element('div', 'mt-3 flex flex-wrap gap-1');
        Object.entries(CONSENT_LABELS).forEach(([scope, label]) => scopes.appendChild(
                badge(`${label} ${hasConsent(profile.publicProfileId, scope) ? '동의' : '미동의'}`,
                        hasConsent(profile.publicProfileId, scope) ? 'success' : 'neutral')));
        row.append(head, scopes);
        if (profile.bio) row.appendChild(element('p', 'mt-3 text-sm leading-6 text-muted-foreground', profile.bio));
        region.appendChild(row);
    });
}

function renderCharacters() {
    const region = lookup('[data-character-list]');
    region.replaceChildren();
    if (!characters.length) {
        region.appendChild(emptyState('등록된 등장인물이 없습니다.'));
        return;
    }
    characters.forEach((character) => {
        const row = element('article', 'py-4');
        const head = element('div', 'flex items-center gap-2');
        head.append(element('h3', 'font-black', character.name),
                badge(IMPORTANCE_LABELS[character.importance], 'info'),
                element('span', 'text-xs text-muted-foreground', `순서 ${character.displayOrder}`));
        const actions = element('div', 'ml-auto flex gap-1');
        actions.append(actionButton('수정', ACTIONS.CHARACTER_EDIT, character.performanceCharacterId));
        const remove = actionButton('삭제', ACTIONS.CHARACTER_DELETE,
                character.performanceCharacterId, 'danger');
        remove.dataset.confirm = '이 등장인물을 삭제할까요? 연결된 캐스팅이 있으면 삭제할 수 없습니다.';
        remove.dataset.confirmAction = '등장인물 삭제';
        actions.appendChild(remove);
        head.appendChild(actions);
        row.appendChild(head);
        if (character.description) row.appendChild(
                element('p', 'mt-2 text-sm leading-6 text-muted-foreground', character.description));
        region.appendChild(row);
    });
}

function renderCasts() {
    const region = lookup('[data-cast-list]');
    region.replaceChildren();
    if (!casts.length) {
        region.appendChild(emptyState('등록된 작품 캐스팅이 없습니다.'));
        return;
    }
    casts.forEach((cast) => {
        const row = element('article', 'py-4');
        const profile = profileById(cast.publicProfileId);
        const head = element('div', 'flex flex-wrap items-center gap-2');
        head.append(element('strong', '', cast.characterName),
                element('span', 'text-muted-foreground', '—'),
                element('span', 'font-bold', profile?.publicName || `프로필 #${cast.publicProfileId}`),
                badge(CAST_TYPE_LABELS[cast.castType], 'accent'));
        const actions = element('div', 'ml-auto flex gap-1');
        actions.append(actionButton('변경', ACTIONS.CAST_EDIT, cast.performanceCastId));
        const remove = actionButton('해제', ACTIONS.CAST_DELETE, cast.performanceCastId, 'danger');
        remove.dataset.confirm = '이 작품 캐스팅을 해제할까요? 변경 이력은 유지됩니다.';
        remove.dataset.confirmAction = '캐스팅 해제';
        actions.appendChild(remove);
        head.appendChild(actions);
        row.append(head, element('p', 'mt-2 text-xs text-muted-foreground', `표시 순서 ${cast.displayOrder}`));
        region.appendChild(row);
    });
}

function renderRoundSelect() {
    const select = document.getElementById('roundCastRoundSelect');
    const previous = select.value;
    select.replaceChildren();
    rounds.forEach((round) => appendOption(select, round.performanceRoundId,
            `${round.roundNo}회차 · ${formatDateTime(round.startDttm)}`));
    if (previous && Array.from(select.options).some((option) => option.value === previous)) {
        select.value = previous;
    }
}

function renderRoundCasts() {
    const region = lookup('[data-round-cast-list]');
    region.replaceChildren();
    if (!selectedRound()) {
        region.appendChild(emptyState('등록된 공연 회차가 없습니다.'));
        return;
    }
    if (!roundCasts.length) {
        region.appendChild(emptyState('이 회차에 확정된 캐스팅이 없습니다.'));
        return;
    }
    roundCasts.forEach((cast) => {
        const row = element('article', 'py-4');
        const profile = profileById(cast.publicProfileId);
        const head = element('div', 'flex flex-wrap items-center gap-2');
        head.append(element('strong', '', cast.characterName),
                element('span', 'text-muted-foreground', '—'),
                element('span', 'font-bold', profile?.publicName || `프로필 #${cast.publicProfileId}`),
                badge(CAST_TYPE_LABELS[cast.castType], 'accent'));
        const actions = element('div', 'ml-auto flex gap-1');
        actions.append(actionButton('변경', ACTIONS.ROUND_CAST_EDIT,
                cast.performanceRoundCastId));
        const remove = actionButton('해제', ACTIONS.ROUND_CAST_DELETE,
                cast.performanceRoundCastId, 'danger');
        remove.dataset.confirm = '이 회차 캐스팅을 해제할까요? 변경 이력은 유지됩니다.';
        remove.dataset.confirmAction = '회차 캐스팅 해제';
        actions.appendChild(remove);
        head.appendChild(actions);
        row.appendChild(head);
        region.appendChild(row);
    });
}

function renderCredits() {
    const region = lookup('[data-credit-list]');
    region.replaceChildren();
    if (!credits.length) {
        region.appendChild(emptyState('등록된 제작진 크레딧이 없습니다.'));
        return;
    }
    credits.forEach((credit) => {
        const row = element('article', 'flex flex-wrap items-center gap-2 py-4');
        row.append(element('strong', 'text-sm', credit.creditRole),
                element('span', 'text-muted-foreground', '—'),
                element('span', 'text-sm', credit.publicName));
        const actions = element('div', 'ml-auto flex gap-1');
        actions.append(actionButton('수정', ACTIONS.CREDIT_EDIT, credit.productionCreditId));
        const remove = actionButton('삭제', ACTIONS.CREDIT_DELETE,
                credit.productionCreditId, 'danger');
        remove.dataset.confirm = '이 제작진 크레딧을 삭제할까요?';
        remove.dataset.confirmAction = '크레딧 삭제';
        actions.appendChild(remove);
        row.appendChild(actions);
        region.appendChild(row);
    });
}

function renderMedia() {
    const region = lookup('[data-media-list]');
    region.replaceChildren();
    if (!mediaItems.length) {
        region.appendChild(emptyState('등록된 공연 미디어가 없습니다.'));
        return;
    }
    mediaItems.forEach((media) => {
        const row = element('article', 'py-4');
        row.dataset.mediaId = String(media.performanceMediaId);
        const head = element('div', 'flex flex-wrap items-center gap-2');
        head.append(element('strong', 'text-sm', media.title),
                badge(MEDIA_TYPE_LABELS[media.mediaType], 'info'),
                badge(media.published ? '게시' : '비공개', media.published ? 'success' : 'neutral'));
        const actions = element('div', 'ml-auto flex gap-1');
        actions.append(actionButton('수정', ACTIONS.MEDIA_EDIT, media.performanceMediaId));
        const published = actionButton(media.published ? '숨기기' : '게시',
                ACTIONS.MEDIA_PUBLISHED, media.performanceMediaId,
                media.published ? 'outline' : 'primary');
        published.dataset.published = String(!media.published);
        const remove = actionButton('삭제', ACTIONS.MEDIA_DELETE,
                media.performanceMediaId, 'danger');
        remove.dataset.confirm = '이 공연 미디어를 삭제할까요?';
        remove.dataset.confirmAction = '미디어 삭제';
        actions.append(published, remove);
        head.appendChild(actions);
        row.append(head, element('p', 'mt-2 line-clamp-2 text-sm text-muted-foreground', media.description),
                element('p', 'mt-2 text-xs text-muted-foreground', `대체 텍스트: ${media.altText} · ${media.creditText}`));
        region.appendChild(row);
    });
}

async function loadProfileConsents() {
    const entries = await Promise.all(profiles.map(async (profile) => [
        profile.publicProfileId,
        await get(`/api/public-profile-management/${profile.publicProfileId}/consents`),
    ]));
    profileConsents = new Map(entries);
}

async function loadPolicyVersions(documents) {
    const active = documents.filter((document) => document.active
            && document.policyType === 'PRIVACY');
    const versions = await Promise.all(active.map(async (document) => {
        const items = await get(`/api/policies/${document.policyDocumentId}/versions`);
        return items.map((version) => ({...version, documentTitle: document.title}));
    }));
    const now = new Date();
    policyVersions = versions.flat().filter((version) =>
        new Date(version.effectiveFromDttm) <= now)
            .sort((left, right) => right.versionNo - left.versionNo);
}

async function loadReferences() {
    const [nextProjects, nextProfiles, nextMembers, documents] = await Promise.all([
        get('/api/performance-management/projects', {limit: 100}),
        get('/api/public-profile-management', {limit: 200}),
        get('/api/members'),
        get('/api/policies'),
    ]);
    projects = nextProjects;
    profiles = nextProfiles;
    members = nextMembers;
    policyDocuments = documents;
    await Promise.all([loadProfileConsents(), loadPolicyVersions(documents)]);
    renderProjectSelect();
    renderProfiles();
}

async function loadRoundCasts() {
    roundCasts = selectedRound()
            ? await get(`/api/performance-content-management/rounds/${selectedRound().performanceRoundId}/casts`)
            : [];
    renderRoundCasts();
    renderNextAction();
}

async function loadProjectContent() {
    const selected = project();
    if (!selected) {
        characters = []; casts = []; rounds = []; credits = []; mediaItems = []; roundCasts = [];
    } else {
        [characters, casts, rounds, credits, mediaItems] = await Promise.all([
            get(`/api/performance-content-management/projects/${selected.performanceProjectId}/characters`),
            get(`/api/performance-content-management/projects/${selected.performanceProjectId}/casts`),
            get(`/api/performance-management/projects/${selected.performanceProjectId}/rounds`),
            get(`/api/performance-content-management/projects/${selected.performanceProjectId}/credits`),
            get(`/api/performance-content-management/projects/${selected.performanceProjectId}/media`),
        ]);
    }
    renderCharacters();
    renderCasts();
    renderRoundSelect();
    renderCredits();
    renderMedia();
    await loadRoundCasts();
    renderSummary();
    renderNextAction();
    announce(`${selected?.title || '공연'} 콘텐츠를 불러왔어요.`);
}

async function loadAll() {
    await loadReferences();
    await loadProjectContent();
}

function fillMemberOptions() {
    const select = document.getElementById('profileMember');
    select.replaceChildren();
    appendOption(select, '', '외부 참여자');
    const used = new Set(profiles.filter((item) => item.publicProfileId !== editingProfile?.publicProfileId)
            .map((item) => item.memberId).filter(Boolean));
    members.filter((member) => !used.has(member.memberId)).forEach((member) =>
        appendOption(select, member.memberId, `${member.name} · ${member.studentNo}`));
}

function openProfileForm(trigger) {
    editingProfile = trigger.dataset.targetId
            ? profileById(Number(trigger.dataset.targetId)) : null;
    lookup('[data-profile-form]').reset();
    fillMemberOptions();
    document.getElementById('profileMember').disabled = Boolean(editingProfile);
    document.getElementById('profileMember').value = editingProfile?.memberId || '';
    document.getElementById('profileName').value = editingProfile?.publicName || '';
    document.getElementById('profileBio').value = editingProfile?.bio || '';
    document.getElementById('profileSocial').value = editingProfile?.socialUrl || '';
    document.getElementById('profileImage').value = '';
    openSheet('publicProfileSheet', trigger);
}

async function saveProfile(trigger) {
    if (!lookup('[data-profile-form]').reportValidity()) return;
    await withBusy(trigger, async () => {
        const imageId = await uploadPublicFile(document.getElementById('profileImage').files[0], true);
        const body = {publicName: readValue('profileName'), bio: readValue('profileBio') || null,
            profileFileId: imageId || editingProfile?.profileFileId || null,
            socialUrl: readValue('profileSocial') || null};
        if (editingProfile) {
            await put(`/api/public-profile-management/${editingProfile.publicProfileId}`, body);
        } else {
            await post('/api/public-profile-management', {
                memberId: Number(readValue('profileMember')) || null, ...body,
            });
        }
        closeSheetOf(trigger);
        await loadReferences();
        await loadProjectContent();
        showToast('공개 프로필을 저장했습니다.');
    });
}

async function changeProfileVisibility(trigger) {
    const profile = profileById(Number(trigger.dataset.targetId));
    const next = trigger.dataset.visibility;
    if (next === 'PUBLISHED' && !hasConsent(profile.publicProfileId, 'NAME')) {
        showToast('공개 이름 동의를 먼저 기록해 주세요.');
        return;
    }
    await withBusy(trigger, async () => {
        await patch(`/api/public-profile-management/${profile.publicProfileId}/visibility`,
                {visibilityStatus: next});
        await loadReferences();
        await loadProjectContent();
        showToast('프로필 게시 상태를 변경했습니다.');
    });
}

function renderConsentList() {
    const list = lookup('[data-consent-list]');
    list.replaceChildren();
    const consents = profileConsents.get(consentingProfile.publicProfileId) || [];
    if (!consents.length) {
        list.appendChild(element('p', 'rounded-md bg-secondary px-3 py-2 text-sm text-muted-foreground',
                '기록된 동의가 없습니다.'));
        return;
    }
    consents.slice().reverse().forEach((consent) => {
        const row = element('div', 'flex items-center gap-2 rounded-md border px-3 py-2');
        row.append(badge(CONSENT_LABELS[consent.consentScope], consent.agreed ? 'success' : 'neutral'),
                element('span', 'min-w-0 flex-1 text-xs text-muted-foreground',
                        `문서 버전 #${consent.policyDocumentVersionId} · ${formatDateTime(consent.agreedDttm)}`));
        if (consent.agreed && !consent.revokedDttm) {
            const revoke = actionButton('철회', ACTIONS.CONSENT_REVOKE,
                    consent.publicProfileConsentId, 'danger');
            revoke.dataset.confirm = `${CONSENT_LABELS[consent.consentScope]} 공개 동의를 철회할까요?`;
            revoke.dataset.confirmAction = '동의 철회';
            row.appendChild(revoke);
        }
        list.appendChild(row);
    });
}

function openConsent(trigger) {
    consentingProfile = profileById(Number(trigger.dataset.targetId));
    lookup('[data-consent-profile-name]').textContent = consentingProfile.publicName;
    const scopeSelect = document.getElementById('consentScope');
    scopeSelect.replaceChildren();
    const availableScopes = [
        ['NAME', true], ['PHOTO', Boolean(consentingProfile.profileFileId)],
        ['BIO', Boolean(consentingProfile.bio)],
        ['SOCIAL', Boolean(consentingProfile.socialUrl)],
    ];
    availableScopes.filter(([, exists]) => exists).forEach(([scope]) =>
        appendOption(scopeSelect, scope, CONSENT_LABELS[scope]));
    updateConsentPolicyOptions();
    renderConsentList();
    openSheet('profileConsentSheet', trigger);
}

function updateConsentPolicyOptions() {
    const select = document.getElementById('consentPolicyVersion');
    select.replaceChildren();
    const scope = readValue('consentScope');
    const usedVersionIds = new Set((profileConsents.get(
            consentingProfile?.publicProfileId) || [])
            .filter((consent) => consent.consentScope === scope)
            .map((consent) => consent.policyDocumentVersionId));
    const available = policyVersions.filter((version) =>
        !usedVersionIds.has(version.policyDocumentVersionId));
    available.forEach((version) => appendOption(select,
            version.policyDocumentVersionId,
            `${version.documentTitle} · v${version.versionNo}`));
    if (!available.length) appendOption(select, '',
            '이 항목에 사용할 새 정책 버전이 필요합니다');
}

function togglePolicyTitle() {
    const isNew = !readValue('policyDocument');
    lookup('[data-policy-title-field]').classList.toggle('hidden', !isNew);
    document.getElementById('policyTitle').required = isNew;
}

function openPolicyForm(trigger) {
    lookup('[data-policy-form]').reset();
    const select = document.getElementById('policyDocument');
    select.replaceChildren();
    appendOption(select, '', '새 개인정보 공개 동의 문서');
    policyDocuments.filter((document) => document.active
            && document.policyType === 'PRIVACY').forEach((document) =>
        appendOption(select, document.policyDocumentId, document.title));
    document.getElementById('policyEffectiveFrom').value = currentLocalDateTime();
    document.getElementById('policyRequired').checked = true;
    togglePolicyTitle();
    openSheet('profilePolicySheet', trigger);
}

async function savePolicy(trigger) {
    if (!lookup('[data-policy-form]').reportValidity()) return;
    await withBusy(trigger, async () => {
        let policyDocumentId = Number(readValue('policyDocument')) || null;
        if (!policyDocumentId) {
            const created = await post('/api/policies', {
                policyType: 'PRIVACY', title: readValue('policyTitle'), audience: 'ALL',
            });
            policyDocumentId = created.id;
        }
        await post(`/api/policies/${policyDocumentId}/versions`, {
            body: readValue('policyBody'),
            effectiveFromDttm: readValue('policyEffectiveFrom'),
            required: document.getElementById('policyRequired').checked,
        });
        closeSheetOf(trigger);
        const documents = await get('/api/policies');
        policyDocuments = documents;
        await loadPolicyVersions(documents);
        showToast('프로필 동의 문서 버전을 발행했습니다.');
    });
}

async function saveConsent(trigger) {
    const policyDocumentVersionId = Number(readValue('consentPolicyVersion'));
    if (!policyDocumentVersionId) {
        showToast('먼저 적용 가능한 정책 문서 버전을 등록해 주세요.');
        return;
    }
    await withBusy(trigger, async () => {
        await post(`/api/public-profile-management/${consentingProfile.publicProfileId}/consents`, {
            policyDocumentVersionId, consentScope: readValue('consentScope'),
        });
        await loadProfileConsents();
        renderConsentList();
        renderProfiles();
        showToast('프로필 공개 동의를 기록했습니다.');
    });
}

async function revokeConsent(trigger) {
    await withBusy(trigger, async () => {
        await post(`/api/public-profile-management/consents/${trigger.dataset.targetId}/revoke`, {});
        await loadProfileConsents();
        renderConsentList();
        renderProfiles();
        await loadProjectContent();
        showToast('프로필 공개 동의를 철회했습니다.');
    });
}

function openCharacterForm(trigger) {
    if (!requireProject()) return;
    editingCharacter = trigger.dataset.targetId
            ? characterById(Number(trigger.dataset.targetId)) : null;
    lookup('[data-character-form]').reset();
    document.getElementById('characterName').value = editingCharacter?.name || '';
    document.getElementById('characterDescription').value = editingCharacter?.description || '';
    document.getElementById('characterImportance').value = editingCharacter?.importance || 'LEAD';
    document.getElementById('characterOrder').value = editingCharacter?.displayOrder || 0;
    openSheet('characterSheet', trigger);
}

async function saveCharacter(trigger) {
    if (!lookup('[data-character-form]').reportValidity()) return;
    await withBusy(trigger, async () => {
        const body = {performanceProjectId: project().performanceProjectId,
            name: readValue('characterName'), description: readValue('characterDescription') || null,
            importance: readValue('characterImportance'),
            displayOrder: Number(readValue('characterOrder'))};
        if (editingCharacter) {
            await put(`/api/performance-content-management/characters/${editingCharacter.performanceCharacterId}`, body);
        } else {
            await post('/api/performance-content-management/characters', body);
        }
        closeSheetOf(trigger);
        await loadProjectContent();
        showToast('등장인물을 저장했습니다.');
    });
}

async function deleteCharacter(trigger) {
    await withBusy(trigger, async () => {
        await del(`/api/performance-content-management/characters/${trigger.dataset.targetId}`);
        await loadProjectContent();
        showToast('등장인물을 삭제했습니다.');
    });
}

function fillCastSelects(characterSelectId, profileSelectId) {
    setSelectItems(characterSelectId, characters, 'performanceCharacterId', (item) => item.name);
    setSelectItems(profileSelectId, availableProfiles(), 'publicProfileId', (item) => item.publicName);
}

function openCastForm(trigger) {
    if (!requireProject()) return;
    if (!characters.length || !availableProfiles().length) {
        showToast('등장인물과 공개 이름 동의가 완료된 게시 프로필이 필요합니다.');
        return;
    }
    editingCast = trigger.dataset.targetId
            ? casts.find((item) => item.performanceCastId === Number(trigger.dataset.targetId)) : null;
    lookup('[data-cast-form]').reset();
    fillCastSelects('castCharacter', 'castProfile');
    document.getElementById('castCharacter').disabled = Boolean(editingCast);
    document.getElementById('castCharacter').value = editingCast?.performanceCharacterId || characters[0].performanceCharacterId;
    document.getElementById('castProfile').value = editingCast?.publicProfileId || availableProfiles()[0].publicProfileId;
    document.getElementById('castType').value = editingCast?.castType || 'PRIMARY';
    document.getElementById('castOrder').value = editingCast?.displayOrder || 0;
    document.getElementById('castReason').value = '';
    openSheet('castSheet', trigger);
}

async function saveCast(trigger) {
    if (!lookup('[data-cast-form]').reportValidity()) return;
    await withBusy(trigger, async () => {
        const common = {publicProfileId: Number(readValue('castProfile')),
            castType: readValue('castType'), displayOrder: Number(readValue('castOrder')),
            reason: readValue('castReason') || null};
        if (editingCast) {
            await put(`/api/performance-content-management/casts/${editingCast.performanceCastId}`, common);
        } else {
            await post('/api/performance-content-management/casts', {
                performanceProjectId: project().performanceProjectId,
                performanceCharacterId: Number(readValue('castCharacter')), ...common,
            });
        }
        closeSheetOf(trigger);
        await loadProjectContent();
        showToast('작품 캐스팅을 저장했습니다.');
    });
}

async function deleteCast(trigger) {
    await withBusy(trigger, async () => {
        await del(`/api/performance-content-management/casts/${trigger.dataset.targetId}`);
        await loadProjectContent();
        showToast('작품 캐스팅을 해제했습니다.');
    });
}

async function openCastHistory(trigger) {
    if (!requireProject()) return;
    await withBusy(trigger, async () => {
        const histories = await get(`/api/performance-content-management/projects/${project().performanceProjectId}/cast-histories`);
        const list = lookup('[data-cast-history-list]');
        list.replaceChildren();
        if (!histories.length) list.appendChild(emptyState('캐스팅 변경 이력이 없습니다.'));
        histories.forEach((history) => {
            const character = characterById(history.performanceCharacterId);
            const previous = profileById(history.previousPublicProfileId);
            const next = profileById(history.newPublicProfileId);
            const row = element('article', 'py-3');
            row.append(element('p', 'text-sm font-bold',
                    `${HISTORY_SCOPE_LABELS[history.scope]} · ${character?.name || `배역 #${history.performanceCharacterId}`} · ${HISTORY_ACTION_LABELS[history.action]}`),
                    element('p', 'mt-1 text-xs text-muted-foreground',
                            `${previous?.publicName || '-'} → ${next?.publicName || '-'} · ${formatDateTime(history.changedDttm)}`));
            if (history.reason) row.appendChild(element('p', 'mt-1 text-xs', history.reason));
            list.appendChild(row);
        });
        openModal('castHistoryModal', trigger);
    });
}

function openRoundCastForm(trigger) {
    if (!requireProject()) return;
    if (!selectedRound()) {
        showToast('먼저 공연 회차를 등록해 주세요.');
        return;
    }
    if (!characters.length || !availableProfiles().length) {
        showToast('등장인물과 공개 이름 동의가 완료된 게시 프로필이 필요합니다.');
        return;
    }
    editingRoundCast = trigger.dataset.targetId
            ? roundCasts.find((item) => item.performanceRoundCastId === Number(trigger.dataset.targetId)) : null;
    lookup('[data-round-cast-form]').reset();
    fillCastSelects('roundCastCharacter', 'roundCastProfile');
    document.getElementById('roundCastCharacter').disabled = Boolean(editingRoundCast);
    document.getElementById('roundCastCharacter').value = editingRoundCast?.performanceCharacterId || characters[0].performanceCharacterId;
    document.getElementById('roundCastProfile').value = editingRoundCast?.publicProfileId || availableProfiles()[0].publicProfileId;
    document.getElementById('roundCastType').value = editingRoundCast?.castType || 'PRIMARY';
    document.getElementById('roundCastReason').value = '';
    openSheet('roundCastSheet', trigger);
}

async function saveRoundCast(trigger) {
    if (!lookup('[data-round-cast-form]').reportValidity()) return;
    await withBusy(trigger, async () => {
        const common = {publicProfileId: Number(readValue('roundCastProfile')),
            castType: readValue('roundCastType'), reason: readValue('roundCastReason') || null};
        if (editingRoundCast) {
            await put(`/api/performance-content-management/round-casts/${editingRoundCast.performanceRoundCastId}`, common);
        } else {
            await post('/api/performance-content-management/round-casts', {
                performanceProjectId: project().performanceProjectId,
                performanceRoundId: selectedRound().performanceRoundId,
                performanceCharacterId: Number(readValue('roundCastCharacter')), ...common,
            });
        }
        closeSheetOf(trigger);
        await loadRoundCasts();
        showToast('회차별 캐스팅을 저장했습니다.');
    });
}

async function deleteRoundCast(trigger) {
    await withBusy(trigger, async () => {
        await del(`/api/performance-content-management/round-casts/${trigger.dataset.targetId}`);
        await loadRoundCasts();
        showToast('회차별 캐스팅을 해제했습니다.');
    });
}

function fillCreditProfiles() {
    const select = document.getElementById('creditProfile');
    select.replaceChildren();
    appendOption(select, '', '연결하지 않음');
    availableProfiles().forEach((profile) =>
        appendOption(select, profile.publicProfileId, profile.publicName));
}

function openCreditForm(trigger) {
    if (!requireProject()) return;
    editingCredit = trigger.dataset.targetId
            ? credits.find((item) => item.productionCreditId === Number(trigger.dataset.targetId)) : null;
    lookup('[data-credit-form]').reset();
    fillCreditProfiles();
    document.getElementById('creditRole').value = editingCredit?.creditRole || '';
    document.getElementById('creditName').value = editingCredit?.publicName || '';
    document.getElementById('creditProfile').value = editingCredit?.publicProfileId || '';
    document.getElementById('creditOrder').value = editingCredit?.displayOrder || 0;
    openSheet('creditSheet', trigger);
}

async function saveCredit(trigger) {
    if (!lookup('[data-credit-form]').reportValidity()) return;
    await withBusy(trigger, async () => {
        const body = {performanceProjectId: project().performanceProjectId,
            creditRole: readValue('creditRole'), publicName: readValue('creditName'),
            publicProfileId: Number(readValue('creditProfile')) || null,
            displayOrder: Number(readValue('creditOrder'))};
        if (editingCredit) {
            await put(`/api/performance-content-management/credits/${editingCredit.productionCreditId}`, body);
        } else {
            await post('/api/performance-content-management/credits', body);
        }
        closeSheetOf(trigger);
        await loadProjectContent();
        showToast('제작진 크레딧을 저장했습니다.');
    });
}

async function deleteCredit(trigger) {
    await withBusy(trigger, async () => {
        await del(`/api/performance-content-management/credits/${trigger.dataset.targetId}`);
        await loadProjectContent();
        showToast('제작진 크레딧을 삭제했습니다.');
    });
}

function openMediaForm(trigger) {
    if (!requireProject()) return;
    editingMedia = trigger.dataset.targetId
            ? mediaItems.find((item) => item.performanceMediaId === Number(trigger.dataset.targetId)) : null;
    lookup('[data-media-form]').reset();
    document.getElementById('mediaType').value = editingMedia?.mediaType || 'POSTER';
    document.getElementById('mediaFile').value = '';
    document.getElementById('mediaFile').required = !editingMedia;
    document.getElementById('mediaTitle').value = editingMedia?.title || '';
    document.getElementById('mediaDescription').value = editingMedia?.description || '';
    document.getElementById('mediaAlt').value = editingMedia?.altText || '';
    document.getElementById('mediaCredit').value = editingMedia?.creditText || '';
    document.getElementById('mediaExternalUrl').value = editingMedia?.externalUrl || '';
    document.getElementById('mediaOrder').value = editingMedia?.displayOrder || 0;
    openSheet('mediaSheet', trigger);
}

async function saveMedia(trigger) {
    if (!lookup('[data-media-form]').reportValidity()) return;
    await withBusy(trigger, async () => {
        const type = readValue('mediaType');
        const file = document.getElementById('mediaFile').files[0];
        if (file && type !== 'VIDEO' && !file.type.startsWith('image/')) {
            throw new Error('영상 외 미디어 유형에는 이미지 파일만 등록할 수 있습니다.');
        }
        const storedFileId = await uploadPublicFile(file)
                || editingMedia?.storedFileId;
        if (!storedFileId) throw new Error('미디어 파일을 선택해 주세요.');
        const body = {performanceProjectId: project().performanceProjectId,
            storedFileId, mediaType: type, title: readValue('mediaTitle'),
            description: readValue('mediaDescription'), altText: readValue('mediaAlt'),
            creditText: readValue('mediaCredit'),
            externalUrl: readValue('mediaExternalUrl') || null,
            displayOrder: Number(readValue('mediaOrder'))};
        if (editingMedia) {
            await put(`/api/performance-content-management/media/${editingMedia.performanceMediaId}`, body);
        } else {
            await post('/api/performance-content-management/media', body);
        }
        closeSheetOf(trigger);
        await loadProjectContent();
        showToast('공연 미디어를 저장했습니다.');
    });
}

async function changeMediaPublished(trigger) {
    await withBusy(trigger, async () => {
        await patch(`/api/performance-content-management/media/${trigger.dataset.targetId}/published`,
                {published: trigger.dataset.published === 'true'});
        await loadProjectContent();
        showToast('미디어 게시 상태를 변경했습니다.');
    });
}

async function deleteMedia(trigger) {
    await withBusy(trigger, async () => {
        await del(`/api/performance-content-management/media/${trigger.dataset.targetId}`);
        await loadProjectContent();
        showToast('공연 미디어를 삭제했습니다.');
    });
}

function focusContentItem(selector) {
    const item = lookup(selector);
    if (!item) {
        return;
    }
    item.tabIndex = -1;
    item.focus({preventScroll: true});
    item.scrollIntoView({behavior: 'smooth', block: 'center'});
}

async function handleNextAction(trigger) {
    const action = trigger.dataset.nextAction;
    const tabName = trigger.dataset.nextTab;
    if (tabName) {
        selectContentTab(tabName);
    }
    if (action === 'project') {
        window.location.href = '/performance-management';
        return;
    }
    if (action === 'public-page') {
        window.location.href = '/performance-management#public';
        return;
    }
    if (action === 'retry') {
        trigger.disabled = true;
        try {
            await loadAll();
        } finally {
            trigger.disabled = false;
        }
        return;
    }
    if (action === 'review-profile') {
        focusContentItem(`[data-profile-id="${trigger.dataset.targetId}"]`);
        return;
    }
    if (action === 'review-media') {
        focusContentItem(`[data-media-id="${trigger.dataset.targetId}"]`);
        return;
    }
    if (action === ACTIONS.PROFILE_CREATE) {
        openProfileForm(trigger);
    } else if (action === ACTIONS.CONSENT_OPEN) {
        openConsent(trigger);
    } else if (action === ACTIONS.CHARACTER_CREATE) {
        openCharacterForm(trigger);
    } else if (action === ACTIONS.CAST_CREATE) {
        openCastForm(trigger);
    } else if (action === ACTIONS.ROUND_CAST_CREATE) {
        const roundId = trigger.dataset.targetId;
        delete trigger.dataset.targetId;
        document.getElementById('roundCastRoundSelect').value = roundId;
        await loadRoundCasts();
        openRoundCastForm(trigger);
        trigger.dataset.targetId = roundId;
    } else if (action === ACTIONS.CREDIT_CREATE) {
        openCreditForm(trigger);
    } else if (action === ACTIONS.MEDIA_CREATE) {
        openMediaForm(trigger);
    }
}

function showLoadError(error) {
    setNextAction('retry', '공연 콘텐츠를 불러오지 못했어요',
            `${error.message || '잠시 후 다시 시도해 주세요.'} 입력한 내용은 없어서 잃어버린 정보는 없어요.`,
            '다시 불러오기');
    announce('공연 콘텐츠를 불러오지 못했어요.');
}

all('[data-content-tab]').forEach((tab) => {
    tab.addEventListener('click', () => selectContentTab(tab.dataset.contentTab));
    tab.addEventListener('keydown', (event) => {
        if (!['ArrowLeft', 'ArrowRight', 'Home', 'End'].includes(event.key)) {
            return;
        }
        event.preventDefault();
        const tabs = all('[data-content-tab]');
        const current = tabs.indexOf(tab);
        let next = current;
        if (event.key === 'ArrowLeft') {
            next = (current - 1 + tabs.length) % tabs.length;
        } else if (event.key === 'ArrowRight') {
            next = (current + 1) % tabs.length;
        } else if (event.key === 'Home') {
            next = 0;
        } else if (event.key === 'End') {
            next = tabs.length - 1;
        }
        selectContentTab(tabs[next].dataset.contentTab, {focus: true});
    });
});
lookup('[data-content-next-action]').addEventListener('click', (event) => {
    handleNextAction(event.currentTarget).catch(showLoadError);
});
document.getElementById('contentProjectSelect').addEventListener('change', () => {
    lookup('[data-content-project-state]').textContent = project()
            ? `${project().title}의 공개 콘텐츠를 편집하고 있어요.`
            : '등록된 공연 프로젝트가 없어요.';
    loadProjectContent().catch(showLoadError);
});
document.getElementById('roundCastRoundSelect').addEventListener('change', () =>
    loadRoundCasts().catch(showLoadError));
document.getElementById('consentScope').addEventListener('change',
        updateConsentPolicyOptions);
document.getElementById('policyDocument').addEventListener('change', togglePolicyTitle);
bindPageActions({
    [ACTIONS.PROFILE_CREATE]: openProfileForm, [ACTIONS.PROFILE_EDIT]: openProfileForm,
    [ACTIONS.PROFILE_SAVE]: saveProfile, [ACTIONS.PROFILE_VISIBILITY]: changeProfileVisibility,
    [ACTIONS.CONSENT_OPEN]: openConsent, [ACTIONS.CONSENT_SAVE]: saveConsent,
    [ACTIONS.CONSENT_REVOKE]: revokeConsent,
    [ACTIONS.POLICY_OPEN]: openPolicyForm, [ACTIONS.POLICY_SAVE]: savePolicy,
    [ACTIONS.CHARACTER_CREATE]: openCharacterForm, [ACTIONS.CHARACTER_EDIT]: openCharacterForm,
    [ACTIONS.CHARACTER_SAVE]: saveCharacter, [ACTIONS.CHARACTER_DELETE]: deleteCharacter,
    [ACTIONS.CAST_CREATE]: openCastForm, [ACTIONS.CAST_EDIT]: openCastForm,
    [ACTIONS.CAST_SAVE]: saveCast, [ACTIONS.CAST_DELETE]: deleteCast,
    [ACTIONS.CAST_HISTORY]: openCastHistory,
    [ACTIONS.ROUND_CAST_CREATE]: openRoundCastForm, [ACTIONS.ROUND_CAST_EDIT]: openRoundCastForm,
    [ACTIONS.ROUND_CAST_SAVE]: saveRoundCast, [ACTIONS.ROUND_CAST_DELETE]: deleteRoundCast,
    [ACTIONS.CREDIT_CREATE]: openCreditForm, [ACTIONS.CREDIT_EDIT]: openCreditForm,
    [ACTIONS.CREDIT_SAVE]: saveCredit, [ACTIONS.CREDIT_DELETE]: deleteCredit,
    [ACTIONS.MEDIA_CREATE]: openMediaForm, [ACTIONS.MEDIA_EDIT]: openMediaForm,
    [ACTIONS.MEDIA_SAVE]: saveMedia, [ACTIONS.MEDIA_PUBLISHED]: changeMediaPublished,
    [ACTIONS.MEDIA_DELETE]: deleteMedia,
});
window.addEventListener('hashchange', () => {
    selectContentTab(window.location.hash.slice(1), {updateHash: false});
});
selectContentTab(window.location.hash.slice(1), {updateHash: false});
loadAll().catch(showLoadError);
