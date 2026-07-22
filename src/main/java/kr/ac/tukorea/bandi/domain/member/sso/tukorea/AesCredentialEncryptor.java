package kr.ac.tukorea.bandi.domain.member.sso.tukorea;

import kr.ac.tukorea.bandi.domain.member.exception.SchoolSsoResponseChangedException;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Component
class AesCredentialEncryptor {

    private static final int IV_LENGTH = 16;

    private final InitializationVectorGenerator ivGenerator;

    AesCredentialEncryptor() {
        SecureRandom secureRandom = new SecureRandom();
        this.ivGenerator = () -> {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            return iv;
        };
    }

    AesCredentialEncryptor(InitializationVectorGenerator ivGenerator) {
        this.ivGenerator = ivGenerator;
    }

    String encrypt(String value, long timestampMillis, String keyHex) {
        try {
            byte[] key = HexFormat.of().parseHex(keyHex);
            byte[] iv = ivGenerator.generate();
            validateLengths(key, iv);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
            byte[] plaintext = (value + "|" + timestampMillis).getBytes(StandardCharsets.UTF_8);
            byte[] ciphertext = cipher.doFinal(plaintext);

            byte[] combined = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(combined);
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new SchoolSsoResponseChangedException();
        }
    }

    private void validateLengths(byte[] key, byte[] iv) {
        if (key.length != 16 || iv.length != IV_LENGTH) {
            throw new SchoolSsoResponseChangedException();
        }
    }
}
