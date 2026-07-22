package kr.ac.tukorea.bandi.domain.notice.service;

import kr.ac.tukorea.bandi.domain.file.dto.response.FileReferenceResponse;
import kr.ac.tukorea.bandi.domain.file.exception.InvalidFileScopeException;
import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.notice.dto.request.PublicNoticeAdminSearchCondition;
import kr.ac.tukorea.bandi.domain.notice.dto.request.PublicNoticeAdminSearchParam;
import kr.ac.tukorea.bandi.domain.notice.dto.request.PublicNoticePublishParam;
import kr.ac.tukorea.bandi.domain.notice.dto.request.PublicNoticeSearchCondition;
import kr.ac.tukorea.bandi.domain.notice.dto.request.PublicNoticeSearchParam;
import kr.ac.tukorea.bandi.domain.notice.dto.request.PublicNoticeUpdateParam;
import kr.ac.tukorea.bandi.domain.notice.dto.request.PublicNoticeWriteParam;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeAdminContentResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeAdminDetailResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeAdminSummaryResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeContentResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeDetailResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeSummaryResponse;
import kr.ac.tukorea.bandi.domain.notice.exception.InvalidPublicNoticeException;
import kr.ac.tukorea.bandi.domain.notice.exception.PublicNoticeAccessDeniedException;
import kr.ac.tukorea.bandi.domain.notice.exception.PublicNoticeNotFoundException;
import kr.ac.tukorea.bandi.domain.notice.mapper.PublicNoticeMapper;
import kr.ac.tukorea.bandi.domain.notice.model.PublicNotice;
import kr.ac.tukorea.bandi.domain.notice.model.PublicNoticeAttachment;
import kr.ac.tukorea.bandi.domain.notice.model.PublicNoticeStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
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
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PublicNoticeServiceTest {

    private static final Long ACTOR_ID = 1L;
    private static final Long NOTICE_ID = 10L;
    private static final Long FIRST_FILE_ID = 20L;
    private static final Long SECOND_FILE_ID = 21L;
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final Instant FIXED_INSTANT = Instant.parse("2026-07-20T03:00:00Z");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 20, 12, 0);

    @Mock
    private PublicNoticeMapper publicNoticeMapper;
    @Mock
    private MemberService memberService;
    @Mock
    private FileService fileService;

    private PublicNoticeService publicNoticeService;

    @BeforeEach
    void setUp() {
        publicNoticeService = new PublicNoticeService(publicNoticeMapper, memberService,
                fileService, Clock.fixed(FIXED_INSTANT, SEOUL));
    }

    @Test
    void 활성_ADMIN은_READY_공개_파일과_함께_공시_초안을_작성한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext(true));
        given(fileService.lookupPublicReady(FIRST_FILE_ID)).willReturn(firstFile());
        given(fileService.lookupPublicReady(SECOND_FILE_ID)).willReturn(secondFile());
        willAnswer(invocation -> {
            assignNoticeId(invocation.getArgument(0), NOTICE_ID);
            return 1;
        }).given(publicNoticeMapper).insert(any());

        Long result = publicNoticeService.createDraft(ACTOR_ID, writeParam(
                List.of(FIRST_FILE_ID, SECOND_FILE_ID)));

        assertThat(result).isEqualTo(NOTICE_ID);
        ArgumentCaptor<PublicNotice> noticeCaptor = ArgumentCaptor.forClass(PublicNotice.class);
        verify(publicNoticeMapper).insert(noticeCaptor.capture());
        assertThat(noticeCaptor.getValue().getStatus()).isEqualTo(PublicNoticeStatus.DRAFT);
        ArgumentCaptor<PublicNoticeAttachment> attachmentCaptor =
                ArgumentCaptor.forClass(PublicNoticeAttachment.class);
        verify(publicNoticeMapper, org.mockito.Mockito.times(2))
                .insertAttachment(attachmentCaptor.capture());
        assertThat(attachmentCaptor.getAllValues())
                .extracting(PublicNoticeAttachment::getStoredFileId)
                .containsExactly(FIRST_FILE_ID, SECOND_FILE_ID);
        assertThat(attachmentCaptor.getAllValues())
                .extracting(PublicNoticeAttachment::getDisplayOrder)
                .containsExactly(0, 1);
    }

    @Test
    void MEMBER와_비활성_ADMIN은_공시를_작성할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext());

        assertThatThrownBy(() -> publicNoticeService.createDraft(
                ACTOR_ID, writeParam(List.of())))
                .isInstanceOf(PublicNoticeAccessDeniedException.class);
        verify(publicNoticeMapper, never()).insert(any());

        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext(false));

        assertThatThrownBy(() -> publicNoticeService.createDraft(
                ACTOR_ID, writeParam(List.of())))
                .isInstanceOf(PublicNoticeAccessDeniedException.class);
    }

    @Test
    void 중복_첨부는_파일_조회_전에_거부한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext(true));

        assertThatThrownBy(() -> publicNoticeService.createDraft(
                ACTOR_ID, writeParam(List.of(FIRST_FILE_ID, FIRST_FILE_ID))))
                .isInstanceOf(InvalidPublicNoticeException.class);
        verify(fileService, never()).lookupPublicReady(any());
        verify(publicNoticeMapper, never()).insert(any());
    }

    @Test
    void 공시를_수정하면_행을_잠그고_본문과_첨부를_함께_교체한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext(true));
        given(publicNoticeMapper.lookupByIdForUpdate(NOTICE_ID))
                .willReturn(Optional.of(persistedDraft()));
        given(fileService.lookupPublicReady(SECOND_FILE_ID)).willReturn(secondFile());
        PublicNoticeUpdateParam param = new PublicNoticeUpdateParam(NOTICE_ID,
                "GENERAL", "운영 안내", "수정 본문", false,
                List.of(SECOND_FILE_ID));

        publicNoticeService.update(ACTOR_ID, param);

        ArgumentCaptor<PublicNotice> noticeCaptor = ArgumentCaptor.forClass(PublicNotice.class);
        verify(publicNoticeMapper).update(noticeCaptor.capture());
        assertThat(noticeCaptor.getValue().getTitle()).isEqualTo("운영 안내");
        assertThat(noticeCaptor.getValue().getUpdatedByMemberId()).isEqualTo(ACTOR_ID);
        verify(publicNoticeMapper).removeAttachments(NOTICE_ID);
        verify(publicNoticeMapper).insertAttachment(any());
    }

    @Test
    void 존재하지_않는_공시는_수정할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext(true));
        given(publicNoticeMapper.lookupByIdForUpdate(NOTICE_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> publicNoticeService.update(ACTOR_ID,
                new PublicNoticeUpdateParam(NOTICE_ID, "GENERAL", "제목", "본문",
                        false, List.of())))
                .isInstanceOf(PublicNoticeNotFoundException.class);
    }

    @Test
    void 공시를_게시하면_주입된_현재_시각으로_즉시_게시_상태를_결정한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext(true));
        given(publicNoticeMapper.lookupByIdForUpdate(NOTICE_ID))
                .willReturn(Optional.of(persistedDraft()));

        publicNoticeService.publish(ACTOR_ID,
                new PublicNoticePublishParam(NOTICE_ID, null, NOW.plusDays(7)));

        ArgumentCaptor<PublicNotice> captor = ArgumentCaptor.forClass(PublicNotice.class);
        verify(publicNoticeMapper).update(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(PublicNoticeStatus.PUBLISHED);
        assertThat(captor.getValue().getPublishStartDttm()).isEqualTo(NOW);
        assertThat(captor.getValue().getPublishedByMemberId()).isEqualTo(ACTOR_ID);
    }

    @Test
    void 게시_중인_공시를_종료하고_초안을_보관한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext(true));
        given(publicNoticeMapper.lookupByIdForUpdate(NOTICE_ID))
                .willReturn(Optional.of(publishedNotice()));

        publicNoticeService.close(ACTOR_ID, NOTICE_ID);

        ArgumentCaptor<PublicNotice> closeCaptor = ArgumentCaptor.forClass(PublicNotice.class);
        verify(publicNoticeMapper).update(closeCaptor.capture());
        assertThat(closeCaptor.getValue().getStatus()).isEqualTo(PublicNoticeStatus.CLOSED);

        given(publicNoticeMapper.lookupByIdForUpdate(NOTICE_ID))
                .willReturn(Optional.of(persistedDraft()));

        publicNoticeService.archive(ACTOR_ID, NOTICE_ID);

        ArgumentCaptor<PublicNotice> archiveCaptor = ArgumentCaptor.forClass(PublicNotice.class);
        verify(publicNoticeMapper, org.mockito.Mockito.times(2)).update(archiveCaptor.capture());
        assertThat(archiveCaptor.getAllValues().get(1).getStatus())
                .isEqualTo(PublicNoticeStatus.ARCHIVED);
    }

    @Test
    void 공개_목록은_현재_시각과_검색어와_페이지_조건을_전달한다() {
        PublicNoticeSummaryResponse summary = new PublicNoticeSummaryResponse(
                NOTICE_ID, "GENERAL", "운영 안내", true,
                NOW.minusDays(1), "이서준", NOW.minusHours(1));
        given(publicNoticeMapper.searchPublic(any())).willReturn(List.of(summary));

        List<PublicNoticeSummaryResponse> result = publicNoticeService.searchPublic(
                new PublicNoticeSearchParam("운영", 2, 20));

        assertThat(result).containsExactly(summary);
        ArgumentCaptor<PublicNoticeSearchCondition> captor =
                ArgumentCaptor.forClass(PublicNoticeSearchCondition.class);
        verify(publicNoticeMapper).searchPublic(captor.capture());
        assertThat(captor.getValue().keyword()).isEqualTo("운영");
        assertThat(captor.getValue().currentDttm()).isEqualTo(NOW);
        assertThat(captor.getValue().offset()).isEqualTo(40);
        assertThat(captor.getValue().limit()).isEqualTo(20);
    }

    @Test
    void ADMIN은_모든_상태의_공시를_검색한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext(true));
        PublicNoticeAdminSummaryResponse summary = new PublicNoticeAdminSummaryResponse(
                NOTICE_ID, "RECRUITMENT", "신입 부원 모집", PublicNoticeStatus.DRAFT,
                true, null, null, "이서준", NOW.minusHours(1));
        given(publicNoticeMapper.searchAdmin(any())).willReturn(List.of(summary));

        List<PublicNoticeAdminSummaryResponse> result = publicNoticeService.searchAdmin(
                ACTOR_ID, new PublicNoticeAdminSearchParam(
                        "모집", PublicNoticeStatus.DRAFT, 1, 20));

        assertThat(result).containsExactly(summary);
        ArgumentCaptor<PublicNoticeAdminSearchCondition> captor =
                ArgumentCaptor.forClass(PublicNoticeAdminSearchCondition.class);
        verify(publicNoticeMapper).searchAdmin(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(PublicNoticeStatus.DRAFT);
        assertThat(captor.getValue().offset()).isEqualTo(20);
    }

    @Test
    void MEMBER는_운영용_공시_목록을_검색할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext());

        assertThatThrownBy(() -> publicNoticeService.searchAdmin(
                ACTOR_ID, new PublicNoticeAdminSearchParam(null, null, 0, 20)))
                .isInstanceOf(PublicNoticeAccessDeniedException.class);
        verify(publicNoticeMapper, never()).searchAdmin(any());
    }

    @Test
    void ADMIN은_임시_공시_상세와_첨부를_조회한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext(true));
        given(publicNoticeMapper.lookupAdminContent(NOTICE_ID))
                .willReturn(Optional.of(adminContentResponse()));
        given(publicNoticeMapper.searchAttachmentFileIds(NOTICE_ID))
                .willReturn(List.of(FIRST_FILE_ID));
        given(fileService.lookupPublicReady(FIRST_FILE_ID)).willReturn(firstFile());

        PublicNoticeAdminDetailResponse result = publicNoticeService.lookupAdmin(
                ACTOR_ID, NOTICE_ID);

        assertThat(result.status()).isEqualTo(PublicNoticeStatus.DRAFT);
        assertThat(result.attachments()).extracting("storedFileId")
                .containsExactly(FIRST_FILE_ID);
    }

    @Test
    void 공개_상세는_파일_내부정보를_제외한_첨부_참조를_조립한다() {
        given(publicNoticeMapper.lookupPublicContent(NOTICE_ID, NOW))
                .willReturn(Optional.of(contentResponse()));
        given(publicNoticeMapper.searchAttachmentFileIds(NOTICE_ID))
                .willReturn(List.of(FIRST_FILE_ID, SECOND_FILE_ID));
        given(fileService.lookupPublicReady(FIRST_FILE_ID)).willReturn(firstFile());
        given(fileService.lookupPublicReady(SECOND_FILE_ID)).willReturn(secondFile());

        PublicNoticeDetailResponse result = publicNoticeService.lookupPublic(NOTICE_ID);

        assertThat(result.title()).isEqualTo("운영 안내");
        assertThat(result.attachments()).extracting("originalName")
                .containsExactly("poster.pdf", "schedule.pdf");
    }

    @Test
    void 공개되지_않은_공시는_상세_조회할_수_없다() {
        given(publicNoticeMapper.lookupPublicContent(NOTICE_ID, NOW))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> publicNoticeService.lookupPublic(NOTICE_ID))
                .isInstanceOf(PublicNoticeNotFoundException.class);
        verify(fileService, never()).lookupPublicReady(any());
    }

    @Test
    void 공개_공시에_연결된_첨부만_직접_전송한다() {
        given(publicNoticeMapper.existsPublicAttachment(NOTICE_ID, FIRST_FILE_ID, NOW))
                .willReturn(true);
        given(fileService.openPublicDownload(FIRST_FILE_ID))
                .willReturn(download());

        var result = publicNoticeService.openAttachmentDownload(
                NOTICE_ID, FIRST_FILE_ID);

        assertThat(result.originalName()).isEqualTo("proof.png");
    }

    @Test
    void 공개되지_않았거나_연결되지_않은_첨부는_URL을_발급하지_않는다() {
        given(publicNoticeMapper.existsPublicAttachment(NOTICE_ID, FIRST_FILE_ID, NOW))
                .willReturn(false);

        assertThatThrownBy(() -> publicNoticeService.openAttachmentDownload(
                NOTICE_ID, FIRST_FILE_ID))
                .isInstanceOf(PublicNoticeAccessDeniedException.class);
        verify(fileService, never()).openPublicDownload(any());
    }

    @Test
    void 비공개_파일은_공시_첨부로_연결할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext(true));
        given(fileService.lookupPublicReady(FIRST_FILE_ID))
                .willThrow(new InvalidFileScopeException());

        assertThatThrownBy(() -> publicNoticeService.createDraft(
                ACTOR_ID, writeParam(List.of(FIRST_FILE_ID))))
                .isInstanceOf(InvalidFileScopeException.class);
        verify(publicNoticeMapper, never()).insert(any());
    }

    private PublicNoticeWriteParam writeParam(List<Long> fileIds) {
        return new PublicNoticeWriteParam("RECRUITMENT", "신입 부원 모집",
                "모집 안내 본문", true, fileIds);
    }

    private MemberAccessContext adminContext(boolean active) {
        return new MemberAccessContext(ACTOR_ID, 4L, true, false, active);
    }

    private MemberAccessContext memberContext() {
        return new MemberAccessContext(ACTOR_ID, 4L, false, false, true);
    }

    private PublicNotice persistedDraft() {
        return new PublicNotice(NOTICE_ID, "RECRUITMENT", "신입 부원 모집", "본문",
                PublicNoticeStatus.DRAFT, true, null, null, ACTOR_ID, ACTOR_ID,
                null, NOW.minusDays(1), NOW.minusDays(1), null);
    }

    private PublicNotice publishedNotice() {
        return persistedDraft().publish(null, NOW.plusDays(7), ACTOR_ID, NOW);
    }

    private PublicNoticeContentResponse contentResponse() {
        return new PublicNoticeContentResponse(NOTICE_ID, "GENERAL", "운영 안내",
                "운영 상세 본문", true, NOW.minusDays(1), NOW.plusDays(7),
                "이서준", "이서준", NOW.minusHours(1));
    }

    private PublicNoticeAdminContentResponse adminContentResponse() {
        return new PublicNoticeAdminContentResponse(NOTICE_ID, "RECRUITMENT",
                "신입 부원 모집", "모집 안내 본문", PublicNoticeStatus.DRAFT,
                true, null, null, "이서준", "이서준", NOW.minusHours(1));
    }

    private FileReferenceResponse firstFile() {
        return new FileReferenceResponse(FIRST_FILE_ID, "poster.pdf",
                "application/pdf", 1024L);
    }

    private FileReferenceResponse secondFile() {
        return new FileReferenceResponse(SECOND_FILE_ID, "schedule.pdf",
                "application/pdf", 2048L);
    }

    private kr.ac.tukorea.bandi.global.response.FileDownloadResponse download() {
        return new kr.ac.tukorea.bandi.global.response.FileDownloadResponse(
                "proof.png", "image/png", 4,
                new org.springframework.core.io.InputStreamResource(
                        new java.io.ByteArrayInputStream(new byte[]{1, 2, 3, 4})));
    }

    private void assignNoticeId(PublicNotice notice, Long publicNoticeId) {
        try {
            Field field = PublicNotice.class.getDeclaredField("publicNoticeId");
            field.setAccessible(true);
            field.set(notice, publicNoticeId);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
