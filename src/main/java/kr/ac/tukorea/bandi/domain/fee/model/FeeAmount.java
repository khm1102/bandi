package kr.ac.tukorea.bandi.domain.fee.model;

import kr.ac.tukorea.bandi.domain.fee.exception.InvalidFeeException;

public record FeeAmount(long value) {

    public FeeAmount {
        if (value < 1) {
            throw new InvalidFeeException("amount");
        }
    }
}
