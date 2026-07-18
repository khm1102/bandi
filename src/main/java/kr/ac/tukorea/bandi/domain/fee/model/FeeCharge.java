package kr.ac.tukorea.bandi.domain.fee.model;

import kr.ac.tukorea.bandi.domain.fee.exception.InvalidFeeException;
import kr.ac.tukorea.bandi.domain.fee.exception.InvalidFeeStateException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class FeeCharge {

    private static final int MAX_NOTE_LENGTH = 500;

    private Long feeChargeId;
    private final Long feeItemId;
    private final Long memberId;
    private final FeeAmount chargeAmount;
    private final FeeChargeStatus status;
    private final LocalDateTime paidDttm;
    private final Long processedByMemberId;
    private final String processNote;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;

    public FeeCharge(Long feeChargeId, Long feeItemId, Long memberId,
                     long chargedAmount, FeeChargeStatus status,
                     LocalDateTime paidDttm, Long processedByMemberId,
                     String processNote, LocalDateTime createdDttm,
                     LocalDateTime updatedDttm) {
        String normalizedNote = normalize(processNote);
        validate(feeItemId, memberId, status, paidDttm,
                processedByMemberId, normalizedNote);
        this.feeChargeId = feeChargeId;
        this.feeItemId = feeItemId;
        this.memberId = memberId;
        this.chargeAmount = new FeeAmount(chargedAmount);
        this.status = status;
        this.paidDttm = paidDttm;
        this.processedByMemberId = processedByMemberId;
        this.processNote = normalizedNote;
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
    }

    public static FeeCharge unpaid(Long feeItemId, Long memberId, long amount) {
        return new FeeCharge(null, feeItemId, memberId, amount,
                FeeChargeStatus.UNPAID, null, null, null, null, null);
    }

    public FeeCharge changeStatus(FeeChargeStatus newStatus,
                                  Long actorMemberId,
                                  LocalDateTime currentDttm,
                                  String note) {
        if (status == FeeChargeStatus.CANCELLED) {
            throw new InvalidFeeStateException("cancelled-charge");
        }
        if (newStatus == null || newStatus == status) {
            throw new InvalidFeeException("status-change");
        }
        if (actorMemberId == null || currentDttm == null) {
            throw new InvalidFeeException("actor-time");
        }
        LocalDateTime newPaidDttm = newStatus == FeeChargeStatus.PAID
                ? currentDttm : null;
        return new FeeCharge(feeChargeId, feeItemId, memberId, getChargedAmount(),
                newStatus, newPaidDttm, actorMemberId, note,
                createdDttm, updatedDttm);
    }

    public boolean isCancelled() {
        return status == FeeChargeStatus.CANCELLED;
    }

    private void validate(Long itemId, Long targetMemberId,
                          FeeChargeStatus targetStatus,
                          LocalDateTime targetPaidDttm, Long processorId,
                          String targetNote) {
        if (itemId == null || targetMemberId == null
                || targetStatus == null) {
            throw new InvalidFeeException("required");
        }
        boolean initialUnpaid = targetStatus == FeeChargeStatus.UNPAID
                && targetPaidDttm == null && processorId == null
                && targetNote == null;
        boolean processed = targetStatus != FeeChargeStatus.PAID
                && targetPaidDttm == null && processorId != null;
        boolean paid = targetStatus == FeeChargeStatus.PAID
                && targetPaidDttm != null && processorId != null;
        if (!initialUnpaid && !processed && !paid) {
            throw new InvalidFeeException("processing-state");
        }
        if (targetNote != null && targetNote.length() > MAX_NOTE_LENGTH) {
            throw new InvalidFeeException("note-length");
        }
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    public long getChargedAmount() {
        return chargeAmount.value();
    }
}
