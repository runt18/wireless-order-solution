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
-- Add field 'gift_price', 'cancel_price' and 'repaid_price'.
-- Change data type of field 'total_price', 'total_price_2' and 'gift_price' to FLOAT
-- Replace the field 'is_paid' with 'status'
-- Drop field 'table2_id', 'table2_alias', 'table2_name'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order` 
ADD COLUMN `cancel_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the cancel price to this order'  AFTER `gift_price` , 
ADD COLUMN `discount_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the discount price to this order'  AFTER `cancel_price`,
ADD COLUMN `repaid_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the repaid price to this order'  AFTER `discount_price`,
ADD COLUMN `price_plan_id` INT DEFAULT 0 COMMENT 'the price plan id this order uses'  AFTER `discount_id`;

ALTER TABLE `wireless_order_db`.`order` 
CHANGE COLUMN `gift_price` `gift_price` FLOAT NOT NULL DEFAULT 0  AFTER `region_name` , 
CHANGE COLUMN `total_price` `total_price` FLOAT NULL DEFAULT NULL COMMENT 'The total price to this order.\nIts default value is NULL, means the order not be paid, otherwise means the order has been paid.'  AFTER `discount_price` , 
CHANGE COLUMN `table_id` `table_id` INT(11) NOT NULL DEFAULT '0' COMMENT 'the table id to this order'  AFTER `comment` , 
CHANGE COLUMN `table_alias` `table_alias` SMALLINT(6) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the table alias id to this order'  AFTER `table_id` , 
CHANGE COLUMN `total_price_2` `total_price_2` FLOAT NULL DEFAULT NULL COMMENT 'the actual total price to this order'  ;

ALTER TABLE `wireless_order_db`.`order` 
CHANGE COLUMN `is_paid` `status` TINYINT(4) NOT NULL DEFAULT '0' COMMENT 'the status to this order is as below.\n0 - unpaid\n1 - paid\n2 - repaid'  ;

ALTER TABLE `wireless_order_db`.`order` 
DROP COLUMN `table2_id` ,
DROP COLUMN `table2_alias` ,
DROP COLUMN `table2_name`  ;

-- -----------------------------------------------------
-- Modify table 'order_food' as below.
-- Change data type of field 'unit_price', 'order_count' to FLOAT
-- Change the comment to field 'is_paid'
-- Drop the field 'comment'
-- Add the field 'cancel_reason_id' & 'cancel_reason'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food` 
CHANGE COLUMN `order_count` `order_count` FLOAT NOT NULL DEFAULT '0.00' COMMENT 'the count that the waiter ordered. the count can be positive or negative.'  ,CHANGE COLUMN `unit_price` `unit_price` FLOAT UNSIGNED NOT NULL DEFAULT '0.00'  , 
CHANGE COLUMN `is_paid` `is_paid` TINYINT(4) NOT NULL DEFAULT '0' COMMENT 'indicates whether this record is occurred before order has been paid or not' ,
DROP COLUMN `comment` , 
ADD COLUMN `cancel_reason_id` INT NULL DEFAULT 1 COMMENT 'the cancel reason id to this order food'  AFTER `taste_group_id` , 
ADD COLUMN `cancel_reason` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the cancel reason description this order food'  AFTER `cancel_reason_id` ,
ADD INDEX `ix_cancel_reason_id` (`cancel_reason_id` ASC)  ;

-- -----------------------------------------------------
-- Modify table 'order_history' as below.
-- Add field 'gift_price', 'cancel_price' and 'repaid_price'.
-- Change data type of field 'total_price', 'total_price_2' and 'gift_price' to FLOAT
-- Replace the field 'is_paid' with 'status'
-- Drop field 'table2_id', 'table2_alias', 'table2_name'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_history` 
ADD COLUMN `cancel_price` FLOAT NOT NULL DEFAULT 0  COMMENT 'the cancel price to this order'AFTER `gift_price` , 
ADD COLUMN `discount_price` FLOAT NOT NULL DEFAULT 0  COMMENT 'the discount price to this order'AFTER `cancel_price`,
ADD COLUMN `repaid_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the repaid price to this order'  AFTER `discount_price` ;

ALTER TABLE `wireless_order_db`.`order_history` 
CHANGE COLUMN `gift_price` `gift_price` FLOAT NOT NULL DEFAULT 0  AFTER `region_name` , 
CHANGE COLUMN `erase_price` `erase_price` INT(10) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the erase price to this order'  AFTER `discount_price` , 
CHANGE COLUMN `total_price` `total_price` FLOAT NULL DEFAULT NULL COMMENT 'The total price to this order.\nIts default value is NULL, means the order not be paid, otherwise means the order has been paid.'  AFTER `erase_price` , 
CHANGE COLUMN `table_id` `table_id` INT(11) NOT NULL DEFAULT '0' COMMENT 'the table id to this order'  AFTER `comment` , 
CHANGE COLUMN `total_price_2` `total_price_2` FLOAT NULL DEFAULT NULL COMMENT 'the actual total price to this order'  ;

ALTER TABLE `wireless_order_db`.`order_history` 
CHANGE COLUMN `is_paid` `status` TINYINT(4) NOT NULL DEFAULT '0' COMMENT 'the status to this order is as below.\n0 - unpaid\n1 - paid\n2 - repaid'  ;

ALTER TABLE `wireless_order_db`.`order_history` 
DROP COLUMN `table2_id` ,
DROP COLUMN `table2_alias` ,
DROP COLUMN `table2_name`  ;

-- -----------------------------------------------------
-- Modify table 'order_food_history' as below.
-- Change data type of field 'unit_price', 'order_count' to FLOAT
-- Change the comment to field 'is_paid'
-- Drop the field 'comment'
-- Add the field 'cancel_reason_id' & 'cancel_reason'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food_history` 
CHANGE COLUMN `order_count` `order_count` FLOAT NOT NULL DEFAULT '0.00' COMMENT 'the count that the waiter ordered. the count can be positive or negative.'  ,CHANGE COLUMN `unit_price` `unit_price` FLOAT UNSIGNED NOT NULL DEFAULT '0.00'  ,
CHANGE COLUMN `is_paid` `is_paid` TINYINT(4) NOT NULL DEFAULT '0' COMMENT 'indicates whether this record is occurred before order has been paid or not'  ,
DROP COLUMN `comment` , 
ADD COLUMN `cancel_reason_id` INT NULL DEFAULT 1 COMMENT 'the cancel reason id to this order food'  AFTER `taste_group_id` , 
ADD COLUMN `cancel_reason` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the cancel reason description this order food'  AFTER `cancel_reason_id` ,
ADD INDEX `ix_cancel_reason_id` (`cancel_reason_id` ASC)  ;

