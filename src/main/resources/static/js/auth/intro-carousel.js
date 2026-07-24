const AUTO_ADVANCE_INTERVAL = 7000;

const carousel = document.querySelector('[data-auth-carousel]');

if (carousel) {
    const slides = [...carousel.querySelectorAll('[data-auth-carousel-slide]')];
    const dots = [...carousel.querySelectorAll('[data-auth-carousel-dot]')];
    const previousButton = carousel.querySelector('[data-auth-carousel-prev]');
    const nextButton = carousel.querySelector('[data-auth-carousel-next]');
    const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)');

    let activeIndex = 0;
    let autoplayTimerId;
    let manualNavigation = false;

    function updateSlides(nextIndex) {
        activeIndex = (nextIndex + slides.length) % slides.length;

        slides.forEach((slide, index) => {
            const isActive = index === activeIndex;
            slide.classList.toggle('opacity-100', isActive);
            slide.classList.toggle('opacity-0', !isActive);
        });

        dots.forEach((dot, index) => {
            const isActive = index === activeIndex;
            dot.setAttribute('aria-pressed', String(isActive));
            dot.classList.toggle('bg-white', isActive);
            dot.classList.toggle('bg-white/50', !isActive);
            dot.classList.toggle('ring-2', isActive);
            dot.classList.toggle('ring-primary', isActive);
            dot.classList.toggle('ring-offset-2', isActive);
            dot.classList.toggle('ring-offset-sidebar', isActive);
        });
    }

    function stopAutoplay() {
        if (!autoplayTimerId) {
            return;
        }

        window.clearInterval(autoplayTimerId);
        autoplayTimerId = undefined;
    }

    function startAutoplay() {
        if (slides.length < 2 || manualNavigation || reducedMotion.matches || autoplayTimerId) {
            return;
        }

        autoplayTimerId = window.setInterval(() => {
            updateSlides(activeIndex + 1);
        }, AUTO_ADVANCE_INTERVAL);
    }

    function selectSlide(nextIndex, isManual) {
        if (isManual) {
            manualNavigation = true;
            stopAutoplay();
        }

        updateSlides(nextIndex);
    }

    previousButton?.addEventListener('click', () => {
        selectSlide(activeIndex - 1, true);
    });

    nextButton?.addEventListener('click', () => {
        selectSlide(activeIndex + 1, true);
    });

    dots.forEach((dot) => {
        dot.addEventListener('click', () => {
            selectSlide(Number(dot.dataset.authCarouselDot), true);
        });
    });

    reducedMotion.addEventListener('change', () => {
        stopAutoplay();
        startAutoplay();
    });

    document.addEventListener('visibilitychange', () => {
        if (document.hidden) {
            stopAutoplay();
            return;
        }

        startAutoplay();
    });

    startAutoplay();
}
