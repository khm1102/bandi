package kr.ac.tukorea.bandi.domain.member.sso.tukorea;

import java.net.URI;

record SchoolSsoHttpResponse(int statusCode, URI finalUri, String body) {
}
