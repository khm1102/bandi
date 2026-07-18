CREATE TABLE performance_round_cast (
    performance_round_cast_id  BIGINT       NOT NULL AUTO_INCREMENT,
    performance_project_id     BIGINT       NOT NULL,
    performance_round_id       BIGINT       NOT NULL,
    performance_character_id   BIGINT       NOT NULL,
    public_profile_id          BIGINT       NOT NULL,
    cast_type_code             VARCHAR(30)  NOT NULL,
    created_dttm               DATETIME(6)  NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm               DATETIME(6)  NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_performance_round_cast PRIMARY KEY (
        performance_round_cast_id
    ),
    CONSTRAINT uk_performance_round_cast_round_character UNIQUE (
        performance_round_id, performance_character_id
    ),
    KEY idx_performance_round_cast_project_round (
        performance_project_id, performance_round_id
    ),
    KEY idx_performance_round_cast_character_project (
        performance_character_id, performance_project_id
    ),
    KEY idx_performance_round_cast_profile (public_profile_id),
    CONSTRAINT fk_performance_round_cast_round_project FOREIGN KEY (
        performance_round_id, performance_project_id
    ) REFERENCES performance_round (
        performance_round_id, performance_project_id
    ),
    CONSTRAINT fk_performance_round_cast_character_project FOREIGN KEY (
        performance_character_id, performance_project_id
    ) REFERENCES performance_character (
        performance_character_id, performance_project_id
    ),
    CONSTRAINT fk_performance_round_cast_profile FOREIGN KEY (
        public_profile_id
    ) REFERENCES public_profile (public_profile_id),
    CONSTRAINT ck_performance_round_cast_type CHECK (
        cast_type_code IN ('PRIMARY', 'ALTERNATE', 'UNDERSTUDY')
    )
) ENGINE=InnoDB;
