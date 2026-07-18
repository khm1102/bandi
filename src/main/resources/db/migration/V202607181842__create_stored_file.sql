-- 파일 바이너리는 MinIO에 저장하고 이 테이블에는 검증·조회용 메타데이터만 둔다.
-- 업무 테이블은 upload_status_code = 'READY'인 행만 연결한다 (정본 7장).
CREATE TABLE stored_file (
    stored_file_id        BIGINT       NOT NULL AUTO_INCREMENT,
    original_name         VARCHAR(255) NOT NULL,
    storage_scope_code    VARCHAR(20)  NOT NULL,
    storage_key           VARCHAR(500) NOT NULL,
    content_type          VARCHAR(100) NOT NULL,
    size_bytes            BIGINT       NOT NULL,
    sha256_hash           CHAR(64)     NOT NULL,
    object_etag           VARCHAR(100) NULL,
    uploaded_by_member_id BIGINT       NULL,
    upload_status_code    VARCHAR(20)  NOT NULL,
    created_dttm          DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm          DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    deleted_dttm          DATETIME(6)  NULL,
    CONSTRAINT pk_stored_file PRIMARY KEY (stored_file_id),
    CONSTRAINT uk_stored_file_scope_storage_key UNIQUE (storage_scope_code, storage_key),
    KEY idx_stored_file_hash_size (sha256_hash, size_bytes),
    KEY idx_stored_file_uploader_created (uploaded_by_member_id, created_dttm),
    KEY idx_stored_file_status_created (upload_status_code, created_dttm),
    CONSTRAINT fk_stored_file_uploaded_by_member
        FOREIGN KEY (uploaded_by_member_id) REFERENCES member (member_id),
    CONSTRAINT ck_stored_file_storage_scope_code
        CHECK (storage_scope_code IN ('PRIVATE', 'PUBLIC')),
    CONSTRAINT ck_stored_file_upload_status_code
        CHECK (upload_status_code IN ('PENDING', 'READY', 'FAILED', 'QUARANTINED')),
    CONSTRAINT ck_stored_file_size_bytes CHECK (size_bytes > 0),
    CONSTRAINT ck_stored_file_sha256_hash CHECK (sha256_hash REGEXP '^[0-9a-f]{64}$'),
    CONSTRAINT ck_stored_file_ready_etag CHECK (
        upload_status_code <> 'READY' OR object_etag IS NOT NULL
    )
) ENGINE=InnoDB;
