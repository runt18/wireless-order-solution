SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';
SET @OLD_SAFE_UPDATES=@@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;
SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`take_out_address`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`take_out_address` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`take_out_address` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `member_id` INT NOT NULL,
  `name` VARCHAR(45) NOT NULL,
  `address` VARCHAR(100) NOT NULL,
  `tele` VARCHAR(45) NOT NULL,
  `last_used` DATETIME NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `member_id` (`member_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Add the field 'address' & 'address_id' to table 'wx_order'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`weixin_order` 
ADD COLUMN `address_id` INT NULL DEFAULT NULL AFTER `code`,
ADD COLUMN `address` VARCHAR(100) NULL DEFAULT NULL AFTER `address_id`,
ADD INDEX `ix_address_id` (`address_id` ASC);

-- -----------------------------------------------------
-- Add the field 'category' to table 'table'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`table` 
ADD COLUMN `category` TINYINT NOT NULL DEFAULT 1 COMMENT 'the category to table as below\n1 - normal\n2 - take out\n3 - join' AFTER `region_id`;

-- -----------------------------------------------------
-- Modify the field 'region_id' to table 'table'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`table` 
CHANGE COLUMN `region_id` `region_id` TINYINT(3) UNSIGNED NULL DEFAULT NULL COMMENT 'the region alias id to this table.' ;

-- -----------------------------------------------------
-- Update the field 'table_id' of table 'order_history'
-- -----------------------------------------------------
UPDATE wireless_order_db.order_history OH
JOIN wireless_order_db.table T ON OH.restaurant_id = T.restaurant_id AND OH.table_alias = T.table_alias
SET OH.table_id = T.table_id;

-- -----------------------------------------------------
-- Add the field 'public_key' & 'private_key' to table 'restaurant'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`restaurant` 
ADD COLUMN `public_key` TEXT NULL DEFAULT NULL AFTER `dianping_id`,
ADD COLUMN `private_key` TEXT NULL DEFAULT NULL AFTER `public_key`;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`token`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`token` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`token` (
  `token_id` INT NOT NULL AUTO_INCREMENT,
  `restaurant_id` INT NOT NULL,
  `birth_date` DATETIME NOT NULL,
  `last_modified` DATETIME NOT NULL,
  `code` INT NULL,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'the status to token\n1 - code\n2 - token',
  PRIMARY KEY (`token_id`),
  INDEX `ix_restaurant_id` (`restaurant_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`food_unit`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`food_unit` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`food_unit` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `food_id` INT NOT NULL,
  `price` FLOAT NOT NULL,
  `unit` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `ix_food_id` (`food_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Add the field 'member_id' to table `wireless_order_db`.`order`
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order` 
ADD COLUMN `member_id` INT NULL DEFAULT 0 AFTER `status`;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;



