ALTER TABLE cohort
    ADD CONSTRAINT ck_cohort_term_code
        CHECK (term_code IN ('FIRST', 'SECOND'));
