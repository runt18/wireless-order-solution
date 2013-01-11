SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Create index on field 'restaurant_id' to table 'order_food'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food` 
ADD INDEX `ix_restaurant_id` (`restaurant_id` ASC) ;

-- -----------------------------------------------------
-- Create index on field 'restaurant_id' to table 'order_food_history'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food_history` 
ADD INDEX `ix_restaurant_id` (`restaurant_id` ASC) ;