SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Modify the `region` table
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`region` 
DROP COLUMN `id` ,
CHANGE COLUMN `alias_id` `region_id` TINYINT(3) UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the alias id to this table region'  , 
DROP PRIMARY KEY , 
ADD PRIMARY KEY (`restaurant_id`, `region_id`) , 
DROP INDEX `fk_region_restaurant1` , 
ADD INDEX `fk_region_restaurant` (`restaurant_id` ASC) ;

-- -----------------------------------------------------
-- Modify the `table` table
-- 1、rename the field "region" to "region_id"
-- 2、add the field "custom_num"
-- 3、add the field "category"
-- 4、add the field "status"
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`table` 
ADD COLUMN `custom_num` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the amount of customer to this table if the status is not idle'  AFTER `enabled` , 
ADD COLUMN `category` TINYINT NOT NULL DEFAULT 1 COMMENT 'the category to this table, it should be one the values below.\n一般 : 1\n外卖 : 2\n并台 : 3\n拼台 : 4'  AFTER `custom_num` , 
ADD COLUMN `status` TINYINT NOT NULL DEFAULT 0 COMMENT 'the status to this table, one of the values below.\n空闲 : 0\n就餐 : 1\n预定 : 2'  AFTER `category` , 
CHANGE COLUMN `minimum_cost` `minimum_cost` DECIMAL(7,2) NOT NULL DEFAULT '0.00' COMMENT 'the minimum cost to this table'  AFTER `name` , 
CHANGE COLUMN `id` `id` INT(11) NOT NULL AUTO_INCREMENT  , CHANGE COLUMN `region` `region_id` TINYINT(3) UNSIGNED NOT NULL DEFAULT '255' COMMENT 'the region alias id to this table.'  ;