DELETE FROM `order_history` WHERE 
id NOT IN(SELECT order_id FROM order_food_history);
