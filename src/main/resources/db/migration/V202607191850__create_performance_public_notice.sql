CREATE TABLE performance_public_notice (
    performance_public_notice_id BIGINT AUTO_INCREMENT,
    performance_project_id BIGINT NOT NULL,
    public_notice_id BIGINT NOT NULL,
    created_dttm DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_performance_public_notice
        PRIMARY KEY (performance_public_notice_id),
    CONSTRAINT uk_performance_public_notice_project_notice
        UNIQUE (performance_project_id, public_notice_id),
    KEY idx_performance_public_notice_notice_id (public_notice_id),
    CONSTRAINT fk_performance_public_notice_project
        FOREIGN KEY (performance_project_id)
        REFERENCES performance_project (performance_project_id),
    CONSTRAINT fk_performance_public_notice_notice
        FOREIGN KEY (public_notice_id)
        REFERENCES public_notice (public_notice_id)
);
