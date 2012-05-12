SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Rename the field 'id' of table 'table' to 'table_id'
-- Rename the field 'alias_id' to 'table_alias'
-- Create the index associated with 'restaurant_id' and 'table_alias'
-- Drop the foreign key to 'restaurant_id'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`table` DROP FOREIGN KEY `fk_table_restaurant1` ;

ALTER TABLE `wireless_order_db`.`table` CHANGE COLUMN `id` `table_id` INT(11) NOT NULL AUTO_INCREMENT COMMENT 'the id to this table'  , CHANGE COLUMN `alias_id` `table_alias` SMALLINT(5) UNSIGNED NULL DEFAULT NULL  

, DROP PRIMARY KEY 

, ADD PRIMARY KEY (`table_id`) 

, DROP INDEX `ix_table_alias_id` 

, ADD INDEX `ix_table_alias_id` (`restaurant_id` ASC, `table_alias` ASC) 

, DROP INDEX `fk_table_restaurant` ;



-- -----------------------------------------------------
-- Rename the field 'table_id' of table 'order' to 'table_alias'
-- Create the index associated with 'restaurant_id'
-- Drop the foreign key to 'restaurant_id'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order` DROP FOREIGN KEY `fk_order_restaurant` ;

ALTER TABLE `wireless_order_db`.`order` CHANGE COLUMN `table_id` `table_alias` SMALLINT(6) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the table alias id to this order'  , CHANGE COLUMN `table2_id` `table2_alias` SMALLINT(6) UNSIGNED NULL DEFAULT NULL COMMENT 'the 2nd table alias id to this order(used for table merger)'  

, ADD INDEX `ix_order_restaurant` USING BTREE (`restaurant_id` ASC) 

, DROP INDEX `fk_order_restaurant` ;

-- -----------------------------------------------------
-- Rename the field 'table_id' of table 'order_history' to 'table_alias'
-- Create the index associated with 'restaurant_id'
-- Drop the foreign key to 'restaurant_id'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_history` DROP FOREIGN KEY `fk_order_restaurant0` ;

ALTER TABLE `wireless_order_db`.`order_history` CHANGE COLUMN `table_id` `table_alias` SMALLINT(6) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the table alias id to this order'  AFTER `comment` , CHANGE COLUMN `table2_id` `table2_alias` SMALLINT(6) UNSIGNED NULL DEFAULT NULL COMMENT 'the 2nd table alias id to this order(used for table merger)'  

, ADD INDEX `ix_order_history_restaurant` USING BTREE (`restaurant_id` ASC) 

, DROP INDEX `fk_order_restaurant` ;

-- -----------------------------------------------------
-- Add the field 'table_id' and 'table2_id' to table 'order'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order` ADD COLUMN `table_id` INT NOT NULL DEFAULT 0 COMMENT 'the table id to this order'  AFTER `region_name` , ADD COLUMN `table2_id` INT NULL DEFAULT NULL COMMENT 'the 2nd table id to this order'  AFTER `table_name` ;

-- -----------------------------------------------------
-- Add the field 'table_id' and 'table2_id' to table 'order_history'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_history` ADD COLUMN `table_id` INT NOT NULL DEFAULT 0 COMMENT 'the table id to this order'  AFTER `region_name` , ADD COLUMN `table2_id` INT NULL DEFAULT NULL COMMENT 'the 2nd table id to this order'  AFTER `table_name` ;

