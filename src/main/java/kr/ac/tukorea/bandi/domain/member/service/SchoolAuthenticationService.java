package kr.ac.tukorea.bandi.domain.member.service;

import kr.ac.tukorea.bandi.domain.member.sso.SchoolCredentials;
import kr.ac.tukorea.bandi.domain.member.sso.SchoolSsoClient;
import kr.ac.tukorea.bandi.domain.member.dto.response.AuthenticatedMemberResponse;
import kr.ac.tukorea.bandi.domain.member.dto.response.SchoolConnectionResponse;
import kr.ac.tukorea.bandi.domain.member.exception.MemberLoginDeniedException;
import kr.ac.tukorea.bandi.domain.member.exception.SchoolAcademicStatusDeniedException;
import kr.ac.tukorea.bandi.domain.member.exception.SchoolCredentialsInvalidException;
import kr.ac.tukorea.bandi.domain.member.exception.SchoolIdentityReviewRequiredException;
import kr.ac.tukorea.bandi.domain.member.exception.SchoolLoginRateLimitedException;
import kr.ac.tukorea.bandi.domain.member.model.SchoolIdentity;
import kr.ac.tukorea.bandi.global.security.LoginPrincipal;
import kr.ac.tukorea.bandi.global.security.SchoolLoginAuthenticator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SchoolAuthenticationService implements SchoolLoginAuthenticator {

    private final SchoolSsoClient schoolSsoClient;
    private final MemberService memberService;
    private final SchoolLoginAttemptService schoolLoginAttemptService;

    public AuthenticatedMemberResponse authenticate(SchoolCredentials credentials) {
        schoolLoginAttemptService.assertAllowed(credentials.studentNo());
        SchoolIdentity identity = authenticateSchool(credentials);
        schoolLoginAttemptService.clearFailures(credentials.studentNo());
        identity.validateStudentNo(credentials.studentNo());
        SchoolConnectionResponse connection = memberService.connectSchoolIdentity(identity);
        return switch (connection.outcome()) {
            case AUTHENTICATED -> AuthenticatedMemberResponse.from(connection);
            case ACADEMIC_STATUS_DENIED -> throw new SchoolAcademicStatusDeniedException();
            case IDENTITY_REVIEW_REQUIRED -> throw new SchoolIdentityReviewRequiredException();
            case MEMBER_STATUS_DENIED -> throw new MemberLoginDeniedException();
        };
    }

    private SchoolIdentity authenticateSchool(SchoolCredentials credentials) {
        try {
            return schoolSsoClient.authenticate(credentials);
        } catch (SchoolCredentialsInvalidException exception) {
            if (schoolLoginAttemptService.recordFailure(credentials.studentNo())) {
                throw new SchoolLoginRateLimitedException();
            }
            throw exception;
        }
    }

    @Override
    public LoginPrincipal authenticate(String studentNo, String password) {
        AuthenticatedMemberResponse member = authenticate(
                new SchoolCredentials(studentNo, password));
        return new LoginPrincipal(member.memberId(), member.role().name());
    }
}
