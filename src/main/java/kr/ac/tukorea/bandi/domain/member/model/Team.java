package kr.ac.tukorea.bandi.domain.member.model;

import kr.ac.tukorea.bandi.domain.member.exception.InactiveTeamException;
import lombok.Getter;

@Getter
public class Team {

    private Long teamId;
    private final String name;
    private final int displayOrder;
    private final boolean active;

    public Team(Long teamId, String name, int displayOrder, boolean active) {
        this.teamId = teamId;
        this.name = name;
        this.displayOrder = displayOrder;
        this.active = active;
    }

    /**
     * 비활성 팀에는 새 멤버를 배정할 수 없다 (정본 5.1).
     * 이미 이 팀에 속한 멤버의 기존 참조는 그대로 유지된다.
     */
    public void validateAssignable() {
        if (!active) {
            throw new InactiveTeamException(teamId);
        }
    }
}
