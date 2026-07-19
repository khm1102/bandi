package kr.ac.tukorea.bandi.global.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.Base64;
import java.util.Map;

@Validated
@ConfigurationProperties(prefix = "bandi.reservation-security")
public record ReservationSecurityProperties(
        @Positive short activeKeyVersion,
        @NotEmpty Map<Short, @NotBlank String> encryptionKeys,
        @NotBlank String hmacKeyBase64,
        @NotNull Duration personalDataRetention
) {

    private static final int MIN_HMAC_KEY_BYTES = 32;

    public ReservationSecurityProperties {
        encryptionKeys = encryptionKeys == null
                ? Map.of() : Map.copyOf(encryptionKeys);
        validateActiveKey(activeKeyVersion, encryptionKeys);
        validateHmacKey(hmacKeyBase64);
        if (personalDataRetention == null
                || personalDataRetention.isZero()
                || personalDataRetention.isNegative()) {
            throw new IllegalArgumentException(
                    "personal data retention must be positive");
        }
    }

    public byte[] encryptionKey(short version) {
        String encoded = encryptionKeys.get(version);
        if (encoded == null) {
            throw new IllegalArgumentException(
                    "unknown reservation encryption key version");
        }
        return decode(encoded, "reservation encryption key");
    }

    public byte[] hmacKey() {
        return decode(hmacKeyBase64, "reservation HMAC key");
    }

    private static void validateActiveKey(
            short activeKeyVersion, Map<Short, String> encryptionKeys) {
        String activeKey = encryptionKeys.get(activeKeyVersion);
        if (activeKey == null) {
            throw new IllegalArgumentException(
                    "active reservation encryption key is missing");
        }
        int length = decode(activeKey, "reservation encryption key").length;
        if (length != 16 && length != 24 && length != 32) {
            throw new IllegalArgumentException(
                    "reservation encryption key must be an AES key");
        }
    }

    private static void validateHmacKey(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "reservation HMAC key is missing");
        }
        if (decode(value, "reservation HMAC key").length
                < MIN_HMAC_KEY_BYTES) {
            throw new IllegalArgumentException(
                    "reservation HMAC key must be at least 32 bytes");
        }
    }

    private static byte[] decode(String value, String field) {
        try {
            return Base64.getDecoder().decode(value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    field + " must be Base64", exception);
        }
    }
}
