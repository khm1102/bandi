package kr.ac.tukorea.bandi.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final SchoolAuthenticationProvider authenticationProvider;
    private final SchoolLoginFailureHandler loginFailureHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {
        http.authenticationProvider(authenticationProvider);
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(PathRequest.toStaticResources()
                        .atCommonLocations()).permitAll()
                .requestMatchers("/login", "/error", "/notices/**",
                        "/performances/**", "/reserve/**", "/docs/**",
                        "/api-docs/**", "/style-guide/**").permitAll()
                .requestMatchers("/members/**", "/reservations/**",
                        "/showops/**", "/api/members/**",
                        "/api/reservations/**", "/api/showops/**")
                .hasRole("ADMIN")
                .anyRequest().authenticated());
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
