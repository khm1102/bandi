-- MinIO 객체는 이 migration 전에 운영 절차로 파기한다.
-- 기존 파일을 보존·이전하지 않는 결정에 따라 파일이 연결된 업무 데이터만 함께 제거한다.
-- 첨부가 없는 공지·자료·활동 기록은 보존한다.

CREATE TEMPORARY TABLE purge_public_notice_id AS
SELECT DISTINCT public_notice_id
FROM public_notice_attachment;

CREATE TEMPORARY TABLE purge_internal_notice_id AS
SELECT DISTINCT internal_notice_id
FROM internal_notice_attachment;

CREATE TEMPORARY TABLE purge_resource_id AS
SELECT DISTINCT resource_id
FROM resource_file;

CREATE TEMPORARY TABLE purge_activity_record_id AS
SELECT DISTINCT activity_record_id
FROM activity_record_file;

DELETE internal_notice_read
FROM internal_notice_read
JOIN purge_internal_notice_id
    ON internal_notice_read.internal_notice_id = purge_internal_notice_id.internal_notice_id;

DELETE public_notice_attachment
FROM public_notice_attachment
JOIN purge_public_notice_id
    ON public_notice_attachment.public_notice_id = purge_public_notice_id.public_notice_id;

DELETE internal_notice_attachment
FROM internal_notice_attachment
JOIN purge_internal_notice_id
    ON internal_notice_attachment.internal_notice_id = purge_internal_notice_id.internal_notice_id;

DELETE resource_file
FROM resource_file
JOIN purge_resource_id
    ON resource_file.resource_id = purge_resource_id.resource_id;

DELETE activity_record_revision
FROM activity_record_revision
JOIN purge_activity_record_id
    ON activity_record_revision.activity_record_id = purge_activity_record_id.activity_record_id;

DELETE activity_review_history
FROM activity_review_history
JOIN purge_activity_record_id
    ON activity_review_history.activity_record_id = purge_activity_record_id.activity_record_id;

-- activity_record_file의 자기 참조를 먼저 끊은 뒤 파일 연결 행을 제거한다.
UPDATE activity_record_file
JOIN purge_activity_record_id
    ON activity_record_file.activity_record_id = purge_activity_record_id.activity_record_id
SET activity_record_file.replaced_by_activity_record_file_id = NULL,
    activity_record_file.replaced_dttm = NULL,
    activity_record_file.replaced_by_member_id = NULL;

DELETE activity_record_file
FROM activity_record_file
JOIN purge_activity_record_id
    ON activity_record_file.activity_record_id = purge_activity_record_id.activity_record_id;

DELETE public_notice
FROM public_notice
JOIN purge_public_notice_id
    ON public_notice.public_notice_id = purge_public_notice_id.public_notice_id;

DELETE internal_notice
FROM internal_notice
JOIN purge_internal_notice_id
    ON internal_notice.internal_notice_id = purge_internal_notice_id.internal_notice_id;

DELETE resource
FROM resource
JOIN purge_resource_id
    ON resource.resource_id = purge_resource_id.resource_id;

DELETE activity_record
FROM activity_record
JOIN purge_activity_record_id
    ON activity_record.activity_record_id = purge_activity_record_id.activity_record_id;

-- 소품·장비 품목과 상태 이력은 유지하고, 기존 사진 연결만 해제한다.
UPDATE asset_item
SET photo_file_id = NULL
WHERE photo_file_id IS NOT NULL;

DELETE FROM stored_file;

DROP TEMPORARY TABLE purge_activity_record_id;
DROP TEMPORARY TABLE purge_resource_id;
DROP TEMPORARY TABLE purge_internal_notice_id;
DROP TEMPORARY TABLE purge_public_notice_id;
