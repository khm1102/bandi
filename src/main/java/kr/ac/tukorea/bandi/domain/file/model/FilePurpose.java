package kr.ac.tukorea.bandi.domain.file.model;

/**
 * 파일의 업무 연결 범위를 구분한다.
 * 프로필 사진은 내부 아바타 전용이므로 일반 첨부와 교차 연결할 수 없다.
 */
public enum FilePurpose {

    GENERAL,
    PROFILE_IMAGE
}
