package kr.ac.tukorea.bandi.domain.reservation.model;

import kr.ac.tukorea.bandi.domain.reservation.exception.InvalidReservationException;
import kr.ac.tukorea.bandi.domain.reservation.exception.InvalidReservationStateException;

import java.time.LocalDateTime;
import java.util.Arrays;

public class Reservation {

    private Long reservationId;
    private final Long performanceRoundId;
    private final String reservationNo;
    private final String lookupTokenHash;
    private final String entryTokenHash;
    private final byte[] applicantNameCiphertext;
    private final byte[] phoneCiphertext;
    private final String phoneSearchHash;
    private final short encryptionKeyVersion;
    private final ReservationStatus status;
    private final Long privacyPolicyVersionId;
    private final LocalDateTime agreedDttm;
    private final LocalDateTime cancelledDttm;
    private final String cancelReason;
    private final LocalDateTime personalDataErasedDttm;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;

    public Reservation(
            Long reservationId, Long performanceRoundId,
            String reservationNo, String lookupTokenHash,
            String entryTokenHash, byte[] applicantNameCiphertext,
            byte[] phoneCiphertext, String phoneSearchHash,
            Short encryptionKeyVersion, ReservationStatus status,
            Long privacyPolicyVersionId, LocalDateTime agreedDttm,
            LocalDateTime cancelledDttm, String cancelReason,
            LocalDateTime personalDataErasedDttm,
            LocalDateTime createdDttm, LocalDateTime updatedDttm) {
        this.reservationId = reservationId;
        this.performanceRoundId = requireId(
                performanceRoundId, "performanceRoundId");
        this.reservationNo = requireText(
                reservationNo, "reservationNo", 30);
        this.lookupTokenHash = optionalHash(lookupTokenHash,
                "lookupTokenHash", personalDataErasedDttm);
        this.entryTokenHash = optionalHash(entryTokenHash,
                "entryTokenHash", personalDataErasedDttm);
        this.applicantNameCiphertext = copyCiphertext(
                applicantNameCiphertext, "applicantNameCiphertext",
                personalDataErasedDttm);
        this.phoneCiphertext = copyCiphertext(phoneCiphertext,
                "phoneCiphertext", personalDataErasedDttm);
        this.phoneSearchHash = optionalHash(phoneSearchHash,
                "phoneSearchHash", personalDataErasedDttm);
        this.encryptionKeyVersion = requireKeyVersion(
                encryptionKeyVersion);
        this.status = requireStatus(status);
        this.privacyPolicyVersionId = requireId(
                privacyPolicyVersionId, "privacyPolicyVersionId");
        this.agreedDttm = requireTime(agreedDttm, "agreedDttm");
        this.cancelledDttm = cancelledDttm;
        this.cancelReason = cancelReason;
        this.personalDataErasedDttm = personalDataErasedDttm;
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
        validateCancellation();
        validatePersonalData();
    }

    public static Reservation confirm(
            Long performanceRoundId, String reservationNo,
            String lookupTokenHash, String entryTokenHash,
            byte[] applicantNameCiphertext, byte[] phoneCiphertext,
            String phoneSearchHash, short encryptionKeyVersion,
            Long privacyPolicyVersionId, LocalDateTime agreedDttm) {
        return new Reservation(null, performanceRoundId, reservationNo,
                lookupTokenHash, entryTokenHash, applicantNameCiphertext,
                phoneCiphertext, phoneSearchHash, encryptionKeyVersion,
                ReservationStatus.CONFIRMED, privacyPolicyVersionId,
                agreedDttm, null, null, null, null, null);
    }

    public Reservation cancel(String reason, LocalDateTime cancelledDttm) {
        if (status == ReservationStatus.CANCELLED) {
            throw new InvalidReservationStateException("cancelled");
        }
        return copy(ReservationStatus.CANCELLED,
                requireTime(cancelledDttm, "cancelledDttm"),
                requireText(reason, "cancelReason", 500),
                lookupTokenHash, entryTokenHash,
                applicantNameCiphertext, phoneCiphertext,
                phoneSearchHash, personalDataErasedDttm);
    }

    public Reservation erasePersonalData(LocalDateTime erasedDttm) {
        if (personalDataErasedDttm != null) {
            throw new InvalidReservationStateException("alreadyErased");
        }
        return copy(status, cancelledDttm, cancelReason,
                null, null, null, null, null,
                requireTime(erasedDttm, "erasedDttm"));
    }

