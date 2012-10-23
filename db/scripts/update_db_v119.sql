SET NAMES utf8;
USE wireless_order_db;

-- -----------------------------------------------------
-- Drop table `wireless_order_db`.`member_charge`
-- -----------------------------------------------------
DROP TABLE `wireless_order_db`.`member_charge`

-- -----------------------------------------------------
-- Table `wireless_order_db`.`member_type`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`member_type` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`member_type` (
  `member_type_id` INT NOT NULL AUTO_INCREMENT COMMENT 'the id to member type' ,
  `discount_id` INT UNSIGNED NOT NULL COMMENT 'the discount id this member type uses' ,
  `name` VARCHAR(45) NOT NULL COMMENT 'the name to this member type' ,
  PRIMARY KEY (`member_type_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the member type information' ;

-- -----------------------------------------------------
-- Table `wireless_order_db`.`client`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`client` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`client` (
  `client_id` INT NOT NULL AUTO_INCREMENT COMMENT 'thie id to this client' ,
  `restaurant_id` INT UNSIGNED NOT NULL COMMENT 'the restaurant id to this client' ,
  `client_type_id` INT NOT NULL COMMENT 'the type this client belongs to' ,
  `name` VARCHAR(45) NOT NULL COMMENT 'the name to this client' ,
  `sex` TINYINT NOT NULL DEFAULT 0 COMMENT 'the sex to this client' ,
  `tele` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the telephone to this client' ,
  `mobile` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the mobile to this client' ,
  `birthday` DATE NULL DEFAULT NULL COMMENT 'the birthday to this client' ,
  `id_card` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the id card to this client' ,
  `company` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the company to this client' ,
  `taste_pref` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the taste preference to client' ,
  `taboo` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the taboo to client' ,
  `contact_addr` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the contact address to client' ,
  `comment` VARCHAR(45) NULL DEFAULT NULL COMMENT 'the comment to client' ,
  `level` TINYINT NULL DEFAULT 0 COMMENT 'the status to client.\n0 - normal\n1 - reserved' ,
  PRIMARY KEY (`client_id`) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) ,
  INDEX `ix_client_type_id` (`client_type_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the client' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`client_type`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`client_type` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`client_type` (
  `client_type_id` INT NOT NULL ,
  `name` VARCHAR(45) NULL ,
  PRIMARY KEY (`client_type_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the clent type' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`member_card`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`member_card` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`member_card` (
  `member_card_id` INT NOT NULL AUTO_INCREMENT ,
  `restaurant_id` INT UNSIGNED NOT NULL ,
  `member_card_alias` VARCHAR(45) NULL DEFAULT NULL ,
  PRIMARY KEY (`member_card_id`) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the member card' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`member_card_ownership`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`member_card_ownership` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`member_card_ownership` (
  `member_id` INT NOT NULL COMMENT 'the id to member' ,
  `member_card_id` INT NOT NULL COMMENT 'the id to member card' ,
  PRIMARY KEY (`member_id`, `member_card_id`) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe which member card belongs to which member' ;


-- -----------------------------------------------------
-- Table `wireless_order_db`.`member`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `wireless_order_db`.`member` ;

CREATE  TABLE IF NOT EXISTS `wireless_order_db`.`member` (
  `member_id` INT NOT NULL COMMENT 'the id to this member' ,
  `restaurant_id` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'the restaurant id to this member' ,
  `client_id` INT NOT NULL COMMENT 'the client id to this member' ,
  `member_type_id` INT NOT NULL COMMENT 'the type this member belongs to' ,
  `balance` DECIMAL(7,2) NOT NULL DEFAULT 0 COMMENT 'the balance to this member' ,
  `point` INT NOT NULL DEFAULT 0 COMMENT 'the remaining point to this member' ,
  PRIMARY KEY (`member_id`) ,
  INDEX `ix_client_id` (`client_id` ASC) ,
  INDEX `ix_member_type_id` (`member_type_id` ASC) ,
  INDEX `ix_restaurant_id` (`restaurant_id` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8, 
COMMENT = 'describe the member' ;