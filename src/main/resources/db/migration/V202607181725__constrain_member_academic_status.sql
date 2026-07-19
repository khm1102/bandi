ALTER TABLE member
    ADD CONSTRAINT ck_member_academic_status_code
        CHECK (academic_status_code IN ('ENROLLED', 'LEAVE_OF_ABSENCE', 'GRADUATED', 'UNKNOWN'));
