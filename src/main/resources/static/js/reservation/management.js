import {showToast} from '../common/toast.js';
import {all, bindPageActions} from '../common/dom.js';

const ACTIONS = Object.freeze({EXPORT: 'reservation-export'});

function exportReservations() {
    const rows = all('tbody tr').map((row) => all('td', row)
        .map((cell) => `"${cell.textContent.trim().replaceAll('"', '""')}"`)
        .join(','));
    const csv = ['관람객명,연락처,좌석,인원,관람일,회차', ...rows].join('\n');
    const anchor = document.createElement('a');
    anchor.href = URL.createObjectURL(new Blob([`\uFEFF${csv}`], {type: 'text/csv;charset=utf-8'}));
    anchor.download = 'bandi_reservations.csv';
    anchor.click();
    URL.revokeObjectURL(anchor.href);
    showToast('명단을 CSV로 내보냈어요');
}

bindPageActions({[ACTIONS.EXPORT]: exportReservations});
