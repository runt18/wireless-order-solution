SET @OLD_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
SET NAMES utf8;
SET SQL_SAFE_UPDATES = 0;
USE wireless_order_db;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`role_price_plan`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`role_price_plan` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`role_price_plan` (
  `role_id` INT NOT NULL,
  `price_plan_id` INT NOT NULL,
  PRIMARY KEY (`role_id`, `price_plan_id`))
ENGINE = InnoDB;

-- -----------------------------------------------------
-- Insert the '转菜' privilege code
-- -----------------------------------------------------
INSERT INTO wireless_order_db.privilege 
(pri_code, cate) VALUES (1009, 1);

-- -----------------------------------------------------
-- Select the privilege id to '转菜'
-- -----------------------------------------------------
SELECT @privilege_id := pri_id FROM wireless_order_db.privilege WHERE pri_code = 1009;

-- -----------------------------------------------------
-- Assign '转菜' privilege to each admin
-- -----------------------------------------------------
INSERT INTO wireless_order_db.role_privilege
(role_id, pri_id, restaurant_id)
SELECT role_id, @privilege_id, restaurant_id FROM wireless_order_db.role WHERE restaurant_id > 10 AND cate = 1;

-- -----------------------------------------------------
-- Assign '转菜' privilege to each boss
-- -----------------------------------------------------
INSERT INTO wireless_order_db.role_privilege
(role_id, pri_id, restaurant_id)
SELECT role_id, @privilege_id, restaurant_id FROM wireless_order_db.role WHERE restaurant_id > 10 AND cate = 2;

-- -----------------------------------------------------
-- Assign '转菜' privilege to each finance
-- -----------------------------------------------------
INSERT INTO wireless_order_db.role_privilege
(role_id, pri_id, restaurant_id)
SELECT role_id, @privilege_id, restaurant_id FROM wireless_order_db.role WHERE restaurant_id > 10 AND cate = 3;

-- -----------------------------------------------------
-- Assign '转菜' privilege to each manager
-- -----------------------------------------------------
INSERT INTO wireless_order_db.role_privilege
(role_id, pri_id, restaurant_id)
SELECT role_id, @privilege_id, restaurant_id FROM wireless_order_db.role WHERE restaurant_id > 10 AND cate = 4;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`pay_type`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`pay_type` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`pay_type` (
  `pay_type_id` INT NOT NULL AUTO_INCREMENT,
  `restaurant_id` INT NOT NULL,
  `name` VARCHAR(45) NOT NULL,
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT 'the type as below\n1 - normal\n2 - reserved',
  PRIMARY KEY (`pay_type_id`),
  INDEX `ix_restaurant_id` (`restaurant_id` ASC))
ENGINE = InnoDB;

-- -----------------------------------------------------
-- Insert the pay type '现金', '刷卡', '签单', '挂账'
-- -----------------------------------------------------
INSERT INTO wireless_order_db.pay_type
(pay_type_id, restaurant_id, name, type) VALUES
(1, 0, '现金', 2);

INSERT INTO wireless_order_db.pay_type
(pay_type_id, restaurant_id, name, type) VALUES
(2, 0, '刷卡', 2);

-- -----------------------------------------------------
-- Insert the pay type '会员卡'
-- -----------------------------------------------------
INSERT INTO wireless_order_db.pay_type
(pay_type_id, restaurant_id, name, type) VALUES
(3, 0, '会员卡', 3);

INSERT INTO wireless_order_db.pay_type
(pay_type_id, restaurant_id, name, type) VALUES
(4, 0, '签单', 2);

INSERT INTO wireless_order_db.pay_type
(pay_type_id, restaurant_id, name, type) VALUES
(5, 0, '挂账', 2);

-- -----------------------------------------------------
-- Insert the pay type '混合结账'
-- -----------------------------------------------------
INSERT INTO wireless_order_db.pay_type
(pay_type_id, restaurant_id, name, type) VALUES
(100, 0, '混合结账', 4);

-- -----------------------------------------------------
-- Rename the filed 'pay_type' to 'pay_type_id' in table 'member_operation_history' & 'member_operation'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`member_operation_history` 
CHANGE COLUMN `pay_type` `pay_type_id` TINYINT(4) NULL DEFAULT NULL COMMENT '付款方式：\n现金 : 1\n刷卡 : 2\n会员 : 3\n签单：4\n挂账 ：5' ;

ALTER TABLE `wireless_order_db`.`member_operation` 
CHANGE COLUMN `pay_type` `pay_type_id` TINYINT(4) NULL DEFAULT NULL COMMENT '付款方式：\n现金 : 1\n刷卡 : 2\n会员 : 3\n签单：4\n挂账 ：5' ;

-- -----------------------------------------------------
-- Rename the filed 'pay_type' to 'pay_type_id' in table 'order_history' & 'order'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order` 
CHANGE COLUMN `pay_type` `pay_type_id` TINYINT(4) NOT NULL DEFAULT '1' COMMENT '付款方式\n现金 : 1 (default)\n刷卡 : 2\n会员卡 : 3\n签单：4\n挂账 ：5\n' ;

ALTER TABLE `wireless_order_db`.`order_history` 
CHANGE COLUMN `pay_type` `pay_type_id` TINYINT(4) NOT NULL DEFAULT '1' COMMENT '付款方式\n现金 : 1 (default)\n刷卡 : 2\n会员卡 : 3\n签单：4\n挂账 ：5\n' ;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`mixed_payment`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`mixed_payment` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`mixed_payment` (
  `order_id` INT NOT NULL,
  `pay_type_id` INT NOT NULL,
  `price` FLOAT NOT NULL,
  PRIMARY KEY (`order_id`, `pay_type_id`))
ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`mixed_payment_history`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`mixed_payment_history` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`mixed_payment_history` (
  `order_id` INT NOT NULL,
  `pay_type_id` INT NOT NULL,
  `price` FLOAT NOT NULL,
  PRIMARY KEY (`order_id`, `pay_type_id`))
ENGINE = InnoDB;

-- -----------------------------------------------------
-- Add the field 'operation' to table 'order_food'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food` 
CHANGE COLUMN `discount` `discount` FLOAT NOT NULL DEFAULT 1 COMMENT 'the discount to this food' ,
ADD COLUMN `operation` TINYINT NOT NULL DEFAULT 1 COMMENT 'the operation as below.\n1 - 加菜\n2 - 退菜\n3 - 转菜' AFTER `order_id`;

-- -----------------------------------------------------
-- Add the field 'operation' to table 'order_food_history'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food_history` 
CHANGE COLUMN `discount` `discount` FLOAT NOT NULL DEFAULT 1 COMMENT 'the discount to this food' ,
ADD COLUMN `operation` TINYINT NOT NULL DEFAULT 1 COMMENT 'the operation as below.\n1 - 加菜\n2 - 退菜\n3 - 转菜' AFTER `order_id`;

-- -----------------------------------------------------
-- Add the field 'operation' to 'cancel' from table 'order_food' & 'order_food_history'
-- -----------------------------------------------------
UPDATE wireless_order_db.order_food SET operation = 2
WHERE order_count < 0;
UPDATE wireless_order_db.order_food_history SET operation = 2
WHERE order_count < 0;

SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;



