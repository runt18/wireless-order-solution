SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Update the dept_id to 253 for the temporary record
-- -----------------------------------------------------
UPDATE wireless_order_db.order_food SET dept_id = 253 WHERE is_temporary = 1;

UPDATE wireless_order_db.order_food_history SET dept_id = 253 WHERE is_temporary = 1;
