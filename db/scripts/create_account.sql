DELETE FROM mysql.user WHERE user='terminal';
-- -----------------------------------------------------
-- create an account terminal at localhost and set its password
-- the terminal account is applied to terminal
-- -----------------------------------------------------
GRANT SELECT, INSERT, UPDATE, DELETE ON wireless_order_db.* to terminal@`localhost`;
SET PASSWORD FOR terminal@localhost=PASSWORD('terminal@digi-e');

DELETE FROM mysql.user WHERE user='custom_user';
-- -----------------------------------------------------
-- create an account custom_user at localhost and set its password
-- the custom_user account is applied to the customer
-- -----------------------------------------------------
GRANT SELECT, INSERT, UPDATE, DELETE ON wireless_order_db.* to custom_user@`localhost`;
SET PASSWORD FOR custom_user@localhost=PASSWORD('custom_user@digi-e');

DELETE FROM mysql.user WHERE user='dba';
-- -----------------------------------------------------
-- create an account dba at localhost and set its password
-- the dba account is applied to the dba of wireless order db
-- it has all the privileges on the db.
-- -----------------------------------------------------
GRANT ALL ON wireless_order_db.* to dba@`localhost`;
SET PASSWORD FOR dba@localhost=PASSWORD('dba@digi-e');

DELETE FROM mysql.user WHERE user='digie';
-- -----------------------------------------------------
-- create an account digie at localhost and set its password
-- the digie account is applied to the digie of wireless order db
-- it has all the privileges on the db.
-- -----------------------------------------------------
GRANT ALL ON wireless_order_db.* to digie@`localhost`;
SET PASSWORD FOR digie@localhost=PASSWORD('HelloZ315');

