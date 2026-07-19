-- 자료실은 내부 공지와 데이터·권한을 분리하고 MinIO 파일의 revision 연결만 보존한다.
CREATE TABLE resource (
    resource_id          BIGINT       NOT NULL AUTO_INCREMENT,
    target_scope_code    VARCHAR(20)  NOT NULL,
    team_id              BIGINT       NULL,
    category_code        VARCHAR(30)  NOT NULL,
    title                VARCHAR(200) NOT NULL,
    description          LONGTEXT     NOT NULL,
    status_code          VARCHAR(20)  NOT NULL,
    is_pinned            TINYINT(1)   NOT NULL DEFAULT 0,
    created_by_member_id BIGINT       NOT NULL,
    updated_by_member_id BIGINT       NOT NULL,
    created_dttm         DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm         DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_dttm         DATETIME(6)  NULL,
    CONSTRAINT pk_resource PRIMARY KEY (resource_id),
    KEY idx_resource_scope_status
        (target_scope_code, team_id, status_code, is_pinned, updated_dttm),
    KEY idx_resource_category_status (category_code, status_code, updated_dttm),
    KEY idx_resource_created_by (created_by_member_id),
    KEY idx_resource_updated_by (updated_by_member_id),
    CONSTRAINT fk_resource_team FOREIGN KEY (team_id) REFERENCES team (team_id),
    CONSTRAINT fk_resource_created_by_member
        FOREIGN KEY (created_by_member_id) REFERENCES member (member_id),
    CONSTRAINT fk_resource_updated_by_member
        FOREIGN KEY (updated_by_member_id) REFERENCES member (member_id),
    CONSTRAINT ck_resource_target_scope_code
        CHECK (target_scope_code IN ('ALL', 'TEAM')),
    CONSTRAINT ck_resource_target_scope_team CHECK (
        (target_scope_code = 'ALL' AND team_id IS NULL)
        OR (target_scope_code = 'TEAM' AND team_id IS NOT NULL)
    ),
    CONSTRAINT ck_resource_status_code
        CHECK (status_code IN ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
) ENGINE=InnoDB;

CREATE TABLE resource_file (
    resource_file_id     BIGINT      NOT NULL AUTO_INCREMENT,
    resource_id          BIGINT      NOT NULL,
    stored_file_id       BIGINT      NOT NULL,
    revision_no          INT         NOT NULL,
    display_order        INT         NOT NULL,
    uploaded_by_member_id BIGINT     NOT NULL,
    created_dttm         DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm         DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_resource_file PRIMARY KEY (resource_file_id),
    CONSTRAINT uk_resource_file_revision_file
        UNIQUE (resource_id, revision_no, stored_file_id),
    CONSTRAINT uk_resource_file_revision_order
        UNIQUE (resource_id, revision_no, display_order),
    KEY idx_resource_file_resource_revision (resource_id, revision_no),
    KEY idx_resource_file_stored_file (stored_file_id),
    KEY idx_resource_file_uploaded_by (uploaded_by_member_id),
    CONSTRAINT fk_resource_file_resource
        FOREIGN KEY (resource_id) REFERENCES resource (resource_id),
    CONSTRAINT fk_resource_file_stored_file
        FOREIGN KEY (stored_file_id) REFERENCES stored_file (stored_file_id),
    CONSTRAINT fk_resource_file_uploaded_by_member
        FOREIGN KEY (uploaded_by_member_id) REFERENCES member (member_id),
    CONSTRAINT ck_resource_file_revision_no CHECK (revision_no > 0),
    CONSTRAINT ck_resource_file_display_order CHECK (display_order >= 0)
) ENGINE=InnoDB;
