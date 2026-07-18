package kr.ac.tukorea.bandi.domain.member.client.tukorea.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SchoolSsoProperties.class)
public class SchoolSsoConfig {
}
