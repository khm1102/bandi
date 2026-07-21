package kr.ac.tukorea.bandi.domain.resource.service;

import kr.ac.tukorea.bandi.domain.file.dto.response.FileReferenceResponse;
import kr.ac.tukorea.bandi.domain.file.service.FileAccessDecision;
import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.resource.dto.request.ResourceManageSearchCondition;
import kr.ac.tukorea.bandi.domain.resource.dto.request.ResourceManageSearchParam;
import kr.ac.tukorea.bandi.domain.resource.dto.request.ResourceReadableSearchCondition;
import kr.ac.tukorea.bandi.domain.resource.dto.request.ResourceRevisionParam;
import kr.ac.tukorea.bandi.domain.resource.dto.request.ResourceSearchParam;
import kr.ac.tukorea.bandi.domain.resource.dto.request.ResourceUpdateParam;
import kr.ac.tukorea.bandi.domain.resource.dto.request.ResourceWriteParam;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceContentResponse;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceDetailResponse;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceFileLinkResponse;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceManageContentResponse;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceManageDetailResponse;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceManageSummaryResponse;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceRevisionResponse;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceSummaryResponse;
import kr.ac.tukorea.bandi.domain.resource.exception.InvalidResourceException;
import kr.ac.tukorea.bandi.domain.resource.exception.ResourceAccessDeniedException;
import kr.ac.tukorea.bandi.domain.resource.exception.ResourceNotFoundException;
import kr.ac.tukorea.bandi.domain.resource.mapper.ResourceMapper;
import kr.ac.tukorea.bandi.domain.resource.model.Resource;
import kr.ac.tukorea.bandi.domain.resource.model.ResourceFile;
import kr.ac.tukorea.bandi.domain.resource.model.ResourceStatus;
import kr.ac.tukorea.bandi.domain.resource.model.ResourceTargetScope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
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
class ResourceServiceTest {