-- -----------------------------------------------------
-- Update the field 'table_id' in table 'order'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order` CHANGE COLUMN `table_id` `table_id` INT(11) NULL DEFAULT '0' COMMENT 'the table id to this order'  ;
UPDATE `wireless_order_db`.`order` AS a SET table_id = (SELECT table_id FROM `wireless_order_db`.`table` AS b WHERE a.table_alias = b.table_alias AND a.restaurant_id = b.restaurant_id);
UPDATE `wireless_order_db`.`order` SET table_id=0 WHERE table_id IS NULL;
ALTER TABLE `wireless_order_db`.`order` CHANGE COLUMN `table_id` `table_id` INT(11) NOT NULL DEFAULT '0' COMMENT 'the table id to this order'  ;

-- -----------------------------------------------------
-- Update the field 'table2_id' in table 'order'
-- -----------------------------------------------------
UPDATE `wireless_order_db`.`order` AS a SET table2_id = (SELECT table_id FROM `wireless_order_db`.`table` AS b WHERE a.table2_alias = b.table_alias AND a.restaurant_id = b.restaurant_id);

-- -----------------------------------------------------
-- Update the field 'table_id' in table 'order_history'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_history` CHANGE COLUMN `table_id` `table_id` INT(11) NULL DEFAULT '0' COMMENT 'the table id to this order'  ;
UPDATE `wireless_order_db`.`order_history` AS a SET table_id = (SELECT table_id FROM `wireless_order_db`.`table` AS b WHERE a.table_alias = b.table_alias AND a.restaurant_id = b.restaurant_id);
UPDATE `wireless_order_db`.`order_history` SET table_id=0 WHERE table_id IS NULL;
ALTER TABLE `wireless_order_db`.`order_history` CHANGE COLUMN `table_id` `table_id` INT(11) NOT NULL DEFAULT '0' COMMENT 'the table id to this order'  ;

-- -----------------------------------------------------
-- Update the field 'table2_id' in table 'order_history'
-- -----------------------------------------------------
UPDATE `wireless_order_db`.`order_history` AS a SET table2_id = (SELECT table_id FROM `wireless_order_db`.`table` AS b WHERE a.table2_alias = b.table_alias AND a.restaurant_id = b.restaurant_id);

-- -----------------------------------------------------
-- Drop the foreign key to restaurant.
-- Rename the field 'id' to 'kitchen_id'.
-- Rename the field 'alias_id' to 'kitchen_alias'
-- Create the index associates with both restaurant_id and kitchen_alias
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`kitchen` DROP FOREIGN KEY `fk_kitchen_restaurant1` ;

ALTER TABLE `wireless_order_db`.`kitchen` CHANGE COLUMN `id` `kitchen_id` INT(11) NOT NULL AUTO_INCREMENT COMMENT 'the id to this kitchen'  , CHANGE COLUMN `alias_id` `kitchen_alias` TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the alias id to this kitchen'  

, DROP PRIMARY KEY 

, ADD PRIMARY KEY (`kitchen_id`) 

, ADD INDEX `ix_kitchen_alias_id` USING BTREE (`restaurant_id` ASC, `kitchen_alias` ASC) 

, DROP INDEX `fk_kitchen_restaurant1` ;

-- -----------------------------------------------------
-- Add the field 'kitchen_id' to table 'order_food'.
-- Rename the field 'kitchen' to 'kitchen_alias'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food` 
ADD COLUMN `kitchen_id` INT NULL DEFAULT NULL COMMENT 'the kitchen id which the order food of this record belong to. '  AFTER `discount` , CHANGE COLUMN `kitchen` `kitchen_alias` TINYINT(3) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the kitchen alias id which the order food of this record belong to.'  ;

-- -----------------------------------------------------
-- Add the field 'kitchen_id' to table 'order_food'.
-- Rename the field 'kitchen' to 'kitchen_alias'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food_history` 
ADD COLUMN `kitchen_id` INT NULL DEFAULT NULL COMMENT 'the kitchen id which the order food of this record belong to. '  AFTER `discount` , CHANGE COLUMN `kitchen` `kitchen_alias` TINYINT(3) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the kitchen alias id which the order food of this record belong to.'  ;

-- -----------------------------------------------------
-- Update the field 'kitchen_id' to table 'order_food'
-- -----------------------------------------------------
UPDATE wireless_order_db.order_food AS a SET kitchen_id = (SELECT kitchen_id FROM wireless_order_db.kitchen b WHERE b.restaurant_id=a.restaurant_id AND b.kitchen_alias=a.kitchen_alias);

