package kr.ac.tukorea.bandi.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;

public record StatusChangeRequest(
        @NotNull(message = "변경할 상태를 선택해 주세요.")
        MemberStatus newStatus,
        @NotBlank(message = "변경 사유를 입력해 주세요.")
        @Size(max = 500, message = "변경 사유는 500자 이하여야 합니다.")
        String reason
) {

    public StatusChangeParam toParam(Long memberId) {
        return new StatusChangeParam(memberId, newStatus, reason);
    }
}
