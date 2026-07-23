import {get, getBlob, postBlob} from '../common/api.js';
import {
    initializeDateTimeFields,
    readDateTimeValue,
    setDateTimeValue,
} from '../common/date-time-field.js';

const MAX_PARTICIPANTS = 14;
const MAX_PHOTO_BYTES = 10 * 1024 * 1024;
const form = document.querySelector('[data-activity-report-form]');
const participantList = document.querySelector('[data-participant-list]');
const participantTemplate = document.querySelector('[data-participant-template]');
const participantEmpty = document.querySelector('[data-participant-empty]');
const participantCount = document.querySelector('[data-participant-count]');
const participantSearch = document.getElementById('participantSearch');
const suggestions = document.querySelector('[data-participant-suggestions]');
const searchError = document.querySelector('[data-participant-search-error]');
const photoInput = document.getElementById('activityReportPhoto');
const photoDropzone = document.querySelector('[data-photo-dropzone]');
const photoPreview = document.querySelector('[data-photo-preview]');
const photoName = document.querySelector('[data-photo-name]');
const photoActions = document.querySelector('[data-photo-actions]');
const contentInput = document.getElementById('content');
const contentCount = document.querySelector('[data-content-count]');
const formError = document.querySelector('[data-form-error]');
const successState = document.querySelector('[data-success-state]');

let photo = null;
let photoPreviewUrl = '';
let lastDownload = null;
let lastDownloadFilename = '';
let dirty = false;
let searchTimer = 0;
let searchGeneration = 0;
let activeSuggestionIndex = -1;

function show(element, visible, display = 'block') {
    if (!element) {
        return;
    }
    element.classList.toggle('hidden', !visible);
    if (visible && display === 'flex') {
        element.classList.add('flex');
    } else if (!visible) {
        element.classList.remove('flex');
    }
}

function setFieldError(field, message = '') {
    const error = document.querySelector(`[data-field-error="${field}"]`);
    if (!error) {
        return;
    }
    error.textContent = message;
    show(error, Boolean(message));
    const input = document.getElementById(field);
    if (input) {
        input.setAttribute('aria-invalid', message ? 'true' : 'false');
    }
}

function clearErrors() {
    document.querySelectorAll('[data-field-error]').forEach((error) => {
        error.textContent = '';
        error.classList.add('hidden');
    });
    form.querySelectorAll('[aria-invalid="true"]').forEach((input) => {
        input.setAttribute('aria-invalid', 'false');
    });
    formError.textContent = '';
    show(formError, false);
}

function updateParticipantState() {
    const count = participantList.querySelectorAll('[data-participant-row]').length;
    participantCount.textContent = `${count} / ${MAX_PARTICIPANTS}명`;
    show(participantEmpty, count === 0);
}

function addParticipant(values = {}) {
    const count = participantList.querySelectorAll('[data-participant-row]').length;
    if (count >= MAX_PARTICIPANTS) {
        setFieldError('participants', '참여 인원은 최대 14명까지 입력할 수 있습니다.');
        return;
    }
    const row = participantTemplate.content.firstElementChild.cloneNode(true);
    row.querySelectorAll('[data-participant-field]').forEach((input) => {
        input.value = values[input.dataset.participantField] || '';
    });
    participantList.appendChild(row);
    setFieldError('participants');
    updateParticipantState();
    dirty = true;
    row.querySelector('[data-participant-field="name"]')?.focus();
}

function removeParticipant(button) {
    button.closest('[data-participant-row]')?.remove();
    updateParticipantState();
    dirty = true;
}

function collectParticipants() {
    return Array.from(participantList.querySelectorAll('[data-participant-row]'))
            .map((row) => {
                const value = (name) => row.querySelector(
                        `[data-participant-field="${name}"]`).value.trim();
                return {
                    name: value('name'),
                    department: value('department') || null,
                    studentNo: value('studentNo') || null,
                    note: value('note') || null,
                };
            });
}

