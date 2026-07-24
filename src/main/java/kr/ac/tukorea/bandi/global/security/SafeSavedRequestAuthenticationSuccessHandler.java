package kr.ac.tukorea.bandi.global.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import java.io.IOException;
import java.net.URI;

public class SafeSavedRequestAuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    private static final String DEFAULT_TARGET_URL = "/dashboard";

    private final RequestCache requestCache;

    public SafeSavedRequestAuthenticationSuccessHandler(RequestCache requestCache) {
        this.requestCache = requestCache;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {
        String targetUrl = lookupTargetUrl(request, response);
        requestCache.removeRequest(request, response);
        response.sendRedirect(response.encodeRedirectURL(targetUrl));
    }

    private String lookupTargetUrl(HttpServletRequest request,
                                   HttpServletResponse response) {
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest == null) {
            return DEFAULT_TARGET_URL;
        }
        try {
            URI redirectUri = URI.create(savedRequest.getRedirectUrl());
            if (!isSameOrigin(request, redirectUri)) {
                return DEFAULT_TARGET_URL;
            }
            String path = redirectUri.getRawPath();
            if (!isAllowedPath(path)) {
                return DEFAULT_TARGET_URL;
            }
            String query = redirectUri.getRawQuery();
            return query == null ? path : path + "?" + query;
        } catch (IllegalArgumentException exception) {
            return DEFAULT_TARGET_URL;
        }
    }

    private boolean isAllowedPath(String path) {
        if (path == null || !path.startsWith("/")) {
            return false;
        }
        return !path.equals("/login") && !path.equals("/logout");
    }

    private boolean isSameOrigin(HttpServletRequest request, URI redirectUri) {
        if (!redirectUri.isAbsolute()) {
            return true;
        }
        String host = redirectUri.getHost();
        return host != null && host.equalsIgnoreCase(request.getServerName());
    }
}
