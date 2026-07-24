ALTER TABLE cohort
    DROP CHECK ck_cohort_term_code,
    DROP INDEX uk_cohort_year_term,
    DROP COLUMN admission_year,
    DROP COLUMN term_code;

ALTER TABLE member
    ADD COLUMN phone_number VARCHAR(20) NULL AFTER department;
