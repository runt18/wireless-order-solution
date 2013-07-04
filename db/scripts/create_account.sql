DELETE FROM mysql.user WHERE user='socket';
DELETE FROM mysql.db WHERE user='socket';
-- -----------------------------------------------------
-- create an account socket at localhost and set its password
-- -----------------------------------------------------
GRANT ALL ON wireless_order_db.* to socket@`localhost`;
SET PASSWORD FOR socket@localhost=PASSWORD('socket@digie');

DELETE FROM mysql.user WHERE user='web';
DELETE FROM mysql.db WHERE user='web';
-- -----------------------------------------------------
-- create an account web at localhost and set its password
-- -----------------------------------------------------
GRANT ALL ON wireless_order_db.* to web@`localhost`;
SET PASSWORD FOR web@localhost=PASSWORD('web@digie');

DELETE FROM mysql.user WHERE user='dba';
DELETE FROM mysql.db WHERE user='dba';
-- -----------------------------------------------------
-- create an account dba at localhost and set its password
-- the dba account is applied to the dba of wireless order db
-- it has all the privileges on the db.
-- -----------------------------------------------------
GRANT ALL ON wireless_order_db.* to dba@`localhost`;
SET PASSWORD FOR dba@localhost=PASSWORD('dba@digie');


