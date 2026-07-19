package kr.ac.tukorea.bandi.domain.member.dto.request;

import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.SsoLinkStatus;

public record MemberSearchFilter(
        MemberStatus status,
        ClubRole role,
        SsoLinkStatus ssoLinkStatus
) {
}
