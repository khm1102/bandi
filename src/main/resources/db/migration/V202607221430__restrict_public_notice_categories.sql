ALTER TABLE public_notice
    DROP CHECK ck_public_notice_category,
    ADD CONSTRAINT ck_public_notice_category
        CHECK (category_code IN ('GENERAL', 'RECRUITMENT'));
