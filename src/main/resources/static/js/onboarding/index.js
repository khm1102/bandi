const onboarding = document.querySelector('[data-onboarding]');

if (onboarding) {
    const stepTitles = [
        '반디를 먼저 살펴봐요',
        '내 정보를 확인해요',
        '일정과 공지를 확인해요',
        '자료와 활동 기록을 알아봐요',
        '소품과 장비를 확인해요',
    ];
    const slides = [...onboarding.querySelectorAll('[data-onboarding-slide]')];
    const stepButtons = [...onboarding.querySelectorAll('[data-onboarding-step]')];
    const previousButton = onboarding.querySelector('[data-onboarding-action="previous"]');
    const nextButton = onboarding.querySelector('[data-onboarding-action="next"]');
    const completeLink = onboarding.querySelector('[data-onboarding-complete]');
    const progress = onboarding.querySelector('[data-onboarding-progress]');
    const status = onboarding.querySelector('[data-onboarding-status]');
    const progressClasses = ['scale-x-20', 'scale-x-40', 'scale-x-60', 'scale-x-80', 'scale-x-100'];

    let activeIndex = 0;

    function updateStepButton(button, isActive) {
        button.setAttribute('aria-current', isActive ? 'step' : 'false');
        button.classList.toggle('border-primary', isActive);
        button.classList.toggle('bg-primary', isActive);
        button.classList.toggle('text-primary-foreground', isActive);
        button.classList.toggle('bg-card', !isActive);
        button.classList.toggle('text-muted-foreground', !isActive);
    }

    function render(index) {
        activeIndex = Math.max(0, Math.min(index, slides.length - 1));
        const isLastSlide = activeIndex === slides.length - 1;

        slides.forEach((slide, slideIndex) => {
            const isActive = slideIndex === activeIndex;
            slide.hidden = !isActive;
            slide.classList.toggle('hidden', !isActive);
            slide.setAttribute('aria-hidden', String(!isActive));
        });

        stepButtons.forEach((button, buttonIndex) => {
            updateStepButton(button, buttonIndex === activeIndex);
        });

        previousButton.disabled = activeIndex === 0;
        nextButton.hidden = isLastSlide;
        nextButton.textContent = activeIndex === slides.length - 2 ? '마지막 안내' : '다음 안내';
        completeLink.hidden = !isLastSlide;
        progress.classList.remove(...progressClasses);
        progress.classList.add(progressClasses[activeIndex]);
        status.textContent = `${activeIndex + 1} / ${slides.length} · ${stepTitles[activeIndex]}`;
    }

    previousButton.addEventListener('click', () => {
        render(activeIndex - 1);
    });

    nextButton.addEventListener('click', () => {
        render(activeIndex + 1);
    });

    stepButtons.forEach((button) => {
        button.addEventListener('click', () => {
            render(Number(button.dataset.onboardingIndex));
        });
    });

    render(0);
}
