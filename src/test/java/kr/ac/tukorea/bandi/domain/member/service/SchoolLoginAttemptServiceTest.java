package kr.ac.tukorea.bandi.domain.member.service;

import kr.ac.tukorea.bandi.domain.member.exception.SchoolLoginRateLimitedException;
import kr.ac.tukorea.bandi.domain.member.mapper.SchoolLoginAttemptMapper;
import kr.ac.tukorea.bandi.domain.member.model.SchoolLoginAttempt;
import kr.ac.tukorea.bandi.global.config.SchoolLoginAttemptProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SchoolLoginAttemptServiceTest {

    private static final String STUDENT_NO = "2021184000";
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 25, 10, 0);

    @Mock
    private SchoolLoginAttemptMapper schoolLoginAttemptMapper;

    private SchoolLoginAttemptService schoolLoginAttemptService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW.atZone(ZoneId.of("Asia/Seoul")).toInstant(),
                ZoneId.of("Asia/Seoul"));
        SchoolLoginAttemptProperties properties = new SchoolLoginAttemptProperties(
                5, Duration.ofMinutes(15), Duration.ofMinutes(15), "test-hmac-secret");
        schoolLoginAttemptService = new SchoolLoginAttemptService(
                schoolLoginAttemptMapper, properties, clock);
    }

    @Test
    void 차단_중인_학번은_학교_SSO_호출_전에_거부한다() {
        given(schoolLoginAttemptMapper.lookupByStudentNoHashForUpdate(anyString()))
                .willReturn(Optional.of(new SchoolLoginAttempt("a".repeat(64), 5,
                        NOW.minusMinutes(4), NOW.plusMinutes(11))));

        assertThatThrownBy(() -> schoolLoginAttemptService.assertAllowed(STUDENT_NO))
                .isInstanceOf(SchoolLoginRateLimitedException.class);
    }

    @Test
    void 다섯번째_실패를_저장하면_대기_상태를_반환한다() {
        given(schoolLoginAttemptMapper.lookupByStudentNoHashForUpdate(anyString()))
                .willReturn(Optional.of(new SchoolLoginAttempt("a".repeat(64), 4,
                        NOW.minusMinutes(4), null)));

        boolean blocked = schoolLoginAttemptService.recordFailure(STUDENT_NO);

        ArgumentCaptor<SchoolLoginAttempt> captor =
                ArgumentCaptor.forClass(SchoolLoginAttempt.class);
        verify(schoolLoginAttemptMapper).update(captor.capture());
        assertThat(blocked).isTrue();
        assertThat(captor.getValue().failureCount()).isEqualTo(5);
        assertThat(captor.getValue().blockedUntilDttm()).isEqualTo(NOW.plusMinutes(15));
    }

    @Test
    void 정상_학교_인증을_마치면_실패_기록을_삭제한다() {
        schoolLoginAttemptService.clearFailures(STUDENT_NO);

        verify(schoolLoginAttemptMapper).removeByStudentNoHash(anyString());
    }
}
