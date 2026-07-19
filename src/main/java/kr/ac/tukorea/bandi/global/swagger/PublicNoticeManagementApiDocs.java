package kr.ac.tukorea.bandi.global.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.ac.tukorea.bandi.domain.notice.dto.request.NoticePublishRequest;
import kr.ac.tukorea.bandi.domain.notice.dto.request.PublicNoticeManageFilter;
import kr.ac.tukorea.bandi.domain.notice.dto.request.PublicNoticeWriteRequest;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeAdminDetailResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeAdminSummaryResponse;
import kr.ac.tukorea.bandi.domain.notice.dto.response.PublicNoticeCreatedResponse;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("/api/admin/public-notices")
@Tag(name = ApiTag.PUBLIC_NOTICE, description = "공시 게시 관리 API")
@SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SCHEME)
public interface PublicNoticeManagementApiDocs {

    @Operation(summary = "관리용 공시 목록 조회")
    @GetMapping
    ResponseEntity<List<PublicNoticeAdminSummaryResponse>> search(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @RequestParam(required = false) String keyword,
            @ParameterObject @ModelAttribute PublicNoticeManageFilter filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize);

    @Operation(summary = "관리용 공시 상세 조회")
    @GetMapping("/{publicNoticeId}")
    ResponseEntity<PublicNoticeAdminDetailResponse> lookup(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long publicNoticeId);

    @Operation(summary = "공시 초안 등록")
    @PostMapping
    ResponseEntity<PublicNoticeCreatedResponse> create(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @Valid @RequestBody PublicNoticeWriteRequest request);

    @Operation(summary = "공시 수정")
    @PutMapping("/{publicNoticeId}")
    ResponseEntity<Void> update(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long publicNoticeId,
            @Valid @RequestBody PublicNoticeWriteRequest request);

    @Operation(summary = "공시 게시 또는 예약 게시")
    @PostMapping("/{publicNoticeId}/publish")
    ResponseEntity<Void> publish(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long publicNoticeId,
            @RequestBody NoticePublishRequest request);

    @Operation(summary = "공시 게시 종료")
    @PostMapping("/{publicNoticeId}/close")
    ResponseEntity<Void> close(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long publicNoticeId);

    @Operation(summary = "공시 보관")
    @PostMapping("/{publicNoticeId}/archive")
    ResponseEntity<Void> archive(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long publicNoticeId);
}
