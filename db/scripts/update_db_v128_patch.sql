SET @OLD_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
SET NAMES utf8;
SET SQL_SAFE_UPDATES = 0;
USE wireless_order_db;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`payment`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`payment` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`payment` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `restaurant_id` INT NOT NULL,
  `staff_id` INT NOT NULL,
  `staff_name` VARCHAR(45) NULL DEFAULT NULL,
  `on_duty` DATETIME NOT NULL,
  `off_duty` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `ix_restaurant_id` (`restaurant_id` ASC),
  INDEX `ix_staff_id` (`staff_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`payment_history`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`payment_history` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`payment_history` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `restaurant_id` INT NOT NULL,
  `staff_id` INT NOT NULL,
  `staff_name` VARCHAR(45) NULL DEFAULT NULL,
  `on_duty` DATETIME NOT NULL,
  `off_duty` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `ix_restaurant_id` (`restaurant_id` ASC),
  INDEX `ix_staff_id` (`staff_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`business_hour`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`business_hour` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`business_hour` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `restaurant_id` INT NOT NULL,
  `name` VARCHAR(45) NULL DEFAULT NULL,
  `opening` TIME NOT NULL,
  `ending` TIME NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `ix_restaurant_id` (`restaurant_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Insert the '早市', '午市', '晚市' to each restaurant
-- -----------------------------------------------------
INSERT INTO wireless_order_db.business_hour
(restaurant_id, name, opening, ending)
SELECT id, '早市', '6:00', '11:00'
FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.business_hour
(restaurant_id, name, opening, ending)
SELECT id, '午市', '11:00', '15:00'
FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.business_hour
(restaurant_id, name, opening, ending)
SELECT id, '晚市', '18:00', '23:00'
FROM wireless_order_db.restaurant WHERE id > 10;

-- -----------------------------------------------------
-- Insert the idle department[11 - 20] to each restaurant
-- -----------------------------------------------------
INSERT INTO wireless_order_db.department
(dept_id, restaurant_id, name, type, display_id)
SELECT 10, id, '部门11', 1, 11
FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.department
(dept_id, restaurant_id, name, type, display_id)
SELECT 11, id, '部门12', 1, 12
FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.department
(dept_id, restaurant_id, name, type, display_id)
SELECT 12, id, '部门13', 1, 13
FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.department
(dept_id, restaurant_id, name, type, display_id)
SELECT 13, id, '部门14', 1, 14
FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.department
(dept_id, restaurant_id, name, type, display_id)
SELECT 14, id, '部门15', 1, 15
FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.department
(dept_id, restaurant_id, name, type, display_id)
SELECT 15, id, '部门16', 1, 16
FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.department
(dept_id, restaurant_id, name, type, display_id)
SELECT 16, id, '部门17', 1, 17
FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.department
(dept_id, restaurant_id, name, type, display_id)
SELECT 17, id, '部门18', 1, 18
FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.department
(dept_id, restaurant_id, name, type, display_id)
SELECT 18, id, '部门19', 1, 19
FROM wireless_order_db.restaurant WHERE id > 10;

INSERT INTO wireless_order_db.department
(dept_id, restaurant_id, name, type, display_id)
SELECT 19, id, '部门20', 1, 20
FROM wireless_order_db.restaurant WHERE id > 10;

SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;



