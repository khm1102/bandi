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
import kr.ac.tukorea.bandi.domain.notice.mapper.PublicNoticeMapper;
import kr.ac.tukorea.bandi.domain.notice.model.PublicNotice;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceProject;
import kr.ac.tukorea.bandi.global.annotation.MapperTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@MapperTest
class PerformancePublicNoticeMapperTest {

    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 8, 1, 12, 0);

    private final PerformancePublicNoticeMapper publicNoticeLinkMapper;
    private final PerformanceProjectMapper projectMapper;
    private final PublicNoticeMapper publicNoticeMapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final MemberMapper memberMapper;

    private Long adminId;
    private Long projectId;
    private Long publishedNoticeId;
    private Long draftNoticeId;

    @Autowired
    PerformancePublicNoticeMapperTest(
            PerformancePublicNoticeMapper publicNoticeLinkMapper,
            PerformanceProjectMapper projectMapper,
            PublicNoticeMapper publicNoticeMapper, TeamMapper teamMapper,
            CohortMapper cohortMapper, MemberMapper memberMapper) {
        this.publicNoticeLinkMapper = publicNoticeLinkMapper;
        this.projectMapper = projectMapper;
        this.publicNoticeMapper = publicNoticeMapper;
        this.teamMapper = teamMapper;
        this.cohortMapper = cohortMapper;
        this.memberMapper = memberMapper;
    }

    @BeforeEach
    void setUp() {
        insertAdmin();
        insertProject();
        publishedNoticeId = insertNotice("게시 공시", true);
        draftNoticeId = insertNotice("초안 공시", false);
    }

    @Test
    void 프로젝트의_연결_공시와_현재_공개_공시를_구분해_조회한다() {
        publicNoticeLinkMapper.insert(projectId, publishedNoticeId);
        publicNoticeLinkMapper.insert(projectId, draftNoticeId);

        assertThat(publicNoticeLinkMapper.searchNoticeIds(projectId))
                .containsExactlyInAnyOrder(publishedNoticeId, draftNoticeId);
        assertThat(publicNoticeLinkMapper.searchPublicNoticeIds(
                projectId, NOW)).containsExactly(publishedNoticeId);
        assertThat(publicNoticeLinkMapper.exists(
                projectId, publishedNoticeId)).isTrue();
    }

    @Test
    void 같은_프로젝트와_공시는_한_번만_연결하고_해제할_수_있다() {
        publicNoticeLinkMapper.insert(projectId, publishedNoticeId);

        assertThatThrownBy(() -> publicNoticeLinkMapper.insert(
                projectId, publishedNoticeId))
                .isInstanceOf(DataAccessException.class);
        assertThat(publicNoticeLinkMapper.remove(
                projectId, publishedNoticeId)).isEqualTo(1);
        assertThat(publicNoticeLinkMapper.exists(
                projectId, publishedNoticeId)).isFalse();
    }

    private void insertAdmin() {
        Long teamId = teamMapper.searchAll().get(0).getTeamId();
        Cohort cohort = new Cohort(null, "26-공시연결", (short) 2027,
                CohortTerm.FIRST, true);
        cohortMapper.insert(cohort);
        Member admin = new Member(null, "2027000931", "공시 연결 운영진",
                null, null, null, teamId, cohort.getCohortId(),
                ClubRole.ADMIN, MemberStatus.ACTIVE,
                SsoLinkStatus.LINKED, null, null, null);
        memberMapper.insert(admin);
        adminId = admin.getMemberId();
    }

    private void insertProject() {
        PerformanceProject project = PerformanceProject.planning(
                (short) 2027, "FIRST", "오셀로",
                LocalDate.of(2027, 3, 1),
                LocalDate.of(2027, 6, 30), "소극장", adminId);
        projectMapper.insert(project);
        projectId = project.getPerformanceProjectId();
    }

    private Long insertNotice(String title, boolean publish) {
        PublicNotice notice = PublicNotice.draft("SHOW", title,
                "공연 관련 안내", false, adminId);
        publicNoticeMapper.insert(notice);
        if (publish) {
            notice = notice.publish(NOW.minusDays(1), NOW.plusDays(1),
                    adminId, NOW.minusDays(1));
            publicNoticeMapper.update(notice);
        }
        return notice.getPublicNoticeId();
    }
}
