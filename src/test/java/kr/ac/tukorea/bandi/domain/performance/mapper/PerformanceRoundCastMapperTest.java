package kr.ac.tukorea.bandi.domain.performance.mapper;

import kr.ac.tukorea.bandi.domain.member.mapper.CohortMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.MemberMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.TeamMapper;
import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.Cohort;
import kr.ac.tukorea.bandi.domain.member.model.CohortTerm;
import kr.ac.tukorea.bandi.domain.member.model.Member;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.SsoLinkStatus;
import kr.ac.tukorea.bandi.domain.performance.model.CastAction;
import kr.ac.tukorea.bandi.domain.performance.model.CastType;
import kr.ac.tukorea.bandi.domain.performance.model.CharacterImportance;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceCastHistory;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceCharacter;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceProject;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceRound;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceRoundCast;
import kr.ac.tukorea.bandi.domain.performance.model.PublicProfile;
import kr.ac.tukorea.bandi.global.annotation.MapperTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@MapperTest
class PerformanceRoundCastMapperTest {

    private static final LocalDateTime START =
            LocalDateTime.of(2033, 6, 21, 19, 0);
    private static final LocalDateTime NOW =
            LocalDateTime.of(2033, 6, 20, 17, 0);

    private final PerformanceRoundCastMapper roundCastMapper;
    private final PerformanceContentMapper contentMapper;
    private final PerformanceRoundMapper roundMapper;
    private final PerformanceProjectMapper projectMapper;
    private final PublicProfileMapper profileMapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final MemberMapper memberMapper;
    private final JdbcTemplate jdbcTemplate;

    private Long adminId;
    private PerformanceProject project;
    private PerformanceRound round;
    private PerformanceCharacter character;
    private Long profileId;
    private Long secondProfileId;

