package kr.ac.tukorea.bandi.domain.member.model;

/**
 * 학교 SSO 계정 연결 상태 (docs/database-schema.md 5.3).
 */
public enum SsoLinkStatus {

    /** 사전 등록만 된 상태, 최초 학교 인증 대기 */
    WAITING,

    /** 학번으로 학교 계정과 연결 완료 */
    LINKED,

    /** 등록 이름과 학교 이름이 달라 운영진 확인이 필요 */
    REVIEW_REQUIRED
}
