package kr.ac.tukorea.bandi.domain.notice.model;

import kr.ac.tukorea.bandi.domain.notice.exception.InvalidPublicNoticeException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PublicNoticeAttachmentTest {

    @Test
    void 공시와_READY_파일을_표시_순서대로_연결한다() {
        PublicNoticeAttachment attachment = PublicNoticeAttachment.create(10L, 20L, 0);

        assertThat(attachment.getPublicNoticeId()).isEqualTo(10L);
        assertThat(attachment.getStoredFileId()).isEqualTo(20L);
        assertThat(attachment.getDisplayOrder()).isZero();
    }

    @Test
    void 공시와_파일_식별자는_필수다() {
        assertThatThrownBy(() -> PublicNoticeAttachment.create(null, 20L, 0))
                .isInstanceOf(InvalidPublicNoticeException.class);
        assertThatThrownBy(() -> PublicNoticeAttachment.create(10L, null, 0))
                .isInstanceOf(InvalidPublicNoticeException.class);
    }

    @Test
    void 표시_순서는_음수일_수_없다() {
        assertThatThrownBy(() -> PublicNoticeAttachment.create(10L, 20L, -1))
                .isInstanceOf(InvalidPublicNoticeException.class);
    }
}