-- -----------------------------------------------------
-- Update the field 'kitchen_id' to table 'order_food_history'
-- -----------------------------------------------------
UPDATE wireless_order_db.order_food_history AS a SET kitchen_id = (SELECT kitchen_id FROM wireless_order_db.kitchen b WHERE b.restaurant_id=a.restaurant_id AND b.kitchen_alias=a.kitchen_alias);


-- -----------------------------------------------------
-- Drop the foreign key to restaurant.
-- Rename the field 'id' to 'food_id'.
-- Rename the field 'alias_id' to 'food_alias'
-- Create the index associates with both restaurant_id and food_alias
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`food` DROP FOREIGN KEY `fk_food_restaurant` ;

ALTER TABLE `wireless_order_db`.`food` CHANGE COLUMN `restaurant_id` `restaurant_id` INT(10) UNSIGNED NOT NULL COMMENT 'indicates the food belong to which restaurant'  AFTER `food_alias` , CHANGE COLUMN `id` `food_id` INT(10) NOT NULL AUTO_INCREMENT COMMENT 'the id to this food'  , CHANGE COLUMN `alias_id` `food_alias` SMALLINT(5) UNSIGNED NOT NULL COMMENT 'the waiter use this alias id to select food in terminal'  

, DROP PRIMARY KEY 

, ADD PRIMARY KEY (`food_id`) 

, DROP INDEX `ix_food_alias_id` 

, ADD INDEX `ix_food_alias_id` (`restaurant_id` ASC, `food_alias` ASC) 

, DROP INDEX `fk_food_restaurant` ;

-- -----------------------------------------------------
-- Rename the field 'food_id' to 'food_alias' in table 'order_food'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food` CHANGE COLUMN `food_id` `food_alias` SMALLINT(6) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the alias id to this food'  ;

-- -----------------------------------------------------
-- Rename the field 'food_id' to 'food_alias' in table 'order_food_history'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food_history` CHANGE COLUMN `food_id` `food_alias` SMALLINT(6) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the alias id to this food'  ;

-- -----------------------------------------------------
-- Add the field 'food_id' to table 'order_food'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food` ADD COLUMN `food_id` INT NULL DEFAULT NULL COMMENT 'the id to this food'  AFTER `restaurant_id` ;

-- -----------------------------------------------------
-- Add the field 'food_id' to table 'order_food_history'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food_history` ADD COLUMN `food_id` INT NULL DEFAULT NULL COMMENT 'the id to this food'  AFTER `restaurant_id` ;

-- -----------------------------------------------------
-- Update the field 'food_id' to table 'order_food'
-- -----------------------------------------------------
UPDATE wireless_order_db.order_food AS a SET food_id = (SELECT food_id FROM wireless_order_db.food b WHERE b.restaurant_id=a.restaurant_id AND b.food_alias=a.food_alias);

-- -----------------------------------------------------
-- Update the field 'food_id' to table 'order_food_history'
-- -----------------------------------------------------
UPDATE wireless_order_db.order_food_history AS a SET food_id = (SELECT food_id FROM wireless_order_db.food b WHERE b.restaurant_id=a.restaurant_id AND b.food_alias=a.food_alias);


-- -----------------------------------------------------
-- Drop the foreign key to restaurant.
-- Rename the field 'id' to 'taste_id'.
-- Rename the field 'alias_id' to 'taste_alias'
-- Create the index associates with both restaurant_id and taste_alias
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`taste` DROP FOREIGN KEY `fk_taste_restaurant_id` ;

ALTER TABLE `wireless_order_db`.`taste` CHANGE COLUMN `id` `taste_id` INT(11) NOT NULL AUTO_INCREMENT COMMENT 'the id to taste table'  , CHANGE COLUMN `alias_id` `taste_alias` SMALLINT(5) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the alias id to this taste preference, the lower the alias id , the more commonly this taste preference used'  

, DROP PRIMARY KEY 

, ADD PRIMARY KEY (`taste_id`) 

, ADD INDEX `ix_taste_alias_id` (`restaurant_id` ASC, `taste_alias` ASC) 

