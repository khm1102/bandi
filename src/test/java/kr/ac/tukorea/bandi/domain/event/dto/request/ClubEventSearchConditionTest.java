package kr.ac.tukorea.bandi.domain.event.dto.request;

import kr.ac.tukorea.bandi.domain.event.exception.InvalidClubEventException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClubEventSearchConditionTest {

    private static final LocalDateTime START =
            LocalDateTime.of(2026, 8, 1, 0, 0);

    @Test
    void 조회_범위와_페이지_크기를_검증한다() {
        assertThatThrownBy(() -> new ClubEventSearchCondition(
                null, null, null, -1, 20))
                .isInstanceOf(InvalidClubEventException.class);
        assertThatThrownBy(() -> new ClubEventSearchCondition(
                null, null, null, 0, 101))
                .isInstanceOf(InvalidClubEventException.class);
        assertThatThrownBy(() -> new ClubEventSearchCondition(
                null, START, START, 0, 20))
                .isInstanceOf(InvalidClubEventException.class);
    }
}
