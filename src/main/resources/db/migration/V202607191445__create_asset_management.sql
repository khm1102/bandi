CREATE TABLE asset_item (
    asset_item_id BIGINT AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    category_code VARCHAR(50) NOT NULL,
    tracking_type_code VARCHAR(30) NOT NULL,
    owner_type_code VARCHAR(30) NOT NULL,
    owner_member_id BIGINT NULL,
    external_owner_name VARCHAR(100) NULL,
    total_quantity INT NOT NULL,
    storage_location VARCHAR(200) NOT NULL,
    status_code VARCHAR(30) NOT NULL,
    photo_file_id BIGINT NULL,
    note TEXT NULL,
    created_dttm DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_dttm DATETIME(6) NULL,
    CONSTRAINT pk_asset_item PRIMARY KEY (asset_item_id),
    CONSTRAINT fk_asset_item_owner_member FOREIGN KEY (owner_member_id)
        REFERENCES member (member_id),
    CONSTRAINT fk_asset_item_photo_file FOREIGN KEY (photo_file_id)
        REFERENCES stored_file (stored_file_id),
    CONSTRAINT ck_asset_item_tracking_type CHECK (
        tracking_type_code IN ('QUANTITY', 'INDIVIDUAL')
    ),
    CONSTRAINT ck_asset_item_owner_type CHECK (
        owner_type_code IN ('CLUB', 'MEMBER', 'EXTERNAL')
    ),
    CONSTRAINT ck_asset_item_owner_scope CHECK (
        (owner_type_code = 'CLUB' AND owner_member_id IS NULL
            AND external_owner_name IS NULL)
        OR (owner_type_code = 'MEMBER' AND owner_member_id IS NOT NULL
            AND external_owner_name IS NULL)
        OR (owner_type_code = 'EXTERNAL' AND owner_member_id IS NULL
            AND external_owner_name IS NOT NULL)
    ),
    CONSTRAINT ck_asset_item_quantity CHECK (total_quantity > 0),
    CONSTRAINT ck_asset_item_status CHECK (
        status_code IN ('AVAILABLE', 'IN_USE', 'LOANED', 'REPAIR',
                        'LOST', 'DISPOSED')
    ),
    INDEX idx_asset_item_category_status (
        category_code, status_code, deleted_dttm
    ),
    INDEX idx_asset_item_owner_member (owner_member_id)
);

CREATE TABLE asset_unit (
    asset_unit_id BIGINT AUTO_INCREMENT,
    asset_item_id BIGINT NOT NULL,
    management_no VARCHAR(50) NOT NULL,
    status_code VARCHAR(30) NOT NULL,
    storage_location VARCHAR(200) NOT NULL,
    created_dttm DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_asset_unit PRIMARY KEY (asset_unit_id),
    CONSTRAINT fk_asset_unit_asset_item FOREIGN KEY (asset_item_id)
        REFERENCES asset_item (asset_item_id),
    CONSTRAINT uk_asset_unit_item_management_no UNIQUE (
        asset_item_id, management_no
    ),
    CONSTRAINT ck_asset_unit_status CHECK (
        status_code IN ('AVAILABLE', 'IN_USE', 'LOANED', 'REPAIR',
                        'LOST', 'DISPOSED')
    )
);

CREATE TABLE asset_usage (
    asset_usage_id BIGINT AUTO_INCREMENT,
    asset_item_id BIGINT NOT NULL,
    asset_unit_id BIGINT NULL,
    performance_project_id BIGINT NOT NULL,
    team_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    status_code VARCHAR(30) NOT NULL,
    start_dttm DATETIME(6) NOT NULL,
    expected_return_dttm DATETIME(6) NOT NULL,
    returned_dttm DATETIME(6) NULL,
    created_by_member_id BIGINT NOT NULL,
    processed_by_member_id BIGINT NOT NULL,
    note VARCHAR(500) NULL,
    created_dttm DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_asset_usage PRIMARY KEY (asset_usage_id),
    CONSTRAINT fk_asset_usage_asset_item FOREIGN KEY (asset_item_id)
        REFERENCES asset_item (asset_item_id),
    CONSTRAINT fk_asset_usage_asset_unit FOREIGN KEY (asset_unit_id)
        REFERENCES asset_unit (asset_unit_id),
    CONSTRAINT fk_asset_usage_project FOREIGN KEY (performance_project_id)
        REFERENCES performance_project (performance_project_id),
    CONSTRAINT fk_asset_usage_team FOREIGN KEY (team_id)
        REFERENCES team (team_id),
    CONSTRAINT fk_asset_usage_created_member FOREIGN KEY (created_by_member_id)
        REFERENCES member (member_id),
    CONSTRAINT fk_asset_usage_processed_member FOREIGN KEY (processed_by_member_id)
        REFERENCES member (member_id),
    CONSTRAINT ck_asset_usage_quantity CHECK (quantity > 0),
    CONSTRAINT ck_asset_usage_status CHECK (
        status_code IN ('RESERVED', 'IN_USE', 'RETURNED', 'CANCELLED')
    ),
    CONSTRAINT ck_asset_usage_period CHECK (
        expected_return_dttm >= start_dttm
    ),
    INDEX idx_asset_usage_item_status (asset_item_id, status_code),
    INDEX idx_asset_usage_unit_status (asset_unit_id, status_code),
    INDEX idx_asset_usage_project_team (
        performance_project_id, team_id, status_code
    )
);

CREATE TABLE asset_history (
    asset_history_id BIGINT AUTO_INCREMENT,
    asset_item_id BIGINT NOT NULL,
    asset_unit_id BIGINT NULL,
    action_code VARCHAR(30) NOT NULL,
    quantity INT NOT NULL,
    previous_status_code VARCHAR(30) NULL,
    new_status_code VARCHAR(30) NULL,
    note VARCHAR(500) NULL,
    changed_by_member_id BIGINT NOT NULL,
    changed_dttm DATETIME(6) NOT NULL,
    created_dttm DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_asset_history PRIMARY KEY (asset_history_id),
    CONSTRAINT fk_asset_history_asset_item FOREIGN KEY (asset_item_id)
        REFERENCES asset_item (asset_item_id),
    CONSTRAINT fk_asset_history_asset_unit FOREIGN KEY (asset_unit_id)
        REFERENCES asset_unit (asset_unit_id),
    CONSTRAINT fk_asset_history_changed_member FOREIGN KEY (changed_by_member_id)
        REFERENCES member (member_id),
    CONSTRAINT ck_asset_history_action CHECK (
        action_code IN ('REGISTER', 'MOVE', 'LOAN', 'RETURN', 'REPAIR',
                        'DAMAGE', 'LOST', 'DISPOSE')
    ),
    CONSTRAINT ck_asset_history_quantity CHECK (quantity > 0),
    CONSTRAINT ck_asset_history_previous_status CHECK (
        previous_status_code IS NULL OR previous_status_code IN (
            'AVAILABLE', 'IN_USE', 'LOANED', 'REPAIR', 'LOST', 'DISPOSED'
        )
    ),
    CONSTRAINT ck_asset_history_new_status CHECK (
        new_status_code IS NULL OR new_status_code IN (
            'AVAILABLE', 'IN_USE', 'LOANED', 'REPAIR', 'LOST', 'DISPOSED'
        )
    ),
    INDEX idx_asset_history_item_changed (
        asset_item_id, changed_dttm, asset_history_id
    )
);
