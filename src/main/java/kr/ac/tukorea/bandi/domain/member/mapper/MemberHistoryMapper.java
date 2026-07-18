package kr.ac.tukorea.bandi.domain.member.mapper;

import kr.ac.tukorea.bandi.domain.member.model.MemberCohortHistory;
import kr.ac.tukorea.bandi.domain.member.model.MemberRoleHistory;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatusHistory;
import kr.ac.tukorea.bandi.domain.member.model.MemberTeamHistory;

import java.util.List;

public interface MemberHistoryMapper {

    int insertTeamHistory(MemberTeamHistory history);

    int insertCohortHistory(MemberCohortHistory history);

    int insertRoleHistory(MemberRoleHistory history);

    int insertStatusHistory(MemberStatusHistory history);

    List<MemberTeamHistory> searchTeamHistoryByMemberId(Long memberId);

    List<MemberCohortHistory> searchCohortHistoryByMemberId(Long memberId);

    List<MemberRoleHistory> searchRoleHistoryByMemberId(Long memberId);

    List<MemberStatusHistory> searchStatusHistoryByMemberId(Long memberId);
}
