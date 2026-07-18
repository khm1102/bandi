-- 통합 캘린더 일정 (docs/database-schema.md 8장).
-- team_id가 NULL이면 동아리 전체 일정이며 반복 일정·알림·일정 조율 참조는 두지 않는다.
CREATE TABLE calendar_event (
    calendar_event_id    BIGINT       NOT NULL AUTO_INCREMENT,
    team_id              BIGINT       NULL,
    title                VARCHAR(150) NOT NULL,
    description          TEXT         NULL,
    start_dttm           DATETIME(6)  NOT NULL,
    end_dttm             DATETIME(6)  NOT NULL,
    is_all_day           TINYINT(1)   NOT NULL DEFAULT 0,
    place                VARCHAR(200) NULL,
    created_by_member_id BIGINT       NOT NULL,
    updated_by_member_id BIGINT       NOT NULL,
    created_dttm         DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm         DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_dttm         DATETIME(6)  NULL,
    CONSTRAINT pk_calendar_event PRIMARY KEY (calendar_event_id),
    KEY idx_calendar_event_period (start_dttm, end_dttm),
    KEY idx_calendar_event_team_start (team_id, start_dttm),
    KEY idx_calendar_event_created_by (created_by_member_id),
    KEY idx_calendar_event_updated_by (updated_by_member_id),
    CONSTRAINT fk_calendar_event_team FOREIGN KEY (team_id) REFERENCES team (team_id),
    CONSTRAINT fk_calendar_event_created_by_member
        FOREIGN KEY (created_by_member_id) REFERENCES member (member_id),
    CONSTRAINT fk_calendar_event_updated_by_member
        FOREIGN KEY (updated_by_member_id) REFERENCES member (member_id),
    CONSTRAINT ck_calendar_event_period CHECK (end_dttm >= start_dttm),
    CONSTRAINT ck_calendar_event_is_all_day CHECK (is_all_day IN (0, 1))
) ENGINE=InnoDB;
