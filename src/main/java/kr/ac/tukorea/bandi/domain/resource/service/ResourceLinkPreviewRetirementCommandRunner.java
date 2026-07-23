package kr.ac.tukorea.bandi.domain.resource.service;

import kr.ac.tukorea.bandi.global.config.ProfilePhotoRetirementMode;
import kr.ac.tukorea.bandi.global.config.ResourceLinkPreviewRetirementProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * RESOURCE_LINK_PREVIEW_RETIREMENT_MODE=APPLY일 때만 링크 카드 이미지 파기를 재시도한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResourceLinkPreviewRetirementCommandRunner implements ApplicationRunner {

    private final ResourceLinkPreviewRetirementProperties properties;
    private final ResourceLinkPreviewRetirementService retirementService;

    @Override
    public void run(ApplicationArguments args) {
        if (properties.mode() != ProfilePhotoRetirementMode.APPLY) {
            return;
        }
        retirementService.processUncompleted();
        log.info("자료 링크 카드 이미지 퇴역 재시도 실행 완료");
    }
}
