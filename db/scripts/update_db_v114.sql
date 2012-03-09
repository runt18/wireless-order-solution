SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Rename the field 'id' of table 'table' to 'table_id'
-- Rename the field 'alias_id' to 'table_alias'
-- Create the index associated with 'restaurant_id' and 'table_alias'
-- Drop the foreign key to 'restaurant_id'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`table` DROP FOREIGN KEY `fk_table_restaurant1` ;

ALTER TABLE `wireless_order_db`.`table` CHANGE COLUMN `id` `table_id` INT(11) NOT NULL AUTO_INCREMENT COMMENT 'the id to this table'  , CHANGE COLUMN `alias_id` `table_alias` SMALLINT(5) UNSIGNED NULL DEFAULT NULL  

, DROP PRIMARY KEY 

, ADD PRIMARY KEY (`table_id`) 

, DROP INDEX `ix_table_alias_id` 

, ADD INDEX `ix_table_alias_id` (`restaurant_id` ASC, `table_alias` ASC) 

, DROP INDEX `fk_table_restaurant` ;



-- -----------------------------------------------------
-- Rename the field 'table_id' of table 'order' to 'table_alias'
-- Create the index associated with 'restaurant_id'
-- Drop the foreign key to 'restaurant_id'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order` DROP FOREIGN KEY `fk_order_restaurant` ;

ALTER TABLE `wireless_order_db`.`order` CHANGE COLUMN `table_id` `table_alias` SMALLINT(6) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the table alias id to this order'  , CHANGE COLUMN `table2_id` `table2_alias` SMALLINT(6) UNSIGNED NULL DEFAULT NULL COMMENT 'the 2nd table alias id to this order(used for table merger)'  

, ADD INDEX `ix_order_restaurant` USING BTREE (`restaurant_id` ASC) 

, DROP INDEX `fk_order_restaurant` ;

-- -----------------------------------------------------
-- Rename the field 'table_id' of table 'order_history' to 'table_alias'
-- Create the index associated with 'restaurant_id'
-- Drop the foreign key to 'restaurant_id'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_history` DROP FOREIGN KEY `fk_order_restaurant0` ;

ALTER TABLE `wireless_order_db`.`order_history` CHANGE COLUMN `table_id` `table_alias` SMALLINT(6) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the table alias id to this order'  AFTER `comment` , CHANGE COLUMN `table2_id` `table2_alias` SMALLINT(6) UNSIGNED NULL DEFAULT NULL COMMENT 'the 2nd table alias id to this order(used for table merger)'  

, ADD INDEX `ix_order_history_restaurant` USING BTREE (`restaurant_id` ASC) 

, DROP INDEX `fk_order_restaurant` ;

-- -----------------------------------------------------
-- Add the field 'table_id' and 'table2_id' to table 'order'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order` ADD COLUMN `table_id` INT NOT NULL DEFAULT 0 COMMENT 'the table id to this order'  AFTER `region_name` , ADD COLUMN `table2_id` INT NULL DEFAULT NULL COMMENT 'the 2nd table id to this order'  AFTER `table_name` ;

-- -----------------------------------------------------
-- Add the field 'table_id' and 'table2_id' to table 'order_history'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_history` ADD COLUMN `table_id` INT NOT NULL DEFAULT 0 COMMENT 'the table id to this order'  AFTER `region_name` , ADD COLUMN `table2_id` INT NULL DEFAULT NULL COMMENT 'the 2nd table id to this order'  AFTER `table_name` ;

