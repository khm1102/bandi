package kr.ac.tukorea.bandi.global.response;

import org.springframework.core.io.Resource;

/**
 * HTTP 파일 전송에 필요한 공통 응답 값이다.
 *
 * <p>도메인 서비스는 파일 접근 권한을 검증한 뒤 이 값만 반환하고,
 * 컨트롤러가 Content-Disposition 정책을 결정한다.</p>
 */
public record FileDownloadResponse(
        String originalName,
        String contentType,
        long sizeBytes,
        Resource resource
) {
}
