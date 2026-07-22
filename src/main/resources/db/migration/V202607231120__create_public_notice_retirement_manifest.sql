-- 외부 공시 폐기 1단계: 첨부 파일을 삭제하기 전에 저장 위치와 공유 참조 여부를 스냅샷한다.
CREATE TABLE public_notice_retirement_manifest (
    public_notice_retirement_manifest_id BIGINT       NOT NULL AUTO_INCREMENT,
    stored_file_id                       BIGINT       NOT NULL,
    storage_scope_code                   VARCHAR(20)  NOT NULL,
    storage_key                          VARCHAR(500) NOT NULL,
    retirement_status_code               VARCHAR(20)  NOT NULL,
    failure_reason                       VARCHAR(100) NULL,
    processed_dttm                       DATETIME(6)  NULL,
    created_dttm                         DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm                         DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_public_notice_retirement_manifest
        PRIMARY KEY (public_notice_retirement_manifest_id),
    CONSTRAINT uk_public_notice_retirement_manifest_stored_file
        UNIQUE (stored_file_id),
    KEY idx_public_notice_retirement_manifest_status (retirement_status_code),
    CONSTRAINT ck_public_notice_retirement_manifest_scope
        CHECK (storage_scope_code IN ('PRIVATE', 'PUBLIC')),
    CONSTRAINT ck_public_notice_retirement_manifest_status
        CHECK (retirement_status_code IN ('PENDING', 'DELETED', 'RETAINED_SHARED', 'FAILED'))
) ENGINE=InnoDB;

INSERT INTO public_notice_retirement_manifest (
    stored_file_id, storage_scope_code, storage_key, retirement_status_code, processed_dttm
)
SELECT stored_file.stored_file_id,
       stored_file.storage_scope_code,
       stored_file.storage_key,
       CASE
           WHEN EXISTS (
               SELECT 1
               FROM internal_notice_attachment
               WHERE stored_file_id = stored_file.stored_file_id
           )
           OR EXISTS (
               SELECT 1
               FROM resource_file
               WHERE stored_file_id = stored_file.stored_file_id
           )
           OR EXISTS (
               SELECT 1
               FROM activity_record_file
               WHERE stored_file_id = stored_file.stored_file_id
           )
           OR EXISTS (
               SELECT 1
               FROM asset_item
               WHERE photo_file_id = stored_file.stored_file_id
           ) THEN 'RETAINED_SHARED'
           ELSE 'PENDING'
       END,
       CASE
           WHEN EXISTS (
               SELECT 1
               FROM internal_notice_attachment
               WHERE stored_file_id = stored_file.stored_file_id
           )
           OR EXISTS (
               SELECT 1
               FROM resource_file
               WHERE stored_file_id = stored_file.stored_file_id
           )
           OR EXISTS (
               SELECT 1
               FROM activity_record_file
               WHERE stored_file_id = stored_file.stored_file_id
           )
           OR EXISTS (
               SELECT 1
               FROM asset_item
               WHERE photo_file_id = stored_file.stored_file_id
           ) THEN NOW(6)
           ELSE NULL
       END
FROM public_notice_attachment
JOIN stored_file
    ON stored_file.stored_file_id = public_notice_attachment.stored_file_id
GROUP BY stored_file.stored_file_id, stored_file.storage_scope_code, stored_file.storage_key;
