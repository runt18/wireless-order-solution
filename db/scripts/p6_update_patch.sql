SET NAMES utf8;

-- -----------------------------------------------------
-- Create table `wireless_order_db`.`temp_order_food_history`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`temp_order_food_history` ;

CREATE TABLE `wireless_order_db`.`temp_order_food_history` (
  `order_id` INT unsigned NOT NULL COMMENT 'the order id to this order detail record',
  `food_id` SMALLINT unsigned NOT NULL COMMENT 'the food id to this order detail record',
  `taste_id` SMALLINT unsigned NOT NULL COMMENT 'the taste id to this order detail record',
  `taste_id2` SMALLINT unsigned NOT NULL COMMENT 'the 2nd taste id to this order detail record',
  `taste_id3` SMALLINT unsigned NOT NULL COMMENT 'the 3rd taste id to this order detail record',
  `name` VARCHAR(45) NOT NULL COMMENT 'the food name to this order detail record',
  `taste` VARCHAR(45) NOT NULL COMMENT 'the taste preference to this order detail record',
  `order_count` DECIMAL(5,2) NOT NULL COMMENT 'the sum of order count to this order detail record',
  `unit_price` DECIMAL(7,2) unsigned NOT NULL COMMENT 'the unit price to this order detail record',
  `taste_price` DECIMAL(7,2) unsigned NOT NULL COMMENT 'the taste price to this order detail record',
  `discount` DECIMAL(3,2) NOT NULL COMMENT 'the discount to this order detail record',
  `food_status` TINYINT NOT NULL COMMENT 'the food status to this order detail record',
  `kitchen` TINYINT unsigned NOT NULL COMMENT 'the kitchen to this order detail record',
  `waiter` VARCHAR(45) NOT NULL COMMENT 'the waiter name to this order detail record',
  PRIMARY KEY (`order_id`,`food_id`,`taste_id`,`taste_id2`,`taste_id3`)) 
  ENGINE = InnoDB
  DEFAULT CHARSET=utf8, 
  COMMENT = 'temporary order food history table for performance problem'  ;


-- -----------------------------------------------------
-- Insert the info to temp_order_food_history
-- -----------------------------------------------------
INSERT INTO temp_order_food_history(order_id,food_id,taste_id,taste_id2,taste_id3,`name`,taste,order_count,unit_price,taste_price,discount,food_status,kitchen,waiter) 
SELECT 
`wireless_order_db`.`order_food_history`.`order_id` AS `order_id`,
`wireless_order_db`.`order_food_history`.`food_id` AS `food_id`,
`wireless_order_db`.`order_food_history`.`taste_id` AS `taste_id`,
`wireless_order_db`.`order_food_history`.`taste_id` AS `taste_id2`,
`wireless_order_db`.`order_food_history`.`taste_id` AS `taste_id3`,
`wireless_order_db`.`order_food_history`.`name` AS `name`,
`wireless_order_db`.`order_food_history`.`taste` AS `taste`,
sum(`wireless_order_db`.`order_food_history`.`order_count`) AS `order_count`,
max(`wireless_order_db`.`order_food_history`.`unit_price`) AS `unit_price`,
max(`wireless_order_db`.`order_food_history`.`taste_price`) AS `taste_price`,
max(`wireless_order_db`.`order_food_history`.`discount`) AS `discount`,
max(`wireless_order_db`.`order_food_history`.`food_status`) AS `food_status`,
max(`wireless_order_db`.`order_food_history`.`kitchen`) AS `kitchen`,
max(`wireless_order_db`.`order_food_history`.`waiter`) AS `waiter`
from `wireless_order_db`.`order_food_history` 
group by `wireless_order_db`.`order_food_history`.`order_id`,
`wireless_order_db`.`order_food_history`.`food_id`,
`wireless_order_db`.`order_food_history`.`taste_id`,
`wireless_order_db`.`order_food_history`.`taste_id2`,
`wireless_order_db`.`order_food_history`.`taste_id3` 
having (sum(`wireless_order_db`.`order_food_history`.`order_count`) > 0);