, DROP INDEX `fk_taste_restaurant_id` ;


-- -----------------------------------------------------
-- Rename the field 'taste_id' to 'taste_alias' in table 'order_food'
-- Rename the field 'taste_id2' to 'taste2_alias' in table 'order_food'
-- Rename the field 'taste_id2' to 'taste3_alias' in table 'order_food'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food` CHANGE COLUMN `taste_id2` `taste2_alias` SMALLINT(5) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the 2nd taste alias id'  AFTER `taste_alias` , CHANGE COLUMN `taste_id3` `taste3_alias` SMALLINT(5) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the 3rd taste alias id'  AFTER `taste2_alias` , CHANGE COLUMN `taste_id` `taste_alias` SMALLINT(5) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the taste alias id'  ;

-- -----------------------------------------------------
-- Add the field 'taste_id' to table 'order_food'
-- Add the field 'taste2_id' to table 'order_food'
-- Add the field 'taste3_id' to table 'order_food'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food` ADD COLUMN `taste_id` INT NULL DEFAULT NULL COMMENT 'the taste id'  AFTER `taste_price` , ADD COLUMN `taste2_id` INT NULL DEFAULT NULL COMMENT 'the 2nd taste id'  AFTER `taste_id` , ADD COLUMN `taste3_id` INT NULL DEFAULT NULL COMMENT 'the 3rd taste id'  AFTER `taste2_id` ;

-- -----------------------------------------------------
-- Rename the field 'taste_id' to 'taste_alias' in table 'order_food_history'
-- Rename the field 'taste_id2' to 'taste2_alias' in table 'order_food_history'
-- Rename the field 'taste_id2' to 'taste3_alias' in table 'order_food_history'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food_history` CHANGE COLUMN `taste_id` `taste_alias` SMALLINT(5) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the taste alias id'  , CHANGE COLUMN `taste_id2` `taste2_alias` SMALLINT(5) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the 2nd taste id to this order food record'  , CHANGE COLUMN `taste_id3` `taste3_alias` SMALLINT(5) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the 3rd taste id to this order food record'  ;

-- -----------------------------------------------------
-- Add the field 'taste_id' to table 'order_food_history'
-- Add the field 'taste2_id' to table 'order_food_history'
-- Add the field 'taste3_id' to table 'order_food_history'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food_history` ADD COLUMN `taste_id` INT NULL DEFAULT NULL COMMENT 'the taste id'  AFTER `taste_price` , ADD COLUMN `taste2_id` INT NULL DEFAULT NULL COMMENT 'the 2nd taste id'  AFTER `taste_id` , ADD COLUMN `taste3_id` INT NULL DEFAULT NULL COMMENT 'the 3rd taste id'  AFTER `taste2_id` ;

-- -----------------------------------------------------
-- Update the field 'taste_id', 'taste2_id', 'taste3_id' to table 'order_food'
-- -----------------------------------------------------
UPDATE wireless_order_db.order_food AS a SET taste_id = (SELECT taste_id FROM wireless_order_db.taste b WHERE b.restaurant_id=a.restaurant_id AND b.taste_alias=a.taste_alias);
UPDATE wireless_order_db.order_food AS a SET taste2_id = (SELECT taste_id FROM wireless_order_db.taste b WHERE b.restaurant_id=a.restaurant_id AND b.taste_alias=a.taste2_alias);
UPDATE wireless_order_db.order_food AS a SET taste3_id = (SELECT taste_id FROM wireless_order_db.taste b WHERE b.restaurant_id=a.restaurant_id AND b.taste_alias=a.taste3_alias);

-- -----------------------------------------------------
-- Update the field 'taste_id', 'taste2_id', 'taste3_id' to table 'order_food_history'
-- -----------------------------------------------------
UPDATE wireless_order_db.order_food_history AS a SET taste_id = (SELECT taste_id FROM wireless_order_db.taste b WHERE b.restaurant_id=a.restaurant_id AND b.taste_alias=a.taste_alias);
UPDATE wireless_order_db.order_food_history AS a SET taste2_id = (SELECT taste_id FROM wireless_order_db.taste b WHERE b.restaurant_id=a.restaurant_id AND b.taste_alias=a.taste2_alias);
UPDATE wireless_order_db.order_food_history AS a SET taste3_id = (SELECT taste_id FROM wireless_order_db.taste b WHERE b.restaurant_id=a.restaurant_id AND b.taste_alias=a.taste3_alias);

