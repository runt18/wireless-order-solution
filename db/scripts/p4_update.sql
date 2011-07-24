SET NAMES utf8;
use `wireless_order_db`;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`shift`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`shift` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`shift` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to each shift record' ,
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `name` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the name of the operator to shift' ,
  `on_duty` DATETIME NULL DEFAULT NULL COMMENT 'the datetime to be on duty' ,
  `off_duty` DATETIME NULL DEFAULT NULL COMMENT 'the datetime to be off duty' ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_shift_restaurant1` (`restaurant_id` ASC) ,
  CONSTRAINT `fk_shift_restaurant1`
    FOREIGN KEY (`restaurant_id` )
    REFERENCES `wireless_order_db`.`restaurant` (`id` )
    ON DELETE RESTRICT
    ON UPDATE RESTRICT)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'the shift history to each restaurant';

ALTER TABLE `terminal`
ADD `gift_quota` DECIMAL(7,2) NULL DEFAULT -1 COMMENT 'the gift quota to this terminal, \"-1\" means no limit';
ALTER TABLE `terminal`
ADD `gift_amount` DECIMAL(7,2) NOT NULL DEFAULT 0 COMMENT 'the gift amount to this terminal';

ALTER TABLE `taste`
ADD `category` TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the category to this taste, one of the values below.\n0 - 口味\n1 - 做法\n2 - 规格';
ALTER TABLE `taste`
ADD `rate` DECIMAL(3,2) NOT NULL DEFAULT 0 COMMENT 'the rate to this taste, used for the calc type is 按比例';
ALTER TABLE `taste`
ADD `calc` TINYINT NOT NULL DEFAULT 0 COMMENT 'the calculate type to this taste, one of the values below.\n0 - 按价格\n1 - 按比例';
ALTER TABLE `taste`
MODIFY `alias_id` SMALLINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the alias id to this taste preference, the lower the alias id , the more commonly this taste preference used';

ALTER TABLE `order_food`
ADD `taste_id2` SMALLINT UNSIGNED NOT NULL DEFAULT 0;
ALTER TABLE `order_food`
ADD `taste_id3` SMALLINT UNSIGNED NOT NULL DEFAULT 0;

ALTER TABLE `wireless_order_db`.`order_food` 
CHANGE COLUMN `food_id` 
`food_id` SMALLINT(6) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the alias id to this food';

ALTER TABLE `wireless_order_db`.`order_food_history` 
CHANGE COLUMN `food_id` 
`food_id` SMALLINT(6) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the alias id to this food';

-- -----------------------------------------------------
-- View`order_food_history_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS order_food_history_view;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `order_food_history_view` AS select sum(`order_food_history`.`order_count`) AS `order_count`,max(`order_food_history`.`unit_price`) AS `unit_price`,max(`order_food_history`.`taste_price`) AS `taste_price`,max(`order_food_history`.`name`) AS `name`,max(`order_food_history`.`taste`) AS `taste`,max(`order_food_history`.`taste_id`) AS `taste_id`,max(`order_food_history`.`discount`) AS `discount`,max(`order_food_history`.`food_status`) AS `food_status`,max(`order_food_history`.`kitchen`) AS `kitchen`,`order_food_history`.`order_id` AS `order_id`,`order_food_history`.`food_id` AS `food_id` from `order_food_history` group by `order_food_history`.`order_id`,`order_food_history`.`food_id`,`order_food_history`.`taste_id` having (sum(`order_food_history`.`order_count`) > 0);

