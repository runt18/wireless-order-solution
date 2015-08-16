SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';
SET @OLD_SAFE_UPDATES=@@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;
SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
aa-- Table `wireless_order_db`.`member_cond`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`member_cond` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`member_cond` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `restaurant_id` INT NULL,
  `name` VARCHAR(45) NULL,
  `member_type_id` INT NULL,
  `range_type` TINYINT NULL,
  `begin_date` DATETIME NULL,
  `end_date` DATETIME NULL,
  `min_consume_money` FLOAT NULL,
  `max_consume_money` FLOAT NULL,
  `min_consume_amount` INT NULL,
  `max_consume_amount` INT NULL,
  `min_balance` FLOAT NULL,
  `max_balance` FLOAT NULL,
  PRIMARY KEY (`id`),
  INDEX `ix_restaurant_id` (`restaurant_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Insert the '沉睡会员' to each restaurant, 3个月内消费不足3次
-- -----------------------------------------------------
INSERT INTO wireless_order_db.member_cond 
(restaurant_id, name, range_type, max_consume_amount)
SELECT id, '沉睡会员', 3, 3 FROM wireless_order_db.restaurant WHERE id > 10;

-- -----------------------------------------------------
-- Insert the '沉睡会员' to each restaurant, 3个月内消费5次
-- -----------------------------------------------------
INSERT INTO wireless_order_db.member_cond 
(restaurant_id, name, range_type, min_consume_amount)
SELECT id, '活跃会员', 3, 5 FROM wireless_order_db.restaurant WHERE id > 10;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;



