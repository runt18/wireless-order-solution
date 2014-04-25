SET @OLD_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
SET NAMES utf8;
SET SQL_SAFE_UPDATES = 0;
USE wireless_order_db;

-- -----------------------------------------------------
-- Insert the '微信' privilege whose code is 7000
-- -----------------------------------------------------
INSERT INTO wireless_order_db.privilege
(pri_code, cate) VALUES (7000, 7);

-- -----------------------------------------------------
-- Assign the 'weixin' privilege to admin, boss, financier of each restaurant
-- -----------------------------------------------------
INSERT INTO wireless_order_db.role_privilege
(role_id, pri_id, restaurant_id)
SELECT role_id, pri_id, restaurant_id
FROM wireless_order_db.role R, 
wireless_order_db.privilege PRI
WHERE R.cate IN (1, 2, 3)
AND PRI.pri_code = 7000;

-- -----------------------------------------------------
-- Insert the '短信' privilege whose code is 7000
-- -----------------------------------------------------
INSERT INTO wireless_order_db.privilege
(pri_code, cate) VALUES (8000, 8);

-- -----------------------------------------------------
-- Assign the 'sms' privilege to admin, boss, financier of each restaurant
-- -----------------------------------------------------
INSERT INTO wireless_order_db.role_privilege
(role_id, pri_id, restaurant_id)
SELECT role_id, pri_id, restaurant_id
FROM wireless_order_db.role R, 
wireless_order_db.privilege PRI
WHERE R.cate IN (1, 2, 3)
AND PRI.pri_code = 8000;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`module`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`module` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`module` (
  `module_id` INT NOT NULL AUTO_INCREMENT,
  `code` INT NOT NULL COMMENT 'the code as below.\n1 - basic\n2 - member\n3 - inventory\n4 - sms',
  `cate` INT NOT NULL COMMENT 'the category as below.\n1 - basic\n2 - member\n3 - inventory\n4 - sms',
  PRIMARY KEY (`module_id`),
  INDEX `ix_code` (`code` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`restaurant_module`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`restaurant_module` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`restaurant_module` (
  `restaurant_id` INT NOT NULL,
  `module_id` INT NOT NULL,
  PRIMARY KEY (`restaurant_id`, `module_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Insert the modules
-- -----------------------------------------------------
INSERT INTO wireless_order_db.module
(module_id, code, cate) VALUES 
-- Basic
(1, 1000, 1), 
-- Member
(2, 2000, 2),
-- Inventory
(3, 3000, 3),
-- SMS 
(4, 4000, 4);

-- -----------------------------------------------------
-- Assign the basic module to each restaurant
-- -----------------------------------------------------
INSERT INTO wireless_order_db.restaurant_module
(restaurant_id, module_id) 
SELECT id, 1 FROM wireless_order_db.restaurant WHERE id > 10;

-- -----------------------------------------------------
-- Drop the table 'food_price_plan' & 'price_plan'
-- -----------------------------------------------------
DROP TABLE `wireless_order_db`.`food_price_plan`;
DROP TABLE `wireless_order_db`.`price_plan`;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`sms_stat`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`sms_stat` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`sms_stat` (
  `restaurant_id` INT NOT NULL,
  `total_used` INT NOT NULL DEFAULT 0,
  `verification_used` INT NOT NULL DEFAULT 0,
  `consumption_used` INT NOT NULL DEFAULT 0,
  `charge_used` INT NOT NULL DEFAULT 0,
  `remaining` INT NOT NULL DEFAULT 0,
  PRIMARY KEY (`restaurant_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`sms_detail`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`sms_detail` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`sms_detail` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `restaurant_id` INT NOT NULL,
  `modified` DATETIME NOT NULL,
  `operation` SMALLINT NOT NULL COMMENT 'the type as below.\n1 - use to verification\n2 - use to consumption\n3 - use to charge\n4 - add\n5 - deduct',
  `delta` INT NOT NULL,
  `remaining` INT NOT NULL,
  `staff_id` INT NOT NULL DEFAULT 0,
  `staff_name` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `ix_restaurant_id` (`restaurant_id` ASC),
  INDEX `ix_staff_id` (`staff_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Insert the SMS state to each restaurant
-- -----------------------------------------------------
INSERT INTO wireless_order_db.sms_stat
(restaurant_id)
SELECT id FROM wireless_order_db.restaurant WHERE id > 10;

-- -----------------------------------------------------
-- Change the field 'id' to unsinged int
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food` 
CHANGE COLUMN `id` `id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'the id to this order detail record' ;
ALTER TABLE `wireless_order_db`.`order_food_history` 
CHANGE COLUMN `id` `id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'the id to this order detail record' ;

-- -----------------------------------------------------
-- Assign the member module to '利雅苑'，'A.R.G'，'淡仔渔村'，'柏拉图'
-- -----------------------------------------------------
INSERT INTO wireless_order_db.restaurant_module
(restaurant_id, module_id) VALUES
-- liyy
(40, 2), 
-- A.R.G
(64, 2),
-- dzyc
(62, 2),
-- blt
(63, 2);

SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;



