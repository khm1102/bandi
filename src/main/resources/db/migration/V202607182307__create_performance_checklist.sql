CREATE TABLE checklist_item (
    checklist_item_id          BIGINT        NOT NULL AUTO_INCREMENT,
    performance_project_id    BIGINT        NOT NULL,
    performance_round_id      BIGINT        NULL,
    team_id                   BIGINT        NOT NULL,
    scope_code                VARCHAR(30)   NOT NULL,
    content                   VARCHAR(500)  NOT NULL,
    is_required               TINYINT(1)    NOT NULL,
    display_order             INT           NOT NULL,
    is_completed              TINYINT(1)    NOT NULL DEFAULT 0,
    completed_by_member_id    BIGINT        NULL,
    completed_dttm            DATETIME(6)   NULL,
    created_by_member_id      BIGINT        NOT NULL,
    updated_by_member_id      BIGINT        NOT NULL,
    created_dttm              DATETIME(6)   NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm              DATETIME(6)   NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_dttm              DATETIME(6)   NULL,
    CONSTRAINT pk_checklist_item PRIMARY KEY (checklist_item_id),
    KEY idx_checklist_item_project_scope_team_order (
        performance_project_id, scope_code, team_id,
        display_order, deleted_dttm
    ),
    KEY idx_checklist_item_round (performance_round_id),
    KEY idx_checklist_item_completed_by (completed_by_member_id),
    KEY idx_checklist_item_created_by (created_by_member_id),
    KEY idx_checklist_item_updated_by (updated_by_member_id),
    CONSTRAINT fk_checklist_item_project FOREIGN KEY (
        performance_project_id
    ) REFERENCES performance_project (performance_project_id),
    CONSTRAINT fk_checklist_item_round_project FOREIGN KEY (
        performance_round_id, performance_project_id
    ) REFERENCES performance_round (
        performance_round_id, performance_project_id
    ),
    CONSTRAINT fk_checklist_item_team FOREIGN KEY (team_id)
        REFERENCES team (team_id),
    CONSTRAINT fk_checklist_item_completed_by_member FOREIGN KEY (
        completed_by_member_id
    ) REFERENCES member (member_id),
    CONSTRAINT fk_checklist_item_created_by_member FOREIGN KEY (
        created_by_member_id
    ) REFERENCES member (member_id),
    CONSTRAINT fk_checklist_item_updated_by_member FOREIGN KEY (
        updated_by_member_id
    ) REFERENCES member (member_id),
    CONSTRAINT ck_checklist_item_scope CHECK (
        (scope_code = 'PROJECT' AND performance_round_id IS NULL)
        OR (scope_code = 'ROUND' AND performance_round_id IS NOT NULL)
    ),
    CONSTRAINT ck_checklist_item_content CHECK (
        CHAR_LENGTH(TRIM(content)) > 0
    ),
    CONSTRAINT ck_checklist_item_required CHECK (is_required IN (0, 1)),
    CONSTRAINT ck_checklist_item_order CHECK (display_order >= 0),
    CONSTRAINT ck_checklist_item_completed CHECK (
        is_completed IN (0, 1)
        AND ((is_completed = 0
            AND completed_by_member_id IS NULL
            AND completed_dttm IS NULL)
        OR (is_completed = 1
            AND completed_by_member_id IS NOT NULL
            AND completed_dttm IS NOT NULL))
    )
) ENGINE=InnoDB;

CREATE TABLE checklist_item_history (
    checklist_item_history_id  BIGINT        NOT NULL AUTO_INCREMENT,
    checklist_item_id          BIGINT        NOT NULL,
    previous_completed         TINYINT(1)    NOT NULL,
    new_completed              TINYINT(1)    NOT NULL,
    changed_by_member_id       BIGINT        NOT NULL,
    changed_dttm               DATETIME(6)   NOT NULL,
    reason                     VARCHAR(500)  NULL,
    created_dttm               DATETIME(6)   NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm               DATETIME(6)   NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_checklist_item_history PRIMARY KEY (
        checklist_item_history_id
    ),
    KEY idx_checklist_item_history_item_changed (
        checklist_item_id, changed_dttm,
        checklist_item_history_id
    ),
    KEY idx_checklist_item_history_changed_by (changed_by_member_id),
    CONSTRAINT fk_checklist_item_history_item FOREIGN KEY (
        checklist_item_id
    ) REFERENCES checklist_item (checklist_item_id),
    CONSTRAINT fk_checklist_item_history_changed_by_member FOREIGN KEY (
        changed_by_member_id
    ) REFERENCES member (member_id),
    CONSTRAINT ck_checklist_item_history_completed CHECK (
        previous_completed IN (0, 1)
        AND new_completed IN (0, 1)
        AND previous_completed <> new_completed
    )
) ENGINE=InnoDB;
