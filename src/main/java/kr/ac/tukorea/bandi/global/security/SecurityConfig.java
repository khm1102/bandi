package kr.ac.tukorea.bandi.global.security;

import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final SchoolAuthenticationProvider authenticationProvider;
    private final SchoolLoginFailureHandler loginFailureHandler;
    private final ApiSecurityFailureHandler apiSecurityFailureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {
        PathPatternRequestMatcher apiRequestMatcher =
                PathPatternRequestMatcher.withDefaults().matcher("/api/**");
        RequestCache requestCache = requestCache();
        http.authenticationProvider(authenticationProvider);
        http.authorizeHttpRequests(authorize -> authorize
                .dispatcherTypeMatchers(DispatcherType.FORWARD,
                        DispatcherType.ERROR).permitAll()
                .requestMatchers(PathRequest.toStaticResources()
                        .atCommonLocations()).permitAll()
                .requestMatchers("/", "/login", "/error", "/performances/**", "/reserve/**", "/docs/**",
                        "/api-docs/**", "/swagger-ui/**").permitAll()
                .requestMatchers("/share/**").permitAll()
                .requestMatchers("/api/public-notices/**").permitAll()
                .requestMatchers("/api/public-performances/**",
                        "/api/public-policies/**",
                        "/api/public-reservations/**").permitAll()
                .requestMatchers(HttpMethod.POST,
                        "/api/files/*/public-promotions")
                .hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/members/me",
                        "/api/members/*/profile-photo",
                        "/api/members/reference/teams")
                .authenticated()
                .requestMatchers("/api/members/me/**")
                .authenticated()
                .requestMatchers("/profile")
                .authenticated()
                .requestMatchers("/team-members", "/api/members/team-members")
                .hasAnyRole("LEADER", "ADMIN")
                .requestMatchers("/notices/write", "/notices/*/edit",
                        "/notices/manage", "/notices/manage/**")
                .hasAnyRole("LEADER", "ADMIN")
                .requestMatchers("/api/internal-notice-management/**")
                .hasAnyRole("LEADER", "ADMIN")
                .requestMatchers("/members/**", "/api/members/**")
                .hasRole("ADMIN")
                .anyRequest().authenticated());
        http.exceptionHandling(exception -> exception
                .defaultAuthenticationEntryPointFor(apiSecurityFailureHandler,
                        apiRequestMatcher)
                .defaultAuthenticationEntryPointFor(
                        new LoginUrlAuthenticationEntryPoint("/login"),
                        new NegatedRequestMatcher(apiRequestMatcher))
                .defaultAccessDeniedHandlerFor(apiSecurityFailureHandler,
                        apiRequestMatcher));
        http.requestCache(cache -> cache.requestCache(requestCache));
        http.formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("studentNo")
                .passwordParameter("password")
                .successHandler(new SafeSavedRequestAuthenticationSuccessHandler(requestCache))
                .failureHandler(loginFailureHandler)
                .permitAll());
        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("SESSION", "JSESSIONID")
                .permitAll());
        return http.build();
    }

    private RequestCache requestCache() {
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setRequestMatcher(request -> {
            if (!HttpMethod.GET.matches(request.getMethod())) {
                return false;
            }
            String path = request.getRequestURI().substring(request.getContextPath().length());
            return !path.equals("/login") && !path.equals("/logout");
        });
        return requestCache;
    }
}
