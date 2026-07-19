package kr.ac.tukorea.bandi.domain.resource.controller;

import kr.ac.tukorea.bandi.domain.resource.dto.request.ResourceSearchParam;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceDetailResponse;
import kr.ac.tukorea.bandi.domain.resource.dto.response.ResourceSummaryResponse;
import kr.ac.tukorea.bandi.domain.resource.service.ResourceService;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import kr.ac.tukorea.bandi.global.swagger.ResourceApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ResourceApiController implements ResourceApiDocs {

    private final ResourceService resourceService;

    @Override
    public ResponseEntity<List<ResourceSummaryResponse>> search(
            @LoginMember Long memberId, String keyword, String categoryCode,
            int page, int pageSize) {
        return ResponseEntity.ok(resourceService.searchReadable(memberId,
                new ResourceSearchParam(keyword, categoryCode, page, pageSize)));
    }

    @Override
    public ResponseEntity<ResourceDetailResponse> lookup(
            @LoginMember Long memberId, Long resourceId) {
        return ResponseEntity.ok(resourceService.lookupReadable(memberId, resourceId));
    }

    @Override
    public ResponseEntity<Void> download(@LoginMember Long memberId,
                                         Long resourceId, Long storedFileId) {
        String url = resourceService.createDownloadUrl(memberId, resourceId,
                storedFileId);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(url))
                .build();
    }
}
