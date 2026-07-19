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
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceProject;
import kr.ac.tukorea.bandi.domain.performance.model.PerformancePublicPage;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceViewingGuide;
import kr.ac.tukorea.bandi.domain.performance.model.PublicPageStatus;
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
class PerformancePublicPageMapperTest {

    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 8, 1, 12, 0);

    private final PerformancePublicPageMapper pageMapper;
    private final PerformanceProjectMapper projectMapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final MemberMapper memberMapper;
    private final JdbcTemplate jdbcTemplate;

    private Long adminId;
    private PerformanceProject project;

    @Autowired
    PerformancePublicPageMapperTest(
            PerformancePublicPageMapper pageMapper,
            PerformanceProjectMapper projectMapper, TeamMapper teamMapper,
            CohortMapper cohortMapper, MemberMapper memberMapper,
            JdbcTemplate jdbcTemplate) {
        this.pageMapper = pageMapper;
        this.projectMapper = projectMapper;
        this.teamMapper = teamMapper;
        this.cohortMapper = cohortMapper;
        this.memberMapper = memberMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void setUp() {
        Long teamId = teamMapper.searchAll().get(0).getTeamId();
        Cohort cohort = new Cohort(null, "26-공개", (short) 2027,
                CohortTerm.FIRST, true);
        cohortMapper.insert(cohort);
        Member admin = new Member(null, "2027000921", "공연 공개 운영진",
                null, null, null, teamId, cohort.getCohortId(),
                ClubRole.ADMIN, MemberStatus.ACTIVE,
                SsoLinkStatus.LINKED, null, null, null);
        memberMapper.insert(admin);
        adminId = admin.getMemberId();
        project = insertProject((short) 2027, "FIRST", "햄릿");
    }

    @Test
    void 공개_페이지와_관람_안내를_저장하고_조회한다() {
        PerformancePublicPage page = insertPage(project,
                "hamlet-2027", null, null);
        PerformanceViewingGuide guide = insertGuide(project);
        pageMapper.updatePage(page.changeStatus(
                PublicPageStatus.PUBLISHED));

        assertThat(pageMapper.lookupPageByIdForUpdate(
                page.getPerformancePublicPageId())).isPresent();
        assertThat(pageMapper.lookupPublicBySlug(
                "hamlet-2027", NOW)).isPresent().get()
                .extracting("projectTitle", "place")
                .containsExactly("햄릿", "소극장");
        assertThat(pageMapper.lookupPublicGuide(
                project.getPerformanceProjectId(), NOW)).isPresent();
        assertThat(pageMapper.lookupGuideByProject(
                project.getPerformanceProjectId())).isPresent().get()
                .extracting("entryPolicy", "directions")
                .containsExactly("공연 30분 전 입장", "정문");
        assertThat(pageMapper.searchPages()).hasSize(1);

        pageMapper.updateGuide(guide.edit("공연 40분 전 입장",
                "지연 입장 제한", "촬영 금지", "전날까지 취소",
                "휠체어 접근 가능", "정문", "교내 주차장"));
        assertThat(pageMapper.lookupGuideByProjectForUpdate(
                project.getPerformanceProjectId())).isPresent().get()
                .extracting(PerformanceViewingGuide::getEntryPolicy)
                .isEqualTo("공연 40분 전 입장");
    }

    @Test
    void 프로젝트와_슬러그는_각각_하나의_공개_페이지만_허용한다() {
        insertPage(project, "hamlet-2027", null, null);

        assertThatThrownBy(() -> insertPage(
                project, "other-slug", null, null))
                .isInstanceOf(DataAccessException.class);

        PerformanceProject other = insertProject(
                (short) 2027, "SECOND", "리어왕");
        assertThatThrownBy(() -> insertPage(
                other, "hamlet-2027", null, null))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void 예약_공개는_시작부터_종료_직전까지만_외부에_노출한다() {
        PerformancePublicPage page = insertPage(project,
                "scheduled-show", NOW.plusHours(1), NOW.plusHours(2));
        pageMapper.updatePage(page.changeStatus(
                PublicPageStatus.SCHEDULED));

        assertThat(pageMapper.lookupPublicBySlug(
                "scheduled-show", NOW)).isEmpty();
        assertThat(pageMapper.lookupPublicBySlug(
                "scheduled-show", NOW.plusHours(1))).isPresent();
        assertThat(pageMapper.lookupPublicBySlug(
                "scheduled-show", NOW.plusHours(2))).isEmpty();
    }

    @Test
    void 종료_취소_보관_페이지는_공개_종료_시각이_지나도_기록으로_남는다() {
        PerformancePublicPage page = insertPage(project,
                "archive-show", NOW.minusDays(2), NOW.minusDays(1));
        page = page.changeStatus(PublicPageStatus.PUBLISHED)
                .changeStatus(PublicPageStatus.ENDED)
                .changeStatus(PublicPageStatus.ARCHIVED);
        pageMapper.updatePage(page);

        assertThat(pageMapper.lookupPublicBySlug(
                "archive-show", NOW)).isPresent();
    }

    @Test
    void DB는_슬러그_상태_기간과_관람_안내_필수값을_제약한다() {
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO performance_public_page (
                    performance_project_id, slug, status_code,
                    short_description, synopsis, genre, age_rating,
                    runtime_minutes, admission_fee, contact_name,
                    contact_channel, organizer_name
                ) VALUES (?, 'Bad Slug', 'DRAFT', '소개', '시놉시스',
                          '비극', '12세', 120, 0, '문의', '채널', 'Bandi')
                """, project.getPerformanceProjectId()))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO performance_viewing_guide (
                    performance_project_id, entry_policy,
                    late_entry_policy, recording_policy,
                    cancellation_policy, accessibility_policy
                ) VALUES (?, '', '지연', '촬영', '취소', '접근성')
                """, project.getPerformanceProjectId()))
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

    private PerformancePublicPage insertPage(
            PerformanceProject targetProject, String slug,
            LocalDateTime publishStartDttm,
            LocalDateTime publishEndDttm) {
        PerformancePublicPage page = PerformancePublicPage.draft(
                targetProject.getPerformanceProjectId(), slug,
                "짧은 소개", "상세 시놉시스", "연출 의도", "비극",
                "12세 이상", 120, 15, 0L, null, null, "#0F6F5D",
                "공연 문의", "bandi@example.com", "Bandi",
                "공연 OG", "공연 소개", null,
                publishStartDttm, publishEndDttm);
        pageMapper.insertPage(page);
        return page;
    }

    private PerformanceViewingGuide insertGuide(
            PerformanceProject targetProject) {
        PerformanceViewingGuide guide = PerformanceViewingGuide.create(
                targetProject.getPerformanceProjectId(),
                "공연 30분 전 입장", "지연 입장 제한", "촬영 금지",
                "전날까지 취소", "휠체어 접근 가능", "정문", "교내 주차장");
        pageMapper.insertGuide(guide);
        return guide;
    }
}
