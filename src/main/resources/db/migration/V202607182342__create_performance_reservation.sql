CREATE TABLE performance_round_seat (
    performance_round_seat_id  BIGINT       NOT NULL AUTO_INCREMENT,
    performance_round_id       BIGINT       NOT NULL,
    seat_label                 VARCHAR(30)  NOT NULL,
    section_code               VARCHAR(30)  NULL,
    row_label                  VARCHAR(30)  NULL,
    column_label               VARCHAR(30)  NULL,
    display_row                INT          NULL,
    display_column             INT          NULL,
    status_code                VARCHAR(30)  NOT NULL,
    accessibility_code         VARCHAR(30)  NULL,
    created_dttm               DATETIME(6)  NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm               DATETIME(6)  NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_performance_round_seat PRIMARY KEY (
        performance_round_seat_id
    ),
    CONSTRAINT uk_performance_round_seat_round_label UNIQUE (
        performance_round_id, seat_label
    ),
    KEY idx_performance_round_seat_round_status (
        performance_round_id, status_code
    ),
    CONSTRAINT fk_performance_round_seat_round FOREIGN KEY (
        performance_round_id
    ) REFERENCES performance_round (performance_round_id),
    CONSTRAINT ck_performance_round_seat_label CHECK (
        CHAR_LENGTH(TRIM(seat_label)) > 0
    ),
    CONSTRAINT ck_performance_round_seat_position CHECK (
        (display_row IS NULL OR display_row >= 0)
        AND (display_column IS NULL OR display_column >= 0)
    ),
    CONSTRAINT ck_performance_round_seat_status CHECK (
        status_code IN ('AVAILABLE', 'BLOCKED')
    )
) ENGINE=InnoDB;

CREATE TABLE reservation (
    reservation_id             BIGINT          NOT NULL AUTO_INCREMENT,
    performance_round_id       BIGINT          NOT NULL,
    reservation_no             VARCHAR(30)     NOT NULL,
    lookup_token_hash          CHAR(64)        NULL,
    entry_token_hash           CHAR(64)        NULL,
    applicant_name_ciphertext  VARBINARY(512)  NULL,
    phone_ciphertext           VARBINARY(512)  NULL,
    phone_search_hash          CHAR(64)        NULL,
    encryption_key_version     SMALLINT        NOT NULL,
    status_code                VARCHAR(30)     NOT NULL,
    privacy_policy_version_id  BIGINT          NOT NULL,
    agreed_dttm                DATETIME(6)     NOT NULL,
    cancelled_dttm             DATETIME(6)     NULL,
    cancel_reason              VARCHAR(500)    NULL,
    personal_data_erased_dttm  DATETIME(6)     NULL,
    created_dttm               DATETIME(6)     NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm               DATETIME(6)     NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_reservation PRIMARY KEY (reservation_id),
    CONSTRAINT uk_reservation_no UNIQUE (reservation_no),
    CONSTRAINT uk_reservation_lookup_token_hash UNIQUE (
        lookup_token_hash
    ),
    CONSTRAINT uk_reservation_entry_token_hash UNIQUE (
        entry_token_hash
    ),
    KEY idx_reservation_round_status (
        performance_round_id, status_code
    ),
    KEY idx_reservation_phone_search_hash (phone_search_hash),
    KEY idx_reservation_personal_data_erased (
        personal_data_erased_dttm, performance_round_id
    ),
    CONSTRAINT fk_reservation_round FOREIGN KEY (
        performance_round_id
    ) REFERENCES performance_round (performance_round_id),
    CONSTRAINT fk_reservation_policy_version FOREIGN KEY (
        privacy_policy_version_id
    ) REFERENCES policy_document_version (policy_document_version_id),
    CONSTRAINT ck_reservation_key_version CHECK (
        encryption_key_version > 0
    ),
    CONSTRAINT ck_reservation_status CHECK (
        status_code IN (
            'CONFIRMED', 'PARTIALLY_CANCELLED', 'CANCELLED'
        )
    ),
    CONSTRAINT ck_reservation_cancellation CHECK (
        (
            status_code = 'CANCELLED'
            AND cancelled_dttm IS NOT NULL
            AND cancel_reason IS NOT NULL
            AND CHAR_LENGTH(TRIM(cancel_reason)) > 0
        ) OR (
            status_code <> 'CANCELLED'
            AND cancelled_dttm IS NULL
            AND cancel_reason IS NULL
        )
    ),
    CONSTRAINT ck_reservation_personal_data CHECK (
        (
            personal_data_erased_dttm IS NULL
            AND lookup_token_hash IS NOT NULL
            AND entry_token_hash IS NOT NULL
            AND applicant_name_ciphertext IS NOT NULL
            AND phone_ciphertext IS NOT NULL
            AND phone_search_hash IS NOT NULL
        ) OR (
            personal_data_erased_dttm IS NOT NULL
            AND lookup_token_hash IS NULL
            AND entry_token_hash IS NULL
            AND applicant_name_ciphertext IS NULL
            AND phone_ciphertext IS NULL
            AND phone_search_hash IS NULL
        )
    )
) ENGINE=InnoDB;

