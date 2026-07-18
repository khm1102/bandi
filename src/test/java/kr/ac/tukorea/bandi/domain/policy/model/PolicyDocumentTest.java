package kr.ac.tukorea.bandi.domain.policy.model;

import kr.ac.tukorea.bandi.domain.policy.exception.InvalidPolicyDocumentException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PolicyDocumentTest {

    @Test
    void 정책_문서를_생성하고_활성_상태를_변경한다() {
        PolicyDocument document = PolicyDocument.create(
                PolicyType.PRIVACY, "공개 프로필 동의", PolicyAudience.MEMBER);

        assertThat(document.isActive()).isTrue();
        assertThat(document.changeActive(false).isActive()).isFalse();
    }

    @Test
    void 정책_문서의_제목은_비어_있을_수_없다() {
        assertThatThrownBy(() -> PolicyDocument.create(
                PolicyType.PRIVACY, " ", PolicyAudience.MEMBER))
                .isInstanceOf(InvalidPolicyDocumentException.class);
    }
}
