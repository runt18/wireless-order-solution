ALTER TABLE `wireless_order_db`.`order` ADD COLUMN `gift_price` DECIMAL(10,2) NOT NULL DEFAULT 0  AFTER `order_date` ;
ALTER TABLE `wireless_order_db`.`order_history` ADD COLUMN `gift_price` DECIMAL(10,2) NOT NULL DEFAULT 0  AFTER `order_date` ;
ALTER TABLE `wireless_order_db`.`restaurant` ADD COLUMN `pwd3` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the 3rd password to this restaurant, whose permission priority is lower than pwd2'  AFTER `pwd2` , 
CHANGE COLUMN `pwd2` `pwd2` VARCHAR(45) NOT NULL DEFAULT '' COMMENT 'the 2nd password to this restaurant, whose priority permisson is lower than pwd'  ;
