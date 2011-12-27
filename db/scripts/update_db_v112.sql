SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Modify the `region` table
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`region` 

DROP COLUMN `id` 

, CHANGE COLUMN `alias_id` `region_id` TINYINT(3) UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the alias id to this table region'  

, DROP PRIMARY KEY 

, ADD PRIMARY KEY (`restaurant_id`, `region_id`) 

, DROP INDEX `fk_region_restaurant1` 

, ADD INDEX `fk_region_restaurant` (`restaurant_id` ASC) ;

-- -----------------------------------------------------
-- Modify the `table` table
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`table` CHANGE COLUMN `region` `region_id` TINYINT(3) UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the region alias id to this table. '  ;