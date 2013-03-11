SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Drop the field 'member' in table 'order'
-- Change the field 'member_id' in table 'order'
-- Add the field 'settle_type' in table 'order'
-- Rename the field 'type' to 'pay_type' in table 'order'
-- Rename the field 'total_price_2' to 'actual_price' in table 'order'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order` 
DROP COLUMN `member`,
DROP COLUMN `member_id`,
ADD COLUMN `member_id` INT UNSIGNED NULL DEFAULT NULL COMMENT 'the member id to this order',
ADD COLUMN `settle_type` TINYINT NOT NULL DEFAULT 1 COMMENT '结帐方式\n一般：1 (default)\n会员：2'  AFTER `waiter`,
CHANGE COLUMN `type` `pay_type` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '付款方式\n现金 : 1 (default)\n刷卡 : 2\n会员卡 : 3\n签单：4\n挂账 ：5\n',
CHANGE COLUMN `total_price_2` `actual_price` FLOAT NULL DEFAULT NULL COMMENT 'the actual total price to this order' ;

-- -----------------------------------------------------
-- Drop the field 'member' in table 'order_history'
-- Change the field 'member_id' in table 'order_history'
-- Add the field 'settle_type' in table 'order_history'
-- Rename the field 'type' to 'pay_type' in table 'order_history'
-- Rename the field 'total_price_2' to 'actual_price' in table 'order_history'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_history` 
DROP COLUMN `member`,
DROP COLUMN `member_id`,
ADD COLUMN `member_id` INT UNSIGNED NULL DEFAULT NULL COMMENT 'the member id to this order',
ADD COLUMN `settle_type` TINYINT NOT NULL DEFAULT 1 COMMENT '结帐方式\n一般：1 (default)\n会员：2'  AFTER `waiter`,
CHANGE COLUMN `type` `pay_type` TINYINT(4) NOT NULL DEFAULT 1 COMMENT '付款方式\n现金 : 1 (default)\n刷卡 : 2\n会员卡 : 3\n签单：4\n挂账 ：5\n',
CHANGE COLUMN `total_price_2` `actual_price` FLOAT NULL DEFAULT NULL COMMENT 'the actual total price to this order' ;