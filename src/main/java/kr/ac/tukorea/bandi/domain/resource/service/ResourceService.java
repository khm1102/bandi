package kr.ac.tukorea.bandi.domain.resource.service;

import kr.ac.tukorea.bandi.domain.file.dto.response.FileReferenceResponse;
import kr.ac.tukorea.bandi.domain.file.service.FileAccessDecision;
import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.global.response.FileDownloadResponse;
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
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceFileResponse;
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
import kr.ac.tukorea.bandi.domain.resource.model.ResourceTargetScope;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResourceService {

    private final ResourceMapper resourceMapper;
    private final MemberService memberService;
    private final FileService fileService;

    @Transactional
    public Long createDraft(Long actorMemberId, ResourceWriteParam param) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        validateManagement(access, param.targetScope(), param.teamId());
        validateActiveTarget(param.targetScope(), param.teamId());
        validateFiles(param.storedFileIds(), actorMemberId, true);
        Resource resource = Resource.draft(param.targetScope(), param.teamId(),
                param.categoryCode(), param.title(), param.description(),
                param.pinned(), actorMemberId);
        resourceMapper.insert(resource);
        if (!param.storedFileIds().isEmpty()) {
            insertRevision(resource.getResourceId(), 1,
                    param.storedFileIds(), actorMemberId);
        }
        return resource.getResourceId();
    }

    @Transactional
    public void update(Long actorMemberId, ResourceUpdateParam param) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        Resource original = lock(param.resourceId());
        validateManagement(access, original.getTargetScope(), original.getTeamId());
        validateManagement(access, param.targetScope(), param.teamId());
        validateActiveTarget(param.targetScope(), param.teamId());
        Resource changed = original.edit(param.targetScope(), param.teamId(),
                param.categoryCode(), param.title(), param.description(),
                param.pinned(), actorMemberId);
        resourceMapper.update(changed);
    }

    @Transactional
    public int replaceFiles(Long actorMemberId, ResourceRevisionParam param) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        Resource resource = lock(param.resourceId());
        validateManagement(access, resource.getTargetScope(), resource.getTeamId());
        validateFiles(param.storedFileIds(), actorMemberId, false);
        int currentRevision = resourceMapper.lookupMaxRevisionForUpdate(param.resourceId())
                .orElse(0);
        if (currentRevision == Integer.MAX_VALUE) {
            throw new InvalidResourceException("revision-overflow");
        }
        int nextRevision = currentRevision + 1;
        insertRevision(param.resourceId(), nextRevision,
                param.storedFileIds(), actorMemberId);
        return nextRevision;
    }

    @Transactional
    public void publish(Long actorMemberId, Long resourceId) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        Resource resource = lock(resourceId);
        validateManagement(access, resource.getTargetScope(), resource.getTeamId());
        validateActiveTarget(resource.getTargetScope(), resource.getTeamId());
        int currentRevision = resourceMapper.lookupMaxRevisionForUpdate(resourceId)
                .orElseThrow(() -> new InvalidResourceException("files"));
        if (!resourceMapper.existsFilesInRevision(resourceId, currentRevision)) {
            throw new InvalidResourceException("files");
        }
        resourceMapper.update(resource.publish(actorMemberId));
    }

    @Transactional
    public void archive(Long actorMemberId, Long resourceId) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        Resource resource = lock(resourceId);
        validateManagement(access, resource.getTargetScope(), resource.getTeamId());
        resourceMapper.update(resource.archive(actorMemberId));
    }

    public List<ResourceManageSummaryResponse> searchManageable(
            Long actorMemberId, ResourceManageSearchParam param) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        ResourceManageSearchCondition condition;
        if (access.canManageGlobal()) {
            condition = ResourceManageSearchCondition.forAdmin(param);
        } else if (access.canManageTeam(access.teamId())) {
            condition = ResourceManageSearchCondition.forLeader(param, access.teamId());
        } else {
            throw new ResourceAccessDeniedException();
        }
        return resourceMapper.searchManageable(condition);
    }

    public ResourceManageDetailResponse lookupManageable(Long actorMemberId,
                                                         Long resourceId) {
        MemberAccessContext access = memberService.lookupAccessContext(actorMemberId);
        ResourceManageContentResponse content = resourceMapper.lookupManageContent(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException(resourceId));
        validateManagement(access, content.targetScope(), content.teamId());
        List<ResourceRevisionResponse> revisions = groupRevisions(
                resourceMapper.searchFileLinks(resourceId));
        return ResourceManageDetailResponse.of(content, revisions);
    }

    public List<ResourceSummaryResponse> searchReadable(
            Long memberId, ResourceSearchParam param) {
        MemberAccessContext access = readableAccess(memberId);
        return resourceMapper.searchReadable(ResourceReadableSearchCondition.from(
                param, access.teamId(), access.canManageGlobal()));
    }

    public ResourceDetailResponse lookupReadable(Long memberId, Long resourceId) {
        MemberAccessContext access = readableAccess(memberId);
        ResourceContentResponse content = resourceMapper.lookupReadableContent(
                        resourceId, access.teamId(), access.canManageGlobal())
                .orElseThrow(() -> new ResourceNotFoundException(resourceId));
        return ResourceDetailResponse.of(content,
                toFiles(resourceMapper.searchCurrentFileLinks(resourceId)));
    }

    public FileDownloadResponse openDownload(Long memberId, Long resourceId, Long storedFileId) {
        MemberAccessContext access = readableAccess(memberId);
        boolean readable = resourceMapper.existsReadableCurrentFile(resourceId,
                storedFileId, access.teamId(), access.canManageGlobal());
        if (!readable) {
            throw new ResourceNotFoundException(resourceId);
        }
        return fileService.openPrivateDownload(
                storedFileId, FileAccessDecision.GRANTED);
    }

    private Resource lock(Long resourceId) {
        return resourceMapper.lookupByIdForUpdate(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException(resourceId));
    }

    private MemberAccessContext readableAccess(Long memberId) {
        MemberAccessContext access = memberService.lookupAccessContext(memberId);
        if (!access.canReadInternal()) {
            throw new ResourceAccessDeniedException();
        }
        return access;
    }

    private void validateManagement(MemberAccessContext access,
                                    ResourceTargetScope scope, Long teamId) {
        boolean allowed = scope == ResourceTargetScope.ALL
                ? access.canManageGlobal()
                : access.canManageTeam(teamId);
        if (!allowed) {
            throw new ResourceAccessDeniedException();
        }
    }

    private void validateActiveTarget(ResourceTargetScope scope, Long teamId) {
        if (scope == ResourceTargetScope.TEAM) {
            memberService.validateActiveTeam(teamId);
        }
    }

    private void validateFiles(List<Long> storedFileIds, Long actorMemberId,
                               boolean allowEmpty) {
        if ((!allowEmpty && storedFileIds.isEmpty())
                || storedFileIds.stream().anyMatch(fileId -> fileId == null)
                || new HashSet<>(storedFileIds).size() != storedFileIds.size()) {
            throw new InvalidResourceException("files");
        }
        storedFileIds.forEach(storedFileId ->
                fileService.validatePrivateReadyOwnedBy(storedFileId, actorMemberId));
    }

    private void insertRevision(Long resourceId, int revisionNo,
                                List<Long> storedFileIds, Long actorMemberId) {
        for (int index = 0; index < storedFileIds.size(); index++) {
            ResourceFile resourceFile = ResourceFile.create(resourceId,
                    storedFileIds.get(index), revisionNo, index, actorMemberId);
            resourceMapper.insertFile(resourceFile);
        }
    }

    private List<ResourceFileResponse> toFiles(List<ResourceFileLinkResponse> links) {
        return links.stream().map(this::toFile).toList();
    }

    private ResourceFileResponse toFile(ResourceFileLinkResponse link) {
        FileReferenceResponse file = fileService.lookupPrivateReady(link.storedFileId());
        return new ResourceFileResponse(file.storedFileId(), file.originalName(),
                file.contentType(), file.sizeBytes(), link.revisionNo(),
                link.displayOrder(), link.uploadedByMemberId(),
                link.uploadedByName(), link.uploadedDttm());
    }

    private List<ResourceRevisionResponse> groupRevisions(
            List<ResourceFileLinkResponse> links) {
        Map<Integer, List<ResourceFileResponse>> grouped = new LinkedHashMap<>();
        for (ResourceFileLinkResponse link : links) {
            grouped.computeIfAbsent(link.revisionNo(), key -> new ArrayList<>())
                    .add(toFile(link));
        }
        return grouped.entrySet().stream()
                .map(entry -> new ResourceRevisionResponse(
                        entry.getKey(), entry.getValue()))
                .toList();
    }
}
