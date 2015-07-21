SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';
SET @OLD_SAFE_UPDATES=@@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;
SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Add the field 'qrcode_status' to table 'weixin_restaurant'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`weixin_restaurant` 
ADD COLUMN `qrcode_status` INT NULL DEFAULT 1 AFTER `qrcode`;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`daily_settle_archive`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`daily_settle_archive`;
CREATE TABLE IF NOT EXISTS `wireless_order_db`.`daily_settle_archive` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT 'the id to each shift record',
  `restaurant_id` INT(10) UNSIGNED NOT NULL,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name of the operator to perform daily settle',
  `on_duty` DATETIME NULL DEFAULT NULL COMMENT 'the datetime to be on duty',
  `off_duty` DATETIME NULL DEFAULT NULL COMMENT 'the datetime to be off duty',
  PRIMARY KEY (`id`),
  INDEX `ix_restaurant_id` (`restaurant_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'the daily settle history to each restaurant';


-- -----------------------------------------------------
-- Table `wireless_order_db`.`member_operation_archive`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`member_operation_archive`;
CREATE TABLE IF NOT EXISTS `wireless_order_db`.`member_operation_archive` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `restaurant_id` INT(10) UNSIGNED NOT NULL,
  `staff_id` INT(11) NOT NULL COMMENT 'the staff id ',
  `staff_name` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the staff name',
  `member_id` INT(11) NOT NULL COMMENT 'the member id',
  `member_name` VARCHAR(45) NOT NULL COMMENT 'the member name',
  `member_mobile` VARCHAR(45) NOT NULL COMMENT 'the member mobile',
  `member_card` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the member card id',
  `operate_seq` VARCHAR(45) NOT NULL COMMENT 'the format to operate seq is defined below.\n挂失YYYYMMDDHHIISS: GS20130101230000',
  `operate_date` DATETIME NOT NULL,
  `operate_type` TINYINT(4) NOT NULL COMMENT 'the operation type:\n1 - 充值\n2 - 消费\n3 - 积分消费\n4 - 积分调整\n5 - 充值调整',
  `pay_type_id` TINYINT(4) NULL DEFAULT NULL COMMENT '付款方式：\n现金 : 1\n刷卡 : 2\n会员 : 3\n签单：4\n挂账 ：5',
  `pay_money` FLOAT NULL DEFAULT NULL COMMENT 'the memory to pay',
  `coupon_id` INT(11) NULL DEFAULT NULL,
  `coupon_money` FLOAT NULL DEFAULT NULL,
  `coupon_name` VARCHAR(45) NULL DEFAULT NULL,
  `order_id` INT(10) UNSIGNED NULL DEFAULT NULL COMMENT 'the order id this member operation, only available in case of either consume or repaid',
  `charge_type` TINYINT(4) NULL DEFAULT NULL COMMENT '充值类型：\n1 - 现金\n2 - 刷卡',
  `charge_money` FLOAT NULL DEFAULT NULL COMMENT 'the memory to charge',
  `delta_base_money` FLOAT NOT NULL DEFAULT '0',
  `delta_extra_money` FLOAT NOT NULL DEFAULT '0',
  `delta_point` INT(11) NOT NULL DEFAULT '0',
  `remaining_base_money` FLOAT NOT NULL DEFAULT '0',
  `remaining_extra_money` FLOAT NOT NULL DEFAULT '0',
  `remaining_point` INT(11) NOT NULL DEFAULT '0',
  `comment` VARCHAR(45) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `ix_staff_id` (`staff_id` ASC),
  INDEX `ix_member_id` (`member_id` ASC),
  INDEX `ix_restaurant_id` (`restaurant_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'describe the member operation to history';


-- -----------------------------------------------------
-- Table `wireless_order_db`.`mixed_payment_archive`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`mixed_payment_archive`;
CREATE TABLE IF NOT EXISTS `wireless_order_db`.`mixed_payment_archive` (
  `order_id` INT(11) NOT NULL,
  `pay_type_id` INT(11) NOT NULL,
  `price` FLOAT NOT NULL,
  PRIMARY KEY (`order_id`, `pay_type_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`normal_taste_group_archive`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`normal_taste_group_archive`;
CREATE TABLE IF NOT EXISTS `wireless_order_db`.`normal_taste_group_archive` (
  `normal_taste_group_id` INT(11) NOT NULL,
  `taste_id` INT(11) NOT NULL,
  PRIMARY KEY (`normal_taste_group_id`, `taste_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'describe the relationship between taste group and its normal';
-- -----------------------------------------------------
-- Insert a empty recrod to 'normal_taste_group_archive'
-- -----------------------------------------------------
INSERT INTO wireless_order_db.normal_taste_group_archive (`normal_taste_group_id`, `taste_id`) VALUES (1, 0);

-- -----------------------------------------------------
-- Table `wireless_order_db`.`order_food_archive`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`order_food_archive`;
CREATE TABLE IF NOT EXISTS `wireless_order_db`.`order_food_archive` (
  `id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'the id to this order detail record',
  `order_id` INT(10) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the order id this order food belongs to',
  `operation` TINYINT(4) NOT NULL DEFAULT '1' COMMENT 'the operation as below.\n1 - 加菜\n2 - 退菜\n3 - 转菜',
  `restaurant_id` INT(10) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the restaurant id to this order detail',
  `food_id` INT(11) NULL DEFAULT NULL COMMENT 'the id to this food',
  `food_unit_id` INT(11) NULL DEFAULT NULL,
  `food_unit` VARCHAR(45) NULL DEFAULT NULL,
  `food_unit_price` FLOAT NULL DEFAULT NULL,
  `order_count` FLOAT NOT NULL DEFAULT '0' COMMENT 'the count that the waiter ordered. the count can be positive or negative.',
  `commission` FLOAT NOT NULL DEFAULT '0' COMMENT 'commission to the food',
  `order_date` DATETIME NOT NULL DEFAULT '1900-01-01 00:00:00',
  `unit_price` FLOAT UNSIGNED NOT NULL DEFAULT '0',
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name to the ordered food',
  `discount` FLOAT NOT NULL DEFAULT '1' COMMENT 'the discount to this food',
  `kitchen_id` INT(11) NULL DEFAULT NULL COMMENT 'the kitchen id which the order food of this record belong to. ',
  `dept_id` TINYINT(3) UNSIGNED NULL DEFAULT NULL COMMENT 'the department alias id to this record',
  `waiter` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name of waiter who deal with this record',
  `staff_id` INT(11) NOT NULL DEFAULT '0',
  `is_temporary` TINYINT(4) NOT NULL DEFAULT '0' COMMENT 'indicates whether the food to this record is temporary',
  `is_paid` TINYINT(4) NOT NULL DEFAULT '0' COMMENT 'indicates whether this record is occurred before order has been paid or not',
  `is_gift` TINYINT(4) NOT NULL DEFAULT '0' COMMENT 'indicates the order food is gift',
  `food_status` SMALLINT(5) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'indicates the status to this food, the value is the combination of values below.\n特价菜 ：0x01\n推荐菜 ：0x02\n停售　 ：0x04\n赠送     ：0x08\n时价     ：0x10\n套菜     ：0x20\n热销     ：0x40\n称重     ：0x80',
  `taste_group_id` INT(11) NOT NULL DEFAULT '1' COMMENT 'the taste group id to this order food',
  `cancel_reason_id` INT(11) NULL DEFAULT '1' COMMENT 'the cancel reason id to this order food',
  `cancel_reason` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the cancel reason description this order food',
  PRIMARY KEY (`id`),
  INDEX `ix_food_id` (`food_id` ASC),
--  INDEX `ix_taste_group_id` (`taste_group_id` ASC),
--  INDEX `ix_cancel_reason_id` (`cancel_reason_id` ASC),
  INDEX `ix_order_id` (`order_id` ASC),
  INDEX `ix_restaurant_id` (`restaurant_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'descirbe the relationship between the order and food';


-- -----------------------------------------------------
-- Table `wireless_order_db`.`order_archive`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`order_archive`;
CREATE TABLE IF NOT EXISTS `wireless_order_db`.`order_archive` (
  `id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'the id to order',
  `seq_id` INT(10) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the sequence id to this order',
  `restaurant_id` INT(10) UNSIGNED NOT NULL COMMENT 'external key associated with the  restaurant table',
  `birth_date` DATETIME NULL DEFAULT NULL COMMENT 'the birth date to this order',
  `order_date` DATETIME NULL DEFAULT NULL COMMENT 'the end date to this order',
  `cancel_price` FLOAT NOT NULL DEFAULT '0' COMMENT 'the cancel price to this order',
  `discount_price` FLOAT NOT NULL DEFAULT '0' COMMENT 'the discount price to this order',
  `erase_price` INT(10) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the erase price to this order',
  `total_price` FLOAT NULL DEFAULT NULL COMMENT 'The total price to this order.\nIts default value is NULL, means the order not be paid, otherwise means the order has been paid.',
  `repaid_price` FLOAT NOT NULL DEFAULT '0' COMMENT 'the repaid price to this order',
  `custom_num` TINYINT(3) UNSIGNED NOT NULL DEFAULT '0',
  `discount_date` DATETIME NULL DEFAULT NULL,
  `discount_staff` VARCHAR(45) NULL DEFAULT NULL,
  `discount_staff_id` INT(11) NULL DEFAULT NULL,
  `waiter` VARCHAR(45) NOT NULL COMMENT 'the waiter who operates on this order',
  `staff_id` INT(11) NOT NULL DEFAULT '0',
  `settle_type` TINYINT(4) NOT NULL DEFAULT '1' COMMENT '结帐方式\n一般：1 (default)\n会员：2',
  `pay_type_id` INT(11) NOT NULL DEFAULT '1' COMMENT '付款方式\n现金 : 1 (default)\n刷卡 : 2\n会员卡 : 3\n签单：4\n挂账 ：5\n',
  `region_id` TINYINT(3) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the region id to this order',
  `region_name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the region name to this order',
  `gift_price` FLOAT NOT NULL DEFAULT '0',
  `coupon_price` FLOAT NOT NULL DEFAULT '0',
  `actual_price` FLOAT NULL DEFAULT NULL COMMENT 'the actual total price to this order',
  `category` TINYINT(4) NOT NULL DEFAULT '1' COMMENT 'the category to this order, it should be one the values below.\n一般 : 1\n外卖 : 2\n并台 : 3\n拼台 : 4',
  `comment` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the comment to this order',
  `table_id` INT(11) NOT NULL DEFAULT '0' COMMENT 'the table id to this order',
  `table_alias` SMALLINT(6) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the table alias id to this order',
  `table_name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the table name to this order',
  `service_rate` DECIMAL(3,2) NOT NULL DEFAULT '0.00' COMMENT 'the service rate to this order',
  `status` TINYINT(4) NOT NULL DEFAULT '0' COMMENT 'the status to this order is as below.\n0 - unpaid\n1 - paid\n2 - repaid',
  PRIMARY KEY (`id`),
  INDEX `ix_order_history_restaurant` USING BTREE (`restaurant_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'This table preserves all the order records.';


-- -----------------------------------------------------
-- Table `wireless_order_db`.`payment_archive`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`payment_archive`;
CREATE TABLE IF NOT EXISTS `wireless_order_db`.`payment_archive` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `restaurant_id` INT(11) NOT NULL,
  `staff_id` INT(11) NOT NULL,
  `staff_name` VARCHAR(45) NULL DEFAULT NULL,
  `on_duty` DATETIME NOT NULL,
  `off_duty` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `ix_restaurant_id` (`restaurant_id` ASC),
  INDEX `ix_staff_id` (`staff_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`daily_settle_archive`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`daily_settle_archive`;
CREATE TABLE IF NOT EXISTS `wireless_order_db`.`daily_settle_archive` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT 'the id to each daily record',
  `restaurant_id` INT(10) UNSIGNED NOT NULL,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name of the operator to shift',
  `on_duty` DATETIME NULL DEFAULT NULL COMMENT 'the datetime to be on duty',
  `off_duty` DATETIME NULL DEFAULT NULL COMMENT 'the datetime to be off duty',
  PRIMARY KEY (`id`),
  INDEX `ix_restaurant_id` (`restaurant_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'the daily history to each restaurant';

-- -----------------------------------------------------
-- Table `wireless_order_db`.`shift_archive`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`shift_archive`;
CREATE TABLE IF NOT EXISTS `wireless_order_db`.`shift_archive` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT 'the id to each shift record',
  `restaurant_id` INT(10) UNSIGNED NOT NULL,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name of the operator to shift',
  `on_duty` DATETIME NULL DEFAULT NULL COMMENT 'the datetime to be on duty',
  `off_duty` DATETIME NULL DEFAULT NULL COMMENT 'the datetime to be off duty',
  PRIMARY KEY (`id`),
  INDEX `ix_restaurant_id` (`restaurant_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'the shift history to each restaurant';


-- -----------------------------------------------------
-- Table `wireless_order_db`.`taste_group_archive`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`taste_group_archive`;
CREATE TABLE IF NOT EXISTS `wireless_order_db`.`taste_group_archive` (
  `taste_group_id` INT(11) NOT NULL AUTO_INCREMENT,
  `normal_taste_group_id` INT(11) NOT NULL DEFAULT '1',
  `normal_taste_pref` VARCHAR(45) NULL DEFAULT NULL,
  `normal_taste_price` DECIMAL(7,2) UNSIGNED NULL DEFAULT NULL,
  `tmp_taste_id` INT(11) NULL DEFAULT NULL,
  `tmp_taste_pref` VARCHAR(45) NULL DEFAULT NULL,
  `tmp_taste_price` DECIMAL(7,2) UNSIGNED NULL DEFAULT NULL,
  PRIMARY KEY (`taste_group_id`),
  INDEX `ix_normal_taste_group_id` (`normal_taste_group_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'describe the taste group';
-- -----------------------------------------------------
-- Insert a empty recrod to 'taste_group_archive'
-- -----------------------------------------------------
INSERT INTO wireless_order_db.taste_group_archive (`taste_group_id`, `normal_taste_group_id`) VALUES (1, 1);

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;



