const PAGE_WINDOW_SIZE = 5;

function createPageButton(pageNumber, currentPage, onPageChange) {
    const button = document.createElement('button');
    button.type = 'button';
    button.className = 'inline-flex h-11 min-w-11 items-center justify-center rounded-md px-3 text-sm font-medium focus-visible:ring-2 focus-visible:ring-ring';
    button.textContent = String(pageNumber + 1);
    button.dataset.page = String(pageNumber);
    if (pageNumber === currentPage) {
        button.classList.add('bg-primary', 'text-primary-foreground');
        button.setAttribute('aria-current', 'page');
    } else {
        button.classList.add('border', 'border-border', 'bg-background', 'text-foreground', 'hover:bg-secondary');
    }
    button.addEventListener('click', () => onPageChange(pageNumber));
    return button;
}

function calculateWindow(currentPage, totalPages) {
    const half = Math.floor(PAGE_WINDOW_SIZE / 2);
    const start = Math.max(0, Math.min(currentPage - half, totalPages - PAGE_WINDOW_SIZE));
    return {start, end: Math.min(totalPages, start + PAGE_WINDOW_SIZE)};
}

export function renderPagination(root, response, onPageChange) {
    if (!root) {
        return;
    }
    const total = root.querySelector('[data-pagination-total]');
    const pages = root.querySelector('[data-pagination-pages]');
    const mobile = root.querySelector('[data-pagination-mobile]');
    const previous = root.querySelector('[data-pagination-action="previous"]');
    const next = root.querySelector('[data-pagination-action="next"]');
    total.textContent = `총 ${response.totalElements.toLocaleString('ko-KR')}건`;
    pages.replaceChildren();
    const window = calculateWindow(response.page, response.totalPages);
    for (let page = window.start; page < window.end; page += 1) {
        pages.append(createPageButton(page, response.page, onPageChange));
    }
    mobile.textContent = response.totalPages === 0
        ? '0 / 0'
        : `${response.page + 1} / ${response.totalPages}`;
    previous.disabled = !response.hasPrevious;
    next.disabled = !response.hasNext;
    previous.dataset.page = String(response.page - 1);
    next.dataset.page = String(response.page + 1);
    root.paginationPageChange = onPageChange;
    if (!root.dataset.paginationBound) {
        root.addEventListener('click', (event) => {
            const action = event.target.closest('[data-pagination-action]');
            if (action && !action.disabled) {
                root.paginationPageChange(Number(action.dataset.page));
            }
        });
        root.dataset.paginationBound = 'true';
    }
    root.classList.remove('hidden');
    root.classList.add('flex');
}

export function readPageFromUrl(searchParams) {
    const parsed = Number.parseInt(searchParams.get('page') || '1', 10);
    return Number.isInteger(parsed) && parsed > 0 ? parsed - 1 : 0;
}

export function writeUrl(searchParams, push) {
    const query = searchParams.toString();
    const url = query ? `${window.location.pathname}?${query}` : window.location.pathname;
    window.history[push ? 'pushState' : 'replaceState']({}, '', url);
}

export function setUrlPage(searchParams, page) {
    if (page <= 0) {
        searchParams.delete('page');
        return;
    }
    searchParams.set('page', String(page + 1));
}

export function normalizePage(response, requestedPage) {
    if (response.totalPages === 0) {
        return 0;
    }
    if (requestedPage < response.totalPages) {
        return requestedPage;
    }
    return response.totalPages - 1;
}
