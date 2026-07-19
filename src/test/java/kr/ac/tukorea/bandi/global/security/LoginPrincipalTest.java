package kr.ac.tukorea.bandi.global.security;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginPrincipalTest {

    @Test
    void 세션_principal은_멤버_식별자와_역할만_직렬화한다() throws Exception {
        LoginPrincipal principal = new LoginPrincipal(10L, "ADMIN");

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream output = new ObjectOutputStream(bytes)) {
            output.writeObject(principal);
        }

        assertThat(principal.memberId()).isEqualTo(10L);
        assertThat(principal.role()).isEqualTo("ADMIN");
        assertThat(principal.authorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
        assertThat(bytes.toByteArray()).isNotEmpty();
    }

    @Test
    void 허용되지_않은_역할은_principal로_만들_수_없다() {
        assertThatThrownBy(() -> new LoginPrincipal(10L, "OWNER"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
