import {ApiError, get, getBlob, post, put} from '../common/api.js';
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
let savedRecordId = null;
let savedDocumentFileId = null;
let lastDownloadFilename = '';
let hasStoredPhoto = false;
let submitted = false;
let submissionPending = false;
let dirty = false;
let searchTimer = 0;
let searchGeneration = 0;
let activeSuggestionIndex = -1;

function show(element, visible, display = 'block') {
    if (!element) {
        return;
    }
    element.hidden = !visible;
    element.classList.toggle('hidden', !visible);
    if (!visible) {
        element.style.setProperty('display', 'none');
    } else {
        element.style.removeProperty('display');
    }
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

function addParticipant(values = {}, options = {}) {
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
    if (options.markDirty !== false) {
        dirty = true;
    }
    if (options.focus !== false) {
        row.querySelector('[data-participant-field="name"]')?.focus();
    }
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
    if (!file && !hasStoredPhoto) {
        return '활동 사진을 선택해 주세요.';
    }
    if (!file) {
        return '';
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
    hasStoredPhoto = false;
    photoPreviewUrl = URL.createObjectURL(file);
    photoPreview.src = photoPreviewUrl;
    photoName.textContent = file.name;
    show(photoPreview, true);
    show(photoActions, true, 'flex');
    photoActions.querySelector('[data-page-action="photo-remove"]')
            ?.classList.remove('hidden');
    setFieldError('photo');
    dirty = true;
}

function clearPhoto() {
    revokePhotoPreview();
    photo = null;
    hasStoredPhoto = false;
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

function setGenerating(generating, submitting = false) {
    form.querySelectorAll('button, input, textarea').forEach((control) => {
        control.disabled = generating;
    });
    const draftButton = form.querySelector('[data-page-action="save-draft"]');
    const submitButton = form.querySelector('[data-page-action="save-submit"]');
    draftButton.textContent = generating && !submitting ? '저장하는 중…' : '임시 저장';
    submitButton.textContent = generating && submitting
        ? '저장하고 요청하는 중…' : '저장 후 검수 요청';
    if (!generating && submitted) {
        lockSubmittedForm();
    }
}

function lockSubmittedForm() {
    show(document.querySelector('[data-save-actions]'), false);
    show(document.querySelector('[data-page-action="submit-saved"]'), false);
    form.querySelectorAll('button, input, textarea, select').forEach((control) => {
        if (!control.closest('[data-success-state]')) {
            control.disabled = true;
        }
    });
}

async function saveDocument(submitAfterSave) {
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
    if (photo) {
        body.append('photo', photo, photo.name);
    }
    setGenerating(true, submitAfterSave);
    try {
        const result = savedRecordId
            ? await put(`/api/activity-report-documents/${savedRecordId}`, body)
            : await post('/api/activity-report-documents', body);
        savedRecordId = result.activityRecordId;
        savedDocumentFileId = result.documentStoredFileId;
        hasStoredPhoto = true;
        photo = null;
        photoInput.value = '';
        lastDownloadFilename = result.filename
            || `${request.activityAt.slice(0, 10)}_반디_동아리_활동_내역서.hwpx`;
        dirty = false;
        window.history.replaceState({}, '', `/activity-documents?activityRecordId=`
                + encodeURIComponent(savedRecordId));
        if (submitAfterSave) {
            try {
                await submitSavedDocument();
            } catch (error) {
                document.querySelector('[data-success-title]').textContent =
                    '임시 저장은 완료했지만 검수를 요청하지 못했어요.';
                document.querySelector('[data-success-message]').textContent =
                    '입력값과 파일은 보존되어 있습니다. 잠시 후 검수 요청을 다시 눌러 주세요.';
                show(document.querySelector('[data-page-action="submit-saved"]'), true);
                formError.textContent = error.message || '검수를 요청하지 못했습니다.';
                show(formError, true);
            }
        } else {
            document.querySelector('[data-success-title]').textContent =
                '활동 내역서를 임시 저장했어요.';
            document.querySelector('[data-success-message]').textContent =
                '입력값·사진·HWPX가 활동 기록에 저장됐습니다.';
            show(document.querySelector('[data-page-action="submit-saved"]'), true);
        }
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
        formError.textContent = error.message || '활동 내역서를 저장하지 못했습니다. 입력 내용은 그대로 유지됩니다.';
        show(formError, true);
    } finally {
        setGenerating(false);
    }
}

async function submitSavedDocument() {
    if (!savedRecordId || submitted || submissionPending) {
        return;
    }
    const submitButton = document.querySelector('[data-page-action="submit-saved"]');
    submissionPending = true;
    if (submitButton) {
        submitButton.disabled = true;
        submitButton.textContent = '요청하는 중…';
    }
    try {
        await post(`/api/activity-report-documents/${savedRecordId}/submit`, {});
        document.querySelector('[data-success-title]').textContent =
            '운영진에게 검수를 요청했어요.';
        document.querySelector('[data-success-message]').textContent =
            '활동 기록에서 처리 상태를 확인할 수 있습니다.';
        submitted = true;
        lockSubmittedForm();
    } catch (error) {
        if (error instanceof ApiError && error.code === 'AR002') {
            await loadSavedDraft();
            clearErrors();
            return;
        }
        throw error;
    } finally {
        submissionPending = false;
        if (submitButton && !submitted) {
            submitButton.disabled = false;
            submitButton.textContent = '검수 요청';
        }
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
    form.querySelectorAll('button, input, textarea, select').forEach((control) => {
        control.disabled = false;
    });
    participantList.replaceChildren();
    clearPhoto();
    setDateTimeValue('activityAt', localDateTimeValue());
    contentCount.textContent = '0 / 300자';
    updateParticipantState();
    clearErrors();
    show(successState, false);
    show(document.querySelector('[data-save-actions]'), true, 'flex');
    savedRecordId = null;
    savedDocumentFileId = null;
    hasStoredPhoto = false;
    submitted = false;
    submissionPending = false;
    lastDownloadFilename = '';
    dirty = false;
    window.history.replaceState({}, '', '/activity-documents');
    document.getElementById('representative').focus();
}

form?.addEventListener('submit', (event) => event.preventDefault());
form?.addEventListener('input', () => {
    dirty = true;
});
document.addEventListener('click', (event) => {
    const action = event.target.closest('[data-page-action]')?.dataset.pageAction;
    if (action === 'download-blank') {
        downloadBlank();
    } else if (action === 'save-draft') {
        saveDocument(false);
    } else if (action === 'save-submit') {
        saveDocument(true);
    } else if (action === 'photo-select') {
        photoInput.click();
    } else if (action === 'photo-remove') {
        clearPhoto();
    } else if (action === 'participant-add-manual') {
        addParticipant();
    } else if (action === 'participant-remove') {
        removeParticipant(event.target.closest('[data-page-action]'));
    } else if (action === 'download-again' && savedRecordId && savedDocumentFileId) {
        getBlob(`/api/activity-management/${savedRecordId}/files/`
                + `${savedDocumentFileId}/download`).then((result) => {
            downloadResult(result, lastDownloadFilename
                    || '반디_동아리_활동_내역서.hwpx');
        }).catch((error) => {
            formError.textContent = error.message || 'HWPX를 내려받지 못했습니다.';
            show(formError, true);
        });
    } else if (action === 'submit-saved') {
        submitSavedDocument().catch((error) => {
            formError.textContent = error.message || '검수를 요청하지 못했습니다.';
            show(formError, true);
        });
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

async function loadSavedDraft() {
    const rawId = new URLSearchParams(window.location.search).get('activityRecordId');
    if (!rawId || !/^\d+$/.test(rawId)) {
        setDateTimeValue('activityAt', localDateTimeValue());
        updateParticipantState();
        return;
    }
    try {
        const draft = await get(`/api/activity-report-documents/${rawId}`);
        savedRecordId = draft.activityRecordId;
        savedDocumentFileId = draft.documentStoredFileId;
        lastDownloadFilename = draft.documentOriginalName;
        document.getElementById('representative').value = draft.representative;
        document.getElementById('location').value = draft.location;
        setDateTimeValue('activityAt', draft.activityAt?.slice(0, 16) || '');
        contentInput.value = draft.content || '';
        contentCount.textContent = `${contentInput.value.length} / 300자`;
        participantList.replaceChildren();
        (draft.participants || []).forEach((participant) => addParticipant(
                participant, {focus: false, markDirty: false}));
        hasStoredPhoto = true;
        photoName.textContent = draft.photoOriginalName || '저장된 활동 사진';
        photoPreview.src = `/api/activity-management/${savedRecordId}/files/`
                + `${draft.photoStoredFileId}/download`;
        show(photoPreview, true);
        show(photoActions, true, 'flex');
        const removeButton = photoActions.querySelector('[data-page-action="photo-remove"]');
        if (removeButton) {
            removeButton.classList.add('hidden');
        }
        updateParticipantState();
        if (['DRAFT', 'REVISION_REQUESTED'].includes(draft.status)) {
            document.querySelector('[data-success-title]').textContent =
                draft.status === 'REVISION_REQUESTED'
                    ? '수정 요청된 활동 내역서를 불러왔어요.'
                    : '임시 저장된 활동 내역서를 불러왔어요.';
            document.querySelector('[data-success-message]').textContent =
                '내용을 수정해 다시 저장하거나 현재 문서를 검수 요청할 수 있습니다.';
            show(document.querySelector('[data-page-action="submit-saved"]'), true);
            show(successState, true);
        } else {
            submitted = true;
            lockSubmittedForm();
            document.querySelector('[data-success-title]').textContent =
                draft.status === 'SUBMITTED'
                    ? '운영진에게 검수를 요청했어요.'
                    : '현재 상태에서는 문서를 수정할 수 없습니다.';
            document.querySelector('[data-success-message]').textContent =
                draft.status === 'SUBMITTED'
                    ? '활동 기록에서 처리 상태를 확인할 수 있습니다.'
                    : '활동 기록에서 검수 상태와 운영진 의견을 확인해 주세요.';
            show(document.querySelector('[data-page-action="submit-saved"]'), false);
            show(successState, true);
        }
        dirty = false;
    } catch (error) {
        formError.textContent = error.message || '저장한 활동 내역서를 불러오지 못했습니다.';
        show(formError, true);
    }
}

loadSavedDraft();
