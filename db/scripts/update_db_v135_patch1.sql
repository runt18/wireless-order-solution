SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';
SET @OLD_SAFE_UPDATES=@@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;
SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Add the field 'age' to table 'member'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`member` 
ADD COLUMN `age` TINYINT NULL DEFAULT NULL COMMENT 'the age as below.\n0 - unknown\n1 - 1960s\n2 - 1970s\n3 - 1980s\n4 - 1990s\n5 - 2000s' AFTER `birthday`;

-- -----------------------------------------------------
-- Drop the table 'member_comment'
-- -----------------------------------------------------
DROP TABLE `wireless_order_db`.`member_comment`;

-- -----------------------------------------------------
-- Drop the table 'member_user'
-- -----------------------------------------------------
DROP TABLE `wireless_order_db`.`member_user`;

-- -----------------------------------------------------
-- Drop the table 'interested_member'
-- -----------------------------------------------------
DROP TABLE `wireless_order_db`.`interested_member`;

-- -----------------------------------------------------
-- Add the field 'sex', 'age', 'min_charge', 'max_charge' to table 'member_cond'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`member_cond` 
ADD COLUMN `sex` TINYINT NULL DEFAULT NULL COMMENT '' AFTER `max_last_consumption`,
ADD COLUMN `age` VARCHAR(45) NULL DEFAULT NULL COMMENT '' AFTER `sex`,
ADD COLUMN `min_charge` FLOAT NULL DEFAULT NULL COMMENT '' AFTER `age`,
ADD COLUMN `max_charge` FLOAT NULL DEFAULT NULL COMMENT '' AFTER `min_charge`,
ADD COLUMN `raw` TINYINT NULL DEFAULT 0 COMMENT '' AFTER `max_charge`;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;



