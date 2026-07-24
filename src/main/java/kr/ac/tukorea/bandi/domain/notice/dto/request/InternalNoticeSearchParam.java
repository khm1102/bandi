package kr.ac.tukorea.bandi.domain.notice.dto.request;

import kr.ac.tukorea.bandi.domain.notice.exception.InvalidInternalNoticeException;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeReadFilter;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeTargetScope;

public record InternalNoticeSearchParam(
        String keyword,
        InternalNoticeReadFilter readFilter,
        InternalNoticeTargetScope targetScope,
        int page,
        int pageSize
) {

    private static final int MAX_KEYWORD_LENGTH = 200;
    private static final int MAX_PAGE_SIZE = 100;

    public InternalNoticeSearchParam(String keyword, int page, int pageSize) {
        this(keyword, InternalNoticeReadFilter.ALL, null, page, pageSize);
    }

    public InternalNoticeSearchParam(String keyword, String readFilterCode,
                                     String targetScopeCode, int page, int pageSize) {
        this(keyword, parseReadFilter(readFilterCode), parseTargetScope(targetScopeCode),
                page, pageSize);
    }

    public InternalNoticeSearchParam {
        if (page < 0 || pageSize < 1 || pageSize > MAX_PAGE_SIZE
                || page > Integer.MAX_VALUE / pageSize) {
            throw new InvalidInternalNoticeException("page");
        }
        if (keyword != null && keyword.length() > MAX_KEYWORD_LENGTH) {
            throw new InvalidInternalNoticeException("keyword");
        }
    }

    private static InternalNoticeReadFilter parseReadFilter(String code) {
        try {
            return code == null ? InternalNoticeReadFilter.ALL
                    : InternalNoticeReadFilter.valueOf(code);
        } catch (IllegalArgumentException exception) {
            throw new InvalidInternalNoticeException("readFilter");
        }
    }

    private static InternalNoticeTargetScope parseTargetScope(String code) {
        try {
            return code == null || code.isBlank() ? null
                    : InternalNoticeTargetScope.valueOf(code);
        } catch (IllegalArgumentException exception) {
            throw new InvalidInternalNoticeException("targetScope");
        }
    }
}
