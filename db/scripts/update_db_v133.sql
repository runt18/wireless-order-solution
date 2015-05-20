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

-- -----------------------------------------------------
-- Add the field 'enabled' to table 'print_func'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`print_func` 
ADD COLUMN `enabled` TINYINT NOT NULL DEFAULT 1 AFTER `comment`;

-- -----------------------------------------------------
-- Add the feast kitchen to each department
-- -----------------------------------------------------
INSERT INTO wireless_order_db.kitchen
(`restaurant_id`, `dept_id`, `name`, `type`, `display_id`, `is_allow_temp`)
SELECT restaurant_id, dept_id, CONCAT(name, "酒席费") AS kitchen_name, 4 AS kitchen_type, 0 AS kitchen_display, 0 AS allow_temp FROM wireless_order_db.department 
WHERE 1 = 1 
AND type IN( 0, 1 ) ;

-- -----------------------------------------------------
-- Add the field 'comment' to table 'print_func'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`print_func` 
ADD COLUMN `comment` VARCHAR(100) NULL DEFAULT NULL AFTER `type`;

-- -----------------------------------------------------
-- Add the field 'qrcode_url', 'nick_name', 'head_img_url', 'refresh_token' to table 'weixin_restaurant'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`weixin_restaurant` 
ADD COLUMN `qrcode_url` VARCHAR(200) NULL DEFAULT NULL AFTER `app_secret`,
ADD COLUMN `nick_name` VARCHAR(45) NULL DEFAULT NULL AFTER `qrcode_url`,
ADD COLUMN `head_img_url` VARCHAR(200) NULL DEFAULT NULL AFTER `nick_name`,
ADD COLUMN `refresh_token` VARCHAR(200) NULL DEFAULT NULL AFTER `head_img_url`;

-- -----------------------------------------------------
-- Add the field 'qrcode' to table 'weixin_restaurant'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`weixin_restaurant` 
ADD COLUMN `qrcode` VARCHAR(100) NULL DEFAULT NULL AFTER `qrcode_url`;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;



