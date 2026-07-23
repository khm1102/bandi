ALTER TABLE calendar_event
    ADD COLUMN color_code VARCHAR(20) NOT NULL DEFAULT 'NAVY' AFTER place,
    ADD CONSTRAINT ck_calendar_event_color_code
        CHECK (color_code IN ('NAVY', 'MINT', 'BLUE', 'PLUM', 'AMBER', 'ROSE'));
