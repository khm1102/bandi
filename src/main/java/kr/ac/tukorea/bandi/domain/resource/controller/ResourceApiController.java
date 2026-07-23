package kr.ac.tukorea.bandi.domain.resource.controller;

import kr.ac.tukorea.bandi.domain.resource.dto.request.ResourceWriteRequest;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceDetailResponse;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceIdentifierResponse;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceSummaryResponse;
import kr.ac.tukorea.bandi.domain.resource.service.ResourceService;
import kr.ac.tukorea.bandi.global.response.FileDownloadResponse;
import kr.ac.tukorea.bandi.global.response.PageResponse;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import kr.ac.tukorea.bandi.global.swagger.ResourceApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceApiController implements ResourceApiDocs {

    private final ResourceService resourceService;

    @Override
    public ResponseEntity<PageResponse<ResourceSummaryResponse>> search(
            @LoginMember Long memberId, @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(resourceService.search(memberId, q, page, pageSize));
    }

    @Override
    public ResponseEntity<ResourceDetailResponse> lookup(
            @LoginMember Long memberId, @PathVariable Long resourceId) {
        return ResponseEntity.ok(resourceService.lookup(memberId, resourceId));
    }

    @Override
    public ResponseEntity<java.util.Map<String, String>> preview(
            @LoginMember Long memberId, @RequestBody java.util.Map<String, String> request) {
        return ResponseEntity.ok(java.util.Map.of("html",
                resourceService.preview(memberId, request.get("bodyMarkdown")).getValue()));
    }

    @Override
    public ResponseEntity<ResourceIdentifierResponse> create(
            @LoginMember Long memberId, @RequestBody @jakarta.validation.Valid ResourceWriteRequest request) {
        Long resourceId = resourceService.create(memberId, request);
        return ResponseEntity.created(URI.create("/api/resources/" + resourceId))
                .body(new ResourceIdentifierResponse(resourceId));
    }

    @Override
    public ResponseEntity<Void> update(
            @LoginMember Long memberId, @PathVariable Long resourceId,
            @RequestBody @jakarta.validation.Valid ResourceWriteRequest request) {
        resourceService.update(memberId, resourceId, request);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> delete(
            @LoginMember Long memberId, @PathVariable Long resourceId) {
        resourceService.delete(memberId, resourceId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Resource> download(
            @LoginMember Long memberId, @PathVariable Long resourceId,
            @PathVariable Long storedFileId) {
        return response(resourceService.openDownload(memberId, resourceId, storedFileId), true);
    }

    @Override
    public ResponseEntity<Resource> inline(
            @LoginMember Long memberId, @PathVariable Long resourceId,
            @PathVariable Long storedFileId) {
        return response(resourceService.openDownload(memberId, resourceId, storedFileId), false);
    }

    @Override
    public ResponseEntity<Resource> inlineLinkPreview(
            @LoginMember Long memberId, @PathVariable Long resourceId,
            @PathVariable Long storedFileId) {
        return response(resourceService.openPreviewImage(memberId, resourceId, storedFileId), false);
    }

    private ResponseEntity<Resource> response(FileDownloadResponse file, boolean attachment) {
        ContentDisposition.Builder disposition = attachment
                ? ContentDisposition.attachment()
                : ContentDisposition.inline();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .contentLength(file.sizeBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition
                        .filename(file.originalName(), StandardCharsets.UTF_8).build().toString())
                .body(file.resource());
    }
}
