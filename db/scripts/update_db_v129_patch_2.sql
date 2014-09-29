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
  `oss_image_id` INT NOT NULL AUTO_INCREMENT,
  `restaurant_id` INT NOT NULL,
  `image` VARCHAR(100) NOT NULL,
  `image_crc` INT UNSIGNED NOT NULL,
  `type` TINYINT NOT NULL,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'the status as below.' /* comment truncated */ /*1 - single
2 - married*/,
  `associated_id` INT NOT NULL,
  `associated_serial` VARCHAR(45) NULL,
  `associated_serial_crc` INT UNSIGNED NULL,
  `last_modified` DATETIME NOT NULL,
  PRIMARY KEY (`oss_image_id`),
  INDEX `ix_restaurant_id` (`restaurant_id` ASC),
  INDEX `ix_associated_id` (`associated_id` ASC),
  INDEX `ix_associated_serial_crc` (`associated_serial_crc` ASC),
  INDEX `ix_image_crc` (`image_crc` ASC))
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

-- -----------------------------------------------------
-- Insert the food image to oss image.
-- -----------------------------------------------------
INSERT INTO wireless_order_db.oss_image
(restaurant_id, image, image_crc, type, status, associated_id, last_modified)
SELECT restaurant_id, img, CRC32(img), 4, 2, food_id, NOW() FROM wireless_order_db.food WHERE img IS NOT NULL AND LENGTH(TRIM(img)) <> 0;

-- -----------------------------------------------------
-- Drop the filed 'img' & 'stock_status' to table 'food'
-- Add the field 'oss_image_id' to table 'food'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`food` 
DROP COLUMN `taste_ref_type`,
DROP COLUMN `img`,
DROP COLUMN `stock_status`,
ADD COLUMN `oss_image_id` INT NULL DEFAULT NULL AFTER `status`,
ADD INDEX `ix_oss_image_id` (`oss_image_id` ASC);

-- -----------------------------------------------------
-- Update the field 'oss_image_id' of table 'food' which are matched with those in table 'oss_image'
-- -----------------------------------------------------
UPDATE wireless_order_db.food F
JOIN wireless_order_db.oss_image OI ON F.food_id = OI.associated_id AND OI.type = 4
SET F.oss_image_id = OI.oss_image_id;

SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;