    private static final Long ACTOR_ID = 1L;
    private static final Long RESOURCE_ID = 10L;
    private static final Long STAGE_TEAM_ID = 4L;
    private static final Long OPERATOR_TEAM_ID = 5L;
    private static final Long FILE_ID = 20L;
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 20, 12, 0);

    @Mock
    private ResourceMapper resourceMapper;
    @Mock
    private MemberService memberService;
    @Mock
    private FileService fileService;

    private ResourceService resourceService;

    @BeforeEach
    void setUp() {
        resourceService = new ResourceService(resourceMapper, memberService, fileService);
    }

    @Test
    void ADMIN은_파일_없이_전체_자료_초안을_만든다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        willAnswer(invocation -> {
            assignResourceId(invocation.getArgument(0), RESOURCE_ID);
            return 1;
        }).given(resourceMapper).insert(any());

        Long result = resourceService.createDraft(ACTOR_ID,
                writeParam(ResourceTargetScope.ALL, null, List.of()));

        assertThat(result).isEqualTo(RESOURCE_ID);
        verify(resourceMapper, never()).insertFile(any());
    }

    @Test
    void LEADER는_소속_팀_자료와_READY_파일_revision_1을_만든다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(leaderContext());
        given(fileService.lookupPrivateReady(FILE_ID)).willReturn(fileReference());
        willAnswer(invocation -> {
            assignResourceId(invocation.getArgument(0), RESOURCE_ID);
            return 1;
        }).given(resourceMapper).insert(any());

        resourceService.createDraft(ACTOR_ID,
                writeParam(ResourceTargetScope.TEAM, STAGE_TEAM_ID, List.of(FILE_ID)));

        ArgumentCaptor<ResourceFile> captor = ArgumentCaptor.forClass(ResourceFile.class);
        verify(resourceMapper).insertFile(captor.capture());
        assertThat(captor.getValue().getRevisionNo()).isEqualTo(1);
        assertThat(captor.getValue().getUploadedByMemberId()).isEqualTo(ACTOR_ID);
    }

    @Test
    void LEADER는_전체나_다른_팀_자료를_관리할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(leaderContext());

        assertThatThrownBy(() -> resourceService.createDraft(ACTOR_ID,
                writeParam(ResourceTargetScope.ALL, null, List.of())))
                .isInstanceOf(ResourceAccessDeniedException.class);
        assertThatThrownBy(() -> resourceService.createDraft(ACTOR_ID,
                writeParam(ResourceTargetScope.TEAM, OPERATOR_TEAM_ID, List.of())))
                .isInstanceOf(ResourceAccessDeniedException.class);
    }

    @Test
    void MEMBER와_비활성_ADMIN은_자료를_관리할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext());

        assertThatThrownBy(() -> resourceService.createDraft(ACTOR_ID,
                writeParam(ResourceTargetScope.TEAM, STAGE_TEAM_ID, List.of())))
                .isInstanceOf(ResourceAccessDeniedException.class);

        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(inactiveAdminContext());
        assertThatThrownBy(() -> resourceService.createDraft(ACTOR_ID,
                writeParam(ResourceTargetScope.ALL, null, List.of())))
                .isInstanceOf(ResourceAccessDeniedException.class);
    }

    @Test
    void 메타데이터_수정은_기존과_변경_대상_권한을_모두_검증한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(leaderContext());
        given(resourceMapper.lookupByIdForUpdate(RESOURCE_ID))
                .willReturn(Optional.of(persistedDraft(
                        ResourceTargetScope.TEAM, OPERATOR_TEAM_ID)));

        assertThatThrownBy(() -> resourceService.update(ACTOR_ID,
                new ResourceUpdateParam(RESOURCE_ID, ResourceTargetScope.TEAM,
                        STAGE_TEAM_ID, "SCRIPT", "수정", "설명", false)))
                .isInstanceOf(ResourceAccessDeniedException.class);

        verify(resourceMapper, never()).update(any());
    }

    @Test
    void 파일_교체는_현재_revision에_1을_더해_모든_파일을_연결한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        given(resourceMapper.lookupByIdForUpdate(RESOURCE_ID))
                .willReturn(Optional.of(persistedDraft(ResourceTargetScope.ALL, null)));
        given(resourceMapper.lookupMaxRevisionForUpdate(RESOURCE_ID))
                .willReturn(Optional.of(2));
        given(fileService.lookupPrivateReady(FILE_ID)).willReturn(fileReference());

        int result = resourceService.replaceFiles(ACTOR_ID,
                new ResourceRevisionParam(RESOURCE_ID, List.of(FILE_ID)));

        assertThat(result).isEqualTo(3);
        ArgumentCaptor<ResourceFile> captor = ArgumentCaptor.forClass(ResourceFile.class);
        verify(resourceMapper).insertFile(captor.capture());
        assertThat(captor.getValue().getRevisionNo()).isEqualTo(3);
    }

    @Test
    void 빈_파일이나_중복_파일_revision은_파일_조회_전에_거부한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        given(resourceMapper.lookupByIdForUpdate(RESOURCE_ID))
                .willReturn(Optional.of(persistedDraft(ResourceTargetScope.ALL, null)));

        assertThatThrownBy(() -> resourceService.replaceFiles(ACTOR_ID,
                new ResourceRevisionParam(RESOURCE_ID, List.of())))
                .isInstanceOf(InvalidResourceException.class);
        assertThatThrownBy(() -> resourceService.replaceFiles(ACTOR_ID,
                new ResourceRevisionParam(RESOURCE_ID, List.of(FILE_ID, FILE_ID))))
                .isInstanceOf(InvalidResourceException.class);

        verify(fileService, never()).lookupPrivateReady(any());
    }

    @Test
    void 현재_파일이_없는_초안은_게시할_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        given(resourceMapper.lookupByIdForUpdate(RESOURCE_ID))
                .willReturn(Optional.of(persistedDraft(ResourceTargetScope.ALL, null)));
        given(resourceMapper.lookupMaxRevisionForUpdate(RESOURCE_ID))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> resourceService.publish(ACTOR_ID, RESOURCE_ID))
                .isInstanceOf(InvalidResourceException.class);

        verify(resourceMapper, never()).update(any());
    }

    @Test
    void 현재_파일이_있는_초안을_게시하고_자료를_보관한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        given(resourceMapper.lookupByIdForUpdate(RESOURCE_ID))
                .willReturn(Optional.of(persistedDraft(ResourceTargetScope.ALL, null)));
        given(resourceMapper.lookupMaxRevisionForUpdate(RESOURCE_ID))
                .willReturn(Optional.of(1));
        given(resourceMapper.existsFilesInRevision(RESOURCE_ID, 1)).willReturn(true);

        resourceService.publish(ACTOR_ID, RESOURCE_ID);

        given(resourceMapper.lookupByIdForUpdate(RESOURCE_ID))
                .willReturn(Optional.of(publishedResource()));
        resourceService.archive(ACTOR_ID, RESOURCE_ID);

        verify(resourceMapper, org.mockito.Mockito.times(2)).update(any());
    }

    @Test
    void ADMIN은_운영_목록을_필터링하고_LEADER는_소속_팀으로_강제된다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        given(resourceMapper.searchManageable(any())).willReturn(List.of(manageSummary()));

        resourceService.searchManageable(ACTOR_ID,
                new ResourceManageSearchParam("대본", "SCRIPT", ResourceStatus.DRAFT,
                        ResourceTargetScope.ALL, null, 0, 20));

        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(leaderContext());
        resourceService.searchManageable(ACTOR_ID,
                new ResourceManageSearchParam(null, null, null, null,
                        null, 0, 20));

        ArgumentCaptor<ResourceManageSearchCondition> captor =
                ArgumentCaptor.forClass(ResourceManageSearchCondition.class);
        verify(resourceMapper, org.mockito.Mockito.times(2)).searchManageable(captor.capture());
        assertThat(captor.getAllValues().get(1).targetScope())
                .isEqualTo(ResourceTargetScope.TEAM);
        assertThat(captor.getAllValues().get(1).teamId()).isEqualTo(STAGE_TEAM_ID);
    }

    @Test
    void 활성_MEMBER는_전체와_소속_팀_게시_자료를_조회한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext());
        given(resourceMapper.searchReadable(any())).willReturn(List.of(readSummary()));

        List<ResourceSummaryResponse> result = resourceService.searchReadable(ACTOR_ID,
                new ResourceSearchParam("대본", null, 0, 20));

        assertThat(result).hasSize(1);
        ArgumentCaptor<ResourceReadableSearchCondition> captor =
                ArgumentCaptor.forClass(ResourceReadableSearchCondition.class);
        verify(resourceMapper).searchReadable(captor.capture());
        assertThat(captor.getValue().memberTeamId()).isEqualTo(STAGE_TEAM_ID);
        assertThat(captor.getValue().admin()).isFalse();
    }

    @Test
    void 비활성_멤버는_자료를_읽을_수_없다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(inactiveMemberContext());

        assertThatThrownBy(() -> resourceService.searchReadable(ACTOR_ID,
                new ResourceSearchParam(null, null, 0, 20)))
                .isInstanceOf(ResourceAccessDeniedException.class);
    }

    @Test
    void 읽기_상세는_현재_revision_파일만_조립한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext());
        given(resourceMapper.lookupReadableContent(RESOURCE_ID, STAGE_TEAM_ID, false))
                .willReturn(Optional.of(readContent()));
        given(resourceMapper.searchCurrentFileLinks(RESOURCE_ID))
                .willReturn(List.of(fileLink(2)));
        given(fileService.lookupPrivateReady(FILE_ID)).willReturn(fileReference());

        ResourceDetailResponse result = resourceService.lookupReadable(ACTOR_ID, RESOURCE_ID);

        assertThat(result.files()).hasSize(1);
        assertThat(result.files().get(0).revisionNo()).isEqualTo(2);
    }

    @Test
    void 현재_revision에_연결되고_읽을_수_있는_파일만_다운로드한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(memberContext());
        given(resourceMapper.existsReadableCurrentFile(
                RESOURCE_ID, FILE_ID, STAGE_TEAM_ID, false)).willReturn(true);
        given(fileService.openPrivateDownload(FILE_ID, FileAccessDecision.GRANTED))
                .willReturn(download());

        var result = resourceService.openDownload(ACTOR_ID, RESOURCE_ID, FILE_ID);

        assertThat(result.originalName()).isEqualTo("proof.png");
    }

    @Test
    void 운영_상세는_모든_revision_이력을_최신순으로_조립한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        given(resourceMapper.lookupManageContent(RESOURCE_ID))
                .willReturn(Optional.of(manageContent()));
        given(resourceMapper.searchFileLinks(RESOURCE_ID))
                .willReturn(List.of(fileLink(2), fileLink(1)));
        given(fileService.lookupPrivateReady(FILE_ID)).willReturn(fileReference());

        ResourceManageDetailResponse result = resourceService.lookupManageable(
                ACTOR_ID, RESOURCE_ID);

        assertThat(result.revisions()).extracting(ResourceRevisionResponse::revisionNo)
                .containsExactly(2, 1);
    }

    @Test
    void 존재하지_않는_자료는_관리_상세에서_예외가_발생한다() {
        given(memberService.lookupAccessContext(ACTOR_ID)).willReturn(adminContext());
        given(resourceMapper.lookupManageContent(RESOURCE_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> resourceService.lookupManageable(ACTOR_ID, RESOURCE_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private ResourceWriteParam writeParam(ResourceTargetScope scope, Long teamId,
                                           List<Long> fileIds) {
        return new ResourceWriteParam(scope, teamId, "SCRIPT", "자료 제목",
                "자료 설명", true, fileIds);
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

    private MemberAccessContext inactiveMemberContext() {
        return new MemberAccessContext(ACTOR_ID, STAGE_TEAM_ID, false, false, false);
    }

    private Resource persistedDraft(ResourceTargetScope scope, Long teamId) {
        return new Resource(RESOURCE_ID, scope, teamId, "SCRIPT", "자료 제목",
                "자료 설명", ResourceStatus.DRAFT, true, ACTOR_ID, ACTOR_ID,
                NOW.minusDays(1), NOW.minusHours(1), null);
    }

    private Resource publishedResource() {
        return persistedDraft(ResourceTargetScope.ALL, null).publish(ACTOR_ID);
    }

    private FileReferenceResponse fileReference() {
        return new FileReferenceResponse(FILE_ID, "script.pdf", "application/pdf", 1024L);
    }

    private ResourceManageSummaryResponse manageSummary() {
        return new ResourceManageSummaryResponse(RESOURCE_ID, ResourceTargetScope.ALL,
                null, null, "SCRIPT", "자료 제목", ResourceStatus.DRAFT,
                true, 1, "관리자", NOW);
    }

    private ResourceSummaryResponse readSummary() {
        return new ResourceSummaryResponse(RESOURCE_ID, ResourceTargetScope.ALL,
                null, null, "SCRIPT", "자료 제목", true, 1, "관리자", NOW);
    }

    private ResourceManageContentResponse manageContent() {
        return new ResourceManageContentResponse(RESOURCE_ID, ResourceTargetScope.ALL,
                null, null, "SCRIPT", "자료 제목", "자료 설명",
                ResourceStatus.DRAFT, true, "관리자", "관리자", NOW);
    }

    private ResourceContentResponse readContent() {
        return new ResourceContentResponse(RESOURCE_ID, ResourceTargetScope.ALL,
                null, null, "SCRIPT", "자료 제목", "자료 설명", true,
                "관리자", NOW);
    }

    private ResourceFileLinkResponse fileLink(int revisionNo) {
        return new ResourceFileLinkResponse(FILE_ID, revisionNo, 0,
                ACTOR_ID, "관리자", NOW);
    }

    private kr.ac.tukorea.bandi.global.response.FileDownloadResponse download() {
        return new kr.ac.tukorea.bandi.global.response.FileDownloadResponse(
                "proof.png", "image/png", 4,
                new org.springframework.core.io.InputStreamResource(
                        new java.io.ByteArrayInputStream(new byte[]{1, 2, 3, 4})));
    }

    private void assignResourceId(Resource resource, Long resourceId) {
        try {
            Field field = Resource.class.getDeclaredField("resourceId");
            field.setAccessible(true);
            field.set(resource, resourceId);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
