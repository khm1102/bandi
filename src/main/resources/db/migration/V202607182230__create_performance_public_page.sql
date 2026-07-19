CREATE TABLE performance_public_page (
    performance_public_page_id  BIGINT        NOT NULL AUTO_INCREMENT,
    performance_project_id      BIGINT        NOT NULL,
    slug                        VARCHAR(150)  NOT NULL,
    status_code                 VARCHAR(30)   NOT NULL,
    short_description           TEXT          NOT NULL,
    synopsis                    LONGTEXT      NOT NULL,
    director_note               TEXT          NULL,
    genre                       VARCHAR(100)  NOT NULL,
    age_rating                  VARCHAR(100)  NOT NULL,
    runtime_minutes             INT           NOT NULL,
    intermission_minutes        INT           NULL,
    admission_fee               BIGINT        NOT NULL,
    hero_file_id                BIGINT        NULL,
    poster_file_id              BIGINT        NULL,
    accent_color                VARCHAR(20)   NULL,
    contact_name                VARCHAR(100)  NOT NULL,
    contact_channel             VARCHAR(500)  NOT NULL,
    organizer_name              VARCHAR(200)  NOT NULL,
    og_title                    VARCHAR(200)  NULL,
    og_description              VARCHAR(500)  NULL,
    og_image_file_id            BIGINT        NULL,
    publish_start_dttm          DATETIME(6)   NULL,
    publish_end_dttm            DATETIME(6)   NULL,
    created_dttm                DATETIME(6)   NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm                DATETIME(6)   NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_performance_public_page PRIMARY KEY (
        performance_public_page_id
    ),
    CONSTRAINT uk_performance_public_page_project UNIQUE (
        performance_project_id
    ),
    CONSTRAINT uk_performance_public_page_slug UNIQUE (slug),
    KEY idx_performance_public_page_publish (
        status_code, publish_start_dttm, publish_end_dttm
    ),
    KEY idx_performance_public_page_hero_file (hero_file_id),
    KEY idx_performance_public_page_poster_file (poster_file_id),
    KEY idx_performance_public_page_og_image_file (og_image_file_id),
    CONSTRAINT fk_performance_public_page_project FOREIGN KEY (
        performance_project_id
    ) REFERENCES performance_project (performance_project_id),
    CONSTRAINT fk_performance_public_page_hero_file FOREIGN KEY (
        hero_file_id
    ) REFERENCES stored_file (stored_file_id),
    CONSTRAINT fk_performance_public_page_poster_file FOREIGN KEY (
        poster_file_id
    ) REFERENCES stored_file (stored_file_id),
    CONSTRAINT fk_performance_public_page_og_image_file FOREIGN KEY (
        og_image_file_id
    ) REFERENCES stored_file (stored_file_id),
    CONSTRAINT ck_performance_public_page_slug CHECK (
        CHAR_LENGTH(slug) BETWEEN 3 AND 150
        AND slug REGEXP '^[a-z0-9]+(-[a-z0-9]+)*$'
    ),
    CONSTRAINT ck_performance_public_page_status CHECK (
        status_code IN (
            'DRAFT', 'SCHEDULED', 'PUBLISHED',
            'ENDED', 'CANCELLED', 'ARCHIVED'
        )
    ),
    CONSTRAINT ck_performance_public_page_description CHECK (
        CHAR_LENGTH(TRIM(short_description)) > 0
        AND CHAR_LENGTH(TRIM(synopsis)) > 0
    ),
    CONSTRAINT ck_performance_public_page_facts CHECK (
        CHAR_LENGTH(TRIM(genre)) > 0
        AND CHAR_LENGTH(TRIM(age_rating)) > 0
        AND runtime_minutes > 0
        AND (intermission_minutes IS NULL OR intermission_minutes >= 0)
        AND admission_fee >= 0
    ),
    CONSTRAINT ck_performance_public_page_contact CHECK (
        CHAR_LENGTH(TRIM(contact_name)) > 0
        AND CHAR_LENGTH(TRIM(contact_channel)) > 0
        AND CHAR_LENGTH(TRIM(organizer_name)) > 0
    ),
    CONSTRAINT ck_performance_public_page_accent CHECK (
        accent_color IS NULL
        OR accent_color REGEXP '^#[0-9A-Fa-f]{6}$'
    ),
    CONSTRAINT ck_performance_public_page_window CHECK (
        publish_end_dttm IS NULL
        OR (publish_start_dttm IS NOT NULL
            AND publish_end_dttm > publish_start_dttm)
    ),
    CONSTRAINT ck_performance_public_page_schedule CHECK (
        status_code <> 'SCHEDULED' OR publish_start_dttm IS NOT NULL
    )
) ENGINE=InnoDB;

CREATE TABLE performance_viewing_guide (
    performance_viewing_guide_id  BIGINT    NOT NULL AUTO_INCREMENT,
    performance_project_id        BIGINT    NOT NULL,
    entry_policy                  TEXT      NOT NULL,
    late_entry_policy             TEXT      NOT NULL,
    recording_policy              TEXT      NOT NULL,
    cancellation_policy           TEXT      NOT NULL,
    accessibility_policy          TEXT      NOT NULL,
    directions                    TEXT      NULL,
    parking_information           TEXT      NULL,
    created_dttm                  DATETIME(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm                  DATETIME(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_performance_viewing_guide PRIMARY KEY (
        performance_viewing_guide_id
    ),
    CONSTRAINT uk_performance_viewing_guide_project UNIQUE (
        performance_project_id
    ),
    CONSTRAINT fk_performance_viewing_guide_project FOREIGN KEY (
        performance_project_id
    ) REFERENCES performance_project (performance_project_id),
    CONSTRAINT ck_performance_viewing_guide_required CHECK (
        CHAR_LENGTH(TRIM(entry_policy)) > 0
        AND CHAR_LENGTH(TRIM(late_entry_policy)) > 0
        AND CHAR_LENGTH(TRIM(recording_policy)) > 0
        AND CHAR_LENGTH(TRIM(cancellation_policy)) > 0
        AND CHAR_LENGTH(TRIM(accessibility_policy)) > 0
    )
) ENGINE=InnoDB;
