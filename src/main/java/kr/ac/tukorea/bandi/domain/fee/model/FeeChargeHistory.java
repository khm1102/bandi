package kr.ac.tukorea.bandi.domain.fee.model;

import kr.ac.tukorea.bandi.domain.fee.exception.InvalidFeeException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class FeeChargeHistory {

    private static final int MAX_REASON_LENGTH = 500;

    private Long feeChargeHistoryId;
    private final Long feeChargeId;
    private final FeeChargeStatus previousStatus;
    private final FeeChargeStatus newStatus;
    private final FeeAmount feeAmount;
    private final String reason;
    private final Long changedByMemberId;
    private final LocalDateTime changedDttm;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;

    public FeeChargeHistory(Long feeChargeHistoryId, Long feeChargeId,
                            FeeChargeStatus previousStatus,
                            FeeChargeStatus newStatus, long amount,
                            String reason, Long changedByMemberId,
                            LocalDateTime changedDttm,
                            LocalDateTime createdDttm,
                            LocalDateTime updatedDttm) {
        String normalizedReason = normalize(reason);
        if (feeChargeId == null || previousStatus == null || newStatus == null
                || previousStatus == newStatus
                || changedByMemberId == null || changedDttm == null
                || (normalizedReason != null
                && normalizedReason.length() > MAX_REASON_LENGTH)) {
            throw new InvalidFeeException("charge-history");
        }
        this.feeChargeHistoryId = feeChargeHistoryId;
        this.feeChargeId = feeChargeId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.feeAmount = new FeeAmount(amount);
        this.reason = normalizedReason;
        this.changedByMemberId = changedByMemberId;
        this.changedDttm = changedDttm;
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
    }

    public static FeeChargeHistory change(Long feeChargeId,
                                          FeeChargeStatus previousStatus,
                                          FeeChargeStatus newStatus,
                                          long amount, String reason,
                                          Long actorMemberId,
                                          LocalDateTime currentDttm) {
        return new FeeChargeHistory(null, feeChargeId, previousStatus,
                newStatus, amount, reason, actorMemberId, currentDttm,
                null, null);
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    public long getAmount() {
        return feeAmount.value();
    }
}