    @Autowired
    PerformanceRoundCastMapperTest(
            PerformanceRoundCastMapper roundCastMapper,
            PerformanceContentMapper contentMapper,
            PerformanceRoundMapper roundMapper,
            PerformanceProjectMapper projectMapper,
            PublicProfileMapper profileMapper, TeamMapper teamMapper,
            CohortMapper cohortMapper, MemberMapper memberMapper,
            JdbcTemplate jdbcTemplate) {
        this.roundCastMapper = roundCastMapper;
        this.contentMapper = contentMapper;
        this.roundMapper = roundMapper;
        this.projectMapper = projectMapper;
        this.profileMapper = profileMapper;
        this.teamMapper = teamMapper;
        this.cohortMapper = cohortMapper;
        this.memberMapper = memberMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void setUp() {
        Long teamId = teamMapper.searchAll().stream()
                .filter(team -> team.getName().equals("배우"))
                .findFirst().orElseThrow().getTeamId();
        Cohort cohort = new Cohort(null, "33-회차배우", (short) 2033,
                CohortTerm.FIRST, true);
        cohortMapper.insert(cohort);
        Member admin = new Member(null, "2033000001", "배우운영진",
                null, null, null, teamId, cohort.getCohortId(),
                ClubRole.ADMIN, MemberStatus.ACTIVE,
                SsoLinkStatus.LINKED, null, null, null);
        memberMapper.insert(admin);
        adminId = admin.getMemberId();
        project = insertProject((short) 2033, "FIRST", "햄릿");
        round = insertRound(project, 1, START);
        character = insertCharacter(project, "햄릿");
        profileId = insertProfile("배우 A");
        secondProfileId = insertProfile("배우 B");
    }

    @Test
    void 회차_캐스팅을_저장하고_조회하고_교체하고_제거한다() {
        PerformanceRoundCast cast = insertCast(
                project, round, character, profileId);

        assertThat(roundCastMapper.searchByRound(
                round.getPerformanceRoundId())).hasSize(1).first()
                .extracting("characterName", "publicProfileId")
                .containsExactly("햄릿", profileId);
        roundCastMapper.update(cast.change(
                secondProfileId, CastType.ALTERNATE));
        assertThat(roundCastMapper.lookupByIdForUpdate(
                cast.getPerformanceRoundCastId())).isPresent().get()
                .extracting(PerformanceRoundCast::getPublicProfileId)
                .isEqualTo(secondProfileId);
        roundCastMapper.remove(cast.getPerformanceRoundCastId());
        assertThat(roundCastMapper.lookupByIdForUpdate(
                cast.getPerformanceRoundCastId())).isEmpty();
    }

    @Test
    void 같은_회차의_하나의_배역에는_한_명만_배정한다() {
        insertCast(project, round, character, profileId);

        assertThatThrownBy(() -> insertCast(
                project, round, character, secondProfileId))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void 회차와_등장인물은_각각_같은_프로젝트에_속해야_한다() {
        PerformanceProject other = insertProject(
                (short) 2033, "SECOND", "리어왕");
        PerformanceRound otherRound = insertRound(
                other, 1, START.plusDays(1));
        PerformanceCharacter otherCharacter = insertCharacter(
                other, "리어");

        assertThatThrownBy(() -> insertCast(
                other, round, otherCharacter, profileId))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> insertCast(
                other, otherRound, character, profileId))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void ROUND_범위_캐스팅_이력을_저장한다() {
        contentMapper.insertCastHistory(PerformanceCastHistory.round(
                project.getPerformanceProjectId(),
                round.getPerformanceRoundId(),
                character.getPerformanceCharacterId(), null, profileId,
                null, CastType.PRIMARY, CastAction.ASSIGN,
                "최초 배정", adminId, NOW));

        assertThat(contentMapper.searchCastHistories(
                project.getPerformanceProjectId())).hasSize(1).first()
                .extracting("performanceRoundId", "action")
                .containsExactly(round.getPerformanceRoundId(),
                        CastAction.ASSIGN);
    }

    @Test
    void DB는_회차_캐스팅_유형을_제약한다() {
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO performance_round_cast (
                    performance_project_id, performance_round_id,
                    performance_character_id, public_profile_id,
                    cast_type_code
                ) VALUES (?, ?, ?, ?, 'GUEST')
                """, project.getPerformanceProjectId(),
                round.getPerformanceRoundId(),
                character.getPerformanceCharacterId(), profileId))
                .isInstanceOf(DataAccessException.class);
    }

    private PerformanceRoundCast insertCast(
            PerformanceProject targetProject,
            PerformanceRound targetRound,
            PerformanceCharacter targetCharacter,
            Long targetProfileId) {
        PerformanceRoundCast cast = PerformanceRoundCast.assign(
                targetProject.getPerformanceProjectId(),
                targetRound.getPerformanceRoundId(),
                targetCharacter.getPerformanceCharacterId(),
                targetProfileId, CastType.PRIMARY);
        roundCastMapper.insert(cast);
        return cast;
    }

    private PerformanceProject insertProject(short year, String term,
                                             String title) {
        PerformanceProject target = PerformanceProject.planning(
                year, term, title, LocalDate.of(year, 3, 1),
                LocalDate.of(year, 6, 30), "소극장", adminId);
        projectMapper.insert(target);
        return target;
    }

    private PerformanceRound insertRound(
            PerformanceProject targetProject, int roundNo,
            LocalDateTime startDttm) {
        PerformanceRound target = PerformanceRound.scheduled(
                targetProject.getPerformanceProjectId(), roundNo,
                startDttm, startDttm.minusMinutes(30),
                startDttm.minusDays(20), startDttm.minusDays(1));
        roundMapper.insertRound(target);
        return target;
    }

    private PerformanceCharacter insertCharacter(
            PerformanceProject targetProject, String name) {
        PerformanceCharacter target = PerformanceCharacter.create(
                targetProject.getPerformanceProjectId(), name,
                "인물 소개", CharacterImportance.LEAD, 0);
        contentMapper.insertCharacter(target);
        return target;
    }

    private Long insertProfile(String name) {
        PublicProfile profile = PublicProfile.draft(
                null, name, "소개", null, null);
        profileMapper.insertProfile(profile);
        return profile.getPublicProfileId();
    }
}
