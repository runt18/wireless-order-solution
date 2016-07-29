SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';
SET @OLD_SAFE_UPDATES=@@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;
SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Add the field 'min_fans_amount' & 'max_fans_amount' to table 'member_cond'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`member_cond` 
ADD COLUMN `min_fans_amount` INT NULL DEFAULT NULL COMMENT '' AFTER `raw`,
ADD COLUMN `max_fans_amount` INT NULL DEFAULT NULL COMMENT '' AFTER `min_fans_amount`;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`represent`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`represent` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`represent` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '',
  `restaurant_id` INT NULL COMMENT '',
  `finish_date` DATETIME NULL COMMENT '',
  `title` VARCHAR(45) NULL DEFAULT NULL COMMENT '',
  `slogon` VARCHAR(45) NULL DEFAULT NULL COMMENT '',
  `oss_image_id` INT NULL DEFAULT NULL COMMENT '',
  `recommend_point` INT NULL DEFAULT NULL COMMENT '',
  `recommend_money` FLOAT NULL DEFAULT NULL COMMENT '',
  `subscribe_point` INT NULL DEFAULT NULL COMMENT '',
  `subscribe_money` FLOAT NULL DEFAULT NULL COMMENT '',
  `commission_rate` FLOAT NULL COMMENT '',
  PRIMARY KEY (`id`)  COMMENT '',
  INDEX `ix_restaurant_id` (`restaurant_id` ASC)  COMMENT '')
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`represent_chain`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`represent_chain` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`represent_chain` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '',
  `restaurant_id` INT NULL COMMENT '',
  `subscribe_date` DATETIME NULL COMMENT '',
  `recommend_member_id` INT NULL COMMENT '',
  `recommend_member` VARCHAR(45) NULL COMMENT '',
  `recommend_point` INT NULL COMMENT '',
  `recommend_money` FLOAT NULL COMMENT '',
  `subscribe_member_id` INT NULL COMMENT '',
  `subscribe_member` VARCHAR(45) NULL COMMENT '',
  `subscribe_point` INT NULL COMMENT '',
  `subscribe_money` FLOAT NULL COMMENT '',
  PRIMARY KEY (`id`)  COMMENT '',
  INDEX `ix_restaurant_id` (`restaurant_id` ASC)  COMMENT '',
  INDEX `ix_recommend_member_id` (`recommend_member_id` ASC) COMMENT '',
  INDEX `ix_subscribe_member_id` (`subscribe_member_id` ASC) COMMENT ''
  )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Insert the represent to each restaurant
-- -----------------------------------------------------
INSERT INTO `wireless_order_db`.`represent`
(`restaurant_id`)
SELECT id FROM `wireless_order_db`.`restaurant` WHERE id > 10;

-- -----------------------------------------------------
-- Add the field 'total_commission' to table 'member'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`member` 
ADD COLUMN `total_commission` FLOAT NULL DEFAULT NULL COMMENT '' AFTER `used_point`;

-- -----------------------------------------------------
-- Add the field 'alarm_amount' to table 'material'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`material` 
ADD COLUMN `alarm_amount` INT NULL DEFAULT NULL COMMENT '' AFTER `status`;

-- -----------------------------------------------------
-- Update the branch id to each coupon operation
-- -----------------------------------------------------
UPDATE `wireless_order_db`.`coupon_operation`
SET branch_id = restaurant_id;

-- -----------------------------------------------------
-- Add the field 'min_commission_amount' & 'max_commission_amount' to table 'member_cond'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`member_cond` 
ADD COLUMN `min_commission_amount` INT NULL DEFAULT NULL COMMENT '' AFTER `max_fans_amount`,
ADD COLUMN `max_commission_amount` INT NULL DEFAULT NULL COMMENT '' AFTER `min_commission_amount`;

-- -----------------------------------------------------
-- Add the field 'order_notify_template' to table 'weixin_restaurant'
-- -----------------------------------------------------
ALTER TABLE wireless_order_db.weixin_restaurant 
ADD order_notify_template varchar(100) AFTER `coupon_timeout_template`;

-- -----------------------------------------------------
-- Add the field 'default_order_type' to table 'weixin_restaurant'
-- -----------------------------------------------------
ALTER TABLE wireless_order_db.weixin_restaurant 
ADD default_order_type int(10) DEFAULT NULL;

-- -----------------------------------------------------
-- Insert the '微信客人' staff to each restaurant
-- -----------------------------------------------------
INSERT INTO `wireless_order_db`.`staff`
(`restaurant_id`, `role_id`, `name`, `tele`, `pwd`, `type`)
SELECT restaurant_id, role_id, '微信客人', tele, pwd, 3 FROM `wireless_order_db`.`staff` WHERE type = 2;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`monthly_cost`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`monthly_cost` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`monthly_cost` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '',
  `material_id` INT NULL COMMENT '',
  `monthly_balance_id` INT NULL COMMENT '',
  `cost` FLOAT NULL COMMENT '',
  PRIMARY KEY (`id`)  COMMENT '',
  INDEX `ix_material_id` (`material_id` ASC)  COMMENT '')
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Drop the table `wireless_order_db`.`monthly_balance_detail`
-- -----------------------------------------------------
DROP TABLE `wireless_order_db`.`monthly_balance_detail`;

-- -----------------------------------------------------
-- Add the field 'gift_desc' to table 'represent'
-- -----------------------------------------------------
ALTER TABLE wireless_order_db.represent ADD gift_desc varchar(500);

-- -----------------------------------------------------
-- Add the field 'total_refund' to table 'member'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`member` 
ADD COLUMN `total_refund` FLOAT NULL DEFAULT 0 AFTER `total_charge`;

-- -----------------------------------------------------
-- Update the total refund to each member
-- -----------------------------------------------------
UPDATE wireless_order_db.member M
JOIN (
	SELECT member_name, member_id, SUM(charge_money) AS refund_money 
	FROM wireless_order_db.member_operation_history 
	AND operate_type = 6
	GROUP by member_id
) AS TMP ON M.member_id = TMP.member_id
SET M.total_refund = ABS(TMP.refund_money);

-- -----------------------------------------------------
-- Add the field 'extra' to table 'print_func'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`print_func` 
ADD COLUMN `extra` INT NULL DEFAULT NULL COMMENT '' AFTER `enabled`;

-- -----------------------------------------------------
-- Add the field 'prefect_member_status' to table 'weixin_restaurant'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`weixin_restaurant` 
ADD COLUMN `prefect_member_status` INT NULL DEFAULT NULL AFTER `default_order_type`;

-- -----------------------------------------------------
-- Add the field 'extra_str' to table 'print_func'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`print_func` 
ADD COLUMN `extra_str` VARCHAR(500) NULL DEFAULT NULL COMMENT '' AFTER `extra`;

-- -----------------------------------------------------
-- Update the '暂结' extra 
-- -----------------------------------------------------
UPDATE wireless_order_db.print_func PF
JOIN wireless_order_db.printer P ON PF.printer_id = P.printer_id
JOIN wireless_order_db.weixin_restaurant WR ON WR.restaurant_id = P.restaurant_id AND WR.qrcode_status = 1
SET PF.extra = 2
WHERE PF.type = 127;

-- -----------------------------------------------------
-- Add the field 'refund_template' to table 'weixin_restaurant'
-- -----------------------------------------------------
ALTER TABLE weixin_restaurant ADD COLUMN refund_template VARCHAR(100);

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;



