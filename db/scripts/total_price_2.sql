SET NAMES utf8;

USE wireless_order_db;

ALTER TABLE `order`
ADD `total_price_2` DECIMAL(10,2) NULL DEFAULT NULL COMMENT 'the actual total price to this order';
ALTER TABLE `order_history`
ADD `total_price_2` DECIMAL(10,2) NULL DEFAULT NULL COMMENT 'the actual total price to this order';
UPDATE `order` SET `total_price_2` = `total_price`;
UPDATE `order_history` SET `total_price_2` = `total_price`;

DROP VIEW IF EXISTS order_view;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `order_view` AS select `a`.`id` AS `id`,`d`.`alias_id` AS `alias_id`,`a`.`order_date` AS `order_date`,format(sum((((`b`.`unit_price` * `b`.`discount`) + `b`.`taste_price`) * `b`.`order_count`)),2) AS `total_price`,`a`.`custom_num` AS `num`,group_concat(concat((case `b`.`taste_id` when 0 then `b`.`name` else concat(`b`.`name`,'-',`b`.`taste`) end),'|',format(`b`.`order_count`,2),'|',(case when (`b`.`discount` < 1) then concat('(',format((`b`.`discount` * 10),1),'折',')') else '' end),'|',format((((`b`.`unit_price` * `b`.`discount`) + `b`.`taste_price`) * `b`.`order_count`),2)) separator ',') AS `foods`,(`a`.`total_price` is not null) AS `is_paid`,`a`.`total_price_2` AS `total_price_2`,`a`.`restaurant_id` AS `restaurant_id`,`r`.`restaurant_name` AS `restaurant_name`,`d`.`id` AS `table_id`,`a`.`waiter` AS `waiter`,`a`.`type` AS `type_value`,(case `a`.`type` when 1 then '现金' when 2 then '刷卡' when 3 then '会员卡' when 4 then '挂账' when 5 then '签单' end) AS `type_name` from (((((`order` `a` left join `order_food_view` `b` on((`a`.`id` = `b`.`order_id`))) left join `food` `c` on(((`b`.`food_id` = `c`.`alias_id`) and (`c`.`restaurant_id` = `a`.`restaurant_id`)))) left join `table` `d` on(((`a`.`table_id` = `d`.`alias_id`) and (`d`.`restaurant_id` = `a`.`restaurant_id`)))) left join `restaurant` `r` on((`a`.`restaurant_id` = `r`.`id`))) left join `terminal` `t` on((`a`.`terminal_pin` = `t`.`pin`))) group by `a`.`id`,`d`.`alias_id`,`a`.`order_date`,`a`.`custom_num`,`a`.`total_price`;
DROP VIEW IF EXISTS order_history_view;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `order_history_view` AS select `a`.`id` AS `id`,`d`.`alias_id` AS `alias_id`,`a`.`order_date` AS `order_date`,format(sum((((`b`.`unit_price` * `b`.`discount`) + `b`.`taste_price`) * `b`.`order_count`)),2) AS `total_price`,`a`.`custom_num` AS `num`,group_concat(concat((case `b`.`taste_id` when 0 then `b`.`name` else concat(`b`.`name`,'-',`b`.`taste`) end),'|',format(`b`.`order_count`,2),'|',(case when (`b`.`discount` < 1) then concat('(',format((`b`.`discount` * 10),1),'折',')') else '' end),'|',format((((`b`.`unit_price` * `b`.`discount`) + `b`.`taste_price`) * `b`.`order_count`),2)) separator ',') AS `foods`,(`a`.`total_price` is not null) AS `is_paid`,`a`.`total_price_2` AS `total_price_2`,`a`.`restaurant_id` AS `restaurant_id`,`r`.`restaurant_name` AS `restaurant_name`,`d`.`id` AS `table_id`,`a`.`waiter` AS `waiter`,`a`.`type` AS `type_value`,(case `a`.`type` when 1 then '现金' when 2 then '刷卡' when 3 then '会员卡' when 4 then '挂账' when 5 then '签单' end) AS `type_name` from (((((`order_history` `a` left join `order_food_history_view` `b` on((`a`.`id` = `b`.`order_id`))) left join `food` `c` on(((`b`.`food_id` = `c`.`alias_id`) and (`c`.`restaurant_id` = `a`.`restaurant_id`)))) left join `table` `d` on(((`a`.`table_id` = `d`.`alias_id`) and (`d`.`restaurant_id` = `a`.`restaurant_id`)))) left join `restaurant` `r` on((`a`.`restaurant_id` = `r`.`id`))) left join `terminal` `t` on((`a`.`terminal_pin` = `t`.`pin`))) group by `a`.`id`,`d`.`alias_id`,`a`.`order_date`,`a`.`custom_num`,`a`.`total_price`;

DELETE FROM `order_food_history` where id IN (select id from order_food);

DELETE FROM `wireless_order_db`.`order_history` WHERE 
id NOT IN(SELECT order_id FROM `wireless_order_db`.`order_food_history`);


