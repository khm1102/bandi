package kr.ac.tukorea.bandi.domain.audit.service;

import kr.ac.tukorea.bandi.domain.audit.mapper.AuditLogMapper;
import kr.ac.tukorea.bandi.domain.audit.model.AuditAction;
import kr.ac.tukorea.bandi.domain.audit.model.AuditLog;
import kr.ac.tukorea.bandi.domain.audit.model.AuditTargetType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogMapper auditLogMapper;

    private AuditService auditService;

    @BeforeEach
    void setUp() {
        auditService = new AuditService(auditLogMapper, Clock.fixed(
                Instant.parse("2026-07-19T10:00:00Z"),
                ZoneId.of("Asia/Seoul")));
    }

    @Test
    void 현재_시각으로_감사_기록을_저장한다() {
        auditService.record(1L, AuditAction.MEMBER_TEAM_CHANGED,
                AuditTargetType.MEMBER, 2L, "멤버 팀 변경");

        ArgumentCaptor<AuditLog> captor =
                ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogMapper).insert(captor.capture());
        assertThat(captor.getValue().getOccurredDttm())
                .isEqualTo(LocalDateTime.of(2026, 7, 19, 19, 0));
    }
}
