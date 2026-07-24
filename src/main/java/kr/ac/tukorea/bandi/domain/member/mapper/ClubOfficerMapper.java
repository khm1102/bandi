package kr.ac.tukorea.bandi.domain.member.mapper;

import kr.ac.tukorea.bandi.domain.member.model.ClubOfficerPosition;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ClubOfficerMapper {

    Optional<String> lookupActiveMemberNameByPosition(ClubOfficerPosition position);

    int insert(@Param("position") ClubOfficerPosition position,
               @Param("memberId") Long memberId,
               @Param("appointedDttm") LocalDateTime appointedDttm);

    int updateMember(@Param("position") ClubOfficerPosition position,
                     @Param("memberId") Long memberId,
                     @Param("appointedDttm") LocalDateTime appointedDttm);
}
