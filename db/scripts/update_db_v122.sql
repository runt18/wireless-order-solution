SET NAMES utf8;
USE wireless_order_db;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`member_operation_history`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`member_operation_history` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`member_operation_history` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `staff_id` INT NOT NULL COMMENT 'the staff id ' ,
  `staff_name` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the staff name' ,
  `member_id` INT NOT NULL COMMENT 'the member id' ,
  `member_card_id` INT NOT NULL COMMENT 'the member card id' ,
  `member_card_alias` VARCHAR(45) NOT NULL ,
  `operate_seq` VARCHAR(45) NOT NULL COMMENT 'the format to operate seq is defined below.\n挂失YYYYMMDDHHIISS: GS20130101230000' ,
  `operate_date` DATETIME NOT NULL ,
  `operate_type` TINYINT NOT NULL COMMENT 'the operation type:\n1 - 充值\n2 - 消费\n3 - 冻结\n4 - 解冻\n5 - 换卡\n6 - 反结帐退款\n7 - 反结帐消费' ,
  `pay_type` TINYINT NULL DEFAULT NULL COMMENT '付款方式：\n现金 : 1\n刷卡 : 2\n会员 : 3\n签单：4\n挂账 ：5' ,
  `pay_money` FLOAT NULL DEFAULT NULL COMMENT 'the memory to pay' ,
  `order_id` INT UNSIGNED NULL DEFAULT NULL COMMENT 'the order id this member operation, only available in case of either consume or repaid' ,
  `charge_type` TINYINT NULL DEFAULT NULL COMMENT '充值类型：\n1 - 现金\n2 - 刷卡' ,
  `charge_money` FLOAT NULL DEFAULT NULL COMMENT 'the memory to charge' ,
  `delta_base_money` FLOAT NOT NULL DEFAULT 0 ,
  `delta_extra_money` FLOAT NOT NULL DEFAULT 0 ,
  `delta_point` INT NOT NULL DEFAULT 0 ,
  `remaining_base_money` FLOAT NOT NULL DEFAULT 0 ,
  `remaining_extra_money` FLOAT NOT NULL DEFAULT 0 ,
  `remaining_point` INT NOT NULL DEFAULT 0 ,
  `comment` VARCHAR(45) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `ix_staff_id` (`staff_id` ASC) ,
  INDEX `ix_member_id` (`member_id` ASC) ,
  INDEX `ix_member_card_id` (`member_card_id` ASC) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the member operation to history' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`member_operation`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`member_operation` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`member_operation` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `staff_id` INT NOT NULL COMMENT 'the staff id ' ,
  `staff_name` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the staff name' ,
  `member_id` INT NOT NULL COMMENT 'the member id' ,
  `member_card_id` INT NOT NULL COMMENT 'the member card id' ,
  `member_card_alias` VARCHAR(45) NOT NULL ,
  `operate_seq` VARCHAR(45) NOT NULL COMMENT 'the format to operate seq is defined below.\n挂失YYYYMMDDHHIISS: GS20130101230000' ,
  `operate_date` DATETIME NOT NULL ,
  `operate_type` TINYINT NOT NULL COMMENT 'the operation type:\n1 - 充值\n2 - 消费\n3 - 冻结\n4 - 解冻\n5 - 换卡\n6 - 反结帐退款\n7 - 反结帐消费' ,
  `pay_type` TINYINT NULL DEFAULT NULL COMMENT '付款方式：\n现金 : 1\n刷卡 : 2\n会员 : 3\n签单：4\n挂账 ：5' ,
  `pay_money` FLOAT NULL DEFAULT NULL COMMENT 'the memory to pay' ,
  `order_id` INT UNSIGNED NULL DEFAULT NULL COMMENT 'the order id this member operation, only available in case of either consume or repaid' ,
  `charge_type` TINYINT NULL DEFAULT NULL COMMENT '充值类型：\n1 - 现金\n2 - 刷卡' ,
  `charge_money` FLOAT NULL DEFAULT NULL COMMENT 'the memory to charge' ,
  `delta_base_money` FLOAT NOT NULL DEFAULT 0 ,
  `delta_extra_money` FLOAT NOT NULL DEFAULT 0 ,
  `delta_point` INT NOT NULL DEFAULT 0 ,
  `remaining_base_money` FLOAT NOT NULL DEFAULT 0 ,
  `remaining_extra_money` FLOAT NOT NULL DEFAULT 0 ,
  `remaining_point` INT NOT NULL DEFAULT 0 ,
  `comment` VARCHAR(45) NULL DEFAULT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `ix_staff_id` (`staff_id` ASC) ,
  INDEX `ix_member_id` (`member_id` ASC) ,
  INDEX `ix_member_card_id` (`member_card_id` ASC) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the member operation to today' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`client_type`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`client_type` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`client_type` (
  `client_type_id` INT NOT NULL AUTO_INCREMENT ,
  `restaurant_id` INT NOT NULL ,
  `parent_id` INT NULL COMMENT 'the parent id to this clent type' ,
  `name` VARCHAR(45) NULL ,
  PRIMARY KEY (`client_type_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the clent type' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`member_card`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`member_card` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`member_card` (
  `member_card_id` INT NOT NULL AUTO_INCREMENT ,
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `member_card_alias` VARCHAR(45) NULL DEFAULT NULL ,
  `status` TINYINT NULL DEFAULT 0 COMMENT 'the status is as below.\n0 - idle\n1 - lost\n2 - disable\n3 - in used' ,
  `comment` VARCHAR(500) NULL DEFAULT NULL COMMENT 'the comment to this member card' ,
  `last_staff_id` INT NULL DEFAULT NULL COMMENT 'the id to last modified staff' ,
  `last_mod_date` DATETIME NULL DEFAULT NULL COMMENT 'the last modified date' ,
  PRIMARY KEY (`member_card_id`) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the member card' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`member`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`member` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`member` (
  `member_id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to this member' ,
  `restaurant_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the restaurant id to this member' ,
  `member_type_id` INT NOT NULL COMMENT 'the type this member belongs to' ,
  `member_card_id` INT NULL DEFAULT NULL COMMENT 'the card this member owns' ,
  `base_balance` FLOAT NOT NULL DEFAULT 0 COMMENT 'the base balance to this member' ,
  `extra_balance` FLOAT NOT NULL DEFAULT 0 COMMENT 'the extra balance to this member' ,
  `point` INT NOT NULL DEFAULT 0 COMMENT 'the remaining point to this member' ,
  `birth_date` DATETIME NULL DEFAULT NULL ,
  `comment` VARCHAR(500) NULL DEFAULT NULL ,
  `status` TINYINT NULL DEFAULT 0 COMMENT 'the status to this member\n0 - normal\n1 - disabled' ,
  `last_mod_date` DATETIME NULL DEFAULT NULL COMMENT 'the last modified date' ,
  `last_staff_id` INT NULL DEFAULT NULL COMMENT 'the id to last modified staff' ,
  PRIMARY KEY (`member_id`) ,
  INDEX `ix_member_type_id` (`member_type_id` ASC) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) ,
  INDEX `ix_member_card_id` (`member_card_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the member' ;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`member_type`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`member_type` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`member_type` (
  `member_type_id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to member type' ,
  `restaurant_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the restaurant id to this member type' ,
  `discount_id` INT UNSIGNED NOT NULL COMMENT 'the discount id this member type uses' ,
  `discount_type` TINYINT NOT NULL DEFAULT 0 COMMENT 'the discount type is as below.\n0 - discount plan\n1 - entire ' ,
  `exchange_rate` FLOAT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the exchange rate used to transfer the price to point' ,
  `charge_rate` FLOAT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the charge rate used to transfer money to balance' ,
  `name` VARCHAR(45) NOT NULL COMMENT 'the name to this member type' ,
  `attribute` TINYINT NOT NULL DEFAULT 0 COMMENT 'the attribute to this member tye as below.\n0 - 充值属性\n1 - 积分属性\n2 - 优惠属性\n\n' ,
  PRIMARY KEY (`member_type_id`) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the member type information' ;

-- -----------------------------------------------------
-- Insert a anonymous client to each restaurant whose level belongs to reserved
-- -----------------------------------------------------
INSERT INTO wireless_order_db.client
(`restaurant_id`, `name`, `level`)
SELECT id, '匿名', 1 FROM wireless_order_db.restaurant WHERE id > 10;

-- -----------------------------------------------------
-- Drop the field 'member' in table 'order'
-- Change the field 'member_id' in table 'order'
-- Add the field 'member_operation_id' in table 'order'
-- Add the field 'settle_type' in table 'order'
-- Rename the field 'type' to 'pay_type' in table 'order'
-- Rename the field 'total_price_2' to 'actual_price' in table 'order'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order` 
DROP COLUMN `member`,
DROP COLUMN `member_id`,
ADD COLUMN `member_id` INT UNSIGNED NULL DEFAULT NULL COMMENT 'the member id to this order',
ADD COLUMN `member_operation_id` INT NULL DEFAULT NULL COMMENT 'the member operation id'  AFTER `member_id`,
ADD COLUMN `settle_type` TINYINT NOT NULL DEFAULT 1 COMMENT '结帐方式\n一般：1 (default)\n会员：2'  AFTER `waiter`,
CHANGE COLUMN `type` `pay_type` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '付款方式\n现金 : 1 (default)\n刷卡 : 2\n会员卡 : 3\n签单：4\n挂账 ：5\n',
CHANGE COLUMN `total_price_2` `actual_price` FLOAT NULL DEFAULT NULL COMMENT 'the actual total price to this order' ;

-- -----------------------------------------------------
-- Drop the field 'member' in table 'order_history'
-- Change the field 'member_id' in table 'order_history'
-- Add the field 'member_operation_id' in table 'order'
-- Add the field 'settle_type' in table 'order_history'
-- Rename the field 'type' to 'pay_type' in table 'order_history'
-- Rename the field 'total_price_2' to 'actual_price' in table 'order_history'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_history` 
DROP COLUMN `member`,
DROP COLUMN `member_id`,
ADD COLUMN `member_id` INT UNSIGNED NULL DEFAULT NULL COMMENT 'the member id to this order',
ADD COLUMN `member_operation_id` INT NULL DEFAULT NULL COMMENT 'the member operation id'  AFTER `member_id`,
ADD COLUMN `settle_type` TINYINT NOT NULL DEFAULT 1 COMMENT '结帐方式\n一般：1 (default)\n会员：2'  AFTER `waiter`,
CHANGE COLUMN `type` `pay_type` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '付款方式\n现金 : 1 (default)\n刷卡 : 2\n会员卡 : 3\n签单：4\n挂账 ：5\n',
CHANGE COLUMN `total_price_2` `actual_price` FLOAT NULL DEFAULT NULL COMMENT 'the actual total price to this order' ;