SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Drop the foreign key 'restaurant_id' from table 'material_detail'
-- Add the field 'food_id' to 'material_detail'
-- Add the index to field 'restauant_id'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`material_detail` DROP FOREIGN KEY `fk_material_detail_restaurant` ;

ALTER TABLE `wireless_order_db`.`material_detail` 

ADD COLUMN `food_id` INT NOT NULL DEFAULT 0 COMMENT 'the food id that this material detail belongs to'  AFTER `material_id` ,

CHANGE COLUMN `restaurant_id` `restaurant_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the restaurant id that this material detial belongs to' 
, 

ADD INDEX `ix_restaurant_id` (`restaurant_id` ASC) , 

DROP INDEX `fk_material_detail_restaurant` ;