CREATE TABLE reservation_seat (
    reservation_seat_id        BIGINT        NOT NULL AUTO_INCREMENT,
    reservation_id             BIGINT        NOT NULL,
    performance_round_seat_id  BIGINT        NOT NULL,
    status_code                VARCHAR(30)   NOT NULL,
    cancelled_dttm             DATETIME(6)   NULL,
    cancel_reason              VARCHAR(500)  NULL,
    checked_in_dttm            DATETIME(6)   NULL,
    checked_in_by_member_id    BIGINT        NULL,
    created_dttm               DATETIME(6)   NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm               DATETIME(6)   NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_reservation_seat PRIMARY KEY (reservation_seat_id),
    CONSTRAINT uk_reservation_seat_reservation_round_seat UNIQUE (
        reservation_id, performance_round_seat_id
    ),
    CONSTRAINT uk_reservation_seat_id_round_seat UNIQUE (
        reservation_seat_id, performance_round_seat_id
    ),
    KEY idx_reservation_seat_round_seat (
        performance_round_seat_id
    ),
    KEY idx_reservation_seat_check_in (
        reservation_id, checked_in_dttm
    ),
    CONSTRAINT fk_reservation_seat_reservation FOREIGN KEY (
        reservation_id
    ) REFERENCES reservation (reservation_id),
    CONSTRAINT fk_reservation_seat_round_seat FOREIGN KEY (
        performance_round_seat_id
    ) REFERENCES performance_round_seat (performance_round_seat_id),
    CONSTRAINT fk_reservation_seat_checked_in_member FOREIGN KEY (
        checked_in_by_member_id
    ) REFERENCES member (member_id),
    CONSTRAINT ck_reservation_seat_status CHECK (
        status_code IN ('CONFIRMED', 'CANCELLED')
    ),
    CONSTRAINT ck_reservation_seat_cancellation CHECK (
        (
            status_code = 'CANCELLED'
            AND cancelled_dttm IS NOT NULL
            AND cancel_reason IS NOT NULL
            AND CHAR_LENGTH(TRIM(cancel_reason)) > 0
        ) OR (
            status_code = 'CONFIRMED'
            AND cancelled_dttm IS NULL
            AND cancel_reason IS NULL
        )
    ),
    CONSTRAINT ck_reservation_seat_check_in CHECK (
        (checked_in_dttm IS NULL AND checked_in_by_member_id IS NULL)
        OR (
            checked_in_dttm IS NOT NULL
            AND checked_in_by_member_id IS NOT NULL
            AND status_code = 'CONFIRMED'
        )
    )
) ENGINE=InnoDB;

