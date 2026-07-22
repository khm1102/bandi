package kr.ac.tukorea.bandi.domain.member.sso.tukorea;

import java.net.URI;
import java.util.Map;

interface SchoolSsoHttpSession {

    SchoolSsoHttpResponse get(URI uri);

    SchoolSsoHttpResponse postForm(URI uri, URI referer, Map<String, String> form);
}
