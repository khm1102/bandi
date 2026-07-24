package kr.ac.tukorea.bandi.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class MemberAuthenticationRefreshFilter extends OncePerRequestFilter {

    private final Optional<MemberService> memberService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof LoginPrincipal principal) {
            memberService.ifPresent(service -> refreshAuthentication(
                    authentication,
                    principal,
                    service.lookupCurrentRole(principal.memberId())));
        }

        filterChain.doFilter(request, response);
    }

    private void refreshAuthentication(
            Authentication authentication,
            LoginPrincipal principal,
            ClubRole currentRole) {
        if (principal.role().equals(currentRole.name())) {
            return;
        }

        LoginPrincipal refreshedPrincipal = new LoginPrincipal(principal.memberId(), currentRole.name());
        UsernamePasswordAuthenticationToken refreshedAuthentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        refreshedPrincipal,
                        null,
                        refreshedPrincipal.authorities());
        refreshedAuthentication.setDetails(authentication.getDetails());
        SecurityContextHolder.getContext().setAuthentication(refreshedAuthentication);
    }
}
