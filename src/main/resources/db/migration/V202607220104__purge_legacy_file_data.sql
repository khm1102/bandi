-- MinIO 객체는 이 migration 전에 운영 절차로 파기한다.
-- 기존 파일을 보존·이전하지 않는 결정에 따라 파일이 연결된 업무 데이터도 함께 제거한다.

DELETE FROM internal_notice_read;
DELETE FROM internal_notice_attachment;
DELETE FROM public_notice_attachment;
DELETE FROM resource_file;

-- activity_record_file은 자기 참조 FK를 가지므로 행 전체를 한 번에 비운다.
DELETE FROM activity_record_file;
DELETE FROM activity_record_revision;
DELETE FROM activity_review_history;
DELETE FROM activity_record;

DELETE FROM internal_notice;
DELETE FROM public_notice;
DELETE FROM resource;

-- 소품·장비 품목과 상태 이력은 유지하고, 기존 사진 연결만 해제한다.
UPDATE asset_item
SET photo_file_id = NULL
WHERE photo_file_id IS NOT NULL;

DELETE FROM stored_file;
