package kr.ac.tukorea.bandi.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TeamChangeRequest(
        @NotNull(message = "변경할 팀을 선택해 주세요.")
        @Positive(message = "팀 식별자가 올바르지 않습니다.")
        Long newTeamId,
        @NotBlank(message = "변경 사유를 입력해 주세요.")
        @Size(max = 500, message = "변경 사유는 500자 이하여야 합니다.")
        String reason
) {

    public TeamChangeParam toParam(Long memberId) {
        return new TeamChangeParam(memberId, newTeamId, reason);
    }
}
