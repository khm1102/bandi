package kr.ac.tukorea.bandi.domain.activity.service;

import kr.ac.tukorea.bandi.domain.activity.document.ActivityReportHwpxGenerator;
import kr.ac.tukorea.bandi.domain.activity.document.ActivityReportPhotoParam;
import kr.ac.tukorea.bandi.domain.activity.document.ActivityReportPhotoProcessor;
import kr.ac.tukorea.bandi.domain.activity.document.ActivityReportPhotoUploadParam;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityReportParticipantCandidateResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityReportDocumentSavedResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityReportDocumentDraftResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityReportParticipantResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityFileResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityRecordManageDetailResponse;
import kr.ac.tukorea.bandi.domain.activity.exception.ActivityRecordNotFoundException;
import kr.ac.tukorea.bandi.domain.activity.exception.InvalidActivityReportDocumentException;
import kr.ac.tukorea.bandi.domain.activity.mapper.ActivityReportDocumentMapper;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityFileRole;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityReportDocument;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityReportDocumentRecord;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityReportParticipantRecord;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecordStatus;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityRecordWriteParam;
import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.domain.file.service.FileUploadParam;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ActivityReportDocumentService {

    private static final int SEARCH_MIN_LENGTH = 2;
    private static final int SEARCH_MAX_LENGTH = 50;
    private static final int MAX_PHOTO_BYTES = 10 * 1024 * 1024;
    private static final DateTimeFormatter FILE_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final MemberService memberService;
    private final ActivityReportPhotoProcessor photoProcessor;
    private final ActivityReportHwpxGenerator hwpxGenerator;
    private final ActivityRecordService activityRecordService;
    private final ActivityReportDocumentMapper activityReportDocumentMapper;
    private final FileService fileService;

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

    @Transactional
    public ActivityReportDocumentSavedResponse saveDraft(
            Long actorMemberId, ActivityReportDocument document,
            ActivityReportPhotoUploadParam photo) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        if (!access.canReadInternal() || access.teamId() == null) {
            throw new InvalidActivityReportDocumentException("member-team");
        }
        byte[] normalizedPhoto = normalizePhoto(photo);
        String presidentName = memberService.lookupActivePresidentName();
        byte[] hwpx = hwpxGenerator.generate(document, presidentName, normalizedPhoto);
        String filename = document.activityAt().format(FILE_DATE_FORMATTER)
                + "_반디_동아리_활동_내역서.hwpx";
        Long activityRecordId = activityRecordService.createDraft(actorMemberId,
                new ActivityRecordWriteParam(access.teamId(), document.activityAt(),
                        document.recordTitle(),
                        document.content(), document.participants().size()));
        Long photoStoredFileId = upload(actorMemberId, "activity-report",
                "activity-photo.png", normalizedPhoto);
        Long documentStoredFileId = upload(actorMemberId, "activity-report",
                filename, hwpx);
        activityRecordService.attachGeneratedFile(actorMemberId, activityRecordId,
                photoStoredFileId, ActivityFileRole.EVIDENCE);
        activityRecordService.attachGeneratedFile(actorMemberId, activityRecordId,
                documentStoredFileId, ActivityFileRole.DOCUMENT);
        persistDocument(activityRecordId, document);
        return new ActivityReportDocumentSavedResponse(activityRecordId,
                documentStoredFileId, filename, ActivityRecordStatus.DRAFT);
    }

    @Transactional
    public int submit(Long actorMemberId, Long activityRecordId) {
        return activityRecordService.submit(actorMemberId, activityRecordId, null);
    }

    public ActivityReportDocumentDraftResponse lookupDraft(Long actorMemberId,
                                                            Long activityRecordId) {
        ActivityRecordManageDetailResponse detail = activityRecordService
                .lookupManageable(actorMemberId, activityRecordId);
        ActivityReportDocumentRecord document = activityReportDocumentMapper
                .lookupByActivityRecordId(activityRecordId)
                .orElseThrow(() -> new ActivityRecordNotFoundException(activityRecordId));
        ActivityFileResponse photo = currentFile(detail, ActivityFileRole.EVIDENCE);
        ActivityFileResponse hwpx = currentFile(detail, ActivityFileRole.DOCUMENT);
        return new ActivityReportDocumentDraftResponse(activityRecordId,
                detail.title(), document.getRepresentative(), document.getLocation(),
                detail.activityDttm(), detail.body(),
                activityReportDocumentMapper.searchParticipants(
                                document.getActivityReportDocumentId()).stream()
                        .map(ActivityReportParticipantResponse::from).toList(),
                detail.status(), photo.storedFileId(), photo.originalName(),
                hwpx.storedFileId(), hwpx.originalName());
    }

    @Transactional
    public ActivityReportDocumentSavedResponse updateDraft(
            Long actorMemberId, Long activityRecordId,
            ActivityReportDocument document, ActivityReportPhotoUploadParam photo) {
        ActivityRecordManageDetailResponse detail = activityRecordService
                .lookupManageable(actorMemberId, activityRecordId);
        if (detail.status() != ActivityRecordStatus.DRAFT
                && detail.status() != ActivityRecordStatus.REVISION_REQUESTED) {
            throw new InvalidActivityReportDocumentException("status");
        }
        ActivityReportDocumentRecord storedDocument = activityReportDocumentMapper
                .lookupByActivityRecordId(activityRecordId)
                .orElseThrow(() -> new ActivityRecordNotFoundException(activityRecordId));
        ActivityFileResponse currentPhoto = currentFile(detail,
                ActivityFileRole.EVIDENCE);
        ActivityFileResponse currentHwpx = currentFile(detail,
                ActivityFileRole.DOCUMENT);
        byte[] normalizedPhoto = photo == null
                ? readCurrentPhoto(actorMemberId, activityRecordId, currentPhoto)
                : normalizePhoto(photo);
        String filename = document.activityAt().format(FILE_DATE_FORMATTER)
                + "_반디_동아리_활동_내역서.hwpx";
        byte[] hwpx = hwpxGenerator.generate(document,
                memberService.lookupActivePresidentName(), normalizedPhoto);
        activityRecordService.update(actorMemberId,
                new kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityRecordUpdateParam(
                        activityRecordId, document.activityAt(),
                        document.recordTitle(),
                        document.content(), document.participants().size()));
        if (photo != null) {
            Long photoStoredFileId = upload(actorMemberId, "activity-report",
                    "activity-photo.png", normalizedPhoto);
            activityRecordService.replaceGeneratedFile(actorMemberId,
                    currentPhoto.activityRecordFileId(), photoStoredFileId,
                    ActivityFileRole.EVIDENCE);
        }
        Long documentStoredFileId = upload(actorMemberId, "activity-report",
                filename, hwpx);
        activityRecordService.replaceGeneratedFile(actorMemberId,
                currentHwpx.activityRecordFileId(), documentStoredFileId,
                ActivityFileRole.DOCUMENT);
        updateDocument(storedDocument, document);
        return new ActivityReportDocumentSavedResponse(activityRecordId,
                documentStoredFileId, filename, detail.status());
    }

    private byte[] normalizePhoto(ActivityReportPhotoUploadParam photo) {
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
        return photoProcessor.normalize(new ActivityReportPhotoParam(
                bytes, photo.contentType()));
    }

    private Long upload(Long actorMemberId, String domain, String filename,
                        byte[] content) {
        return fileService.uploadPrivate(new FileUploadParam(domain, filename,
                content.length, () -> new ByteArrayInputStream(content), actorMemberId));
    }

    private void persistDocument(Long activityRecordId,
                                 ActivityReportDocument document) {
        ActivityReportDocumentRecord record = ActivityReportDocumentRecord.create(
                activityRecordId, document);
        activityReportDocumentMapper.insert(record);
        if (record.getActivityReportDocumentId() == null) {
            throw new InvalidActivityReportDocumentException("document-id");
        }
        for (int index = 0; index < document.participants().size(); index++) {
            activityReportDocumentMapper.insertParticipant(
                    ActivityReportParticipantRecord.create(
                            record.getActivityReportDocumentId(), index,
                            document.participants().get(index)));
        }
    }

    private void updateDocument(ActivityReportDocumentRecord stored,
                                ActivityReportDocument document) {
        ActivityReportDocumentRecord changed = new ActivityReportDocumentRecord(
                stored.getActivityReportDocumentId(), stored.getActivityRecordId(),
                document.representative(), document.location(),
                stored.getCreatedDttm(), stored.getUpdatedDttm());
        activityReportDocumentMapper.update(changed);
        activityReportDocumentMapper.removeParticipants(
                stored.getActivityReportDocumentId());
        for (int index = 0; index < document.participants().size(); index++) {
            activityReportDocumentMapper.insertParticipant(
                    ActivityReportParticipantRecord.create(
                            stored.getActivityReportDocumentId(), index,
                            document.participants().get(index)));
        }
    }

    private ActivityFileResponse currentFile(ActivityRecordManageDetailResponse detail,
                                             ActivityFileRole role) {
        return detail.currentFiles().stream()
                .filter(file -> file.fileRole() == role)
                .findFirst()
                .orElseThrow(() -> new InvalidActivityReportDocumentException(
                        "missing-" + role.name().toLowerCase(java.util.Locale.ROOT)));
    }

    private byte[] readCurrentPhoto(Long actorMemberId, Long activityRecordId,
                                    ActivityFileResponse photo) {
        try (java.io.InputStream input = activityRecordService.openManageableDownload(
                actorMemberId, activityRecordId, photo.storedFileId())
                .resource().getInputStream()) {
            byte[] bytes = input.readNBytes(MAX_PHOTO_BYTES + 1);
            if (bytes.length > MAX_PHOTO_BYTES) {
                throw new InvalidActivityReportDocumentException("photo.size");
            }
            return bytes;
        } catch (IOException exception) {
            throw new InvalidActivityReportDocumentException("photo.read");
        }
    }
}
