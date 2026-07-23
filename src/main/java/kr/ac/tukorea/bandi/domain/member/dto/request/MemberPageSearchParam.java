package kr.ac.tukorea.bandi.domain.member.dto.request;

import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.SsoLinkStatus;

public record MemberPageSearchParam(
        String keyword,
        Long teamId,
        Long cohortId,
        MemberStatus status,
        ClubRole role,
        SsoLinkStatus ssoLinkStatus,
        int page,
        int pageSize
) {

    private static final int MAX_KEYWORD_LENGTH = 200;
    private static final int MAX_PAGE_SIZE = 100;

    public MemberPageSearchParam {
        if (page < 0 || pageSize < 1 || pageSize > MAX_PAGE_SIZE
                || page > Integer.MAX_VALUE / pageSize) {
            throw new IllegalArgumentException("잘못된 페이지 정보입니다.");
        }
        if (keyword != null && keyword.length() > MAX_KEYWORD_LENGTH) {
            throw new IllegalArgumentException("검색어가 너무 깁니다.");
        }
    }
}
