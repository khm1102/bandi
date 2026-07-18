CREATE TABLE policy_document (
    policy_document_id  BIGINT       NOT NULL AUTO_INCREMENT,
    policy_type_code    VARCHAR(30)  NOT NULL,
    title               VARCHAR(200) NOT NULL,
    audience_code       VARCHAR(30)  NOT NULL,
    is_active           TINYINT(1)   NOT NULL DEFAULT 1,
    created_dttm        DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm        DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_policy_document PRIMARY KEY (policy_document_id),
    CONSTRAINT ck_policy_document_type_code CHECK (
        policy_type_code IN (
            'PRIVACY', 'CLUB_RULE', 'RESERVATION_PRIVACY', 'TERMS'
        )
    ),
    CONSTRAINT ck_policy_document_audience_code CHECK (
        audience_code IN ('MEMBER', 'VISITOR', 'ALL')
    ),
    CONSTRAINT ck_policy_document_title CHECK (
        CHAR_LENGTH(TRIM(title)) > 0
    ),
    CONSTRAINT ck_policy_document_active CHECK (is_active IN (0, 1))
) ENGINE=InnoDB;

CREATE TABLE policy_document_version (
    policy_document_version_id  BIGINT      NOT NULL AUTO_INCREMENT,
    policy_document_id          BIGINT      NOT NULL,
    version_no                  INT         NOT NULL,
    body                        LONGTEXT    NOT NULL,
    published_dttm              DATETIME(6) NOT NULL,
    published_by_member_id      BIGINT      NULL,
    effective_from_dttm         DATETIME(6) NOT NULL,
    is_required                 TINYINT(1)  NOT NULL,
    CONSTRAINT pk_policy_document_version PRIMARY KEY (
        policy_document_version_id
    ),
    CONSTRAINT uk_policy_document_version_document_no UNIQUE (
        policy_document_id, version_no
    ),
    KEY idx_policy_document_version_effective (
        policy_document_id, effective_from_dttm,
        policy_document_version_id
    ),
    KEY idx_policy_document_version_published_by (
        published_by_member_id
    ),
    CONSTRAINT fk_policy_document_version_document FOREIGN KEY (
        policy_document_id
    ) REFERENCES policy_document (policy_document_id),
    CONSTRAINT fk_policy_document_version_published_by_member FOREIGN KEY (
        published_by_member_id
    ) REFERENCES member (member_id),
    CONSTRAINT ck_policy_document_version_no CHECK (version_no > 0),
    CONSTRAINT ck_policy_document_version_body CHECK (
        CHAR_LENGTH(TRIM(body)) > 0
    ),
    CONSTRAINT ck_policy_document_version_effective CHECK (
        effective_from_dttm >= published_dttm
    ),
    CONSTRAINT ck_policy_document_version_required CHECK (
        is_required IN (0, 1)
    )
) ENGINE=InnoDB;

CREATE TABLE public_profile (
    public_profile_id       BIGINT       NOT NULL AUTO_INCREMENT,
    member_id               BIGINT       NULL,
    public_name             VARCHAR(100) NOT NULL,
    bio                     TEXT         NULL,
    profile_file_id         BIGINT       NULL,
    social_url              VARCHAR(500) NULL,
    visibility_status_code  VARCHAR(30)  NOT NULL,
    created_dttm            DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_dttm            DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_public_profile PRIMARY KEY (public_profile_id),
    CONSTRAINT uk_public_profile_member UNIQUE (member_id),
    KEY idx_public_profile_visibility (
        visibility_status_code, public_profile_id
    ),
    KEY idx_public_profile_file (profile_file_id),
    CONSTRAINT fk_public_profile_member FOREIGN KEY (member_id)
        REFERENCES member (member_id),
    CONSTRAINT fk_public_profile_file FOREIGN KEY (profile_file_id)
        REFERENCES stored_file (stored_file_id),
    CONSTRAINT ck_public_profile_name CHECK (
        CHAR_LENGTH(TRIM(public_name)) > 0
    ),
    CONSTRAINT ck_public_profile_visibility CHECK (
        visibility_status_code IN ('DRAFT', 'PUBLISHED', 'ARCHIVED')
    )
) ENGINE=InnoDB;

CREATE TABLE public_profile_consent (
    public_profile_consent_id    BIGINT      NOT NULL AUTO_INCREMENT,
    public_profile_id            BIGINT      NOT NULL,
    policy_document_version_id   BIGINT      NOT NULL,
    consent_scope_code           VARCHAR(30) NOT NULL,
    is_agreed                    TINYINT(1)  NOT NULL,
    agreed_dttm                  DATETIME(6) NOT NULL,
    revoked_dttm                 DATETIME(6) NULL,
    recorded_by_member_id        BIGINT      NOT NULL,
    CONSTRAINT pk_public_profile_consent PRIMARY KEY (
        public_profile_consent_id
    ),
    CONSTRAINT uk_public_profile_consent_profile_version_scope UNIQUE (
        public_profile_id, policy_document_version_id,
        consent_scope_code
    ),
    KEY idx_public_profile_consent_latest (
        public_profile_id, consent_scope_code,
        policy_document_version_id, public_profile_consent_id
    ),
    KEY idx_public_profile_consent_recorded_by (
        recorded_by_member_id
    ),
    CONSTRAINT fk_public_profile_consent_profile FOREIGN KEY (
        public_profile_id
    ) REFERENCES public_profile (public_profile_id),
    CONSTRAINT fk_public_profile_consent_policy_version FOREIGN KEY (
        policy_document_version_id
    ) REFERENCES policy_document_version (policy_document_version_id),
    CONSTRAINT fk_public_profile_consent_recorded_by_member FOREIGN KEY (
        recorded_by_member_id
    ) REFERENCES member (member_id),
    CONSTRAINT ck_public_profile_consent_scope CHECK (
        consent_scope_code IN ('NAME', 'PHOTO', 'BIO', 'SOCIAL')
    ),
    CONSTRAINT ck_public_profile_consent_agreed CHECK (
        is_agreed IN (0, 1)
    ),
    CONSTRAINT ck_public_profile_consent_state CHECK (
        (is_agreed = 1 AND revoked_dttm IS NULL)
        OR (is_agreed = 0 AND revoked_dttm IS NOT NULL
            AND revoked_dttm >= agreed_dttm)
    )
) ENGINE=InnoDB;
