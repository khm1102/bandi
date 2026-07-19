CREATE TABLE activity_record (
    activity_record_id   BIGINT       NOT NULL AUTO_INCREMENT,
    team_id              BIGINT       NOT NULL,
    activity_dttm        DATETIME(6)  NOT NULL,
    title                VARCHAR(150) NOT NULL,
    body                 TEXT         NOT NULL,
    participant_count    INT          NOT NULL,
    status_code          VARCHAR(30)  NOT NULL,
    created_by_member_id BIGINT       NOT NULL,
    updated_by_member_id BIGINT       NOT NULL,
    submitted_dttm       DATETIME(6)  NULL,
    reviewed_dttm        DATETIME(6)  NULL,
    reviewed_by_member_id BIGINT      NULL,
    created_dttm         DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm         DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_dttm         DATETIME(6)  NULL,
    CONSTRAINT pk_activity_record PRIMARY KEY (activity_record_id),
    KEY idx_activity_record_team_status_date (team_id, status_code, activity_dttm),
    KEY idx_activity_record_creator_status (created_by_member_id, status_code, updated_dttm),
    KEY idx_activity_record_updated_by (updated_by_member_id),
    KEY idx_activity_record_reviewed_by (reviewed_by_member_id),
    CONSTRAINT fk_activity_record_team FOREIGN KEY (team_id) REFERENCES team (team_id),
    CONSTRAINT fk_activity_record_created_by_member FOREIGN KEY (created_by_member_id) REFERENCES member (member_id),
    CONSTRAINT fk_activity_record_updated_by_member FOREIGN KEY (updated_by_member_id) REFERENCES member (member_id),
    CONSTRAINT fk_activity_record_reviewed_by_member FOREIGN KEY (reviewed_by_member_id) REFERENCES member (member_id),
    CONSTRAINT ck_activity_record_participant_count CHECK (participant_count > 0),
    CONSTRAINT ck_activity_record_status_code CHECK (
        status_code IN ('DRAFT', 'SUBMITTED', 'APPROVED', 'REVISION_REQUESTED', 'ARCHIVED')
    ),
    CONSTRAINT ck_activity_record_status_dttm CHECK (
        (status_code = 'DRAFT' AND submitted_dttm IS NULL AND reviewed_dttm IS NULL AND reviewed_by_member_id IS NULL)
        OR (status_code = 'SUBMITTED' AND submitted_dttm IS NOT NULL AND reviewed_dttm IS NULL AND reviewed_by_member_id IS NULL)
        OR (status_code IN ('APPROVED', 'REVISION_REQUESTED') AND submitted_dttm IS NOT NULL AND reviewed_dttm IS NOT NULL AND reviewed_by_member_id IS NOT NULL)
        OR status_code = 'ARCHIVED'
    )
) ENGINE=InnoDB;

CREATE TABLE activity_record_file (
    activity_record_file_id             BIGINT      NOT NULL AUTO_INCREMENT,
    activity_record_id                  BIGINT      NOT NULL,
    stored_file_id                      BIGINT      NOT NULL,
    file_role_code                      VARCHAR(20) NOT NULL,
    display_order                       INT         NOT NULL,
    uploaded_by_member_id               BIGINT      NOT NULL,
    replaced_by_activity_record_file_id BIGINT      NULL,
    replaced_dttm                       DATETIME(6) NULL,
    replaced_by_member_id               BIGINT      NULL,
    created_dttm                        DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm                        DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_activity_record_file PRIMARY KEY (activity_record_file_id),
    KEY idx_activity_record_file_current (activity_record_id, file_role_code, replaced_dttm),
    KEY idx_activity_record_file_stored (stored_file_id),
    KEY idx_activity_record_file_uploader (uploaded_by_member_id),
    KEY idx_activity_record_file_replacement (replaced_by_activity_record_file_id),
    KEY idx_activity_record_file_replacer (replaced_by_member_id),
    CONSTRAINT fk_activity_record_file_record FOREIGN KEY (activity_record_id) REFERENCES activity_record (activity_record_id),
    CONSTRAINT fk_activity_record_file_stored FOREIGN KEY (stored_file_id) REFERENCES stored_file (stored_file_id),
    CONSTRAINT fk_activity_record_file_uploader FOREIGN KEY (uploaded_by_member_id) REFERENCES member (member_id),
    CONSTRAINT fk_activity_record_file_replacement FOREIGN KEY (replaced_by_activity_record_file_id) REFERENCES activity_record_file (activity_record_file_id),
    CONSTRAINT fk_activity_record_file_replacer FOREIGN KEY (replaced_by_member_id) REFERENCES member (member_id),
    CONSTRAINT ck_activity_record_file_role CHECK (file_role_code IN ('EVIDENCE', 'ADDITIONAL')),
    CONSTRAINT ck_activity_record_file_order CHECK (display_order >= 0),
    CONSTRAINT ck_activity_record_file_replaced CHECK (
        (replaced_by_activity_record_file_id IS NULL AND replaced_dttm IS NULL AND replaced_by_member_id IS NULL)
        OR (replaced_by_activity_record_file_id IS NOT NULL AND replaced_dttm IS NOT NULL AND replaced_by_member_id IS NOT NULL)
    )
) ENGINE=InnoDB;

