SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';
SET @OLD_SAFE_UPDATES=@@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;
SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`promotion_trigger`
-- -----------------------------------------------------
INSERT INTO wireless_order_db.privilege (`pri_id`, `pri_code`) VALUE (23, 3006);

-- -----------------------------------------------------
-- Add the adjust point privilege to each boss & admin
-- -----------------------------------------------------
INSERT INTO wireless_order_db.role_privilege
(`role_id`, `restaurant_id`, `pri_id`)
SELECT R.role_id, MAX(R.restaurant_id), 23 FROM wireless_order_db.role R
JOIN wireless_order_db.role_privilege RP ON RP.role_id = R.role_id
WHERE 1 = 1 
AND R.type = 2
GROUP BY R.role_id;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;



