package kr.ac.tukorea.bandi.domain.member.sso.tukorea;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AesCredentialEncryptorTest {

    @Test
    void Python_PoC와_같은_AES_CBC_URL_SAFE_Base64_형식으로_암호화한다() {
        // given
        byte[] fixedIv = hex("101112131415161718191a1b1c1d1e1f");
        AesCredentialEncryptor encryptor = new AesCredentialEncryptor(() -> fixedIv);

        // when
        String encrypted = encryptor.encrypt(
                "2021184000", 1721289600000L, "000102030405060708090a0b0c0d0e0f");

        // then — Python Crypto.Cipher AES 결과와 독립 비교한 고정 벡터
        assertThat(encrypted).isEqualTo(
                "EBESExQVFhcYGRobHB0eH7qZk4Q-2dY2fsV58d16N7PVMYUPnDKbeRHwQohjpqRr");
        assertThat(encrypted).doesNotContain("+", "/", "=");
    }

    private byte[] hex(String value) {
        return java.util.HexFormat.of().parseHex(value);
    }
}