-- -----------------------------------------------------
-- Update the field 'table_id' in table 'order'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order` CHANGE COLUMN `table_id` `table_id` INT(11) NULL DEFAULT '0' COMMENT 'the table id to this order'  ;
UPDATE `wireless_order_db`.`order` AS a SET table_id = (SELECT table_id FROM `wireless_order_db`.`table` AS b WHERE a.table_alias = b.table_alias AND a.restaurant_id = b.restaurant_id);
UPDATE `wireless_order_db`.`order` SET table_id=0 WHERE table_id IS NULL;
ALTER TABLE `wireless_order_db`.`order` CHANGE COLUMN `table_id` `table_id` INT(11) NOT NULL DEFAULT '0' COMMENT 'the table id to this order'  ;

-- -----------------------------------------------------
-- Update the field 'table2_id' in table 'order'
-- -----------------------------------------------------
UPDATE `wireless_order_db`.`order` AS a SET table2_id = (SELECT table_id FROM `wireless_order_db`.`table` AS b WHERE a.table2_alias = b.table_alias AND a.restaurant_id = b.restaurant_id);

-- -----------------------------------------------------
-- Update the field 'table_id' in table 'order_history'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_history` CHANGE COLUMN `table_id` `table_id` INT(11) NULL DEFAULT '0' COMMENT 'the table id to this order'  ;
UPDATE `wireless_order_db`.`order_history` AS a SET table_id = (SELECT table_id FROM `wireless_order_db`.`table` AS b WHERE a.table_alias = b.table_alias AND a.restaurant_id = b.restaurant_id);
UPDATE `wireless_order_db`.`order_history` SET table_id=0 WHERE table_id IS NULL;
ALTER TABLE `wireless_order_db`.`order_history` CHANGE COLUMN `table_id` `table_id` INT(11) NOT NULL DEFAULT '0' COMMENT 'the table id to this order'  ;

-- -----------------------------------------------------
-- Update the field 'table2_id' in table 'order_history'
-- -----------------------------------------------------
UPDATE `wireless_order_db`.`order_history` AS a SET table2_id = (SELECT table_id FROM `wireless_order_db`.`table` AS b WHERE a.table2_alias = b.table_alias AND a.restaurant_id = b.restaurant_id);

-- -----------------------------------------------------
-- Drop the foreign key to restaurant.
-- Rename the field 'id' to 'kitchen_id'.
-- Rename the field 'alias_id' to 'kitchen_alias'
-- Create the index associates with both restaurant_id and kitchen_alias
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`kitchen` DROP FOREIGN KEY `fk_kitchen_restaurant1` ;

ALTER TABLE `wireless_order_db`.`kitchen` CHANGE COLUMN `id` `kitchen_id` INT(11) NOT NULL AUTO_INCREMENT COMMENT 'the id to this kitchen'  , CHANGE COLUMN `alias_id` `kitchen_alias` TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the alias id to this kitchen'  

, DROP PRIMARY KEY 

, ADD PRIMARY KEY (`kitchen_id`) 

, ADD INDEX `ix_kitchen_alias_id` USING BTREE (`restaurant_id` ASC, `kitchen_alias` ASC) 

, DROP INDEX `fk_kitchen_restaurant1` ;

-- -----------------------------------------------------
-- Add the field 'kitchen_id' to table 'order_food'.
-- Rename the field 'kitchen' to 'kitchen_alias'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food` 
ADD COLUMN `kitchen_id` INT NULL DEFAULT NULL COMMENT 'the kitchen id which the order food of this record belong to. '  AFTER `discount` , CHANGE COLUMN `kitchen` `kitchen_alias` TINYINT(3) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the kitchen alias id which the order food of this record belong to.'  ;

-- -----------------------------------------------------
-- Add the field 'kitchen_id' to table 'order_food'.
-- Rename the field 'kitchen' to 'kitchen_alias'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_food_history` 
ADD COLUMN `kitchen_id` INT NULL DEFAULT NULL COMMENT 'the kitchen id which the order food of this record belong to. '  AFTER `discount` , CHANGE COLUMN `kitchen` `kitchen_alias` TINYINT(3) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the kitchen alias id which the order food of this record belong to.'  ;

-- -----------------------------------------------------
-- Update the field 'kitchen_id' to table 'order_food'
-- -----------------------------------------------------
UPDATE wireless_order_db.order_food AS a SET kitchen_id = (SELECT kitchen_id FROM wireless_order_db.kitchen b WHERE b.restaurant_id=a.restaurant_id AND b.kitchen_alias=a.kitchen_alias);

-- -----------------------------------------------------
-- Update the field 'kitchen_id' to table 'order_food_history'
-- -----------------------------------------------------
UPDATE wireless_order_db.order_food_history AS a SET kitchen_id = (SELECT kitchen_id FROM wireless_order_db.kitchen b WHERE b.restaurant_id=a.restaurant_id AND b.kitchen_alias=a.kitchen_alias);


