package kr.ac.tukorea.bandi.global.security;

public interface SchoolLoginAuthenticator {

    LoginPrincipal authenticate(String studentNo, String password);
}
