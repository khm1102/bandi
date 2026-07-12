package kr.ac.tukorea.bandi.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * TODO 로그인 기능 도입 시 인가 규칙 작성 (컨벤션 18장 — 세션 기반, formLogin + /login).
     * 그 전까지는 임시로 전체 개방한다. CSRF는 기본값(활성)을 유지한다 (MUST 10 — 비활성화 금지).
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
