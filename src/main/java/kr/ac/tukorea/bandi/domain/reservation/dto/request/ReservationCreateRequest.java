package kr.ac.tukorea.bandi.domain.reservation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ReservationCreateRequest(
        @NotNull @Positive Long performanceRoundId,
        @NotEmpty List<@NotNull @Positive Long> performanceRoundSeatIds,
        @NotBlank @Size(max = 100) String applicantName,
        @NotBlank @Pattern(regexp = "[0-9\\- ]{10,20}") String phone,
        @NotNull @Positive Long privacyPolicyVersionId
) {

    public ReservationCreateParam toParam() {
        return new ReservationCreateParam(performanceRoundId,
                performanceRoundSeatIds, applicantName, phone,
                privacyPolicyVersionId);
    }
}
