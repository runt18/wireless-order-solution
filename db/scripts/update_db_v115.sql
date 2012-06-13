SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Add the field 'taste_tmp_alias' to table 'order_food'
-- Add the field 'taste_tmp' to 'order_food'
-- Add the field 'taste_tmp_price' to 'order_food'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food` 
ADD COLUMN `taste_tmp_alias` SMALLINT UNSIGNED NULL DEFAULT NULL COMMENT 'the alias id to temporay taste'  AFTER `taste3_alias` , 
ADD COLUMN `taste_tmp` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the value to temporary taste'  AFTER `taste_tmp_alias` , 
ADD COLUMN `taste_tmp_price` DECIMAL(7,2) NULL DEFAULT NULL COMMENT 'the price to temporary taste'  AFTER `taste_tmp` ;

-- -----------------------------------------------------
-- Add the field 'taste_tmp_alias' to table 'order_food_history'
-- Add the field 'taste_tmp' to 'order_food_history'
-- Add the field 'taste_tmp_price' to 'order_food_history'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food_history` 
ADD COLUMN `taste_tmp_alias` SMALLINT UNSIGNED NULL DEFAULT NULL COMMENT 'the alias id to temporay taste'  AFTER `taste3_alias` , 
ADD COLUMN `taste_tmp` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the value to temporary taste'  AFTER `taste_tmp_alias` , 
ADD COLUMN `taste_tmp_price` DECIMAL(7,2) NULL DEFAULT NULL COMMENT 'the price to temporary taste'  AFTER `taste_tmp` ;