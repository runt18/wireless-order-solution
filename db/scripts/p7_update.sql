SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Add the field "is_temporary" to table "temp_order_food_history"
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`temp_order_food_history` ADD COLUMN `is_temporary` TINYINT NOT NULL DEFAULT 0 COMMENT 'the flag indicates whether the food to this record is temporary'  AFTER `taste_id3` ;

-- -----------------------------------------------------
-- Add the field "is_temporary" to table "order_food"
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food` ADD COLUMN `is_temporary` TINYINT NOT NULL DEFAULT 0 COMMENT 'indicates whether the food to this record is temporary'  AFTER `waiter` ;

-- -----------------------------------------------------
-- Add the field "is_temporary" to table "order_food_history"
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food_history` ADD COLUMN `is_temporary` TINYINT NOT NULL DEFAULT 0 COMMENT 'indicates whether the food to this record is temporary'  AFTER `waiter` ;

-- -----------------------------------------------------
-- Add the field "hang_status" to table "order_food"
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food` ADD COLUMN `hang_status` TINYINT NOT NULL DEFAULT 0 COMMENT 'indicates hang up status.\n0 - normal\n1 - hang_up\n2 - immediate'  AFTER `food_status` ;

-- -----------------------------------------------------
-- Add the field "super_kitchen" to table "kitchen"
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`kitchen` ADD COLUMN `super_kitchen` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the super kitchen that this kitchen belong to.'  AFTER `alias_id` ;

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

-- -----------------------------------------------------
-- Redefine the `order_food_history_view` to add the `is_temporary` as one of the GROUP BY condition
-- -----------------------------------------------------
DROP VIEW IF EXISTS order_food_history_view;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `order_food_history_view` AS select sum(`order_food_history`.`order_count`) AS `order_count`,max(`order_food_history`.`unit_price`) AS `unit_price`,max(`order_food_history`.`taste_price`) AS `taste_price`,max(`order_food_history`.`name`) AS `name`,max(`order_food_history`.`taste`) AS `taste`,max(`order_food_history`.`taste_id`) AS `taste_id`,max(`order_food_history`.`discount`) AS `discount`,max(`order_food_history`.`food_status`) AS `food_status`,max(`order_food_history`.`kitchen`) AS `kitchen`,max(`order_food_history`.`waiter`) AS `waiter`,`order_food_history`.`order_id` AS `order_id`,`order_food_history`.`food_id` AS `food_id` from `order_food_history` group by `order_food_history`.`order_id`,`order_food_history`.`food_id`,`order_food_history`.`taste_id`,`order_food_history`.`taste_id2`,`order_food_history`.`taste_id3`,`order_food_history`.`is_temporary` having (sum(`order_food_history`.`order_count`) > 0);

-- -----------------------------------------------------
-- Redefine the `order_food_view` to add the `is_temporary` as one of the GROUP BY condition
-- -----------------------------------------------------
DROP VIEW IF EXISTS order_food_view;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `order_food_view` AS select sum(`order_food`.`order_count`) AS `order_count`,max(`order_food`.`unit_price`) AS `unit_price`,max(`order_food`.`taste_price`) AS `taste_price`,max(`order_food`.`name`) AS `name`,max(`order_food`.`taste`) AS `taste`,max(`order_food`.`taste_id`) AS `taste_id`,max(`order_food`.`discount`) AS `discount`,max(`order_food`.`food_status`) AS `food_status`,`order_food`.`order_id` AS `order_id`,`order_food`.`food_id` AS `food_id` from `order_food` group by `order_food`.`order_id`,`order_food`.`food_id`,`order_food`.`taste_id`,`order_food`.`taste_id2`,`order_food`.`taste_id3`,`order_food`.`is_temporary` having (sum(`order_food`.`order_count`) > 0);
