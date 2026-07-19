package kr.ac.tukorea.bandi.domain.notice.dto.request;

import java.time.LocalDateTime;

public record PublicNoticeSearchCondition(
        String keyword,
        LocalDateTime currentDttm,
        int offset,
        int limit
) {

    public static PublicNoticeSearchCondition from(PublicNoticeSearchParam param,
                                                   LocalDateTime currentDttm) {
        String normalizedKeyword = param.keyword() == null || param.keyword().isBlank()
                ? null
                : param.keyword().strip();
        return new PublicNoticeSearchCondition(normalizedKeyword, currentDttm,
                param.page() * param.pageSize(), param.pageSize());
    }
}
