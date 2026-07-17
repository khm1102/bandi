import {showToast} from '../common/toast.js';
import {all, bindPageActions, element, lookup, readValue} from '../common/dom.js';
import {currentUserRole} from '../common/session.js';
import {badge, closeActionModal} from '../common/view.js';

const ACTIONS = Object.freeze({ADD: 'checklist-add'});

function updateChecklistSummary() {
    const items = all('[data-checklist-item]');
    const completed = items.filter((item) => item.dataset.complete === 'true').length;
    const percent = items.length === 0 ? 0 : Math.round(completed / items.length * 100);
    lookup('[data-checklist-count]').textContent = `${completed} / ${items.length}`;
    const progress = lookup('[data-checklist-progress]');
    progress.max = items.length || 1;
    progress.value = completed;
    progress.textContent = `${percent}%`;
    const tone = percent === 100 ? 'success' : 'warning';
    lookup('[data-checklist-summary]').replaceChildren(badge(`전체 준비 ${percent}%`, tone));
}

function appendChecklistDelete(item) {
    if (currentUserRole === 'member' || lookup('[data-checklist-delete]', item)) {
        return;
    }
    const template = lookup('[data-checklist-delete-template]');
    const remove = template.content.firstElementChild.cloneNode(true);
    remove.dataset.checklistDelete = '';
    item.appendChild(remove);
}

function toggleChecklistItem(item) {
    const completed = item.dataset.complete === 'true';
    item.dataset.complete = String(!completed);
    item.setAttribute('aria-checked', String(!completed));
    const checkbox = item.firstElementChild;
    const label = item.children[1];
    checkbox.textContent = completed ? '' : '✓';
    checkbox.classList.toggle('border-success', !completed);
    checkbox.classList.toggle('bg-success', !completed);
    checkbox.classList.toggle('bg-card', completed);
    label.classList.toggle('text-muted-foreground', !completed);
    label.classList.toggle('line-through', !completed);
    updateChecklistSummary();
}

function addChecklistItem(trigger) {
    const content = readValue('ckItem');
    if (!content) {
        showToast('체크 항목을 입력해 주세요');
        return;
    }
    const team = readValue('ckTeam');
    const list = all('[data-checklist-list]').find((candidate) => candidate.dataset.checklistTeam === team);
    if (!list) {
        showToast('담당 팀 카드를 찾을 수 없어요');
        return;
    }
    const item = element('div', 'flex cursor-pointer items-center gap-3 rounded-md px-2 py-2');
    item.dataset.checklistItem = '';
    item.dataset.complete = 'false';
    item.setAttribute('role', 'checkbox');
    item.setAttribute('aria-checked', 'false');
    item.tabIndex = 0;
    const checkboxClasses = 'flex size-5 shrink-0 items-center justify-center rounded-md border '
        + 'bg-card text-white';
    item.append(
        element('span', checkboxClasses),
        element('span', 'flex-1 text-sm font-semibold', content)
    );
    appendChecklistDelete(item);
    const addButton = lookup('[data-open-modal="checkModal"]', list);
    list.insertBefore(item, addButton);
    closeActionModal(trigger);
    updateChecklistSummary();
    showToast('체크리스트 항목을 추가했어요');
}

all('[data-checklist-item]').forEach((item) => {
    item.setAttribute('role', 'checkbox');
    item.setAttribute('aria-checked', String(item.dataset.complete === 'true'));
    item.tabIndex = 0;
    appendChecklistDelete(item);
});

document.addEventListener('click', (event) => {
    const deleteButton = event.target.closest('[data-checklist-delete]');
    if (deleteButton) {
        deleteButton.closest('[data-checklist-item]').remove();
        updateChecklistSummary();
        showToast('체크리스트 항목을 삭제했어요');
        return;
    }
    const modalButton = event.target.closest('[data-open-modal="checkModal"]');
    if (modalButton) {
        const list = modalButton.closest('[data-checklist-list]');
        if (list?.dataset.checklistTeam) {
            document.getElementById('ckTeam').value = list.dataset.checklistTeam;
        }
        return;
    }
    const item = event.target.closest('[data-checklist-item]');
    if (item) {
        toggleChecklistItem(item);
    }
});

document.addEventListener('keydown', (event) => {
    if (event.target.closest('button, a, input, select, textarea')) {
        return;
    }
    const item = event.target.closest('[data-checklist-item]');
    if (item && (event.key === 'Enter' || event.key === ' ')) {
        event.preventDefault();
        toggleChecklistItem(item);
    }
});

bindPageActions({[ACTIONS.ADD]: addChecklistItem});
updateChecklistSummary();
