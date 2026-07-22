-- 외부 공시 2단계: 1단계 파일 퇴역이 모두 끝난 경우에만 스키마를 제거한다.
DELIMITER $$

CREATE PROCEDURE assert_public_notice_retirement_complete()
BEGIN
    IF EXISTS (
        SELECT 1
        FROM public_notice_retirement_manifest
        WHERE retirement_status_code NOT IN ('DELETED', 'RETAINED_SHARED')
    ) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'public notice retirement manifest is incomplete';
    END IF;
END$$

CALL assert_public_notice_retirement_complete()$$
DROP PROCEDURE assert_public_notice_retirement_complete$$

DELIMITER ;

DROP TABLE public_notice_attachment;
DROP TABLE public_notice;

DELETE stored_file
FROM stored_file
JOIN public_notice_retirement_manifest
    ON public_notice_retirement_manifest.stored_file_id = stored_file.stored_file_id
WHERE public_notice_retirement_manifest.retirement_status_code = 'DELETED'
  AND NOT EXISTS (
      SELECT 1 FROM internal_notice_attachment
      WHERE internal_notice_attachment.stored_file_id = stored_file.stored_file_id
  )
  AND NOT EXISTS (
      SELECT 1 FROM resource_file
      WHERE resource_file.stored_file_id = stored_file.stored_file_id
  )
  AND NOT EXISTS (
      SELECT 1 FROM activity_record_file
      WHERE activity_record_file.stored_file_id = stored_file.stored_file_id
  )
  AND NOT EXISTS (
      SELECT 1 FROM asset_item
      WHERE asset_item.photo_file_id = stored_file.stored_file_id
  );

DROP TABLE public_notice_retirement_manifest;
