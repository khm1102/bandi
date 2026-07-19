import {ApiError, get} from './api.js';

const ROLE_LABELS = {
    ADMIN: '운영진',
    LEADER: '팀장',
    MEMBER: '일반 부원',
};

function lookupProfilePart(selector) {
    return document.querySelector(`[data-session-${selector}]`);
}

function firstCharacter(name) {
    return Array.from(name || '?')[0] || '?';
}

function memberMeta(member) {
    const roleLabel = ROLE_LABELS[member.role] || '멤버';
    return member.department ? `${roleLabel} · ${member.department}`
        : roleLabel;
}

function renderMember(member) {
    lookupProfilePart('initial').textContent = firstCharacter(member.name);
    lookupProfilePart('name').textContent = member.name;
    lookupProfilePart('meta').textContent = memberMeta(member);
}

function renderError() {
    lookupProfilePart('initial').textContent = '!';
    lookupProfilePart('name').textContent = '사용자 정보 확인 필요';
    lookupProfilePart('meta').textContent = '새로고침 후 다시 확인해 주세요';
}

async function loadLoginMember() {
    const profile = document.querySelector('[data-session-profile]');
    if (!profile) {
        return;
    }
    try {
        renderMember(await get('/api/members/me'));
    } catch (error) {
        if (error instanceof ApiError && error.status === 401) {
            window.location.replace('/login');
            return;
        }
        renderError();
    } finally {
        profile.setAttribute('aria-busy', 'false');
    }
}

loadLoginMember();
