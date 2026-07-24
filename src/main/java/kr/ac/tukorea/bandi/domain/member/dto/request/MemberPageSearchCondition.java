package kr.ac.tukorea.bandi.domain.member.dto.request;

import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.SsoLinkStatus;

public record MemberPageSearchCondition(
        String keyword,
        Long teamId,
        Long cohortId,
        MemberStatus status,
        ClubRole role,
        SsoLinkStatus ssoLinkStatus,
        int offset,
        int limit
) {

    public static MemberPageSearchCondition from(MemberPageSearchParam param) {
        return create(param, param.teamId());
    }

    public static MemberPageSearchCondition forTeam(MemberPageSearchParam param, Long teamId) {
        return create(param, teamId);
    }

    private static MemberPageSearchCondition create(MemberPageSearchParam param, Long teamId) {
        return new MemberPageSearchCondition(normalize(param.keyword()), teamId,
                param.cohortId(), param.status(), param.role(), param.ssoLinkStatus(),
                param.page() * param.pageSize(), param.pageSize());
    }

    private static String normalize(String keyword) {
        return keyword == null || keyword.isBlank() ? null : keyword.strip();
    }
}
