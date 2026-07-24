package kr.ac.tukorea.bandi.domain.member.service;

import kr.ac.tukorea.bandi.domain.member.exception.SchoolLoginRateLimitedException;
import kr.ac.tukorea.bandi.domain.member.mapper.SchoolLoginAttemptMapper;
import kr.ac.tukorea.bandi.domain.member.model.SchoolLoginAttempt;
import kr.ac.tukorea.bandi.global.config.SchoolLoginAttemptProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HexFormat;

/**
 * 학교 포털 계정 보호를 위해 자격증명 실패만 제한한다.
 * 성공·실패 상태에는 학번 원문이나 비밀번호를 저장하지 않는다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SchoolLoginAttemptService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final SchoolLoginAttemptMapper schoolLoginAttemptMapper;
    private final SchoolLoginAttemptProperties properties;
    private final Clock clock;

    @Transactional
    public void assertAllowed(String studentNo) {
        String studentNoHash = hashStudentNo(studentNo);
        SchoolLoginAttempt attempt = schoolLoginAttemptMapper
                .lookupByStudentNoHashForUpdate(studentNoHash)
                .orElse(null);
        if (attempt == null) {
            return;
        }

        LocalDateTime currentDttm = now();
        if (attempt.isBlockedAt(currentDttm)) {
            throw new SchoolLoginRateLimitedException();
        }
        if (attempt.isExpiredAt(currentDttm, properties)) {
            schoolLoginAttemptMapper.removeByStudentNoHash(studentNoHash);
        }
    }

    @Transactional
    public boolean recordFailure(String studentNo) {
        String studentNoHash = hashStudentNo(studentNo);
        LocalDateTime currentDttm = now();
        schoolLoginAttemptMapper.insertIfAbsent(studentNoHash, currentDttm);
        SchoolLoginAttempt attempt = schoolLoginAttemptMapper
                .lookupByStudentNoHashForUpdate(studentNoHash)
                .orElseThrow(IllegalStateException::new);
        SchoolLoginAttempt updatedAttempt = attempt.recordFailure(currentDttm, properties);
        schoolLoginAttemptMapper.update(updatedAttempt);
        return updatedAttempt.isBlockedAt(currentDttm);
    }

    @Transactional
    public void clearFailures(String studentNo) {
        schoolLoginAttemptMapper.removeByStudentNoHash(hashStudentNo(studentNo));
    }

    private String hashStudentNo(String studentNo) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec key = new SecretKeySpec(
                    properties.hashSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(key);
            return HexFormat.of().formatHex(mac.doFinal(
                    studentNo.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("school login attempt hash unavailable", exception);
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
