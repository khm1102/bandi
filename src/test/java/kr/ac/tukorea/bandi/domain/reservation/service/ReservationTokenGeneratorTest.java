package kr.ac.tukorea.bandi.domain.reservation.service;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationTokenGeneratorTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-11-01T01:00:00Z"),
            ZoneId.of("Asia/Seoul"));

    private final ReservationTokenGenerator generator =
            new ReservationTokenGenerator(new SecureRandom(), CLOCK);

    @Test
    void 신청번호와_조회_입장_토큰을_생성한다() {
        ReservationCredentials result = generator.generate();

        assertThat(result.reservationNo())
                .startsWith("R20261101").hasSizeLessThanOrEqualTo(30);
        assertThat(result.lookupToken()).hasSizeGreaterThanOrEqualTo(40);
        assertThat(result.entryToken()).hasSizeGreaterThanOrEqualTo(40);
        assertThat(result.lookupTokenHash()).hasSize(64);
        assertThat(result.entryTokenHash()).hasSize(64);
    }

    @Test
    void 원본_토큰의_해시가_저장용_해시와_일치한다() {
        ReservationCredentials result = generator.generate();

        assertThat(generator.hash(result.lookupToken()))
                .isEqualTo(result.lookupTokenHash());
        assertThat(generator.hash(result.entryToken()))
                .isEqualTo(result.entryTokenHash());
    }

    @Test
    void 각_신청의_토큰은_서로_다르다() {
        ReservationCredentials first = generator.generate();
        ReservationCredentials second = generator.generate();

        assertThat(first.reservationNo()).isNotEqualTo(second.reservationNo());
        assertThat(first.lookupToken()).isNotEqualTo(second.lookupToken());
        assertThat(first.entryToken()).isNotEqualTo(second.entryToken());
    }
}
