package kr.ac.tukorea.bandi.domain.share.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShareTokenGeneratorTest {

    @Test
    void 생성한_공유_토큰은_256비트_Base64URL_형식이다() {
        ShareTokenGenerator generator = new ShareTokenGenerator();

        String token = generator.generate();

        assertThat(token).matches("[A-Za-z0-9_-]{43}");
    }
}
