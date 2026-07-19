package kr.ac.tukorea.bandi.domain.member.client;

import kr.ac.tukorea.bandi.domain.member.model.SchoolIdentity;

public interface SchoolSsoClient {

    SchoolIdentity authenticate(SchoolCredentials credentials);
}
