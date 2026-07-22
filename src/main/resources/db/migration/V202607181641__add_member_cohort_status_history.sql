-- 멤버 기수·상태 변경 이력 (docs/database-schema.md 5.4)
-- 기존 조직 기반 마이그레이션은 수정하지 않고 리뷰에서 확인된 이력 공백을 보완한다.

CREATE TABLE member_cohort_history (
    member_cohort_history_id BIGINT       NOT NULL AUTO_INCREMENT,
    member_id                BIGINT       NOT NULL,
    previous_cohort_id       BIGINT       NOT NULL,
    new_cohort_id            BIGINT       NOT NULL,
    reason                   VARCHAR(500) NOT NULL,
    changed_by_member_id     BIGINT       NOT NULL,
    changed_dttm             DATETIME(6)  NOT NULL,
    created_dttm             DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm             DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_member_cohort_history PRIMARY KEY (member_cohort_history_id),
    KEY idx_member_cohort_history_member_changed (member_id, changed_dttm),
    KEY idx_member_cohort_history_previous_cohort (previous_cohort_id),
    KEY idx_member_cohort_history_new_cohort (new_cohort_id),
    KEY idx_member_cohort_history_changed_by (changed_by_member_id),
    CONSTRAINT fk_member_cohort_history_member FOREIGN KEY (member_id) REFERENCES member (member_id),
    CONSTRAINT fk_member_cohort_history_previous_cohort FOREIGN KEY (previous_cohort_id) REFERENCES cohort (cohort_id),
    CONSTRAINT fk_member_cohort_history_new_cohort FOREIGN KEY (new_cohort_id) REFERENCES cohort (cohort_id),
    CONSTRAINT fk_member_cohort_history_changed_by_member FOREIGN KEY (changed_by_member_id) REFERENCES member (member_id)
) ENGINE=InnoDB;

CREATE TABLE member_status_history (
    member_status_history_id BIGINT       NOT NULL AUTO_INCREMENT,
    member_id                BIGINT       NOT NULL,
    previous_status_code     VARCHAR(30)  NOT NULL,
    new_status_code          VARCHAR(30)  NOT NULL,
    reason                   VARCHAR(500) NOT NULL,
    changed_by_member_id     BIGINT       NOT NULL,
    changed_dttm             DATETIME(6)  NOT NULL,
    created_dttm             DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm             DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_member_status_history PRIMARY KEY (member_status_history_id),
    KEY idx_member_status_history_member_changed (member_id, changed_dttm),
    KEY idx_member_status_history_changed_by (changed_by_member_id),
    CONSTRAINT fk_member_status_history_member FOREIGN KEY (member_id) REFERENCES member (member_id),
    CONSTRAINT fk_member_status_history_changed_by_member FOREIGN KEY (changed_by_member_id) REFERENCES member (member_id),
    CONSTRAINT ck_member_status_history_previous_status_code CHECK (
        previous_status_code IN ('PRE_REGISTERED', 'ACTIVE', 'SUSPENDED', 'WITHDRAWN', 'REGISTRATION_CANCELLED')
    ),
    CONSTRAINT ck_member_status_history_new_status_code CHECK (
        new_status_code IN ('PRE_REGISTERED', 'ACTIVE', 'SUSPENDED', 'WITHDRAWN', 'REGISTRATION_CANCELLED')
    )
) ENGINE=InnoDB;
