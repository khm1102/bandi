package kr.ac.tukorea.bandi.domain.file.service;

import kr.ac.tukorea.bandi.global.config.ProfilePhotoRetirementMode;
import kr.ac.tukorea.bandi.global.config.ProfilePhotoRetirementProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * PROFILE_PHOTO_RETIREMENT_MODE=APPLY일 때만 남은 프로필 사진 파기를 재시도한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProfilePhotoRetirementCommandRunner implements ApplicationRunner {

    private final ProfilePhotoRetirementProperties properties;
    private final ProfilePhotoRetirementService retirementService;

    @Override
    public void run(ApplicationArguments args) {
        if (properties.mode() != ProfilePhotoRetirementMode.APPLY) {
            return;
        }
        retirementService.processUncompleted();
        log.info("프로필 사진 퇴역 재시도 실행 완료");
    }
}
