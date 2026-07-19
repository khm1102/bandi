import {all, lookup, readValue} from '../common/dom.js';

const authForm = lookup('#schoolLoginForm');
const passwordToggle = lookup('[data-password-toggle]');

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
        showAuthError('학번을 입력해 주세요.', 'studentNo');
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
            return;
        }
        authForm.setAttribute('aria-busy', 'true');
        const submit = event.submitter || authForm.querySelector('button[type="submit"]');
        if (submit) {
            submit.disabled = true;
            submit.textContent = '학교 계정 확인 중…';
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

if (passwordToggle) {
    passwordToggle.addEventListener('click', () => {
        const password = document.getElementById('password');
        const visible = password.type === 'text';
        password.type = visible ? 'password' : 'text';
        passwordToggle.textContent = visible ? '보기' : '숨기기';
        passwordToggle.setAttribute('aria-pressed', String(!visible));
        password.focus();
    });
}

window.addEventListener('pageshow', () => {
    if (!authForm) {
        return;
    }
    authForm.removeAttribute('aria-busy');
    const submit = authForm.querySelector('button[type="submit"]');
    if (submit) {
        submit.disabled = false;
        submit.textContent = '학교 계정 확인하고 로그인';
    }
});
