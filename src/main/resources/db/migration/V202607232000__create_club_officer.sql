CREATE TABLE club_officer (
    club_officer_id BIGINT       NOT NULL AUTO_INCREMENT,
    position_code  VARCHAR(30)  NOT NULL,
    member_id      BIGINT       NOT NULL,
    appointed_dttm DATETIME     NOT NULL,
    created_dttm   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_dttm   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (club_officer_id),
    UNIQUE KEY uk_club_officer_position (position_code),
    KEY idx_club_officer_member (member_id),
    CONSTRAINT fk_club_officer_member
        FOREIGN KEY (member_id) REFERENCES member (member_id),
    CONSTRAINT ck_club_officer_position
        CHECK (position_code IN ('PRESIDENT'))
);
