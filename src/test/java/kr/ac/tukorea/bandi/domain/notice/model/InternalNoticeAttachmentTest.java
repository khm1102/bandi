package kr.ac.tukorea.bandi.domain.notice.model;

import kr.ac.tukorea.bandi.domain.notice.exception.InvalidInternalNoticeException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InternalNoticeAttachmentTest {

    @Test
    void 공지와_파일을_표시_순서대로_연결한다() {
        InternalNoticeAttachment attachment = InternalNoticeAttachment.create(10L, 20L, 0);

        assertThat(attachment.getInternalNoticeId()).isEqualTo(10L);
        assertThat(attachment.getStoredFileId()).isEqualTo(20L);
        assertThat(attachment.getDisplayOrder()).isZero();
    }

    @Test
    void 공지와_파일_식별자는_필수이고_표시순서는_음수가_아니다() {
        assertThatThrownBy(() -> InternalNoticeAttachment.create(null, 20L, 0))
                .isInstanceOf(InvalidInternalNoticeException.class);
        assertThatThrownBy(() -> InternalNoticeAttachment.create(10L, null, 0))
                .isInstanceOf(InvalidInternalNoticeException.class);
        assertThatThrownBy(() -> InternalNoticeAttachment.create(10L, 20L, -1))
                .isInstanceOf(InvalidInternalNoticeException.class);
    }
}