-- -----------------------------------------------------
-- Update the field 'discount_price' to table 'order'.
-- -----------------------------------------------------
UPDATE wireless_order_db.order O,
(
SELECT OF.order_id, 
       ROUND(SUM((unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * order_count * (1 - discount)), 2) AS discount_price 
FROM 
wireless_order_db.order_food OF JOIN wireless_order_db.taste_group TG ON OF.taste_group_id = TG.taste_group_id
WHERE 1 = 1
AND OF.discount < 1 
GROUP BY OF.order_id
) AS DS
SET O.discount_price = DS.discount_price
WHERE O.id = DS.order_id AND O.total_price IS NOT NULL;

-- -----------------------------------------------------
-- Update the field 'cancel_price' to table 'order'.
-- -----------------------------------------------------
UPDATE wireless_order_db.order_history O,
(
SELECT OF.order_id,
       ABS(ROUND(SUM((unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * order_count * OF.discount), 2)) AS cancel_price 
FROM 
wireless_order_db.order_food OF JOIN wireless_order_db.taste_group TG ON OF.taste_group_id = TG.taste_group_id
WHERE 1 = 1
AND OF.order_count < 0
GROUP BY OF.order_id
) AS CS
SET O.cancel_price = CS.cancel_price
WHERE O.id = CS.order_id AND O.total_price IS NOT NULL;

-- -----------------------------------------------------
-- Update the field 'repaid_price' to table 'order'.
-- -----------------------------------------------------
UPDATE wireless_order_db.order O,
(
SELECT
OF.order_id,
ROUND(SUM((unit_price + IFNULL(TG.normal_taste_price, 0) + IFNULL(TG.tmp_taste_price, 0)) * order_count * OF.discount), 2) AS repaid_price
FROM
wireless_order_db.order_food OF
JOIN wireless_order_db.taste_group TG
ON OF.taste_group_id = TG.taste_group_id
WHERE
OF.is_paid = 1
GROUP BY OF.order_id
) AS RS
SET O.repaid_price = RS.repaid_price
WHERE O.id = RS.order_id;

-- -----------------------------------------------------
-- Update the field 'discount_price' to table 'order_history'.
-- -----------------------------------------------------
UPDATE wireless_order_db.order_history OH,
(
SELECT OFH.order_id, 
       ROUND(SUM((unit_price + IFNULL(TGH.normal_taste_price, 0) + IFNULL(TGH.tmp_taste_price, 0)) * order_count * (1 - discount)), 2) AS discount_price 
FROM 
wireless_order_db.order_food_history OFH JOIN wireless_order_db.taste_group_history TGH ON OFH.taste_group_id = TGH.taste_group_id
WHERE 1 = 1
AND OFH.discount < 1 
GROUP BY OFH.order_id
) AS DS
SET OH.discount_price = DS.discount_price
WHERE OH.id = DS.order_id;

-- -----------------------------------------------------
-- Update the field 'cancel_price' to table 'order_history'.
-- -----------------------------------------------------
UPDATE wireless_order_db.order_history OH,
(
SELECT OFH.order_id,
       ABS(ROUND(SUM((unit_price + IFNULL(TGH.normal_taste_price, 0) + IFNULL(TGH.tmp_taste_price, 0)) * order_count * OFH.discount), 2)) AS cancel_price 
FROM 
wireless_order_db.order_food_history OFH JOIN wireless_order_db.taste_group_history TGH ON OFH.taste_group_id = TGH.taste_group_id
WHERE 1 = 1
AND OFH.order_count < 0
GROUP BY OFH.order_id
) AS CS
SET OH.cancel_price = CS.cancel_price
WHERE OH.id = CS.order_id;

-- -----------------------------------------------------
-- Update the field 'repaid_price' to table 'order_history'.
-- -----------------------------------------------------
UPDATE wireless_order_db.order_history OH,
(
SELECT
OFH.order_id,
ROUND(SUM((unit_price + IFNULL(TGH.normal_taste_price, 0) + IFNULL(TGH.tmp_taste_price, 0)) * order_count * OFH.discount), 2) AS repaid_price
FROM
wireless_order_db.order_food_history OFH
JOIN wireless_order_db.taste_group_history TGH
ON OFH.taste_group_id = TGH.taste_group_id
WHERE
OFH.is_paid = 1
GROUP BY OFH.order_id
) AS RS
SET OH.repaid_price = RS.repaid_price
WHERE OH.id = RS.order_id;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`cancel_reason`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`cancel_reason` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`cancel_reason` (
  `cancel_reason_id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to this cancel reason' ,
  `reason` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the description to this cancel reason' ,
  `restaurant_id` INT UNSIGNED NULL DEFAULT 0 COMMENT 'the restaurant id this cancel reason belongs to' ,
  PRIMARY KEY (`cancel_reason_id`) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the cancel reason' ;

INSERT INTO wireless_order_db.cancel_reason (`cancel_reason_id`, `reason`) VALUES (1, '无原因');

-- -----------------------------------------------------
-- Update the field 'status' to paid in table 'order'.
-- -----------------------------------------------------
UPDATE wireless_order_db.order
SET status = 1
WHERE total_price IS NOT NULL;

-- -----------------------------------------------------
-- Update the field 'status' to re-paid in table 'order'.
-- -----------------------------------------------------
UPDATE 
wireless_order_db.order O,
(
    SELECT order_id FROM wireless_order_db.order_food
    GROUP BY order_id
    HAVING SUM(is_paid) > 0
) AS RO
SET O.status = 2
WHERE O.id = RO.order_id;

-- -----------------------------------------------------
-- Update the field 'status' to paid in table 'order_history'.
-- -----------------------------------------------------
UPDATE wireless_order_db.order_history
SET status = 1
WHERE total_price IS NOT NULL;

-- -----------------------------------------------------
-- Update the field 'status' to re-paid in table 'order'.
-- -----------------------------------------------------
UPDATE 
wireless_order_db.order_history O,
(
    SELECT order_id FROM wireless_order_db.order_food_history
    GROUP BY order_id
    HAVING SUM(is_paid) > 0
) AS RO
SET O.status = 2
WHERE O.id = RO.order_id;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`price_plan`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`price_plan` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`price_plan` (
  `price_plan_id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to this food price plan' ,
  `restaurant_id` INT UNSIGNED NOT NULL COMMENT 'the restaurant id this food price plan belongs to' ,
  `name` VARCHAR(45) NOT NULL COMMENT 'the name to this food price plan' ,
  `status` TINYINT NULL DEFAULT 0 COMMENT 'the status to price plan is as below.\n0 - normal\n1 - reserved' ,
  PRIMARY KEY (`price_plan_id`) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the general information to food price plan' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`food_price_plan`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`food_price_plan` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`food_price_plan` (
  `price_plan_id` INT NOT NULL ,
  `food_id` INT NOT NULL ,
  `unit_price` FLOAT NOT NULL DEFAULT 0 ,
  `restaurant_id` INT UNSIGNED NOT NULL DEFAULT 0 ,
  PRIMARY KEY (`price_plan_id`, `food_id`) ,
  INDEX `ix_food_id` (`food_id` ASC) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the food unit price to a specific plan' ;

-- -----------------------------------------------------
-- Insert a price plan to each restaurant
-- -----------------------------------------------------
INSERT INTO wireless_order_db.price_plan
(`restaurant_id`, `name`, `status`)
SELECT id, '价格方案1', 1 FROM wireless_order_db.restaurant WHERE id > 10;

-- -----------------------------------------------------
-- Insert food price and its corresponding plan to each food
-- -----------------------------------------------------
INSERT INTO wireless_order_db.food_price_plan
(`price_plan_id`, `food_id`, `unit_price`, `restaurant_id`)
SELECT PP.price_plan_id, F.food_id, F.unit_price, F.restaurant_id FROM 
wireless_order_db.food F
JOIN wireless_order_db.price_plan PP ON F.restaurant_id = PP.restaurant_id;

-- -----------------------------------------------------
-- Drop the field 'unit_price' to table 'food'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`food` 
DROP COLUMN `unit_price`;

-- -----------------------------------------------------
-- Update the field 'price_plan_id' to table 'order'
-- -----------------------------------------------------
UPDATE wireless_order_db.order O, wireless_order_db.price_plan PP 
SET O.price_plan_id = PP.price_plan_id
WHERE O.restaurant_id = PP.restaurant_id;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`order_group`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`order_group` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`order_group` (
  `order_id` INT UNSIGNED NULL ,
  `sub_order_id` INT UNSIGNED NULL ,
  `restaurant_id` INT UNSIGNED NULL ,
  PRIMARY KEY (`order_id`, `sub_order_id`) ,
  INDEX `ix_sub_order_id` (`sub_order_id` ASC) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the general order group information' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`sub_order`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`sub_order` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`sub_order` (
  `order_id` INT NOT NULL COMMENT 'the order id to this sub order' ,
  `table_id` INT NOT NULL COMMENT 'the table id to this sub order' ,
  `table_name` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the table name to this sub order' ,
  `cancel_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the cancel price to this sub order' ,
  `gift_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the gift price to this sub order' ,
  `discount_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the discount price to this sub order' ,
  `erase_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the erase price to this sub order' ,
  `total_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the total price to this sub order' ,
  `actual_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the actual price to this sub order' ,
  PRIMARY KEY (`order_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the information to sub order' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`order_group_history`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`order_group_history` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`order_group_history` (
  `order_id` INT UNSIGNED NULL ,
  `sub_order_id` INT UNSIGNED NULL ,
  `restaurant_id` INT UNSIGNED NULL ,
  PRIMARY KEY (`order_id`, `sub_order_id`) ,
  INDEX `ix_sub_order_id` (`sub_order_id` ASC) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the general order group history information' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`sub_order_history`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`sub_order_history` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`sub_order_history` (
  `order_id` INT NOT NULL COMMENT 'the order id to this sub order' ,
  `table_id` INT NOT NULL COMMENT 'the table id to this sub order' ,
  `table_name` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the table name to this sub order' ,
  `cancel_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the cancel price to this sub order' ,
  `gift_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the gift price to this sub order' ,
  `discount_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the discount price to this sub order' ,
  `erase_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the erase price to this sub order' ,
  `total_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the total price to this sub order' ,
  `actual_price` FLOAT NOT NULL DEFAULT 0 COMMENT 'the actual price to this sub order' ,
  PRIMARY KEY (`order_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the information to sub order history' ;

-- -----------------------------------------------------
-- Drop the foreign key on 'fk_order_food_order'
-- Add the index 'ix_order_id' on field 'order_id' 
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food` DROP FOREIGN KEY `fk_order_food_order` ;
ALTER TABLE `wireless_order_db`.`order_food` 
CHANGE COLUMN `order_id` `order_id` INT(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the order id this order food belongs to'  , 
ADD INDEX `ix_order_id` (`order_id` ASC) ;
-- -----------------------------------------------------
-- Drop the foreign key on 'fk_order_food_history_order_history1'
-- Add the index 'ix_order_id' on field 'order_id'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food_history`
DROP FOREIGN KEY `fk_order_food_history_order_history1` ;
ALTER TABLE `wireless_order_db`.`order_food_history` 
CHANGE COLUMN `order_id` `order_id` INT(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the order id this order food belongs to'  , 
ADD INDEX `ix_order_id` (`order_id` ASC) ;