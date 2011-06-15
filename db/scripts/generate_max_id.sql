USE `wireless_order_db` ;
SELECT @order_id:= (MAX(`id`) + 1) FROM 
(SELECT id FROM `order`
UNION SELECT id FROM `order_history`) AS all_order;
INSERT INTO `order`(id,restaurant_id,`order_date`,`waiter`) 
VALUES(@order_id,11,NOW(),'');

SELECT @order_food_id:= (MAX(`id`)+1) FROM 
(SELECT id FROM `order_food`
UNION SELECT id FROM `order_food_history`) AS all_order_food;
SELECT @order_id,@order_food_id;
INSERT INTO `order_food`(`id`,`order_id`)  
VALUES(@order_food_id,@order_id);

DELETE FROM `order_food` WHERE id = @order_food_id;
DELETE FROM `order` WHERE id = @order_id;