CREATE TABLE active_seat_occupancy (
    performance_round_seat_id  BIGINT       NOT NULL,
    reservation_seat_id        BIGINT       NOT NULL,
    occupied_dttm              DATETIME(6)  NOT NULL,
    created_dttm               DATETIME(6)  NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm               DATETIME(6)  NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_active_seat_occupancy PRIMARY KEY (
        performance_round_seat_id
    ),
    CONSTRAINT uk_active_seat_occupancy_reservation_seat UNIQUE (
        reservation_seat_id
    ),
    CONSTRAINT fk_active_seat_occupancy_round_seat FOREIGN KEY (
        performance_round_seat_id
    ) REFERENCES performance_round_seat (performance_round_seat_id),
    CONSTRAINT fk_active_seat_occupancy_reservation_seat FOREIGN KEY (
        reservation_seat_id, performance_round_seat_id
    ) REFERENCES reservation_seat (
        reservation_seat_id, performance_round_seat_id
    )
) ENGINE=InnoDB;

CREATE TABLE reservation_status_history (
    reservation_status_history_id  BIGINT        NOT NULL AUTO_INCREMENT,
    reservation_id                 BIGINT        NOT NULL,
    previous_status_code           VARCHAR(30)   NULL,
    new_status_code                VARCHAR(30)   NOT NULL,
    reason                         VARCHAR(500)  NOT NULL,
    changed_by_member_id           BIGINT        NULL,
    changed_dttm                   DATETIME(6)   NOT NULL,
    created_dttm                   DATETIME(6)   NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm                   DATETIME(6)   NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_reservation_status_history PRIMARY KEY (
        reservation_status_history_id
    ),
    KEY idx_reservation_status_history_reservation_changed (
        reservation_id, changed_dttm
    ),
    CONSTRAINT fk_reservation_status_history_reservation FOREIGN KEY (
        reservation_id
    ) REFERENCES reservation (reservation_id),
    CONSTRAINT fk_reservation_status_history_member FOREIGN KEY (
        changed_by_member_id
    ) REFERENCES member (member_id),
    CONSTRAINT ck_reservation_status_history_previous CHECK (
        previous_status_code IS NULL
        OR previous_status_code IN (
            'CONFIRMED', 'PARTIALLY_CANCELLED', 'CANCELLED'
        )
    ),
    CONSTRAINT ck_reservation_status_history_new CHECK (
        new_status_code IN (
            'CONFIRMED', 'PARTIALLY_CANCELLED', 'CANCELLED'
        )
    ),
    CONSTRAINT ck_reservation_status_history_reason CHECK (
        CHAR_LENGTH(TRIM(reason)) > 0
    )
) ENGINE=InnoDB;

CREATE TABLE seat_entry_history (
    seat_entry_history_id    BIGINT        NOT NULL AUTO_INCREMENT,
    reservation_seat_id      BIGINT        NOT NULL,
    action_code              VARCHAR(30)   NOT NULL,
    processed_by_member_id   BIGINT        NOT NULL,
    processed_dttm           DATETIME(6)   NOT NULL,
    reason                   VARCHAR(500)  NULL,
    created_dttm             DATETIME(6)   NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm             DATETIME(6)   NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_seat_entry_history PRIMARY KEY (
        seat_entry_history_id
    ),
    KEY idx_seat_entry_history_seat_processed (
        reservation_seat_id, processed_dttm
    ),
    CONSTRAINT fk_seat_entry_history_reservation_seat FOREIGN KEY (
        reservation_seat_id
    ) REFERENCES reservation_seat (reservation_seat_id),
    CONSTRAINT fk_seat_entry_history_member FOREIGN KEY (
        processed_by_member_id
    ) REFERENCES member (member_id),
    CONSTRAINT ck_seat_entry_history_action CHECK (
        action_code IN ('CHECK_IN', 'CANCEL_CHECK_IN')
    ),
    CONSTRAINT ck_seat_entry_history_reason CHECK (
        (
            action_code = 'CHECK_IN' AND reason IS NULL
        ) OR (
            action_code = 'CANCEL_CHECK_IN'
            AND reason IS NOT NULL
            AND CHAR_LENGTH(TRIM(reason)) > 0
        )
    )
) ENGINE=InnoDB;
