SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';
SET @OLD_SAFE_UPDATES=@@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;
SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Add the field 'food_unit' & 'food_unit_id' to table 'combo_order_food'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`combo_order_food` 
ADD COLUMN `food_unit` VARCHAR(45) NULL DEFAULT NULL AFTER `food_amount`,
ADD COLUMN `food_unit_id` INT NULL DEFAULT NULL AFTER `food_unit`;

-- -----------------------------------------------------
-- Add the field 'limit_remaing' & 'limit_amount' to table 'food'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`food` 
ADD COLUMN `limit_amount` INT NULL DEFAULT NULL AFTER `status`,
ADD COLUMN `limit_remaing` INT NULL DEFAULT NULL AFTER `limit_amount`;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;



