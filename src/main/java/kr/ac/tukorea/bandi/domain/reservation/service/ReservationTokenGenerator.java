package kr.ac.tukorea.bandi.domain.reservation.service;

import kr.ac.tukorea.bandi.domain.reservation.exception.InvalidReservationException;
import kr.ac.tukorea.bandi.domain.reservation.exception.ReservationSecurityException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HexFormat;

@Component
@RequiredArgsConstructor
public class ReservationTokenGenerator {

    private static final int TOKEN_BYTES = 32;
    private static final int RESERVATION_NO_RANDOM_BYTES = 10;
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.BASIC_ISO_DATE;

    private final SecureRandom secureRandom;
    private final Clock clock;

    public ReservationCredentials generate() {
        String lookupToken = randomToken(TOKEN_BYTES);
        String entryToken = randomToken(TOKEN_BYTES);
        return new ReservationCredentials(
                reservationNo(), lookupToken, hash(lookupToken),
                entryToken, hash(entryToken));
    }

    public String hash(String token) {
        if (token == null || token.isBlank()) {
            throw new InvalidReservationException("token");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new ReservationSecurityException(exception);
        }
    }

    private String reservationNo() {
        return "R" + LocalDate.now(clock).format(DATE_FORMAT)
                + randomToken(RESERVATION_NO_RANDOM_BYTES);
    }

    private String randomToken(int size) {
        byte[] value = new byte[size];
        secureRandom.nextBytes(value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }
}
