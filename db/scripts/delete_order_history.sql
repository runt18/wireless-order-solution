USE wireless_order_db;
DELETE FROM `wireless_order_db`.`order_history` WHERE 
id NOT IN(SELECT order_id FROM `wireless_order_db`.`order_food_history`);
