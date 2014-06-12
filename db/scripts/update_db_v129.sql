SET @OLD_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
SET NAMES utf8;
SET SQL_SAFE_UPDATES = 0;
USE wireless_order_db;

-- -----------------------------------------------------
-- Add the field 'is_gift' to table 'order_food' & 'order_food_history'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food` 
CHANGE COLUMN `hang_status` `is_gift` TINYINT(4) NOT NULL DEFAULT '0' COMMENT 'indicates the order food is gift' ;

ALTER TABLE `wireless_order_db`.`order_food_history` 
ADD COLUMN `is_gift` TINYINT NOT NULL DEFAULT 0 COMMENT 'indicates the order food is gift' AFTER `is_paid`;

-- -----------------------------------------------------
-- Update the the gift status whose record belongs to gift
-- -----------------------------------------------------
UPDATE `wireless_order_db`.`order_food`
SET is_gift = 1
WHERE (food_status & (1 << 3)) <> 0;

UPDATE `wireless_order_db`.`order_food_history`
SET is_gift = 1
WHERE (food_status & (1 << 3)) <> 0;

SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;



