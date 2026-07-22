package kr.ac.tukorea.bandi.domain.member.dto.response;

import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.MemberCohortHistory;
import kr.ac.tukorea.bandi.domain.member.model.MemberRoleHistory;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatusHistory;
import kr.ac.tukorea.bandi.domain.member.model.MemberTeamHistory;

import java.time.LocalDateTime;
import java.util.List;

public record MemberHistoryResponse(
        List<TeamHistoryResponse> teamHistories,
        List<CohortHistoryResponse> cohortHistories,
        List<RoleHistoryResponse> roleHistories,
        List<StatusHistoryResponse> statusHistories
) {

    public MemberHistoryResponse {
        teamHistories = List.copyOf(teamHistories);
        cohortHistories = List.copyOf(cohortHistories);
        roleHistories = List.copyOf(roleHistories);
        statusHistories = List.copyOf(statusHistories);
    }

    public static MemberHistoryResponse from(
            List<MemberTeamHistory> teamHistories,
            List<MemberCohortHistory> cohortHistories,
            List<MemberRoleHistory> roleHistories,
            List<MemberStatusHistory> statusHistories) {
        return new MemberHistoryResponse(
                teamHistories.stream().map(TeamHistoryResponse::from).toList(),
                cohortHistories.stream().map(CohortHistoryResponse::from)
                        .toList(),
                roleHistories.stream().map(RoleHistoryResponse::from).toList(),
                statusHistories.stream().map(StatusHistoryResponse::from)
                        .toList());
    }

    public record TeamHistoryResponse(
            Long historyId,
            Long previousTeamId,
            Long newTeamId,
            String reason,
            Long changedByMemberId,
            LocalDateTime changedDttm
    ) {

        private static TeamHistoryResponse from(MemberTeamHistory history) {
            return new TeamHistoryResponse(history.memberTeamHistoryId(),
                    history.previousTeamId(), history.newTeamId(),
                    history.reason(), history.changedByMemberId(),
                    history.changedDttm());
        }
    }

    public record CohortHistoryResponse(
            Long historyId,
            Long previousCohortId,
            Long newCohortId,
            String reason,
            Long changedByMemberId,
            LocalDateTime changedDttm
    ) {

        private static CohortHistoryResponse from(
                MemberCohortHistory history) {
            return new CohortHistoryResponse(history.memberCohortHistoryId(),
                    history.previousCohortId(), history.newCohortId(),
                    history.reason(), history.changedByMemberId(),
                    history.changedDttm());
        }
    }

    public record RoleHistoryResponse(
            Long historyId,
            ClubRole previousRole,
            ClubRole newRole,
            String reason,
            Long changedByMemberId,
            LocalDateTime changedDttm
    ) {

        private static RoleHistoryResponse from(MemberRoleHistory history) {
            return new RoleHistoryResponse(history.memberRoleHistoryId(),
                    history.previousRole(), history.newRole(), history.reason(),
                    history.changedByMemberId(), history.changedDttm());
        }
    }

    public record StatusHistoryResponse(
            Long historyId,
            MemberStatus previousStatus,
            MemberStatus newStatus,
            String reason,
            Long changedByMemberId,
            LocalDateTime changedDttm
    ) {

        private static StatusHistoryResponse from(
                MemberStatusHistory history) {
            return new StatusHistoryResponse(history.memberStatusHistoryId(),
                    history.previousStatus(), history.newStatus(),
                    history.reason(), history.changedByMemberId(),
                    history.changedDttm());
        }
    }
}
