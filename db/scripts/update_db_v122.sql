SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`client`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`client` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`client` (
  `client_id` INT NOT NULL AUTO_INCREMENT COMMENT 'thie id to this client' ,
  `restaurant_id` INT UNSIGNED NOT NULL COMMENT 'the restaurant id to this client' ,
  `client_type_id` INT NULL DEFAULT NULL COMMENT 'the type this client belongs to' ,
  `name` VARCHAR(45) NOT NULL COMMENT 'the name to this client' ,
  `sex` TINYINT NOT NULL DEFAULT 0 COMMENT 'the sex to this client' ,
  `birth_date` DATETIME NULL DEFAULT NULL COMMENT 'the birth date to client' ,
  `tele` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the telephone to this client' ,
  `mobile` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the mobile to this client' ,
  `birthday` DATE NULL DEFAULT NULL COMMENT 'the birthday to this client' ,
  `id_card` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the id card to this client' ,
  `company` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the company to this client' ,
  `taste_pref` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the taste preference to client' ,
  `taboo` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the taboo to client' ,
  `contact_addr` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the contact address to client' ,
  `comment` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the comment to client' ,
  `level` TINYINT NULL DEFAULT 0 COMMENT 'the status to client.\n0 - normal\n1 - anonymous' ,
  `last_staff_id` INT NULL DEFAULT NULL COMMENT 'the id to last modifed staff' ,
  PRIMARY KEY (`client_id`) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) ,
  INDEX `ix_client_type_id` (`client_type_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the client' ;


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
-- Add the field 'settle_type' in table 'order'
-- Rename the field 'type' to 'pay_type' in table 'order'
-- Rename the field 'total_price_2' to 'actual_price' in table 'order'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order` 
DROP COLUMN `member`,
DROP COLUMN `member_id`,
ADD COLUMN `member_id` INT UNSIGNED NULL DEFAULT NULL COMMENT 'the member id to this order',
ADD COLUMN `settle_type` TINYINT NOT NULL DEFAULT 1 COMMENT '结帐方式\n一般：1 (default)\n会员：2'  AFTER `waiter`,
CHANGE COLUMN `type` `pay_type` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '付款方式\n现金 : 1 (default)\n刷卡 : 2\n会员卡 : 3\n签单：4\n挂账 ：5\n',
CHANGE COLUMN `total_price_2` `actual_price` FLOAT NULL DEFAULT NULL COMMENT 'the actual total price to this order' ;

-- -----------------------------------------------------
-- Drop the field 'member' in table 'order_history'
-- Change the field 'member_id' in table 'order_history'
-- Add the field 'settle_type' in table 'order_history'
-- Rename the field 'type' to 'pay_type' in table 'order_history'
-- Rename the field 'total_price_2' to 'actual_price' in table 'order_history'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_history` 
DROP COLUMN `member`,
DROP COLUMN `member_id`,
ADD COLUMN `member_id` INT UNSIGNED NULL DEFAULT NULL COMMENT 'the member id to this order',
ADD COLUMN `settle_type` TINYINT NOT NULL DEFAULT 1 COMMENT '结帐方式\n一般：1 (default)\n会员：2'  AFTER `waiter`,
CHANGE COLUMN `type` `pay_type` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '付款方式\n现金 : 1 (default)\n刷卡 : 2\n会员卡 : 3\n签单：4\n挂账 ：5\n',
CHANGE COLUMN `total_price_2` `actual_price` FLOAT NULL DEFAULT NULL COMMENT 'the actual total price to this order' ;