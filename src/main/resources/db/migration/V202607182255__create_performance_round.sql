CREATE TABLE performance_round (
    performance_round_id      BIGINT       NOT NULL AUTO_INCREMENT,
    performance_project_id    BIGINT       NOT NULL,
    round_no                  INT          NOT NULL,
    start_dttm                DATETIME(6)  NOT NULL,
    entry_start_dttm          DATETIME(6)  NOT NULL,
    reservation_open_dttm     DATETIME(6)  NOT NULL,
    reservation_close_dttm    DATETIME(6)  NOT NULL,
    status_code               VARCHAR(30)  NOT NULL,
    created_dttm              DATETIME(6)  NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm              DATETIME(6)  NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_performance_round PRIMARY KEY (performance_round_id),
    CONSTRAINT uk_performance_round_project_no UNIQUE (
        performance_project_id, round_no
    ),
    CONSTRAINT uk_performance_round_id_project UNIQUE (
        performance_round_id, performance_project_id
    ),
    KEY idx_performance_round_project_start (
        performance_project_id, start_dttm
    ),
    CONSTRAINT fk_performance_round_project FOREIGN KEY (
        performance_project_id
    ) REFERENCES performance_project (performance_project_id),
    CONSTRAINT ck_performance_round_no CHECK (round_no > 0),
    CONSTRAINT ck_performance_round_status CHECK (
        status_code IN (
            'SCHEDULED', 'RESERVATION_OPEN', 'RESERVATION_CLOSED',
            'ENTRY_OPEN', 'ENDED', 'CANCELLED'
        )
    ),
    CONSTRAINT ck_performance_round_times CHECK (
        reservation_open_dttm < reservation_close_dttm
        AND reservation_close_dttm <= start_dttm
        AND entry_start_dttm <= start_dttm
    )
) ENGINE=InnoDB;

CREATE TABLE performance_round_accessibility (
    performance_round_accessibility_id  BIGINT        NOT NULL
        AUTO_INCREMENT,
    performance_round_id                BIGINT        NOT NULL,
    support_type_code                   VARCHAR(30)   NOT NULL,
    title                               VARCHAR(100)  NOT NULL,
    description                         TEXT          NULL,
    display_order                       INT           NOT NULL,
    created_dttm                        DATETIME(6)   NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm                        DATETIME(6)   NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_performance_round_accessibility PRIMARY KEY (
        performance_round_accessibility_id
    ),
    CONSTRAINT uk_performance_round_accessibility_round_type UNIQUE (
        performance_round_id, support_type_code
    ),
    KEY idx_performance_round_accessibility_round_order (
        performance_round_id, display_order,
        performance_round_accessibility_id
    ),
    CONSTRAINT fk_performance_round_accessibility_round FOREIGN KEY (
        performance_round_id
    ) REFERENCES performance_round (performance_round_id),
    CONSTRAINT ck_performance_round_accessibility_type CHECK (
        support_type_code IN (
            'CAPTION', 'SIGN_LANGUAGE', 'AUDIO_DESCRIPTION', 'OTHER'
        )
    ),
    CONSTRAINT ck_performance_round_accessibility_title CHECK (
        CHAR_LENGTH(TRIM(title)) > 0
    ),
    CONSTRAINT ck_performance_round_accessibility_order CHECK (
        display_order >= 0
    )
) ENGINE=InnoDB;

ALTER TABLE performance_cast_history
    DROP INDEX idx_performance_cast_history_round,
    ADD KEY idx_performance_cast_history_round_project (
        performance_round_id, performance_project_id
    ),
    ADD CONSTRAINT fk_performance_cast_history_round_project FOREIGN KEY (
        performance_round_id, performance_project_id
    ) REFERENCES performance_round (
        performance_round_id, performance_project_id
    );
