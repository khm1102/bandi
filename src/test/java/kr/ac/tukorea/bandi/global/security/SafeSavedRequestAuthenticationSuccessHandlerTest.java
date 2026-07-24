package kr.ac.tukorea.bandi.global.security;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

class SafeSavedRequestAuthenticationSuccessHandlerTest {

    @Test
    void 저장된_내부_GET_주소가_있으면_해당_주소로_복귀한다() throws Exception {
        RequestCache requestCache = Mockito.mock(RequestCache.class);
        SavedRequest savedRequest = Mockito.mock(SavedRequest.class);
        MockHttpServletRequest request = request();
        MockHttpServletResponse response = new MockHttpServletResponse();
        given(requestCache.getRequest(request, response)).willReturn(savedRequest);
        given(savedRequest.getRedirectUrl()).willReturn(
                "http://localhost/notices/10?source=share");

        new SafeSavedRequestAuthenticationSuccessHandler(requestCache)
                .onAuthenticationSuccess(request, response, null);

        assertThat(response.getRedirectedUrl()).isEqualTo("/notices/10?source=share");
        verify(requestCache).removeRequest(request, response);
    }

    @Test
    void 외부_주소와_로그인_주소는_대시보드로_복귀한다() throws Exception {
        RequestCache requestCache = Mockito.mock(RequestCache.class);
        SavedRequest savedRequest = Mockito.mock(SavedRequest.class);
        MockHttpServletRequest request = request();
        MockHttpServletResponse response = new MockHttpServletResponse();
        given(requestCache.getRequest(request, response)).willReturn(savedRequest);
        given(savedRequest.getRedirectUrl()).willReturn("https://example.com/notices/10");

        new SafeSavedRequestAuthenticationSuccessHandler(requestCache)
                .onAuthenticationSuccess(request, response, null);

        assertThat(response.getRedirectedUrl()).isEqualTo("/dashboard");
    }

    @Test
    void 저장된_요청이_없으면_대시보드로_이동한다() throws Exception {
        RequestCache requestCache = Mockito.mock(RequestCache.class);
        MockHttpServletRequest request = request();
        MockHttpServletResponse response = new MockHttpServletResponse();

        new SafeSavedRequestAuthenticationSuccessHandler(requestCache)
                .onAuthenticationSuccess(request, response, null);

        assertThat(response.getRedirectedUrl()).isEqualTo("/dashboard");
    }

    private MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServerName("localhost");
        return request;
    }
}