function validatePhoto(file) {
    if (!file) {
        return '활동 사진을 선택해 주세요.';
    }
    if (!['image/jpeg', 'image/png'].includes(file.type)) {
        return 'JPG 또는 PNG 사진만 사용할 수 있습니다.';
    }
    if (file.size > MAX_PHOTO_BYTES) {
        return '사진은 10MiB 이하만 사용할 수 있습니다.';
    }
    return '';
}

function revokePhotoPreview() {
    if (photoPreviewUrl) {
        URL.revokeObjectURL(photoPreviewUrl);
        photoPreviewUrl = '';
    }
}

function setPhoto(file) {
    const message = validatePhoto(file);
    if (message) {
        setFieldError('photo', message);
        return;
    }
    revokePhotoPreview();
    photo = file;
    photoPreviewUrl = URL.createObjectURL(file);
    photoPreview.src = photoPreviewUrl;
    photoName.textContent = file.name;
    show(photoPreview, true);
    show(photoActions, true, 'flex');
    setFieldError('photo');
    dirty = true;
}

function clearPhoto() {
    revokePhotoPreview();
    photo = null;
    photoInput.value = '';
    photoPreview.removeAttribute('src');
    photoName.textContent = '';
    show(photoPreview, false);
    show(photoActions, false);
    dirty = true;
}

function renderSuggestions(items) {
    suggestions.replaceChildren();
    activeSuggestionIndex = -1;
    participantSearch.removeAttribute('aria-activedescendant');
    items.forEach((item, index) => {
        const button = document.createElement('button');
        button.type = 'button';
        button.id = `participantSuggestion${index}`;
        button.className = 'flex min-h-11 w-full items-center gap-3 rounded px-3 py-2 text-left hover:bg-secondary focus-visible:ring-2 focus-visible:ring-ring';
        button.dataset.suggestionIndex = String(index);
        button.setAttribute('role', 'option');
        const main = document.createElement('span');
        main.className = 'min-w-0 flex-1';
        const name = document.createElement('b');
        name.className = 'block text-sm';
        name.textContent = item.name;
        const meta = document.createElement('span');
        meta.className = 'block truncate text-xs text-muted-foreground';
        meta.textContent = [item.department, item.studentNo].filter(Boolean).join(' · ');
        main.append(name, meta);
        button.append(main);
        button.addEventListener('click', () => selectSuggestion(item));
        suggestions.appendChild(button);
    });
    show(suggestions, items.length > 0);
    participantSearch.setAttribute('aria-expanded', items.length > 0 ? 'true' : 'false');
}

function selectSuggestion(item) {
    addParticipant(item);
    participantSearch.value = '';
    renderSuggestions([]);
    participantSearch.focus();
}

async function searchParticipants() {
    const query = participantSearch.value.trim();
    searchGeneration += 1;
    const generation = searchGeneration;
    if (query.length < 2) {
        renderSuggestions([]);
        show(searchError, false);
        return;
    }
    try {
        const items = await get('/api/activity-report-documents/participants', {q: query});
        if (generation !== searchGeneration) {
            return;
        }
        renderSuggestions(items);
        searchError.textContent = items.length === 0
            ? '일치하는 활성 멤버가 없습니다. 직접 입력으로 추가할 수 있습니다.' : '';
        show(searchError, Boolean(searchError.textContent));
    } catch (error) {
        if (generation !== searchGeneration) {
            return;
        }
        renderSuggestions([]);
        searchError.textContent = '멤버를 검색하지 못했습니다. 직접 입력은 계속 사용할 수 있습니다.';
        show(searchError, true);
    }
}