-- -----------------------------------------------------
-- View`order_food_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS order_food_view;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `order_food_view` AS select sum(`order_food`.`order_count`) AS `order_count`,max(`order_food`.`unit_price`) AS `unit_price`,max(`order_food`.`taste_price`) AS `taste_price`,max(`order_food`.`name`) AS `name`,max(`order_food`.`taste`) AS `taste`,max(`order_food`.`taste_id`) AS `taste_id`,max(`order_food`.`discount`) AS `discount`,max(`order_food`.`food_status`) AS `food_status`,`order_food`.`order_id` AS `order_id`,`order_food`.`food_id` AS `food_id` from `order_food` group by `order_food`.`order_id`,`order_food`.`food_id`,`order_food`.`taste_id` having (sum(`order_food`.`order_count`) > 0);

-- -----------------------------------------------------
-- View`order_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS order_view;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `order_view` AS select `a`.`id` AS `id`,`a`.`table_id` AS `alias_id`,`a`.`table_name` AS `table_name`,`a`.`order_date` AS `order_date`,`a`.`category` AS `category`,(case `a`.`category` when 1 then '一般' when 2 then '外卖' when 3 then '并台' when 4 then '拼台' end) AS `category_name`,`a`.`comment` AS `comment`,`a`.`service_rate` AS `service_rate`,format((sum((((`b`.`unit_price` * `b`.`discount`) + `b`.`taste_price`) * (case when ((`b`.`food_status` & 8) <> 0) then 0 else `b`.`order_count` end))) * (1 + `a`.`service_rate`)),2) AS `total_price`,format((sum(((`b`.`unit_price` * (1 - `b`.`discount`)) * (case when ((`b`.`food_status` & 8) <> 0) then 0 else `b`.`order_count` end))) * (1 + `a`.`service_rate`)),2) AS `total_price_discount`,format((sum(((`b`.`unit_price` * `b`.`discount`) * (case when ((`b`.`food_status` & 8) = 0) then 0 else `b`.`order_count` end))) * (1 + `a`.`service_rate`)),2) AS `total_price_present`,`a`.`custom_num` AS `num`,group_concat(concat((case `b`.`taste_id` when 0 then `b`.`name` else concat(`b`.`name`,'-',`b`.`taste`) end),'|',format(`b`.`order_count`,2),'|',(case `b`.`food_status` when 1 then '(特)' when 2 then '(荐)' when 3 then '(特,荐)' when 8 then '(赠)' when 9 then '(特,赠)' when 10 then '(荐,赠)' when 11 then '(特,荐,赠)' else '' end),'|',(case when (`b`.`discount` < 1) then concat('(',format((`b`.`discount` * 10),1),'折',')') else '' end),'|',format((((`b`.`unit_price` * `b`.`discount`) + `b`.`taste_price`) * `b`.`order_count`),2)) separator ';') AS `foods`,(`a`.`total_price` is not null) AS `is_paid`,`a`.`total_price_2` AS `total_price_2`,`a`.`restaurant_id` AS `restaurant_id`,`r`.`restaurant_name` AS `restaurant_name`,`d`.`id` AS `table_id`,`a`.`waiter` AS `waiter`,`a`.`type` AS `type_value`,(case `a`.`type` when 1 then '现金' when 2 then '刷卡' when 3 then '会员卡' when 4 then '挂账' when 5 then '签单' end) AS `type_name` from (((((`order` `a` left join `order_food_view` `b` on((`a`.`id` = `b`.`order_id`))) left join `food` `c` on(((`b`.`food_id` = `c`.`alias_id`) and (`c`.`restaurant_id` = `a`.`restaurant_id`)))) left join `table` `d` on(((`a`.`table_id` = `d`.`alias_id`) and (`d`.`restaurant_id` = `a`.`restaurant_id`)))) left join `restaurant` `r` on((`a`.`restaurant_id` = `r`.`id`))) left join `terminal` `t` on((`a`.`terminal_pin` = `t`.`pin`))) group by `a`.`id`;

