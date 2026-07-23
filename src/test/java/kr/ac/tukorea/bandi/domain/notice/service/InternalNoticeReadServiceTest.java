package kr.ac.tukorea.bandi.domain.notice.service;

import kr.ac.tukorea.bandi.domain.file.service.FileAccessDecision;
import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.notice.dto.request.InternalNoticeReadableSearchCondition;
import kr.ac.tukorea.bandi.domain.notice.dto.request.InternalNoticeSearchParam;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeContentResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeDetailResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeReadStatusResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeSummaryResponse;
import kr.ac.tukorea.bandi.domain.notice.exception.InternalNoticeAccessDeniedException;
import kr.ac.tukorea.bandi.domain.notice.exception.InternalNoticeNotFoundException;
import kr.ac.tukorea.bandi.domain.notice.mapper.InternalNoticeMapper;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNotice;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeStatus;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeTargetScope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InternalNoticeReadServiceTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long NOTICE_ID = 10L;
    private static final Long STAGE_TEAM_ID = 4L;
    private static final Long OPERATOR_TEAM_ID = 5L;
    private static final Long FILE_ID = 20L;
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 20, 12, 0);
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-07-20T03:00:00Z"), ZoneId.of("Asia/Seoul"));

    @Mock
    private InternalNoticeMapper internalNoticeMapper;
    @Mock
    private MemberService memberService;
    @Mock
    private FileService fileService;

    private InternalNoticeService internalNoticeService;

    @BeforeEach
    void setUp() {
        internalNoticeService = new InternalNoticeService(internalNoticeMapper,
                memberService, fileService, new MarkdownRenderer(), CLOCK);
    }

    @Test
    void 활성_MEMBER는_전체와_소속_팀_공지를_조회한다() {
        given(memberService.lookupAccessContext(MEMBER_ID)).willReturn(memberContext());
        given(internalNoticeMapper.searchReadable(any())).willReturn(List.of(summary()));
        given(internalNoticeMapper.countReadable(any())).willReturn(1L);

        var result = internalNoticeService.searchReadable(
                MEMBER_ID, new InternalNoticeSearchParam("안내", 0, 20));

        assertThat(result.items()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1);
        ArgumentCaptor<InternalNoticeReadableSearchCondition> captor =
                ArgumentCaptor.forClass(InternalNoticeReadableSearchCondition.class);
        verify(internalNoticeMapper).searchReadable(captor.capture());
        assertThat(captor.getValue().memberTeamId()).isEqualTo(STAGE_TEAM_ID);
        assertThat(captor.getValue().admin()).isFalse();
        assertThat(captor.getValue().currentDttm()).isEqualTo(NOW);
    }

    @Test
    void 활성_ADMIN은_모든_팀_공지를_조회한다() {
        given(memberService.lookupAccessContext(MEMBER_ID)).willReturn(adminContext());
        given(internalNoticeMapper.searchReadable(any())).willReturn(List.of());
        given(internalNoticeMapper.countReadable(any())).willReturn(0L);

        internalNoticeService.searchReadable(MEMBER_ID,
                new InternalNoticeSearchParam(null, 0, 20));

        ArgumentCaptor<InternalNoticeReadableSearchCondition> captor =
                ArgumentCaptor.forClass(InternalNoticeReadableSearchCondition.class);
        verify(internalNoticeMapper).searchReadable(captor.capture());
        assertThat(captor.getValue().admin()).isTrue();
    }

    @Test
    void 비활성_멤버는_내부_공지를_읽을_수_없다() {
        given(memberService.lookupAccessContext(MEMBER_ID)).willReturn(inactiveContext());

        assertThatThrownBy(() -> internalNoticeService.searchReadable(MEMBER_ID,
                new InternalNoticeSearchParam(null, 0, 20)))
                .isInstanceOf(InternalNoticeAccessDeniedException.class);

        verify(internalNoticeMapper, never()).searchReadable(any());
    }

    @Test
    void 상세_조회는_접근_가능한_공지의_읽음과_첨부를_기록한다() {
        given(memberService.lookupAccessContext(MEMBER_ID)).willReturn(memberContext());
        given(internalNoticeMapper.lookupReadableContent(
                NOTICE_ID, NOW, STAGE_TEAM_ID, false))
                .willReturn(Optional.of(content(InternalNoticeTargetScope.TEAM,
                        STAGE_TEAM_ID)));
        given(internalNoticeMapper.searchAttachmentFileIds(NOTICE_ID))
                .willReturn(List.of());

        InternalNoticeDetailResponse result = internalNoticeService.lookupReadable(
                MEMBER_ID, NOTICE_ID);

        assertThat(result.internalNoticeId()).isEqualTo(NOTICE_ID);
        assertThat(result.createdByName()).isEqualTo("김현민");
        assertThat(result.canManage()).isFalse();
        verify(internalNoticeMapper).upsertRead(NOTICE_ID, MEMBER_ID, NOW);
    }

    @Test
    void 상세의_관리_버튼은_실제_대상_관리_범위로_판정한다() {
        given(memberService.lookupAccessContext(MEMBER_ID)).willReturn(leaderContext());
        given(internalNoticeMapper.lookupReadableContent(
                NOTICE_ID, NOW, STAGE_TEAM_ID, false))
                .willReturn(Optional.of(content(InternalNoticeTargetScope.TEAM,
                        STAGE_TEAM_ID)));
        given(internalNoticeMapper.searchAttachmentFileIds(NOTICE_ID))
                .willReturn(List.of());

        InternalNoticeDetailResponse teamNotice = internalNoticeService.lookupReadable(
                MEMBER_ID, NOTICE_ID);

        assertThat(teamNotice.canManage()).isTrue();

        given(internalNoticeMapper.lookupReadableContent(
                NOTICE_ID, NOW, STAGE_TEAM_ID, false))
                .willReturn(Optional.of(content(InternalNoticeTargetScope.ALL, null)));

        InternalNoticeDetailResponse globalNotice = internalNoticeService.lookupReadable(
                MEMBER_ID, NOTICE_ID);

        assertThat(globalNotice.canManage()).isFalse();
    }

    @Test
    void 접근할_수_없는_상세는_존재를_숨기고_읽음도_남기지_않는다() {
        given(memberService.lookupAccessContext(MEMBER_ID)).willReturn(memberContext());
        given(internalNoticeMapper.lookupReadableContent(
                NOTICE_ID, NOW, STAGE_TEAM_ID, false)).willReturn(Optional.empty());

        assertThatThrownBy(() -> internalNoticeService.lookupReadable(MEMBER_ID, NOTICE_ID))
                .isInstanceOf(InternalNoticeNotFoundException.class);

        verify(internalNoticeMapper, never()).upsertRead(any(), any(), any());
    }

    @Test
    void 읽을_수_있는_공지의_연결된_첨부만_다운로드한다() {
        given(memberService.lookupAccessContext(MEMBER_ID)).willReturn(memberContext());
        given(internalNoticeMapper.existsReadableAttachment(
                NOTICE_ID, FILE_ID, NOW, STAGE_TEAM_ID, false)).willReturn(true);
        given(fileService.openPrivateDownload(FILE_ID, FileAccessDecision.GRANTED))
                .willReturn(download());

        var result = internalNoticeService.openAttachmentDownload(
                MEMBER_ID, NOTICE_ID, FILE_ID);

        assertThat(result.originalName()).isEqualTo("proof.png");
    }

    @Test
    void 연결되지_않거나_접근할_수_없는_첨부는_다운로드하지_않는다() {
        given(memberService.lookupAccessContext(MEMBER_ID)).willReturn(memberContext());
        given(internalNoticeMapper.existsReadableAttachment(
                NOTICE_ID, FILE_ID, NOW, STAGE_TEAM_ID, false)).willReturn(false);

        assertThatThrownBy(() -> internalNoticeService.openAttachmentDownload(
                MEMBER_ID, NOTICE_ID, FILE_ID))
                .isInstanceOf(InternalNoticeNotFoundException.class);

        verify(fileService, never()).openPrivateDownload(any(), any());
    }

    @Test
    void 읽을_수_있는_공지의_이미지_첨부만_inline으로_조회한다() {
        given(memberService.lookupAccessContext(MEMBER_ID)).willReturn(memberContext());
        given(internalNoticeMapper.existsReadableAttachment(
                NOTICE_ID, FILE_ID, NOW, STAGE_TEAM_ID, false)).willReturn(true);
        given(fileService.openPrivateNoticeInlineImage(FILE_ID, FileAccessDecision.GRANTED))
                .willReturn(download());

        var result = internalNoticeService.openAttachmentInline(MEMBER_ID, NOTICE_ID, FILE_ID);

        assertThat(result.contentType()).isEqualTo("image/png");
    }

    @Test
    void ADMIN은_모든_공지의_읽음_현황을_조회한다() {
        given(memberService.lookupAccessContext(MEMBER_ID)).willReturn(adminContext());
        given(internalNoticeMapper.lookupById(NOTICE_ID))
                .willReturn(Optional.of(notice(InternalNoticeTargetScope.ALL, null)));
        given(internalNoticeMapper.searchReadStatuses(
                NOTICE_ID, InternalNoticeTargetScope.ALL, null))
                .willReturn(List.of(readStatus()));

        List<InternalNoticeReadStatusResponse> result =
                internalNoticeService.searchReadStatuses(MEMBER_ID, NOTICE_ID);

        assertThat(result).hasSize(1);
    }

    @Test
    void LEADER는_소속_팀_공지의_읽음_현황만_조회한다() {
        given(memberService.lookupAccessContext(MEMBER_ID)).willReturn(leaderContext());
        given(internalNoticeMapper.lookupById(NOTICE_ID))
                .willReturn(Optional.of(notice(
                        InternalNoticeTargetScope.TEAM, STAGE_TEAM_ID)));
        given(internalNoticeMapper.searchReadStatuses(
                NOTICE_ID, InternalNoticeTargetScope.TEAM, STAGE_TEAM_ID))
                .willReturn(List.of());

        internalNoticeService.searchReadStatuses(MEMBER_ID, NOTICE_ID);

        verify(internalNoticeMapper).searchReadStatuses(
                NOTICE_ID, InternalNoticeTargetScope.TEAM, STAGE_TEAM_ID);
    }

    @Test
    void LEADER는_전체나_다른_팀_공지의_읽음_현황을_볼_수_없다() {
        given(memberService.lookupAccessContext(MEMBER_ID)).willReturn(leaderContext());
        given(internalNoticeMapper.lookupById(NOTICE_ID))
                .willReturn(Optional.of(notice(InternalNoticeTargetScope.ALL, null)));

        assertThatThrownBy(() -> internalNoticeService.searchReadStatuses(
                MEMBER_ID, NOTICE_ID))
                .isInstanceOf(InternalNoticeAccessDeniedException.class);
    }

    private MemberAccessContext memberContext() {
        return new MemberAccessContext(MEMBER_ID, STAGE_TEAM_ID, false, false, true);
    }

    private MemberAccessContext adminContext() {
        return new MemberAccessContext(MEMBER_ID, STAGE_TEAM_ID, true, false, true);
    }

    private MemberAccessContext leaderContext() {
        return new MemberAccessContext(MEMBER_ID, STAGE_TEAM_ID, false, true, true);
    }

    private MemberAccessContext inactiveContext() {
        return new MemberAccessContext(MEMBER_ID, STAGE_TEAM_ID, false, false, false);
    }

    private InternalNoticeSummaryResponse summary() {
        return new InternalNoticeSummaryResponse(NOTICE_ID,
                InternalNoticeTargetScope.TEAM, STAGE_TEAM_ID, "무대팀", "공지 제목",
                "이서준", true, NOW.minusHours(1), NOW.plusDays(1), false);
    }

    private InternalNoticeContentResponse content(InternalNoticeTargetScope scope,
                                                   Long teamId) {
        return new InternalNoticeContentResponse(NOTICE_ID, scope, teamId, "무대팀",
                "공지 제목", "공지 본문", true, NOW.minusHours(1), NOW.plusDays(1),
                "김현민", "이서준", NOW.minusHours(1));
    }

    private InternalNotice notice(InternalNoticeTargetScope scope, Long teamId) {
        return new InternalNotice(NOTICE_ID, scope, teamId, "공지 제목", "공지 본문",
                InternalNoticeStatus.PUBLISHED, true, NOW.minusHours(1), NOW.plusDays(1),
                MEMBER_ID, MEMBER_ID, MEMBER_ID, NOW.minusDays(1), NOW.minusHours(1), null);
    }

    private InternalNoticeReadStatusResponse readStatus() {
        return new InternalNoticeReadStatusResponse(MEMBER_ID, "202012345",
                "이서준", STAGE_TEAM_ID, "무대팀", NOW.minusMinutes(30),
                NOW.minusMinutes(5));
    }

    private kr.ac.tukorea.bandi.global.response.FileDownloadResponse download() {
        return new kr.ac.tukorea.bandi.global.response.FileDownloadResponse(
                "proof.png", "image/png", 4,
                new org.springframework.core.io.InputStreamResource(
                        new java.io.ByteArrayInputStream(new byte[]{1, 2, 3, 4})));
    }
}
