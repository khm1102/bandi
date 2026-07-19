package kr.ac.tukorea.bandi.domain.file.service;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StorageKeyGeneratorTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-07-18T03:00:00Z"), ZoneId.of("Asia/Seoul"));

    private final StorageKeyGenerator generator = new StorageKeyGenerator(CLOCK);

    @Test
    void 도메인과_날짜와_UUID만으로_저장_키를_만든다() {
        String key = generator.generate("activity");

        assertThat(key).matches("activity/2026/07/[0-9a-f-]{36}");
    }

    @Test
    void 도메인에_경로나_개인정보를_끼워_넣을_수_없다() {
        assertThatThrownBy(() -> generator.generate("activity/member-2021184000"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