-- -----------------------------------------------------
-- Add the field 'dept_id' to table 'order_food'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food` ADD COLUMN `dept_id` TINYINT UNSIGNED NULL DEFAULT NULL COMMENT 'the department alias id to this record'  AFTER `kitchen_id` ;

-- -----------------------------------------------------
-- Add the field 'dept_id' to table 'order_food_history'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food_history` ADD COLUMN `dept_id` TINYINT UNSIGNED NULL DEFAULT NULL COMMENT 'the department alias id to this record'  AFTER `kitchen_id` ;

-- -----------------------------------------------------
-- Update the field 'dept_id' to table 'order_food'
-- -----------------------------------------------------
UPDATE wireless_order_db.order_food AS a SET dept_id = (SELECT dept_id FROM wireless_order_db.kitchen b WHERE b.restaurant_id=a.restaurant_id AND b.kitchen_alias=a.kitchen_alias);

-- -----------------------------------------------------
-- Update the field 'dept_id' to table 'order_food_history'
-- -----------------------------------------------------
UPDATE wireless_order_db.order_food_history AS a SET dept_id = (SELECT dept_id FROM wireless_order_db.kitchen b WHERE b.restaurant_id=a.restaurant_id AND b.kitchen_alias=a.kitchen_alias);

-- -----------------------------------------------------
-- Rename the field 'kitchen' to 'kitchen_alias' to table 'food'
-- Add the field 'kitchen_id' to table 'food'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`food` ADD COLUMN `kitchen_id` INT NULL DEFAULT NULL COMMENT 'the kitchen id the food belong to'  AFTER `unit_price` , CHANGE COLUMN `kitchen` `kitchen_alias` TINYINT(3) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the kitchen number which the food belong to. the maximum value (255) means the food does not belong to any kitchen.'  ;

-- -----------------------------------------------------
-- Update the field 'dept_id' to table 'order_food_history'
-- -----------------------------------------------------
UPDATE wireless_order_db.food AS a SET kitchen_id = (SELECT kitchen_id FROM wireless_order_db.kitchen b WHERE b.restaurant_id=a.restaurant_id AND b.kitchen_alias=a.kitchen_alias);

-- -----------------------------------------------------
-- Add the field 'is_paid' to table 'order_food'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food` ADD COLUMN `is_paid` TINYINT NOT NULL DEFAULT 0 COMMENT 'indicates whether this record is paid before'  AFTER `is_temporary` ;

-- -----------------------------------------------------
-- Add the field 'is_paid' to table 'order_food_history'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food_history` ADD COLUMN `is_paid` TINYINT NOT NULL DEFAULT 0 COMMENT 'indicates whether this record is paid before'  AFTER `is_temporary` ;

-- -----------------------------------------------------
-- Add the field 'is_paid' to table 'order'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order` ADD COLUMN `is_paid` TINYINT NOT NULL DEFAULT 0 COMMENT 'indicates whether the order has been paid before'  AFTER `service_rate` ;

-- -----------------------------------------------------
-- Add the field 'is_paid' to table 'order_history'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_history` ADD COLUMN `is_paid` TINYINT NOT NULL DEFAULT 0 COMMENT 'indicates whether the order has been paid before'  AFTER `service_rate` ;

