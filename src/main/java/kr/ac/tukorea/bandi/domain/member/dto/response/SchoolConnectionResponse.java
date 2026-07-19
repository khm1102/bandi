package kr.ac.tukorea.bandi.domain.member.dto.response;

import kr.ac.tukorea.bandi.domain.member.model.AcademicStatus;
import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.Member;
import kr.ac.tukorea.bandi.domain.member.model.MemberSchoolConnection;
import kr.ac.tukorea.bandi.domain.member.model.SchoolConnectionOutcome;

public record SchoolConnectionResponse(
        Long memberId,
        Long teamId,
        ClubRole role,
        AcademicStatus academicStatus,
        SchoolConnectionOutcome outcome
) {

    public static SchoolConnectionResponse from(Member member, MemberSchoolConnection connection) {
        return new SchoolConnectionResponse(member.getMemberId(), member.getTeamId(), member.getRole(),
                connection.academicStatus(), connection.outcome());
    }
}