function handleSearchKeydown(event) {
    const options = Array.from(suggestions.querySelectorAll('[role="option"]'));
    if (event.key === 'Escape') {
        renderSuggestions([]);
        return;
    }
    if (!options.length || !['ArrowDown', 'ArrowUp', 'Enter'].includes(event.key)) {
        return;
    }
    event.preventDefault();
    if (event.key === 'ArrowDown') {
        activeSuggestionIndex = (activeSuggestionIndex + 1) % options.length;
    } else if (event.key === 'ArrowUp') {
        activeSuggestionIndex = (activeSuggestionIndex - 1 + options.length) % options.length;
    } else if (activeSuggestionIndex >= 0) {
        options[activeSuggestionIndex].click();
        return;
    }
    options.forEach((option, index) => option.setAttribute('aria-selected',
            index === activeSuggestionIndex ? 'true' : 'false'));
    participantSearch.setAttribute('aria-activedescendant',
            options[activeSuggestionIndex].id);
    options[activeSuggestionIndex]?.scrollIntoView({block: 'nearest'});
}

function validateForm() {
    clearErrors();
    const representative = document.getElementById('representative').value.trim();
    const location = document.getElementById('location').value.trim();
    const activityAt = readDateTimeValue('activityAt');
    const content = contentInput.value.trim();
    const participants = collectParticipants();
    const errors = [];
    if (!representative) {
        errors.push(['representative', '대표자를 입력해 주세요.']);
    }
    if (!location) {
        errors.push(['location', '활동 장소를 입력해 주세요.']);
    }
    if (!activityAt) {
        errors.push(['activityAt', '활동 일시를 선택해 주세요.']);
    }
    if (!content) {
        errors.push(['content', '활동 내용을 입력해 주세요.']);
    }
    if (!participants.length) {
        errors.push(['participants', '참여 인원을 한 명 이상 추가해 주세요.']);
    } else if (participants.some((participant) => !participant.name)) {
        errors.push(['participants', '모든 참여자의 이름을 입력해 주세요.']);
    }
    const photoError = validatePhoto(photo);
    if (photoError) {
        errors.push(['photo', photoError]);
    }
    errors.forEach(([field, message]) => setFieldError(field, message));
    if (errors.length) {
        const firstField = errors[0][0];
        const target = firstField === 'photo' ? photoDropzone
            : firstField === 'participants' ? participantSearch
                : document.getElementById(firstField);
        target?.scrollIntoView({behavior: 'smooth', block: 'center'});
        target?.focus();
        return null;
    }
    return {representative, location, activityAt, content, participants};
}

function filenameOrFallback(result, fallback) {
    return result.filename || fallback;
}

