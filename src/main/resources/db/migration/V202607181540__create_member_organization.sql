-- 멤버 조직 기반 스키마 (docs/database-schema.md 5장)
-- team, cohort, member와 팀·권한 변경 이력.
-- 세 테이블 모두 deleted_dttm을 두지 않는다:
--   team/cohort는 is_active로 종료하고(5.1), member는 학번 정체성의 정본이라
--   소프트 삭제 후 같은 학번으로 새 행을 만들지 않는다(5.3). 이력은 append-only.
-- 인덱스는 CREATE TABLE 안에 KEY로 선언한다. FK보다 먼저 선언해야 MySQL이
-- 해당 인덱스를 FK에 재사용하고 중복 인덱스를 자동 생성하지 않는다.

CREATE TABLE team (
    team_id       BIGINT      NOT NULL AUTO_INCREMENT,
    name          VARCHAR(50) NOT NULL,
    display_order INT         NOT NULL,
    is_active     TINYINT(1)  NOT NULL DEFAULT 1,
    created_dttm  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_team PRIMARY KEY (team_id),
    CONSTRAINT uk_team_name UNIQUE (name),
    KEY idx_team_is_active_display_order (is_active, display_order)
) ENGINE=InnoDB;

CREATE TABLE cohort (
    cohort_id      BIGINT      NOT NULL AUTO_INCREMENT,
    name           VARCHAR(30) NOT NULL,
    admission_year SMALLINT    NOT NULL,
    term_code      VARCHAR(20) NOT NULL,
    is_active      TINYINT(1)  NOT NULL DEFAULT 1,
    created_dttm   DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm   DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_cohort PRIMARY KEY (cohort_id),
    CONSTRAINT uk_cohort_name UNIQUE (name),
    CONSTRAINT uk_cohort_year_term UNIQUE (admission_year, term_code)
) ENGINE=InnoDB;

-- academic_status_code는 학교 SSO 연동이 채우는 값이다. 정본에 전체 값 목록이
-- 정의되지 않아 CHECK 제약과 Java enum을 두지 않는다(SSO 구현 시점에 확정).
CREATE TABLE member (
    member_id                     BIGINT       NOT NULL AUTO_INCREMENT,
    student_no                    VARCHAR(20)  NOT NULL,
    name                          VARCHAR(50)  NOT NULL,
    department                    VARCHAR(100) NULL,
    academic_status_code          VARCHAR(30)  NULL,
    academic_status_verified_dttm DATETIME(6)  NULL,
    team_id                       BIGINT       NOT NULL,
    cohort_id                     BIGINT       NOT NULL,
    role_code                     VARCHAR(30)  NOT NULL,
    member_status_code            VARCHAR(30)  NOT NULL,
    sso_link_status_code          VARCHAR(30)  NOT NULL,
    sso_linked_dttm               DATETIME(6)  NULL,
    last_login_dttm               DATETIME(6)  NULL,
    registered_by_member_id       BIGINT       NULL,
    created_dttm                  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm                  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_member PRIMARY KEY (member_id),
    CONSTRAINT uk_member_student_no UNIQUE (student_no),
    KEY idx_member_team_status (team_id, member_status_code),
    KEY idx_member_cohort_status (cohort_id, member_status_code),
    KEY idx_member_sso_link_status (sso_link_status_code),
    KEY idx_member_registered_by (registered_by_member_id),
    CONSTRAINT fk_member_team FOREIGN KEY (team_id) REFERENCES team (team_id),
    CONSTRAINT fk_member_cohort FOREIGN KEY (cohort_id) REFERENCES cohort (cohort_id),
    CONSTRAINT fk_member_registered_by_member FOREIGN KEY (registered_by_member_id) REFERENCES member (member_id),
    CONSTRAINT ck_member_role_code CHECK (role_code IN ('ADMIN', 'LEADER', 'MEMBER')),
    CONSTRAINT ck_member_status_code CHECK (member_status_code IN ('PRE_REGISTERED', 'ACTIVE', 'SUSPENDED', 'WITHDRAWN', 'REGISTRATION_CANCELLED')),
    CONSTRAINT ck_member_sso_link_status_code CHECK (sso_link_status_code IN ('WAITING', 'LINKED', 'REVIEW_REQUIRED'))
) ENGINE=InnoDB;

-- 이력은 '변경'만 기록한다. 최초 배정은 member 행 생성으로 표현하므로
-- previous_* 컬럼은 NOT NULL이다. reason은 변경 사유 필수 규칙을 DB에서도 강제한다.
-- changed_dttm은 Service가 Clock으로 채우는 업무 시각이며 created_dttm(적재 시각)과 구분한다.
CREATE TABLE member_team_history (
    member_team_history_id BIGINT       NOT NULL AUTO_INCREMENT,
    member_id              BIGINT       NOT NULL,
    previous_team_id       BIGINT       NOT NULL,
    new_team_id            BIGINT       NOT NULL,
    reason                 VARCHAR(500) NOT NULL,
    changed_by_member_id   BIGINT       NOT NULL,
    changed_dttm           DATETIME(6)  NOT NULL,
    created_dttm           DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm           DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_member_team_history PRIMARY KEY (member_team_history_id),
    KEY idx_member_team_history_member_changed (member_id, changed_dttm),
    KEY idx_member_team_history_previous_team (previous_team_id),
    KEY idx_member_team_history_new_team (new_team_id),
    KEY idx_member_team_history_changed_by (changed_by_member_id),
    CONSTRAINT fk_member_team_history_member FOREIGN KEY (member_id) REFERENCES member (member_id),
    CONSTRAINT fk_member_team_history_previous_team FOREIGN KEY (previous_team_id) REFERENCES team (team_id),
    CONSTRAINT fk_member_team_history_new_team FOREIGN KEY (new_team_id) REFERENCES team (team_id),
    CONSTRAINT fk_member_team_history_changed_by_member FOREIGN KEY (changed_by_member_id) REFERENCES member (member_id)
) ENGINE=InnoDB;

CREATE TABLE member_role_history (
    member_role_history_id BIGINT       NOT NULL AUTO_INCREMENT,
    member_id              BIGINT       NOT NULL,
    previous_role_code     VARCHAR(30)  NOT NULL,
    new_role_code          VARCHAR(30)  NOT NULL,
    reason                 VARCHAR(500) NOT NULL,
    changed_by_member_id   BIGINT       NOT NULL,
    changed_dttm           DATETIME(6)  NOT NULL,
    created_dttm           DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm           DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_member_role_history PRIMARY KEY (member_role_history_id),
    KEY idx_member_role_history_member_changed (member_id, changed_dttm),
    KEY idx_member_role_history_changed_by (changed_by_member_id),
    CONSTRAINT fk_member_role_history_member FOREIGN KEY (member_id) REFERENCES member (member_id),
    CONSTRAINT fk_member_role_history_changed_by_member FOREIGN KEY (changed_by_member_id) REFERENCES member (member_id),
    CONSTRAINT ck_member_role_history_previous_role_code CHECK (previous_role_code IN ('ADMIN', 'LEADER', 'MEMBER')),
    CONSTRAINT ck_member_role_history_new_role_code CHECK (new_role_code IN ('ADMIN', 'LEADER', 'MEMBER'))
) ENGINE=InnoDB;
