package kr.ac.tukorea.bandi.global.security;

import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class MemberAuthenticationRefreshFilterTest {

    private static final Long MEMBER_ID = 100L;

    @Mock
    private MemberService memberService;

    private MemberAuthenticationRefreshFilter filter;

    @BeforeEach
    void setUp() {
        filter = new MemberAuthenticationRefreshFilter(Optional.of(memberService));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void DB에서_변경된_권한으로_현재_세션의_인증_정보를_교체한다() throws Exception {
        LoginPrincipal stalePrincipal = new LoginPrincipal(MEMBER_ID, "ADMIN");
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(stalePrincipal, null,
                        stalePrincipal.authorities()));
        given(memberService.lookupCurrentRole(MEMBER_ID)).willReturn(ClubRole.MEMBER);

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(),
                new MockFilterChain());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication.getPrincipal())
                .isEqualTo(new LoginPrincipal(MEMBER_ID, "MEMBER"));
        assertThat(authentication.getAuthorities())
                .extracting(authority -> authority.getAuthority())
                .containsExactly("ROLE_MEMBER");
    }

    @Test
    void 로그인_세션이_아니면_DB_권한을_조회하지_않는다() throws Exception {
        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(),
                new MockFilterChain());

        verifyNoInteractions(memberService);
    }
}
