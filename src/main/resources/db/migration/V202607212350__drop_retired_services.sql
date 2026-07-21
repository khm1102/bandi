-- 폐기 서비스: 회비, 행사·출석, 공연 제작·콘텐츠·운영, 관람 신청·입장, 정책,
-- 프로젝트 기반 소품 사용 기록. stored_file은 파일 저장 전환 migration까지 유지한다.

DROP TABLE seat_entry_history;
DROP TABLE active_seat_occupancy;
DROP TABLE reservation_status_history;
DROP TABLE reservation_seat;
DROP TABLE reservation;
DROP TABLE performance_round_seat;

DROP TABLE fee_charge_history;
DROP TABLE fee_charge;
DROP TABLE fee_item;

DROP TABLE event_attendance_history;
DROP TABLE event_attendance;
DROP TABLE club_event;

DROP TABLE production_task_history;
DROP TABLE production_task;
DROP TABLE checklist_item_history;
DROP TABLE checklist_item;
DROP TABLE asset_usage;

DROP TABLE performance_round_cast;
DROP TABLE performance_round_accessibility;
DROP TABLE performance_cast_history;
DROP TABLE performance_cast;
DROP TABLE production_credit;
DROP TABLE performance_media;
DROP TABLE performance_character;
DROP TABLE performance_public_notice;
DROP TABLE performance_public_page;
DROP TABLE performance_viewing_guide;
DROP TABLE performance_round;

DROP TABLE public_profile_consent;
DROP TABLE public_profile;
DROP TABLE policy_document_version;
DROP TABLE policy_document;
DROP TABLE performance_project;
