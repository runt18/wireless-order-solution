SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Drop table `wireless_order_db`.`member_charge`
-- -----------------------------------------------------
DROP TABLE `wireless_order_db`.`member_charge`;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`member_type`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`member_type` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`member_type` (
  `member_type_id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to member type' ,
  `restaurant_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the restaurant id to this member type' ,
  `discount_id` INT UNSIGNED NOT NULL COMMENT 'the discount id this member type uses' ,
  `discount_type` TINYINT NOT NULL DEFAULT 0 COMMENT 'the discount type is as below.\n0 - discount plan\n1 - entire ' ,
  `exchange_rate` DECIMAL(4,2) UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the exchange rate used to transfer the price to point' ,
  `charge_rate` DECIMAL(4,2) NOT NULL DEFAULT 0 COMMENT 'the charge rate used to transfer money to balance' ,
  `name` VARCHAR(45) NOT NULL COMMENT 'the name to this member type' ,
  `attribute` TINYINT NOT NULL DEFAULT 0 COMMENT 'the attribute to this member tye as below.\n0 - 充值属性\n1 - 积分属性\n2 - 优惠属性\n\n' ,
  PRIMARY KEY (`member_type_id`) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the member type information' ;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`client`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`client` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`client` (
  `client_id` INT NOT NULL AUTO_INCREMENT COMMENT 'thie id to this client' ,
  `restaurant_id` INT UNSIGNED NOT NULL COMMENT 'the restaurant id to this client' ,
  `client_type_id` INT NOT NULL COMMENT 'the type this client belongs to' ,
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
  `level` TINYINT NULL DEFAULT 0 COMMENT 'the status to client.\n0 - normal\n1 - reserved' ,
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
  `balance` DECIMAL(7,2) NOT NULL DEFAULT 0 COMMENT 'the balance to this member' ,
  `point` INT NOT NULL DEFAULT 0 COMMENT 'the remaining point to this member' ,
  `birth_date` DATETIME NULL DEFAULT NULL ,
  `last_mod_date` DATETIME NULL DEFAULT NULL ,
  `comment` VARCHAR(500) NULL DEFAULT NULL ,
  `status` TINYINT NULL DEFAULT 0 COMMENT 'the status to this member\n0 - normal\n1 - disabled' ,
  `last_staff_id` INT NULL DEFAULT NULL COMMENT 'the id to last modified staff' ,
  PRIMARY KEY (`member_id`) ,
  INDEX `ix_member_type_id` (`member_type_id` ASC) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) ,
  INDEX `ix_member_card_id` (`member_card_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the member' ;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`client_member`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`client_member` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`client_member` (
  `id` INT NOT NULL ,
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
-- Table `wireless_order_db`.`food_statistics`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`food_statistics` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`food_statistics` (
  `food_id` INT NOT NULL ,
  `order_cnt` INT UNSIGNED NOT NULL DEFAULT 0 ,
  PRIMARY KEY (`food_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the food statistics' ;


-- -----------------------------------------------------
-- Remove the duplicated taste id to table 'order_food_history'
-- -----------------------------------------------------
UPDATE wireless_order_db.order_food_history 
SET taste2_id = NULL, taste2_alias = 0
WHERE 
taste_id IS NOT NULL
AND
taste_id = taste2_id;

UPDATE wireless_order_db.order_food_history 
SET taste3_id = NULL, taste3_alias = 0
WHERE 
taste_id IS NOT NULL
AND
taste_id = taste3_id;

UPDATE wireless_order_db.order_food_history 
SET taste3_id = NULL, taste3_alias = 0
WHERE 
taste2_id IS NOT NULL
AND
taste2_id = taste3_id;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`taste_group_history`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`taste_group_history` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`taste_group_history` (
  `taste_group_id` INT NOT NULL AUTO_INCREMENT ,
  `normal_taste_group_id` INT NOT NULL DEFAULT 1 ,
  `normal_taste_pref` VARCHAR(45) NULL DEFAULT NULL ,
  `normal_taste_price` DECIMAL(7,2) UNSIGNED NULL DEFAULT NULL ,
  `tmp_taste_id` INT NULL DEFAULT NULL ,
  `tmp_taste_pref` VARCHAR(45) NULL DEFAULT NULL ,
  `tmp_taste_price` DECIMAL(7,2) UNSIGNED NULL DEFAULT NULL ,
  PRIMARY KEY (`taste_group_id`) ,
  INDEX `ix_normal_taste_group_id` (`normal_taste_group_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the taste group' ;

-- -----------------------------------------------------
-- The fields to table 'taste_group_history' below are created temporary 
-- for taste group data transferring.
-- So would be removed later.
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`taste_group_history` 
ADD COLUMN `food_alias` SMALLINT UNSIGNED NULL  AFTER `tmp_taste_price` , 
ADD COLUMN `taste_alias` SMALLINT UNSIGNED NULL  AFTER `food_alias` , 
ADD COLUMN `taste2_alias` SMALLINT UNSIGNED NULL  AFTER `taste_alias` , 
ADD COLUMN `taste3_alias` SMALLINT UNSIGNED NULL  AFTER `taste2_alias` , 
ADD COLUMN `taste_tmp_alias` SMALLINT UNSIGNED NULL  AFTER `taste3_alias` , 
ADD COLUMN `order_id` INT NULL  AFTER `taste_tmp_alias`,
ADD COLUMN `taste_id` INT NULL  AFTER `order_id` , 
ADD COLUMN `taste2_id` INT NULL  AFTER `taste_id` , 
ADD COLUMN `taste3_id` INT NULL  AFTER `taste2_id` ;

-- -----------------------------------------------------
-- Insert a record whose taste_group_id equals 1 to indicate
-- the empty taste group
-- -----------------------------------------------------
INSERT INTO wireless_order_db.taste_group_history 
(`taste_group_id`, `normal_taste_group_id`)
VALUES
(1, 1);

-- -----------------------------------------------------
-- Table `wireless_order_db`.`normal_taste_group_history`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`normal_taste_group_history` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`normal_taste_group_history` (
  `normal_taste_group_id` INT NOT NULL ,
  `taste_id` INT NOT NULL ,
  PRIMARY KEY (`normal_taste_group_id`, `taste_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the relationship between taste group and its normal' /* comment truncated */ ;

-- -----------------------------------------------------
-- Insert a record whose taste_normal_id equals 1 to indicate
-- the empty taste normal group
-- -----------------------------------------------------
INSERT INTO wireless_order_db.normal_taste_group_history
(`normal_taste_group_id`, `taste_id`)
VALUES
(1, 0);

-- -----------------------------------------------------
-- Extract the taste group info from table 'order_food_history'.
-- And update it to table 'taste_group_history'
-- -----------------------------------------------------
SET @normal_taste_group_id = 1;

INSERT INTO wireless_order_db.taste_group_history
(`normal_taste_group_id`, `normal_taste_pref`, `normal_taste_price`,
 `tmp_taste_id`, `tmp_taste_pref`, `tmp_taste_price`,
 `food_alias`, `taste_alias`, `taste2_alias`, `taste3_alias`, `taste_tmp_alias`, `order_id`,
 `taste_id`, `taste2_id`, `taste3_id`) 
SELECT 
(CASE WHEN MAX(taste) IS NULL THEN 1 ELSE (@normal_taste_group_id := @normal_taste_group_id + 1) END) AS normal_taste_group_id,
MAX(taste) AS normal_taste_pref, MAX(taste_price) AS normal_taste_price
, taste_tmp_alias AS tmp_taste_id, MAX(taste_tmp) AS tmp_taste_pref, MAX(taste_tmp_price) AS tmp_taste_price
, food_alias, taste_alias, taste2_alias, taste3_alias, taste_tmp_alias, order_id
, MAX(taste_id) AS taste_id, MAX(taste2_id) AS taste2_id, MAX(taste3_id) AS taste3_id
FROM
wireless_order_db.order_food_history
WHERE 1 = 1 
AND taste_alias <> 0 OR taste2_alias <> 0 OR taste3_alias <> 0 OR taste_tmp_alias IS NOT NULL
GROUP BY order_id, food_alias, taste_alias, taste2_alias, taste3_alias, taste_tmp_alias;

-- -----------------------------------------------------
-- Insert the 1st taste id to normal taste group history
-- -----------------------------------------------------
INSERT INTO wireless_order_db.normal_taste_group_history
(`normal_taste_group_id`, `taste_id`)
SELECT 
normal_taste_group_id, taste_id
FROM 
wireless_order_db.taste_group_history
WHERE normal_taste_group_id <> 1 AND taste_id IS NOT NULL;

-- -----------------------------------------------------
-- Insert the 2nd taste id to normal taste group history
-- -----------------------------------------------------
INSERT INTO wireless_order_db.normal_taste_group_history
(`normal_taste_group_id`, `taste_id`)
SELECT 
normal_taste_group_id, taste2_id
FROM 
wireless_order_db.taste_group_history
WHERE normal_taste_group_id <> 1 AND taste2_id IS NOT NULL;

-- -----------------------------------------------------
-- Insert the 3rd taste id to normal taste group history
-- -----------------------------------------------------
INSERT INTO wireless_order_db.normal_taste_group_history
(`normal_taste_group_id`, `taste_id`)
SELECT 
normal_taste_group_id, taste3_id
FROM 
wireless_order_db.taste_group_history
WHERE normal_taste_group_id <> 1 AND taste3_id IS NOT NULL;

-- -----------------------------------------------------
-- Add the field 'taste_group_id' whose default is 1 (means empty taste group)
-- to table 'order_food_history' and create index on it.
-- Drop the index 'ix_food_id'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food_history` 
ADD COLUMN `taste_group_id` INT NOT NULL DEFAULT 1 COMMENT 'the taste group id to this order food',
ADD INDEX `ix_taste_group_id` (`taste_group_id` ASC);

-- -----------------------------------------------------
-- Update the taste_group_id to table 'order_food_history'
-- -----------------------------------------------------
UPDATE wireless_order_db.order_food_history ORDER_FOOD_HISTORY, 

(SELECT OFH.id, TGH.taste_group_id FROM 
`wireless_order_db`.`order_food_history` OFH
JOIN
`wireless_order_db`.`taste_group_history` TGH
ON TGH.order_id = OFH.order_id AND TGH.food_alias = OFH.food_alias
AND TGH.taste_alias = OFH.taste_alias AND TGH.taste2_alias = OFH.taste2_alias
AND TGH.taste3_alias = OFH.taste3_alias AND IFNULL(OFH.taste_tmp_alias, 0) = IFNULL(TGH.taste_tmp_alias, 0)) AS A

SET ORDER_FOOD_HISTORY.taste_group_id = A.taste_group_id

WHERE ORDER_FOOD_HISTORY.id = A.id
;

-- -----------------------------------------------------
-- Check to see if the data to history is correct after refactoring
-- -----------------------------------------------------
SELECT CASE WHEN A.count = 0 THEN 'pass' ELSE 'failed' END AS test_1, 
       CASE WHEN (B.total_normal_taste_price = C.total_normal_taste_price) THEN 'pass' ELSE 'failed' END AS test_2, 
       CASE WHEN (B.total_tmp_taste_price = C.total_tmp_taste_price) THEN 'pass' ELSE 'failed' END AS test_3

FROM

(SELECT COUNT(*) AS count 
FROM `wireless_order_db`.`order_food_history`
WHERE taste_group_id <> 1
AND taste_id IS NULL AND taste2_id IS NULL AND taste3_id IS NULL AND taste_tmp_alias IS NULL) AS A,

(SELECT 
SUM(IFNULL(taste_price, 0) * order_count) AS total_normal_taste_price, 
SUM(IFNULL(taste_tmp_price, 0) * order_count) AS total_tmp_taste_price 
FROM `wireless_order_db`.`order_food_history`) AS B,

(SELECT 
SUM(IFNULL(TGH.normal_taste_price, 0) * OFH.order_count) AS total_normal_taste_price,
SUM(IFNULL(TGH.tmp_taste_price, 0) * OFH.order_count) AS total_tmp_taste_price
FROM 
wireless_order_db.order_food_history OFH
JOIN
wireless_order_db.taste_group_history TGH
ON OFH.taste_group_id = TGH.taste_group_id AND TGH.taste_group_id <> 1) AS C;

-- -----------------------------------------------------
-- Drop the fields below to table 'order_food_history'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food_history` 
DROP COLUMN `taste_tmp_price` , 
DROP COLUMN `taste_tmp` , 
DROP COLUMN `taste_tmp_alias` , 
DROP COLUMN `taste3_alias` , 
DROP COLUMN `taste2_alias` , 
DROP COLUMN `taste_alias` , 
DROP COLUMN `taste3_id` , 
DROP COLUMN `taste2_id` , 
DROP COLUMN `taste_id` , 
DROP COLUMN `taste_price` , 
DROP COLUMN `taste` ;

-- -----------------------------------------------------
-- Drop the temporary fields below to table 'taste_group_history'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`taste_group_history` 
DROP COLUMN `food_alias` , 
DROP COLUMN `taste_alias` , 
DROP COLUMN `taste2_alias` , 
DROP COLUMN `taste3_alias` , 
DROP COLUMN `taste_tmp_alias` , 
DROP COLUMN `order_id` ,
DROP COLUMN `taste_id` , 
DROP COLUMN `taste2_id`, 
DROP COLUMN `taste3_id` ;

-- -----------------------------------------------------
-- Remove the duplicated taste id to table 'order_food'
-- -----------------------------------------------------
UPDATE wireless_order_db.order_food
SET taste2_id = NULL, taste2_alias = 0
WHERE 
taste_id IS NOT NULL
AND
taste_id = taste2_id;

UPDATE wireless_order_db.order_food
SET taste3_id = NULL, taste3_alias = 0
WHERE 
taste_id IS NOT NULL
AND
taste_id = taste3_id;

UPDATE wireless_order_db.order_food
SET taste3_id = NULL, taste3_alias = 0
WHERE 
taste2_id IS NOT NULL
AND
taste2_id = taste3_id;

-- -----------------------------------------------------
-- Add the field 'taste_group_id' whose default is 1 (means empty taste group)
-- to table 'order_food' and create index on it
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food` 
ADD COLUMN `taste_group_id` INT NOT NULL DEFAULT 1 COMMENT 'the taste group id to this order food'  AFTER `hang_status`,
ADD INDEX `ix_taste_group_id` (`taste_group_id` ASC) ;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`taste_group`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`taste_group` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`taste_group` (
  `taste_group_id` INT NOT NULL AUTO_INCREMENT ,
  `normal_taste_group_id` INT NOT NULL DEFAULT 1 ,
  `normal_taste_pref` VARCHAR(45) NULL DEFAULT NULL ,
  `normal_taste_price` DECIMAL(7,2) UNSIGNED NULL DEFAULT NULL ,
  `tmp_taste_id` INT NULL DEFAULT NULL ,
  `tmp_taste_pref` VARCHAR(45) NULL DEFAULT NULL ,
  `tmp_taste_price` DECIMAL(7,2) UNSIGNED NULL DEFAULT NULL ,
  PRIMARY KEY (`taste_group_id`) ,
  INDEX `ix_normal_taste_group_id` (`normal_taste_group_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the taste group' ;

-- -----------------------------------------------------
-- Insert a record whose taste_group_id equals 1 to indicate
-- the empty taste group
-- -----------------------------------------------------
INSERT INTO wireless_order_db.taste_group 
(`taste_group_id`, `normal_taste_group_id`)
VALUES
(1, 1);

-- -----------------------------------------------------
-- Table `wireless_order_db`.`normal_taste_group`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`normal_taste_group` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`normal_taste_group` (
  `normal_taste_group_id` INT NOT NULL ,
  `taste_id` INT NOT NULL ,
  PRIMARY KEY (`normal_taste_group_id`, `taste_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the relationship between taste group and its normal' /* comment truncated */ ;

-- -----------------------------------------------------
-- Insert a record whose taste_normal_id equals 1 to indicate
-- the empty taste normal group
-- -----------------------------------------------------
INSERT INTO wireless_order_db.normal_taste_group
(`normal_taste_group_id`, `taste_id`)
VALUES
(1, 0);

-- -----------------------------------------------------
-- The fields to table 'order_food' below are created temporary 
-- for taste group data transferring.
-- So would be removed later.
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`taste_group` 
ADD COLUMN `food_alias` SMALLINT UNSIGNED NULL  AFTER `tmp_taste_price` , 
ADD COLUMN `taste_alias` SMALLINT UNSIGNED NULL  AFTER `food_alias` , 
ADD COLUMN `taste2_alias` SMALLINT UNSIGNED NULL  AFTER `taste_alias` , 
ADD COLUMN `taste3_alias` SMALLINT UNSIGNED NULL  AFTER `taste2_alias` , 
ADD COLUMN `taste_tmp_alias` SMALLINT UNSIGNED NULL  AFTER `taste3_alias` , 
ADD COLUMN `order_id` INT NULL  AFTER `taste_tmp_alias`,
ADD COLUMN `taste_id` INT NULL  AFTER `order_id` , 
ADD COLUMN `taste2_id` INT NULL  AFTER `taste_id` , 
ADD COLUMN `taste3_id` INT NULL  AFTER `taste2_id` ;

-- -----------------------------------------------------
-- Insert the max id to taste group
-- -----------------------------------------------------
INSERT INTO wireless_order_db.taste_group
(`taste_group_id`, `normal_taste_group_id`)
SELECT MAX(taste_group_id) + 1, MAX(normal_taste_group_id) + 1 FROM
(
SELECT MAX(taste_group_id) AS taste_group_id, MAX(normal_taste_group_id) AS normal_taste_group_id FROM wireless_order_db.taste_group
UNION
SELECT MAX(taste_group_id) AS taste_group_id, MAX(normal_taste_group_id) AS normal_taste_group_id FROM wireless_order_db.taste_group_history
)
AS all_taste_group;

-- -----------------------------------------------------
-- Extract the taste group info from table 'order_food'.
-- And update it to table 'taste_group'
-- -----------------------------------------------------
SET @normal_taste_group_id = (SELECT MAX(normal_taste_group_id) FROM wireless_order_db.taste_group);

UPDATE wireless_order_db.order_food
SET taste_group_id = (SELECT MAX(taste_group_id) FROM wireless_order_db.taste_group)
WHERE restaurant_id = 0;

INSERT INTO wireless_order_db.taste_group
(`normal_taste_group_id`, `normal_taste_pref`, `normal_taste_price`,
 `tmp_taste_id`, `tmp_taste_pref`, `tmp_taste_price`,
 `food_alias`, `taste_alias`, `taste2_alias`, `taste3_alias`, `taste_tmp_alias`, `order_id`,
 `taste_id`, `taste2_id`, `taste3_id`) 
SELECT 
(CASE WHEN MAX(taste) IS NULL THEN 1 ELSE (@normal_taste_group_id := @normal_taste_group_id + 1) END) AS normal_taste_group_id,
MAX(taste) AS normal_taste_pref, MAX(taste_price) AS normal_taste_price
, taste_tmp_alias AS tmp_taste_id, MAX(taste_tmp) AS tmp_taste_pref, MAX(taste_tmp_price) AS tmp_taste_price
, food_alias, taste_alias, taste2_alias, taste3_alias, taste_tmp_alias, order_id
, MAX(taste_id) AS taste_id, MAX(taste2_id) AS taste2_id, MAX(taste3_id) AS taste3_id
FROM
wireless_order_db.order_food
WHERE 1 = 1 
AND taste_alias <> 0 OR taste2_alias <> 0 OR taste3_alias <> 0 OR taste_tmp_alias IS NOT NULL
GROUP BY order_id, food_alias, taste_alias, taste2_alias, taste3_alias, taste_tmp_alias;

-- -----------------------------------------------------
-- Insert the 1st taste id to normal taste group
-- -----------------------------------------------------
INSERT INTO wireless_order_db.normal_taste_group
(`normal_taste_group_id`, `taste_id`)
SELECT 
normal_taste_group_id, taste_id
FROM 
wireless_order_db.taste_group
WHERE normal_taste_group_id <> 1 AND taste_id IS NOT NULL;

-- -----------------------------------------------------
-- Insert the 2nd taste id to normal taste group
-- -----------------------------------------------------
INSERT INTO wireless_order_db.normal_taste_group
(`normal_taste_group_id`, `taste_id`)
SELECT 
normal_taste_group_id, taste2_id
FROM 
wireless_order_db.taste_group
WHERE normal_taste_group_id <> 1 AND taste2_id IS NOT NULL;

-- -----------------------------------------------------
-- Insert the 3rd taste id to normal taste group
-- -----------------------------------------------------
INSERT INTO wireless_order_db.normal_taste_group
(`normal_taste_group_id`, `taste_id`)
SELECT 
normal_taste_group_id, taste3_id
FROM 
wireless_order_db.taste_group
WHERE normal_taste_group_id <> 1 AND taste3_id IS NOT NULL;


-- -----------------------------------------------------
-- Update the taste_group_id to table 'order_food'
-- -----------------------------------------------------
UPDATE wireless_order_db.order_food ORDER_FOOD, 

(SELECT OF.id, TG.taste_group_id FROM 
`wireless_order_db`.`order_food` OF
JOIN
`wireless_order_db`.`taste_group` TG
ON TG.order_id = OF.order_id AND TG.food_alias = OF.food_alias
AND TG.taste_alias = OF.taste_alias AND TG.taste2_alias = OF.taste2_alias
AND TG.taste3_alias = OF.taste3_alias AND IFNULL(OF.taste_tmp_alias, 0) = IFNULL(TG.taste_tmp_alias, 0)) AS A

SET ORDER_FOOD.taste_group_id = A.taste_group_id

WHERE ORDER_FOOD.id = A.id
;

-- -----------------------------------------------------
-- Check to see if the data of today is correct after refactoring
-- -----------------------------------------------------
SELECT CASE WHEN A.count = 0 THEN 'pass' ELSE 'failed' END AS test_1, 
       CASE WHEN (B.total_normal_taste_price = C.total_normal_taste_price) THEN 'pass' ELSE 'failed' END AS test_2, 
       CASE WHEN (B.total_tmp_taste_price = C.total_tmp_taste_price) THEN 'pass' ELSE 'failed' END AS test_3

FROM

(SELECT COUNT(*) AS count 
FROM `wireless_order_db`.`order_food`
WHERE taste_group_id <> 1
AND taste_id IS NULL AND taste2_id IS NULL AND taste3_id IS NULL AND taste_tmp_alias IS NULL) AS A,

(SELECT 
SUM(IFNULL(taste_price, 0) * order_count) AS total_normal_taste_price, 
SUM(IFNULL(taste_tmp_price, 0) * order_count) AS total_tmp_taste_price 
FROM `wireless_order_db`.`order_food`) AS B,

(SELECT 
SUM(IFNULL(TGH.normal_taste_price, 0) * OFH.order_count) AS total_normal_taste_price,
SUM(IFNULL(TGH.tmp_taste_price, 0) * OFH.order_count) AS total_tmp_taste_price
FROM 
wireless_order_db.order_food OFH
JOIN
wireless_order_db.taste_group TGH
ON OFH.taste_group_id = TGH.taste_group_id AND TGH.taste_group_id <> 1) AS C;

-- -----------------------------------------------------
-- Drop the temporary fields below to table 'taste_group'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`taste_group` 
DROP COLUMN `food_alias` , 
DROP COLUMN `taste_alias` , 
DROP COLUMN `taste2_alias` , 
DROP COLUMN `taste3_alias` , 
DROP COLUMN `taste_tmp_alias` , 
DROP COLUMN `order_id` ,
DROP COLUMN `taste_id` , 
DROP COLUMN `taste2_id`, 
DROP COLUMN `taste3_id` ;

-- -----------------------------------------------------
-- Drop the fields below to table 'order_food'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food` 
DROP COLUMN `taste_tmp_price` , 
DROP COLUMN `taste_tmp` , 
DROP COLUMN `taste_tmp_alias` , 
DROP COLUMN `taste3_alias` , 
DROP COLUMN `taste2_alias` , 
DROP COLUMN `taste_alias` , 
DROP COLUMN `taste3_id` , 
DROP COLUMN `taste2_id` , 
DROP COLUMN `taste_id` , 
DROP COLUMN `taste_price` , 
DROP COLUMN `taste` ;

-- -----------------------------------------------------
-- Add the field 'is_allow_temp' to table 'kitchen'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`kitchen` 
ADD COLUMN `is_allow_temp` TINYINT NOT NULL DEFAULT 0 COMMENT 'the flag to indicate whether allow temporary food'  AFTER `type` ;

-- -----------------------------------------------------
-- Add the index 'ix_discount_id' for field 'discount_id' to table 'discount_plan'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`discount_plan` 
ADD INDEX `ix_discount_id` (`discount_id` ASC) ;
