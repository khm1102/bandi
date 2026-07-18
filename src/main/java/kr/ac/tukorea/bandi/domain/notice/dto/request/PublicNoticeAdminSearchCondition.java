package kr.ac.tukorea.bandi.domain.notice.dto.request;

import kr.ac.tukorea.bandi.domain.notice.model.PublicNoticeStatus;

public record PublicNoticeAdminSearchCondition(
        String keyword,
        PublicNoticeStatus status,
        int offset,
        int limit
) {

    public static PublicNoticeAdminSearchCondition from(PublicNoticeAdminSearchParam param) {
        String normalizedKeyword = param.keyword() == null || param.keyword().isBlank()
                ? null
                : param.keyword().strip();
        return new PublicNoticeAdminSearchCondition(normalizedKeyword, param.status(),
                param.page() * param.pageSize(), param.pageSize());
    }
}
