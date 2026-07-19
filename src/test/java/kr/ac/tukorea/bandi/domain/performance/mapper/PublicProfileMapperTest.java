package kr.ac.tukorea.bandi.domain.performance.mapper;

import kr.ac.tukorea.bandi.domain.file.mapper.StoredFileMapper;
import kr.ac.tukorea.bandi.domain.file.model.StorageScope;
import kr.ac.tukorea.bandi.domain.file.model.StoredFile;
import kr.ac.tukorea.bandi.domain.member.mapper.CohortMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.MemberMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.TeamMapper;
import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.Cohort;
import kr.ac.tukorea.bandi.domain.member.model.CohortTerm;
import kr.ac.tukorea.bandi.domain.member.model.Member;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.SsoLinkStatus;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PublicProfileSearchCondition;
import kr.ac.tukorea.bandi.domain.performance.model.ConsentScope;
import kr.ac.tukorea.bandi.domain.performance.model.PublicProfile;
import kr.ac.tukorea.bandi.domain.performance.model.PublicProfileConsent;
import kr.ac.tukorea.bandi.domain.performance.model.PublicProfileVisibility;
import kr.ac.tukorea.bandi.domain.policy.mapper.PolicyMapper;
import kr.ac.tukorea.bandi.domain.policy.model.PolicyAudience;
import kr.ac.tukorea.bandi.domain.policy.model.PolicyDocument;
import kr.ac.tukorea.bandi.domain.policy.model.PolicyDocumentVersion;
import kr.ac.tukorea.bandi.domain.policy.model.PolicyType;
import kr.ac.tukorea.bandi.global.annotation.MapperTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@MapperTest
class PublicProfileMapperTest {

    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 7, 18, 23, 30);

    private final PublicProfileMapper profileMapper;
    private final PolicyMapper policyMapper;
    private final StoredFileMapper storedFileMapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final MemberMapper memberMapper;

    private Long adminId;
    private Long memberId;
    private Long publicFileId;
    private PolicyDocument policyDocument;

    @Autowired
    PublicProfileMapperTest(PublicProfileMapper profileMapper,
                            PolicyMapper policyMapper,
                            StoredFileMapper storedFileMapper,
                            TeamMapper teamMapper,
                            CohortMapper cohortMapper,
                            MemberMapper memberMapper) {
        this.profileMapper = profileMapper;
        this.policyMapper = policyMapper;
        this.storedFileMapper = storedFileMapper;
        this.teamMapper = teamMapper;
        this.cohortMapper = cohortMapper;
        this.memberMapper = memberMapper;
    }

    @BeforeEach
    void setUp() {
        Long teamId = teamMapper.searchAll().get(0).getTeamId();
        Cohort cohort = new Cohort(null, "26-프로필", (short) 2026,
                CohortTerm.SECOND, true);
        cohortMapper.insert(cohort);
        adminId = insertMember("2026000911", "프로필 운영진", teamId,
                cohort.getCohortId(), ClubRole.ADMIN);
        memberId = insertMember("2026000912", "출연 멤버", teamId,
                cohort.getCohortId(), ClubRole.MEMBER);
        publicFileId = insertPublicFile();
        policyDocument = PolicyDocument.create(PolicyType.PRIVACY,
                "공개 프로필 동의", PolicyAudience.MEMBER);
        policyMapper.insertDocument(policyDocument);
    }

    @Test
    void 내부와_외부_참여자_프로필을_저장하고_검색한다() {
        PublicProfile memberProfile = insertProfile(memberId);
        PublicProfile externalProfile = insertProfile(null);

        profileMapper.updateProfile(memberProfile.changeVisibility(
                PublicProfileVisibility.PUBLISHED));

        assertThat(profileMapper.lookupProfileByIdForUpdate(
                memberProfile.getPublicProfileId())).isPresent();
        assertThat(profileMapper.lookupPublishedById(
                memberProfile.getPublicProfileId())).isPresent();
        assertThat(profileMapper.lookupPublishedById(
                externalProfile.getPublicProfileId())).isEmpty();
        assertThat(profileMapper.searchProfiles(
                new PublicProfileSearchCondition(null, null, 0, 20)))
                .hasSize(2);
    }

    @Test
    void 같은_멤버에는_공개_프로필을_하나만_연결한다() {
        insertProfile(memberId);

        assertThatThrownBy(() -> insertProfile(memberId))
                .isInstanceOf(DataAccessException.class);
        assertThat(insertProfile(null).getPublicProfileId()).isNotNull();
        assertThat(insertProfile(null).getPublicProfileId()).isNotNull();
    }

    @Test
    void 항목별_최신_동의가_철회되면_과거_동의로_되돌아가지_않는다() {
        PublicProfile profile = insertProfile(memberId);
        PolicyDocumentVersion first = insertVersion(1, NOW, NOW);
        PolicyDocumentVersion second = insertVersion(
                2, NOW.plusMinutes(1), NOW.plusMinutes(1));
        profileMapper.insertConsent(PublicProfileConsent.agree(
                profile.getPublicProfileId(),
                first.getPolicyDocumentVersionId(), ConsentScope.NAME,
                adminId, NOW));
        profileMapper.insertConsent(PublicProfileConsent.agree(
                profile.getPublicProfileId(),
                first.getPolicyDocumentVersionId(), ConsentScope.BIO,
                adminId, NOW));
        PublicProfileConsent nameSecond = PublicProfileConsent.agree(
                profile.getPublicProfileId(),
                second.getPolicyDocumentVersionId(), ConsentScope.NAME,
                adminId, NOW.plusMinutes(1));
        profileMapper.insertConsent(nameSecond);
        profileMapper.updateConsent(nameSecond.revoke(
                adminId, NOW.plusMinutes(2)));

        assertThat(profileMapper.searchLatestAgreedScopes(
                profile.getPublicProfileId(), NOW.plusMinutes(3)))
                .containsExactly(ConsentScope.BIO);
    }

    @Test
    void 미래_효력_동의는_발효_전까지_현재_동의를_대체하지_않는다() {
        PublicProfile profile = insertProfile(memberId);
        PolicyDocumentVersion current = insertVersion(1, NOW, NOW);
        PolicyDocumentVersion future = insertVersion(
                2, NOW.plusMinutes(1), NOW.plusDays(1));
        profileMapper.insertConsent(PublicProfileConsent.agree(
                profile.getPublicProfileId(),
                current.getPolicyDocumentVersionId(), ConsentScope.NAME,
                adminId, NOW));
        PublicProfileConsent futureConsent = PublicProfileConsent.agree(
                profile.getPublicProfileId(),
                future.getPolicyDocumentVersionId(), ConsentScope.NAME,
                adminId, NOW.plusMinutes(1));
        profileMapper.insertConsent(futureConsent);
        profileMapper.updateConsent(futureConsent.revoke(
                adminId, NOW.plusMinutes(2)));

        assertThat(profileMapper.searchLatestAgreedScopes(
                profile.getPublicProfileId(), NOW.plusHours(1)))
                .containsExactly(ConsentScope.NAME);
        assertThat(profileMapper.searchLatestAgreedScopes(
                profile.getPublicProfileId(), NOW.plusDays(1)))
                .isEmpty();
    }

    @Test
    void 같은_정책_버전과_항목의_동의를_중복_기록할_수_없다() {
        PublicProfile profile = insertProfile(memberId);
        PolicyDocumentVersion version = insertVersion(1, NOW, NOW);
        PublicProfileConsent consent = PublicProfileConsent.agree(
                profile.getPublicProfileId(),
                version.getPolicyDocumentVersionId(), ConsentScope.NAME,
                adminId, NOW);
        profileMapper.insertConsent(consent);

        assertThatThrownBy(() -> profileMapper.insertConsent(
                PublicProfileConsent.agree(profile.getPublicProfileId(),
                        version.getPolicyDocumentVersionId(),
                        ConsentScope.NAME, adminId, NOW.plusSeconds(1))))
                .isInstanceOf(DataAccessException.class);
    }

    private Long insertMember(String studentNo, String name, Long teamId,
                              Long cohortId, ClubRole role) {
        Member member = new Member(null, studentNo, name, null, null,
                null, teamId, cohortId, role, MemberStatus.ACTIVE,
                SsoLinkStatus.LINKED, null, null, null);
        memberMapper.insert(member);
        return member.getMemberId();
    }

    private Long insertPublicFile() {
        StoredFile file = StoredFile.pending("profile.png",
                StorageScope.PUBLIC,
                "performance/2026/07/profile-test", "image/png", 10L,
                "a".repeat(64), adminId);
        storedFileMapper.insert(file);
        storedFileMapper.updateReady(file.getStoredFileId(), "etag-public");
        return file.getStoredFileId();
    }

    private PublicProfile insertProfile(Long linkedMemberId) {
        PublicProfile profile = PublicProfile.draft(linkedMemberId,
                linkedMemberId == null ? "외부 배우" : "출연 배우",
                "소개", publicFileId, "https://example.com/actor");
        profileMapper.insertProfile(profile);
        return profile;
    }

    private PolicyDocumentVersion insertVersion(
            int versionNo, LocalDateTime publishedDttm,
            LocalDateTime effectiveFromDttm) {
        PolicyDocumentVersion version = PolicyDocumentVersion.publish(
                policyDocument.getPolicyDocumentId(), versionNo,
                "동의 본문 " + versionNo, publishedDttm,
                effectiveFromDttm, true, adminId);
        policyMapper.insertVersion(version);
        return version;
    }
}
