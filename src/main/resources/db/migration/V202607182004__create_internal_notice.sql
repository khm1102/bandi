-- 내부 공지는 활성 멤버에게만 제공하며 조회수 대신 멤버별 최초·최근 읽음 시각을 기록한다.
CREATE TABLE internal_notice (
    internal_notice_id     BIGINT       NOT NULL AUTO_INCREMENT,
    target_scope_code      VARCHAR(20)  NOT NULL,
    team_id                BIGINT       NULL,
    title                  VARCHAR(200) NOT NULL,
    body                   LONGTEXT     NOT NULL,
    status_code            VARCHAR(20)  NOT NULL,
    is_important           TINYINT(1)   NOT NULL DEFAULT 0,
    publish_start_dttm     DATETIME(6)  NULL,
    publish_end_dttm       DATETIME(6)  NULL,
    created_by_member_id   BIGINT       NOT NULL,
    updated_by_member_id   BIGINT       NOT NULL,
    published_by_member_id BIGINT       NULL,
    created_dttm           DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm           DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_dttm           DATETIME(6)  NULL,
    CONSTRAINT pk_internal_notice PRIMARY KEY (internal_notice_id),
    KEY idx_internal_notice_scope_publish
        (target_scope_code, team_id, status_code, publish_start_dttm),
    KEY idx_internal_notice_visible_order
        (status_code, is_important, publish_start_dttm, internal_notice_id),
    KEY idx_internal_notice_created_by (created_by_member_id),
    KEY idx_internal_notice_updated_by (updated_by_member_id),
    KEY idx_internal_notice_published_by (published_by_member_id),
    CONSTRAINT fk_internal_notice_team FOREIGN KEY (team_id) REFERENCES team (team_id),
    CONSTRAINT fk_internal_notice_created_by_member
        FOREIGN KEY (created_by_member_id) REFERENCES member (member_id),
    CONSTRAINT fk_internal_notice_updated_by_member
        FOREIGN KEY (updated_by_member_id) REFERENCES member (member_id),
    CONSTRAINT fk_internal_notice_published_by_member
        FOREIGN KEY (published_by_member_id) REFERENCES member (member_id),
    CONSTRAINT ck_internal_notice_target_scope_code
        CHECK (target_scope_code IN ('ALL', 'TEAM')),
    CONSTRAINT ck_internal_notice_target_scope_team CHECK (
        (target_scope_code = 'ALL' AND team_id IS NULL)
        OR (target_scope_code = 'TEAM' AND team_id IS NOT NULL)
    ),
    CONSTRAINT ck_internal_notice_status_code CHECK (
        status_code IN ('DRAFT', 'SCHEDULED', 'PUBLISHED', 'CLOSED', 'ARCHIVED')
    ),
    CONSTRAINT ck_internal_notice_publish_period CHECK (
        publish_start_dttm IS NULL OR publish_end_dttm IS NULL
        OR publish_start_dttm <= publish_end_dttm
    ),
    CONSTRAINT ck_internal_notice_publish_state CHECK (
        (status_code IN ('DRAFT', 'ARCHIVED'))
        OR (publish_start_dttm IS NOT NULL AND published_by_member_id IS NOT NULL)
    )
) ENGINE=InnoDB;

CREATE TABLE internal_notice_read (
    internal_notice_read_id BIGINT      NOT NULL AUTO_INCREMENT,
    internal_notice_id      BIGINT      NOT NULL,
    member_id               BIGINT      NOT NULL,
    first_read_dttm         DATETIME(6) NOT NULL,
    last_read_dttm          DATETIME(6) NOT NULL,
    created_dttm            DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm            DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_internal_notice_read PRIMARY KEY (internal_notice_read_id),
    CONSTRAINT uk_internal_notice_read_notice_member
        UNIQUE (internal_notice_id, member_id),
    KEY idx_internal_notice_read_member_last (member_id, last_read_dttm),
    CONSTRAINT fk_internal_notice_read_notice
        FOREIGN KEY (internal_notice_id) REFERENCES internal_notice (internal_notice_id),
    CONSTRAINT fk_internal_notice_read_member
        FOREIGN KEY (member_id) REFERENCES member (member_id),
    CONSTRAINT ck_internal_notice_read_period
        CHECK (first_read_dttm <= last_read_dttm)
) ENGINE=InnoDB;

CREATE TABLE internal_notice_attachment (
    internal_notice_attachment_id BIGINT      NOT NULL AUTO_INCREMENT,
    internal_notice_id            BIGINT      NOT NULL,
    stored_file_id                BIGINT      NOT NULL,
    display_order                 INT         NOT NULL,
    created_dttm                  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm                  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_internal_notice_attachment
        PRIMARY KEY (internal_notice_attachment_id),
    CONSTRAINT uk_internal_notice_attachment_notice_file
        UNIQUE (internal_notice_id, stored_file_id),
    CONSTRAINT uk_internal_notice_attachment_notice_order
        UNIQUE (internal_notice_id, display_order),
    KEY idx_internal_notice_attachment_file (stored_file_id),
    CONSTRAINT fk_internal_notice_attachment_notice
        FOREIGN KEY (internal_notice_id) REFERENCES internal_notice (internal_notice_id),
    CONSTRAINT fk_internal_notice_attachment_file
        FOREIGN KEY (stored_file_id) REFERENCES stored_file (stored_file_id),
    CONSTRAINT ck_internal_notice_attachment_display_order CHECK (display_order >= 0)
) ENGINE=InnoDB;
