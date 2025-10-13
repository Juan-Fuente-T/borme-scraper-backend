ALTER TABLE companies
ADD COLUMN capital_numeric BIGINT;

ALTER TABLE companies DROP COLUMN start_date;
ALTER TABLE companies ADD COLUMN start_date DATE;