-- -----------------------------------------------------
-- View `order_food_history_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS order_food_history_view;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `order_food_history_view` AS select sum(`order_food_history`.`order_count`) AS `order_count`,max(`order_food_history`.`unit_price`) AS `unit_price`,max(`order_food_history`.`taste_price`) AS `taste_price`,max(`order_food_history`.`name`) AS `name`,max(`order_food_history`.`taste`) AS `taste`,max(`order_food_history`.`taste_id`) AS `taste_id`,max(`order_food_history`.`discount`) AS `discount`,max(`order_food_history`.`food_status`) AS `food_status`,max(`order_food_history`.`kitchen_alias`) AS `kitchen`,max(`order_food_history`.`waiter`) AS `waiter`,`order_food_history`.`order_id` AS `order_id`,`order_food_history`.`food_id` AS `food_id` from `order_food_history` group by `order_food_history`.`order_id`,`order_food_history`.`food_id`,`order_food_history`.`taste_id`,`order_food_history`.`taste_id2`,`order_food_history`.`taste_id3`,`order_food_history`.`is_temporary` having (sum(`order_food_history`.`order_count`) > 0);

-- -----------------------------------------------------
-- View `order_food_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS order_food_view;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `order_food_view` AS select sum(`order_food`.`order_count`) AS `order_count`,max(`order_food`.`unit_price`) AS `unit_price`,max(`order_food`.`taste_price`) AS `taste_price`,max(`order_food`.`name`) AS `name`,max(`order_food`.`taste`) AS `taste`,max(`order_food`.`taste_id`) AS `taste_id`,max(`order_food`.`discount`) AS `discount`,max(`order_food`.`food_status`) AS `food_status`,`order_food`.`order_id` AS `order_id`,`order_food`.`food_id` AS `food_id` from `order_food` group by `order_food`.`order_id`,`order_food`.`food_id`,`order_food`.`taste_id`,`order_food`.`taste_id2`,`order_food`.`taste_id3`,`order_food`.`is_temporary` having (sum(`order_food`.`order_count`) > 0);

