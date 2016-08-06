SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';
SET @OLD_SAFE_UPDATES=@@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;
SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`privilege` and `wireless_order_db`.`role_privilege`
-- -----------------------------------------------------
 SET @pri_id = 0;
 INSERT INTO wireless_order_db.privilege(pri_code)VALUES(5001);
 SELECT @pri_id := pri_id FROM wireless_order_db.privilege WHERE pri_code = 5001;
 INSERT INTO wireless_order_db.role_privilege 
 (role_id, pri_id, restaurant_id)
 SELECT role_id, @pri_id, restaurant_id FROM wireless_order_db.role_privilege RP
 JOIN wireless_order_db.privilege P ON P.pri_id = RP.pri_id
 WHERE P.pri_code = 5000;
 
 SET @pri_id = 0;
 INSERT INTO wireless_order_db.privilege(pri_code)VALUES(5002);
 SELECT @pri_id := pri_id FROM wireless_order_db.privilege WHERE pri_code = 5002;
 INSERT INTO wireless_order_db.role_privilege 
 (role_id, pri_id, restaurant_id)
 SELECT role_id, @pri_id, restaurant_id FROM wireless_order_db.role_privilege RP
 JOIN wireless_order_db.privilege P ON P.pri_id = RP.pri_id
 WHERE P.pri_code = 5000;
 
 SET @pri_id = 0;
 INSERT INTO wireless_order_db.privilege(pri_code)VALUES(5003);
 SELECT @pri_id := pri_id FROM wireless_order_db.privilege WHERE pri_code = 5003;
 INSERT INTO wireless_order_db.role_privilege 
 (role_id, pri_id, restaurant_id)
 SELECT role_id, @pri_id, restaurant_id FROM wireless_order_db.role_privilege RP
 JOIN wireless_order_db.privilege P ON P.pri_id = RP.pri_id
 WHERE P.pri_code = 5000;
 
 SET @pri_id = 0;
 INSERT INTO wireless_order_db.privilege(pri_code)VALUES(5004);
 SELECT @pri_id := pri_id FROM wireless_order_db.privilege WHERE pri_code = 5004;
 INSERT INTO wireless_order_db.role_privilege 
 (role_id, pri_id, restaurant_id)
 SELECT role_id, @pri_id, restaurant_id FROM wireless_order_db.role_privilege RP
 JOIN wireless_order_db.privilege P ON P.pri_id = RP.pri_id
 WHERE P.pri_code = 5000;
 
 SET @pri_id = 0;
 INSERT INTO wireless_order_db.privilege(pri_code)VALUES(5005);
 SELECT @pri_id := pri_id FROM wireless_order_db.privilege WHERE pri_code = 5005;
 INSERT INTO wireless_order_db.role_privilege 
 (role_id, pri_id, restaurant_id)
 SELECT role_id, @pri_id, restaurant_id FROM wireless_order_db.role_privilege RP
 JOIN wireless_order_db.privilege P ON P.pri_id = RP.pri_id
 WHERE P.pri_code = 5000;
 
 SET @pri_id = 0;
 INSERT INTO wireless_order_db.privilege(pri_code)VALUES(5006);
 SELECT @pri_id := pri_id FROM wireless_order_db.privilege WHERE pri_code = 5006;
 INSERT INTO wireless_order_db.role_privilege 
 (role_id, pri_id, restaurant_id)
 SELECT role_id, @pri_id, restaurant_id FROM wireless_order_db.role_privilege RP
 JOIN wireless_order_db.privilege P ON P.pri_id = RP.pri_id
 WHERE P.pri_code = 5000;
 
 SET @pri_id = 0;
 INSERT INTO wireless_order_db.privilege(pri_code)VALUES(5007);
 SELECT @pri_id := pri_id FROM wireless_order_db.privilege WHERE pri_code = 5007;
 INSERT INTO wireless_order_db.role_privilege 
 (role_id, pri_id, restaurant_id)
 SELECT role_id, @pri_id, restaurant_id FROM wireless_order_db.role_privilege RP
 JOIN wireless_order_db.privilege P ON P.pri_id = RP.pri_id
 WHERE P.pri_code = 5000;
 
 SET @pri_id = 0;
 INSERT INTO wireless_order_db.privilege(pri_code)VALUES(5008);
 SELECT @pri_id := pri_id FROM wireless_order_db.privilege WHERE pri_code = 5008;
 INSERT INTO wireless_order_db.role_privilege 
 (role_id, pri_id, restaurant_id)
 SELECT role_id, @pri_id, restaurant_id FROM wireless_order_db.role_privilege RP
 JOIN wireless_order_db.privilege P ON P.pri_id = RP.pri_id
 WHERE P.pri_code = 5000;
 
 SET @pri_id = 0;
 INSERT INTO wireless_order_db.privilege(pri_code)VALUES(5009);
 SELECT @pri_id := pri_id FROM wireless_order_db.privilege WHERE pri_code = 5009;
 INSERT INTO wireless_order_db.role_privilege 
 (role_id, pri_id, restaurant_id)
 SELECT role_id, @pri_id, restaurant_id FROM wireless_order_db.role_privilege RP
 JOIN wireless_order_db.privilege P ON P.pri_id = RP.pri_id
 WHERE P.pri_code = 5000;
 
 SET @pri_id = 0;
 INSERT INTO wireless_order_db.privilege(pri_code)VALUES(5010);
 SELECT @pri_id := pri_id FROM wireless_order_db.privilege WHERE pri_code = 5010;
 INSERT INTO wireless_order_db.role_privilege 
 (role_id, pri_id, restaurant_id)
 SELECT role_id, @pri_id, restaurant_id FROM wireless_order_db.role_privilege RP
 JOIN wireless_order_db.privilege P ON P.pri_id = RP.pri_id
 WHERE P.pri_code = 5000;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;



