package kr.ac.tukorea.bandi.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({FileStorageProperties.class, PublicNoticeRetirementProperties.class})
public class FileStorageConfig {
}
