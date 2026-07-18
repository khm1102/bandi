package kr.ac.tukorea.bandi.domain.member.service;

import kr.ac.tukorea.bandi.domain.member.client.SchoolCredentials;
import kr.ac.tukorea.bandi.domain.member.client.SchoolSsoClient;
import kr.ac.tukorea.bandi.domain.member.dto.response.AuthenticatedMemberResponse;
import kr.ac.tukorea.bandi.domain.member.dto.response.SchoolConnectionResponse;
import kr.ac.tukorea.bandi.domain.member.exception.MemberLoginDeniedException;
import kr.ac.tukorea.bandi.domain.member.exception.SchoolAcademicStatusDeniedException;
import kr.ac.tukorea.bandi.domain.member.exception.SchoolIdentityReviewRequiredException;
import kr.ac.tukorea.bandi.domain.member.model.SchoolIdentity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SchoolAuthenticationService {

    private final SchoolSsoClient schoolSsoClient;
    private final MemberService memberService;

    public AuthenticatedMemberResponse authenticate(SchoolCredentials credentials) {
        SchoolIdentity identity = schoolSsoClient.authenticate(credentials);
        identity.validateStudentNo(credentials.studentNo());
        SchoolConnectionResponse connection = memberService.connectSchoolIdentity(identity);
        return switch (connection.outcome()) {
            case AUTHENTICATED -> AuthenticatedMemberResponse.from(connection);
            case ACADEMIC_STATUS_DENIED -> throw new SchoolAcademicStatusDeniedException();
            case IDENTITY_REVIEW_REQUIRED -> throw new SchoolIdentityReviewRequiredException();
            case MEMBER_STATUS_DENIED -> throw new MemberLoginDeniedException();
        };
    }
}
