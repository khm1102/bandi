package kr.ac.tukorea.bandi.domain.member.mapper;

import kr.ac.tukorea.bandi.domain.member.dto.request.MemberSearchCondition;
import kr.ac.tukorea.bandi.domain.member.dto.request.MemberPageSearchCondition;
import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.Member;
import kr.ac.tukorea.bandi.domain.member.model.MemberSchoolConnection;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

public interface MemberMapper {

    Optional<Member> lookupById(Long memberId);

    Optional<Member> lookupByStudentNo(String studentNo);

    Optional<Member> lookupByStudentNoForUpdate(String studentNo);

    /**
     * 변경 대상 멤버를 잠금 조회한다. 팀·권한·상태 변경 트랜잭션의 시작점이다.
     */
    Optional<Member> lookupByIdForUpdate(Long memberId);

    List<Member> searchByCondition(MemberSearchCondition condition);

    List<Member> searchPage(MemberPageSearchCondition condition);

    long countByPageCondition(MemberPageSearchCondition condition);

    long countActive();

    long countSsoVerificationRequired();

    /**
     * 활성 운영진의 식별자를 잠금 조회한다. 마지막 운영진 보호 규칙(정본 5.4)이
     * 동시 요청에서도 성립하도록 검증 전에 해당 행을 잠근다.
     */
    List<Long> searchActiveAdminIdsForUpdate();

    boolean existsByStudentNo(String studentNo);

    int insert(Member member);

    int updateTeam(@Param("memberId") Long memberId, @Param("teamId") Long teamId);

    int updateCohort(@Param("memberId") Long memberId, @Param("cohortId") Long cohortId);

    int updateRole(@Param("memberId") Long memberId, @Param("role") ClubRole role);

    int updateStatus(@Param("memberId") Long memberId, @Param("status") MemberStatus status);

    int updateSchoolConnection(MemberSchoolConnection connection);

    int updateProfilePhoto(@Param("memberId") Long memberId,
                           @Param("profilePhotoFileId") Long profilePhotoFileId);
}
