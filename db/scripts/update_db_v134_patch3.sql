SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';
SET @OLD_SAFE_UPDATES=@@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;
SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Add the field 'cate' to table 'weixin_menu_action'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`weixin_menu_action` 
ADD COLUMN `cate` TINYINT NULL DEFAULT NULL COMMENT 'the category as below\n1 - normal\n2 - subcribe replay' AFTER `action`;

-- -----------------------------------------------------
-- Remove the field 'cate' from table 'privilege'
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`privilege` 
DROP COLUMN `cate`;

-- -----------------------------------------------------
-- Get the '会员' privilege
-- -----------------------------------------------------
SELECT @member_pri_id := pri_id FROM wireless_order_db.privilege WHERE pri_code = 3000;

-- -----------------------------------------------------
-- Insert the '会员增加' privilege
-- -----------------------------------------------------
INSERT INTO wireless_order_db.privilege
(pri_code, cate) VALUES 
(3001, 5);

-- -----------------------------------------------------
-- Insert the '会员增加' privilege to each role contains '会员' privilege before
-- -----------------------------------------------------
SELECT @pri_id := pri_id FROM wireless_order_db.privilege WHERE pri_code = 3001;
INSERT INTO wireless_order_db.role_privilege 
(role_id, pri_id, restaurant_id)
SELECT role_id, @pri_id, restaurant_id FROM wireless_order_db.role_privilege
WHERE pri_id = @member_pri_id GROUP BY role_id;

-- -----------------------------------------------------
-- Insert the '会员修改' privilege
-- -----------------------------------------------------
INSERT INTO wireless_order_db.privilege
(pri_code, cate) VALUES 
(3002, 5);

-- -----------------------------------------------------
-- Insert the '会员修改' privilege to each role contains '会员' privilege before
-- -----------------------------------------------------
SELECT @pri_id := pri_id FROM wireless_order_db.privilege WHERE pri_code = 3002;
INSERT INTO wireless_order_db.role_privilege 
(role_id, pri_id, restaurant_id)
SELECT role_id, @pri_id, restaurant_id FROM wireless_order_db.role_privilege
WHERE pri_id = @member_pri_id GROUP BY role_id;

-- -----------------------------------------------------
-- Insert the '会员删除' privilege
-- -----------------------------------------------------
INSERT INTO wireless_order_db.privilege
(pri_code, cate) VALUES 
(3003, 5);

-- -----------------------------------------------------
-- Insert the '会员删除' privilege to each role contains '会员' privilege before
-- -----------------------------------------------------
SELECT @pri_id := pri_id FROM wireless_order_db.privilege WHERE pri_code = 3003;
INSERT INTO wireless_order_db.role_privilege 
(role_id, pri_id, restaurant_id)
SELECT role_id, @pri_id, restaurant_id FROM wireless_order_db.role_privilege
WHERE pri_id = @member_pri_id GROUP BY role_id;

-- -----------------------------------------------------
-- Insert the '会员充值' privilege
-- -----------------------------------------------------
INSERT INTO wireless_order_db.privilege
(pri_code, cate) VALUES 
(3004, 5);

-- -----------------------------------------------------
-- Insert the '会员充值' privilege to each role contains '会员' privilege before
-- -----------------------------------------------------
SELECT @pri_id := pri_id FROM wireless_order_db.privilege WHERE pri_code = 3004;
INSERT INTO wireless_order_db.role_privilege 
(role_id, pri_id, restaurant_id)
SELECT role_id, @pri_id, restaurant_id FROM wireless_order_db.role_privilege
WHERE pri_id = @member_pri_id GROUP BY role_id;

-- -----------------------------------------------------
-- Insert the '会员取款' privilege
-- -----------------------------------------------------
INSERT INTO wireless_order_db.privilege
(pri_code, cate) VALUES 
(3005, 5);

-- -----------------------------------------------------
-- Insert the '会员取款' privilege to each role contains '会员' privilege before
-- -----------------------------------------------------
SELECT @pri_id := pri_id FROM wireless_order_db.privilege WHERE pri_code = 3005;
INSERT INTO wireless_order_db.role_privilege 
(role_id, pri_id, restaurant_id)
SELECT role_id, @pri_id, restaurant_id FROM wireless_order_db.role_privilege
WHERE pri_id = @member_pri_id GROUP BY role_id;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;



