SET NAMES utf8;
use `wireless_order_db`;

-- -----------------------------------------------------
-- Add the `discount_type` to `order`
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order` ADD COLUMN `discount_type` TINYINT NOT NULL DEFAULT 1 COMMENT 'the discount type to this order'  AFTER `type` ;

-- -----------------------------------------------------
-- Add the `discount_type` to `order_history`
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_history` ADD COLUMN `discount_type` TINYINT NOT NULL DEFAULT 1 COMMENT 'the discount type to this order'  AFTER `type` ;

-- -----------------------------------------------------
-- Add the `taste_id2` and `taste_id3` to `order_food_history`
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food_history` ADD COLUMN `taste_id2` SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the 2nd taste id to this order food record'  AFTER `taste` , ADD COLUMN `taste_id3` SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the 3rd taste id to this order food record'  AFTER `taste_id2` ;

-- -----------------------------------------------------
-- View`order_food_history_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS order_food_history_view;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `order_food_history_view` AS select sum(`order_food_history`.`order_count`) AS `order_count`,max(`order_food_history`.`unit_price`) AS `unit_price`,max(`order_food_history`.`taste_price`) AS `taste_price`,max(`order_food_history`.`name`) AS `name`,max(`order_food_history`.`taste`) AS `taste`,max(`order_food_history`.`taste_id`) AS `taste_id`,max(`order_food_history`.`discount`) AS `discount`,max(`order_food_history`.`food_status`) AS `food_status`,max(`order_food_history`.`kitchen`) AS `kitchen`,max(`order_food_history`.`waiter`) AS `waiter`,`order_food_history`.`order_id` AS `order_id`,`order_food_history`.`food_id` AS `food_id` from `order_food_history` group by `order_food_history`.`order_id`,`order_food_history`.`food_id`,`order_food_history`.`taste_id`,`order_food_history`.`taste_id2`,`order_food_history`.`taste_id3` having (sum(`order_food_history`.`order_count`) > 0);

-- -----------------------------------------------------
-- View`order_food_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS order_food_view;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `order_food_view` AS select sum(`order_food`.`order_count`) AS `order_count`,max(`order_food`.`unit_price`) AS `unit_price`,max(`order_food`.`taste_price`) AS `taste_price`,max(`order_food`.`name`) AS `name`,max(`order_food`.`taste`) AS `taste`,max(`order_food`.`taste_id`) AS `taste_id`,max(`order_food`.`discount`) AS `discount`,max(`order_food`.`food_status`) AS `food_status`,`order_food`.`order_id` AS `order_id`,`order_food`.`food_id` AS `food_id` from `order_food` group by `order_food`.`order_id`,`order_food`.`food_id`,`order_food`.`taste_id`,`order_food`.`taste_id2`,`order_food`.`taste_id3` having (sum(`order_food`.`order_count`) > 0);
