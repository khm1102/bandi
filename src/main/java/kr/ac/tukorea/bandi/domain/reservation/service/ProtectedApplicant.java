package kr.ac.tukorea.bandi.domain.reservation.service;

import java.util.Arrays;

public record ProtectedApplicant(
        byte[] applicantNameCiphertext,
        byte[] phoneCiphertext,
        String phoneSearchHash,
        short encryptionKeyVersion
) {

    public ProtectedApplicant {
        applicantNameCiphertext = copy(applicantNameCiphertext);
        phoneCiphertext = copy(phoneCiphertext);
    }

    @Override
    public byte[] applicantNameCiphertext() {
        return copy(applicantNameCiphertext);
    }

    @Override
    public byte[] phoneCiphertext() {
        return copy(phoneCiphertext);
    }

    private static byte[] copy(byte[] value) {
        return value == null ? null : Arrays.copyOf(value, value.length);
    }
}
