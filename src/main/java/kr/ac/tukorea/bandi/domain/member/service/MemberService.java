package kr.ac.tukorea.bandi.domain.member.service;

import kr.ac.tukorea.bandi.domain.member.dto.request.MemberPreRegisterParam;
import kr.ac.tukorea.bandi.domain.member.dto.request.RoleChangeParam;
import kr.ac.tukorea.bandi.domain.member.dto.request.StatusChangeParam;
import kr.ac.tukorea.bandi.domain.member.dto.request.TeamChangeParam;
import kr.ac.tukorea.bandi.domain.member.exception.ChangeReasonRequiredException;
import kr.ac.tukorea.bandi.domain.member.exception.CohortNotFoundException;
import kr.ac.tukorea.bandi.domain.member.exception.DuplicateStudentNoException;
import kr.ac.tukorea.bandi.domain.member.exception.LastActiveAdminException;
import kr.ac.tukorea.bandi.domain.member.exception.MemberNotFoundException;
import kr.ac.tukorea.bandi.domain.member.exception.TeamNotFoundException;
import kr.ac.tukorea.bandi.domain.member.mapper.CohortMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.MemberHistoryMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.MemberMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.TeamMapper;
import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.Member;
import kr.ac.tukorea.bandi.domain.member.model.MemberRoleHistory;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.MemberTeamHistory;
import kr.ac.tukorea.bandi.domain.member.model.Team;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 멤버 등록과 팀·권한·상태 변경.
 * 현재값 갱신과 이력 삽입은 항상 같은 트랜잭션에서 처리한다 (정본 5.4).
 * 로그에는 학번·이름 같은 개인정보를 남기지 않고 memberId만 남긴다 (컨벤션 20.3).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberMapper memberMapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final MemberHistoryMapper memberHistoryMapper;
    private final Clock clock;

    @Transactional
    public Long preRegister(MemberPreRegisterParam param) {
        if (memberMapper.existsByStudentNo(param.studentNo())) {
            throw new DuplicateStudentNoException();
        }
        findAssignableTeam(param.teamId());
        cohortMapper.lookupById(param.cohortId())
                .orElseThrow(() -> new CohortNotFoundException(param.cohortId()));

        Member member = Member.preRegister(param.studentNo(), param.name(), param.teamId(),
                param.cohortId(), param.role(), param.registeredByMemberId());
        memberMapper.insert(member);

        log.info("멤버 사전 등록 - memberId={}, teamId={}, cohortId={}",
                member.getMemberId(), param.teamId(), param.cohortId());
        return member.getMemberId();
    }

    @Transactional
    public void changeTeam(TeamChangeParam param) {
        validateReason(param.reason());
        Member member = lockMember(param.memberId());
        member.validateTeamChangeTo(param.newTeamId());
        findAssignableTeam(param.newTeamId());

        memberMapper.updateTeam(param.memberId(), param.newTeamId());
        memberHistoryMapper.insertTeamHistory(MemberTeamHistory.of(param.memberId(), member.getTeamId(),
                param.newTeamId(), param.reason(), param.actorMemberId(), now()));

        log.info("멤버 팀 변경 - memberId={}, previousTeamId={}, newTeamId={}",
                param.memberId(), member.getTeamId(), param.newTeamId());
    }

    @Transactional
    public void changeRole(RoleChangeParam param) {
        validateReason(param.reason());
        Member member = lockMember(param.memberId());
        member.validateRoleChangeTo(param.newRole(), param.actorMemberId());
        if (member.isActiveAdmin() && param.newRole() != ClubRole.ADMIN) {
            validateAnotherActiveAdminRemains(param.memberId());
        }

        memberMapper.updateRole(param.memberId(), param.newRole());
        memberHistoryMapper.insertRoleHistory(MemberRoleHistory.of(param.memberId(), member.getRole(),
                param.newRole(), param.reason(), param.actorMemberId(), now()));

        log.info("멤버 권한 변경 - memberId={}, previousRole={}, newRole={}, actorMemberId={}",
                param.memberId(), member.getRole(), param.newRole(), param.actorMemberId());
    }

    @Transactional
    public void changeStatus(StatusChangeParam param) {
        validateReason(param.reason());
        Member member = lockMember(param.memberId());
        member.validateStatusChangeTo(param.newStatus());
        if (member.isActiveAdmin() && param.newStatus() != MemberStatus.ACTIVE) {
            validateAnotherActiveAdminRemains(param.memberId());
        }

        memberMapper.updateStatus(param.memberId(), param.newStatus());

        log.info("멤버 상태 변경 - memberId={}, newStatus={}, actorMemberId={}",
                param.memberId(), param.newStatus(), param.actorMemberId());
    }

    private Member lockMember(Long memberId) {
        return memberMapper.lookupByIdForUpdate(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
    }

    private Team findAssignableTeam(Long teamId) {
        Team team = teamMapper.lookupById(teamId).orElseThrow(() -> new TeamNotFoundException(teamId));
        team.validateAssignable();
        return team;
    }

    /**
     * 활성 운영진 행을 잠근 뒤 대상 외에 다른 운영진이 남는지 확인한다 (정본 5.4).
     * 두 운영진이 동시에 서로를 강등해 0명이 되는 경합을 DB 잠금으로 차단한다.
     */
    private void validateAnotherActiveAdminRemains(Long targetMemberId) {
        List<Long> activeAdminIds = memberMapper.searchActiveAdminIdsForUpdate();
        boolean anotherAdminRemains = activeAdminIds.stream()
                .anyMatch(adminId -> !Objects.equals(adminId, targetMemberId));
        if (!anotherAdminRemains) {
            throw new LastActiveAdminException(targetMemberId);
        }
    }

    private void validateReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new ChangeReasonRequiredException();
        }
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
