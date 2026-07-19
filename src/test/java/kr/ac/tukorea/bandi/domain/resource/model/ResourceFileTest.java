package kr.ac.tukorea.bandi.domain.resource.model;

import kr.ac.tukorea.bandi.domain.resource.exception.InvalidResourceException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourceFileTest {

    @Test
    void 자료_파일을_revision과_표시_순서로_연결한다() {
        ResourceFile file = ResourceFile.create(1L, 2L, 3, 0, 4L);

        assertThat(file.getRevisionNo()).isEqualTo(3);
        assertThat(file.getDisplayOrder()).isZero();
        assertThat(file.getUploadedByMemberId()).isEqualTo(4L);
    }

    @Test
    void 식별자와_revision과_업로더를_검증한다() {
        assertThatThrownBy(() -> ResourceFile.create(null, 2L, 1, 0, 4L))
                .isInstanceOf(InvalidResourceException.class);
        assertThatThrownBy(() -> ResourceFile.create(1L, 2L, 0, 0, 4L))
                .isInstanceOf(InvalidResourceException.class);
        assertThatThrownBy(() -> ResourceFile.create(1L, 2L, 1, -1, 4L))
                .isInstanceOf(InvalidResourceException.class);
        assertThatThrownBy(() -> ResourceFile.create(1L, 2L, 1, 0, null))
                .isInstanceOf(InvalidResourceException.class);
    }
}
