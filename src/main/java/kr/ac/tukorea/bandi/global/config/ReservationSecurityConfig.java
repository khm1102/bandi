package kr.ac.tukorea.bandi.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;

@Configuration
@EnableConfigurationProperties(ReservationSecurityProperties.class)
public class ReservationSecurityConfig {

    @Bean
    public SecureRandom reservationSecureRandom() {
        return new SecureRandom();
    }
}
