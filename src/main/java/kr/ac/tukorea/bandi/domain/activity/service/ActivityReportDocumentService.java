package kr.ac.tukorea.bandi.domain.activity.service;

import kr.ac.tukorea.bandi.domain.activity.document.ActivityReportHwpxGenerator;
import kr.ac.tukorea.bandi.domain.activity.document.ActivityReportPhotoParam;
import kr.ac.tukorea.bandi.domain.activity.document.ActivityReportPhotoProcessor;
import kr.ac.tukorea.bandi.domain.activity.document.ActivityReportPhotoUploadParam;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityReportParticipantCandidateResponse;
import kr.ac.tukorea.bandi.domain.activity.exception.InvalidActivityReportDocumentException;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityReportDocument;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ActivityReportDocumentService {

    private static final int SEARCH_MIN_LENGTH = 2;
    private static final int SEARCH_MAX_LENGTH = 50;
    private static final int MAX_PHOTO_BYTES = 10 * 1024 * 1024;

    private final MemberService memberService;
    private final ActivityReportPhotoProcessor photoProcessor;
    private final ActivityReportHwpxGenerator hwpxGenerator;

    public Optional<String> lookupActivePresidentNameForPage() {
        return memberService.lookupActivePresidentNameIfPresent();
    }

    public List<ActivityReportParticipantCandidateResponse> searchParticipants(
            String rawKeyword) {
        String keyword = rawKeyword == null ? "" : rawKeyword.trim();
        if (keyword.length() < SEARCH_MIN_LENGTH
                || keyword.length() > SEARCH_MAX_LENGTH) {
            throw new InvalidActivityReportDocumentException("participantQuery");
        }
        return memberService.searchActivityReportParticipants(keyword).stream()
                .map(member -> new ActivityReportParticipantCandidateResponse(
                        member.name(), member.department(), member.studentNo()))
                .toList();
    }

    public byte[] createBlank() {
        return hwpxGenerator.generateBlank(memberService.lookupActivePresidentName());
    }

    public byte[] create(ActivityReportDocument document,
                         ActivityReportPhotoUploadParam photo) {
        if (photo == null || photo.source() == null || photo.size() <= 0
                || photo.size() > MAX_PHOTO_BYTES) {
            throw new InvalidActivityReportDocumentException("photo");
        }
        byte[] bytes;
        try (java.io.InputStream input = photo.source().getInputStream()) {
            bytes = input.readNBytes(MAX_PHOTO_BYTES + 1);
        } catch (IOException exception) {
            throw new InvalidActivityReportDocumentException("photo.read");
        }
        if (bytes.length > MAX_PHOTO_BYTES) {
            throw new InvalidActivityReportDocumentException("photo.size");
        }
        byte[] normalized = photoProcessor.normalize(new ActivityReportPhotoParam(
                bytes, photo.contentType()));
        return hwpxGenerator.generate(document,
                memberService.lookupActivePresidentName(), normalized);
    }
}
