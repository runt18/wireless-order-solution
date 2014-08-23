SET @OLD_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
SET NAMES utf8;
SET SQL_SAFE_UPDATES = 0;
USE wireless_order_db;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`coupon_type`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`coupon_type` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`coupon_type` (
  `coupon_type_id` INT NOT NULL AUTO_INCREMENT,
  `restaurant_id` INT NOT NULL,
  `name` VARCHAR(45) NOT NULL,
  `price` FLOAT NOT NULL,
  `expired` DATETIME NULL DEFAULT NULL,
  `comment` VARCHAR(45) NULL DEFAULT NULL,
  `image` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`coupon_type_id`),
  INDEX `ix_restaurant_id` (`restaurant_id` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`coupon`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`coupon` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`coupon` (
  `coupon_id` INT NOT NULL AUTO_INCREMENT,
  `restaurant_id` INT NOT NULL,
  `coupon_type_id` INT NOT NULL,
  `promotion_id` INT NOT NULL,
  `birth_date` DATETIME NOT NULL,
  `member_id` INT NOT NULL,
  `order_id` INT NULL DEFAULT NULL,
  `order_date` DATETIME NULL DEFAULT NULL,
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT 'the status as below\n1 - 已创建\n2 - 已发布\n3 - 已领取\n4 - 已使用\n5 - 已过期',
  PRIMARY KEY (`coupon_id`),
  INDEX `ix_coupon_type_id` (`coupon_type_id` ASC),
  INDEX `ix_restaurant_id` (`restaurant_id` ASC),
  INDEX `ix_member_id` (`member_id` ASC),
  INDEX `ix_promotion_id` (`promotion_id` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`promotion`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`promotion` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`promotion` (
  `promotion_id` INT NOT NULL AUTO_INCREMENT,
  `restaurant_id` INT NOT NULL,
  `create_date` DATE NOT NULL,
  `start_date` DATE NOT NULL,
  `finish_date` DATE NOT NULL,
  `title` VARCHAR(45) NOT NULL,
  `body` TEXT NOT NULL,
  `type` TINYINT NOT NULL COMMENT 'the type as below\n1 - 免费领取\n2 - 单次消费满X积分\n3 - 累计消费满X积分',
  `point` INT NOT NULL DEFAULT 0,
  `status` TINYINT NOT NULL COMMENT 'the status as below.\n1 - 已创建\n2 - 已发布\n3 - 进行中\n4 - 已结束',
  `coupon_type_id` INT NOT NULL,
  PRIMARY KEY (`promotion_id`),
  INDEX `ix_restaurant_id` (`restaurant_id` ASC),
  INDEX `ix_coupon_type_id` (`coupon_type_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Adjust the status to table 'weixin_restaurant'
-- -----------------------------------------------------
UPDATE wireless_order_db.weixin_restaurant
SET status = 3
WHERE status = 2;

UPDATE wireless_order_db.weixin_restaurant
SET status = 2
WHERE status = 1;

-- -----------------------------------------------------
-- Adjust the status to table 'weixin_restaurant'
-- Change the comment to field 'status'
-- Add the field 'weixin_logo', 'weixin_info', 'app_id', 'app_secret'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`weixin_restaurant` 
CHANGE COLUMN `status` `status` TINYINT(4) NOT NULL DEFAULT 1 COMMENT 'the status as below.\n1 - 已创建\n2 - 已验证\n3 - 已绑定',
ADD COLUMN `weixin_logo` TEXT NULL DEFAULT NULL AFTER `status`,
ADD COLUMN `weixin_info` TEXT NULL DEFAULT NULL AFTER `weixin_logo`,
ADD COLUMN `app_id` VARCHAR(100) NULL DEFAULT NULL AFTER `weixin_info`,
ADD COLUMN `app_secret` VARCHAR(100) NULL DEFAULT NULL AFTER `app_id`;

-- -----------------------------------------------------
-- Insert the weixin_restaurant record to each restaurant
-- -----------------------------------------------------
INSERT INTO wireless_order_db.weixin_restaurant
(restaurant_id)
SELECT id FROM wireless_order_db.restaurant 
WHERE id NOT IN(
	SELECT restaurant_id FROM wireless_order_db.weixin_restaurant
)
AND id > 10;

-- -----------------------------------------------------
-- Move logo & info from table 'weixin_misc' to 'weixin_restaurant'
-- -----------------------------------------------------
UPDATE wireless_order_db.weixin_restaurant WR
JOIN wireless_order_db.weixin_misc WM ON WR.restaurant_id = WM.restaurant_id
SET WR.weixin_logo = WM.weixin_logo, WR.weixin_info = WM.weixin_info;

-- -----------------------------------------------------
-- Drop the table 'weixin_misc'
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`weixin_misc`;

-- -----------------------------------------------------
-- Drop the table 'weixin_image'
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`weixin_image`;

SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;