-- -----------------------------------------------------
-- View `order_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS order_view;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `order_view` AS select `a`.`id` AS `id`,`a`.`table_alias` AS `alias_id`,`a`.`table_name` AS `table_name`,`a`.`order_date` AS `order_date`,`a`.`category` AS `category`,(case `a`.`category` when 1 then '一般' when 2 then '外卖' when 3 then '并台' when 4 then '拼台' end) AS `category_name`,`a`.`comment` AS `comment`,`a`.`service_rate` AS `service_rate`,format((sum((((`b`.`unit_price` * `b`.`discount`) + `b`.`taste_price`) * (case when ((`b`.`food_status` & 8) <> 0) then 0 else `b`.`order_count` end))) * (1 + `a`.`service_rate`)),2) AS `total_price`,format((sum(((`b`.`unit_price` * (1 - `b`.`discount`)) * (case when ((`b`.`food_status` & 8) <> 0) then 0 else `b`.`order_count` end))) * (1 + `a`.`service_rate`)),2) AS `total_price_discount`,format((sum(((`b`.`unit_price` * `b`.`discount`) * (case when ((`b`.`food_status` & 8) = 0) then 0 else `b`.`order_count` end))) * (1 + `a`.`service_rate`)),2) AS `total_price_present`,`a`.`custom_num` AS `num`,group_concat(concat((case `b`.`taste_id` when 0 then `b`.`name` else concat(`b`.`name`,'-',`b`.`taste`) end),'|',format(`b`.`order_count`,2),'|',(case `b`.`food_status` when 1 then '(特)' when 2 then '(荐)' when 3 then '(特,荐)' when 8 then '(赠)' when 9 then '(特,赠)' when 10 then '(荐,赠)' when 11 then '(特,荐,赠)' else '' end),'|',(case when (`b`.`discount` < 1) then concat('(',format((`b`.`discount` * 10),1),'折',')') else '' end),'|',format((((`b`.`unit_price` * `b`.`discount`) + `b`.`taste_price`) * `b`.`order_count`),2)) separator ';') AS `foods`,(`a`.`total_price` is not null) AS `is_paid`,`a`.`total_price_2` AS `total_price_2`,`a`.`restaurant_id` AS `restaurant_id`,`r`.`restaurant_name` AS `restaurant_name`,`d`.`table_id` AS `table_id`,`a`.`waiter` AS `waiter`,`a`.`type` AS `type_value`,(case `a`.`type` when 1 then '现金' when 2 then '刷卡' when 3 then '会员卡' when 4 then '签单' when 5 then '挂账' end) AS `type_name` from (((((`order` `a` left join `order_food_view` `b` on((`a`.`id` = `b`.`order_id`))) left join `food` `c` on(((`b`.`food_id` = `c`.`alias_id`) and (`c`.`restaurant_id` = `a`.`restaurant_id`)))) left join `table` `d` on(((`a`.`table_alias` = `d`.`table_alias`) and (`d`.`restaurant_id` = `a`.`restaurant_id`)))) left join `restaurant` `r` on((`a`.`restaurant_id` = `r`.`id`))) left join `terminal` `t` on((`a`.`terminal_pin` = `t`.`pin`) and (`t`.model_id = `a`.terminal_model))) group by `a`.`id`;

-- -----------------------------------------------------
-- View `order_history_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS order_history_view;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `order_history_view` AS select `a`.`id` AS `id`,`a`.`table_alias` AS `alias_id`,`a`.`table_name` AS `table_name`,`a`.`order_date` AS `order_date`,`a`.`category` AS `category`,(case `a`.`category` when 1 then '一般' when 2 then '外卖' when 3 then '并台' when 4 then '拼台' end) AS `category_name`,`a`.`comment` AS `comment`,`a`.`service_rate` AS `service_rate`,format((sum((((`b`.`unit_price` * `b`.`discount`) + `b`.`taste_price`) * (case when ((`b`.`food_status` & 8) <> 0) then 0 else `b`.`order_count` end))) * (1 + `a`.`service_rate`)),2) AS `total_price`,format((sum(((`b`.`unit_price` * (1 - `b`.`discount`)) * (case when ((`b`.`food_status` & 8) <> 0) then 0 else `b`.`order_count` end))) * (1 + `a`.`service_rate`)),2) AS `total_price_discount`,format((sum(((`b`.`unit_price` * `b`.`discount`) * (case when ((`b`.`food_status` & 8) = 0) then 0 else `b`.`order_count` end))) * (1 + `a`.`service_rate`)),2) AS `total_price_present`,`a`.`custom_num` AS `num`,group_concat(concat((case `b`.`taste_id` when 0 then `b`.`name` else concat(`b`.`name`,'-',`b`.`taste`) end),'|',format(`b`.`order_count`,2),'|',(case `b`.`food_status` when 1 then '(特)' when 2 then '(荐)' when 3 then '(特,荐)' when 8 then '(赠)' when 9 then '(特,赠)' when 10 then '(荐,赠)' when 11 then '(特,荐,赠)' else '' end),'|',(case when (`b`.`discount` < 1) then concat('(',format((`b`.`discount` * 10),1),'折',')') else '' end),'|',format((((`b`.`unit_price` * `b`.`discount`) + `b`.`taste_price`) * `b`.`order_count`),2)) separator ';') AS `foods`,(`a`.`total_price` is not null) AS `is_paid`,`a`.`total_price_2` AS `total_price_2`,`a`.`restaurant_id` AS `restaurant_id`,`r`.`restaurant_name` AS `restaurant_name`,`d`.`table_id` AS `table_id`,`a`.`waiter` AS `waiter`,`a`.`type` AS `type_value`,(case `a`.`type` when 1 then '现金' when 2 then '刷卡' when 3 then '会员卡' when 4 then '签单' when 5 then '挂账' end) AS `type_name` from (((((`order_history` `a` left join `temp_order_food_history` `b` on((`a`.`id` = `b`.`order_id`))) left join `food` `c` on(((`b`.`food_id` = `c`.`alias_id`) and (`c`.`restaurant_id` = `a`.`restaurant_id`)))) left join `table` `d` on(((`a`.`table_alias` = `d`.`table_alias`) and (`d`.`restaurant_id` = `a`.`restaurant_id`)))) left join `restaurant` `r` on((`a`.`restaurant_id` = `r`.`id`))) left join `terminal` `t` on((`a`.`terminal_pin` = `t`.`pin`) and (`t`.model_id = `a`.terminal_model))) group by `a`.`id`;

