package kr.ac.tukorea.bandi.domain.reservation.service;

import kr.ac.tukorea.bandi.domain.reservation.exception.InvalidReservationException;
import kr.ac.tukorea.bandi.domain.reservation.exception.ReservationSecurityException;
import kr.ac.tukorea.bandi.global.config.ReservationSecurityProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.HexFormat;

@Component
@RequiredArgsConstructor
public class ReservationDataProtector {

    private static final String CIPHER_TRANSFORMATION =
            "AES/GCM/NoPadding";
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final int NONCE_BYTES = 12;
    private static final int GCM_TAG_BITS = 128;

    private final ReservationSecurityProperties properties;
    private final SecureRandom secureRandom;

    public ProtectedApplicant protect(String applicantName, String phone) {
        String normalizedName = normalizeName(applicantName);
        String normalizedPhone = normalizePhone(phone);
        short keyVersion = properties.activeKeyVersion();
        return new ProtectedApplicant(
                encrypt(normalizedName, keyVersion),
                encrypt(normalizedPhone, keyVersion),
                hmac(normalizedPhone), keyVersion);
    }

    public String decryptName(ProtectedApplicant applicant) {
        return decrypt(applicant.applicantNameCiphertext(),
                applicant.encryptionKeyVersion());
    }

    public String decryptPhone(ProtectedApplicant applicant) {
        return decrypt(applicant.phoneCiphertext(),
                applicant.encryptionKeyVersion());
    }

    public String decryptName(byte[] ciphertext, short keyVersion) {
        return decrypt(ciphertext, keyVersion);
    }

    public String decryptPhone(byte[] ciphertext, short keyVersion) {
        return decrypt(ciphertext, keyVersion);
    }

    public String phoneSearchHash(String phone) {
        return hmac(normalizePhone(phone));
    }

    private byte[] encrypt(String plaintext, short keyVersion) {
        try {
            byte[] nonce = new byte[NONCE_BYTES];
            secureRandom.nextBytes(nonce);
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(properties.encryptionKey(keyVersion),
                            "AES"),
                    new GCMParameterSpec(GCM_TAG_BITS, nonce));
            byte[] encrypted = cipher.doFinal(
                    plaintext.getBytes(StandardCharsets.UTF_8));
            return ByteBuffer.allocate(nonce.length + encrypted.length)
                    .put(nonce).put(encrypted).array();
        } catch (GeneralSecurityException exception) {
            throw new ReservationSecurityException(exception);
        }
    }

    private String decrypt(byte[] value, short keyVersion) {
        if (value == null || value.length <= NONCE_BYTES) {
            throw new InvalidReservationException("ciphertext");
        }
        try {
            ByteBuffer buffer = ByteBuffer.wrap(value);
            byte[] nonce = new byte[NONCE_BYTES];
            buffer.get(nonce);
            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE,
                    new SecretKeySpec(properties.encryptionKey(keyVersion),
                            "AES"),
                    new GCMParameterSpec(GCM_TAG_BITS, nonce));
            return new String(cipher.doFinal(encrypted),
                    StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new ReservationSecurityException(exception);
        }
    }

    private String hmac(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(properties.hmacKey(),
                    HMAC_ALGORITHM));
            return HexFormat.of().formatHex(mac.doFinal(
                    value.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new ReservationSecurityException(exception);
        }
    }

    private String normalizeName(String value) {
        if (value == null || value.isBlank()
                || value.trim().length() > 100) {
            throw new InvalidReservationException("applicantName");
        }
        return value.trim();
    }

    private String normalizePhone(String value) {
        if (value == null) {
            throw new InvalidReservationException("phone");
        }
        String normalized = value.replaceAll("[^0-9]", "");
        if (normalized.length() < 10 || normalized.length() > 11) {
            throw new InvalidReservationException("phone");
        }
        return normalized;
    }
}
