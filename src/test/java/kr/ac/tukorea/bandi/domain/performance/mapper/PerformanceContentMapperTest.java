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
import kr.ac.tukorea.bandi.domain.performance.model.CastAction;
import kr.ac.tukorea.bandi.domain.performance.model.CastType;
import kr.ac.tukorea.bandi.domain.performance.model.CharacterImportance;
import kr.ac.tukorea.bandi.domain.performance.model.MediaType;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceCast;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceCastHistory;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceCharacter;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceMedia;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceProject;
import kr.ac.tukorea.bandi.domain.performance.model.PublicProfile;
import kr.ac.tukorea.bandi.domain.performance.model.ProductionCredit;
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
class PerformanceContentMapperTest {

    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 8, 2, 14, 0);

    private final PerformanceContentMapper contentMapper;
    private final PerformanceProjectMapper projectMapper;
    private final PublicProfileMapper profileMapper;
    private final StoredFileMapper storedFileMapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final MemberMapper memberMapper;
    private final JdbcTemplate jdbcTemplate;

    private Long adminId;
    private PerformanceProject project;
    private Long profileId;
    private Long secondProfileId;
    private Long publicFileId;

    @Autowired
    PerformanceContentMapperTest(
            PerformanceContentMapper contentMapper,
            PerformanceProjectMapper projectMapper,
            PublicProfileMapper profileMapper,
            StoredFileMapper storedFileMapper, TeamMapper teamMapper,
            CohortMapper cohortMapper, MemberMapper memberMapper,
            JdbcTemplate jdbcTemplate) {
        this.contentMapper = contentMapper;
        this.projectMapper = projectMapper;
        this.profileMapper = profileMapper;
        this.storedFileMapper = storedFileMapper;
        this.teamMapper = teamMapper;
        this.cohortMapper = cohortMapper;
        this.memberMapper = memberMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void setUp() {
        Long teamId = teamMapper.searchAll().get(0).getTeamId();
        Cohort cohort = new Cohort(null, "26-콘텐츠", (short) 2028,
                CohortTerm.FIRST, true);
        cohortMapper.insert(cohort);
        Member admin = new Member(null, "2028000931", "콘텐츠 운영진",
                null, null, null, teamId, cohort.getCohortId(),
                ClubRole.ADMIN, MemberStatus.ACTIVE,
                SsoLinkStatus.LINKED, null, null, null);
        memberMapper.insert(admin);
        adminId = admin.getMemberId();
        project = insertProject((short) 2028, "FIRST", "햄릿");
        profileId = insertProfile("배우 A");
        secondProfileId = insertProfile("배우 B");
        publicFileId = insertPublicFile();
    }

    @Test
    void 등장인물과_캐스팅과_이력을_저장하고_조회한다() {
        PerformanceCharacter character = insertCharacter(project, "햄릿");
        PerformanceCast cast = PerformanceCast.assign(
                project.getPerformanceProjectId(),
                character.getPerformanceCharacterId(), profileId,
                CastType.PRIMARY, 0);
        contentMapper.insertCast(cast);
        contentMapper.insertCastHistory(PerformanceCastHistory.project(
                project.getPerformanceProjectId(),
                character.getPerformanceCharacterId(), null, profileId,
                null, CastType.PRIMARY, CastAction.ASSIGN,
                "최초 배정", adminId, NOW));

        assertThat(contentMapper.existsCastByCharacter(
                character.getPerformanceCharacterId())).isTrue();
        assertThat(contentMapper.existsCastHistoryByCharacter(
                character.getPerformanceCharacterId())).isTrue();
        assertThat(contentMapper.searchCharacters(
                project.getPerformanceProjectId())).hasSize(1);
        assertThat(contentMapper.searchCasts(
                project.getPerformanceProjectId())).hasSize(1).first()
                .extracting("characterName", "publicProfileId")
                .containsExactly("햄릿", profileId);
        assertThat(contentMapper.searchCastHistories(
                project.getPerformanceProjectId())).hasSize(1);

        PerformanceCast changed = cast.change(secondProfileId,
                CastType.ALTERNATE, 1);
        contentMapper.updateCast(changed);
        assertThat(contentMapper.lookupCastForUpdate(
                cast.getPerformanceCastId())).isPresent().get()
                .extracting(PerformanceCast::getPublicProfileId)
                .isEqualTo(secondProfileId);
        contentMapper.removeCast(cast.getPerformanceCastId());
        assertThat(contentMapper.lookupCastForUpdate(
                cast.getPerformanceCastId())).isEmpty();
    }

    @Test
    void 캐스팅은_같은_프로젝트의_등장인물만_연결한다() {
        PerformanceCharacter character = insertCharacter(project, "햄릿");
        PerformanceProject other = insertProject(
                (short) 2028, "SECOND", "리어왕");

        assertThatThrownBy(() -> contentMapper.insertCast(
                PerformanceCast.assign(other.getPerformanceProjectId(),
                        character.getPerformanceCharacterId(), profileId,
                        CastType.PRIMARY, 0)))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void 같은_프로젝트_배역_프로필_캐스팅은_중복할_수_없다() {
        PerformanceCharacter character = insertCharacter(project, "햄릿");
        contentMapper.insertCast(PerformanceCast.assign(
                project.getPerformanceProjectId(),
                character.getPerformanceCharacterId(), profileId,
                CastType.PRIMARY, 0));

        assertThatThrownBy(() -> contentMapper.insertCast(
                PerformanceCast.assign(project.getPerformanceProjectId(),
                        character.getPerformanceCharacterId(), profileId,
                        CastType.UNDERSTUDY, 1)))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void 제작진_크레딧과_미디어를_저장_수정_삭제한다() {
        ProductionCredit credit = ProductionCredit.create(
                project.getPerformanceProjectId(), "연출", "김연출",
                profileId, 0);
        contentMapper.insertCredit(credit);
        contentMapper.updateCredit(credit.edit(
                "총연출", "김연출", profileId, 1));
        assertThat(contentMapper.searchCredits(
                project.getPerformanceProjectId())).hasSize(1).first()
                .extracting("creditRole").isEqualTo("총연출");

        PerformanceMedia media = PerformanceMedia.create(
                project.getPerformanceProjectId(), publicFileId,
                MediaType.REHEARSAL, "연습 사진", "1막 연습",
                "배우들이 연습하는 모습", "촬영 영상팀", null, 0);
        contentMapper.insertMedia(media);
        contentMapper.updateMedia(media.changePublished(true));
        assertThat(contentMapper.searchMedia(
                project.getPerformanceProjectId(), true)).hasSize(1);

        contentMapper.removeCredit(credit.getProductionCreditId());
        contentMapper.removeMedia(media.getPerformanceMediaId());
        assertThat(contentMapper.searchCredits(
                project.getPerformanceProjectId())).isEmpty();
        assertThat(contentMapper.searchMedia(
                project.getPerformanceProjectId(), false)).isEmpty();
    }

    @Test
    void DB는_이력_액션과_미디어_URL_대체텍스트를_제약한다() {
        PerformanceCharacter character = insertCharacter(project, "햄릿");

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO performance_cast_history (
                    performance_project_id, performance_character_id,
                    previous_public_profile_id, new_public_profile_id,
                    scope_code, action_code,
                    changed_by_member_id, changed_dttm
                ) VALUES (?, ?, NULL, NULL, 'PROJECT', 'CHANGE', ?, ?)
                """, project.getPerformanceProjectId(),
                character.getPerformanceCharacterId(), adminId, NOW))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO performance_media (
                    performance_project_id, stored_file_id,
                    media_type_code, title, description, alt_text,
                    credit_text, external_url, display_order, is_published
                ) VALUES (?, ?, 'VIDEO', '영상', '설명', '', '영상팀',
                          'javascript:alert(1)', 0, 0)
                """, project.getPerformanceProjectId(), publicFileId))
                .isInstanceOf(DataAccessException.class);
    }

    private PerformanceProject insertProject(short year, String term,
                                             String title) {
        PerformanceProject created = PerformanceProject.planning(
                year, term, title, LocalDate.of(year, 3, 1),
                LocalDate.of(year, 6, 30), "소극장", adminId);
        projectMapper.insert(created);
        return created;
    }

    private Long insertProfile(String name) {
        PublicProfile profile = PublicProfile.draft(
                null, name, "소개", null, null);
        profileMapper.insertProfile(profile);
        return profile.getPublicProfileId();
    }

    private Long insertPublicFile() {
        StoredFile file = StoredFile.pending("rehearsal.png",
                StorageScope.PUBLIC,
                "performance/2026/08/rehearsal-test", "image/png", 10L,
                "b".repeat(64), adminId);
        storedFileMapper.insert(file);
        storedFileMapper.updateReady(file.getStoredFileId(), "etag-media");
        return file.getStoredFileId();
    }

    private PerformanceCharacter insertCharacter(
            PerformanceProject targetProject, String name) {
        PerformanceCharacter character = PerformanceCharacter.create(
                targetProject.getPerformanceProjectId(), name,
                "인물 소개", CharacterImportance.LEAD, 0);
        contentMapper.insertCharacter(character);
        return character;
    }
}
