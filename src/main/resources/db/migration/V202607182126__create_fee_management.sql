CREATE TABLE fee_item (
    fee_item_id             BIGINT       NOT NULL AUTO_INCREMENT,
    name                    VARCHAR(150) NOT NULL,
    description             TEXT         NULL,
    reference_year          SMALLINT     NOT NULL,
    reference_term_code     VARCHAR(20)  NULL,
    amount                  BIGINT       NOT NULL,
    due_date                DATE         NOT NULL,
    status_code             VARCHAR(20)  NOT NULL,
    created_by_member_id    BIGINT       NOT NULL,
    updated_by_member_id    BIGINT       NOT NULL,
    created_dttm            DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm            DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_dttm            DATETIME(6)  NULL,
    CONSTRAINT pk_fee_item PRIMARY KEY (fee_item_id),
    KEY idx_fee_item_status_due_date (status_code, due_date),
    KEY idx_fee_item_reference (reference_year, reference_term_code),
    KEY idx_fee_item_created_by (created_by_member_id),
    KEY idx_fee_item_updated_by (updated_by_member_id),
    CONSTRAINT fk_fee_item_created_by_member FOREIGN KEY (created_by_member_id)
        REFERENCES member (member_id),
    CONSTRAINT fk_fee_item_updated_by_member FOREIGN KEY (updated_by_member_id)
        REFERENCES member (member_id),
    CONSTRAINT ck_fee_item_name CHECK (CHAR_LENGTH(TRIM(name)) > 0),
    CONSTRAINT ck_fee_item_reference_year CHECK (reference_year > 0),
    CONSTRAINT ck_fee_item_amount CHECK (amount > 0),
    CONSTRAINT ck_fee_item_status_code CHECK (
        status_code IN ('DRAFT', 'OPEN', 'CLOSED', 'CANCELLED')
    )
) ENGINE=InnoDB;

CREATE TABLE fee_charge (
    fee_charge_id           BIGINT       NOT NULL AUTO_INCREMENT,
    fee_item_id             BIGINT       NOT NULL,
    member_id               BIGINT       NOT NULL,
    charged_amount          BIGINT       NOT NULL,
    status_code             VARCHAR(20)  NOT NULL,
    paid_dttm               DATETIME(6)  NULL,
    processed_by_member_id  BIGINT       NULL,
    process_note            VARCHAR(500) NULL,
    created_dttm            DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm            DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_fee_charge PRIMARY KEY (fee_charge_id),
    CONSTRAINT uk_fee_charge_item_member UNIQUE (fee_item_id, member_id),
    KEY idx_fee_charge_item_status (fee_item_id, status_code),
    KEY idx_fee_charge_member_status (member_id, status_code),
    KEY idx_fee_charge_processed_by (processed_by_member_id),
    CONSTRAINT fk_fee_charge_fee_item FOREIGN KEY (fee_item_id)
        REFERENCES fee_item (fee_item_id),
    CONSTRAINT fk_fee_charge_member FOREIGN KEY (member_id)
        REFERENCES member (member_id),
    CONSTRAINT fk_fee_charge_processed_by_member FOREIGN KEY (processed_by_member_id)
        REFERENCES member (member_id),
    CONSTRAINT ck_fee_charge_amount CHECK (charged_amount > 0),
    CONSTRAINT ck_fee_charge_status_code CHECK (
        status_code IN ('UNPAID', 'PAID', 'EXEMPT', 'CANCELLED')
    ),
    CONSTRAINT ck_fee_charge_processing CHECK (
        (status_code = 'UNPAID' AND paid_dttm IS NULL
            AND ((processed_by_member_id IS NULL AND process_note IS NULL)
                OR processed_by_member_id IS NOT NULL))
        OR (status_code = 'PAID' AND paid_dttm IS NOT NULL
            AND processed_by_member_id IS NOT NULL)
        OR (status_code IN ('EXEMPT', 'CANCELLED') AND paid_dttm IS NULL
            AND processed_by_member_id IS NOT NULL)
    )
) ENGINE=InnoDB;

CREATE TABLE fee_charge_history (
    fee_charge_history_id   BIGINT       NOT NULL AUTO_INCREMENT,
    fee_charge_id           BIGINT       NOT NULL,
    previous_status_code    VARCHAR(20)  NOT NULL,
    new_status_code         VARCHAR(20)  NOT NULL,
    amount                  BIGINT       NOT NULL,
    reason                  VARCHAR(500) NULL,
    changed_by_member_id    BIGINT       NOT NULL,
    changed_dttm            DATETIME(6)  NOT NULL,
    created_dttm            DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm            DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_fee_charge_history PRIMARY KEY (fee_charge_history_id),
    KEY idx_fee_charge_history_charge_dttm (fee_charge_id, changed_dttm),
    KEY idx_fee_charge_history_changed_by (changed_by_member_id),
    CONSTRAINT fk_fee_charge_history_charge FOREIGN KEY (fee_charge_id)
        REFERENCES fee_charge (fee_charge_id),
    CONSTRAINT fk_fee_charge_history_changed_by_member FOREIGN KEY (changed_by_member_id)
        REFERENCES member (member_id),
    CONSTRAINT ck_fee_charge_history_previous CHECK (
        previous_status_code IN ('UNPAID', 'PAID', 'EXEMPT', 'CANCELLED')
    ),
    CONSTRAINT ck_fee_charge_history_new CHECK (
        new_status_code IN ('UNPAID', 'PAID', 'EXEMPT', 'CANCELLED')
    ),
    CONSTRAINT ck_fee_charge_history_change CHECK (
        previous_status_code <> new_status_code
    ),
    CONSTRAINT ck_fee_charge_history_amount CHECK (amount > 0)
) ENGINE=InnoDB;
