import {del, post} from './api.js';
import {showToast} from './toast.js';

async function copyUrl(url) {
    if (navigator.clipboard?.writeText) {
        await navigator.clipboard.writeText(url);
        return;
    }
    const input = document.createElement('textarea');
    input.value = url;
    input.setAttribute('readonly', '');
    input.className = 'fixed opacity-0';
    document.body.append(input);
    input.select();
    const copied = document.execCommand('copy');
    input.remove();
    if (!copied) {
        throw new Error('링크를 복사하지 못했어요.');
    }
}

function absoluteUrl(url) {
    return new URL(url, window.location.origin).toString();
}

async function presentShare(url, title) {
    if (navigator.share) {
        try {
            await navigator.share({title, url});
            return;
        } catch (error) {
            if (error.name === 'AbortError') {
                return;
            }
        }
    }
    await copyUrl(url);
    showToast('링크를 복사했어요.');
}

function setShareEnabled(button, enabled) {
    button.dataset.shareEnabled = String(enabled);
    const revokeButton = document.querySelector(button.dataset.shareRevokeTarget);
    if (revokeButton) {
        revokeButton.classList.toggle('hidden', !enabled);
    }
}

async function issueAndShare(button) {
    const canIssue = button.dataset.shareCanIssue === 'true';
    let shareUrl = absoluteUrl(button.dataset.shareInternalUrl);
    let title = '반디 내부 게시글';
    if (canIssue) {
        if (button.dataset.shareEnabled !== 'true') {
            const accepted = window.confirm(
                '게시글 제목이 카카오·디스코드 미리보기에 표시될 수 있어요. 공유 링크를 만들까요?');
            if (!accepted) {
                return;
            }
        }
        const result = await post(button.dataset.shareIssueUrl);
        shareUrl = absoluteUrl(result.shareUrl);
        title = button.dataset.shareTitle;
        setShareEnabled(button, true);
    }
    await presentShare(shareUrl, title);
}

async function revoke(button) {
    const accepted = window.confirm(
        '공유 링크를 중단할까요? 이미 카카오·디스코드에 저장된 제목 미리보기는 바로 지워지지 않을 수 있어요.');
    if (!accepted) {
        return;
    }
    button.disabled = true;
    try {
        await del(button.dataset.shareRevokeUrl);
        const shareButton = document.querySelector(button.dataset.shareButtonTarget);
        if (shareButton) {
            setShareEnabled(shareButton, false);
        }
        showToast('제목 공개 공유 링크를 중단했어요.');
    } finally {
        button.disabled = false;
    }
}

export function initializeShareActions(root = document) {
    root.querySelectorAll('[data-share-button]').forEach((button) => {
        if (button.dataset.shareInitialized === 'true') {
            return;
        }
        button.dataset.shareInitialized = 'true';
        button.addEventListener('click', async () => {
            button.disabled = true;
            try {
                await issueAndShare(button);
            } catch (error) {
                showToast(error.message || '공유 링크를 준비하지 못했어요.');
            } finally {
                button.disabled = false;
            }
        });
    });
    root.querySelectorAll('[data-share-revoke]').forEach((button) => {
        if (button.dataset.shareInitialized === 'true') {
            return;
        }
        button.dataset.shareInitialized = 'true';
        button.addEventListener('click', async () => {
            try {
                await revoke(button);
            } catch (error) {
                showToast(error.message || '공유 링크를 중단하지 못했어요.');
            }
        });
    });
}
