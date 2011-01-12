use wireless_order_db;
DROP VIEW restaurant_view;

CREATE VIEW `restaurant_view` AS select 
`r`.`id` AS `id`,`r`.`account` AS `account`,
`r`.`restaurant_name` AS `restaurant_name`,
`r`.`restaurant_info` AS `restaurant_info`,
`r`.`record_alive` AS `record_alive`,
(select count(`order`.`id`) from `order` where (`order`.`restaurant_id` = `r`.`id`)) AS `order_num`,
(select count(`terminal`.`pin`) from `terminal` where (`terminal`.`restaurant_id` = `r`.`id`)) AS `terminal_num`,
(select count(`food`.`id`) from `food` where (`food`.`restaurant_id` = `r`.`id`)) AS `food_num`,
(select count(`table`.`id`) from `table` where (`table`.`restaurant_id` = `r`.`id`)) AS `table_num`,
(select count(`order`.`id`) from `order` where (`order`.`restaurant_id` = `r`.`id` AND `order`.total_price>0)) AS `order_paid`,
(select count(`table`.`id`) from `table` where (`table`.`restaurant_id` = `r`.`id` AND EXISTS (SELECT * FROM `order` WHERE `order`.table_id = `table`.id AND NOT `order`.total_price>0))) AS `table_using`
from `restaurant` `r`
