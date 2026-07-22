import {all, lookup, readValue} from '../common/dom.js';

const authForm = lookup('#schoolLoginForm');

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

function validateLogin() {
    if (!readValue('studentNo')) {
        showAuthError('학교 포털 아이디를 입력해 주세요.', 'studentNo');
        return false;
    }
    if (!readValue('password')) {
        showAuthError('학교 포털 비밀번호를 입력해 주세요.', 'password');
        return false;
    }
    return true;
}

if (authForm) {
    authForm.addEventListener('submit', (event) => {
        if (!validateLogin()) {
            event.preventDefault();
        }
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
}
