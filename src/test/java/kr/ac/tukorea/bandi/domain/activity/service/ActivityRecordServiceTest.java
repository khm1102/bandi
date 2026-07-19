package kr.ac.tukorea.bandi.domain.activity.service;

import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityFileAddParam;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityFileReplaceParam;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityManageSearchCondition;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityManageSearchParam;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityRecordSearchCondition;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityRecordSearchParam;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityRecordUpdateParam;
import kr.ac.tukorea.bandi.domain.activity.dto.request.ActivityRecordWriteParam;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityFileLinkResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityRecordContentResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityRecordDetailResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityRecordManageContentResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityRecordManageDetailResponse;
import kr.ac.tukorea.bandi.domain.activity.dto.response.ActivityRecordSummaryResponse;
import kr.ac.tukorea.bandi.domain.activity.exception.ActivityRecordAccessDeniedException;
import kr.ac.tukorea.bandi.domain.activity.exception.ActivityRecordFileNotFoundException;
import kr.ac.tukorea.bandi.domain.activity.exception.InvalidActivityRecordException;
import kr.ac.tukorea.bandi.domain.activity.mapper.ActivityRecordMapper;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityFileRole;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecord;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecordFile;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecordRevision;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecordStatus;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityReviewHistory;
import kr.ac.tukorea.bandi.domain.file.dto.response.FileReferenceResponse;
import kr.ac.tukorea.bandi.domain.file.service.FileAccessDecision;
import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
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
class ActivityRecordServiceTest {

