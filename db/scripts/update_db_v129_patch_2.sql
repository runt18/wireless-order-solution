SET @OLD_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
SET NAMES utf8;
SET SQL_SAFE_UPDATES = 0;
USE wireless_order_db;

-- -----------------------------------------------------
-- Add the field 'draw_date' to table 'coupon'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`coupon` 
ADD COLUMN `draw_date` DATETIME NULL AFTER `member_id`;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`oss_image`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`oss_image` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`oss_image` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `restaurant_id` INT NOT NULL,
  `image` VARCHAR(100) NOT NULL,
  `type` TINYINT NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'the status as below.\n1 - single\n2 - married',
  `associated_id` INT NOT NULL,
  `associated_serial` VARCHAR(45) NULL,
  `associated_serial_crc` INT UNSIGNED NULL,
  `last_modified` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `ix_restaurant_id` (`restaurant_id` ASC),
  INDEX `ix_associated_id` (`associated_id` ASC),
  INDEX `ix_associated_serial_crc` (`associated_serial_crc` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Drop the filed 'image' to table 'coupon_type'
-- Add the field 'oss_image_id' to table 'coupon_type'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`coupon_type` 
DROP COLUMN `image`,
ADD COLUMN `oss_image_id` INT NULL DEFAULT NULL AFTER `expired`,
ADD INDEX `ix_oss_image_id` (`oss_image_id` ASC);

SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;



