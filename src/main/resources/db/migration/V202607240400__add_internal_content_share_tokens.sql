ALTER TABLE internal_notice
    ADD COLUMN share_token VARCHAR(43) NULL,
    ADD CONSTRAINT uk_internal_notice_share_token UNIQUE (share_token);

ALTER TABLE resource
    ADD COLUMN share_token VARCHAR(43) NULL,
    ADD CONSTRAINT uk_resource_share_token UNIQUE (share_token);
