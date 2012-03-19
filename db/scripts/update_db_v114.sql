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
-- View `order_food_history_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS order_food_history_view;
DROP VIEW IF EXISTS order_food_view;
DROP VIEW IF EXISTS order_view;
DROP VIEW IF EXISTS order_history_view;
DROP VIEW IF EXISTS restaurant_view;
DROP VIEW IF EXISTS terminal_view;

