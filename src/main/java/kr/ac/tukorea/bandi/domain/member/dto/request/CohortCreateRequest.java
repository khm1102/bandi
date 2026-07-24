package kr.ac.tukorea.bandi.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CohortCreateRequest(
        @NotBlank(message = "기수 이름을 입력해 주세요.")
        @Size(max = 30, message = "기수 이름은 30자 이하여야 합니다.")
        String name
) {

    public String normalizedName() {
        return name.trim();
    }
}
