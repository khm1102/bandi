package kr.ac.tukorea.bandi.domain.file.controller;

import kr.ac.tukorea.bandi.domain.file.dto.request.PublicFilePromotionRequest;
import kr.ac.tukorea.bandi.domain.file.dto.response.FileIdentifierResponse;
import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.domain.file.service.FileUploadParam;
import kr.ac.tukorea.bandi.global.security.LoginMember;
import kr.ac.tukorea.bandi.global.swagger.FileApiDocs;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class FileApiController implements FileApiDocs {

    private final FileService fileService;

    @Override
    public ResponseEntity<FileIdentifierResponse> uploadPrivate(
            @LoginMember Long actorMemberId, String domain,
            MultipartFile file) {
        Long id = fileService.uploadPrivate(new FileUploadParam(
                domain, file.getOriginalFilename(), file.getSize(),
                file::getInputStream, actorMemberId));
        return created(id);
    }

    @Override
    public ResponseEntity<FileIdentifierResponse> promoteToPublic(
            @LoginMember Long actorMemberId, Long storedFileId,
            PublicFilePromotionRequest request) {
        Long id = fileService.promoteToPublic(storedFileId,
                request.domain(), actorMemberId);
        return created(id);
    }

    private ResponseEntity<FileIdentifierResponse> created(Long id) {
        return ResponseEntity.created(URI.create("/api/files/" + id))
                .body(new FileIdentifierResponse(id));
    }
}
