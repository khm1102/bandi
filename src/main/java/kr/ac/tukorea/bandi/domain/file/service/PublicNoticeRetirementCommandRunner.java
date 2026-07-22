package kr.ac.tukorea.bandi.domain.file.service;

import kr.ac.tukorea.bandi.domain.file.model.PublicNoticeRetirementReport;
import kr.ac.tukorea.bandi.global.config.PublicNoticeRetirementMode;
import kr.ac.tukorea.bandi.global.config.PublicNoticeRetirementProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PublicNoticeRetirementCommandRunner implements ApplicationRunner {

    private final PublicNoticeRetirementProperties properties;
    private final PublicNoticeRetirementService retirementService;

    @Override
    public void run(ApplicationArguments args) {
        if (properties.mode() == PublicNoticeRetirementMode.OFF) {
            return;
        }

        PublicNoticeRetirementReport report = properties.mode() == PublicNoticeRetirementMode.APPLY
                ? retirementService.apply()
                : retirementService.report();
        log.info("외부 공시 파일 퇴역 실행 결과 - mode={}, total={}, pending={}, deleted={}, retainedShared={}, failed={}",
                properties.mode(), report.totalCount(), report.pendingCount(), report.deletedCount(),
                report.retainedSharedCount(), report.failedCount());
    }
}
