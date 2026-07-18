package kr.ac.tukorea.bandi.domain.member.mapper;

import kr.ac.tukorea.bandi.domain.member.model.MemberRoleHistory;
import kr.ac.tukorea.bandi.domain.member.model.MemberTeamHistory;

import java.util.List;

public interface MemberHistoryMapper {

    int insertTeamHistory(MemberTeamHistory history);

    int insertRoleHistory(MemberRoleHistory history);

    List<MemberTeamHistory> searchTeamHistoryByMemberId(Long memberId);

    List<MemberRoleHistory> searchRoleHistoryByMemberId(Long memberId);
}
