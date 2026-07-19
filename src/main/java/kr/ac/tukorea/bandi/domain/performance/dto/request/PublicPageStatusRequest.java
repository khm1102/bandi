package kr.ac.tukorea.bandi.domain.performance.dto.request;

import jakarta.validation.constraints.NotNull;
import kr.ac.tukorea.bandi.domain.performance.model.PublicPageStatus;

public record PublicPageStatusRequest(@NotNull PublicPageStatus status) {
}
