import {showToast} from '../common/toast.js';
import {all, element, lookup} from '../common/dom.js';
import {badge} from '../common/view.js';

const reservationState = {
    date: '6/21',
    selected: [],
    taken: {
        '6/21': ['A3', 'A4', 'C5', 'C6', 'D1', 'D2', 'E4', 'E5'],
        '6/22': ['B2']
    }
};

function styleSeat(button, state) {
    button.classList.remove(
        'border-primary',
        'bg-primary',
        'text-primary-foreground',
        'cursor-not-allowed',
        'bg-secondary',
        'text-muted-foreground/50',
        'bg-card',
        'text-muted-foreground'
    );
    if (state === 'taken') {
        button.disabled = true;
        button.setAttribute('aria-disabled', 'true');
        button.setAttribute('aria-pressed', 'false');
        button.classList.add('cursor-not-allowed', 'bg-secondary', 'text-muted-foreground/50');
        return;
    }
    button.disabled = false;
    button.setAttribute('aria-disabled', 'false');
    if (state === 'selected') {
        button.setAttribute('aria-pressed', 'true');
        button.classList.add('border-primary', 'bg-primary', 'text-primary-foreground');
        return;
    }
    button.setAttribute('aria-pressed', 'false');
    button.classList.add('bg-card', 'text-muted-foreground');
}

function renderSelectedSeats() {
    const wrapper = lookup('[data-selected-seats]');
    wrapper.replaceChildren();
    if (reservationState.selected.length === 0) {
        wrapper.appendChild(element('span', 'text-xs text-muted-foreground', '아직 선택한 좌석이 없어요'));
    } else {
        reservationState.selected.slice().sort().forEach((seatLabel) => {
            const chipClasses = 'inline-flex items-center gap-1 rounded-md bg-accent '
                + 'py-1 pl-2.5 pr-1.5 text-xs font-extrabold text-accent-foreground';
            const chip = element('span', chipClasses, seatLabel);
            const remove = element(
                'button',
                'flex size-8 items-center justify-center rounded-md bg-primary/20 text-base',
                '×'
            );
            remove.type = 'button';
            remove.dataset.seatRemove = seatLabel;
            remove.setAttribute('aria-label', `${seatLabel} 좌석 선택 해제`);
            chip.appendChild(remove);
            wrapper.appendChild(chip);
        });
    }
    const dayName = reservationState.date === '6/21' ? '토' : '일';
    lookup('[data-reservation-summary]').firstChild.textContent = `${reservationState.date} ${dayName} 17:00`;
    lookup('[data-reservation-count]').textContent = `${reservationState.selected.length}석`;
    const submit = lookup('[data-reservation-submit]');
    submit.textContent = reservationState.selected.length > 0
        ? `${reservationState.selected.length}석 관람 신청하기`
        : '좌석을 선택하세요';
}

function refreshSeatMap(date) {
    all('[data-seat-label]').forEach((button) => {
        const label = button.dataset.seatLabel;
        const state = reservationState.taken[date].includes(label)
            ? 'taken'
            : reservationState.selected.includes(label) ? 'selected' : 'free';
        styleSeat(button, state);
    });
    const total = all('[data-seat-label]').length;
    const remaining = total - reservationState.taken[date].length;
    lookup('[data-seat-remaining]').replaceChildren(badge(`잔여 ${remaining}석`, 'success'));
    renderSelectedSeats();
}

function selectSeat(button) {
    const label = button.dataset.seatLabel;
    if (reservationState.taken[reservationState.date].includes(label)) {
        return;
    }
    const selectedIndex = reservationState.selected.indexOf(label);
    if (selectedIndex >= 0) {
        reservationState.selected.splice(selectedIndex, 1);
    } else {
        if (reservationState.selected.length >= 6) {
            showToast('한 번에 최대 6석까지 선택할 수 있어요');
            return;
        }
        reservationState.selected.push(label);
    }
    refreshSeatMap(reservationState.date);
}

function changeReservationDate(button) {
    reservationState.date = button.dataset.reservationDate;
    reservationState.selected = [];
    all('[data-reservation-date]').forEach((candidate) => {
        candidate.classList.remove('border-primary', 'bg-accent', 'text-accent-foreground');
        candidate.setAttribute('aria-pressed', 'false');
    });
    button.classList.add('border-primary', 'bg-accent', 'text-accent-foreground');
    button.setAttribute('aria-pressed', 'true');
    const dayName = reservationState.date === '6/21' ? '토' : '일';
    lookup('[data-seat-date-label]').textContent = `${reservationState.date} (${dayName}) 17:00, 좌석을 눌러 선택하세요`;
    refreshSeatMap(reservationState.date);
}

function showReservationFeedback(message, tone) {
    const feedback = lookup('[data-reservation-feedback]');
    feedback.textContent = message;
    feedback.classList.remove(
        'hidden',
        'border-destructive',
        'bg-destructive-soft',
        'text-destructive',
        'border-success',
        'bg-success-soft',
        'text-success'
    );
    if (tone === 'success') {
        feedback.classList.add('border-success', 'bg-success-soft', 'text-success');
        return;
    }
    feedback.classList.add('border-destructive', 'bg-destructive-soft', 'text-destructive');
}

function reserveSeats() {
    if (reservationState.selected.length === 0) {
        showReservationFeedback('좌석을 먼저 선택해 주세요.', 'danger');
        lookup('[data-seat-label]:not(:disabled)')?.focus();
        return;
    }
    const requiredFields = ['guestName', 'guestPhone'].map((id) => document.getElementById(id));
    const firstInvalidField = requiredFields.find((field) => !field.value.trim());
    if (firstInvalidField) {
        requiredFields.forEach((field) => {
            field.setAttribute('aria-invalid', String(!field.value.trim()));
        });
        showReservationFeedback('이름과 연락처를 모두 입력해 주세요.', 'danger');
        firstInvalidField.focus();
        return;
    }
    const selected = reservationState.selected.slice().sort();
    reservationState.taken[reservationState.date].push(...selected);
    reservationState.selected = [];
    refreshSeatMap(reservationState.date);
    showReservationFeedback(`${selected.join(', ')} 좌석 신청이 접수되었습니다.`, 'success');
    showToast(`${selected.join(', ')} 좌석 신청이 접수되었어요!`);
}

all('[data-seat-map] > div').forEach((rowNode) => {
    const rowLabel = lookup('span', rowNode).textContent.trim();
    all('button', rowNode).forEach((button, index) => {
        button.dataset.seatLabel = `${rowLabel}${index + 1}`;
        button.setAttribute('aria-label', `${rowLabel}열 ${index + 1}번 좌석`);
    });
});

document.addEventListener('click', (event) => {
    const remove = event.target.closest('[data-seat-remove]');
    if (remove) {
        const selectedIndex = reservationState.selected.indexOf(remove.dataset.seatRemove);
        if (selectedIndex >= 0) {
            reservationState.selected.splice(selectedIndex, 1);
            refreshSeatMap(reservationState.date);
        }
        return;
    }
    const seat = event.target.closest('[data-seat-label]');
    if (seat) {
        selectSeat(seat);
        return;
    }
    const date = event.target.closest('[data-reservation-date]');
    if (date) {
        changeReservationDate(date);
    }
});

document.addEventListener('input', (event) => {
    if (['guestName', 'guestPhone'].includes(event.target.id)) {
        event.target.removeAttribute('aria-invalid');
    }
});

lookup('[data-reservation-form]').addEventListener('submit', (event) => {
    event.preventDefault();
    reserveSeats();
});
refreshSeatMap('6/21');
