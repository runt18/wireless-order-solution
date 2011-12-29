SET NAMES utf8;
USE wireless_order_db;
-- -----------------------------------------------------
-- Add the field 'receipt_style' to 'setting'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`setting` 
DROP COLUMN `id` , 
ADD COLUMN `receipt_style` INT UNSIGNED NOT NULL DEFAULT '4294967295' COMMENT 'the receipt style is as below.\n0x01 : 结帐单是否显示折扣\n0x02 : 结帐单是否显示数量\n0x04 : 结帐单是否显示状态\n0x08 : 结帐单是否显示折扣额'  AFTER `auto_reprint`,
DROP PRIMARY KEY ;

-- -----------------------------------------------------
-- ‘金满堂’和‘新惠爱’不显示折扣和菜品状态
-- -----------------------------------------------------
UPDATE `wireless_order_db`.`setting` SET receipt_style=0xFFFFFFFA WHERE restaurant_id=29;
UPDATE `wireless_order_db`.`setting` SET receipt_style=0xFFFFFFFA WHERE restaurant_id=26;