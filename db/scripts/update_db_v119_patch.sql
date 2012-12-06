SET NAMES utf8;
USE wireless_order_db;

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
-- Table `wireless_order_db`.`member_card`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`member_card` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`member_card` (
  `member_card_id` INT NOT NULL AUTO_INCREMENT ,
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `member_card_alias` VARCHAR(45) NULL DEFAULT NULL ,
  `status` TINYINT NULL DEFAULT 0 COMMENT 'the status is as below.\n0 - normal\n1 - lost' ,
  `comment` VARCHAR(500) NULL DEFAULT NULL COMMENT 'the comment to this member card' ,
  `last_staff_id` INT NULL DEFAULT NULL COMMENT 'the id to last modified staff' ,
  `last_mod_date` DATETIME NULL DEFAULT NULL COMMENT 'the last modified date' ,
  PRIMARY KEY (`member_card_id`) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the member card' ;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`client_member`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`client_member` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`client_member` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `client_id` INT NOT NULL COMMENT 'the id to client' ,
  `member_id` INT NOT NULL COMMENT 'the id to member' ,
  `restaurant_id` INT NOT NULL COMMENT 'the id to restaurant' ,
  PRIMARY KEY (`id`) ,
  INDEX `ix_client_id` (`client_id` ASC) ,
  INDEX `ix_memeber_id` (`member_id` ASC) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the relationship between member and client' ;

-- -----------------------------------------------------
-- Modify table 'order' as below.
-- Add field 'gift_price' and 'cancel_price'.
-- Change data type of field 'total_price', 'total_price_2' and 'gift_price' to FLOAT
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order` 
ADD COLUMN `cancel_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the cancel price to this order'  AFTER `gift_price` , 
ADD COLUMN `discount_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the discount price to this order'  AFTER `cancel_price`;
ALTER TABLE `wireless_order_db`.`order` 
CHANGE COLUMN `gift_price` `gift_price` FLOAT NOT NULL DEFAULT 0  AFTER `region_name` , 
CHANGE COLUMN `total_price` `total_price` FLOAT NULL DEFAULT NULL COMMENT 'The total price to this order.\nIts default value is NULL, means the order not be paid, otherwise means the order has been paid.'  AFTER `discount_price` , 
CHANGE COLUMN `table_id` `table_id` INT(11) NOT NULL DEFAULT '0' COMMENT 'the table id to this order'  AFTER `comment` , 
CHANGE COLUMN `table_alias` `table_alias` SMALLINT(6) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the table alias id to this order'  AFTER `table_id` , 
CHANGE COLUMN `total_price_2` `total_price_2` FLOAT NULL DEFAULT NULL COMMENT 'the actual total price to this order'  ;



-- -----------------------------------------------------
-- Modify table 'order' as below.
-- Add field 'gift_price' and 'cancel_price'.
-- Change data type of field 'total_price', 'total_price_2' and 'gift_price' to FLOAT
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_history` 
ADD COLUMN `cancel_price` FLOAT NOT NULL DEFAULT 0  AFTER `gift_price` , 
ADD COLUMN `discount_price` FLOAT NOT NULL DEFAULT 0  AFTER `cancel_price`;
ALTER TABLE `wireless_order_db`.`order_history` 
CHANGE COLUMN `gift_price` `gift_price` FLOAT NOT NULL DEFAULT 0  AFTER `region_name` , 
CHANGE COLUMN `erase_price` `erase_price` INT(10) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the erase price to this order'  AFTER `discount_price` , 
CHANGE COLUMN `total_price` `total_price` FLOAT NULL DEFAULT NULL COMMENT 'The total price to this order.\nIts default value is NULL, means the order not be paid, otherwise means the order has been paid.'  AFTER `erase_price` , 
CHANGE COLUMN `table_id` `table_id` INT(11) NOT NULL DEFAULT '0' COMMENT 'the table id to this order'  AFTER `comment` , 
CHANGE COLUMN `total_price_2` `total_price_2` FLOAT NULL DEFAULT NULL COMMENT 'the actual total price to this order'  ;

-- -----------------------------------------------------
-- Update the field 'discount_price' to table 'order'.
-- -----------------------------------------------------
UPDATE wireless_order_db.order O,
(
SELECT order_id, 
       ROUND(SUM(unit_price * order_count * (1 - discount)), 2) AS discount_price FROM wireless_order_db.order_food 
WHERE 1 = 1
AND discount < 1 
GROUP BY order_id
) AS DS
SET O.discount_price = DS.discount_price
WHERE O.id = DS.order_id AND O.total_price IS NOT NULL;

-- -----------------------------------------------------
-- Update the field 'cancel_price' to table 'order'.
-- -----------------------------------------------------
UPDATE wireless_order_db.order_history O,
(
SELECT order_id,
       ABS(ROUND(SUM(unit_price * order_count), 2)) AS cancel_price FROM wireless_order_db.order_food
WHERE 1 = 1
AND order_count < 0
GROUP BY order_id
) AS CS
SET O.cancel_price = CS.cancel_price
WHERE O.id = CS.order_id AND O.total_price IS NOT NULL;

-- -----------------------------------------------------
-- Update the field 'discount_price' to table 'order_history'.
-- -----------------------------------------------------
UPDATE wireless_order_db.order_history OH,
(
SELECT order_id, 
       ROUND(SUM(unit_price * order_count * (1 - discount)), 2) AS discount_price FROM wireless_order_db.order_food_history 
WHERE 1 = 1
AND discount < 1 
GROUP BY order_id
) AS DS
SET OH.discount_price = DS.discount_price
WHERE OH.id = DS.order_id;

-- -----------------------------------------------------
-- Update the field 'cancel_price' to table 'order_history'.
-- -----------------------------------------------------
UPDATE wireless_order_db.order_history OH,
(
SELECT order_id,
       ABS(ROUND(SUM(unit_price * order_count), 2)) AS cancel_price FROM wireless_order_db.order_food_history 
WHERE 1 = 1
AND order_count < 0
GROUP BY order_id
) AS CS
SET OH.cancel_price = CS.cancel_price
WHERE OH.id = CS.order_id;
