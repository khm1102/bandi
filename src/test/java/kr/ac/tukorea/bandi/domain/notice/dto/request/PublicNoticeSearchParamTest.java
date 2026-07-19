package kr.ac.tukorea.bandi.domain.notice.dto.request;

import kr.ac.tukorea.bandi.domain.notice.exception.InvalidPublicNoticeException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PublicNoticeSearchParamTest {

    @Test
    void 페이지는_0_이상이고_크기는_1부터_100까지다() {
        assertThatThrownBy(() -> new PublicNoticeSearchParam(null, -1, 20))
                .isInstanceOf(InvalidPublicNoticeException.class);
        assertThatThrownBy(() -> new PublicNoticeSearchParam(null, 0, 0))
                .isInstanceOf(InvalidPublicNoticeException.class);
        assertThatThrownBy(() -> new PublicNoticeSearchParam(null, 0, 101))
                .isInstanceOf(InvalidPublicNoticeException.class);
    }

    @Test
    void 검색어는_200자를_넘을_수_없다() {
        assertThatThrownBy(() -> new PublicNoticeSearchParam("가".repeat(201), 0, 20))
                .isInstanceOf(InvalidPublicNoticeException.class);
    }

    @Test
    void 페이지_오프셋이_INT_범위를_넘을_수_없다() {
        assertThatThrownBy(() -> new PublicNoticeSearchParam(
                null, Integer.MAX_VALUE, 100))
                .isInstanceOf(InvalidPublicNoticeException.class);
    }
}
