-- 자료실은 팀·분류·상태·리비전 없이 공용 Markdown 자료로 전환한다.
CREATE TEMPORARY TABLE retained_resource_file AS
SELECT rf.resource_file_id
FROM resource_file rf
JOIN (
    SELECT resource_id, MAX(revision_no) AS revision_no
    FROM resource_file
    GROUP BY resource_id
) latest ON latest.resource_id = rf.resource_id
       AND latest.revision_no = rf.revision_no
JOIN resource r ON r.resource_id = rf.resource_id
WHERE r.status_code = 'PUBLISHED'
  AND r.deleted_dttm IS NULL;

DELETE rf
FROM resource_file rf
LEFT JOIN retained_resource_file retained ON retained.resource_file_id = rf.resource_file_id
WHERE retained.resource_file_id IS NULL;

UPDATE resource
SET deleted_dttm = CURRENT_TIMESTAMP(6)
WHERE status_code IN ('DRAFT', 'ARCHIVED')
  AND deleted_dttm IS NULL;

ALTER TABLE resource_file
    DROP CHECK ck_resource_file_revision_no,
    DROP INDEX uk_resource_file_revision_file,
    DROP INDEX uk_resource_file_revision_order,
    DROP INDEX idx_resource_file_resource_revision,
    DROP COLUMN revision_no,
    ADD CONSTRAINT uk_resource_file_resource_file UNIQUE (resource_id, stored_file_id),
    ADD CONSTRAINT uk_resource_file_resource_order UNIQUE (resource_id, display_order),
    ADD KEY idx_resource_file_resource (resource_id);

ALTER TABLE resource
    DROP FOREIGN KEY fk_resource_team,
    DROP INDEX fk_resource_team,
    DROP INDEX idx_resource_scope_status,
    DROP INDEX idx_resource_category_status,
    DROP CHECK ck_resource_target_scope_code,
    DROP CHECK ck_resource_target_scope_team,
    DROP CHECK ck_resource_status_code,
    DROP COLUMN target_scope_code,
    DROP COLUMN team_id,
    DROP COLUMN category_code,
    DROP COLUMN status_code,
    DROP COLUMN is_pinned,
    CHANGE COLUMN description body_markdown LONGTEXT NOT NULL,
    ADD KEY idx_resource_visible_updated (deleted_dttm, updated_dttm, resource_id);

CREATE TABLE resource_link_preview (
    resource_link_preview_id BIGINT NOT NULL AUTO_INCREMENT,
    resource_id BIGINT NOT NULL,
    normalized_url VARCHAR(2048) NOT NULL,
    url_hash CHAR(64) NOT NULL,
    domain VARCHAR(255) NOT NULL,
    title VARCHAR(500) NULL,
    description VARCHAR(1000) NULL,
    preview_image_file_id BIGINT NULL,
    fetched_dttm DATETIME(6) NOT NULL,
    created_dttm DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_resource_link_preview PRIMARY KEY (resource_link_preview_id),
    CONSTRAINT uk_resource_link_preview_url UNIQUE (resource_id, url_hash),
    KEY idx_resource_link_preview_resource (resource_id),
    CONSTRAINT fk_resource_link_preview_resource
        FOREIGN KEY (resource_id) REFERENCES resource (resource_id),
    CONSTRAINT fk_resource_link_preview_image
        FOREIGN KEY (preview_image_file_id) REFERENCES stored_file (stored_file_id)
) ENGINE=InnoDB;

CREATE TABLE resource_link_preview_retirement_manifest (
    resource_link_preview_retirement_manifest_id BIGINT       NOT NULL AUTO_INCREMENT,
    stored_file_id                               BIGINT       NOT NULL,
    storage_scope_code                           VARCHAR(20)  NOT NULL,
    storage_key                                  VARCHAR(500) NOT NULL,
    retirement_status_code                       VARCHAR(20)  NOT NULL,
    attempt_count                                INT          NOT NULL DEFAULT 0,
    failure_reason                               VARCHAR(500) NULL,
    completed_dttm                               DATETIME(6)  NULL,
    created_dttm                                 DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm                                 DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_resource_link_preview_retirement_manifest
        PRIMARY KEY (resource_link_preview_retirement_manifest_id),
    CONSTRAINT uk_resource_link_preview_retirement_file UNIQUE (stored_file_id),
    KEY idx_resource_link_preview_retirement_status (retirement_status_code, created_dttm),
    CONSTRAINT ck_resource_link_preview_retirement_scope
        CHECK (storage_scope_code = 'PRIVATE'),
    CONSTRAINT ck_resource_link_preview_retirement_status
        CHECK (retirement_status_code IN ('PENDING', 'DELETED', 'FAILED')),
    CONSTRAINT ck_resource_link_preview_retirement_attempt CHECK (attempt_count >= 0)
) ENGINE=InnoDB;

DROP TEMPORARY TABLE retained_resource_file;
