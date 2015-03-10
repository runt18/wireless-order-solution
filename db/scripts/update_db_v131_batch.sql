SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';
SET @OLD_SAFE_UPDATES=@@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;
SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Add the field 'temp_paid_staff' & 'temp_paid_date' to table 'order'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order` 
ADD COLUMN `temp_staff` VARCHAR(45) NULL DEFAULT NULL AFTER `member_id`,
ADD COLUMN `temp_date` DATETIME NULL DEFAULT NULL AFTER `temp_paid_staff`;

-- -----------------------------------------------------
-- Add the field 'coupon_id' to table 'order'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order` 
ADD COLUMN `coupon_id` INT NULL DEFAULT NULL AFTER `table_name`;

-- -----------------------------------------------------
-- Add the index 'ix_order_date' to table 'order_history'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_history` 
ADD INDEX `ix_order_date` (`order_date` ASC);

-- -----------------------------------------------------
-- Table `wireless_order_db`.`billboard`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`billboard` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`billboard` (
  `billboard_id` INT NOT NULL AUTO_INCREMENT,
  `restaurant_id` INT NULL DEFAULT NULL,
  `title` VARCHAR(45) NOT NULL,
  `body` TEXT NULL DEFAULT NULL,
  `created` DATETIME NOT NULL,
  `expired` DATETIME NOT NULL,
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT 'the type as below.\n1 - system\n2 - restaurant',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1 - created\n2 - read',
  PRIMARY KEY (`billboard_id`),
  INDEX `ix_restaurant_id` (`restaurant_id` ASC))
ENGINE = InnoDB;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;