-- -----------------------------------------------------
-- View`order_history_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS order_history_view;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `order_history_view` AS select `a`.`id` AS `id`,`a`.`table_id` AS `alias_id`,`a`.`table_name` AS `table_name`,`a`.`order_date` AS `order_date`,`a`.`category` AS `category`,(case `a`.`category` when 1 then '一般' when 2 then '外卖' when 3 then '并台' when 4 then '拼台' end) AS `category_name`,`a`.`comment` AS `comment`,`a`.`service_rate` AS `service_rate`,format((sum((((`b`.`unit_price` * `b`.`discount`) + `b`.`taste_price`) * (case when ((`b`.`food_status` & 8) <> 0) then 0 else `b`.`order_count` end))) * (1 + `a`.`service_rate`)),2) AS `total_price`,format((sum(((`b`.`unit_price` * (1 - `b`.`discount`)) * (case when ((`b`.`food_status` & 8) <> 0) then 0 else `b`.`order_count` end))) * (1 + `a`.`service_rate`)),2) AS `total_price_discount`,format((sum(((`b`.`unit_price` * `b`.`discount`) * (case when ((`b`.`food_status` & 8) = 0) then 0 else `b`.`order_count` end))) * (1 + `a`.`service_rate`)),2) AS `total_price_present`,`a`.`custom_num` AS `num`,group_concat(concat((case `b`.`taste_id` when 0 then `b`.`name` else concat(`b`.`name`,'-',`b`.`taste`) end),'|',format(`b`.`order_count`,2),'|',(case `b`.`food_status` when 1 then '(特)' when 2 then '(荐)' when 3 then '(特,荐)' when 8 then '(赠)' when 9 then '(特,赠)' when 10 then '(荐,赠)' when 11 then '(特,荐,赠)' else '' end),'|',(case when (`b`.`discount` < 1) then concat('(',format((`b`.`discount` * 10),1),'折',')') else '' end),'|',format((((`b`.`unit_price` * `b`.`discount`) + `b`.`taste_price`) * `b`.`order_count`),2)) separator ';') AS `foods`,(`a`.`total_price` is not null) AS `is_paid`,`a`.`total_price_2` AS `total_price_2`,`a`.`restaurant_id` AS `restaurant_id`,`r`.`restaurant_name` AS `restaurant_name`,`d`.`id` AS `table_id`,`a`.`waiter` AS `waiter`,`a`.`type` AS `type_value`,(case `a`.`type` when 1 then '现金' when 2 then '刷卡' when 3 then '会员卡' when 4 then '挂账' when 5 then '签单' end) AS `type_name` from (((((`order_history` `a` left join `order_food_history_view` `b` on((`a`.`id` = `b`.`order_id`))) left join `food` `c` on(((`b`.`food_id` = `c`.`alias_id`) and (`c`.`restaurant_id` = `a`.`restaurant_id`)))) left join `table` `d` on(((`a`.`table_id` = `d`.`alias_id`) and (`d`.`restaurant_id` = `a`.`restaurant_id`)))) left join `restaurant` `r` on((`a`.`restaurant_id` = `r`.`id`))) left join `terminal` `t` on((`a`.`terminal_pin` = `t`.`pin`))) group by `a`.`id`;

-- -----------------------------------------------------
-- View`restaurant_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS restaurant_view;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `restaurant_view` AS select `r`.`id` AS `id`,`r`.`account` AS `account`,`r`.`restaurant_name` AS `restaurant_name`,`r`.`tele1` AS `tele1`,`r`.`tele2` AS `tele2`,`r`.`address` AS `address`,`r`.`restaurant_info` AS `restaurant_info`,`r`.`record_alive` AS `record_alive`,(select count(`order`.`id`) from `order` where (`order`.`restaurant_id` = `r`.`id`)) AS `order_num`,(select count(`order_history`.`id`) from `order_history` where (`order_history`.`restaurant_id` = `r`.`id`)) AS `order_history_num`,(select count(`terminal`.`pin`) from `terminal` where ((`terminal`.`restaurant_id` = `r`.`id`) and (`terminal`.`model_id` <= 0x7f))) AS `terminal_num`,(select count(`terminal`.`pin`) from `terminal` where ((`terminal`.`restaurant_id` = `r`.`id`) and (`terminal`.`model_id` > 0x7f))) AS `terminal_virtual_num`,(select count(`food`.`id`) from `food` where (`food`.`restaurant_id` = `r`.`id`)) AS `food_num`,(select count(`table`.`id`) from `table` where (`table`.`restaurant_id` = `r`.`id`)) AS `table_num`,(select count(`order`.`id`) from `order` where ((`order`.`restaurant_id` = `r`.`id`) and (`order`.`total_price` is not null))) AS `order_paid`,(select count(`order_history`.`id`) from `order_history` where ((`order_history`.`restaurant_id` = `r`.`id`) and (`order_history`.`total_price` is not null))) AS `order_history_paid`,(select count(`table`.`id`) from `table` where ((`table`.`restaurant_id` = `r`.`id`) and exists(select 1 from `order` where ((`order`.`table_id` = `table`.`alias_id`) and isnull(`order`.`total_price`) and (`order`.`restaurant_id` = `r`.`id`))))) AS `table_using` from `restaurant` `r`;

-- -----------------------------------------------------
-- View`terminal_view`
-- -----------------------------------------------------
DROP VIEW IF EXISTS terminal_view;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `terminal_view` AS select `t`.`pin` AS `pin`,`t`.`restaurant_id` AS `restaurant_id`,`r`.`restaurant_name` AS `restaurant_name`,`t`.`model_name` AS `model_name`,`t`.`model_id` AS `model_id`,(case `t`.`model_id` when 0 then 'BlackBerry' when 1 then 'Android' when 2 then 'iPhone' when 3 then 'WindowsMobile' end) AS `model_id_name`,`t`.`entry_date` AS `entry_date`,`t`.`discard_date` AS `discard_date`,format((((`t`.`idle_duration` / 3600) / 24) / 30),1) AS `idle_month`,format((((`t`.`work_duration` / 3600) / 24) / 30),1) AS `work_month`,`t`.`expire_date` AS `expire_date`,(case when (`t`.`restaurant_id` = 2) then '空闲' when (`t`.`restaurant_id` = 3) then '废弃' when ((`t`.`restaurant_id` > 10) and (now() <= `t`.`expire_date`)) then '使用' when ((`t`.`restaurant_id` > 10) and (now() > `t`.`expire_date`)) then '过期' end) AS `status`,format(((`t`.`work_duration` / (`t`.`work_duration` + `t`.`idle_duration`)) * 100),0) AS `use_rate`,`t`.`owner_name` AS `owner_name`,`t`.`idle_duration` AS `idle_duration`,`t`.`work_duration` AS `work_duration` from (`terminal` `t` left join `restaurant` `r` on((`t`.`restaurant_id` = `r`.`id`)));

