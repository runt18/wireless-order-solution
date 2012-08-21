SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`combo`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`combo` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`combo` (
  `food_id` INT NULL DEFAULT NULL COMMENT 'the main food id to this combo' ,
  `sub_food_id` INT NULL DEFAULT NULL COMMENT 'the sub food id to this combo' ,
  `restaurant_id` INT NULL DEFAULT NULL COMMENT 'the restaurant id' ,
  `amount` SMALLINT UNSIGNED NULL DEFAULT 1 COMMENT 'the amount of sub food to this combo' ,
  INDEX `ix_food_id` (`food_id` ASC) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the combo information' ;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`food_taste_rank`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`food_taste_rank` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`food_taste_rank` (
  `food_id` INT NULL DEFAULT NULL COMMENT 'the food id' ,
  `taste_id` INT NULL DEFAULT NULL COMMENT 'the taste id' ,
  `restaurant_id` INT NULL DEFAULT NULL COMMENT 'the restaurant id' ,
  `rank` SMALLINT UNSIGNED NULL DEFAULT 0 COMMENT 'the rank of taste reference to this food. ' ,
  INDEX `ix_food_id` (`food_id` ASC) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the rank of taste reference to each food' ;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`food_taste`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`food_taste` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`food_taste` (
  `food_id` INT NULL DEFAULT NULL COMMENT 'the food id' ,
  `taste_id` INT NULL DEFAULT NULL COMMENT 'the taste id' ,
  `restaurant_id` INT NULL DEFAULT NULL COMMENT 'the restaurant id' ,
  `ref_cnt` INT UNSIGNED NULL DEFAULT 0 COMMENT 'the reference count of taste to this food' ,
  INDEX `ix_food_id` (`food_id` ASC) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the rank of taste to each food' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`kitchen_taste`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`kitchen_taste` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`kitchen_taste` (
  `kitchen_id` INT NULL DEFAULT NULL COMMENT 'the kitchen id' ,
  `taste_id` INT NULL DEFAULT NULL COMMENT 'the taste id' ,
  `restaurant_id` INT NULL DEFAULT NULL COMMENT 'the restaurant id' ,
  `ref_cnt` INT UNSIGNED NULL DEFAULT 0 COMMENT 'the reference count of taste to this kitchen' ,
  INDEX `ix_kitchen_id` (`kitchen_id` ASC) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the taste reference information to each kitchen' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`dept_taste`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`dept_taste` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`dept_taste` (
  `dept_id` INT NULL DEFAULT NULL COMMENT 'the department id' ,
  `taste_id` INT NULL DEFAULT NULL COMMENT 'the taste id' ,
  `restaurant_id` INT NULL DEFAULT NULL COMMENT 'the restaurant id' ,
  `ref_cnt` INT UNSIGNED NULL DEFAULT 0 COMMENT 'the reference count of taste to this department' ,
  INDEX `ix_dept_id` (`dept_id` ASC) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the taste reference information to each department' ;

-- -----------------------------------------------------
-- Create the index 'ix_food_id' to table 'order_food_history'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food_history` 
ADD INDEX `ix_food_id` (`food_id` ASC) ;

-- -----------------------------------------------------
-- Drop the foreign key of 'region' to 'restaurant'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`region` DROP FOREIGN KEY `fk_region_restaurant1` ;
ALTER TABLE `wireless_order_db`.`region` 
DROP INDEX `fk_region_restaurant` ;

-- -----------------------------------------------------
-- Drop the foreign key of 'setting' to 'restaurant'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`setting` DROP FOREIGN KEY `fk_setting_restaurant` ;
ALTER TABLE `wireless_order_db`.`setting` ADD COLUMN `setting_id` INT NOT NULL AUTO_INCREMENT  FIRST 
, ADD PRIMARY KEY (`setting_id`) 
, ADD INDEX `ix_restaurant_id` (`restaurant_id` ASC) 
, DROP INDEX `fk_setting_restaurant` ;

-- -----------------------------------------------------
-- Add the field 'taste_ref_type' to table 'food'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`food` CHANGE COLUMN `enabled` `taste_ref_type` TINYINT(4) NOT NULL DEFAULT '1' COMMENT 'the taste reference type to this food is below.\n1 - smart reference\n2 - manual reference'  ;

-- -----------------------------------------------------
-- Drop the field 'img1..3' to table 'food'
-- Add the field 'desc' to table 'food' 
-- Add the field 'img' to table 'food'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`food` DROP COLUMN `img3` , 
DROP COLUMN `img2` , 
DROP COLUMN `img1` , 
ADD COLUMN `desc` VARCHAR(500) NULL DEFAULT NULL COMMENT 'the description to this food'  AFTER `taste_ref_type` , 
ADD COLUMN `img` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the image to this food'  AFTER `desc` ;

-- -----------------------------------------------------
-- Add the field 'type' to table 'taste'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`taste` ADD COLUMN `type` TINYINT NOT NULL DEFAULT 0 COMMENT 'the type to this taste as below.\n0 - normal\n1 - reserved'  AFTER `calc` ;

-- -----------------------------------------------------
-- Add the '例牌' to each restaurant's taste
-- -----------------------------------------------------
INSERT INTO wireless_order_db.taste (`restaurant_id`, `taste_alias`, `preference`, `category`, `calc`, `type`)
SELECT id, 0, '例牌', 2, 1, 1 FROM wireless_order_db.restaurant WHERE id > 10;

-- -----------------------------------------------------
-- Add the '中牌' to each restaurant's taste
-- -----------------------------------------------------
INSERT INTO wireless_order_db.taste (`restaurant_id`, `taste_alias`, `preference`, `category`, `calc`, `type`)
SELECT id, 0, '中牌', 2, 1, 1 FROM wireless_order_db.restaurant WHERE id > 10;

-- -----------------------------------------------------
-- Add the '大牌' to each restaurant's taste
-- -----------------------------------------------------
INSERT INTO wireless_order_db.taste (`restaurant_id`, `taste_alias`, `preference`, `category`, `calc`, `type`)
SELECT id, 0, '大牌', 2, 1, 1 FROM wireless_order_db.restaurant WHERE id > 10;

-- -----------------------------------------------------
-- Change the all the '做法' to '口味'
-- -----------------------------------------------------
UPDATE `wireless_order_db`.`taste` SET 
category = 0,rate = 0,calc = 0
WHERE category = 1
