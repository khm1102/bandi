package kr.ac.tukorea.bandi.domain.fee.dto.response;

public record MemberFeeSummaryResponse(
        long totalAmount,
        long paidAmount,
        long unpaidAmount
) {
}