-- -----------------------------------------------------
-- Rename the index `fk_table_restaurant1` to `fk_table_restaurant`
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`table` 
DROP INDEX `fk_table_restaurant1` 
, ADD INDEX `fk_table_restaurant` (`restaurant_id` ASC) ;

-- -----------------------------------------------------
-- Add the index `ix_table_alias_id`
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`table` 
ADD INDEX `ix_table_alias_id` (`alias_id` ASC) ;

-- -----------------------------------------------------
-- Add the index `ix_food_alias_id`
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`food` 
ADD INDEX `ix_food_alias_id` (`alias_id` ASC) ;

-- -----------------------------------------------------
-- Add the index `ix_terminal_pm`
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`terminal` 
ADD INDEX `ix_terminal_pm` (`pin` ASC, `model_id` ASC) ;

-- -----------------------------------------------------
-- Redefine view `order_history_view` using `temp_order_food_history` instead of `order_food_history_view`
-- and adding the terminal_model
-- -----------------------------------------------------
DROP VIEW IF EXISTS order_history_view;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `order_history_view` AS select `a`.`id` AS `id`,`a`.`table_id` AS `alias_id`,`a`.`table_name` AS `table_name`,`a`.`order_date` AS `order_date`,`a`.`category` AS `category`,(case `a`.`category` when 1 then '一般' when 2 then '外卖' when 3 then '并台' when 4 then '拼台' end) AS `category_name`,`a`.`comment` AS `comment`,`a`.`service_rate` AS `service_rate`,format((sum((((`b`.`unit_price` * `b`.`discount`) + `b`.`taste_price`) * (case when ((`b`.`food_status` & 8) <> 0) then 0 else `b`.`order_count` end))) * (1 + `a`.`service_rate`)),2) AS `total_price`,format((sum(((`b`.`unit_price` * (1 - `b`.`discount`)) * (case when ((`b`.`food_status` & 8) <> 0) then 0 else `b`.`order_count` end))) * (1 + `a`.`service_rate`)),2) AS `total_price_discount`,format((sum(((`b`.`unit_price` * `b`.`discount`) * (case when ((`b`.`food_status` & 8) = 0) then 0 else `b`.`order_count` end))) * (1 + `a`.`service_rate`)),2) AS `total_price_present`,`a`.`custom_num` AS `num`,group_concat(concat((case `b`.`taste_id` when 0 then `b`.`name` else concat(`b`.`name`,'-',`b`.`taste`) end),'|',format(`b`.`order_count`,2),'|',(case `b`.`food_status` when 1 then '(特)' when 2 then '(荐)' when 3 then '(特,荐)' when 8 then '(赠)' when 9 then '(特,赠)' when 10 then '(荐,赠)' when 11 then '(特,荐,赠)' else '' end),'|',(case when (`b`.`discount` < 1) then concat('(',format((`b`.`discount` * 10),1),'折',')') else '' end),'|',format((((`b`.`unit_price` * `b`.`discount`) + `b`.`taste_price`) * `b`.`order_count`),2)) separator ';') AS `foods`,(`a`.`total_price` is not null) AS `is_paid`,`a`.`total_price_2` AS `total_price_2`,`a`.`restaurant_id` AS `restaurant_id`,`r`.`restaurant_name` AS `restaurant_name`,`d`.`id` AS `table_id`,`a`.`waiter` AS `waiter`,`a`.`type` AS `type_value`,(case `a`.`type` when 1 then '现金' when 2 then '刷卡' when 3 then '会员卡' when 4 then '签单' when 5 then '挂账' end) AS `type_name` from (((((`order_history` `a` left join `temp_order_food_history` `b` on((`a`.`id` = `b`.`order_id`))) left join `food` `c` on(((`b`.`food_id` = `c`.`alias_id`) and (`c`.`restaurant_id` = `a`.`restaurant_id`)))) left join `table` `d` on(((`a`.`table_id` = `d`.`alias_id`) and (`d`.`restaurant_id` = `a`.`restaurant_id`)))) left join `restaurant` `r` on((`a`.`restaurant_id` = `r`.`id`))) left join `terminal` `t` on((`a`.`terminal_pin` = `t`.`pin`) and (`t`.model_id = `a`.terminal_model))) group by `a`.`id`;

