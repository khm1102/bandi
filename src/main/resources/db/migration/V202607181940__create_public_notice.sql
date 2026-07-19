-- 외부 공시 게시판 (docs/database-schema.md 9.1).
CREATE TABLE public_notice (
    public_notice_id       BIGINT       NOT NULL AUTO_INCREMENT,
    category_code          VARCHAR(30)  NOT NULL,
    title                  VARCHAR(200) NOT NULL,
    body                   LONGTEXT     NOT NULL,
    status_code            VARCHAR(20)  NOT NULL,
    is_pinned              TINYINT(1)   NOT NULL DEFAULT 0,
    publish_start_dttm     DATETIME(6)  NULL,
    publish_end_dttm       DATETIME(6)  NULL,
    created_by_member_id   BIGINT       NOT NULL,
    updated_by_member_id   BIGINT       NOT NULL,
    published_by_member_id BIGINT       NULL,
    created_dttm           DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm           DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_dttm           DATETIME(6)  NULL,
    CONSTRAINT pk_public_notice PRIMARY KEY (public_notice_id),
    KEY idx_public_notice_visibility
        (status_code, publish_start_dttm, publish_end_dttm, is_pinned),
    KEY idx_public_notice_created_by (created_by_member_id),
    KEY idx_public_notice_updated_by (updated_by_member_id),
    KEY idx_public_notice_published_by (published_by_member_id),
    CONSTRAINT fk_public_notice_created_by_member
        FOREIGN KEY (created_by_member_id) REFERENCES member (member_id),
    CONSTRAINT fk_public_notice_updated_by_member
        FOREIGN KEY (updated_by_member_id) REFERENCES member (member_id),
    CONSTRAINT fk_public_notice_published_by_member
        FOREIGN KEY (published_by_member_id) REFERENCES member (member_id),
    CONSTRAINT ck_public_notice_status CHECK (
        status_code IN ('DRAFT', 'SCHEDULED', 'PUBLISHED', 'CLOSED', 'ARCHIVED')
    ),
    CONSTRAINT ck_public_notice_is_pinned CHECK (is_pinned IN (0, 1)),
    CONSTRAINT ck_public_notice_category CHECK (CHAR_LENGTH(TRIM(category_code)) > 0),
    CONSTRAINT ck_public_notice_title CHECK (CHAR_LENGTH(TRIM(title)) > 0),
    CONSTRAINT ck_public_notice_body CHECK (CHAR_LENGTH(TRIM(body)) > 0),
    CONSTRAINT ck_public_notice_publish_period CHECK (
        publish_end_dttm IS NULL
        OR publish_start_dttm IS NULL
        OR publish_end_dttm >= publish_start_dttm
    ),
    CONSTRAINT ck_public_notice_draft_publish CHECK (
        status_code <> 'DRAFT'
        OR (
            publish_start_dttm IS NULL
            AND publish_end_dttm IS NULL
            AND published_by_member_id IS NULL
        )
    ),
    CONSTRAINT ck_public_notice_active_publish CHECK (
        status_code IN ('DRAFT', 'ARCHIVED')
        OR (
            publish_start_dttm IS NOT NULL
            AND published_by_member_id IS NOT NULL
        )
    )
) ENGINE=InnoDB;

CREATE TABLE public_notice_attachment (
    public_notice_attachment_id BIGINT      NOT NULL AUTO_INCREMENT,
    public_notice_id            BIGINT      NOT NULL,
    stored_file_id              BIGINT      NOT NULL,
    display_order               INT         NOT NULL,
    created_dttm                DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm                DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_public_notice_attachment PRIMARY KEY (public_notice_attachment_id),
    CONSTRAINT uk_public_notice_attachment_file
        UNIQUE (public_notice_id, stored_file_id),
    CONSTRAINT uk_public_notice_attachment_order
        UNIQUE (public_notice_id, display_order),
    KEY idx_public_notice_attachment_file (stored_file_id),
    CONSTRAINT fk_public_notice_attachment_notice
        FOREIGN KEY (public_notice_id) REFERENCES public_notice (public_notice_id),
    CONSTRAINT fk_public_notice_attachment_stored_file
        FOREIGN KEY (stored_file_id) REFERENCES stored_file (stored_file_id),
    CONSTRAINT ck_public_notice_attachment_order CHECK (display_order >= 0)
) ENGINE=InnoDB;
