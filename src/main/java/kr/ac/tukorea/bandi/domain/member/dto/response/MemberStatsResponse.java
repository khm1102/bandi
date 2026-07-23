package kr.ac.tukorea.bandi.domain.member.dto.response;

public record MemberStatsResponse(
        long activeMemberCount,
        long activeCohortCount,
        long ssoVerificationRequiredCount
) {
}
