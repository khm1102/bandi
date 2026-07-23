package kr.ac.tukorea.bandi.domain.activity.service;

import kr.ac.tukorea.bandi.domain.activity.document.ActivityReportHwpxGenerator;
import kr.ac.tukorea.bandi.domain.activity.document.ActivityReportPhotoProcessor;
import kr.ac.tukorea.bandi.domain.activity.document.ActivityReportPhotoUploadParam;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityReportParticipantCandidateResponse;
import kr.ac.tukorea.bandi.domain.activity.exception.InvalidActivityReportDocumentException;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityReportDocument;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityReportParticipant;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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

    private ActivityReportDocumentService service;

    @BeforeEach
    void setUp() {
        service = new ActivityReportDocumentService(memberService,
                photoProcessor, hwpxGenerator);
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
    void 완성본은_사진을_메모리에서_정규화하고_현재_회장으로_생성한다() {
        ActivityReportDocument document = document();
        byte[] source = {1, 2, 3};
        byte[] normalized = {4, 5, 6};
        given(memberService.lookupActivePresidentName()).willReturn("원동연");
        given(photoProcessor.normalize(any())).willReturn(normalized);
        given(hwpxGenerator.generate(document, "원동연", normalized))
                .willReturn(new byte[]{7, 8});

        byte[] result = service.create(document, new ActivityReportPhotoUploadParam(
                source.length, "image/png", new ByteArrayResource(source)));

        assertThat(result).containsExactly(7, 8);
        verify(hwpxGenerator).generate(document, "원동연", normalized);
    }

    @Test
    void 사진_누락과_십_메비바이트_초과는_읽기_전에_거부한다() {
        assertThatThrownBy(() -> service.create(document(), null))
                .isInstanceOf(InvalidActivityReportDocumentException.class);
        ActivityReportPhotoUploadParam tooLarge = new ActivityReportPhotoUploadParam(
                10L * 1024 * 1024 + 1, "image/png", new ByteArrayResource(new byte[]{1}));

        assertThatThrownBy(() -> service.create(document(), tooLarge))
                .isInstanceOf(InvalidActivityReportDocumentException.class);
        verify(photoProcessor, never()).normalize(any());
    }

    private ActivityReportDocument document() {
        return ActivityReportDocument.create("대표", "장소",
                LocalDateTime.of(2026, 2, 11, 16, 30), "내용",
                List.of(new ActivityReportParticipant("참여자", null, null, null)));
    }
}
