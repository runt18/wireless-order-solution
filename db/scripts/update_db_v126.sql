SET @OLD_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
SET NAMES utf8;
SET SQL_SAFE_UPDATES = 0;
USE wireless_order_db;

-- -----------------------------------------------------
-- Change the field 'category' to 'category_id'.
-- Change the type to field 'price' to float.
-- Change the type to field 'rate' to float.
-- -----------------------------------------------------
ALTER TABLE `wireless_order_db`.`taste` 
CHANGE COLUMN `category` `category_id` INT NOT NULL AFTER `restaurant_id`,
CHANGE COLUMN `price` `price` FLOAT NOT NULL DEFAULT '0.00' COMMENT 'the price to this taste preference' ,
CHANGE COLUMN `rate` `rate` FLOAT NOT NULL DEFAULT '0.00' COMMENT 'the rate to this taste, used for the calc type is 按比例' ,
ADD INDEX `ix_category_id` (`category_id` ASC);

-- -----------------------------------------------------
-- Table `wireless_order_db`.`taste_category`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`taste_category` ;

CREATE TABLE IF NOT EXISTS `wireless_order_db`.`taste_category` (
  `category_id` INT NOT NULL AUTO_INCREMENT,
  `restaurant_id` INT NOT NULL,
  `display_id` INT NOT NULL,
  `name` VARCHAR(45) NOT NULL,
  `type` TINYINT NOT NULL COMMENT 'the type as below.\n1 - normal\n2 - reserved',
  `status` TINYINT NOT NULL COMMENT 'the status as below.\n1 - 规格\n2 - 口味',
  PRIMARY KEY (`category_id`),
  INDEX `ix_restaurant_id` (`restaurant_id` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8;

-- -----------------------------------------------------
-- Insert '口味'、'规格' to each restaurant
-- -----------------------------------------------------
INSERT INTO wireless_order_db.taste_category
(`restaurant_id`, `name`, `type`, `status`, `display_id`)
SELECT id, '规格', 2, 1, 2 FROM `wireless_order_db`.`restaurant` WHERE id > 10;

INSERT INTO wireless_order_db.taste_category
(`restaurant_id`, `name`, `type`, `status`, `display_id`)
SELECT id, '口味', 1, 2, 1 FROM `wireless_order_db`.`restaurant` WHERE id > 10;

-- -----------------------------------------------------
-- Update each taste category_id 
-- -----------------------------------------------------
UPDATE wireless_order_db.taste T
JOIN wireless_order_db.taste_category TC ON T.restaurant_id = TC.restaurant_id AND T.category_id = 0 AND TC.status = 2
SET T.category_id = TC.category_id;

UPDATE wireless_order_db.taste T
JOIN wireless_order_db.taste_category TC ON T.restaurant_id = TC.restaurant_id AND T.category_id = 2 AND TC.status = 1
SET T.category_id = TC.category_id;

SET SQL_SAFE_UPDATES = @OLD_SAFE_UPDATES;