SET NAMES utf8;

-- -----------------------------------------------------
-- Add the field "hang_status" to table "order_food"
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food` ADD COLUMN `hang_status` TINYINT NOT NULL DEFAULT 0 COMMENT 'indicates hang up status.\n0 - normal\n1 - hang_up\n2 - immediate'  AFTER `food_status` ;

-- -----------------------------------------------------
-- Add the field "super_kitchen" to table "kitchen"
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`kitchen` ADD COLUMN `super_kitchen` TINYINT UNSIGNED NOT NULL DEFAULT 255 COMMENT 'the super to this kitchen. 255 means NOT belong to any super kitchen.'  AFTER `alias_id` ;

-- -----------------------------------------------------
-- Add the field "region" to table "table"
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`table` ADD COLUMN `region` TINYINT UNSIGNED NOT NULL DEFAULT 255 COMMENT 'the region alias id to this table. 255 means the table does NOT belong to any region.'  AFTER `restaurant_id` ;

-- -----------------------------------------------------
-- Create table `wireless_order_db`.`super_kitchen`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`super_kitchen` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`super_kitchen` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `alias_id` TINYINT UNSIGNED NULL ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_super_kitchen_restaurant1` (`restaurant_id` ASC) ,
  CONSTRAINT `fk_super_kitchen_restaurant1`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the super kitchen information' ;

-- -----------------------------------------------------
-- Create table `wireless_order_db`.`region`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`region` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`region` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'the id to this record' ,
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `alias_id` TINYINT UNSIGNED NULL COMMENT 'the alias id to this table region' ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name to this table region' ,
  PRIMARY KEY (`id`, `restaurant_id`) ,
  INDEX `fk_region_restaurant1` (`restaurant_id` ASC) ,
  CONSTRAINT `fk_region_restaurant1`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the region information to the tables' ;

