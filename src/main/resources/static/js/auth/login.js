import {all, lookup, readValue} from '../common/dom.js';

const MOCK_ACCOUNTS = {
    admin: {password: '1234', team: '운영진', role: 'admin'},
    leader: {password: '1234', team: '무대', role: 'leader'},
    member: {password: '1234', team: '배우연출', role: 'member'}
};
const authForm = lookup('[data-auth-form]');

function redirectToDashboard(role) {
    window.location.assign(`${authForm.dataset.dashboardUrl}?role=${role}`);
}

function showAuthError(message, fieldId) {
    const error = lookup('[data-auth-error]');
    const field = document.getElementById(fieldId);
    all('[aria-describedby~="authError"]').forEach((candidate) => {
        candidate.removeAttribute('aria-invalid');
    });
    error.textContent = message;
    error.classList.remove('hidden');
    if (field) {
        field.setAttribute('aria-invalid', 'true');
        field.focus();
    }
}

function login() {
    const loginId = readValue('loginId');
    const password = readValue('loginPw');
    const team = readValue('loginTeam');
    const account = MOCK_ACCOUNTS[loginId];
    if (!account || account.password !== password) {
        showAuthError('아이디 또는 비밀번호를 확인해 주세요.', 'loginId');
        return;
    }
    if (account.role !== 'admin' && account.team !== team) {
        showAuthError('가입할 때 선택한 팀과 일치하지 않습니다.', 'loginTeam');
        return;
    }
    redirectToDashboard(account.role);
}

function signup() {
    const name = readValue('joinName');
    const loginId = readValue('joinId');
    const password = readValue('joinPw');
    const passwordConfirm = readValue('joinPw2');
    const code = readValue('joinCode').toUpperCase();
    const requiredFields = [
        ['joinName', name],
        ['joinId', loginId],
        ['joinPw', password],
        ['joinPw2', passwordConfirm],
        ['joinCode', code]
    ];
    const firstMissingField = requiredFields.find(([, value]) => !value);
    if (firstMissingField) {
        showAuthError('필수 항목을 모두 입력해 주세요.', firstMissingField[0]);
        return;
    }
    if (password !== passwordConfirm) {
        showAuthError('비밀번호와 비밀번호 확인이 일치하지 않습니다.', 'joinPw2');
        return;
    }
    if (!['BANDI-261-A7K2', 'BANDI-262-M9Q4'].includes(code)) {
        showAuthError('사용할 수 없는 초대코드입니다.', 'joinCode');
        return;
    }
    redirectToDashboard('member');
}

authForm.addEventListener('submit', (event) => {
    event.preventDefault();
    if (authForm.dataset.authMode === 'login') {
        login();
        return;
    }
    signup();
});

document.addEventListener('input', (event) => {
    const field = event.target.closest('[aria-describedby~="authError"]');
    if (!field) {
        return;
    }
    field.removeAttribute('aria-invalid');
    const error = lookup('[data-auth-error]');
    error.classList.add('hidden');
    error.textContent = '';
});
