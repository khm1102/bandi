package kr.ac.tukorea.bandi.global.security;

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
        http.authenticationProvider(authenticationProvider);
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(PathRequest.toStaticResources()
                        .atCommonLocations()).permitAll()
                .requestMatchers("/login", "/error", "/notices/**",
                        "/performances/**", "/reserve/**", "/docs/**",
                        "/api-docs/**", "/swagger-ui/**",
                        "/style-guide/**").permitAll()
                .requestMatchers(HttpMethod.GET,
                        "/api/public-notices/**",
                        "/api/public-performances/**",
                        "/api/public-policies/**").permitAll()
                .requestMatchers("/api/public-reservations/**").permitAll()
                .requestMatchers(HttpMethod.POST,
                        "/api/files/*/public-promotions")
                .hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/members/me",
                        "/api/members/reference/teams")
                .authenticated()
                .requestMatchers("/members/**", "/reservations/**",
                        "/notice-management/**",
                        "/showops/**", "/api/members/**",
                        "/api/reservations/**", "/api/showops/**",
                        "/api/reservation-management/**",
                        "/api/policies/**",
                        "/api/admin/public-notices/**")
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
        http.formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("studentNo")
                .passwordParameter("password")
                .defaultSuccessUrl("/dashboard", true)
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
}
