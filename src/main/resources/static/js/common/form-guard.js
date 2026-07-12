document.addEventListener('submit', (event) => {
    const form = event.target.closest('form[data-guard]');
    if (!form) {
        return;
    }
    if (form.dataset.submitted === 'true') {
        event.preventDefault();
        return;
    }
    form.dataset.submitted = 'true';
    form.querySelectorAll('button[type="submit"]').forEach((button) => {
        button.classList.add('pointer-events-none', 'opacity-50');
    });
});
