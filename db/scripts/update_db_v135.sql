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




SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;



