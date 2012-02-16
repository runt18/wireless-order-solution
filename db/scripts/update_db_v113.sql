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

-- -----------------------------------------------------
-- Drop the foreign key to terminal_id in table 'staff'
-- Rename field 'id' to 'terminal_id' in table 'terminal'
-- Drop the foreign key between staff and terminal
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`staff` DROP FOREIGN KEY `fk_staff_terminal1` ;

ALTER TABLE `wireless_order_db`.`terminal` CHANGE COLUMN `id` `terminal_id` INT(11) NOT NULL AUTO_INCREMENT COMMENT 'the id to this terminal'  ,
DROP PRIMARY KEY , 
ADD PRIMARY KEY (`terminal_id`) ;

-- ALTER TABLE `wireless_order_db`.`staff` 
--  ADD CONSTRAINT `fk_staff_terminal`
--  FOREIGN KEY (`terminal_id` )
--  REFERENCES `wireless_order_db`.`terminal` (`terminal_id` )
--  ON DELETE RESTRICT
--  ON UPDATE RESTRICT;

ALTER TABLE `wireless_order_db`.`staff` 
ADD INDEX `fk_staff_terminal` (`terminal_id` ASC) ;

ALTER TABLE `wireless_order_db`.`staff` 
DROP INDEX `fk_staff_terminal1` ;