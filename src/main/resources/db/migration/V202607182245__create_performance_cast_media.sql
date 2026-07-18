CREATE TABLE performance_character (
    performance_character_id  BIGINT        NOT NULL AUTO_INCREMENT,
    performance_project_id    BIGINT        NOT NULL,
    name                      VARCHAR(100)  NOT NULL,
    description               TEXT          NULL,
    importance_code           VARCHAR(30)   NOT NULL,
    display_order             INT           NOT NULL,
    CONSTRAINT pk_performance_character PRIMARY KEY (
        performance_character_id
    ),
    CONSTRAINT uk_performance_character_id_project UNIQUE (
        performance_character_id, performance_project_id
    ),
    KEY idx_performance_character_project_order (
        performance_project_id, importance_code,
        display_order, performance_character_id
    ),
    CONSTRAINT fk_performance_character_project FOREIGN KEY (
        performance_project_id
    ) REFERENCES performance_project (performance_project_id),
    CONSTRAINT ck_performance_character_name CHECK (
        CHAR_LENGTH(TRIM(name)) > 0
    ),
    CONSTRAINT ck_performance_character_importance CHECK (
        importance_code IN ('LEAD', 'SUPPORT', 'ENSEMBLE')
    ),
    CONSTRAINT ck_performance_character_display_order CHECK (
        display_order >= 0
    )
) ENGINE=InnoDB;

CREATE TABLE performance_cast (
    performance_cast_id       BIGINT      NOT NULL AUTO_INCREMENT,
    performance_project_id    BIGINT      NOT NULL,
    performance_character_id  BIGINT      NOT NULL,
    public_profile_id         BIGINT      NOT NULL,
    cast_type_code            VARCHAR(30) NOT NULL,
    display_order             INT         NOT NULL,
    CONSTRAINT pk_performance_cast PRIMARY KEY (performance_cast_id),
    CONSTRAINT uk_performance_cast_project_character_profile UNIQUE (
        performance_project_id, performance_character_id,
        public_profile_id
    ),
    KEY idx_performance_cast_project_order (
        performance_project_id, performance_character_id,
        display_order, performance_cast_id
    ),
    KEY idx_performance_cast_profile (public_profile_id),
    CONSTRAINT fk_performance_cast_character_project FOREIGN KEY (
        performance_character_id, performance_project_id
    ) REFERENCES performance_character (
        performance_character_id, performance_project_id
    ),
    CONSTRAINT fk_performance_cast_profile FOREIGN KEY (
        public_profile_id
    ) REFERENCES public_profile (public_profile_id),
    CONSTRAINT ck_performance_cast_type CHECK (
        cast_type_code IN ('PRIMARY', 'ALTERNATE', 'UNDERSTUDY')
    ),
    CONSTRAINT ck_performance_cast_display_order CHECK (
        display_order >= 0
    )
) ENGINE=InnoDB;

CREATE TABLE performance_cast_history (
    performance_cast_history_id  BIGINT        NOT NULL AUTO_INCREMENT,
    performance_project_id       BIGINT        NOT NULL,
    performance_round_id         BIGINT        NULL,
    performance_character_id     BIGINT        NOT NULL,
    previous_public_profile_id   BIGINT        NULL,
    new_public_profile_id        BIGINT        NULL,
    previous_cast_type_code      VARCHAR(30)   NULL,
    new_cast_type_code           VARCHAR(30)   NULL,
    scope_code                   VARCHAR(30)   NOT NULL,
    action_code                  VARCHAR(30)   NOT NULL,
    reason                       VARCHAR(500)  NULL,
    changed_by_member_id         BIGINT        NOT NULL,
    changed_dttm                 DATETIME(6)   NOT NULL,
    CONSTRAINT pk_performance_cast_history PRIMARY KEY (
        performance_cast_history_id
    ),
    KEY idx_performance_cast_history_project (
        performance_project_id, changed_dttm,
        performance_cast_history_id
    ),
    KEY idx_performance_cast_history_round (performance_round_id),
    KEY idx_performance_cast_history_character (
        performance_character_id
    ),
    KEY idx_performance_cast_history_previous_profile (
        previous_public_profile_id
    ),
    KEY idx_performance_cast_history_new_profile (
        new_public_profile_id
    ),
    KEY idx_performance_cast_history_changed_by (
        changed_by_member_id
    ),
    CONSTRAINT fk_performance_cast_history_project FOREIGN KEY (
        performance_project_id
    ) REFERENCES performance_project (performance_project_id),
    CONSTRAINT fk_performance_cast_history_character FOREIGN KEY (
        performance_character_id
    ) REFERENCES performance_character (performance_character_id),
    CONSTRAINT fk_performance_cast_history_previous_profile FOREIGN KEY (
        previous_public_profile_id
    ) REFERENCES public_profile (public_profile_id),
    CONSTRAINT fk_performance_cast_history_new_profile FOREIGN KEY (
        new_public_profile_id
    ) REFERENCES public_profile (public_profile_id),
    CONSTRAINT fk_performance_cast_history_changed_by_member FOREIGN KEY (
        changed_by_member_id
    ) REFERENCES member (member_id),
    CONSTRAINT ck_performance_cast_history_types CHECK (
        (previous_cast_type_code IS NULL
            OR previous_cast_type_code IN (
                'PRIMARY', 'ALTERNATE', 'UNDERSTUDY'
            ))
        AND (new_cast_type_code IS NULL
            OR new_cast_type_code IN (
                'PRIMARY', 'ALTERNATE', 'UNDERSTUDY'
            ))
    ),
    CONSTRAINT ck_performance_cast_history_scope CHECK (
        (scope_code = 'PROJECT' AND performance_round_id IS NULL)
        OR (scope_code = 'ROUND' AND performance_round_id IS NOT NULL)
    ),
    CONSTRAINT ck_performance_cast_history_action CHECK (
        (action_code = 'ASSIGN'
            AND previous_public_profile_id IS NULL
            AND new_public_profile_id IS NOT NULL
            AND previous_cast_type_code IS NULL
            AND new_cast_type_code IS NOT NULL)
        OR (action_code = 'CHANGE'
            AND previous_public_profile_id IS NOT NULL
            AND new_public_profile_id IS NOT NULL
            AND previous_cast_type_code IS NOT NULL
            AND new_cast_type_code IS NOT NULL)
        OR (action_code = 'REMOVE'
            AND previous_public_profile_id IS NOT NULL
            AND new_public_profile_id IS NULL
            AND previous_cast_type_code IS NOT NULL
            AND new_cast_type_code IS NULL)
    )
) ENGINE=InnoDB;

