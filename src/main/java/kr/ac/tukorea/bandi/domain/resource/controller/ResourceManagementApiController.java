package kr.ac.tukorea.bandi.domain.resource.controller;

import kr.ac.tukorea.bandi.domain.resource.dto.request.ResourceCreateRequest;
import kr.ac.tukorea.bandi.domain.resource.dto.request.ResourceManageFilter;
import kr.ac.tukorea.bandi.domain.resource.dto.request.ResourceManageSearchParam;
import kr.ac.tukorea.bandi.domain.resource.dto.request.ResourceRevisionRequest;
import kr.ac.tukorea.bandi.domain.resource.dto.request.ResourceUpdateRequest;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceCreatedResponse;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceManageDetailResponse;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceManageSummaryResponse;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceRevisionCreatedResponse;
import kr.ac.tukorea.bandi.domain.resource.service.ResourceService;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import kr.ac.tukorea.bandi.global.swagger.ResourceManagementApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ResourceManagementApiController implements ResourceManagementApiDocs {

    private final ResourceService resourceService;

    @Override
    public ResponseEntity<List<ResourceManageSummaryResponse>> search(
            @LoginMember Long actorMemberId, String keyword, String categoryCode,
            ResourceManageFilter filter, Long teamId, int page, int pageSize) {
        return ResponseEntity.ok(resourceService.searchManageable(actorMemberId,
                new ResourceManageSearchParam(keyword, categoryCode,
                        filter.status(), filter.targetScope(), teamId, page,
                        pageSize)));
    }

    @Override
    public ResponseEntity<ResourceManageDetailResponse> lookup(
            @LoginMember Long actorMemberId, Long resourceId) {
        return ResponseEntity.ok(resourceService.lookupManageable(actorMemberId,
                resourceId));
    }

    @Override
    public ResponseEntity<ResourceCreatedResponse> create(
            @LoginMember Long actorMemberId, ResourceCreateRequest request) {
        Long id = resourceService.createDraft(actorMemberId, request.toParam());
        return ResponseEntity.created(URI.create("/api/resource-management/" + id))
                .body(new ResourceCreatedResponse(id));
    }

    @Override
    public ResponseEntity<Void> update(@LoginMember Long actorMemberId,
                                       Long resourceId,
                                       ResourceUpdateRequest request) {
        resourceService.update(actorMemberId, request.toParam(resourceId));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<ResourceRevisionCreatedResponse> replaceFiles(
            @LoginMember Long actorMemberId, Long resourceId,
            ResourceRevisionRequest request) {
        int revisionNo = resourceService.replaceFiles(actorMemberId,
                request.toParam(resourceId));
        return ResponseEntity.created(URI.create("/api/resource-management/"
                        + resourceId + "/revisions/" + revisionNo))
                .body(new ResourceRevisionCreatedResponse(revisionNo));
    }

    @Override
    public ResponseEntity<Void> publish(@LoginMember Long actorMemberId,
                                        Long resourceId) {
        resourceService.publish(actorMemberId, resourceId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> archive(@LoginMember Long actorMemberId,
                                        Long resourceId) {
        resourceService.archive(actorMemberId, resourceId);
        return ResponseEntity.noContent().build();
    }
}
