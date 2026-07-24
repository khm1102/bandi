package kr.ac.tukorea.bandi.global.response;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageResponseTest {

    @Test
    void 빈_결과는_0개의_전체_페이지를_가진다() {
        PageResponse<String> page = PageResponse.of(List.of(), 0, 20, 0);

        assertThat(page.items()).isEmpty();
        assertThat(page.page()).isZero();
        assertThat(page.pageSize()).isEqualTo(20);
        assertThat(page.totalElements()).isZero();
        assertThat(page.totalPages()).isZero();
        assertThat(page.hasPrevious()).isFalse();
        assertThat(page.hasNext()).isFalse();
    }

    @Test
    void 빈_결과의_범위_밖_페이지에도_이전_페이지가_있다고_표시하지_않는다() {
        PageResponse<String> page = PageResponse.of(List.of(), 4, 20, 0);

        assertThat(page.totalPages()).isZero();
        assertThat(page.hasPrevious()).isFalse();
        assertThat(page.hasNext()).isFalse();
    }

    @Test
    void 첫_페이지는_다음_페이지가_있다() {
        PageResponse<Integer> page = PageResponse.of(List.of(1, 2), 0, 20, 41);

        assertThat(page.totalPages()).isEqualTo(3);
        assertThat(page.hasPrevious()).isFalse();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    void 중간_페이지는_이전과_다음_페이지가_있다() {
        PageResponse<Integer> page = PageResponse.of(List.of(21), 1, 20, 41);

        assertThat(page.hasPrevious()).isTrue();
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    void 마지막_페이지는_이전_페이지만_있다() {
        PageResponse<Integer> page = PageResponse.of(List.of(41), 2, 20, 41);

        assertThat(page.totalPages()).isEqualTo(3);
        assertThat(page.hasPrevious()).isTrue();
        assertThat(page.hasNext()).isFalse();
    }

    @Test
    void 잘못된_페이지_정보는_거부한다() {
        assertThatThrownBy(() -> PageResponse.of(List.of(), -1, 20, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PageResponse.of(List.of(), 0, 0, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PageResponse.of(List.of(), 0, 101, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PageResponse.of(List.of(), 0, 20, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
