package kr.ac.tukorea.bandi.domain.member.client.tukorea;

import java.net.URI;

record SchoolSsoHttpResponse(int statusCode, URI finalUri, String body) {
}
