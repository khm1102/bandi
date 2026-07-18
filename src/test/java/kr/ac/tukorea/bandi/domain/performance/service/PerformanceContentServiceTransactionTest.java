package kr.ac.tukorea.bandi.domain.performance.service;

import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.domain.member.mapper.CohortMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.MemberMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.TeamMapper;
import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.Cohort;
import kr.ac.tukorea.bandi.domain.member.model.CohortTerm;
import kr.ac.tukorea.bandi.domain.member.model.Member;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.SsoLinkStatus;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceCastChangeParam;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PublicProfileViewResponse;
import kr.ac.tukorea.bandi.domain.performance.mapper.PerformanceContentMapper;
import kr.ac.tukorea.bandi.domain.performance.mapper.PerformanceProjectMapper;
import kr.ac.tukorea.bandi.domain.performance.mapper.PublicProfileMapper;
import kr.ac.tukorea.bandi.domain.performance.model.CastType;
import kr.ac.tukorea.bandi.domain.performance.model.CharacterImportance;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceCast;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceCharacter;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceProject;
import kr.ac.tukorea.bandi.domain.performance.model.PublicProfile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@SpringBootTest
@ActiveProfiles("test")
class PerformanceContentServiceTransactionTest {

    private final PerformanceContentService service;
    private final PerformanceProjectMapper projectMapper;
    private final PublicProfileMapper profileMapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final MemberMapper memberMapper;
    private final JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private PerformanceContentMapper contentMapper;
    @MockitoBean
    private MemberService memberService;
    @MockitoBean
    private PerformanceProjectService projectService;
    @MockitoBean
    private PerformancePublicPageService publicPageService;
    @MockitoBean
    private PublicProfileService publicProfileService;
    @MockitoBean
    private FileService fileService;
    @MockitoBean
    private Clock clock;

    private Long cohortId;
    private Long adminId;
    private Long projectId;
    private Long characterId;
    private Long castId;
    private Long currentProfileId;
    private Long newProfileId;

    @Autowired
    PerformanceContentServiceTransactionTest(
            PerformanceContentService service,
            PerformanceProjectMapper projectMapper,
            PublicProfileMapper profileMapper, TeamMapper teamMapper,
            CohortMapper cohortMapper, MemberMapper memberMapper,
            JdbcTemplate jdbcTemplate) {
        this.service = service;
        this.projectMapper = projectMapper;
        this.profileMapper = profileMapper;
        this.teamMapper = teamMapper;
        this.cohortMapper = cohortMapper;
        this.memberMapper = memberMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void setUp() {
        given(clock.instant()).willReturn(
                Instant.parse("2026-08-02T03:00:00Z"));
        given(clock.getZone()).willReturn(ZoneId.of("Asia/Seoul"));
        Long teamId = teamMapper.searchAll().get(0).getTeamId();
        Cohort cohort = new Cohort(null, "콘텐츠트랜잭션", (short) 2995,
                CohortTerm.FIRST, true);
        cohortMapper.insert(cohort);
        cohortId = cohort.getCohortId();
        Member admin = new Member(null, "2995000001", "콘텐츠운영진",
                null, null, null, teamId, cohortId, ClubRole.ADMIN,
                MemberStatus.ACTIVE, SsoLinkStatus.LINKED,
                null, null, null);
        memberMapper.insert(admin);
        adminId = admin.getMemberId();
        given(memberService.lookupAccessContext(adminId))
                .willReturn(new MemberAccessContext(
                        adminId, teamId, true, false, true));

        PerformanceProject project = PerformanceProject.planning(
                (short) 2995, "FIRST", "콘텐츠 트랜잭션 공연",
                LocalDate.of(2995, 3, 1), LocalDate.of(2995, 6, 30),
                "소극장", adminId);
        projectMapper.insert(project);
        projectId = project.getPerformanceProjectId();
        currentProfileId = insertProfile("배우 A");
        newProfileId = insertProfile("배우 B");
        PerformanceCharacter character = PerformanceCharacter.create(
                projectId, "햄릿", "덴마크의 왕자",
                CharacterImportance.LEAD, 0);
        contentMapper.insertCharacter(character);
        characterId = character.getPerformanceCharacterId();
        PerformanceCast cast = PerformanceCast.assign(projectId,
                characterId, currentProfileId, CastType.PRIMARY, 0);
        contentMapper.insertCast(cast);
        castId = cast.getPerformanceCastId();
        given(publicProfileService.lookupPublicCandidate(newProfileId))
                .willReturn(Optional.of(new PublicProfileViewResponse(
                        newProfileId, "배우 B", null, null, null)));
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM performance_cast_history");
        jdbcTemplate.update("DELETE FROM performance_cast");
        jdbcTemplate.update("DELETE FROM performance_character");
        jdbcTemplate.update("DELETE FROM public_profile");
        jdbcTemplate.update("DELETE FROM performance_project");
        jdbcTemplate.update("DELETE FROM member WHERE member_id = ?", adminId);
        jdbcTemplate.update("DELETE FROM cohort WHERE cohort_id = ?", cohortId);
    }

    @Test
    void 이력_저장이_실패하면_캐스팅_변경도_롤백한다() {
        willThrow(new IllegalStateException("이력 저장 실패"))
                .given(contentMapper).insertCastHistory(any());

        assertThatThrownBy(() -> service.changeCast(adminId,
                new PerformanceCastChangeParam(castId, newProfileId,
                        CastType.ALTERNATE, 1, "일정 변경")))
                .isInstanceOf(IllegalStateException.class);

        PerformanceCast cast = contentMapper.lookupCastForUpdate(castId)
                .orElseThrow();
        assertThat(cast.getPublicProfileId()).isEqualTo(currentProfileId);
        assertThat(cast.getCastType()).isEqualTo(CastType.PRIMARY);
    }

    private Long insertProfile(String name) {
        PublicProfile profile = PublicProfile.draft(
                null, name, "소개", null, null);
        profileMapper.insertProfile(profile);
        return profile.getPublicProfileId();
    }
}
