package kr.ac.tukorea.bandi.domain.audit.mapper;

import kr.ac.tukorea.bandi.domain.audit.model.AuditAction;
import kr.ac.tukorea.bandi.domain.audit.model.AuditLog;
import kr.ac.tukorea.bandi.domain.audit.model.AuditTargetType;
import kr.ac.tukorea.bandi.domain.member.mapper.CohortMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.MemberMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.TeamMapper;
import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.Cohort;
import kr.ac.tukorea.bandi.domain.member.model.CohortTerm;
import kr.ac.tukorea.bandi.domain.member.model.Member;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.SsoLinkStatus;
import kr.ac.tukorea.bandi.global.annotation.MapperTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@MapperTest
class AuditLogMapperTest {

    private final AuditLogMapper auditLogMapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final MemberMapper memberMapper;

    @Autowired
    AuditLogMapperTest(AuditLogMapper auditLogMapper,
                       TeamMapper teamMapper, CohortMapper cohortMapper,
                       MemberMapper memberMapper) {
        this.auditLogMapper = auditLogMapper;
        this.teamMapper = teamMapper;
        this.cohortMapper = cohortMapper;
        this.memberMapper = memberMapper;
    }

    @Test
    void 대상과_발생_시각으로_감사_기록을_저장하고_조회한다() {
        Long actorId = insertActor();
        AuditLog log = AuditLog.record(actorId,
                AuditAction.MEMBER_STATUS_CHANGED,
                AuditTargetType.MEMBER, actorId, "멤버 상태 변경",
                LocalDateTime.of(2026, 7, 19, 19, 5));

        auditLogMapper.insert(log);

        assertThat(log.getAuditLogId()).isNotNull();
        assertThat(auditLogMapper.searchByTarget(
                AuditTargetType.MEMBER, actorId)).singleElement()
                .extracting(AuditLog::getAction, AuditLog::getSummary)
                .containsExactly(AuditAction.MEMBER_STATUS_CHANGED,
                        "멤버 상태 변경");
    }

    private Long insertActor() {
        Long teamId = teamMapper.searchAll().get(0).getTeamId();
        Cohort cohort = new Cohort(null, "26-감사", (short) 2027,
                CohortTerm.FIRST, true);
        cohortMapper.insert(cohort);
        Member actor = new Member(null, "2027000941", "감사 운영진",
                null, null, null, teamId, cohort.getCohortId(),
                ClubRole.ADMIN, MemberStatus.ACTIVE,
                SsoLinkStatus.LINKED, null, null, null);
        memberMapper.insert(actor);
        return actor.getMemberId();
    }
}
