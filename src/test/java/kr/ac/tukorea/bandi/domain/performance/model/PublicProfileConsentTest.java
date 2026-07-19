package kr.ac.tukorea.bandi.domain.performance.model;

import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPublicProfileConsentException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PublicProfileConsentTest {

    private static final LocalDateTime AGREED_AT =
            LocalDateTime.of(2026, 7, 18, 21, 0);

    @Test
    void 항목별_동의를_기록하고_철회한다() {
        PublicProfileConsent consent = PublicProfileConsent.agree(
                1L, 2L, ConsentScope.PHOTO, 3L, AGREED_AT);

        PublicProfileConsent revoked = consent.revoke(
                3L, AGREED_AT.plusHours(1));

        assertThat(revoked.isAgreed()).isFalse();
        assertThat(revoked.getRevokedDttm())
                .isEqualTo(AGREED_AT.plusHours(1));
    }

    @Test
    void 이미_철회한_동의는_다시_철회할_수_없다() {
        PublicProfileConsent revoked = PublicProfileConsent.agree(
                1L, 2L, ConsentScope.NAME, 3L, AGREED_AT)
                .revoke(3L, AGREED_AT.plusMinutes(1));

        assertThatThrownBy(() -> revoked.revoke(
                3L, AGREED_AT.plusMinutes(2)))
                .isInstanceOf(InvalidPublicProfileConsentException.class);
    }
}
