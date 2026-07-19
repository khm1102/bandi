import {all, bindPageActions, lookup, readValue} from '../common/dom.js';

const ACTIONS = Object.freeze({SUBMIT: 'auth-submit'});
const authForm = lookup('[data-auth-form]');

function showAuthError(message, fieldId) {
    const error = lookup('[data-auth-error]');
    const field = document.getElementById(fieldId);
    all('[aria-describedby~="authError"]').forEach((candidate) => {
        candidate.removeAttribute('aria-invalid');
    });
    error.textContent = message;
    error.classList.remove('hidden');
    field.setAttribute('aria-invalid', 'true');
    field.focus();
}

function previewLogin() {
    if (!readValue('schoolId')) {
        showAuthError('학교 포털 아이디를 입력해 주세요.', 'schoolId');
        return;
    }
    if (!readValue('schoolPassword')) {
        showAuthError('학교 포털 비밀번호를 입력해 주세요.', 'schoolPassword');
        return;
    }
    window.location.assign(`${authForm.dataset.dashboardUrl}?role=member`);
}

bindPageActions({[ACTIONS.SUBMIT]: previewLogin});

authForm.addEventListener('keydown', (event) => {
    if (event.key !== 'Enter') {
        return;
    }
    event.preventDefault();
    previewLogin();
});

authForm.addEventListener('input', (event) => {
    const field = event.target.closest('[aria-describedby~="authError"]');
    if (!field) {
        return;
    }
    field.removeAttribute('aria-invalid');
    const error = lookup('[data-auth-error]');
    error.classList.add('hidden');
    error.textContent = '';
});
