SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Add field 'service_rate' to table 'table' 
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`table` 
ADD COLUMN `service_rate` DECIMAL(3,2) NOT NULL DEFAULT 0 COMMENT 'the service rate to this table'  AFTER `status` ;

-- -----------------------------------------------------
-- Rename field 'id' to 'staff_id', 'alias_id' to 'staff_alias'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`staff` 
CHANGE COLUMN `id` `staff_id` INT(11) NOT NULL AUTO_INCREMENT  , 
CHANGE COLUMN `alias_id` `staff_alias` SMALLINT(6) NOT NULL DEFAULT '0' COMMENT 'the alias id to this stuff'  , 
DROP PRIMARY KEY ,
ADD PRIMARY KEY (`staff_id`) ;

