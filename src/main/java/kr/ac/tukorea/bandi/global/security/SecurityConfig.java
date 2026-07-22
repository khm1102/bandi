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
                .dispatcherTypeMatchers(DispatcherType.FORWARD,
                        DispatcherType.ERROR).permitAll()
                .requestMatchers(PathRequest.toStaticResources()
                        .atCommonLocations()).permitAll()
                .requestMatchers("/login", "/error", "/performances/**", "/reserve/**", "/docs/**",
                        "/api-docs/**", "/swagger-ui/**").permitAll()
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
                .requestMatchers("/notices/write", "/notices/*/edit")
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
