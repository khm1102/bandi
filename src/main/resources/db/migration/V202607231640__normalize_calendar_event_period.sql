-- 종일 일정은 시작일 00:00 이상, 사용자가 선택한 마지막 날짜 다음 날 00:00 미만으로 저장한다.
UPDATE calendar_event
SET start_dttm = TIMESTAMP(DATE(start_dttm)),
    end_dttm = CASE
        WHEN TIME(end_dttm) = '00:00:00' AND DATE(end_dttm) > DATE(start_dttm)
            THEN end_dttm
        ELSE TIMESTAMP(DATE_ADD(DATE(end_dttm), INTERVAL 1 DAY))
    END
WHERE is_all_day = 1;

-- 기존 비종일 0분 일정은 새 엄격한 기간 제약을 적용할 수 있도록 최소 30분으로 보정한다.
UPDATE calendar_event
SET end_dttm = DATE_ADD(start_dttm, INTERVAL 30 MINUTE)
WHERE is_all_day = 0
  AND end_dttm = start_dttm;

ALTER TABLE calendar_event
    DROP CHECK ck_calendar_event_period;

ALTER TABLE calendar_event
    ADD CONSTRAINT ck_calendar_event_period CHECK (end_dttm > start_dttm);
