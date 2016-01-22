SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';
SET @OLD_SAFE_UPDATES=@@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;
SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Add the field 'type' to table 'restaurant'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`restaurant` 
ADD COLUMN `type` TINYINT NULL DEFAULT 1 COMMENT 'the type as below\n1 - restaurant\n2 - group\n3 - branch' AFTER `bee_cloud_app_secret`;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`restaurant_chain`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`restaurant_chain` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`restaurant_chain` (
  `group_id` INT NOT NULL COMMENT '',
  `branch_id` INT NOT NULL COMMENT '',
  PRIMARY KEY (`group_id`, `branch_id`)  COMMENT '')
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`member_chain_discount`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`member_chain_discount` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`member_chain_discount` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '',
  `group_member_type_id` INT NULL COMMENT '',
  `branch_id` INT NULL COMMENT '',
  `discount_id` INT NULL COMMENT '',
  `type` TINYINT NULL COMMENT 'the type as below\n1 - normal\n2 - default',
  PRIMARY KEY (`id`)  COMMENT '',
  INDEX `ix_branch_id` (`branch_id` ASC)  COMMENT '')
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`member_chain_price`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`member_chain_price` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`member_chain_price` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '',
  `group_member_type_id` INT NULL COMMENT '',
  `branch_id` INT NULL COMMENT '',
  `price_plan_id` INT NULL COMMENT '',
  `type` TINYINT NULL COMMENT 'the type as below\n1 - normal\n2 - default',
  PRIMARY KEY (`id`)  COMMENT '',
  INDEX `ix_branch_id` (`branch_id` ASC)  COMMENT '')
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Add the field 'branch_id' to table 'member'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`member` 
ADD COLUMN `branch_id` INT NULL DEFAULT NULL COMMENT '' AFTER `restaurant_id`,
ADD INDEX `ix_branch_id` (`branch_id` ASC)  COMMENT '';

-- -----------------------------------------------------
-- Add the field 'branch_id' to table 'member_operation'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`member_operation` 
ADD COLUMN `branch_id` INT NULL DEFAULT NULL COMMENT '' AFTER `restaurant_id`,
ADD INDEX `ix_branch_id` (`branch_id` ASC)  COMMENT '';

-- -----------------------------------------------------
-- Add the field 'branch_id' to table 'member_operation_history'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`member_operation_history` 
ADD COLUMN `branch_id` INT NULL DEFAULT NULL COMMENT '' AFTER `restaurant_id`,
ADD INDEX `ix_branch_id` (`branch_id` ASC)  COMMENT '';

-- -----------------------------------------------------
-- Add the field 'branch_id' to table 'member_operation_archive'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`member_operation_archive` 
ADD COLUMN `branch_id` INT NULL DEFAULT NULL COMMENT '' AFTER `restaurant_id`,
ADD INDEX `ix_branch_id` (`branch_id` ASC)  COMMENT '';

-- -----------------------------------------------------
-- Add the field 'branch_id' to table 'coupon_operation'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`coupon_operation` 
ADD COLUMN `branch_id` INT NULL DEFAULT NULL COMMENT '' AFTER `restaurant_id`,
ADD INDEX `ix_branch_id` (`branch_id` ASC)  COMMENT '';

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;



