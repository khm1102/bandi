package kr.ac.tukorea.bandi.domain.member.sso;

import kr.ac.tukorea.bandi.domain.member.model.SchoolIdentity;

public interface SchoolSsoClient {

    SchoolIdentity authenticate(SchoolCredentials credentials);
}