function downloadResult(result, fallbackName) {
    const url = URL.createObjectURL(result.blob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = filenameOrFallback(result, fallbackName);
    document.body.appendChild(anchor);
    anchor.click();
    anchor.remove();
    window.setTimeout(() => URL.revokeObjectURL(url), 1000);
}

function setGenerating(generating) {
    form.querySelectorAll('button, input, textarea').forEach((control) => {
        control.disabled = generating;
    });
    const button = form.querySelector('[data-page-action="generate"]');
    if (button) {
        button.textContent = generating ? 'HWPX 만드는 중…' : 'HWPX 내역서 만들기';
    }
}

async function generateDocument() {
    if (form.dataset.presidentConfigured !== 'true') {
        formError.textContent = '현재 회장이 등록되지 않아 문서를 만들 수 없습니다.';
        show(formError, true);
        return;
    }
    const request = validateForm();
    if (!request) {
        return;
    }
    const body = new FormData();
    body.append('request', new Blob([JSON.stringify(request)], {
        type: 'application/json',
    }));
    body.append('photo', photo, photo.name);
    setGenerating(true);
    try {
        const result = await postBlob('/api/activity-report-documents', body);
        lastDownload = result.blob;
        lastDownloadFilename = filenameOrFallback(result,
                `${request.activityAt.slice(0, 10)}_반디_동아리_활동_내역서.hwpx`);
        downloadResult(result, lastDownloadFilename);
        show(successState, true);
        successState.scrollIntoView({behavior: 'smooth', block: 'nearest'});
    } catch (error) {
        error.fieldErrors?.forEach((fieldError) => {
            const field = fieldError.field?.startsWith('participants')
                ? 'participants' : fieldError.field;
            if (field) {
                setFieldError(field, fieldError.message);
            }
        });
        formError.textContent = error.message || '활동 내역서를 만들지 못했습니다. 입력 내용은 그대로 유지됩니다.';
        show(formError, true);
    } finally {
        setGenerating(false);
    }
}

async function downloadBlank() {
    try {
        const result = await getBlob('/api/activity-report-documents/blank');
        downloadResult(result, '반디_동아리_활동_내역서_빈_양식.hwpx');
    } catch (error) {
        formError.textContent = error.message || '빈 양식을 내려받지 못했습니다.';
        show(formError, true);
        formError.scrollIntoView({block: 'center'});
    }
}

function resetForm() {
    form.reset();
    participantList.replaceChildren();
    clearPhoto();
    setDateTimeValue('activityAt', localDateTimeValue());
    contentCount.textContent = '0 / 300자';
    updateParticipantState();
    clearErrors();
    show(successState, false);
    lastDownload = null;
    lastDownloadFilename = '';
    dirty = false;
    document.getElementById('representative').focus();
}

form?.addEventListener('submit', (event) => {
    event.preventDefault();
    generateDocument();
});
form?.addEventListener('input', () => {
    dirty = true;
});
document.addEventListener('click', (event) => {
    const action = event.target.closest('[data-page-action]')?.dataset.pageAction;
    if (action === 'download-blank') {
        downloadBlank();
    } else if (action === 'photo-select') {
        photoInput.click();
    } else if (action === 'photo-remove') {
        clearPhoto();
    } else if (action === 'participant-add-manual') {
        addParticipant();
    } else if (action === 'participant-remove') {
        removeParticipant(event.target.closest('[data-page-action]'));
    } else if (action === 'download-again' && lastDownload) {
        downloadResult({blob: lastDownload, filename: ''},
                lastDownloadFilename || '반디_동아리_활동_내역서.hwpx');
    } else if (action === 'reset-form') {
        resetForm();
    }
});
photoInput?.addEventListener('change', () => setPhoto(photoInput.files[0]));
photoDropzone?.addEventListener('dragover', (event) => {
    event.preventDefault();
    photoDropzone.classList.add('border-ring', 'bg-secondary');
});
photoDropzone?.addEventListener('dragleave', () => {
    photoDropzone.classList.remove('border-ring', 'bg-secondary');
});
photoDropzone?.addEventListener('drop', (event) => {
    event.preventDefault();
    photoDropzone.classList.remove('border-ring', 'bg-secondary');
    setPhoto(event.dataTransfer.files[0]);
});
participantSearch?.addEventListener('input', () => {
    window.clearTimeout(searchTimer);
    searchTimer = window.setTimeout(searchParticipants, 300);
});
participantSearch?.addEventListener('keydown', handleSearchKeydown);
contentInput?.addEventListener('input', () => {
    contentCount.textContent = `${contentInput.value.length} / 300자`;
});
window.addEventListener('beforeunload', (event) => {
    if (dirty) {
        event.preventDefault();
        event.returnValue = '';
    }
});
window.addEventListener('pagehide', () => {
    revokePhotoPreview();
});

initializeDateTimeFields();
function localDateTimeValue(date = new Date()) {
    const rounded = new Date(date);
    rounded.setMinutes(Math.floor(rounded.getMinutes() / 5) * 5, 0, 0);
    const pad = (value) => String(value).padStart(2, '0');
    return `${rounded.getFullYear()}-${pad(rounded.getMonth() + 1)}-${pad(rounded.getDate())}`
            + `T${pad(rounded.getHours())}:${pad(rounded.getMinutes())}`;
}

setDateTimeValue('activityAt', localDateTimeValue());
updateParticipantState();
