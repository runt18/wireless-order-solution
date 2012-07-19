SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`food_taste`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`food_taste` (
  `food_id` INT NULL DEFAULT NULL COMMENT 'the food id' ,
  `taste_id` INT NULL DEFAULT NULL COMMENT 'the taste id' ,
  `restaurant_id` INT NULL DEFAULT NULL COMMENT 'the restaurant id' ,
  `rank` TINYINT UNSIGNED NULL DEFAULT 0 COMMENT 'the rank of taste to this food' ,
  INDEX `ix_food_id` (`food_id` ASC) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the rank of taste to each food' ;

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



