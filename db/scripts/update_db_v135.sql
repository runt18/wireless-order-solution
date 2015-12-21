SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';
SET @OLD_SAFE_UPDATES=@@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;
SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Add the field 'payment_template' & 'coupon_draw_template' & 'coupon_timeout_template' to table 'weixin_restaurant'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`weixin_restaurant` 
ADD COLUMN `payment_template` VARCHAR(100) NULL DEFAULT NULL COMMENT '' AFTER `refresh_token`,
ADD COLUMN `coupon_draw_template` VARCHAR(100) NULL DEFAULT NULL COMMENT '' AFTER `payment_template`,
ADD COLUMN `coupon_timeout_template` VARCHAR(100) NULL DEFAULT NULL COMMENT '' AFTER `coupon_draw_template`;

-- -----------------------------------------------------
-- Add the field 'table_id' to table 'weixin_order'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`weixin_order` 
ADD COLUMN `table_id` INT NULL DEFAULT NULL COMMENT '' AFTER `address`;

-- -----------------------------------------------------
-- Add the field 'min_last_consumption' & 'max_last_consumption' to table 'member_cond'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`member_cond` 
ADD COLUMN `min_last_consumption` INT NULL DEFAULT NULL COMMENT '' AFTER `max_balance`,
ADD COLUMN `max_last_consumption` INT NULL DEFAULT NULL COMMENT '' AFTER `min_last_consumption`;


-- -----------------------------------------------------
-- Add the field 'food_unit_id' to table 'weixin_order_food'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`weixin_order_food` 
ADD COLUMN `food_unit_id` INT NULL DEFAULT NULL COMMENT '' AFTER `food_count`;

-- -----------------------------------------------------
-- Add the field 'member_id' to table 'weixin_order'
-- Drop the field 'weixin_serial_crc' & 'weixin_serial' from table 'weixin_order'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`weixin_order` 
DROP COLUMN `weixin_serial_crc`,
DROP COLUMN `weixin_serial`,
ADD COLUMN `member_id` INT NOT NULL DEFAULT 0 COMMENT '' AFTER `restaurant_id`,
ADD INDEX `ix_member_id` (`member_id` ASC)  COMMENT '',
DROP INDEX `ix_weixin_serial_crc` ;

-- -----------------------------------------------------
-- Add the field 'wx_order_amount' to table 'member'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`member` 
ADD COLUMN `wx_order_amount` INT NULL DEFAULT NULL COMMENT '' AFTER `referrer_id`;

-- -----------------------------------------------------
-- Add the field 'comment' to table 'weixin_order'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`weixin_order` 
ADD COLUMN `comment` VARCHAR(45) NULL DEFAULT NULL COMMENT '' AFTER `table_id`;

-- -----------------------------------------------------
-- Add the field 'charge_template' to table 'weixin_restaurant'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`weixin_restaurant` 
ADD COLUMN `charge_template` VARCHAR(100) NULL DEFAULT NULL COMMENT '' AFTER `coupon_timeout_template`;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;



