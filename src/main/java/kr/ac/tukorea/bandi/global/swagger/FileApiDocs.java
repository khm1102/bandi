package kr.ac.tukorea.bandi.global.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import kr.ac.tukorea.bandi.domain.file.dto.request.PublicFilePromotionRequest;
import kr.ac.tukorea.bandi.domain.file.dto.response.FileIdentifierResponse;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@RequestMapping("/api/files")
@Tag(name = ApiTag.FILE, description = "로컬 파일 업로드·공개 승격 API")
@SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE_SCHEME)
public interface FileApiDocs {

    @Operation(summary = "비공개 파일 업로드",
            description = "파일을 검증해 비공개 저장소에 저장하고 storedFileId를 반환합니다.")
    @PostMapping(value = "/private",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<FileIdentifierResponse> uploadPrivate(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @RequestParam
            @Pattern(regexp = "[a-z][a-z0-9-]{1,29}") String domain,
            @RequestPart("file") MultipartFile file);

    @Operation(summary = "비공개 파일을 공개용 파일로 복사",
            description = "원본 비공개 파일은 유지하고 별도의 공개 파일과 storedFileId를 만듭니다.")
    @PostMapping("/{storedFileId}/public-promotions")
    ResponseEntity<FileIdentifierResponse> promoteToPublic(
            @Parameter(hidden = true) @LoginMember Long actorMemberId,
            @PathVariable Long storedFileId,
            @Valid @org.springframework.web.bind.annotation.RequestBody
            PublicFilePromotionRequest request);
}
