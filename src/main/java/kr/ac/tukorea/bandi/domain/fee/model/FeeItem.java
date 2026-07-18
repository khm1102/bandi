package kr.ac.tukorea.bandi.domain.fee.model;

import kr.ac.tukorea.bandi.domain.fee.exception.InvalidFeeException;
import kr.ac.tukorea.bandi.domain.fee.exception.InvalidFeeStateException;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class FeeItem {

    private static final int MAX_NAME_LENGTH = 150;
    private static final int MAX_TERM_CODE_LENGTH = 20;

    private Long feeItemId;
    private final String name;
    private final String description;
    private final short referenceYear;
    private final String referenceTermCode;
    private final FeeAmount feeAmount;
    private final LocalDate dueDate;
    private final FeeItemStatus status;
    private final Long createdByMemberId;
    private final Long updatedByMemberId;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;
    private final LocalDateTime deletedDttm;

    public FeeItem(Long feeItemId, String name, String description,
                   short referenceYear, String referenceTermCode, long amount,
                   LocalDate dueDate, FeeItemStatus status,
                   Long createdByMemberId, Long updatedByMemberId,
                   LocalDateTime createdDttm, LocalDateTime updatedDttm,
                   LocalDateTime deletedDttm) {
        validate(name, referenceYear, referenceTermCode, dueDate,
                status, createdByMemberId, updatedByMemberId);
        this.feeItemId = feeItemId;
        this.name = name.strip();
        this.description = normalize(description);
        this.referenceYear = referenceYear;
        this.referenceTermCode = normalize(referenceTermCode);
        this.feeAmount = new FeeAmount(amount);
        this.dueDate = dueDate;
        this.status = status;
        this.createdByMemberId = createdByMemberId;
        this.updatedByMemberId = updatedByMemberId;
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
        this.deletedDttm = deletedDttm;
    }

    public static FeeItem draft(String name, String description,
                                short referenceYear, String referenceTermCode,
                                long amount, LocalDate dueDate,
                                Long actorMemberId) {
        return new FeeItem(null, name, description, referenceYear,
                referenceTermCode, amount, dueDate, FeeItemStatus.DRAFT,
                actorMemberId, actorMemberId, null, null, null);
    }

    public FeeItem edit(String newName, String newDescription,
                        short newReferenceYear, String newReferenceTermCode,
                        long newAmount, LocalDate newDueDate,
                        Long actorMemberId) {
        validateStatus(FeeItemStatus.DRAFT, "edit");
        return copy(newName, newDescription, newReferenceYear,
                newReferenceTermCode, newAmount, newDueDate,
                status, actorMemberId);
    }

    public FeeItem open(Long actorMemberId) {
        validateStatus(FeeItemStatus.DRAFT, "open");
        return copy(name, description, referenceYear, referenceTermCode,
                getAmount(), dueDate, FeeItemStatus.OPEN, actorMemberId);
    }

    public FeeItem close(Long actorMemberId) {
        validateStatus(FeeItemStatus.OPEN, "close");
        return copy(name, description, referenceYear, referenceTermCode,
                getAmount(), dueDate, FeeItemStatus.CLOSED, actorMemberId);
    }

    public FeeItem cancel(Long actorMemberId) {
        if (status == FeeItemStatus.CANCELLED) {
            throw new InvalidFeeStateException("cancel");
        }
        return copy(name, description, referenceYear, referenceTermCode,
                getAmount(), dueDate, FeeItemStatus.CANCELLED, actorMemberId);
    }

    public void validateChargeProcessing() {
        if (status != FeeItemStatus.OPEN && status != FeeItemStatus.CLOSED) {
            throw new InvalidFeeStateException("charge-processing");
        }
    }

    private FeeItem copy(String newName, String newDescription,
                         short newReferenceYear, String newReferenceTermCode,
                         long newAmount, LocalDate newDueDate,
                         FeeItemStatus newStatus, Long actorMemberId) {
        return new FeeItem(feeItemId, newName, newDescription,
                newReferenceYear, newReferenceTermCode, newAmount, newDueDate,
                newStatus, createdByMemberId, actorMemberId,
                createdDttm, updatedDttm, deletedDttm);
    }

    private void validate(String targetName, short targetYear,
                          String targetTermCode, LocalDate targetDueDate,
                          FeeItemStatus targetStatus,
                          Long creatorId, Long updaterId) {
        if (targetName == null || targetName.isBlank()
                || targetName.length() > MAX_NAME_LENGTH || targetYear < 1
                || targetDueDate == null
                || targetStatus == null || creatorId == null
                || updaterId == null) {
            throw new InvalidFeeException("required");
        }
        String normalizedTermCode = normalize(targetTermCode);
        if (normalizedTermCode != null
                && normalizedTermCode.length() > MAX_TERM_CODE_LENGTH) {
            throw new InvalidFeeException("term-code");
        }
    }

    private void validateStatus(FeeItemStatus expectedStatus, String operation) {
        if (status != expectedStatus) {
            throw new InvalidFeeStateException(operation);
        }
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    public long getAmount() {
        return feeAmount.value();
    }
}
