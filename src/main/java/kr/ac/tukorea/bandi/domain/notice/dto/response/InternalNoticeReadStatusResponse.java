package kr.ac.tukorea.bandi.domain.notice.dto.response;

import java.time.LocalDateTime;

public record InternalNoticeReadStatusResponse(
        Long memberId,
        String studentNo,
        String memberName,
        Long teamId,
        String teamName,
        LocalDateTime firstReadDttm,
        LocalDateTime lastReadDttm
) {

    public boolean read() {
        return firstReadDttm != null;
    }
}
