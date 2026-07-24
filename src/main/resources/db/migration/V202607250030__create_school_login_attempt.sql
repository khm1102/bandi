-- 학교 포털 계정 잠금을 줄이기 위한 실패 제한 상태다.
-- 학번 원문은 보관하지 않고 서버 비밀값으로 생성한 HMAC-SHA-256 값만 보관한다.
CREATE TABLE school_login_attempt (
    school_login_attempt_id BIGINT      NOT NULL AUTO_INCREMENT,
    student_no_hash         CHAR(64)    NOT NULL,
    failure_count           INT         NOT NULL DEFAULT 0,
    first_failure_dttm      DATETIME(6) NULL,
    blocked_until_dttm      DATETIME(6) NULL,
    created_dttm            DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm            DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_school_login_attempt PRIMARY KEY (school_login_attempt_id),
    CONSTRAINT uk_school_login_attempt_student_no_hash UNIQUE (student_no_hash),
    CONSTRAINT ck_school_login_attempt_failure_count CHECK (failure_count >= 0)
) ENGINE=InnoDB;
