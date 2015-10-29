SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';
SET @OLD_SAFE_UPDATES=@@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;
SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Add the field 'bee_cloud_app_id' & 'bee_cloud_app_secret' to table 'restaurant'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`restaurant` 
ADD COLUMN `bee_cloud_app_id` TEXT NULL DEFAULT NULL COMMENT 'app id to bee cloud' AFTER `private_key`,
ADD COLUMN `bee_cloud_app_secret` TEXT NULL DEFAULT NULL COMMENT 'app secret to bee cloud' AFTER `bee_cloud_app_id`;

-- -----------------------------------------------------
-- Add the field 'pure_price' to table 'order'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order` 
ADD COLUMN `pure_price` FLOAT NULL DEFAULT NULL COMMENT '' AFTER `total_price`;

-- -----------------------------------------------------
-- Add the field 'pure_price' to table 'order_history'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_history` 
ADD COLUMN `pure_price` FLOAT NULL DEFAULT NULL COMMENT '' AFTER `total_price`;

-- -----------------------------------------------------
-- Add the field 'pure_price' to table 'order_archive'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_archive` 
ADD COLUMN `pure_price` FLOAT NULL DEFAULT NULL COMMENT '' AFTER `total_price`;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;



