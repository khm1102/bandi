CREATE TABLE production_task (
    production_task_id      BIGINT       NOT NULL AUTO_INCREMENT,
    performance_project_id  BIGINT       NOT NULL,
    team_id                 BIGINT       NOT NULL,
    title                   VARCHAR(200) NOT NULL,
    description             TEXT         NULL,
    start_date              DATE         NULL,
    due_date                DATE         NULL,
    status_code             VARCHAR(30)  NOT NULL,
    blocked_reason          VARCHAR(500) NULL,
    created_by_member_id    BIGINT       NOT NULL,
    updated_by_member_id    BIGINT       NOT NULL,
    created_dttm            DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm            DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_dttm            DATETIME(6)  NULL,
    CONSTRAINT pk_production_task PRIMARY KEY (production_task_id),
    KEY idx_production_task_project_status_due (
        performance_project_id, status_code, due_date, deleted_dttm
    ),
    KEY idx_production_task_project_team_status (
        performance_project_id, team_id, status_code, deleted_dttm
    ),
    KEY idx_production_task_created_by (created_by_member_id),
    KEY idx_production_task_updated_by (updated_by_member_id),
    CONSTRAINT fk_production_task_performance_project FOREIGN KEY (performance_project_id)
        REFERENCES performance_project (performance_project_id),
    CONSTRAINT fk_production_task_team FOREIGN KEY (team_id)
        REFERENCES team (team_id),
    CONSTRAINT fk_production_task_created_by_member FOREIGN KEY (created_by_member_id)
        REFERENCES member (member_id),
    CONSTRAINT fk_production_task_updated_by_member FOREIGN KEY (updated_by_member_id)
        REFERENCES member (member_id),
    CONSTRAINT ck_production_task_title CHECK (
        CHAR_LENGTH(TRIM(title)) > 0
    ),
    CONSTRAINT ck_production_task_dates CHECK (
        start_date IS NULL OR due_date IS NULL OR due_date >= start_date
    ),
    CONSTRAINT ck_production_task_status_code CHECK (
        status_code IN (
            'TODO', 'IN_PROGRESS', 'REVIEW_REQUIRED', 'BLOCKED', 'COMPLETED'
        )
    ),
    CONSTRAINT ck_production_task_blocked_reason CHECK (
        (status_code = 'BLOCKED' AND blocked_reason IS NOT NULL
            AND CHAR_LENGTH(TRIM(blocked_reason)) > 0)
        OR (status_code <> 'BLOCKED' AND blocked_reason IS NULL)
    )
) ENGINE=InnoDB;

CREATE TABLE production_task_history (
    production_task_history_id BIGINT       NOT NULL AUTO_INCREMENT,
    production_task_id         BIGINT       NOT NULL,
    previous_status_code       VARCHAR(30)  NOT NULL,
    new_status_code            VARCHAR(30)  NOT NULL,
    comment                    VARCHAR(500) NULL,
    changed_by_member_id       BIGINT       NOT NULL,
    changed_dttm               DATETIME(6)  NOT NULL,
    created_dttm               DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm               DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_production_task_history PRIMARY KEY (production_task_history_id),
    KEY idx_production_task_history_task_dttm (
        production_task_id, changed_dttm
    ),
    KEY idx_production_task_history_changed_by (changed_by_member_id),
    CONSTRAINT fk_production_task_history_task FOREIGN KEY (production_task_id)
        REFERENCES production_task (production_task_id),
    CONSTRAINT fk_production_task_history_changed_by_member FOREIGN KEY (changed_by_member_id)
        REFERENCES member (member_id),
    CONSTRAINT ck_production_task_history_previous CHECK (
        previous_status_code IN (
            'TODO', 'IN_PROGRESS', 'REVIEW_REQUIRED', 'BLOCKED', 'COMPLETED'
        )
    ),
    CONSTRAINT ck_production_task_history_new CHECK (
        new_status_code IN (
            'TODO', 'IN_PROGRESS', 'REVIEW_REQUIRED', 'BLOCKED', 'COMPLETED'
        )
    ),
    CONSTRAINT ck_production_task_history_change CHECK (
        previous_status_code <> new_status_code
    )
) ENGINE=InnoDB;
