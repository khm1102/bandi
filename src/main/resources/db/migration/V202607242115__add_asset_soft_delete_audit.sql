ALTER TABLE asset_history
    DROP CHECK ck_asset_history_action;

ALTER TABLE asset_history
    ADD CONSTRAINT ck_asset_history_action CHECK (
        action_code IN (
            'REGISTER', 'ADJUST', 'MOVE', 'LOAN', 'RETURN', 'REPAIR',
            'DAMAGE', 'LOST', 'DISPOSE', 'DELETE', 'RESTORE'
        )
    );

ALTER TABLE audit_log
    DROP CHECK ck_audit_log_action_code;

ALTER TABLE audit_log
    ADD CONSTRAINT ck_audit_log_action_code CHECK (
        action_code IN (
            'MEMBER_TEAM_CHANGED',
            'MEMBER_COHORT_CHANGED',
            'MEMBER_ROLE_CHANGED',
            'MEMBER_STATUS_CHANGED',
            'ASSET_DELETED',
            'ASSET_RESTORED'
        )
    );

ALTER TABLE audit_log
    DROP CHECK ck_audit_log_target_type_code;

ALTER TABLE audit_log
    ADD CONSTRAINT ck_audit_log_target_type_code CHECK (
        target_type_code IN ('MEMBER', 'ASSET')
    );

ALTER TABLE asset_item
    ADD INDEX idx_asset_item_deleted_name (
        deleted_dttm, name, asset_item_id
    );
