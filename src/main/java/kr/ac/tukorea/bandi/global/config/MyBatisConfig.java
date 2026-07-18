package kr.ac.tukorea.bandi.global.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * Mapper 인터페이스 스캔 (컨벤션 10.1).
 * 개별 @Mapper 애노테이션은 생략하고 이 한 곳에서 feature별 mapper 패키지를 잡는다.
 */
@Configuration
@MapperScan("kr.ac.tukorea.bandi.domain.**.mapper")
public class MyBatisConfig {
}
