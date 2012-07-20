SET NAMES utf8;
USE wireless_order_db;

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



