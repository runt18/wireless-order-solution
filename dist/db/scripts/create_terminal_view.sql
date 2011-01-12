-- -----------------------------------------------------
-- View`terminal_view`
-- -----------------------------------------------------
USE wireless_order_db;
DROP VIEW IF EXISTS terminal_view;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `terminal_view` AS select `t`.`pin` AS
`pin`,`t`.`restaurant_id` AS `restaurant_id`,`r`.`restaurant_name` AS `restaurant_name`,
`t`.`model_name` AS `model_name`,`t`.`entry_date` AS `entry_date`,`t`.`discard_date` AS `discard_date`,
format((((`t`.`idle_duration` / 3600) / 24) / 30),1) AS `idle_month`,
format((((`t`.`work_duration` / 3600) / 24) / 30),1) AS `work_month`,
`t`.`expire_date` AS `expire_date`,
(case when (`t`.`restaurant_id` = 2) then '空闲' when (`t`.`restaurant_id` = 3) then '废弃' when ((`t`.`restaurant_id` > 10) and (now() <= `t`.`expire_date`)) then '使用' when ((`t`.`restaurant_id` > 10) and (now() > `t`.`expire_date`)) then '过期' end) AS `status`,
format(((`t`.`work_duration` / (`t`.`work_duration` + `t`.`idle_duration`)) * 100),0) AS `use_rate`,
`t`.`owner_name`,
`t`.idle_duration,
`t`.work_duration
from (`terminal` `t` left join `restaurant` `r` on((`t`.`restaurant_id` = `r`.`id`)))