-- -----------------------------------------------------
-- View `restaurant_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS restaurant_view;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `restaurant_view` AS select `r`.`id` AS `id`,`r`.`account` AS `account`,`r`.`restaurant_name` AS `restaurant_name`,`r`.`tele1` AS `tele1`,`r`.`tele2` AS `tele2`,`r`.`address` AS `address`,`r`.`restaurant_info` AS `restaurant_info`,`r`.`record_alive` AS `record_alive`,(select count(`order`.`id`) from `order` where (`order`.`restaurant_id` = `r`.`id`)) AS `order_num`,(select count(`order_history`.`id`) from `order_history` where (`order_history`.`restaurant_id` = `r`.`id`)) AS `order_history_num`,(select count(`terminal`.`pin`) from `terminal` where ((`terminal`.`restaurant_id` = `r`.`id`) and (`terminal`.`model_id` <= 0x7f))) AS `terminal_num`,(select count(`terminal`.`pin`) from `terminal` where ((`terminal`.`restaurant_id` = `r`.`id`) and (`terminal`.`model_id` > 0x7f))) AS `terminal_virtual_num`,(select count(`food`.`id`) from `food` where (`food`.`restaurant_id` = `r`.`id`)) AS `food_num`,(select count(`table`.`table_id`) from `table` where (`table`.`restaurant_id` = `r`.`id`)) AS `table_num`,(select count(`order`.`id`) from `order` where ((`order`.`restaurant_id` = `r`.`id`) and (`order`.`total_price` is not null))) AS `order_paid`,(select count(`order_history`.`id`) from `order_history` where ((`order_history`.`restaurant_id` = `r`.`id`) and (`order_history`.`total_price` is not null))) AS `order_history_paid`,(select count(`table`.`table_id`) from `table` where ((`table`.`restaurant_id` = `r`.`id`) and exists(select 1 from `order` where ((`order`.`table_alias` = `table`.`table_alias`) and isnull(`order`.`total_price`) and (`order`.`restaurant_id` = `r`.`id`))))) AS `table_using` from `restaurant` `r`;

-- -----------------------------------------------------
-- View `terminal_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS terminal_view;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `terminal_view` AS select `t`.`pin` AS `pin`,`t`.`restaurant_id` AS `restaurant_id`,`r`.`restaurant_name` AS `restaurant_name`,`t`.`model_name` AS `model_name`,`t`.`model_id` AS `model_id`,(case `t`.`model_id` when 0 then 'BlackBerry' when 1 then 'Android' when 2 then 'iPhone' when 3 then 'WindowsMobile' end) AS `model_id_name`,`t`.`entry_date` AS `entry_date`,`t`.`discard_date` AS `discard_date`,format((((`t`.`idle_duration` / 3600) / 24) / 30),1) AS `idle_month`,format((((`t`.`work_duration` / 3600) / 24) / 30),1) AS `work_month`,`t`.`expire_date` AS `expire_date`,(case when (`t`.`restaurant_id` = 2) then '空闲' when (`t`.`restaurant_id` = 3) then '废弃' when ((`t`.`restaurant_id` > 10) and (now() <= `t`.`expire_date`)) then '使用' when ((`t`.`restaurant_id` > 10) and (now() > `t`.`expire_date`)) then '过期' end) AS `status`,format(((`t`.`work_duration` / (`t`.`work_duration` + `t`.`idle_duration`)) * 100),0) AS `use_rate`,`t`.`owner_name` AS `owner_name`,`t`.`idle_duration` AS `idle_duration`,`t`.`work_duration` AS `work_duration` from (`terminal` `t` left join `restaurant` `r` on((`t`.`restaurant_id` = `r`.`id`)));
