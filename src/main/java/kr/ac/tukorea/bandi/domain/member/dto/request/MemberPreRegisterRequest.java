package kr.ac.tukorea.bandi.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record MemberPreRegisterRequest(
        @NotBlank(message = "학번을 입력해 주세요.")
        @Size(max = 20, message = "학번은 20자 이하여야 합니다.")
        String studentNo,
        @NotBlank(message = "이름을 입력해 주세요.")
        @Size(max = 50, message = "이름은 50자 이하여야 합니다.")
        String name,
        @NotNull(message = "팀을 선택해 주세요.")
        @Positive(message = "팀 식별자가 올바르지 않습니다.")
        Long teamId,
        @NotNull(message = "기수를 선택해 주세요.")
        @Positive(message = "기수 식별자가 올바르지 않습니다.")
        Long cohortId
) {

    public MemberPreRegisterParam toParam() {
        return new MemberPreRegisterParam(studentNo, name, teamId, cohortId);
    }
}