CREATE TABLE activity_record_revision (
    activity_record_revision_id BIGINT       NOT NULL AUTO_INCREMENT,
    activity_record_id          BIGINT       NOT NULL,
    revision_no                 INT          NOT NULL,
    activity_dttm               DATETIME(6)  NOT NULL,
    title                       VARCHAR(150) NOT NULL,
    body                        TEXT         NOT NULL,
    participant_count           INT          NOT NULL,
    changed_by_member_id        BIGINT       NOT NULL,
    changed_dttm                DATETIME(6)  NOT NULL,
    change_reason               VARCHAR(500) NULL,
    created_dttm                DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm                DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_activity_record_revision PRIMARY KEY (activity_record_revision_id),
    CONSTRAINT uk_activity_record_revision UNIQUE (activity_record_id, revision_no),
    KEY idx_activity_record_revision_changed_by (changed_by_member_id),
    CONSTRAINT fk_activity_record_revision_record FOREIGN KEY (activity_record_id) REFERENCES activity_record (activity_record_id),
    CONSTRAINT fk_activity_record_revision_changed_by FOREIGN KEY (changed_by_member_id) REFERENCES member (member_id),
    CONSTRAINT ck_activity_record_revision_no CHECK (revision_no > 0),
    CONSTRAINT ck_activity_record_revision_participants CHECK (participant_count > 0)
) ENGINE=InnoDB;

CREATE TABLE activity_review_history (
    activity_review_history_id BIGINT      NOT NULL AUTO_INCREMENT,
    activity_record_id         BIGINT      NOT NULL,
    previous_status_code       VARCHAR(30) NOT NULL,
    new_status_code            VARCHAR(30) NOT NULL,
    comment                    TEXT        NULL,
    reviewed_by_member_id      BIGINT      NOT NULL,
    reviewed_dttm              DATETIME(6) NOT NULL,
    created_dttm               DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm               DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_activity_review_history PRIMARY KEY (activity_review_history_id),
    KEY idx_activity_review_history_record_dttm (activity_record_id, reviewed_dttm),
    KEY idx_activity_review_history_reviewer (reviewed_by_member_id),
    CONSTRAINT fk_activity_review_history_record FOREIGN KEY (activity_record_id) REFERENCES activity_record (activity_record_id),
    CONSTRAINT fk_activity_review_history_reviewer FOREIGN KEY (reviewed_by_member_id) REFERENCES member (member_id),
    CONSTRAINT ck_activity_review_history_previous CHECK (previous_status_code IN ('DRAFT', 'SUBMITTED', 'APPROVED', 'REVISION_REQUESTED', 'ARCHIVED')),
    CONSTRAINT ck_activity_review_history_new CHECK (new_status_code IN ('DRAFT', 'SUBMITTED', 'APPROVED', 'REVISION_REQUESTED', 'ARCHIVED')),
    CONSTRAINT ck_activity_review_history_change CHECK (previous_status_code <> new_status_code),
    CONSTRAINT ck_activity_review_history_comment CHECK (new_status_code <> 'REVISION_REQUESTED' OR comment IS NOT NULL)
) ENGINE=InnoDB;
