import {ApiError, del, get, patch, put} from '../common/api.js';
import {lookup, readValue} from '../common/dom.js';
import {showToast} from '../common/toast.js';

const ROLE_LABELS = Object.freeze({
    ADMIN: '운영진',
    LEADER: '팀장',
    MEMBER: '일반 부원',
});
const SSO_LABELS = Object.freeze({
    WAITING: '연결 대기',
    LINKED: '연결 완료',
    REVIEW_REQUIRED: '확인 필요',
});
const ACADEMIC_STATUS_LABELS = Object.freeze({
    ENROLLED: '재학생',
    LEAVE_OF_ABSENCE: '휴학생',
    GRADUATED: '졸업생',
    UNKNOWN: '확인 필요',
});

let profile = null;
let teams = [];

function firstCharacter(name) {
    return Array.from(name || '?')[0] || '?';
}

function messageFrom(error) {
    if (error instanceof ApiError && error.fieldErrors.length > 0) {
        return error.fieldErrors[0].reason;
    }
    return error.message || '요청을 처리하지 못했습니다.';
}

function formatDateTime(value) {
    if (!value) {
        return '확인 기록이 없어요';
    }
    return new Intl.DateTimeFormat('ko-KR', {
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit', hour12: false,
    }).format(new Date(value));
}

function setText(selector, value) {
    lookup(selector).textContent = value || '—';
}

function renderPhoto(nextProfile) {
    const image = lookup('[data-profile-photo]');
    const initial = lookup('[data-profile-initial]');
    initial.textContent = firstCharacter(nextProfile.name);
    image.classList.add('hidden');
    initial.classList.remove('hidden');
    image.removeAttribute('src');
    lookup('[data-profile-photo-delete]').classList.toggle('hidden',
            !nextProfile.hasProfilePhoto);
    if (!nextProfile.hasProfilePhoto) {
        return;
    }
    image.src = `/api/members/${nextProfile.memberId}/profile-photo`;
    image.onload = () => {
        image.classList.remove('hidden');
        initial.classList.add('hidden');
    };
    image.onerror = () => {
        image.removeAttribute('src');
        image.classList.add('hidden');
        initial.classList.remove('hidden');
    };
}

function updateTeamSubmitState() {
    const submit = lookup('[data-profile-team-submit]');
    const selectedTeamId = Number(readValue('profileTeam'));
    const reason = readValue('profileTeamReason');
    submit.disabled = selectedTeamId === profile.teamId || !reason;
}

function renderTeamOptions() {
    const select = lookup('[data-profile-team-select]');
    select.replaceChildren();
    teams.forEach((team) => {
        const option = document.createElement('option');
        option.value = String(team.teamId);
        option.textContent = team.name;
        option.selected = team.teamId === profile.teamId;
        select.appendChild(option);
    });
    updateTeamSubmitState();
}

function render(nextProfile) {
    profile = nextProfile;
    renderPhoto(profile);
    setText('[data-profile-name]', profile.name);
    setText('[data-profile-student-no]', profile.studentNo);
    setText('[data-profile-cohort]', profile.cohortName);
    setText('[data-profile-team]', profile.teamName);
    setText('[data-profile-role]', ROLE_LABELS[profile.role] || profile.role);
    setText('[data-profile-department]', profile.department);
    setText('[data-profile-phone-number]', profile.phoneNumber);
    setText('[data-profile-academic-status]',
            ACADEMIC_STATUS_LABELS[profile.academicStatus] || profile.academicStatus);
    setText('[data-profile-sso-status]', SSO_LABELS[profile.ssoLinkStatus]
            || profile.ssoLinkStatus);
    setText('[data-profile-verified-at]', formatDateTime(profile.academicStatusVerifiedDttm));
    setText('[data-profile-login-at]', formatDateTime(profile.lastLoginDttm));
    renderTeamOptions();
}

async function loadProfile() {
    const root = lookup('[data-profile-root]');
    try {
        const [nextProfile, nextTeams] = await Promise.all([
            get('/api/members/me/profile'),
            get('/api/members/reference/teams'),
        ]);
        teams = nextTeams;
        render(nextProfile);
    } catch (error) {
        lookup('[data-profile-photo-message]').textContent =
                `프로필을 불러오지 못했어요. ${messageFrom(error)}`;
    } finally {
        root.setAttribute('aria-busy', 'false');
    }
}

async function uploadPhoto(event) {
    const file = event.target.files?.[0];
    if (!file) {
        return;
    }
    const message = lookup('[data-profile-photo-message]');
    const formData = new FormData();
    formData.append('file', file);
    event.target.disabled = true;
    message.textContent = '사진을 저장하고 있어요.';
    try {
        render(await put('/api/members/me/profile-photo', formData));
        message.textContent = '프로필 사진을 바꿨어요. 이전 사진은 삭제 처리 중이에요.';
        showToast('프로필 사진을 바꿨어요.');
    } catch (error) {
        message.textContent = messageFrom(error);
    } finally {
        event.target.value = '';
        event.target.disabled = false;
    }
}

async function deletePhoto() {
    const button = lookup('[data-profile-photo-delete]');
    button.disabled = true;
    try {
        await del('/api/members/me/profile-photo');
        profile = {...profile, hasProfilePhoto: false};
        renderPhoto(profile);
        lookup('[data-profile-photo-message]').textContent =
                '기본 아바타로 바꿨어요. 이전 사진은 삭제 처리 중이에요.';
        showToast('기본 아바타로 바꿨어요.');
    } catch (error) {
        lookup('[data-profile-photo-message]').textContent = messageFrom(error);
    } finally {
        button.disabled = false;
    }
}

async function changeTeam(event) {
    event.preventDefault();
    const error = lookup('[data-profile-team-error]');
    const button = lookup('[data-profile-team-submit]');
    const newTeamId = Number(readValue('profileTeam'));
    const reason = readValue('profileTeamReason');
    if (!reason) {
        error.textContent = '변경 사유를 입력해 주세요.';
        error.classList.remove('hidden');
        return;
    }
    error.classList.add('hidden');
    button.disabled = true;
    try {
        await patch(`/api/members/${profile.memberId}/team`, {newTeamId, reason});
        lookup('[data-profile-team-reason]').value = '';
        await loadProfile();
        showToast('소속 팀을 변경했어요.');
    } catch (requestError) {
        error.textContent = messageFrom(requestError);
        error.classList.remove('hidden');
        updateTeamSubmitState();
    }
}

lookup('[data-profile-photo-input]').addEventListener('change', uploadPhoto);
lookup('[data-profile-photo-delete]').addEventListener('click', deletePhoto);
lookup('[data-profile-team-form]').addEventListener('submit', changeTeam);
lookup('[data-profile-team-select]').addEventListener('change', updateTeamSubmitState);
lookup('[data-profile-team-reason]').addEventListener('input', updateTeamSubmitState);

loadProfile();
