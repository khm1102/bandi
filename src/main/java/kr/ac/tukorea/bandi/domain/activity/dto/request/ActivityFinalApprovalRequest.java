package kr.ac.tukorea.bandi.domain.activity.dto.request;

import jakarta.validation.constraints.Size;

public record ActivityFinalApprovalRequest(
        @Size(max = 500, message = "긴급 승인 사유는 500자 이하여야 합니다.")
        String emergencyReason
) {
}
