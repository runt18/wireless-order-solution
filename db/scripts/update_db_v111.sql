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
  `material_id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'the id to this material' ,
  `restaurant_id` INT UNSIGNED NOT NULL COMMENT 'the id to related restaurant' ,
  `material_alias` SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the alias id to this material' ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name to this material' ,
  `warning_threshold` FLOAT NOT NULL DEFAULT 0 COMMENT 'the warning threshold to this material' ,
  `danger_threshold` FLOAT NOT NULL DEFAULT 0 COMMENT 'the danger threshold to this material' ,
  INDEX `fk_material_restaurant` (`restaurant_id` ASC) ,
  PRIMARY KEY (`material_id`) ,
  CONSTRAINT `fk_material_restaurant`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the material information.' ;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`material_dept`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`material_dept` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`material_dept` (
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `material_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the id to material ' ,
  `dept_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the id to department' ,
  `dept_name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name to department' ,
  `material_name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name to material' ,
  `price` DECIMAL(7,2) NOT NULL DEFAULT 0 COMMENT 'the price to this material' ,
  `stock` FLOAT NOT NULL DEFAULT 0 COMMENT 'the stock to this material' ,
  INDEX `fk_material_dept_restaurant1` (`restaurant_id` ASC) ,
  CONSTRAINT `fk_material_dept_restaurant1`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the stock to each material of department' ;

-- -----------------------------------------------------
-- Create table `wireless_order_db`.`material_detail`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`material_detail` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`material_detail` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `supplier_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the supplier alias id that this material detail record belong to' ,
  `material_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the material alias id that this material detail record belong to' ,
  `price` DECIMAL(7,2) NOT NULL DEFAULT 0 COMMENT 'the price to this material record' ,
  `date` DATETIME NULL DEFAULT NULL COMMENT 'the date to this material detail record' ,
  `staff` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the staff name to this material detail record' ,
  `dept_id` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the super kitchen id to this material detail record' ,
  `dept2_id` TINYINT UNSIGNED NULL DEFAULT NULL COMMENT 'indicates the 调入部门 in case of the type is “调出”' ,
  `amount` FLOAT NOT NULL DEFAULT 0 COMMENT 'the amount to this material detail record' ,
  `type` TINYINT NOT NULL DEFAULT 0 COMMENT 'the type is as below.\n0 : 消耗\n1 : 报损 \n2 : 销售\n3 : 退货\n4 : 入库\n5 : 调出\n6 : 调入\n7 : 盘点' ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_material_detail_restaurant` (`restaurant_id` ASC) ,
  CONSTRAINT `fk_material_detail_restaurant`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

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
-- Table `wireless_order_db`.`supplier`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`supplier` ;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`supplier`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`supplier` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`supplier` (
  `supplier_id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'the id to this supplier' ,
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `supplier_alias` SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the alias id to this supplier' ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name to ths supplier' ,
  `tele` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the telephone to this supplier' ,
  `addr` VARCHAR(100) NOT NULL DEFAULT '' COMMENT 'the address to this supplier' ,
  `contact` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the contact person to this supplier' ,
  INDEX `fk_supplier_restaurant` (`restaurant_id` ASC) ,
  PRIMARY KEY (`supplier_id`) ,
  CONSTRAINT `fk_supplier_restaurant1`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

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