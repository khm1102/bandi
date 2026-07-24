const registry = new Map();
const DESKTOP_PICKER_QUERY = '(min-width: 768px) and (hover: hover) and (pointer: fine)';
const DATE_PATTERN = /^\d{4}-\d{2}-\d{2}$/;
const DATE_TIME_PATTERN = /^(\d{4}-\d{2}-\d{2})T(\d{2}:\d{2})/;

function lookupField(id) {
    return registry.get(id) || null;
}

function splitValue(value) {
    const matched = String(value || '').match(DATE_TIME_PATTERN);
    if (matched) {
        return {date: matched[1], time: matched[2]};
    }
    if (DATE_PATTERN.test(String(value || ''))) {
        return {date: String(value), time: ''};
    }
    return {date: '', time: ''};
}

function synchronizeNative(field, notify = true) {
    const date = field.dateInput.value.trim();
    const time = field.timeInput.value;
    field.nativeInput.value = field.dateOnly
        ? date
        : date && time ? `${date}T${time}` : '';
    if (notify) {
        field.nativeInput.dispatchEvent(new Event('input', {bubbles: true}));
        field.nativeInput.dispatchEvent(new Event('change', {bubbles: true}));
    }
}

function synchronizeEnhanced(field) {
    const value = splitValue(field.nativeInput.value);
    field.dateInput.value = value.date;
    field.timeInput.value = value.time;
    if (field.calendar) {
        field.calendar.set({
            selectedDates: value.date ? [value.date] : [],
        }, {dates: true, month: true, year: true});
    }
}

function createCalendar(field) {
    const Calendar = window.VanillaCalendarPro?.Calendar;
    if (!Calendar) {
        return null;
    }
    const selected = splitValue(field.nativeInput.value);
    const calendar = new Calendar(field.dateInput, {
        inputMode: true,
        locale: 'ko-KR',
        firstWeekday: 1,
        positionToInput: 'auto',
        selectedTheme: 'light',
        selectionDatesMode: 'single',
        selectedDates: selected.date ? [selected.date] : [],
        labels: {
            application: '날짜 선택 달력',
            navigation: '달력 이동',
            arrowNext: {month: '다음 달', year: '다음 연도 목록'},
            arrowPrev: {month: '이전 달', year: '이전 연도 목록'},
            month: '월 선택',
            months: '월 목록',
            year: '연도 선택',
            years: '연도 목록',
            week: '요일',
            weekNumber: '주 번호',
            dates: '날짜 목록',
            selectingTime: '시간 선택',
            inputHour: '시',
            inputMinute: '분',
            rangeHour: '시 선택',
            rangeMinute: '분 선택',
            btnKeeping: '오전 오후 전환',
        },
        onClickDate: (instance) => {
            field.dateInput.value = instance.context.selectedDates[0] || '';
            synchronizeNative(field);
            instance.hide();
        },
    });
    calendar.init();
    return calendar;
}

function enhanceField(root) {
    const id = root.dataset.dateTimeId;
    const nativeInput = root.querySelector('[data-date-time-native]');
    const enhanced = root.querySelector('[data-date-time-enhanced]');
    const dateInput = root.querySelector('[data-date-time-date]');
    const timeInput = root.querySelector('[data-date-time-time]');
    const timeWrap = root.querySelector('[data-date-time-time-wrap]');
    if (!id || !nativeInput || !enhanced || !dateInput || !timeInput || !timeWrap) {
        return;
    }

    const field = {
        id,
        root,
        nativeInput,
        enhanced,
        dateInput,
        timeInput,
        timeWrap,
        calendar: null,
        dateOnly: nativeInput.type === 'date',
    };
    registry.set(id, field);
    synchronizeEnhanced(field);
    if (!window.matchMedia(DESKTOP_PICKER_QUERY).matches) {
        return;
    }

    try {
        field.calendar = createCalendar(field);
        if (!field.calendar) {
            return;
        }
        dateInput.required = nativeInput.required;
        timeInput.required = nativeInput.required && !field.dateOnly;
        dateInput.disabled = nativeInput.disabled;
        timeInput.disabled = nativeInput.disabled;
        dateInput.addEventListener('change', () => synchronizeNative(field));
        timeInput.addEventListener('change', () => synchronizeNative(field));
        nativeInput.classList.add('hidden');
        enhanced.classList.remove('hidden');
        enhanced.classList.add('grid');
        root.dataset.dateTimeEnhanced = 'true';
    } catch (error) {
        registry.delete(id);
    }
}

export function initializeDateTimeFields(root = document) {
    root.querySelectorAll('[data-date-time-field]').forEach((fieldRoot) => {
        if (!registry.has(fieldRoot.dataset.dateTimeId)) {
            enhanceField(fieldRoot);
        }
    });
}

export function readDateTimeValue(id) {
    const field = lookupField(id);
    if (field?.root.dataset.dateTimeEnhanced === 'true') {
        synchronizeNative(field, false);
    }
    return field?.nativeInput.value || document.getElementById(id)?.value || '';
}

export function setDateTimeValue(id, value) {
    const field = lookupField(id);
    const nativeInput = field?.nativeInput || document.getElementById(id);
    if (!nativeInput) {
        return;
    }
    nativeInput.value = value ? String(value).slice(0, 16) : '';
    if (field) {
        synchronizeEnhanced(field);
    }
}

export function setDateTimeDisabled(id, disabled) {
    const field = lookupField(id);
    const nativeInput = field?.nativeInput || document.getElementById(id);
    if (!nativeInput) {
        return;
    }
    nativeInput.disabled = disabled;
    if (field) {
        field.dateInput.disabled = disabled;
        field.timeInput.disabled = disabled;
    }
}

export function setDateTimeMode(id, dateOnly) {
    const field = lookupField(id);
    const nativeInput = field?.nativeInput || document.getElementById(id);
    if (!nativeInput) {
        return;
    }
    nativeInput.type = dateOnly ? 'date' : 'datetime-local';
    if (!field) {
        return;
    }
    field.dateOnly = dateOnly;
    field.timeWrap.classList.toggle('hidden', dateOnly);
    field.enhanced.classList.toggle('md:grid-cols-1', dateOnly);
    field.enhanced.classList.toggle('md:grid-cols-2', !dateOnly);
    field.timeInput.required = field.nativeInput.required && !dateOnly;
    synchronizeEnhanced(field);
}

export function focusDateTimeField(id) {
    const field = lookupField(id);
    const target = field?.root.dataset.dateTimeEnhanced === 'true'
        ? field.dateInput : field?.nativeInput || document.getElementById(id);
    target?.focus();
}
