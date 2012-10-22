SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Add the field 'birth_date' to table 'order'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order` 
ADD COLUMN `birth_date` DATETIME NOT NULL DEFAULT 0 COMMENT 'the birth date to this order'  AFTER `restaurant_id`,
CHANGE COLUMN `order_date` `order_date` DATETIME NOT NULL DEFAULT 0 COMMENT 'the end date to this order'  ;

-- -----------------------------------------------------
-- Add the field 'birth_date' to table 'order_history'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_history` 
ADD COLUMN `birth_date` DATETIME NOT NULL DEFAULT 0 COMMENT 'the birth date to this order'  AFTER `restaurant_id`,
CHANGE COLUMN `order_date` `order_date` DATETIME NOT NULL DEFAULT 0 COMMENT 'the end date to this order'  ;