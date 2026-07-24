package kr.ac.tukorea.bandi.domain.resource.service;

import kr.ac.tukorea.bandi.domain.file.dto.response.FileReferenceResponse;
import kr.ac.tukorea.bandi.domain.file.service.FileAccessDecision;
import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.notice.service.MarkdownRenderer;
import kr.ac.tukorea.bandi.domain.resource.dto.request.ResourceWriteRequest;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceDetailResponse;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceFileLinkResponse;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceFileResponse;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceSummaryResponse;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourcePublicShareResponse;
import kr.ac.tukorea.bandi.domain.resource.exception.ResourceAccessDeniedException;
import kr.ac.tukorea.bandi.domain.resource.exception.ResourceNotFoundException;
import kr.ac.tukorea.bandi.domain.resource.mapper.ResourceMapper;
import kr.ac.tukorea.bandi.domain.resource.model.Resource;
import kr.ac.tukorea.bandi.domain.resource.model.ResourceFile;
import kr.ac.tukorea.bandi.domain.share.service.ShareTokenGenerator;
import kr.ac.tukorea.bandi.global.response.FileDownloadResponse;
import kr.ac.tukorea.bandi.global.response.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class ResourceService {
    private final ResourceMapper resourceMapper;
    private final MemberService memberService;
    private final FileService fileService;
    private final MarkdownRenderer markdownRenderer;
    private final ResourceLinkPreviewFetcher linkPreviewFetcher;
    private final ResourceLinkPreviewRetirementService linkPreviewRetirementService;
    private final ShareTokenGenerator shareTokenGenerator;

    @Autowired
    public ResourceService(ResourceMapper resourceMapper, MemberService memberService,
                           FileService fileService, MarkdownRenderer markdownRenderer,
                           ResourceLinkPreviewFetcher linkPreviewFetcher,
                           ResourceLinkPreviewRetirementService linkPreviewRetirementService,
                           ShareTokenGenerator shareTokenGenerator) {
        this.resourceMapper = resourceMapper;
        this.memberService = memberService;
        this.fileService = fileService;
        this.markdownRenderer = markdownRenderer;
        this.linkPreviewFetcher = linkPreviewFetcher;
        this.linkPreviewRetirementService = linkPreviewRetirementService;
        this.shareTokenGenerator = shareTokenGenerator;
    }

    ResourceService(ResourceMapper resourceMapper, MemberService memberService,
                    FileService fileService, MarkdownRenderer markdownRenderer,
                    ResourceLinkPreviewFetcher linkPreviewFetcher,
                    ResourceLinkPreviewRetirementService linkPreviewRetirementService) {
        this(resourceMapper, memberService, fileService, markdownRenderer,
                linkPreviewFetcher, linkPreviewRetirementService, new ShareTokenGenerator());
    }

    public PageResponse<ResourceSummaryResponse> search(Long memberId, String keyword, int page, int pageSize) {
        requireReadable(memberId);
        return PageResponse.of(resourceMapper.search(keyword, pageSize, (long) page * pageSize), page, pageSize, resourceMapper.count(keyword));
    }

    public ResourceDetailResponse lookup(Long memberId, Long resourceId) {
        requireReadable(memberId);
        ResourceMapper.ResourceDetailRow row = resourceMapper.lookupDetail(resourceId).orElseThrow(() -> new ResourceNotFoundException(resourceId));
        List<ResourceFileResponse> files = resourceMapper.searchFiles(resourceId).stream().map(this::file).toList();
        java.util.Map<Long, String> imageUrls = files.stream().filter(file -> file.contentType().startsWith("image/"))
                .collect(java.util.stream.Collectors.toMap(ResourceFileResponse::storedFileId,
                        file -> "/api/resources/" + resourceId + "/files/" + file.storedFileId() + "/inline"));
        return new ResourceDetailResponse(row.resourceId(), row.title(), row.createdByName(), row.updatedByName(), row.createdDttm(), row.updatedDttm(), row.bodyMarkdown(),
                markdownRenderer.renderInternalImagesOnly(row.bodyMarkdown(), imageUrls), files, resourceMapper.searchLinkPreviews(resourceId), canManage(memberId, row.createdByMemberId()),
                canManage(memberId, row.createdByMemberId()), resourceMapper.existsShareToken(resourceId));
    }

    @Transactional
    public String issuePublicShare(Long memberId, Long resourceId) {
        requireReadable(memberId);
        Resource resource = lock(resourceId);
        requireManager(memberId, resource);
        return resourceMapper.lookupShareTokenForUpdate(resourceId)
                .orElseGet(() -> createPublicShareToken(resourceId));
    }

    @Transactional
    public void revokePublicShare(Long memberId, Long resourceId) {
        requireReadable(memberId);
        Resource resource = lock(resourceId);
        requireManager(memberId, resource);
        resourceMapper.updateShareToken(resourceId, null);
    }

    public ResourcePublicShareResponse lookupPublicShare(String shareToken) {
        return resourceMapper.lookupPublicShare(shareToken)
                .orElseThrow(() -> new ResourceNotFoundException(null));
    }

    public kr.ac.tukorea.bandi.domain.notice.service.SafeMarkdownHtml preview(Long memberId, String bodyMarkdown) {
        requireReadable(memberId);
        return markdownRenderer.renderInternalImagesOnly(bodyMarkdown == null ? "" : bodyMarkdown, java.util.Map.of());
    }

    @Transactional
    public Long create(Long memberId, ResourceWriteRequest request) {
        requireReadable(memberId);
        validateFiles(null, request.attachmentFileIds(), request.bodyMarkdown(), memberId);
        Resource resource = Resource.create(request.title(), request.bodyMarkdown(), memberId);
        resourceMapper.insert(resource);
        replaceFiles(resource.getResourceId(), request.attachmentFileIds(), memberId);
        replacePreviews(resource.getResourceId(), memberId, request.bodyMarkdown());
        return resource.getResourceId();
    }

    @Transactional
    public void update(Long memberId, Long resourceId, ResourceWriteRequest request) {
        requireReadable(memberId);
        Resource resource = lock(resourceId);
        requireManager(memberId, resource);
        validateFiles(resourceId, request.attachmentFileIds(), request.bodyMarkdown(), memberId);
        resourceMapper.update(resource.edit(request.title(), request.bodyMarkdown(), memberId));
        resourceMapper.removeFiles(resourceId);
        replaceFiles(resourceId, request.attachmentFileIds(), memberId);
        replacePreviews(resourceId, memberId, request.bodyMarkdown());
    }

    @Transactional
    public void delete(Long memberId, Long resourceId) {
        requireReadable(memberId);
        Resource resource = lock(resourceId);
        requireManager(memberId, resource);
        retirePreviewImages(resourceId);
        resourceMapper.delete(resourceId);
    }

    public FileDownloadResponse openDownload(Long memberId, Long resourceId, Long storedFileId) {
        requireReadable(memberId);
        if (!resourceMapper.existsFile(resourceId, storedFileId)) { throw new ResourceNotFoundException(resourceId); }
        return fileService.openPrivateDownload(storedFileId, FileAccessDecision.GRANTED);
    }

    public FileDownloadResponse openPreviewImage(Long memberId, Long resourceId,
                                                 Long storedFileId) {
        requireReadable(memberId);
        if (!resourceMapper.existsPreviewImage(resourceId, storedFileId)) {
            throw new ResourceNotFoundException(resourceId);
        }
        return fileService.openPrivateDownload(storedFileId, FileAccessDecision.GRANTED);
    }

    private void replacePreviews(Long resourceId, Long memberId, String markdown) {
        retirePreviewImages(resourceId);
        linkPreviewFetcher.fetchAll(resourceId, memberId, extractStandaloneUrls(markdown))
                .values().forEach(resourceMapper::insertLinkPreview);
    }

    private void retirePreviewImages(Long resourceId) {
        List<Long> previewImageFileIds = resourceMapper.searchPreviewImageFileIds(resourceId);
        resourceMapper.removeLinkPreviews(resourceId);
        previewImageFileIds.forEach(linkPreviewRetirementService::queue);
    }
    private Set<String> extractStandaloneUrls(String markdown) {
        Set<String> urls = new java.util.LinkedHashSet<>();
        for (String line : markdown.split("\\R")) { if (line.matches("https://[^\\s]+")) { urls.add(line.strip()); } }
        return urls.stream().limit(5).collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
    }
    private void replaceFiles(Long resourceId, List<Long> ids, Long memberId) {
        for (int index = 0; index < ids.size(); index += 1) {
            resourceMapper.insertFile(ResourceFile.attach(resourceId, ids.get(index), index,
                    memberId));
        }
    }

    private void validateFiles(Long resourceId, List<Long> ids, String markdown,
                               Long memberId) {
        if (new HashSet<>(ids).size() != ids.size()) {
            throw new kr.ac.tukorea.bandi.domain.resource.exception.InvalidResourceException("files");
        }
        Set<Long> existingFileIds = resourceId == null ? Set.of()
                : resourceMapper.searchFiles(resourceId).stream()
                .map(ResourceFileLinkResponse::storedFileId).collect(java.util.stream.Collectors.toSet());
        HashMap<Long, FileReferenceResponse> files = new HashMap<>();
        for (Long storedFileId : ids) {
            if (!existingFileIds.contains(storedFileId)) {
                fileService.validatePrivateReadyOwnedBy(storedFileId, memberId);
            }
            files.put(storedFileId, fileService.lookupPrivateReady(storedFileId));
        }
        validateAttachmentImages(markdown, files);
    }

    private void validateAttachmentImages(String markdown,
                                          java.util.Map<Long, FileReferenceResponse> files) {
        markdownRenderer.extractAttachmentImageReferences(markdown).forEach(reference -> {
            Long storedFileId = Long.valueOf(reference.substring("attachment://".length()));
            FileReferenceResponse file = files.get(storedFileId);
            if (file == null || file.contentType() == null
                    || !file.contentType().startsWith("image/")) {
                throw new kr.ac.tukorea.bandi.domain.resource.exception.InvalidResourceException("image");
            }
        });
    }
    private ResourceFileResponse file(ResourceFileLinkResponse link) { FileReferenceResponse file = fileService.lookupPrivateReady(link.storedFileId()); return new ResourceFileResponse(file.storedFileId(), file.originalName(), file.contentType(), file.sizeBytes(), link.displayOrder(), link.uploadedByMemberId(), link.uploadedByName(), link.uploadedDttm()); }
    private Resource lock(Long id) { return resourceMapper.lookupByIdForUpdate(id).orElseThrow(() -> new ResourceNotFoundException(id)); }
    private void requireReadable(Long memberId) { if (!memberService.lookupAccessContext(memberId).canReadInternal()) { throw new ResourceAccessDeniedException(); } }
    private boolean canManage(Long memberId, Long creatorId) { MemberAccessContext access = memberService.lookupAccessContext(memberId); return creatorId.equals(memberId) || access.canManageGlobal(); }
    private void requireManager(Long memberId, Resource resource) { if (!canManage(memberId, resource.getCreatedByMemberId())) { throw new ResourceAccessDeniedException(); } }
    private String createPublicShareToken(Long resourceId) { String token = shareTokenGenerator.generate(); resourceMapper.updateShareToken(resourceId, token); return token; }
}