    public Long getReservationId() {
        return reservationId;
    }

    public Long getPerformanceRoundId() {
        return performanceRoundId;
    }

    public String getReservationNo() {
        return reservationNo;
    }

    public String getLookupTokenHash() {
        return lookupTokenHash;
    }

    public String getEntryTokenHash() {
        return entryTokenHash;
    }

    public byte[] getApplicantNameCiphertext() {
        return copy(applicantNameCiphertext);
    }

    public byte[] getPhoneCiphertext() {
        return copy(phoneCiphertext);
    }

    public String getPhoneSearchHash() {
        return phoneSearchHash;
    }

    public short getEncryptionKeyVersion() {
        return encryptionKeyVersion;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public Long getPrivacyPolicyVersionId() {
        return privacyPolicyVersionId;
    }

    public LocalDateTime getAgreedDttm() {
        return agreedDttm;
    }

    public LocalDateTime getCancelledDttm() {
        return cancelledDttm;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public LocalDateTime getPersonalDataErasedDttm() {
        return personalDataErasedDttm;
    }

    public LocalDateTime getCreatedDttm() {
        return createdDttm;
    }

    public LocalDateTime getUpdatedDttm() {
        return updatedDttm;
    }

    private Reservation copy(
            ReservationStatus status, LocalDateTime cancelledDttm,
            String cancelReason, String lookupTokenHash,
            String entryTokenHash, byte[] applicantNameCiphertext,
            byte[] phoneCiphertext, String phoneSearchHash,
            LocalDateTime personalDataErasedDttm) {
        return new Reservation(reservationId, performanceRoundId,
                reservationNo, lookupTokenHash, entryTokenHash,
                applicantNameCiphertext, phoneCiphertext, phoneSearchHash,
                encryptionKeyVersion, status, privacyPolicyVersionId,
                agreedDttm, cancelledDttm, cancelReason,
                personalDataErasedDttm, createdDttm, updatedDttm);
    }

    private void validateCancellation() {
        if (status == ReservationStatus.CANCELLED
                && (cancelledDttm == null || cancelReason == null
                || cancelReason.isBlank()
                || cancelReason.trim().length() > 500)) {
            throw new InvalidReservationException("cancellation");
        }
        if (status != ReservationStatus.CANCELLED
                && (cancelledDttm != null || cancelReason != null)) {
            throw new InvalidReservationException("cancellation");
        }
    }

    private void validatePersonalData() {
        if (personalDataErasedDttm == null) {
            return;
        }
        if (lookupTokenHash != null || entryTokenHash != null
                || applicantNameCiphertext != null
                || phoneCiphertext != null || phoneSearchHash != null) {
            throw new InvalidReservationException("personalDataErasure");
        }
    }

    private static String optionalHash(
            String value, String field,
            LocalDateTime personalDataErasedDttm) {
        if (personalDataErasedDttm != null && value == null) {
            return null;
        }
        if (value == null || !value.matches("[0-9a-f]{64}")) {
            throw new InvalidReservationException(field);
        }
        return value;
    }

    private static byte[] copyCiphertext(
            byte[] value, String field,
            LocalDateTime personalDataErasedDttm) {
        if (personalDataErasedDttm != null && value == null) {
            return null;
        }
        if (value == null || value.length == 0) {
            throw new InvalidReservationException(field);
        }
        return copy(value);
    }

    private static byte[] copy(byte[] value) {
        return value == null ? null : Arrays.copyOf(value, value.length);
    }

    private static Long requireId(Long value, String field) {
        if (value == null || value < 1) {
            throw new InvalidReservationException(field);
        }
        return value;
    }

    private static short requireKeyVersion(Short value) {
        if (value == null || value < 1) {
            throw new InvalidReservationException("encryptionKeyVersion");
        }
        return value;
    }

    private static ReservationStatus requireStatus(
            ReservationStatus value) {
        if (value == null) {
            throw new InvalidReservationException("status");
        }
        return value;
    }

    private static String requireText(
            String value, String field, int maxLength) {
        if (value == null || value.isBlank()
                || value.trim().length() > maxLength) {
            throw new InvalidReservationException(field);
        }
        return value.trim();
    }

    private static LocalDateTime requireTime(
            LocalDateTime value, String field) {
        if (value == null) {
            throw new InvalidReservationException(field);
        }
        return value;
    }
}
