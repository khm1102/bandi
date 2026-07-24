ALTER TABLE activity_record
    DROP CHECK ck_activity_record_status_code,
    DROP CHECK ck_activity_record_status_dttm,
    ADD KEY idx_activity_record_status_updated (status_code, updated_dttm),
    ADD CONSTRAINT ck_activity_record_status_code CHECK (
        status_code IN ('DRAFT', 'SUBMITTED', 'TEAM_APPROVED', 'APPROVED',
                        'REVISION_REQUESTED', 'ARCHIVED')
    ),
    ADD CONSTRAINT ck_activity_record_status_dttm CHECK (
        (status_code = 'DRAFT'
            AND submitted_dttm IS NULL
            AND reviewed_dttm IS NULL
            AND reviewed_by_member_id IS NULL)
        OR (status_code = 'SUBMITTED'
            AND submitted_dttm IS NOT NULL
            AND reviewed_dttm IS NULL
            AND reviewed_by_member_id IS NULL)
        OR (status_code IN ('TEAM_APPROVED', 'APPROVED', 'REVISION_REQUESTED')
            AND submitted_dttm IS NOT NULL
            AND reviewed_dttm IS NOT NULL
            AND reviewed_by_member_id IS NOT NULL)
        OR status_code = 'ARCHIVED'
    );

ALTER TABLE activity_review_history
    DROP CHECK ck_activity_review_history_previous,
    DROP CHECK ck_activity_review_history_new,
    ADD CONSTRAINT ck_activity_review_history_previous CHECK (
        previous_status_code IN ('DRAFT', 'SUBMITTED', 'TEAM_APPROVED',
                                 'APPROVED', 'REVISION_REQUESTED', 'ARCHIVED')
    ),
    ADD CONSTRAINT ck_activity_review_history_new CHECK (
        new_status_code IN ('DRAFT', 'SUBMITTED', 'TEAM_APPROVED',
                            'APPROVED', 'REVISION_REQUESTED', 'ARCHIVED')
    );
