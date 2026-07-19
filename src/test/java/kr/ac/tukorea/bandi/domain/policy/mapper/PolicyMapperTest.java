package kr.ac.tukorea.bandi.domain.policy.mapper;

import kr.ac.tukorea.bandi.domain.member.mapper.CohortMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.MemberMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.TeamMapper;
import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.Cohort;
import kr.ac.tukorea.bandi.domain.member.model.CohortTerm;
import kr.ac.tukorea.bandi.domain.member.model.Member;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.SsoLinkStatus;
import kr.ac.tukorea.bandi.domain.policy.model.PolicyAudience;
import kr.ac.tukorea.bandi.domain.policy.model.PolicyDocument;
import kr.ac.tukorea.bandi.domain.policy.model.PolicyDocumentVersion;
import kr.ac.tukorea.bandi.domain.policy.model.PolicyType;
import kr.ac.tukorea.bandi.global.annotation.MapperTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@MapperTest
class PolicyMapperTest {

    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 7, 18, 23, 0);

    private final PolicyMapper policyMapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final MemberMapper memberMapper;
    private final JdbcTemplate jdbcTemplate;

    private Long adminId;

    @Autowired
    PolicyMapperTest(PolicyMapper policyMapper, TeamMapper teamMapper,
                     CohortMapper cohortMapper, MemberMapper memberMapper,
                     JdbcTemplate jdbcTemplate) {
        this.policyMapper = policyMapper;
        this.teamMapper = teamMapper;
        this.cohortMapper = cohortMapper;
        this.memberMapper = memberMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void setUp() {
        Long teamId = teamMapper.searchAll().get(0).getTeamId();
        Cohort cohort = new Cohort(null, "26-정책", (short) 2026,
                CohortTerm.FIRST, true);
        cohortMapper.insert(cohort);
        Member admin = new Member(null, "2026000901", "정책 운영진",
                null, null, null, teamId, cohort.getCohortId(),
                ClubRole.ADMIN, MemberStatus.ACTIVE,
                SsoLinkStatus.LINKED, null, null, null);
        memberMapper.insert(admin);
        adminId = admin.getMemberId();
    }

    @Test
    void 정책_문서와_버전을_저장하고_순서대로_조회한다() {
        PolicyDocument document = insertDocument();
        PolicyDocumentVersion first = insertVersion(document, 1,
                NOW, NOW);
        PolicyDocumentVersion second = insertVersion(document, 2,
                NOW.plusMinutes(1), NOW.plusDays(1));

        assertThat(policyMapper.lookupDocumentByIdForUpdate(
                document.getPolicyDocumentId())).isPresent();
        assertThat(policyMapper.lookupVersionById(
                first.getPolicyDocumentVersionId())).isPresent();
        assertThat(policyMapper.lookupNextVersionNo(
                document.getPolicyDocumentId())).isEqualTo(3);
        assertThat(policyMapper.searchVersions(
                document.getPolicyDocumentId()))
                .extracting("policyDocumentVersionId")
                .containsExactly(second.getPolicyDocumentVersionId(),
                        first.getPolicyDocumentVersionId());
    }

    @Test
    void 활성이고_발행과_효력_시각이_지난_버전만_동의에_사용한다() {
        PolicyDocument document = insertDocument();
        PolicyDocumentVersion version = insertVersion(document, 1,
                NOW, NOW.plusHours(1));

        assertThat(policyMapper.existsEffectiveVersion(
                version.getPolicyDocumentVersionId(), NOW)).isFalse();
        assertThat(policyMapper.existsEffectiveVersion(
                version.getPolicyDocumentVersionId(), NOW.plusHours(1)))
                .isTrue();

        policyMapper.updateDocumentActive(
                document.getPolicyDocumentId(), false);
        assertThat(policyMapper.existsEffectiveVersion(
                version.getPolicyDocumentVersionId(), NOW.plusHours(1)))
                .isFalse();
    }

    @Test
    void 정책_유형까지_일치해야_해당_기능의_동의에_사용한다() {
        PolicyDocument document = insertDocument();
        PolicyDocumentVersion version = insertVersion(
                document, 1, NOW, NOW);

        assertThat(policyMapper.existsEffectiveVersionOfType(
                version.getPolicyDocumentVersionId(),
                PolicyType.PRIVACY, NOW)).isTrue();
        assertThat(policyMapper.existsEffectiveVersionOfType(
                version.getPolicyDocumentVersionId(),
                PolicyType.RESERVATION_PRIVACY, NOW)).isFalse();
    }

    @Test
    void 유형과_대상이_맞는_현재_최신_유효_버전을_조회한다() {
        PolicyDocument document = insertDocument(
                PolicyType.RESERVATION_PRIVACY, PolicyAudience.VISITOR);
        insertVersion(document, 1, NOW.minusDays(1), NOW.minusDays(1));
        PolicyDocumentVersion current = insertVersion(
                document, 2, NOW, NOW);
        insertVersion(document, 3, NOW.plusHours(1), NOW.plusHours(1));

        assertThat(policyMapper.lookupCurrentEffectiveVersion(
                PolicyType.RESERVATION_PRIVACY,
                PolicyAudience.VISITOR, NOW)).isPresent().get()
                .extracting("policyDocumentVersionId")
                .isEqualTo(current.getPolicyDocumentVersionId());
        assertThat(policyMapper.lookupCurrentEffectiveVersion(
                PolicyType.RESERVATION_PRIVACY,
                PolicyAudience.MEMBER, NOW)).isEmpty();
    }

    @Test
    void 같은_문서의_버전_번호는_중복할_수_없다() {
        PolicyDocument document = insertDocument();
        insertVersion(document, 1, NOW, NOW);

        assertThatThrownBy(() -> insertVersion(
                document, 1, NOW, NOW))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void DB는_정책_코드와_효력_시각을_제약한다() {
        PolicyDocument document = insertDocument();

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO policy_document (
                    policy_type_code, title, audience_code, is_active
                ) VALUES ('INVALID', '잘못된 정책', 'ALL', 1)
                """))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO policy_document_version (
                    policy_document_id, version_no, body,
                    published_dttm, published_by_member_id,
                    effective_from_dttm, is_required
                ) VALUES (?, 1, '본문', ?, ?, ?, 1)
                """, document.getPolicyDocumentId(), NOW, adminId,
                NOW.minusSeconds(1)))
                .isInstanceOf(DataAccessException.class);
    }

    private PolicyDocument insertDocument() {
        return insertDocument(PolicyType.PRIVACY, PolicyAudience.MEMBER);
    }

    private PolicyDocument insertDocument(
            PolicyType type, PolicyAudience audience) {
        PolicyDocument document = PolicyDocument.create(type,
                "정책 동의", audience);
        policyMapper.insertDocument(document);
        return document;
    }

    private PolicyDocumentVersion insertVersion(
            PolicyDocument document, int versionNo,
            LocalDateTime publishedDttm,
            LocalDateTime effectiveFromDttm) {
        PolicyDocumentVersion version = PolicyDocumentVersion.publish(
                document.getPolicyDocumentId(), versionNo, "동의 본문",
                publishedDttm, effectiveFromDttm, true, adminId);
        policyMapper.insertVersion(version);
        return version;
    }
}
