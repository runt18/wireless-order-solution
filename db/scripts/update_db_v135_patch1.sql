SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';
SET @OLD_SAFE_UPDATES=@@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;
SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`order_food_price`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`order_food_price` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`order_food_price` (
  `order_food_id` INT NOT NULL COMMENT '',
  `food_unit` VARCHAR(45) NULL DEFAULT NULL COMMENT '',
  `food_unit_price` FLOAT NULL DEFAULT NULL COMMENT '',
  `plan_price` FLOAT NULL DEFAULT NULL COMMENT '',
  PRIMARY KEY (`order_food_id`)  COMMENT '')
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Add the field 'plan_price' to table 'order_food'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food` 
ADD COLUMN `plan_price` FLOAT NULL DEFAULT NULL COMMENT '' AFTER `commission`;

-- -----------------------------------------------------
-- Add the field 'plan_price' to table 'order_food_history'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food_history` 
ADD COLUMN `plan_price` FLOAT NULL DEFAULT NULL COMMENT '' AFTER `commission`;

-- -----------------------------------------------------
-- Add the field 'plan_price' to table 'order_food_archive'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food_archive` 
ADD COLUMN `plan_price` FLOAT NULL DEFAULT NULL COMMENT '' AFTER `commission`;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;



