use wireless_order_db;
DROP VIEW order_view;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `order_view` 
AS select `a`.`id` AS `id`,`d`.`alias_id` AS `alias_id`,`a`.`order_date` AS `order_date`,
format(sum((`b`.`unit_price` * `b`.`order_count`)),2) AS `total_price`,`a`.`custom_num` AS `num`,
group_concat(concat(`b`.`name`,'|',format(`b`.`order_count`,2),'|',
format((`b`.`unit_price` * `b`.`order_count`),2)) separator ',') AS `foods`,
(`a`.`total_price` > 0) AS `is_paid`,`a`.`restaurant_id` AS `restaurant_id`,
`r`.`restaurant_name` AS `restaurant_name`,
d.id as table_id
from ((((`order` `a` left join `order_food` `b` on((`a`.`id` = `b`.`order_id`))) left join `food` `c` on((`b`.`food_id` = `c`.`id`))) left join `table` `d` on((`a`.`table_id` = `d`.`id`))) left join `restaurant` `r` on((`a`.`restaurant_id` = `r`.`id`))) group by `a`.`id`,`d`.`alias_id`,`a`.`order_date`,`a`.`custom_num`,`a`.`total_price`