    private static final Long ACTOR_ID = 1L;
    private static final Long OTHER_MEMBER_ID = 2L;
    private static final Long RECORD_ID = 10L;
    private static final Long RECORD_FILE_ID = 11L;
    private static final Long STAGE_TEAM_ID = 4L;
    private static final Long OPERATOR_TEAM_ID = 5L;
    private static final Long FILE_ID = 20L;
    private static final Long NEW_FILE_ID = 21L;
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 20, 20, 0);
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-07-20T11:00:00Z"), ZoneId.of("Asia/Seoul"));

    @Mock
    private ActivityRecordMapper activityRecordMapper;
    @Mock
    private MemberService memberService;
    @Mock
    private FileService fileService;

    private ActivityRecordService activityRecordService;

    @BeforeEach
    void setUp() {
        activityRecordService = new ActivityRecordService(
                activityRecordMapper, memberService, fileService, CLOCK);
    }

    @Test
    void 일반_MEMBER는_소속_팀_활동_초안을_작성한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext());
        willAnswer(invocation -> {
            assignRecordId(invocation.getArgument(0), RECORD_ID);
            return 1;
        }).given(activityRecordMapper).insert(any());

        Long result = activityRecordService.createDraft(ACTOR_ID,
                writeParam(STAGE_TEAM_ID));

        assertThat(result).isEqualTo(RECORD_ID);
        verify(memberService).validateActiveTeam(STAGE_TEAM_ID);
    }

    @Test
    void 일반_MEMBER는_다른_팀_초안을_작성할_수_없고_ADMIN은_가능하다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext());

        assertThatThrownBy(() -> activityRecordService.createDraft(
                ACTOR_ID, writeParam(OPERATOR_TEAM_ID)))
                .isInstanceOf(ActivityRecordAccessDeniedException.class);

        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        willAnswer(invocation -> {
            assignRecordId(invocation.getArgument(0), RECORD_ID);
            return 1;
        }).given(activityRecordMapper).insert(any());
        assertThat(activityRecordService.createDraft(
                ACTOR_ID, writeParam(OPERATOR_TEAM_ID))).isEqualTo(RECORD_ID);
    }

    @Test
    void 작성자는_초안과_보완요청_기록을_수정하고_다른_MEMBER는_수정하지_못한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext());
        given(activityRecordMapper.lookupByIdForUpdate(RECORD_ID))
                .willReturn(Optional.of(draft(ACTOR_ID)));

        activityRecordService.update(ACTOR_ID, updateParam());

        verify(activityRecordMapper).update(any());
        given(memberService.lookupAccessContext(OTHER_MEMBER_ID))
                .willReturn(new MemberAccessContext(OTHER_MEMBER_ID,
                        STAGE_TEAM_ID, false, false, true));
        assertThatThrownBy(() -> activityRecordService.update(
                OTHER_MEMBER_ID, updateParam()))
                .isInstanceOf(ActivityRecordAccessDeniedException.class);
    }

    @Test
    void 현재_이미지_파일을_증빙으로_추가한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext());
        given(activityRecordMapper.lookupByIdForUpdate(RECORD_ID))
                .willReturn(Optional.of(draft(ACTOR_ID)));
        given(fileService.lookupPrivateReady(FILE_ID)).willReturn(imageFile(FILE_ID));
        given(activityRecordMapper.existsCurrentStoredFile(RECORD_ID, FILE_ID))
                .willReturn(false);
        given(activityRecordMapper.lookupNextDisplayOrder(
                RECORD_ID, ActivityFileRole.EVIDENCE)).willReturn(0);

        activityRecordService.addFile(ACTOR_ID,
                new ActivityFileAddParam(RECORD_ID, FILE_ID, ActivityFileRole.EVIDENCE));

        verify(activityRecordMapper).insertFile(any());
    }

    @Test
    void 이미지가_아니거나_현재_중복인_파일은_추가하지_않는다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext());
        given(activityRecordMapper.lookupByIdForUpdate(RECORD_ID))
                .willReturn(Optional.of(draft(ACTOR_ID)));
        given(fileService.lookupPrivateReady(FILE_ID)).willReturn(
                new FileReferenceResponse(FILE_ID, "evidence.pdf",
                        "application/pdf", 1024L, ACTOR_ID));

        assertThatThrownBy(() -> activityRecordService.addFile(ACTOR_ID,
                new ActivityFileAddParam(RECORD_ID, FILE_ID, ActivityFileRole.EVIDENCE)))
                .isInstanceOf(InvalidActivityRecordException.class);

        given(fileService.lookupPrivateReady(FILE_ID)).willReturn(imageFile(FILE_ID));
        given(activityRecordMapper.existsCurrentStoredFile(RECORD_ID, FILE_ID))
                .willReturn(true);
        assertThatThrownBy(() -> activityRecordService.addFile(ACTOR_ID,
                new ActivityFileAddParam(RECORD_ID, FILE_ID, ActivityFileRole.EVIDENCE)))
                .isInstanceOf(InvalidActivityRecordException.class);
        verify(activityRecordMapper, never()).insertFile(any());
    }

    @Test
    void 다른_MEMBER가_업로드한_비공개_파일은_연결하지_않는다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext());
        given(activityRecordMapper.lookupByIdForUpdate(RECORD_ID))
                .willReturn(Optional.of(draft(ACTOR_ID)));
        given(fileService.lookupPrivateReady(FILE_ID)).willReturn(
                new FileReferenceResponse(FILE_ID, "evidence.jpg",
                        "image/jpeg", 1024L, OTHER_MEMBER_ID));

        assertThatThrownBy(() -> activityRecordService.addFile(ACTOR_ID,
                new ActivityFileAddParam(RECORD_ID, FILE_ID,
                        ActivityFileRole.EVIDENCE)))
                .isInstanceOf(ActivityRecordAccessDeniedException.class);

        verify(activityRecordMapper, never()).insertFile(any());
    }

    @Test
    void 파일_교체는_새_연결을_먼저_저장하고_기존_연결에_참조를_남긴다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext());
        ActivityRecordFile original = currentFile(FILE_ID);
        given(activityRecordMapper.lookupFileByIdForUpdate(RECORD_FILE_ID))
                .willReturn(Optional.of(original));
        given(activityRecordMapper.lookupByIdForUpdate(RECORD_ID))
                .willReturn(Optional.of(draft(ACTOR_ID)));
        given(fileService.lookupPrivateReady(NEW_FILE_ID))
                .willReturn(imageFile(NEW_FILE_ID));
        given(activityRecordMapper.existsCurrentStoredFile(RECORD_ID, NEW_FILE_ID))
                .willReturn(false);
        willAnswer(invocation -> {
            assignRecordFileId(invocation.getArgument(0), 30L);
            return 1;
        }).given(activityRecordMapper).insertFile(any());

        activityRecordService.replaceFile(ACTOR_ID,
                new ActivityFileReplaceParam(RECORD_FILE_ID, NEW_FILE_ID));

        ArgumentCaptor<ActivityRecordFile> captor =
                ArgumentCaptor.forClass(ActivityRecordFile.class);
        verify(activityRecordMapper).updateFile(captor.capture());
        assertThat(captor.getValue().getReplacedByActivityRecordFileId()).isEqualTo(30L);
    }

    @Test
    void 증빙_사진이_없으면_제출할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext());
        given(activityRecordMapper.lookupByIdForUpdate(RECORD_ID))
                .willReturn(Optional.of(draft(ACTOR_ID)));
        given(activityRecordMapper.countCurrentFiles(
                RECORD_ID, ActivityFileRole.EVIDENCE)).willReturn(0);

        assertThatThrownBy(() -> activityRecordService.submit(
                ACTOR_ID, RECORD_ID, null))
                .isInstanceOf(InvalidActivityRecordException.class);

        verify(activityRecordMapper, never()).insertRevision(any());
    }

    @Test
    void 제출은_본문_revision과_상태_이력을_같이_저장한다() {
        ActivityRecord draft = draft(ACTOR_ID);
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext());
        given(activityRecordMapper.lookupByIdForUpdate(RECORD_ID))
                .willReturn(Optional.of(draft));
        given(activityRecordMapper.countCurrentFiles(
                RECORD_ID, ActivityFileRole.EVIDENCE)).willReturn(1);
        given(activityRecordMapper.lookupMaxRevisionNo(RECORD_ID))
                .willReturn(Optional.empty());

        int revisionNo = activityRecordService.submit(ACTOR_ID, RECORD_ID, null);

        assertThat(revisionNo).isEqualTo(1);
        ArgumentCaptor<ActivityRecordRevision> revisionCaptor =
                ArgumentCaptor.forClass(ActivityRecordRevision.class);
        verify(activityRecordMapper).insertRevision(revisionCaptor.capture());
        assertThat(revisionCaptor.getValue().getTitle()).isEqualTo(draft.getTitle());
        ArgumentCaptor<ActivityReviewHistory> historyCaptor =
                ArgumentCaptor.forClass(ActivityReviewHistory.class);
        verify(activityRecordMapper).insertReviewHistory(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getNewStatus())
                .isEqualTo(ActivityRecordStatus.SUBMITTED);
    }

    @Test
    void revision_번호가_한계에_도달하면_제출하지_않는다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext());
        given(activityRecordMapper.lookupByIdForUpdate(RECORD_ID))
                .willReturn(Optional.of(draft(ACTOR_ID)));
        given(activityRecordMapper.countCurrentFiles(
                RECORD_ID, ActivityFileRole.EVIDENCE)).willReturn(1);
        given(activityRecordMapper.lookupMaxRevisionNo(RECORD_ID))
                .willReturn(Optional.of(Integer.MAX_VALUE));

        assertThatThrownBy(() -> activityRecordService.submit(
                ACTOR_ID, RECORD_ID, null))
                .isInstanceOf(InvalidActivityRecordException.class);

        verify(activityRecordMapper, never()).insertRevision(any());
        verify(activityRecordMapper, never()).update(any());
    }

    @Test
    void 팀장은_소속_팀_제출을_승인하고_보완_요청에는_의견을_남긴다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(leaderContext());
        given(activityRecordMapper.lookupByIdForUpdate(RECORD_ID))
                .willReturn(Optional.of(submitted(OTHER_MEMBER_ID)));

        activityRecordService.approve(ACTOR_ID, RECORD_ID);

        verify(activityRecordMapper).update(any());
        given(activityRecordMapper.lookupByIdForUpdate(RECORD_ID))
                .willReturn(Optional.of(submitted(OTHER_MEMBER_ID)));
        assertThatThrownBy(() -> activityRecordService.requestRevision(
                ACTOR_ID, RECORD_ID, " "))
                .isInstanceOf(InvalidActivityRecordException.class);
        activityRecordService.requestRevision(ACTOR_ID, RECORD_ID, "인증 사진 보완");
        verify(activityRecordMapper, org.mockito.Mockito.times(2))
                .insertReviewHistory(any());
    }

    @Test
    void MEMBER는_검수할_수_없고_LEADER는_다른_팀을_검수할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext());
        given(activityRecordMapper.lookupByIdForUpdate(RECORD_ID))
                .willReturn(Optional.of(submitted(OTHER_MEMBER_ID)));
        assertThatThrownBy(() -> activityRecordService.approve(ACTOR_ID, RECORD_ID))
                .isInstanceOf(ActivityRecordAccessDeniedException.class);

        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(leaderContext());
        given(activityRecordMapper.lookupByIdForUpdate(RECORD_ID))
                .willReturn(Optional.of(submittedForTeam(OPERATOR_TEAM_ID)));
        assertThatThrownBy(() -> activityRecordService.approve(ACTOR_ID, RECORD_ID))
                .isInstanceOf(ActivityRecordAccessDeniedException.class);
    }

    @Test
    void 팀장이_기록을_보관하면_상태_이력을_남긴다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(leaderContext());
        given(activityRecordMapper.lookupByIdForUpdate(RECORD_ID))
                .willReturn(Optional.of(draft(OTHER_MEMBER_ID)));

        activityRecordService.archive(ACTOR_ID, RECORD_ID);

        ArgumentCaptor<ActivityReviewHistory> historyCaptor =
                ArgumentCaptor.forClass(ActivityReviewHistory.class);
        verify(activityRecordMapper).insertReviewHistory(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getPreviousStatus())
                .isEqualTo(ActivityRecordStatus.DRAFT);
        assertThat(historyCaptor.getValue().getNewStatus())
                .isEqualTo(ActivityRecordStatus.ARCHIVED);
    }

    @Test
    void 활성_MEMBER는_승인된_전체_팀_기록을_조회한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext());
        given(activityRecordMapper.searchApproved(any())).willReturn(List.of(summary()));

        List<ActivityRecordSummaryResponse> result = activityRecordService.searchApproved(
                ACTOR_ID, new ActivityRecordSearchParam(null, null, null, 0, 20));

        assertThat(result).hasSize(1);
        ArgumentCaptor<ActivityRecordSearchCondition> captor =
                ArgumentCaptor.forClass(ActivityRecordSearchCondition.class);
        verify(activityRecordMapper).searchApproved(captor.capture());
        assertThat(captor.getValue().teamId()).isNull();
    }

    @Test
    void 관리_목록은_ADMIN_전체_LEADER_소속팀_MEMBER_본인으로_제한한다() {
        given(activityRecordMapper.searchManageable(any())).willReturn(List.of());
        ActivityManageSearchParam param = new ActivityManageSearchParam(
                null, null, null, 0, 20);

        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        activityRecordService.searchManageable(ACTOR_ID, param);
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(leaderContext());
        activityRecordService.searchManageable(ACTOR_ID, param);
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext());
        activityRecordService.searchManageable(ACTOR_ID, param);

        ArgumentCaptor<ActivityManageSearchCondition> captor =
                ArgumentCaptor.forClass(ActivityManageSearchCondition.class);
        verify(activityRecordMapper, org.mockito.Mockito.times(3))
                .searchManageable(captor.capture());
        assertThat(captor.getAllValues().get(1).teamId()).isEqualTo(STAGE_TEAM_ID);
        assertThat(captor.getAllValues().get(2).createdByMemberId()).isEqualTo(ACTOR_ID);
    }

    @Test
    void 승인_전_상세는_작성자나_관리자만_조회한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext());
        given(activityRecordMapper.lookupManageContent(RECORD_ID))
                .willReturn(Optional.of(manageContent(ACTOR_ID)));
        given(activityRecordMapper.searchCurrentFileLinks(RECORD_ID)).willReturn(List.of());
        given(activityRecordMapper.searchRevisions(RECORD_ID)).willReturn(List.of());
        given(activityRecordMapper.searchReviewHistories(RECORD_ID)).willReturn(List.of());

        ActivityRecordManageDetailResponse result =
                activityRecordService.lookupManageable(ACTOR_ID, RECORD_ID);

        assertThat(result.activityRecordId()).isEqualTo(RECORD_ID);
        given(memberService.lookupAccessContext(OTHER_MEMBER_ID))
                .willReturn(new MemberAccessContext(OTHER_MEMBER_ID,
                        STAGE_TEAM_ID, false, false, true));
        assertThatThrownBy(() -> activityRecordService.lookupManageable(
                OTHER_MEMBER_ID, RECORD_ID))
                .isInstanceOf(ActivityRecordAccessDeniedException.class);
    }

    @Test
    void 승인된_상세와_현재_사진은_활성_MEMBER가_조회하고_다운로드한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext());
        given(activityRecordMapper.lookupApprovedContent(RECORD_ID))
                .willReturn(Optional.of(approvedContent()));
        given(activityRecordMapper.searchCurrentFileLinks(RECORD_ID))
                .willReturn(List.of(fileLink()));
        given(fileService.lookupPrivateReady(FILE_ID)).willReturn(imageFile(FILE_ID));

        ActivityRecordDetailResponse detail = activityRecordService.lookupApproved(
                ACTOR_ID, RECORD_ID);

        assertThat(detail.files()).hasSize(1);
        given(activityRecordMapper.existsApprovedCurrentFile(RECORD_ID, FILE_ID))
                .willReturn(true);
        given(fileService.createPrivateDownloadUrl(FILE_ID, FileAccessDecision.GRANTED))
                .willReturn("http://localhost:9000/evidence");
        assertThat(activityRecordService.createApprovedDownloadUrl(
                ACTOR_ID, RECORD_ID, FILE_ID))
                .isEqualTo("http://localhost:9000/evidence");
    }

    @Test
    void 작성자는_관리_가능한_기록의_현재_사진을_다운로드한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext());
        given(activityRecordMapper.lookupManageContent(RECORD_ID))
                .willReturn(Optional.of(manageContent(ACTOR_ID)));
        given(activityRecordMapper.existsCurrentStoredFile(RECORD_ID, FILE_ID))
                .willReturn(true);
        given(fileService.createPrivateDownloadUrl(
                FILE_ID, FileAccessDecision.GRANTED))
                .willReturn("http://localhost:9000/manage-evidence");

        String result = activityRecordService.createManageableDownloadUrl(
                ACTOR_ID, RECORD_ID, FILE_ID);

        assertThat(result).isEqualTo("http://localhost:9000/manage-evidence");
    }

    @Test
    void 권한이_없는_MEMBER는_관리용_사진을_다운로드하지_못한다() {
        given(memberService.lookupAccessContext(OTHER_MEMBER_ID))
                .willReturn(new MemberAccessContext(OTHER_MEMBER_ID,
                        STAGE_TEAM_ID, false, false, true));
        given(activityRecordMapper.lookupManageContent(RECORD_ID))
                .willReturn(Optional.of(manageContent(ACTOR_ID)));

        assertThatThrownBy(() -> activityRecordService
                .createManageableDownloadUrl(
                        OTHER_MEMBER_ID, RECORD_ID, FILE_ID))
                .isInstanceOf(ActivityRecordAccessDeniedException.class);

        verify(fileService, never()).createPrivateDownloadUrl(any(), any());
    }

    @Test
    void 현재_연결이_아닌_파일은_관리용으로_다운로드하지_못한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext());
        given(activityRecordMapper.lookupManageContent(RECORD_ID))
                .willReturn(Optional.of(manageContent(ACTOR_ID)));
        given(activityRecordMapper.existsCurrentStoredFile(RECORD_ID, FILE_ID))
                .willReturn(false);

        assertThatThrownBy(() -> activityRecordService
                .createManageableDownloadUrl(ACTOR_ID, RECORD_ID, FILE_ID))
                .isInstanceOf(ActivityRecordFileNotFoundException.class);

        verify(fileService, never()).createPrivateDownloadUrl(any(), any());
    }

    private ActivityRecordWriteParam writeParam(Long teamId) {
        return new ActivityRecordWriteParam(teamId, NOW.minusHours(2),
                "1막 연습", "런스루", 8);
    }

    private ActivityRecordUpdateParam updateParam() {
        return new ActivityRecordUpdateParam(RECORD_ID, NOW.minusHours(1),
                "수정 연습", "수정 내용", 9);
    }

    private MemberAccessContext adminContext() {
        return new MemberAccessContext(ACTOR_ID, STAGE_TEAM_ID, true, false, true);
    }

    private MemberAccessContext leaderContext() {
        return new MemberAccessContext(ACTOR_ID, STAGE_TEAM_ID, false, true, true);
    }

    private MemberAccessContext memberContext() {
        return new MemberAccessContext(ACTOR_ID, STAGE_TEAM_ID, false, false, true);
    }

    private ActivityRecord draft(Long creatorId) {
        return new ActivityRecord(RECORD_ID, STAGE_TEAM_ID, NOW.minusHours(2),
                "1막 연습", "런스루", 8, ActivityRecordStatus.DRAFT,
                creatorId, creatorId, null, null, null,
                NOW.minusDays(1), NOW.minusDays(1), null);
    }

    private ActivityRecord submitted(Long creatorId) {
        return draft(creatorId).submit(creatorId, NOW.minusHours(1));
    }

    private ActivityRecord submittedForTeam(Long teamId) {
        return new ActivityRecord(RECORD_ID, teamId, NOW.minusHours(2),
                "연습", "내용", 8, ActivityRecordStatus.SUBMITTED,
                OTHER_MEMBER_ID, OTHER_MEMBER_ID, NOW.minusHours(1),
                null, null, NOW.minusDays(1), NOW.minusHours(1), null);
    }

    private ActivityRecordFile currentFile(Long storedFileId) {
        return new ActivityRecordFile(RECORD_FILE_ID, RECORD_ID, storedFileId,
                ActivityFileRole.EVIDENCE, 0, ACTOR_ID,
                null, null, null, NOW.minusHours(1), NOW.minusHours(1));
    }

    private FileReferenceResponse imageFile(Long storedFileId) {
        return new FileReferenceResponse(storedFileId, "evidence.jpg",
                "image/jpeg", 1024L, ACTOR_ID);
    }

    private ActivityRecordSummaryResponse summary() {
        return new ActivityRecordSummaryResponse(RECORD_ID, STAGE_TEAM_ID,
                "무대팀", NOW.minusHours(2), "1막 연습", 8,
                ActivityRecordStatus.APPROVED, "작성자", FILE_ID, NOW);
    }

    private ActivityRecordManageContentResponse manageContent(Long creatorId) {
        return new ActivityRecordManageContentResponse(RECORD_ID, STAGE_TEAM_ID,
                "무대팀", NOW.minusHours(2), "1막 연습", "런스루", 8,
                ActivityRecordStatus.DRAFT, creatorId, "작성자", "작성자",
                null, null, NOW);
    }

    private ActivityRecordContentResponse approvedContent() {
        return new ActivityRecordContentResponse(RECORD_ID, STAGE_TEAM_ID,
                "무대팀", NOW.minusHours(2), "1막 연습", "런스루", 8,
                "작성자", NOW);
    }

    private ActivityFileLinkResponse fileLink() {
        return new ActivityFileLinkResponse(RECORD_FILE_ID, FILE_ID,
                ActivityFileRole.EVIDENCE, 0, ACTOR_ID, "작성자", NOW);
    }

    private void assignRecordId(ActivityRecord record, Long activityRecordId) {
        setField(record, "activityRecordId", activityRecordId);
    }

    private void assignRecordFileId(ActivityRecordFile file, Long fileId) {
        setField(file, "activityRecordFileId", fileId);
    }

    private void setField(Object target, String name, Long value) {
        try {
            Field field = target.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
