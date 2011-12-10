-- -----------------------------------------------------
-- Change the table_id & table2_id in table order to unsigned small int
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order` CHANGE COLUMN `table_id` `table_id` SMALLINT(6) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the table alias id to this order'  , CHANGE COLUMN `table2_id` `table2_id` SMALLINT(6) UNSIGNED NULL DEFAULT NULL COMMENT 'the 2nd table alias id to this order(used for table merger)';

-- -----------------------------------------------------
-- Change the table_id & table2_id in table order_history to unsigned small int
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`order_history` CHANGE COLUMN `table_id` `table_id` SMALLINT(6) UNSIGNED NOT NULL DEFAULT '0' COMMENT 'the table alias id to this order'  , CHANGE COLUMN `table2_id` `table2_id` SMALLINT(6) UNSIGNED NULL DEFAULT NULL COMMENT 'the 2nd table alias id to this order(used for table merger)'  ;