-- -----------------------------------------------------
-- Add the field 'seq_id' to table 'order'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order` ADD COLUMN `seq_id` INT UNSIGNED NULL DEFAULT NULL COMMENT 'the sequence id to this order'  AFTER `id` ;

-- -----------------------------------------------------
-- Add the field 'seq_id' to table 'order_history'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_history` ADD COLUMN `seq_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the sequence id to this order'  AFTER `id` ;

-- -----------------------------------------------------
-- Add the admin record to terminal
-- -----------------------------------------------------
INSERT INTO 
wireless_order_db.terminal (`pin`, `restaurant_id`, `model_id`, `owner_name`)

SELECT MAX(pin) + ROUND(0.5 - RAND() * 100), restaurant_id, 254, '管理员'
FROM 
    `wireless_order_db`.`terminal` 
    
WHERE   restaurant_id NOT IN (SELECT restaurant_id FROM `wireless_order_db`.`terminal` WHERE model_id = 254)
    AND restaurant_id > 10 
    
GROUP BY restaurant_id 

;

-- -----------------------------------------------------
-- Add the field 'pwd4' to table 'restaurant'
-- Add the field 'pwd5' to table 'restaurant'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`restaurant` 
ADD COLUMN `pwd4` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the 4th password to this restaurant, whose permission priority is lower than pwd3'  AFTER `pwd3` , 
ADD COLUMN `pwd5` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the 5th password to this restaurant, whose permission priority is lower than pwd4'  AFTER `pwd4` , 
CHANGE COLUMN `pwd` `pwd` VARCHAR(45) NOT NULL COMMENT 'the password for the restaurant to log in'  AFTER `address` ;

-- -----------------------------------------------------
-- Cut the field 'pwd3' to field 'pwd5' (将原先的权限密码2移到退菜密码)
-- Cut the field 'pwd2' to field 'pwd3' (将原先的权限密码1移到店长权限密码)
-- -----------------------------------------------------
UPDATE wireless_order_db.restaurant SET 
pwd5 = pwd3, pwd3='', 
pwd3 = pwd2, pwd2='';


-- -----------------------------------------------------
-- Append the kitchen 11 - 50 
-- -----------------------------------------------------
INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 10, '厨房11' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 11, '厨房12' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 12, '厨房13' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 13, '厨房14' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 14, '厨房15' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 15, '厨房16' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 16, '厨房17' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 17, '厨房18' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 18, '厨房19' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 19, '厨房20' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 20, '厨房21' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 21, '厨房22' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 22, '厨房23' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 23, '厨房24' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 24, '厨房25' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 25, '厨房26' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 26, '厨房27' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 27, '厨房28' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 28, '厨房29' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 29, '厨房30' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 30, '厨房31' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 31, '厨房32' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 32, '厨房33' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 33, '厨房34' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 34, '厨房35' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 35, '厨房36' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 36, '厨房37' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 37, '厨房38' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 38, '厨房39' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 39, '厨房40' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 40, '厨房41' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 41, '厨房42' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 42, '厨房43' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 43, '厨房44' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 44, '厨房45' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 45, '厨房46' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 46, '厨房47' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 47, '厨房48' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 48, '厨房49' FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.kitchen (`restaurant_id`, `kitchen_alias`, `name`)
SELECT id, 49, '厨房50' FROM wireless_order_db.restaurant WHERE id > 10;


-- -----------------------------------------------------
-- Drop the table 'temp_order_food_history'
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`temp_order_food_history`;

-- -----------------------------------------------------
-- Create table `wireless_order_db`.`shift_history`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`shift_history` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`shift_history` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to each shift record' ,
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name of the operator to shift' ,
  `on_duty` DATETIME NULL DEFAULT NULL COMMENT 'the datetime to be on duty' ,
  `off_duty` DATETIME NULL DEFAULT NULL COMMENT 'the datetime to be off duty' ,
  PRIMARY KEY (`id`) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'the shift history to each restaurant' ;

-- -----------------------------------------------------
-- Create table `wireless_order_db`.`daily_settle_history`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`daily_settle_history` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`daily_settle_history` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to each shift record' ,
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name of the operator to perform daily settle' ,
  `on_duty` DATETIME NULL DEFAULT NULL COMMENT 'the datetime to be on duty' ,
  `off_duty` DATETIME NULL DEFAULT NULL COMMENT 'the datetime to be off duty' ,
  PRIMARY KEY (`id`) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'the daily settle history to each restaurant' ;

-- -----------------------------------------------------
-- View `order_food_history_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS order_food_history_view;
DROP VIEW IF EXISTS order_food_view;
DROP VIEW IF EXISTS order_view;
DROP VIEW IF EXISTS order_history_view;