CREATE TABLE production_credit (
    production_credit_id    BIGINT        NOT NULL AUTO_INCREMENT,
    performance_project_id  BIGINT        NOT NULL,
    credit_role             VARCHAR(100)  NOT NULL,
    public_name             VARCHAR(100)  NOT NULL,
    public_profile_id       BIGINT        NULL,
    display_order           INT           NOT NULL,
    CONSTRAINT pk_production_credit PRIMARY KEY (production_credit_id),
    KEY idx_production_credit_project_order (
        performance_project_id, display_order, production_credit_id
    ),
    KEY idx_production_credit_profile (public_profile_id),
    CONSTRAINT fk_production_credit_project FOREIGN KEY (
        performance_project_id
    ) REFERENCES performance_project (performance_project_id),
    CONSTRAINT fk_production_credit_profile FOREIGN KEY (
        public_profile_id
    ) REFERENCES public_profile (public_profile_id),
    CONSTRAINT ck_production_credit_text CHECK (
        CHAR_LENGTH(TRIM(credit_role)) > 0
        AND CHAR_LENGTH(TRIM(public_name)) > 0
    ),
    CONSTRAINT ck_production_credit_display_order CHECK (
        display_order >= 0
    )
) ENGINE=InnoDB;

CREATE TABLE performance_media (
    performance_media_id    BIGINT         NOT NULL AUTO_INCREMENT,
    performance_project_id  BIGINT         NOT NULL,
    stored_file_id          BIGINT         NOT NULL,
    media_type_code         VARCHAR(30)    NOT NULL,
    title                   VARCHAR(200)   NOT NULL,
    description             TEXT           NOT NULL,
    alt_text                VARCHAR(500)   NOT NULL,
    credit_text             VARCHAR(500)   NOT NULL,
    external_url            VARCHAR(1000)  NULL,
    display_order           INT            NOT NULL,
    is_published            TINYINT(1)     NOT NULL DEFAULT 0,
    CONSTRAINT pk_performance_media PRIMARY KEY (performance_media_id),
    KEY idx_performance_media_project_publish_order (
        performance_project_id, is_published,
        display_order, performance_media_id
    ),
    KEY idx_performance_media_file (stored_file_id),
    CONSTRAINT fk_performance_media_project FOREIGN KEY (
        performance_project_id
    ) REFERENCES performance_project (performance_project_id),
    CONSTRAINT fk_performance_media_file FOREIGN KEY (stored_file_id)
        REFERENCES stored_file (stored_file_id),
    CONSTRAINT ck_performance_media_type CHECK (
        media_type_code IN (
            'POSTER', 'PROFILE', 'REHEARSAL',
            'BEHIND', 'STAGE', 'VIDEO'
        )
    ),
    CONSTRAINT ck_performance_media_text CHECK (
        CHAR_LENGTH(TRIM(title)) > 0
        AND CHAR_LENGTH(TRIM(description)) > 0
        AND CHAR_LENGTH(TRIM(alt_text)) > 0
        AND CHAR_LENGTH(TRIM(credit_text)) > 0
    ),
    CONSTRAINT ck_performance_media_external_url CHECK (
        external_url IS NULL OR external_url REGEXP '^https?://'
    ),
    CONSTRAINT ck_performance_media_display_order CHECK (
        display_order >= 0
    ),
    CONSTRAINT ck_performance_media_published CHECK (
        is_published IN (0, 1)
    )
) ENGINE=InnoDB;
