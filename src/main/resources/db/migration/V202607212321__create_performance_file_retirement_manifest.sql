CREATE TABLE performance_file_retirement_manifest (
    performance_file_retirement_manifest_id BIGINT       NOT NULL AUTO_INCREMENT,
    stored_file_id                          BIGINT       NOT NULL,
    storage_scope_code                      VARCHAR(20)  NOT NULL,
    storage_key                             VARCHAR(500) NOT NULL,
    status_code                             VARCHAR(20)  NOT NULL,
    failure_reason                          VARCHAR(100) NULL,
    object_deleted_dttm                     DATETIME(6)  NULL,
    created_dttm                            DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm                            DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_performance_file_retirement_manifest PRIMARY KEY (
        performance_file_retirement_manifest_id
    ),
    CONSTRAINT uk_performance_file_retirement_manifest_stored_file UNIQUE (
        stored_file_id
    ),
    KEY idx_performance_file_retirement_manifest_status (
        status_code, performance_file_retirement_manifest_id
    ),
    CONSTRAINT ck_performance_file_retirement_manifest_scope CHECK (
        storage_scope_code IN ('PRIVATE', 'PUBLIC')
    ),
    CONSTRAINT ck_performance_file_retirement_manifest_status CHECK (
        status_code IN ('PENDING', 'DELETED', 'SKIPPED', 'FAILED')
    ),
    CONSTRAINT ck_performance_file_retirement_manifest_deleted CHECK (
        (status_code = 'DELETED' AND object_deleted_dttm IS NOT NULL)
        OR (status_code <> 'DELETED' AND object_deleted_dttm IS NULL)
    )
) ENGINE=InnoDB;

-- stored_file에는 FK를 두지 않는다. 2단계에서 객체 삭제가 확인된 메타데이터를
-- 하드 삭제한 뒤 이 임시 manifest 자체를 제거해야 하기 때문이다.
