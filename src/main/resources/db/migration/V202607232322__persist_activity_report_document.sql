ALTER TABLE activity_record_file
    DROP CHECK ck_activity_record_file_role,
    ADD CONSTRAINT ck_activity_record_file_role
        CHECK (file_role_code IN ('EVIDENCE', 'ADDITIONAL', 'DOCUMENT'));

CREATE TABLE activity_report_document (
    activity_report_document_id BIGINT      NOT NULL AUTO_INCREMENT,
    activity_record_id          BIGINT      NOT NULL,
    representative             VARCHAR(20) NOT NULL,
    location                   VARCHAR(50) NOT NULL,
    created_dttm               DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm               DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_activity_report_document PRIMARY KEY (activity_report_document_id),
    CONSTRAINT uk_activity_report_document_record UNIQUE (activity_record_id),
    CONSTRAINT fk_activity_report_document_record
        FOREIGN KEY (activity_record_id) REFERENCES activity_record (activity_record_id)
) ENGINE=InnoDB;

CREATE TABLE activity_report_participant (
    activity_report_participant_id BIGINT      NOT NULL AUTO_INCREMENT,
    activity_report_document_id    BIGINT      NOT NULL,
    display_order                  INT         NOT NULL,
    name                           VARCHAR(20) NOT NULL,
    department                     VARCHAR(30) NULL,
    student_no                     VARCHAR(20) NULL,
    note                           VARCHAR(40) NULL,
    created_dttm                   DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm                   DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_activity_report_participant PRIMARY KEY (activity_report_participant_id),
    CONSTRAINT uk_activity_report_participant_order
        UNIQUE (activity_report_document_id, display_order),
    CONSTRAINT fk_activity_report_participant_document
        FOREIGN KEY (activity_report_document_id)
        REFERENCES activity_report_document (activity_report_document_id),
    CONSTRAINT ck_activity_report_participant_order
        CHECK (display_order BETWEEN 0 AND 13)
) ENGINE=InnoDB;
