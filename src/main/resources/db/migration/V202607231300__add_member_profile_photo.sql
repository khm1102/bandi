-- 내부 프로필 아바타는 일반 첨부와 목적을 분리한다.
ALTER TABLE stored_file
    ADD COLUMN file_purpose_code VARCHAR(30) NOT NULL DEFAULT 'GENERAL'
        AFTER upload_status_code,
    ADD CONSTRAINT ck_stored_file_purpose_code
        CHECK (file_purpose_code IN ('GENERAL', 'PROFILE_IMAGE'));

ALTER TABLE member
    ADD COLUMN profile_photo_file_id BIGINT NULL AFTER registered_by_member_id,
    ADD KEY idx_member_profile_photo_file (profile_photo_file_id),
    ADD CONSTRAINT fk_member_profile_photo_file
        FOREIGN KEY (profile_photo_file_id) REFERENCES stored_file (stored_file_id);

-- 사진 연결을 해제한 뒤 물리 파일과 메타데이터를 재시도 가능하게 파기한다.
CREATE TABLE member_profile_photo_retirement_manifest (
    member_profile_photo_retirement_manifest_id BIGINT       NOT NULL AUTO_INCREMENT,
    stored_file_id                              BIGINT       NOT NULL,
    storage_scope_code                          VARCHAR(20)  NOT NULL,
    storage_key                                 VARCHAR(500) NOT NULL,
    retirement_status_code                      VARCHAR(20)  NOT NULL,
    attempt_count                               INT          NOT NULL DEFAULT 0,
    failure_reason                              VARCHAR(500) NULL,
    completed_dttm                              DATETIME(6)  NULL,
    created_dttm                                DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm                                DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_member_profile_photo_retirement_manifest
        PRIMARY KEY (member_profile_photo_retirement_manifest_id),
    CONSTRAINT uk_member_profile_photo_retirement_file UNIQUE (stored_file_id),
    KEY idx_member_profile_photo_retirement_status (retirement_status_code, created_dttm),
    CONSTRAINT ck_member_profile_photo_retirement_scope
        CHECK (storage_scope_code = 'PRIVATE'),
    CONSTRAINT ck_member_profile_photo_retirement_status
        CHECK (retirement_status_code IN ('PENDING', 'DELETED', 'FAILED')),
    CONSTRAINT ck_member_profile_photo_retirement_attempt_count CHECK (attempt_count >= 0)
) ENGINE=InnoDB;
