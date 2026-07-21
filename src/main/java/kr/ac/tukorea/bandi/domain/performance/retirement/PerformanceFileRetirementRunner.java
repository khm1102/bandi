package kr.ac.tukorea.bandi.domain.performance.retirement;

import kr.ac.tukorea.bandi.domain.performance.retirement.service.PerformanceFileRetirementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "bandi.retirement.performance-files",
        name = "enabled", havingValue = "true")
public class PerformanceFileRetirementRunner implements ApplicationRunner {

    private final PerformanceFileRetirementProperties properties;
    private final PerformanceFileRetirementService retirementService;

    @Override
    public void run(ApplicationArguments args) {
        PerformanceFileRetirementResult result = switch (properties.mode()) {
            case REPORT -> retirementService.report();
            case APPLY -> retirementService.apply();
        };
        log.info("공연 파일 폐기 {} 완료 - candidates={}, pending={}, skipped={}, deleted={}",
                properties.mode(), result.candidateCount(), result.pendingCount(),
                result.skippedCount(), result.deletedCount());
    }
}
