-- 개발 환경에서 실제로 등록된 활성 멤버의 학번으로 바꿔 실행한다.
SET @president_student_no = 'REPLACE_WITH_ACTUAL_STUDENT_NO';

SELECT member_id, student_no, name, member_status_code
FROM member
WHERE student_no = @president_student_no;

INSERT INTO club_officer (position_code, member_id, appointed_dttm)
SELECT 'PRESIDENT', member_id, CURRENT_TIMESTAMP
FROM member
WHERE student_no = @president_student_no
  AND member_status_code = 'ACTIVE'
ON DUPLICATE KEY UPDATE
    member_id = VALUES(member_id),
    appointed_dttm = VALUES(appointed_dttm),
    updated_dttm = CURRENT_TIMESTAMP;