-- -----------------------------------------------------
-- View`restaurant_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS restaurant_view;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `restaurant_view` AS select `r`.`id` AS `id`,`r`.`account` AS `account`,`r`.`restaurant_name` AS `restaurant_name`,`r`.`tele1` AS `tele1`,`r`.`tele2` AS `tele2`,`r`.`address` AS `address`,`r`.`restaurant_info` AS `restaurant_info`,`r`.`record_alive` AS `record_alive`,(select count(`order`.`id`) from `order` where (`order`.`restaurant_id` = `r`.`id`)) AS `order_num`,(select count(`order_history`.`id`) from `order_history` where (`order_history`.`restaurant_id` = `r`.`id`)) AS `order_history_num`,(select count(`terminal`.`pin`) from `terminal` where ((`terminal`.`restaurant_id` = `r`.`id`) and (`terminal`.`model_id` <= 0x7f))) AS `terminal_num`,(select count(`terminal`.`pin`) from `terminal` where ((`terminal`.`restaurant_id` = `r`.`id`) and (`terminal`.`model_id` > 0x7f))) AS `terminal_virtual_num`,(select count(`food`.`food_id`) from `food` where (`food`.`restaurant_id` = `r`.`id`)) AS `food_num`,(select count(`table`.`table_id`) from `table` where (`table`.`restaurant_id` = `r`.`id`)) AS `table_num`,(select count(`order`.`id`) from `order` where ((`order`.`restaurant_id` = `r`.`id`) and (`order`.`total_price` is not null))) AS `order_paid`,(select count(`order_history`.`id`) from `order_history` where ((`order_history`.`restaurant_id` = `r`.`id`) and (`order_history`.`total_price` is not null))) AS `order_history_paid`,(select count(`table`.`table_id`) from `table` where ((`table`.`restaurant_id` = `r`.`id`) and exists(select 1 from `order` where ((`order`.`table_alias` = `table`.`table_alias`) and isnull(`order`.`total_price`) and (`order`.`restaurant_id` = `r`.`id`))))) AS `table_using` from `restaurant` `r`;

-- -----------------------------------------------------
-- View`terminal_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS terminal_view;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `terminal_view` AS select `t`.`pin` AS `pin`,`t`.`restaurant_id` AS `restaurant_id`,`r`.`restaurant_name` AS `restaurant_name`,`t`.`model_name` AS `model_name`,`t`.`model_id` AS `model_id`,(case `t`.`model_id` when 0 then 'BlackBerry' when 1 then 'Android' when 2 then 'iPhone' when 3 then 'WindowsMobile' end) AS `model_id_name`,`t`.`entry_date` AS `entry_date`,`t`.`discard_date` AS `discard_date`,format((((`t`.`idle_duration` / 3600) / 24) / 30),1) AS `idle_month`,format((((`t`.`work_duration` / 3600) / 24) / 30),1) AS `work_month`,`t`.`expire_date` AS `expire_date`,(case when (`t`.`restaurant_id` = 2) then '空闲' when (`t`.`restaurant_id` = 3) then '废弃' when ((`t`.`restaurant_id` > 10) and (now() <= `t`.`expire_date`)) then '使用' when ((`t`.`restaurant_id` > 10) and (now() > `t`.`expire_date`)) then '过期' end) AS `status`,format(((`t`.`work_duration` / (`t`.`work_duration` + `t`.`idle_duration`)) * 100),0) AS `use_rate`,`t`.`owner_name` AS `owner_name`,`t`.`idle_duration` AS `idle_duration`,`t`.`work_duration` AS `work_duration` from (`terminal` `t` left join `restaurant` `r` on((`t`.`restaurant_id` = `r`.`id`)));

