package kr.ac.tukorea.bandi.domain.activity.service;

import kr.ac.tukorea.bandi.domain.activity.document.ActivityReportHwpxGenerator;
import kr.ac.tukorea.bandi.domain.activity.document.ActivityReportPhotoProcessor;
import kr.ac.tukorea.bandi.domain.activity.document.ActivityReportPhotoUploadParam;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityReportParticipantCandidateResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityReportDocumentSavedResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityFileResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityRecordManageDetailResponse;
import kr.ac.tukorea.bandi.domain.activity.exception.InvalidActivityReportDocumentException;
import kr.ac.tukorea.bandi.domain.activity.mapper.ActivityReportDocumentMapper;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityReportDocument;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityReportParticipant;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecordStatus;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityFileRole;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityReportDocumentRecord;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityRecordWriteParam;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityRecordUpdateParam;
import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ActivityReportDocumentServiceTest {

    @Mock
    private MemberService memberService;
    @Mock
    private ActivityReportPhotoProcessor photoProcessor;
    @Mock
    private ActivityReportHwpxGenerator hwpxGenerator;
    @Mock
    private ActivityRecordService activityRecordService;
    @Mock
    private ActivityReportDocumentMapper activityReportDocumentMapper;
    @Mock
    private FileService fileService;

    private ActivityReportDocumentService service;

    @BeforeEach
    void setUp() {
        service = new ActivityReportDocumentService(memberService,
                photoProcessor, hwpxGenerator, activityRecordService,
                activityReportDocumentMapper, fileService);
    }

    @Test
    void 빈_양식은_생성_시점의_활성_회장_이름을_사용한다() {
        given(memberService.lookupActivePresidentName()).willReturn("원동연");
        given(hwpxGenerator.generateBlank("원동연")).willReturn(new byte[]{1, 2});

        assertThat(service.createBlank()).containsExactly(1, 2);
        verify(hwpxGenerator).generateBlank("원동연");
    }

    @Test
    void 멤버_검색은_두_자_이상만_허용하고_최소_정보만_반환한다() {
        given(memberService.searchActivityReportParticipants("김현"))
                .willReturn(List.of(new MemberService.ActivityReportParticipantLookup(
                        "김현민", "컴퓨터공학부", "2025591010")));

        List<ActivityReportParticipantCandidateResponse> result =
                service.searchParticipants(" 김현 ");

        assertThat(result).containsExactly(new ActivityReportParticipantCandidateResponse(
                "김현민", "컴퓨터공학부", "2025591010"));
        assertThatThrownBy(() -> service.searchParticipants("김"))
                .isInstanceOf(InvalidActivityReportDocumentException.class);
    }

    @Test
    void 사진_누락과_십_메비바이트_초과는_읽기_전에_거부한다() {
        given(memberService.lookupAccessContext(11L))
                .willReturn(new MemberAccessContext(11L, 2L, false, false, true));

        assertThatThrownBy(() -> service.saveDraft(11L, document(), null))
                .isInstanceOf(InvalidActivityReportDocumentException.class);
        ActivityReportPhotoUploadParam tooLarge = new ActivityReportPhotoUploadParam(
                10L * 1024 * 1024 + 1, "image/png", new ByteArrayResource(new byte[]{1}));

        assertThatThrownBy(() -> service.saveDraft(11L, document(), tooLarge))
                .isInstanceOf(InvalidActivityReportDocumentException.class);
        verify(photoProcessor, never()).normalize(any());
        verify(activityRecordService, never()).createDraft(any(), any());
    }

    @Test
    void 임시_저장하면_활동_기록과_입력값_사진_HWPX를_함께_저장한다() {
        ActivityReportDocument document = document("여름방학 대본 리딩");
        byte[] source = {1, 2, 3};
        byte[] normalized = {4, 5, 6};
        byte[] hwpx = {7, 8, 9};
        given(memberService.lookupAccessContext(11L))
                .willReturn(new MemberAccessContext(11L, 2L, false, false, true));
        given(memberService.lookupActivePresidentName()).willReturn("원동연");
        given(photoProcessor.normalize(any())).willReturn(normalized);
        given(hwpxGenerator.generate(document, "원동연", normalized)).willReturn(hwpx);
        given(activityRecordService.createDraft(eq(11L), any())).willReturn(21L);
        given(fileService.uploadPrivate(any())).willReturn(31L, 32L);
        willAnswer(invocation -> {
            Object documentRecord = invocation.getArgument(0);
            ReflectionTestUtils.setField(documentRecord,
                    "activityReportDocumentId", 41L);
            return 1;
        }).given(activityReportDocumentMapper).insert(any());

        ActivityReportDocumentSavedResponse result = service.saveDraft(11L, document,
                new ActivityReportPhotoUploadParam(source.length, "image/png",
                        new ByteArrayResource(source)));

        assertThat(result.activityRecordId()).isEqualTo(21L);
        assertThat(result.documentStoredFileId()).isEqualTo(32L);
        assertThat(result.status()).isEqualTo(ActivityRecordStatus.DRAFT);
        verify(activityRecordService).attachGeneratedFile(11L, 21L, 31L,
                kr.ac.tukorea.bandi.domain.activity.model.ActivityFileRole.EVIDENCE);
        verify(activityRecordService).attachGeneratedFile(11L, 21L, 32L,
                kr.ac.tukorea.bandi.domain.activity.model.ActivityFileRole.DOCUMENT);
        verify(activityReportDocumentMapper).insert(any());
        verify(activityReportDocumentMapper).insertParticipant(any());
        ArgumentCaptor<ActivityRecordWriteParam> writeParamCaptor =
                ArgumentCaptor.forClass(ActivityRecordWriteParam.class);
        verify(activityRecordService).createDraft(eq(11L), writeParamCaptor.capture());
        assertThat(writeParamCaptor.getValue().title()).isEqualTo("여름방학 대본 리딩");
    }

    @Test
    void 검수_요청은_저장된_활동_기록을_기존_제출_흐름으로_전환한다() {
        given(activityRecordService.submit(11L, 21L, null)).willReturn(1);

        int revisionNo = service.submit(11L, 21L);

        assertThat(revisionNo).isEqualTo(1);
        verify(activityRecordService).submit(11L, 21L, null);
    }

    @Test
    void 임시_저장한_문서는_기존_사진을_유지하며_HWPX와_입력값을_갱신한다() {
        ActivityReportDocument document = document();
        byte[] normalized = {4, 5, 6};
        byte[] hwpx = {7, 8, 9};
        given(activityRecordService.lookupManageable(11L, 21L))
                .willReturn(savedDetail());
        given(activityReportDocumentMapper.lookupByActivityRecordId(21L))
                .willReturn(java.util.Optional.of(new ActivityReportDocumentRecord(
                        41L, 21L, "이전 대표", "이전 장소", null, null)));
        given(activityRecordService.openManageableDownload(11L, 21L, 31L))
                .willReturn(new kr.ac.tukorea.bandi.global.response.FileDownloadResponse(
                        "activity-photo.png", "image/png", normalized.length,
                        new org.springframework.core.io.ByteArrayResource(normalized)));
        given(memberService.lookupActivePresidentName()).willReturn("원동연");
        given(hwpxGenerator.generate(document, "원동연", normalized)).willReturn(hwpx);
        given(fileService.uploadPrivate(any())).willReturn(33L);

        ActivityReportDocumentSavedResponse result = service.updateDraft(
                11L, 21L, document, null);

        assertThat(result.documentStoredFileId()).isEqualTo(33L);
        ArgumentCaptor<ActivityRecordUpdateParam> updateParamCaptor =
                ArgumentCaptor.forClass(ActivityRecordUpdateParam.class);
        verify(activityRecordService).update(eq(11L), updateParamCaptor.capture());
        assertThat(updateParamCaptor.getValue().title()).isEqualTo("대본 리딩 기록");
        verify(activityRecordService).replaceGeneratedFile(11L, 52L, 33L,
                ActivityFileRole.DOCUMENT);
        verify(activityReportDocumentMapper).update(any());
        verify(activityReportDocumentMapper).removeParticipants(41L);
    }

    private ActivityReportDocument document() {
        return document("대본 리딩 기록");
    }

    private ActivityReportDocument document(String recordTitle) {
        return ActivityReportDocument.create(recordTitle, "대표", "장소",
                LocalDateTime.of(2026, 2, 11, 16, 30), "내용",
                List.of(new ActivityReportParticipant("참여자", null, null, null)));
    }

    private ActivityRecordManageDetailResponse savedDetail() {
        LocalDateTime now = LocalDateTime.of(2026, 2, 11, 16, 30);
        return new ActivityRecordManageDetailResponse(21L, 2L, "무대팀", now,
                "2월 활동 내역서", "이전 내용", 1, ActivityRecordStatus.DRAFT,
                11L, "작성자", "작성자", null, null, now, true,
                List.of(
                        new ActivityFileResponse(51L, 31L, "activity-photo.png",
                                "image/png", 3L, ActivityFileRole.EVIDENCE,
                                0, 11L, "작성자", now),
                        new ActivityFileResponse(52L, 32L, "activity.hwpx",
                                "application/hwp+zip", 3L, ActivityFileRole.DOCUMENT,
                                0, 11L, "작성자", now)),
                List.of(), List.of());
    }
}
