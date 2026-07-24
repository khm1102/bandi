import {get, post, put} from '../common/api.js';

const form = document.querySelector('[data-asset-form]');
const root = document.querySelector('[data-asset-form-root]');

if (form && root) {
    const assetId = Number(window.location.pathname.match(/^\/props\/(\d+)\/edit$/)?.[1]) || null;
    const nameInput = form.querySelector('[data-asset-name]');
    const categoryInput = form.querySelector('[data-asset-category]');
    const storageLocationInput = form.querySelector('[data-asset-storage-location]');
    const photoInput = form.querySelector('[data-asset-photo-input]');
    const photoPreview = form.querySelector('[data-asset-photo-preview]');
    const photoPlaceholder = form.querySelector('[data-asset-photo-placeholder]');
    const photoName = form.querySelector('[data-asset-photo-name]');
    const photoError = form.querySelector('[data-asset-photo-error]');
    const photoDropzone = form.querySelector('[data-asset-photo-dropzone]');
    const trackingTypeInput = form.querySelector('[data-asset-tracking-type]');
    const trackingTypeHelp = form.querySelector('[data-asset-tracking-type-help]');
    const totalQuantityInput = form.querySelector('[data-asset-total-quantity]');
    const ownerTypeInput = form.querySelector('[data-asset-owner-type]');
    const ownerMemberIdInput = form.querySelector('[data-asset-owner-member-id]');
    const memberOwnerField = form.querySelector('[data-asset-member-owner-field]');
    const externalOwnerField = form.querySelector('[data-asset-external-owner-field]');
    const externalOwnerNameInput = form.querySelector('[data-asset-external-owner-name]');
    const noteInput = form.querySelector('[data-asset-note]');
    const errorBox = form.querySelector('[data-asset-form-error]');
    const submitButton = form.querySelector('[data-page-action="save-asset"]');
    let selectedPhoto = null;
    let previewUrl = null;
    let submitting = false;
    let membersLoaded = false;

    function setMessage(target, message = '') {
        target.textContent = message;
        target.classList.toggle('hidden', !message);
    }

    function clearPreviewUrl() {
        if (previewUrl) {
            URL.revokeObjectURL(previewUrl);
            previewUrl = null;
        }
    }

    function setPreview(source, fileName = '') {
        photoPreview.src = source;
        photoPreview.classList.remove('hidden');
        photoPlaceholder.classList.add('hidden');
        photoName.textContent = fileName || '현재 등록된 사진이에요.';
    }

    function clearPhotoError() {
        setMessage(photoError);
    }

    function selectPhoto(file) {
        clearPhotoError();
        if (!file) {
            return;
        }
        if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
            setMessage(photoError, 'JPG, PNG, WebP 이미지만 선택할 수 있어요.');
            return;
        }
        selectedPhoto = file;
        clearPreviewUrl();
        previewUrl = URL.createObjectURL(file);
        setPreview(previewUrl, `${file.name} · 저장할 때 사진이 교체돼요.`);
    }

    function updateOwnerFields() {
        const type = ownerTypeInput.value;
        memberOwnerField.classList.toggle('hidden', type !== 'MEMBER');
        externalOwnerField.classList.toggle('hidden', type !== 'EXTERNAL');
        ownerMemberIdInput.required = type === 'MEMBER';
        externalOwnerNameInput.required = type === 'EXTERNAL';
        if (type !== 'MEMBER') {
            ownerMemberIdInput.value = '';
        }
        if (type !== 'EXTERNAL') {
            externalOwnerNameInput.value = '';
        }
        if (type === 'MEMBER') {
            loadMembers();
        }
    }

    async function loadMembers() {
        if (membersLoaded) {
            return;
        }
        try {
            const page = await get('/api/members', {
                page: 0,
                pageSize: 100,
                status: 'ACTIVE',
            });
            const selected = ownerMemberIdInput.value;
            const items = page.items || [];
            items.forEach((member) => {
                const option = document.createElement('option');
                option.value = String(member.memberId);
                option.textContent = `${member.name} · ${member.studentNo}`;
                ownerMemberIdInput.append(option);
            });
            ownerMemberIdInput.value = selected;
            membersLoaded = true;
        } catch (error) {
            setMessage(errorBox, '소유 멤버 목록을 불러오지 못했어요. 잠시 후 다시 선택해 주세요.');
        }
    }

    function payload(photoFileId) {
        const ownerType = ownerTypeInput.value;
        return {
            name: nameInput.value.trim(),
            categoryCode: categoryInput.value,
            trackingType: trackingTypeInput.value,
            ownerType,
            ownerMemberId: ownerType === 'MEMBER'
                ? Number(ownerMemberIdInput.value) || null : null,
            externalOwnerName: ownerType === 'EXTERNAL'
                ? externalOwnerNameInput.value.trim() : null,
            totalQuantity: Number(totalQuantityInput.value),
            storageLocation: storageLocationInput.value.trim(),
            photoFileId,
            note: noteInput.value.trim() || null,
        };
    }

    function setSubmitting(next) {
        submitting = next;
        submitButton.disabled = next;
        submitButton.textContent = next ? '저장 중…' : '품목 저장';
    }

    async function uploadPhotoIfNeeded() {
        if (!selectedPhoto) {
            return null;
        }
        const data = new FormData();
        data.append('file', selectedPhoto);
        const response = await post('/api/files/private?domain=asset', data);
        const storedFileId = response.id ?? response.storedFileId;
        if (!storedFileId) {
            throw new Error('사진 업로드 결과를 확인하지 못했어요. 다시 시도해 주세요.');
        }
        return storedFileId;
    }

    function ensureValid() {
        if (!nameInput.value.trim() || !storageLocationInput.value.trim()) {
            throw new Error('품목명과 보관 위치를 입력해 주세요.');
        }
        if (Number(totalQuantityInput.value) < 1) {
            throw new Error('수량은 1개 이상으로 입력해 주세요.');
        }
        if (ownerTypeInput.value === 'MEMBER' && !ownerMemberIdInput.value) {
            throw new Error('소유 멤버를 선택해 주세요.');
        }
        if (ownerTypeInput.value === 'EXTERNAL'
                && !externalOwnerNameInput.value.trim()) {
            throw new Error('외부 소유자 이름을 입력해 주세요.');
        }
    }

    async function save() {
        if (submitting) {
            return;
        }
        setMessage(errorBox);
        clearPhotoError();
        try {
            ensureValid();
            setSubmitting(true);
            const photoFileId = await uploadPhotoIfNeeded();
            const request = payload(photoFileId);
            if (assetId) {
                delete request.trackingType;
                await put(`/api/assets/${assetId}`, request);
                window.location.assign(`/props/${assetId}`);
                return;
            }
            const response = await post('/api/assets', request);
            const createdId = response.assetItemId ?? response.id;
            window.location.assign(createdId ? `/props/${createdId}` : '/props');
        } catch (error) {
            setMessage(errorBox, error.message || '품목을 저장하지 못했어요. 입력한 내용은 유지돼요. 다시 시도해 주세요.');
            setSubmitting(false);
        }
    }

    async function loadEdit() {
        if (!assetId) {
            return;
        }
        try {
            const asset = await get(`/api/assets/${assetId}`);
            document.title = '소품·장비 수정';
            nameInput.value = asset.name;
            categoryInput.value = asset.categoryCode;
            storageLocationInput.value = asset.storageLocation;
            trackingTypeInput.value = asset.trackingType;
            trackingTypeInput.disabled = true;
            trackingTypeHelp.classList.remove('hidden');
            totalQuantityInput.value = asset.totalQuantity;
            ownerTypeInput.value = asset.ownerType;
            externalOwnerNameInput.value = asset.externalOwnerName || '';
            noteInput.value = asset.note || '';
            if (asset.photoFileId) {
                setPreview(`/api/assets/${asset.assetItemId}/photo/download`, '현재 등록된 사진이에요. 새 사진을 고르면 교체돼요.');
            }
            updateOwnerFields();
            if (asset.ownerMemberId) {
                ownerMemberIdInput.value = String(asset.ownerMemberId);
                await loadMembers();
                ownerMemberIdInput.value = String(asset.ownerMemberId);
            }
        } catch (error) {
            setMessage(errorBox, error.message || '품목 정보를 불러오지 못했어요. 목록으로 돌아가 다시 시도해 주세요.');
        }
    }

    form.addEventListener('submit', (event) => {
        event.preventDefault();
        save();
    });
    form.addEventListener('click', (event) => {
        if (event.target.closest('[data-asset-photo-select]')) {
            photoInput.click();
        }
    });
    photoInput.addEventListener('change', () => selectPhoto(photoInput.files?.[0]));
    photoDropzone.addEventListener('dragover', (event) => {
        event.preventDefault();
        photoDropzone.classList.add('border-primary');
    });
    photoDropzone.addEventListener('dragleave', () => photoDropzone.classList.remove('border-primary'));
    photoDropzone.addEventListener('drop', (event) => {
        event.preventDefault();
        photoDropzone.classList.remove('border-primary');
        selectPhoto(event.dataTransfer.files?.[0]);
    });
    ownerTypeInput.addEventListener('change', updateOwnerFields);
    window.addEventListener('pagehide', clearPreviewUrl);
    updateOwnerFields();
    loadEdit();
}
