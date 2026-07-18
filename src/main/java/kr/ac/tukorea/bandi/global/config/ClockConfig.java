package kr.ac.tukorea.bandi.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * 현재 시각 제공자 (컨벤션 9.5).
 * Service가 LocalDateTime.now()를 직접 호출하지 않고 이 빈을 주입받아,
 * 테스트에서 Clock.fixed로 시각을 고정할 수 있게 한다.
 * 저장 시각 기준은 KST로 통일한다 (컨벤션 17.4).
 */
@Configuration
public class ClockConfig {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    @Bean
    public Clock clock() {
        return Clock.system(SEOUL);
    }
}
