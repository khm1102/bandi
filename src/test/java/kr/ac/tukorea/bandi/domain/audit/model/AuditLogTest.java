package kr.ac.tukorea.bandi.domain.audit.model;

import kr.ac.tukorea.bandi.domain.audit.exception.InvalidAuditLogException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuditLogTest {

    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 7, 19, 19, 0);

    @Test
    void 필수_정보로_감사_기록을_생성한다() {
        AuditLog log = AuditLog.record(1L,
                AuditAction.MEMBER_ROLE_CHANGED,
                AuditTargetType.MEMBER, 2L, "멤버 권한 변경", NOW);

        assertThat(log.getActorMemberId()).isEqualTo(1L);
        assertThat(log.getAction())
                .isEqualTo(AuditAction.MEMBER_ROLE_CHANGED);
        assertThat(log.getMetadataJson()).isNull();
        assertThat(log.getOccurredDttm()).isEqualTo(NOW);
    }

    @Test
    void 필수_정보가_없거나_요약이_너무_길면_거부한다() {
        assertThatThrownBy(() -> AuditLog.record(null,
                AuditAction.MEMBER_ROLE_CHANGED,
                AuditTargetType.MEMBER, 2L, "멤버 권한 변경", NOW))
                .isInstanceOf(InvalidAuditLogException.class);
        assertThatThrownBy(() -> AuditLog.record(1L,
                AuditAction.MEMBER_ROLE_CHANGED,
                AuditTargetType.MEMBER, 2L, "가".repeat(501), NOW))
                .isInstanceOf(InvalidAuditLogException.class);
    }
}
