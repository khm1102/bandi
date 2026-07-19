package kr.ac.tukorea.bandi.domain.performance.service;

import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeAdminDetailResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeDetailResponse;
import kr.ac.tukorea.bandi.domain.notice.model.PublicNoticeStatus;
import kr.ac.tukorea.bandi.domain.notice.service.PublicNoticeService;
import kr.ac.tukorea.bandi.domain.performance.dto.response.PerformancePublicPageResponse;
import kr.ac.tukorea.bandi.domain.performance.exception.PerformanceAccessDeniedException;
import kr.ac.tukorea.bandi.domain.performance.mapper.PerformancePublicNoticeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PerformancePublicNoticeServiceTest {

    private static final Long ACTOR_ID = 1L;
    private static final Long PROJECT_ID = 10L;
    private static final Long NOTICE_ID = 20L;
    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 7, 19, 18, 40);
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-07-19T09:40:00Z"),
            ZoneId.of("Asia/Seoul"));

    @Mock
    private PerformancePublicNoticeMapper publicNoticeMapper;
    @Mock
    private MemberService memberService;
    @Mock
    private PerformanceProjectService projectService;
    @Mock
    private PerformancePublicPageService publicPageService;
    @Mock
    private PublicNoticeService noticeService;

    private PerformancePublicNoticeService service;

    @BeforeEach
    void setUp() {
        service = new PerformancePublicNoticeService(publicNoticeMapper,
                memberService, projectService, publicPageService,
                noticeService, CLOCK);
    }

    @Test
    void 운영진은_공연_프로젝트와_공시를_연결한다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        given(publicNoticeMapper.exists(PROJECT_ID, NOTICE_ID))
                .willReturn(false);
        given(noticeService.lookupAdmin(ACTOR_ID, NOTICE_ID))
                .willReturn(adminNotice());

        service.link(ACTOR_ID, PROJECT_ID, NOTICE_ID);

        verify(projectService).validateExists(ACTOR_ID, PROJECT_ID);
        verify(publicNoticeMapper).insert(PROJECT_ID, NOTICE_ID);
    }

    @Test
    void 이미_연결된_공시는_중복_행을_만들지_않는다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());
        given(publicNoticeMapper.exists(PROJECT_ID, NOTICE_ID))
                .willReturn(true);
        given(noticeService.lookupAdmin(ACTOR_ID, NOTICE_ID))
                .willReturn(adminNotice());

        service.link(ACTOR_ID, PROJECT_ID, NOTICE_ID);

        verify(publicNoticeMapper, never()).insert(PROJECT_ID, NOTICE_ID);
    }

    @Test
    void 운영진이_아니면_공시를_연결할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(memberContext());

        assertThatThrownBy(() -> service.link(
                ACTOR_ID, PROJECT_ID, NOTICE_ID))
                .isInstanceOf(PerformanceAccessDeniedException.class);

        verify(publicNoticeMapper, never()).insert(PROJECT_ID, NOTICE_ID);
    }

    @Test
    void 운영진은_연결된_공시를_해제한다() {
        given(memberService.lookupAccessContext(ACTOR_ID))
                .willReturn(adminContext());

        service.unlink(ACTOR_ID, PROJECT_ID, NOTICE_ID);

        verify(projectService).validateExists(ACTOR_ID, PROJECT_ID);
        verify(publicNoticeMapper).remove(PROJECT_ID, NOTICE_ID);
    }

    @Test
    void 공개_페이지는_현재_게시_가능한_연결_공시만_반환한다() {
        PerformancePublicPageResponse page =
                mock(PerformancePublicPageResponse.class);
        given(page.performanceProjectId()).willReturn(PROJECT_ID);
        given(publicPageService.lookupPublic("hamlet")).willReturn(page);
        given(publicNoticeMapper.searchPublicNoticeIds(PROJECT_ID, NOW))
                .willReturn(List.of(NOTICE_ID));
        given(noticeService.lookupPublic(NOTICE_ID))
                .willReturn(publicNotice());

        var result = service.searchPublic("hamlet");

        assertThat(result).singleElement()
                .extracting("publicNoticeId", "title")
                .containsExactly(NOTICE_ID, "공연 변경 안내");
    }

    private MemberAccessContext adminContext() {
        return new MemberAccessContext(ACTOR_ID, 3L, true, false, true);
    }

    private MemberAccessContext memberContext() {
        return new MemberAccessContext(ACTOR_ID, 3L, false, false, true);
    }

    private PublicNoticeAdminDetailResponse adminNotice() {
        return new PublicNoticeAdminDetailResponse(NOTICE_ID, "SHOW",
                "공연 변경 안내", "공연 시간이 변경됩니다.",
                PublicNoticeStatus.PUBLISHED, true, NOW.minusDays(1),
                null, "운영진", "운영진", NOW, List.of());
    }

    private PublicNoticeDetailResponse publicNotice() {
        return new PublicNoticeDetailResponse(NOTICE_ID, "SHOW",
                "공연 변경 안내", "공연 시간이 변경됩니다.", true,
                NOW.minusDays(1), null, "운영진", "운영진", NOW,
                List.of());
    }
}
