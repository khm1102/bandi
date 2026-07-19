ALTER TABLE asset_history
    DROP CHECK ck_asset_history_action;

ALTER TABLE asset_history
    ADD CONSTRAINT ck_asset_history_action CHECK (
        action_code IN ('REGISTER', 'ADJUST', 'MOVE', 'LOAN', 'RETURN',
                        'REPAIR', 'DAMAGE', 'LOST', 'DISPOSE')
    );