-- -----------------------------------------------------
-- Redefine view `order_view` adding the terminal_model
-- -----------------------------------------------------
DROP VIEW IF EXISTS order_view;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `order_view` AS select `a`.`id` AS `id`,`a`.`table_id` AS `alias_id`,`a`.`table_name` AS `table_name`,`a`.`order_date` AS `order_date`,`a`.`category` AS `category`,(case `a`.`category` when 1 then '一般' when 2 then '外卖' when 3 then '并台' when 4 then '拼台' end) AS `category_name`,`a`.`comment` AS `comment`,`a`.`service_rate` AS `service_rate`,format((sum((((`b`.`unit_price` * `b`.`discount`) + `b`.`taste_price`) * (case when ((`b`.`food_status` & 8) <> 0) then 0 else `b`.`order_count` end))) * (1 + `a`.`service_rate`)),2) AS `total_price`,format((sum(((`b`.`unit_price` * (1 - `b`.`discount`)) * (case when ((`b`.`food_status` & 8) <> 0) then 0 else `b`.`order_count` end))) * (1 + `a`.`service_rate`)),2) AS `total_price_discount`,format((sum(((`b`.`unit_price` * `b`.`discount`) * (case when ((`b`.`food_status` & 8) = 0) then 0 else `b`.`order_count` end))) * (1 + `a`.`service_rate`)),2) AS `total_price_present`,`a`.`custom_num` AS `num`,group_concat(concat((case `b`.`taste_id` when 0 then `b`.`name` else concat(`b`.`name`,'-',`b`.`taste`) end),'|',format(`b`.`order_count`,2),'|',(case `b`.`food_status` when 1 then '(特)' when 2 then '(荐)' when 3 then '(特,荐)' when 8 then '(赠)' when 9 then '(特,赠)' when 10 then '(荐,赠)' when 11 then '(特,荐,赠)' else '' end),'|',(case when (`b`.`discount` < 1) then concat('(',format((`b`.`discount` * 10),1),'折',')') else '' end),'|',format((((`b`.`unit_price` * `b`.`discount`) + `b`.`taste_price`) * `b`.`order_count`),2)) separator ';') AS `foods`,(`a`.`total_price` is not null) AS `is_paid`,`a`.`total_price_2` AS `total_price_2`,`a`.`restaurant_id` AS `restaurant_id`,`r`.`restaurant_name` AS `restaurant_name`,`d`.`id` AS `table_id`,`a`.`waiter` AS `waiter`,`a`.`type` AS `type_value`,(case `a`.`type` when 1 then '现金' when 2 then '刷卡' when 3 then '会员卡' when 4 then '签单' when 5 then '挂账' end) AS `type_name` from (((((`order` `a` left join `order_food_view` `b` on((`a`.`id` = `b`.`order_id`))) left join `food` `c` on(((`b`.`food_id` = `c`.`alias_id`) and (`c`.`restaurant_id` = `a`.`restaurant_id`)))) left join `table` `d` on(((`a`.`table_id` = `d`.`alias_id`) and (`d`.`restaurant_id` = `a`.`restaurant_id`)))) left join `restaurant` `r` on((`a`.`restaurant_id` = `r`.`id`))) left join `terminal` `t` on((`a`.`terminal_pin` = `t`.`pin`) and (`t`.model_id = `a`.terminal_model))) group by `a`.`id`;