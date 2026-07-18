package kr.ac.tukorea.bandi.domain.member.model;

/**
 * 동아리 권한 (컨벤션 18.2, docs/database-schema.md 5.3).
 * DB의 member.role_code와 ck_member_role_code 제약이 이 세 값과 동기화된다.
 */
public enum ClubRole {

    /** 전체 운영 */
    ADMIN,

    /** 소속 팀 관리 */
    LEADER,

    /** 일반 부원 */
    MEMBER
}
