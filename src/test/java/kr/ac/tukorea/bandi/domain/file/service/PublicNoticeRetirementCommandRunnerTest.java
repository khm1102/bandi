package kr.ac.tukorea.bandi.domain.file.service;

import kr.ac.tukorea.bandi.domain.file.model.PublicNoticeRetirementReport;
import kr.ac.tukorea.bandi.global.config.PublicNoticeRetirementMode;
import kr.ac.tukorea.bandi.global.config.PublicNoticeRetirementProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class PublicNoticeRetirementCommandRunnerTest {

    @Test
    void OFF_모드에서는_퇴역_작업을_실행하지_않는다() throws Exception {
        PublicNoticeRetirementService retirementService = mock(PublicNoticeRetirementService.class);
        PublicNoticeRetirementCommandRunner runner = new PublicNoticeRetirementCommandRunner(
                new PublicNoticeRetirementProperties(PublicNoticeRetirementMode.OFF), retirementService);

        runner.run(new DefaultApplicationArguments());

        verify(retirementService, never()).report();
        verify(retirementService, never()).apply();
    }

    @Test
    void REPORT_모드에서는_파일_삭제_없이_현황만_조회한다() throws Exception {
        PublicNoticeRetirementService retirementService = mock(PublicNoticeRetirementService.class);
        given(retirementService.report()).willReturn(new PublicNoticeRetirementReport(1, 1, 0, 0, 0));
        PublicNoticeRetirementCommandRunner runner = new PublicNoticeRetirementCommandRunner(
                new PublicNoticeRetirementProperties(PublicNoticeRetirementMode.REPORT), retirementService);

        runner.run(new DefaultApplicationArguments());

        verify(retirementService).report();
        verify(retirementService, never()).apply();
    }

    @Test
    void APPLY_모드에서만_파일_삭제_작업을_실행한다() throws Exception {
        PublicNoticeRetirementService retirementService = mock(PublicNoticeRetirementService.class);
        given(retirementService.apply()).willReturn(new PublicNoticeRetirementReport(1, 0, 1, 0, 0));
        PublicNoticeRetirementCommandRunner runner = new PublicNoticeRetirementCommandRunner(
                new PublicNoticeRetirementProperties(PublicNoticeRetirementMode.APPLY), retirementService);

        runner.run(new DefaultApplicationArguments());

        verify(retirementService).apply();
        verify(retirementService, never()).report();
    }
}
