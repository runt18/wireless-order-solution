SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Change the field 'terminal_pin' of table 'order' to unsigned int
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order` 
CHANGE COLUMN `terminal_pin` `terminal_pin` INT(11) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the terminal pin to this order'  ;

-- -----------------------------------------------------
-- Change the field 'terminal_pin' of table 'order_history' to unsigned int
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_history` 
CHANGE COLUMN `terminal_pin` `terminal_pin` INT(11) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the terminal pin to this order'  ;