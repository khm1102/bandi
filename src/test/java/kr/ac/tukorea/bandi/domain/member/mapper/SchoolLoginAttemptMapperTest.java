package kr.ac.tukorea.bandi.domain.member.mapper;

import kr.ac.tukorea.bandi.domain.member.model.SchoolLoginAttempt;
import kr.ac.tukorea.bandi.global.annotation.MapperTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@MapperTest
class SchoolLoginAttemptMapperTest {

    private static final String STUDENT_NO_HASH = "a".repeat(64);
    private static final LocalDateTime FIRST_FAILURE_AT =
            LocalDateTime.of(2026, 7, 25, 10, 0);

    private final SchoolLoginAttemptMapper schoolLoginAttemptMapper;

    @Autowired
    SchoolLoginAttemptMapperTest(SchoolLoginAttemptMapper schoolLoginAttemptMapper) {
        this.schoolLoginAttemptMapper = schoolLoginAttemptMapper;
    }

    @Test
    void 해시만으로_실패_상태를_생성_갱신하고_삭제한다() {
        schoolLoginAttemptMapper.insertIfAbsent(STUDENT_NO_HASH, FIRST_FAILURE_AT);
        SchoolLoginAttempt inserted = schoolLoginAttemptMapper
                .lookupByStudentNoHashForUpdate(STUDENT_NO_HASH)
                .orElseThrow();
        SchoolLoginAttempt blocked = new SchoolLoginAttempt(STUDENT_NO_HASH, 5,
                FIRST_FAILURE_AT, FIRST_FAILURE_AT.plusMinutes(15));

        int updated = schoolLoginAttemptMapper.update(blocked);
        int removed = schoolLoginAttemptMapper.removeByStudentNoHash(STUDENT_NO_HASH);

        assertThat(inserted.failureCount()).isZero();
        assertThat(updated).isOne();
        assertThat(removed).isOne();
        assertThat(schoolLoginAttemptMapper
                .lookupByStudentNoHashForUpdate(STUDENT_NO_HASH)).isEmpty();
    }
}
