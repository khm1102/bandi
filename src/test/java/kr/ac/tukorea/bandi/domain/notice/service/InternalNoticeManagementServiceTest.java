package kr.ac.tukorea.bandi.domain.notice.service;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import kr.ac.tukorea.bandi.domain.file.dto.response.FileReferenceResponse;
import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.domain.file.service.FileUploadParam;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.notice.dto.request.InternalNoticeManageSearchCondition;
import kr.ac.tukorea.bandi.domain.notice.dto.request.InternalNoticeManageSearchParam;
import kr.ac.tukorea.bandi.domain.notice.dto.request.InternalNoticePublishParam;
import kr.ac.tukorea.bandi.domain.notice.dto.request.InternalNoticeUpdateParam;
import kr.ac.tukorea.bandi.domain.notice.dto.request.InternalNoticeWriteParam;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeManageContentResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeManageDetailResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.InternalNoticeManageSummaryResponse;
import kr.ac.tukorea.bandi.domain.notice.exception.InternalNoticeAccessDeniedException;
import kr.ac.tukorea.bandi.domain.notice.exception.InvalidInternalNoticeException;
import kr.ac.tukorea.bandi.domain.notice.mapper.InternalNoticeMapper;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNotice;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeAttachment;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeStatus;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeTargetScope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InternalNoticeManagementServiceTest {

    private static final Long ACTOR_ID = 1L;
    private static final Long NOTICE_ID = 10L;
    private static final Long STAGE_TEAM_ID = 4L;
    private static final Long OPERATOR_TEAM_ID = 5L;
    private static final Long FILE_ID = 20L;
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final Instant FIXED_INSTANT = Instant.parse("2026-07-20T03:00:00Z");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 20, 12, 0);

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
                memberService, fileService, new MarkdownRenderer(),
                Clock.fixed(FIXED_INSTANT, SEOUL));
    }

    @Test
    void ADMIN은_전체_공지_초안을_작성한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        willAnswer(invocation -> {
            assignNoticeId(invocation.getArgument(0), NOTICE_ID);
            return 1;
        }).given(internalNoticeMapper).insert(any());

        Long result = internalNoticeService.createDraft(ACTOR_ID,
                writeParam(InternalNoticeTargetScope.ALL, null, List.of()));

        assertThat(result).isEqualTo(NOTICE_ID);
        ArgumentCaptor<InternalNotice> captor = ArgumentCaptor.forClass(InternalNotice.class);
        verify(internalNoticeMapper).insert(captor.capture());
        assertThat(captor.getValue().getTargetScope())
                .isEqualTo(InternalNoticeTargetScope.ALL);
    }

    @Test
    void LEADER는_소속_팀_공지와_READY_첨부를_작성한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(leaderContext());
        willAnswer(invocation -> {
            assignNoticeId(invocation.getArgument(0), NOTICE_ID);
            return 1;
        }).given(internalNoticeMapper).insert(any());

        internalNoticeService.createDraft(ACTOR_ID,
                writeParam(InternalNoticeTargetScope.TEAM, STAGE_TEAM_ID,
                        List.of(FILE_ID)));

        verify(memberService).validateActiveTeam(STAGE_TEAM_ID);
        verify(fileService).validatePrivateReadyOwnedBy(FILE_ID, ACTOR_ID);
        ArgumentCaptor<InternalNoticeAttachment> captor =
                ArgumentCaptor.forClass(InternalNoticeAttachment.class);
        verify(internalNoticeMapper).insertAttachment(captor.capture());
        assertThat(captor.getValue().getStoredFileId()).isEqualTo(FILE_ID);
    }

    @Test
    void LEADER는_전체나_다른_팀_공지를_작성할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(leaderContext());

        assertThatThrownBy(() -> internalNoticeService.createDraft(ACTOR_ID,
                writeParam(InternalNoticeTargetScope.ALL, null, List.of())))
                .isInstanceOf(InternalNoticeAccessDeniedException.class);
        assertThatThrownBy(() -> internalNoticeService.createDraft(ACTOR_ID,
                writeParam(InternalNoticeTargetScope.TEAM, OPERATOR_TEAM_ID, List.of())))
                .isInstanceOf(InternalNoticeAccessDeniedException.class);
        verify(internalNoticeMapper, never()).insert(any());
    }

    @Test
    void MEMBER와_비활성_ADMIN은_공지를_관리할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext());

        assertThatThrownBy(() -> internalNoticeService.createDraft(ACTOR_ID,
                writeParam(InternalNoticeTargetScope.TEAM, STAGE_TEAM_ID, List.of())))
                .isInstanceOf(InternalNoticeAccessDeniedException.class);

        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(inactiveAdminContext());

        assertThatThrownBy(() -> internalNoticeService.createDraft(ACTOR_ID,
                writeParam(InternalNoticeTargetScope.ALL, null, List.of())))
                .isInstanceOf(InternalNoticeAccessDeniedException.class);
    }

    @Test
    void 수정은_기존_대상과_변경_대상_권한을_모두_검증한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(leaderContext());
        given(internalNoticeMapper.lookupByIdForUpdate(NOTICE_ID))
                .willReturn(Optional.of(persistedDraft(
                        InternalNoticeTargetScope.TEAM, OPERATOR_TEAM_ID)));
        InternalNoticeUpdateParam param = new InternalNoticeUpdateParam(NOTICE_ID,
                InternalNoticeTargetScope.TEAM, STAGE_TEAM_ID, "수정 공지", "수정 본문",
                false, List.of());

        assertThatThrownBy(() -> internalNoticeService.update(ACTOR_ID, param))
                .isInstanceOf(InternalNoticeAccessDeniedException.class);
        verify(internalNoticeMapper, never()).update(any());
    }

    @Test
    void 권한_없는_수정은_첨부_파일_정보를_조회하지_않는다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(leaderContext());
        given(internalNoticeMapper.lookupByIdForUpdate(NOTICE_ID))
                .willReturn(Optional.of(persistedDraft(
                        InternalNoticeTargetScope.TEAM, OPERATOR_TEAM_ID)));
        InternalNoticeUpdateParam param = new InternalNoticeUpdateParam(NOTICE_ID,
                InternalNoticeTargetScope.TEAM, STAGE_TEAM_ID, "수정 공지", "수정 본문",
                false, List.of(FILE_ID));

        assertThatThrownBy(() -> internalNoticeService.update(ACTOR_ID, param))
                .isInstanceOf(InternalNoticeAccessDeniedException.class);

        verify(fileService, never()).lookupPrivateReady(any());
    }

    @Test
    void 소속_팀_공지_수정은_기존_첨부를_유지하고_새_첨부만_검증한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(leaderContext());
        given(internalNoticeMapper.lookupByIdForUpdate(NOTICE_ID))
                .willReturn(Optional.of(persistedDraft(
                        InternalNoticeTargetScope.TEAM, STAGE_TEAM_ID)));
        given(internalNoticeMapper.searchAttachmentFileIds(NOTICE_ID))
                .willReturn(List.of(FILE_ID));
        InternalNoticeUpdateParam param = new InternalNoticeUpdateParam(NOTICE_ID,
                InternalNoticeTargetScope.TEAM, STAGE_TEAM_ID, "수정 공지", "수정 본문",
                true, List.of(FILE_ID, FILE_ID + 1));

        internalNoticeService.update(ACTOR_ID, param);

        verify(internalNoticeMapper).update(any());
        verify(internalNoticeMapper).removeAttachmentsExcept(eq(NOTICE_ID),
                eq(List.of(FILE_ID, FILE_ID + 1)));
        verify(internalNoticeMapper).insertAttachment(any());
        verify(fileService).validatePrivateReadyOwnedBy(FILE_ID + 1, ACTOR_ID);
        verify(fileService, never()).validatePrivateReadyOwnedBy(FILE_ID, ACTOR_ID);
    }

    @Test
    void 중복_첨부는_파일_조회_전에_거부한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());

        assertThatThrownBy(() -> internalNoticeService.createDraft(ACTOR_ID,
                writeParam(InternalNoticeTargetScope.ALL, null,
                        List.of(FILE_ID, FILE_ID))))
                .isInstanceOf(InvalidInternalNoticeException.class);
        verify(fileService, never()).lookupPrivateReady(any());
    }

    @Test
    void 본문_이미지는_반드시_첨부_목록에_포함되어야_한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());

        assertThatThrownBy(() -> internalNoticeService.createDraft(ACTOR_ID,
                new InternalNoticeWriteParam(InternalNoticeTargetScope.ALL, null,
                        "공지 제목", "![포스터](attachment://" + FILE_ID + ")", false,
                        List.of())))
                .isInstanceOf(InvalidInternalNoticeException.class);

        verify(internalNoticeMapper, never()).insert(any());
    }

    @Test
    void 본문_이미지_업로드는_독립_저장_트랜잭션의_파일을_다시_조회하지_않는다() {
        FileReferenceResponse uploaded = new FileReferenceResponse(FILE_ID, "poster.png",
                "image/png", 4);
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        doReturn(uploaded).when(fileService).uploadNoticeInlineImage(any());

        FileReferenceResponse result = internalNoticeService.uploadInlineImage(ACTOR_ID,
                new FileUploadParam("notice", "poster.png", 4,
                        () -> new ByteArrayInputStream(new byte[]{1, 2, 3, 4}), ACTOR_ID));

        assertThat(result).isEqualTo(uploaded);
        verify(fileService, never()).lookupPrivateNoticeInlineImage(any());
    }

    @Test
    void 게시_종료_보관은_잠근_공지의_대상_권한과_상태를_검증한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(leaderContext());
        given(internalNoticeMapper.lookupByIdForUpdate(NOTICE_ID))
                .willReturn(Optional.of(persistedDraft(
                        InternalNoticeTargetScope.TEAM, STAGE_TEAM_ID)));

        internalNoticeService.publish(ACTOR_ID,
                new InternalNoticePublishParam(NOTICE_ID, null, NOW.plusDays(7)));

        ArgumentCaptor<InternalNotice> publishCaptor =
                ArgumentCaptor.forClass(InternalNotice.class);
        verify(internalNoticeMapper).update(publishCaptor.capture());
        assertThat(publishCaptor.getValue().getStatus())
                .isEqualTo(InternalNoticeStatus.PUBLISHED);

        given(internalNoticeMapper.lookupByIdForUpdate(NOTICE_ID))
                .willReturn(Optional.of(publishedNotice()));
        internalNoticeService.close(ACTOR_ID, NOTICE_ID);

        given(internalNoticeMapper.lookupByIdForUpdate(NOTICE_ID))
                .willReturn(Optional.of(persistedDraft(
                        InternalNoticeTargetScope.TEAM, STAGE_TEAM_ID)));
        internalNoticeService.archive(ACTOR_ID, NOTICE_ID);

        verify(internalNoticeMapper, org.mockito.Mockito.times(3)).update(any());
    }

    @Test
    void ADMIN은_운영_목록을_필터링하고_LEADER는_소속_팀으로_강제된다() {
        InternalNoticeManageSummaryResponse summary = manageSummary();
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        given(internalNoticeMapper.searchManageable(any())).willReturn(List.of(summary));

        List<InternalNoticeManageSummaryResponse> adminResult =
                internalNoticeService.searchManageable(ACTOR_ID,
                        new InternalNoticeManageSearchParam("공지", InternalNoticeStatus.DRAFT,
                                InternalNoticeTargetScope.ALL, null, 0, 20));

        assertThat(adminResult).containsExactly(summary);
        ArgumentCaptor<InternalNoticeManageSearchCondition> adminCaptor =
                ArgumentCaptor.forClass(InternalNoticeManageSearchCondition.class);
        verify(internalNoticeMapper).searchManageable(adminCaptor.capture());
        assertThat(adminCaptor.getValue().targetScope())
                .isEqualTo(InternalNoticeTargetScope.ALL);

        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(leaderContext());

        internalNoticeService.searchManageable(ACTOR_ID,
                new InternalNoticeManageSearchParam(null, null, null, null, 0, 20));

        ArgumentCaptor<InternalNoticeManageSearchCondition> leaderCaptor =
                ArgumentCaptor.forClass(InternalNoticeManageSearchCondition.class);
        verify(internalNoticeMapper, org.mockito.Mockito.times(2))
                .searchManageable(leaderCaptor.capture());
        assertThat(leaderCaptor.getAllValues().get(1).targetScope())
                .isEqualTo(InternalNoticeTargetScope.TEAM);
        assertThat(leaderCaptor.getAllValues().get(1).teamId()).isEqualTo(STAGE_TEAM_ID);
    }

    @Test
    void 운영_상세는_권한을_검증하고_첨부_참조를_조립한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(leaderContext());
        given(internalNoticeMapper.lookupManageContent(NOTICE_ID))
                .willReturn(Optional.of(manageContent()));
        given(internalNoticeMapper.searchAttachmentFileIds(NOTICE_ID))
                .willReturn(List.of(FILE_ID));
        given(fileService.lookupPrivateReady(FILE_ID)).willReturn(fileReference());

        InternalNoticeManageDetailResponse result = internalNoticeService.lookupManageable(
                ACTOR_ID, NOTICE_ID);

        assertThat(result.targetScope()).isEqualTo(InternalNoticeTargetScope.TEAM);
        assertThat(result.attachments()).extracting("storedFileId")
                .containsExactly(FILE_ID);
    }

    private InternalNoticeWriteParam writeParam(InternalNoticeTargetScope scope,
                                                 Long teamId, List<Long> fileIds) {
        return new InternalNoticeWriteParam(scope, teamId, "공지 제목", "공지 본문",
                true, fileIds);
    }

    private MemberAccessContext adminContext() {
        return new MemberAccessContext(ACTOR_ID, STAGE_TEAM_ID, true, false, true);
    }

    private MemberAccessContext inactiveAdminContext() {
        return new MemberAccessContext(ACTOR_ID, STAGE_TEAM_ID, true, false, false);
    }

    private MemberAccessContext leaderContext() {
        return new MemberAccessContext(ACTOR_ID, STAGE_TEAM_ID, false, true, true);
    }

    private MemberAccessContext memberContext() {
        return new MemberAccessContext(ACTOR_ID, STAGE_TEAM_ID, false, false, true);
    }

    private InternalNotice persistedDraft(InternalNoticeTargetScope scope, Long teamId) {
        return new InternalNotice(NOTICE_ID, scope, teamId, "공지 제목", "공지 본문",
                InternalNoticeStatus.DRAFT, true, null, null, ACTOR_ID, ACTOR_ID,
                null, NOW.minusDays(1), NOW.minusDays(1), null);
    }

    private InternalNotice publishedNotice() {
        return persistedDraft(InternalNoticeTargetScope.TEAM, STAGE_TEAM_ID)
                .publish(null, NOW.plusDays(7), ACTOR_ID, NOW);
    }

    private FileReferenceResponse fileReference() {
        return new FileReferenceResponse(FILE_ID, "notice.pdf", "application/pdf", 1024L);
    }

    private InternalNoticeManageSummaryResponse manageSummary() {
        return new InternalNoticeManageSummaryResponse(NOTICE_ID,
                InternalNoticeTargetScope.TEAM, STAGE_TEAM_ID, "무대팀", "공지 제목",
                InternalNoticeStatus.DRAFT, true, null, null, "이서준",
                NOW.minusHours(1));
    }

    private InternalNoticeManageContentResponse manageContent() {
        return new InternalNoticeManageContentResponse(NOTICE_ID,
                InternalNoticeTargetScope.TEAM, STAGE_TEAM_ID, "무대팀", "공지 제목",
                "공지 본문", InternalNoticeStatus.DRAFT, true, null, null,
                "이서준", "이서준", NOW.minusHours(1));
    }

    private void assignNoticeId(InternalNotice notice, Long internalNoticeId) {
        try {
            Field field = InternalNotice.class.getDeclaredField("internalNoticeId");
            field.setAccessible(true);
            field.set(notice, internalNoticeId);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
