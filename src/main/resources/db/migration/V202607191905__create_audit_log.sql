CREATE TABLE audit_log (
    audit_log_id BIGINT AUTO_INCREMENT,
    actor_member_id BIGINT NULL,
    action_code VARCHAR(50) NOT NULL,
    target_type_code VARCHAR(30) NOT NULL,
    target_id BIGINT NOT NULL,
    summary VARCHAR(500) NOT NULL,
    metadata_json JSON NULL,
    occurred_dttm DATETIME(6) NOT NULL,
    created_dttm DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_audit_log PRIMARY KEY (audit_log_id),
    CONSTRAINT fk_audit_log_member
        FOREIGN KEY (actor_member_id) REFERENCES member (member_id),
    CONSTRAINT ck_audit_log_action_code CHECK (
        action_code IN (
            'MEMBER_TEAM_CHANGED',
            'MEMBER_COHORT_CHANGED',
            'MEMBER_ROLE_CHANGED',
            'MEMBER_STATUS_CHANGED'
        )
    ),
    CONSTRAINT ck_audit_log_target_type_code CHECK (
        target_type_code IN ('MEMBER')
    ),
    KEY idx_audit_log_actor_member_id (actor_member_id),
    KEY idx_audit_log_target (
        target_type_code, target_id, occurred_dttm
    )
);
