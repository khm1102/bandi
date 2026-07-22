package kr.ac.tukorea.bandi.domain.member.dto.request;

import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.SsoLinkStatus;

/**
 * 멤버 목록 검색 조건. 값이 null인 항목은 조건에서 제외한다.
 * 화면 폼에 바인딩되면 컨벤션 7.2에 따라 setter 클래스로 바꾼다.
 */
public record MemberSearchCondition(
        String keyword,
        Long teamId,
        MemberStatus status,
        ClubRole role,
        SsoLinkStatus ssoLinkStatus
) {

    public MemberSearchCondition {
        keyword = keyword == null || keyword.isBlank()
                ? null : keyword.trim();
    }

    public MemberSearchCondition(Long teamId, MemberStatus status,
                                 ClubRole role) {
        this(null, teamId, status, role, null);
    }
}
