package kr.ac.tukorea.bandi.domain.member.model;

/**
 * 동아리 활동 상태 (docs/database-schema.md 5.3).
 * 학교 학적 상태와 별도로 관리하며 두 조건을 모두 통과해야 내부 기능을 사용할 수 있다.
 */
public enum MemberStatus {

    /** 운영진이 사전 등록했고 아직 SSO 연결 전 */
    PRE_REGISTERED,

    /** 활동 중 */
    ACTIVE,

    /** 활동 중지 */
    SUSPENDED,

    /** 탈퇴 */
    WITHDRAWN,

    /** 합격 취소·중복 등록 등으로 등록이 취소됨 */
    REGISTRATION_CANCELLED;

    public boolean canTransitionByAdminTo(MemberStatus newStatus) {
        return switch (this) {
            case PRE_REGISTERED -> newStatus == REGISTRATION_CANCELLED;
            case ACTIVE -> newStatus == SUSPENDED || newStatus == WITHDRAWN;
            case SUSPENDED -> newStatus == ACTIVE || newStatus == WITHDRAWN;
            case WITHDRAWN -> newStatus == ACTIVE;
            case REGISTRATION_CANCELLED -> false;
        };
    }
}
