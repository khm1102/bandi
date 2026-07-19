CREATE TABLE club_event (
    club_event_id           BIGINT       NOT NULL AUTO_INCREMENT,
    calendar_event_id       BIGINT       NULL,
    target_scope_code       VARCHAR(20)  NOT NULL,
    team_id                 BIGINT       NULL,
    title                   VARCHAR(150) NOT NULL,
    description             TEXT         NULL,
    place                   VARCHAR(150) NOT NULL,
    start_dttm              DATETIME(6)  NOT NULL,
    end_dttm                DATETIME(6)  NOT NULL,
    check_in_start_dttm     DATETIME(6)  NOT NULL,
    check_in_end_dttm       DATETIME(6)  NOT NULL,
    status_code             VARCHAR(30)  NOT NULL,
    created_by_member_id    BIGINT       NOT NULL,
    updated_by_member_id    BIGINT       NOT NULL,
    created_dttm            DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm            DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_dttm            DATETIME(6)  NULL,
    CONSTRAINT pk_club_event PRIMARY KEY (club_event_id),
    CONSTRAINT uk_club_event_calendar_event UNIQUE (calendar_event_id),
    KEY idx_club_event_status_start (status_code, start_dttm),
    KEY idx_club_event_team_status_start (team_id, status_code, start_dttm),
    KEY idx_club_event_created_by (created_by_member_id),
    KEY idx_club_event_updated_by (updated_by_member_id),
    CONSTRAINT fk_club_event_calendar_event FOREIGN KEY (calendar_event_id)
        REFERENCES calendar_event (calendar_event_id),
    CONSTRAINT fk_club_event_team FOREIGN KEY (team_id) REFERENCES team (team_id),
    CONSTRAINT fk_club_event_created_by_member FOREIGN KEY (created_by_member_id)
        REFERENCES member (member_id),
    CONSTRAINT fk_club_event_updated_by_member FOREIGN KEY (updated_by_member_id)
        REFERENCES member (member_id),
    CONSTRAINT ck_club_event_target_scope CHECK (
        (target_scope_code = 'TEAM' AND team_id IS NOT NULL)
        OR (target_scope_code IN ('ALL', 'SELECTED') AND team_id IS NULL)
    ),
    CONSTRAINT ck_club_event_time CHECK (end_dttm > start_dttm),
    CONSTRAINT ck_club_event_check_in_time CHECK (
        check_in_end_dttm > check_in_start_dttm
    ),
    CONSTRAINT ck_club_event_status_code CHECK (
        status_code IN ('DRAFT', 'SCHEDULED', 'IN_PROGRESS', 'CLOSED', 'ARCHIVED')
    )
) ENGINE=InnoDB;

CREATE TABLE event_attendance (
    event_attendance_id     BIGINT       NOT NULL AUTO_INCREMENT,
    club_event_id           BIGINT       NOT NULL,
    member_id               BIGINT       NOT NULL,
    status_code             VARCHAR(20)  NOT NULL,
    processed_by_member_id  BIGINT       NULL,
    processed_dttm          DATETIME(6)  NULL,
    reason                  VARCHAR(500) NULL,
    created_dttm            DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm            DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_event_attendance PRIMARY KEY (event_attendance_id),
    CONSTRAINT uk_event_attendance_event_member UNIQUE (club_event_id, member_id),
    KEY idx_event_attendance_event_status (club_event_id, status_code),
    KEY idx_event_attendance_member_status (member_id, status_code),
    KEY idx_event_attendance_processed_by (processed_by_member_id),
    CONSTRAINT fk_event_attendance_club_event FOREIGN KEY (club_event_id)
        REFERENCES club_event (club_event_id),
    CONSTRAINT fk_event_attendance_member FOREIGN KEY (member_id)
        REFERENCES member (member_id),
    CONSTRAINT fk_event_attendance_processed_by_member FOREIGN KEY (processed_by_member_id)
        REFERENCES member (member_id),
    CONSTRAINT ck_event_attendance_status_code CHECK (
        status_code IN ('PENDING', 'PRESENT', 'LATE', 'ABSENT', 'EXCUSED')
    ),
    CONSTRAINT ck_event_attendance_processing CHECK (
        (status_code = 'PENDING' AND processed_by_member_id IS NULL
            AND processed_dttm IS NULL AND reason IS NULL)
        OR (status_code <> 'PENDING' AND processed_by_member_id IS NOT NULL
            AND processed_dttm IS NOT NULL)
    ),
    CONSTRAINT ck_event_attendance_excused_reason CHECK (
        status_code <> 'EXCUSED' OR (reason IS NOT NULL AND CHAR_LENGTH(TRIM(reason)) > 0)
    )
) ENGINE=InnoDB;

CREATE TABLE event_attendance_history (
    event_attendance_history_id BIGINT       NOT NULL AUTO_INCREMENT,
    event_attendance_id         BIGINT       NOT NULL,
    previous_status_code        VARCHAR(20)  NOT NULL,
    new_status_code             VARCHAR(20)  NOT NULL,
    reason                      VARCHAR(500) NULL,
    changed_by_member_id        BIGINT       NOT NULL,
    changed_dttm                DATETIME(6)  NOT NULL,
    created_dttm                DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm                DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_event_attendance_history PRIMARY KEY (event_attendance_history_id),
    KEY idx_event_attendance_history_attendance_dttm (event_attendance_id, changed_dttm),
    KEY idx_event_attendance_history_changed_by (changed_by_member_id),
    CONSTRAINT fk_event_attendance_history_attendance FOREIGN KEY (event_attendance_id)
        REFERENCES event_attendance (event_attendance_id),
    CONSTRAINT fk_event_attendance_history_changed_by_member FOREIGN KEY (changed_by_member_id)
        REFERENCES member (member_id),
    CONSTRAINT ck_event_attendance_history_previous CHECK (
        previous_status_code IN ('PENDING', 'PRESENT', 'LATE', 'ABSENT', 'EXCUSED')
    ),
    CONSTRAINT ck_event_attendance_history_new CHECK (
        new_status_code IN ('PRESENT', 'LATE', 'ABSENT', 'EXCUSED')
    ),
    CONSTRAINT ck_event_attendance_history_change CHECK (
        previous_status_code <> new_status_code
    ),
    CONSTRAINT ck_event_attendance_history_excused_reason CHECK (
        new_status_code <> 'EXCUSED'
        OR (reason IS NOT NULL AND CHAR_LENGTH(TRIM(reason)) > 0)
    )
) ENGINE=InnoDB;
