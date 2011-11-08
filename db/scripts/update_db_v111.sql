SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Drop table 'material_history'
-- -----------------------------------------------------
DROP TABLE `wireless_order_db`.`material_history`;

-- -----------------------------------------------------
-- Drop table 'order_food_material'
-- -----------------------------------------------------
DROP TABLE `wireless_order_db`.`order_food_material`;

-- -----------------------------------------------------
-- Drop table 'order_food_material_history'
-- -----------------------------------------------------
DROP TABLE `wireless_order_db`.`order_food_material_history`;

-- -----------------------------------------------------
-- Recreate table 'food_material'
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`food_material` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`food_material` (
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `material_id` SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the material alias id' ,
  `food_id` SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the food alias id' ,
  `consumption` FLOAT NOT NULL DEFAULT 0 COMMENT 'the consumption between the food and the material' ,
  INDEX `fk_food_material_restaurant` (`restaurant_id` ASC) ,
  CONSTRAINT `fk_food_material_restaurant1`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the releation ship between food and material' ;

-- -----------------------------------------------------
-- Recreate table `material`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`material` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`material` (
  `restaurant_id` INT UNSIGNED NOT NULL COMMENT 'the id to related restaurant' ,
  `material_id` SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the alias id to this material' ,
  `supplier_id` SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the alias supplier id this material belog to' ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name to this material' ,
  `stock` FLOAT NOT NULL DEFAULT 0 COMMENT 'the remaining amount to this material' ,
  `price` DECIMAL(7,2) NOT NULL DEFAULT 0 COMMENT 'the unit price to this material' ,
  `warning_threshold` FLOAT NOT NULL DEFAULT 0 COMMENT 'the warning threshold to this material' ,
  `danger_threshold` FLOAT NOT NULL DEFAULT 0 COMMENT 'the danger threshold to this material' ,
  INDEX `fk_material_restaurant` (`restaurant_id` ASC) ,
  PRIMARY KEY (`material_id`, `restaurant_id`) ,
  CONSTRAINT `fk_material_restaurant`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the material information.' ;


-- -----------------------------------------------------
-- Create table 'department' instead of 'super_kitchen'
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`super_kitchen` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`department` (
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `dept_id` TINYINT UNSIGNED NOT NULL DEFAULT 0 ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' ,
  INDEX `fk_super_kitchen_restaurant` (`restaurant_id` ASC) ,
  PRIMARY KEY (`dept_id`, `restaurant_id`) ,
  CONSTRAINT `fk_super_kitchen_restaurant1`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the department information' ;

-- -----------------------------------------------------
-- Change the field 'super_kitchen' to 'dept_id' in table 'kitchen'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`kitchen` CHANGE COLUMN `super_kitchen` `dept_id` TINYINT(3) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the department that this kitchen belong to.'  ;

-- -----------------------------------------------------
-- Insert a '仓管部' to 'department' for every restaurant
-- -----------------------------------------------------
INSERT INTO wireless_order_db.department(restaurant_id, dept_id, name)
SELECT id, 0, "仓管部" FROM wireless_order_db.restaurant WHERE id > 10;

-- -----------------------------------------------------
-- Add a 'restaurant_id' to table 'order_food'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food` ADD COLUMN `restaurant_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the restaurant id to this order detail'  AFTER `order_id` ;

-- -----------------------------------------------------
-- Insert restaurant id to each order food record
-- -----------------------------------------------------
UPDATE wireless_order_db.order_food A SET restaurant_id=(
SELECT restaurant_id FROM wireless_order_db.order B WHERE A.order_id=B.id);

-- -----------------------------------------------------
-- Add a 'restaurant_id' to table 'order_food_history'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food_history` ADD COLUMN `restaurant_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the restaurant id to this order detail'  AFTER `order_id` ;

-- -----------------------------------------------------
-- Insert restaurant id to each order food history record
-- -----------------------------------------------------
UPDATE wireless_order_db.order_food_history A SET restaurant_id=(
SELECT restaurant_id FROM wireless_order_db.order_history B WHERE A.order_id=B.id);