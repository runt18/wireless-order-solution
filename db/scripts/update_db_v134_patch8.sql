SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';
SET @OLD_SAFE_UPDATES=@@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;
SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- modify the table 'coupon'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`coupon` 
DROP COLUMN `order_date`,
DROP COLUMN `order_id`,
DROP COLUMN `draw_date`,
CHANGE COLUMN `status` `status` TINYINT(4) NOT NULL DEFAULT '1' COMMENT 'the status as below\n1 - 已创建\n2 - 已发放\n3 - 已使用\n4 - 已使用\n5 - 已过期' ,
ADD COLUMN `issue_date` DATETIME NULL DEFAULT NULL COMMENT '' AFTER `member_id`,
ADD COLUMN `issue_staff_id` INT NULL DEFAULT NULL COMMENT '' AFTER `issue_date`,
ADD COLUMN `issue_staff` VARCHAR(45) NULL DEFAULT NULL COMMENT '' AFTER `issue_staff_id`,
ADD COLUMN `issue_mode` TINYINT NULL DEFAULT NULL COMMENT 'the issue mode as below\n1 - 快速\n2 - ' AFTER `issue_staff`,
ADD COLUMN `issue_associate_id` INT NULL DEFAULT NULL COMMENT '' AFTER `issue_mode`,
ADD COLUMN `issue_comment` VARCHAR(45) NULL DEFAULT NULL COMMENT '' AFTER `issue_associate_id`,
ADD COLUMN `use_date` DATETIME NULL DEFAULT NULL COMMENT '' AFTER `issue_comment`,
ADD COLUMN `use_staff_id` INT NULL DEFAULT NULL COMMENT '' AFTER `use_date`,
ADD COLUMN `use_staff` VARCHAR(45) NULL DEFAULT NULL COMMENT '' AFTER `use_staff_id`,
ADD COLUMN `use_mode` TINYINT NULL DEFAULT NULL COMMENT '' AFTER `use_staff`,
ADD COLUMN `use_associate_id` INT NULL DEFAULT NULL COMMENT '' AFTER `use_mode`,
ADD COLUMN `use_comment` VARCHAR(45) NULL DEFAULT NULL COMMENT '' AFTER `use_associate_id`;

-- -----------------------------------------------------
-- Add the index the table 'coupon'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`coupon` 
ADD INDEX `ix_issue_associate_id` (`issue_associate_id` ASC)  COMMENT '',
ADD INDEX `ix_use_assocaite_id` (`use_associate_id` ASC)  COMMENT '';

-- -----------------------------------------------------
-- Modify the field 'coupon_price' to default null
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order` 
CHANGE COLUMN `coupon_price` `coupon_price` FLOAT NULL DEFAULT NULL COMMENT '' ;

-- -----------------------------------------------------
-- Drop the field 'coupon_id' to table 'order'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order` 
DROP COLUMN `coupon_id`;

-- -----------------------------------------------------
-- Add Table `wireless_order_db`.`promotion_trigger`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`promotion_trigger` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`promotion_trigger` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '',
  `promotion_id` INT NULL COMMENT '',
  `trigger_type` INT NULL COMMENT 'the trigger type as below\n1 - 微信关注',
  PRIMARY KEY (`id`)  COMMENT '',
  INDEX `ix_promotion_id` (`promotion_id` ASC)  COMMENT '')
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;



