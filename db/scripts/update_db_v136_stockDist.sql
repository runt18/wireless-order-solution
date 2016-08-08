SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';
SET @OLD_SAFE_UPDATES=@@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;
SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Add the field 'associate_id'
-- -----------------------------------------------------
 ALTER TABLE wireless_order_db.stock_action_detail ADD COLUMN associate_id INT(100);

-- -----------------------------------------------------
-- Add the field 'associate_id'
-- -----------------------------------------------------
ALTER TABLE wireless_order_db.material ADD COLUMN associate_id INT(100);

ALTER TABLE wireless_order_db.stock_action_detail ADD COLUMN associate_id INT(100);

ALTER TABLE wireless_order_db.material ADD COLUMN associate_id INT(100);

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;



