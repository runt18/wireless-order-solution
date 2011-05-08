USE wireless_order_db;
ALTER TABLE `kitchen` ADD `discount_2` DECIMAL(3,2) NOT NULL DEFAULT 1 COMMENT 'the 2nd discount to the food belong to this kitchen, range from 0.00 to 1.00';