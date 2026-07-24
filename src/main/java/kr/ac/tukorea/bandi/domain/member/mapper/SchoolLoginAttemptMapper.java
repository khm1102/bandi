package kr.ac.tukorea.bandi.domain.member.mapper;

import kr.ac.tukorea.bandi.domain.member.model.SchoolLoginAttempt;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SchoolLoginAttemptMapper {

    Optional<SchoolLoginAttempt> lookupByStudentNoHashForUpdate(String studentNoHash);

    int insertIfAbsent(@Param("studentNoHash") String studentNoHash,
                       @Param("currentDttm") LocalDateTime currentDttm);

    int update(SchoolLoginAttempt attempt);

    int removeByStudentNoHash(String studentNoHash);
}
