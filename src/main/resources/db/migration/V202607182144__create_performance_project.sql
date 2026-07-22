CREATE TABLE performance_project (
    performance_project_id  BIGINT       NOT NULL AUTO_INCREMENT,
    academic_year           SMALLINT     NOT NULL,
    term_code               VARCHAR(20)  NOT NULL,
    title                   VARCHAR(200) NOT NULL,
    production_start_date   DATE         NOT NULL,
    production_end_date     DATE         NOT NULL,
    place                   VARCHAR(200) NOT NULL,
    status_code             VARCHAR(30)  NOT NULL,
    created_by_member_id    BIGINT       NOT NULL,
    updated_by_member_id    BIGINT       NOT NULL,
    created_dttm            DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm            DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_dttm            DATETIME(6)  NULL,
    term_occupancy_key      VARCHAR(40) GENERATED ALWAYS AS (
        CASE
            WHEN deleted_dttm IS NULL AND status_code <> 'CANCELLED'
                THEN CONCAT(CAST(academic_year AS CHAR), ':', term_code)
            ELSE NULL
        END
    ) STORED,
    CONSTRAINT pk_performance_project PRIMARY KEY (performance_project_id),
    CONSTRAINT uk_performance_project_term_occupancy UNIQUE (term_occupancy_key),
    KEY idx_performance_project_term_status (
        academic_year, term_code, status_code, deleted_dttm
    ),
    KEY idx_performance_project_created_by (created_by_member_id),
    KEY idx_performance_project_updated_by (updated_by_member_id),
    CONSTRAINT fk_performance_project_created_by_member FOREIGN KEY (created_by_member_id)
        REFERENCES member (member_id),
    CONSTRAINT fk_performance_project_updated_by_member FOREIGN KEY (updated_by_member_id)
        REFERENCES member (member_id),
    CONSTRAINT ck_performance_project_academic_year CHECK (academic_year > 0),
    CONSTRAINT ck_performance_project_term_code CHECK (
        CHAR_LENGTH(TRIM(term_code)) > 0
    ),
    CONSTRAINT ck_performance_project_title CHECK (
        CHAR_LENGTH(TRIM(title)) > 0
    ),
    CONSTRAINT ck_performance_project_place CHECK (
        CHAR_LENGTH(TRIM(place)) > 0
    ),
    CONSTRAINT ck_performance_project_dates CHECK (
        production_end_date >= production_start_date
    ),
    CONSTRAINT ck_performance_project_status_code CHECK (
        status_code IN (
            'PLANNING', 'PRODUCING', 'RESERVATION_OPEN', 'PERFORMING',
            'ENDED', 'CANCELLED', 'ARCHIVED'
        )
    )
) ENGINE=InnoDB;
