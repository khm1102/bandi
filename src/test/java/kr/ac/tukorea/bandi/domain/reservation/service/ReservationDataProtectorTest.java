package kr.ac.tukorea.bandi.domain.reservation.service;

import kr.ac.tukorea.bandi.domain.reservation.exception.InvalidReservationException;
import kr.ac.tukorea.bandi.global.config.ReservationSecurityProperties;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationDataProtectorTest {

    private final ReservationDataProtector protector =
            new ReservationDataProtector(properties(), new SecureRandom());

    @Test
    void 이름과_연락처를_AES_GCM으로_암호화하고_복호화한다() {
        ProtectedApplicant protectedApplicant = protector.protect(
                "홍길동", "010-1234-5678");

        assertThat(protectedApplicant.applicantNameCiphertext())
                .isNotEqualTo("홍길동".getBytes(StandardCharsets.UTF_8));
        assertThat(protectedApplicant.phoneCiphertext())
                .isNotEqualTo("01012345678".getBytes(StandardCharsets.UTF_8));
        assertThat(protector.decryptName(protectedApplicant)).isEqualTo("홍길동");
        assertThat(protector.decryptPhone(protectedApplicant))
                .isEqualTo("01012345678");
        assertThat(protectedApplicant.encryptionKeyVersion())
                .isEqualTo((short) 1);
    }

    @Test
    void 같은_평문도_매번_다른_암호문이_된다() {
        ProtectedApplicant first = protector.protect(
                "홍길동", "01012345678");
        ProtectedApplicant second = protector.protect(
                "홍길동", "01012345678");

        assertThat(first.applicantNameCiphertext())
                .isNotEqualTo(second.applicantNameCiphertext());
        assertThat(first.phoneCiphertext())
                .isNotEqualTo(second.phoneCiphertext());
    }

    @Test
    void 연락처_표기법이_달라도_같은_HMAC을_생성한다() {
        String formatted = protector.phoneSearchHash("010-1234-5678");
        String digits = protector.phoneSearchHash("01012345678");

        assertThat(formatted).isEqualTo(digits).hasSize(64);
    }

    @Test
    void 잘못된_연락처는_거부한다() {
        assertThatThrownBy(() -> protector.protect("홍길동", "1234"))
                .isInstanceOf(InvalidReservationException.class);
    }

    @Test
    void 보호_결과의_배열은_외부에서_변경할_수_없다() {
        ProtectedApplicant result = protector.protect(
                "홍길동", "01012345678");
        byte[] exposed = result.applicantNameCiphertext();

        exposed[0] = 0;

        assertThat(protector.decryptName(result)).isEqualTo("홍길동");
    }

    private ReservationSecurityProperties properties() {
        return new ReservationSecurityProperties((short) 1,
                Map.of((short) 1, encode(
                        "01234567890123456789012345678901")),
                encode("abcdefghijklmnopqrstuvwxyz123456"),
                Duration.ofDays(90));
    }

    private String encode(String value) {
        return Base64.getEncoder().encodeToString(
                value.getBytes(StandardCharsets.UTF_8));
    }
}
