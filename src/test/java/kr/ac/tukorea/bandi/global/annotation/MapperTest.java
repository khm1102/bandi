package kr.ac.tukorea.bandi.global.annotation;

import kr.ac.tukorea.bandi.global.config.MyBatisConfig;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mapper 테스트 공통 설정 (컨벤션 14).
 *
 * <ul>
 *   <li>{@code replace = NONE} — 클래스패스에 H2가 없으므로 임베디드 DB 교체를 막는다.</li>
 *   <li>{@code @ActiveProfiles("test")} — 없으면 기본 프로파일 dev의 bandi 스키마를 오염시킨다.</li>
 *   <li>{@code FlywayAutoConfiguration} — @MybatisTest 슬라이스는 Flyway를 구성하지 않는다.
 *       이것이 없으면 비어 있는 bandi_test 스키마에서 모든 쿼리가 실패한다.</li>
 *   <li>{@code MyBatisConfig} — 슬라이스는 @Configuration을 로드하지 않아 @MapperScan이 빠진다.</li>
 * </ul>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
@Import(MyBatisConfig.class)
public @interface MapperTest {
}
