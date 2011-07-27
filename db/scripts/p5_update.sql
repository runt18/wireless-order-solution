ALTER TABLE `wireless_order_db`.`order` ADD COLUMN `gift_price` DECIMAL(10,2) NOT NULL DEFAULT 0  AFTER `order_date` ;
ALTER TABLE `wireless_order_db`.`order_history` ADD COLUMN `gift_price` DECIMAL(10,2) NOT NULL DEFAULT 0  AFTER `order_date` ;
