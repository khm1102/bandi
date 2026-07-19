package kr.ac.tukorea.bandi.domain.event.dto.request;

import kr.ac.tukorea.bandi.domain.event.model.ClubEventStatus;

public record ClubEventSearchFilter(ClubEventStatus status) {
}
