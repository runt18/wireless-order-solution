SET NAMES utf8;

-- -----------------------------------------------------
-- Delete all the info before creating the demo	
-- -----------------------------------------------------
DELETE FROM wireless_order_db.order_food_material_history WHERE order_food_id IN (SELECT id FROM wireless_order_db.order_food_history WHERE order_id IN (SELECT id FROM wireless_order_db.order_history WHERE restaurant_id=11) );
DELETE FROM wireless_order_db.order_food_history WHERE order_id IN (SELECT id FROM wireless_order_db.order_history WHERE restaurant_id=11);
DELETE FROM wireless_order_db.order_history WHERE restaurant_id=11;

DELETE FROM wireless_order_db.order_food_material WHERE order_food_id IN (SELECT id FROM wireless_order_db.order_food WHERE order_id IN (SELECT id FROM wireless_order_db.order WHERE restaurant_id=11) );
DELETE FROM wireless_order_db.order_food WHERE order_id IN (SELECT id FROM wireless_order_db.order WHERE restaurant_id=11);
DELETE FROM wireless_order_db.order WHERE restaurant_id=11;

DELETE FROM wireless_order_db.staff WHERE restaurant_id=11;

DELETE FROM wireless_order_db.food_material WHERE food_id IN (SELECT id FROM wireless_order_db.food WHERE restaurant_id=11);
DELETE FROM wireless_order_db.material_history WHERE material_id IN (SELECT id FROM wireless_order_db.material WHERE restaurant_id=11);
DELETE FROM wireless_order_db.material WHERE restaurant_id=11;
DELETE FROM wireless_order_db.terminal WHERE restaurant_id=11;
DELETE FROM wireless_order_db.table WHERE restaurant_id=11;
DELETE FROM wireless_order_db.food WHERE restaurant_id=11;
DELETE FROM wireless_order_db.taste WHERE restaurant_id=11;
DELETE FROM wireless_order_db.kitchen WHERE restaurant_id=11;

DELETE FROM wireless_order_db.member_charge WHERE member_id IN (SELECT id FROM wireless_order_db.member WHERE restaurant_id=11);
DELETE FROM wireless_order_db.member WHERE restaurant_id=11;

DELETE FROM wireless_order_db.restaurant WHERE id=11;

-- -----------------------------------------------------
-- Insert the demo restaruant
-- -----------------------------------------------------
SET AUTOCOMMIT=1;
INSERT INTO `wireless_order_db`.`restaurant` (`id`, `pwd`, `account`, `restaurant_name`, `record_alive`) VALUES ('11', MD5('demo@123'), 'demo', '演示餐厅', '0');

-- -----------------------------------------------------
-- Insert test taste perference bound to the demo restaurant
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
-- DELETE FROM wireless_order_db.taste WHERE restaurant_id=11;
INSERT INTO `wireless_order_db`.`taste` (`restaurant_id`, `alias_id`, `preference`, `price`) VALUES (11, 1, '加辣', 2.5);
INSERT INTO `wireless_order_db`.`taste` (`restaurant_id`, `alias_id`, `preference`, `price`) VALUES (11, 2, '少盐', 0);
INSERT INTO `wireless_order_db`.`taste` (`restaurant_id`, `alias_id`, `preference`, `price`) VALUES (11, 3, '少辣', 5.0);
COMMIT;
-- SELECT * FROM terminal;

-- -----------------------------------------------------
-- Insert test terminals bound to the demo restaurant
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
-- DELETE FROM wireless_order_db.terminal WHERE restaurant_id=11;
INSERT INTO `wireless_order_db`.`terminal` (`pin`, `restaurant_id`, `model_id`, `model_name`, `owner_name`, `entry_date`, `expire_date`, `work_date`) VALUES (0x2100000A, 11, 0x00, 'BlackBerry 8100', '温晓宁', 20101223, 20140717, NOW());
COMMIT;
-- SELECT * FROM terminal;

-- -----------------------------------------------------
-- Insert test tables(bound to demo restaurant, range from 100 to 145, 200 to 205)
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
-- DELETE FROM wireless_order_db.table WHERE restaurant_id=11;
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`, `name`) VALUES (0xB00000064, 0x64, 11, '菊花厅');
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`, `name`) VALUES (0xB00000065, 0x65, 11, '水仙厅');
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`, `name`) VALUES (0xB00000066, 0x66, 11, '兰花厅');
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB00000067, 0x67, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB00000068, 0x68, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB00000069, 0x69, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB0000006A, 0x6A, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB0000006B, 0x6B, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB0000006C, 0x6C, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB0000006D, 0x6D, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB0000006E, 0x6E, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB0000006F, 0x6F, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB00000070, 0x70, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB00000071, 0x71, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB00000072, 0x72, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB00000073, 0x73, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB00000074, 0x74, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB00000075, 0x75, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB00000076, 0x76, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB00000077, 0x77, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB00000078, 0x78, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB00000079, 0x79, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB0000007A, 0x7A, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB0000007B, 0x7B, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB0000007C, 0x7C, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB0000007D, 0x7D, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB0000007E, 0x7E, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB0000007F, 0x7F, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB00000080, 0x80, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB00000081, 0x81, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB00000082, 0x82, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB00000083, 0x83, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB00000084, 0x84, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB00000085, 0x85, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB00000086, 0x86, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB00000087, 0x87, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB00000088, 0x88, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB00000089, 0x89, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB0000008A, 0x8A, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB0000008B, 0x8B, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB0000008C, 0x8C, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB0000008D, 0x8D, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB0000008E, 0x8E, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB0000008F, 0x8F, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB00000090, 0x90, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB00000091, 0x91, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB000000C8, 0xC8, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB000000C9, 0xC9, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB000000CA, 0xCA, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB000000CB, 0xCB, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `restaurant_id`) VALUES (0xB000000CC, 0xCC, 11);

COMMIT;
-- SELECT * FROM wireless_order_db_demo.table;

-- -----------------------------------------------------
-- Insert kitchen records, note that each restaruant has ten kitchens
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
INSERT INTO `wireless_order_db`.`kitchen` (`restaurant_id`, `alias_id`, `name`, `discount`, `member_discount_1`, `member_discount_2`) VALUES (11, 0, '明档', 1.00, 1.00, 1.00);
INSERT INTO `wireless_order_db`.`kitchen` (`restaurant_id`, `alias_id`, `name`, `discount`, `member_discount_1`, `member_discount_2`) VALUES (11, 1, '烧味', 0.95, 0.9, 0.9);
INSERT INTO `wireless_order_db`.`kitchen` (`restaurant_id`, `alias_id`, `name`, `discount`, `member_discount_1`, `member_discount_2`) VALUES (11, 2, '海鲜', 1.00, 1.00, 1.00);
INSERT INTO `wireless_order_db`.`kitchen` (`restaurant_id`, `alias_id`, `name`) VALUES (11, 3, '厨房4');
INSERT INTO `wireless_order_db`.`kitchen` (`restaurant_id`, `alias_id`, `name`) VALUES (11, 4, '厨房5');
INSERT INTO `wireless_order_db`.`kitchen` (`restaurant_id`, `alias_id`, `name`) VALUES (11, 5, '厨房6');
INSERT INTO `wireless_order_db`.`kitchen` (`restaurant_id`, `alias_id`, `name`) VALUES (11, 6, '厨房7');
INSERT INTO `wireless_order_db`.`kitchen` (`restaurant_id`, `alias_id`, `name`) VALUES (11, 7, '厨房8');
INSERT INTO `wireless_order_db`.`kitchen` (`restaurant_id`, `alias_id`, `name`) VALUES (11, 8, '厨房9');
INSERT INTO `wireless_order_db`.`kitchen` (`restaurant_id`, `alias_id`, `name`) VALUES (11, 9, '厨房10');
COMMIT;
SET AUTOCOMMIT=1;

-- -----------------------------------------------------
-- Insert member records
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
INSERT INTO `wireless_order_db`.`member` (`id`, `restaurant_id`, `alias_id`, `name`, `tele`, `birth`, `balance`, `discount_type`, `exchange_rate`) VALUES (1, 11, '13694260535', '熊至明', '13694260535', 19810315, 1500, 0, 0);
INSERT INTO `wireless_order_db`.`member` (`id`, `restaurant_id`, `alias_id`, `name`, `tele`, `birth`, `balance`, `discount_type`, `exchange_rate`) VALUES (2, 11, '13632654789', '刘天宁', '13632654789', 19720615, 250.00, 0, 0);
INSERT INTO `wireless_order_db`.`member` (`id`, `restaurant_id`, `alias_id`, `name`, `tele`, `birth`, `balance`, `discount_type`, `exchange_rate`) VALUES (3, 11, '18854236534', '李小明', '18854236534', 19781230, 300.00, 0, 0);
COMMIT;
SET AUTOCOMMIT=1;

-- -----------------------------------------------------
-- Insert member charge records
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
INSERT INTO `wireless_order_db`.`member_charge` (`member_id`, `date`, `money`) VALUES (1, 20110102142014, 500);
INSERT INTO `wireless_order_db`.`member_charge` (`member_id`, `date`, `money`) VALUES (1, 20110202090005, 1500);
INSERT INTO `wireless_order_db`.`member_charge` (`member_id`, `date`, `money`) VALUES (1, 20110404143054, -500);
INSERT INTO `wireless_order_db`.`member_charge` (`member_id`, `date`, `money`) VALUES (2, 20110101113014, 124.5);
INSERT INTO `wireless_order_db`.`member_charge` (`member_id`, `date`, `money`) VALUES (2, 20110101115014, 124.5);
INSERT INTO `wireless_order_db`.`member_charge` (`member_id`, `date`, `money`) VALUES (3, 20110501000000, 300);
COMMIT;
SET AUTOCOMMIT=1;

-- -----------------------------------------------------
-- Insert material records
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
INSERT INTO `wireless_order_db`.`material` (`id`, `restaurant_id`, `alias_id`, `name`, `stock`, `price`, `warning_threshold`, `danger_threshold`) VALUES (1, 11, '100', '排骨', 35.00, 18.5, 10.5, 5.5);
INSERT INTO `wireless_order_db`.`material` (`id`, `restaurant_id`, `alias_id`, `name`, `stock`, `price`, `warning_threshold`, `danger_threshold`) VALUES (2, 11, '101', '鸡肉', 57.50, 13, 22, 14);
INSERT INTO `wireless_order_db`.`material` (`id`, `restaurant_id`, `alias_id`, `name`, `stock`, `price`, `warning_threshold`, `danger_threshold`) VALUES (3, 11, '102', '牛肉', 43.00, 16, 27, 13);
INSERT INTO `wireless_order_db`.`material` (`id`, `restaurant_id`, `alias_id`, `name`, `stock`, `price`, `warning_threshold`, `danger_threshold`) VALUES (4, 11, '103', '猪肉', 54.00, 13.5, 27, 13);
INSERT INTO `wireless_order_db`.`material` (`id`, `restaurant_id`, `alias_id`, `name`, `stock`, `price`, `warning_threshold`, `danger_threshold`) VALUES (5, 11, '104', '酱汁', 100.00, 1, 30, 20);
COMMIT;
SET AUTOCOMMIT=1;

-- -----------------------------------------------------
-- Insert material records
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
INSERT INTO `wireless_order_db`.`material_history` (`material_id`, `date`, `price`, `amount`) VALUES (1, 20110410, 14.5, 20);
INSERT INTO `wireless_order_db`.`material_history` (`material_id`, `date`, `price`, `amount`) VALUES (1, 20110411, 13.5, 5.5);
INSERT INTO `wireless_order_db`.`material_history` (`material_id`, `date`, `price`, `amount`) VALUES (1, 20110412, 15, 10);
INSERT INTO `wireless_order_db`.`material_history` (`material_id`, `date`, `price`, `amount`) VALUES (2, 20110410, 10, 13);
INSERT INTO `wireless_order_db`.`material_history` (`material_id`, `date`, `price`, `amount`) VALUES (2, 20110411, 9, 6.5);
INSERT INTO `wireless_order_db`.`material_history` (`material_id`, `date`, `price`, `amount`) VALUES (2, 20110412, 12, 10);
INSERT INTO `wireless_order_db`.`material_history` (`material_id`, `date`, `price`, `amount`) VALUES (3, 20110410, 4.5, 23);
INSERT INTO `wireless_order_db`.`material_history` (`material_id`, `date`, `price`, `amount`) VALUES (3, 20110411, 9.7, 5.5);
INSERT INTO `wireless_order_db`.`material_history` (`material_id`, `date`, `price`, `amount`) VALUES (3, 20110412, 4.2, 10);
INSERT INTO `wireless_order_db`.`material_history` (`material_id`, `date`, `price`, `amount`) VALUES (4, 20110410, 10.5, 20);
INSERT INTO `wireless_order_db`.`material_history` (`material_id`, `date`, `price`, `amount`) VALUES (4, 20110411, 13.5, 5.5);
INSERT INTO `wireless_order_db`.`material_history` (`material_id`, `date`, `price`, `amount`) VALUES (4, 20110412, 19, 10);
INSERT INTO `wireless_order_db`.`material_history` (`material_id`, `date`, `price`, `amount`) VALUES (5, 20110410, 14.5, 12);
INSERT INTO `wireless_order_db`.`material_history` (`material_id`, `date`, `price`, `amount`) VALUES (5, 20110411, 22.5, 22.5);
INSERT INTO `wireless_order_db`.`material_history` (`material_id`, `date`, `price`, `amount`) VALUES (5, 20110412, 15, 10);
COMMIT;
SET AUTOCOMMIT=1;

-- -----------------------------------------------------
-- Insert test food menu(bound to demo restaurant, range from 1100 to 1150, 1200 to 1250)
-- Insert consumption between food and material
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
-- DELETE FROM wireless_order_db.food WHERE restaurant_id=11;
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB0000044C, 0x44C, '京都骨', 23.53, 11, 1, 'jdg');
INSERT INTO `wireless_order_db`.`food_material` (`food_id`, `material_id`, `consumption`) VALUES (0xB0000044C, 1, 1);
INSERT INTO `wireless_order_db`.`food_material` (`food_id`, `material_id`, `consumption`) VALUES (0xB0000044C, 5, 1);
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB0000044D, 0x44D, '京酱肉丝', 35.3, 11, 0, 'jjrs');
INSERT INTO `wireless_order_db`.`food_material` (`food_id`, `material_id`, `consumption`) VALUES (0xB0000044D, 4, 1);
INSERT INTO `wireless_order_db`.`food_material` (`food_id`, `material_id`, `consumption`) VALUES (0xB0000044D, 5, 1.5);
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB0000044E, 0x44E, '白切鸡', 21.00, 11, 0, 'bqj');
INSERT INTO `wireless_order_db`.`food_material` (`food_id`, `material_id`, `consumption`) VALUES (0xB0000044E, 2, 1);
INSERT INTO `wireless_order_db`.`food_material` (`food_id`, `material_id`, `consumption`) VALUES (0xB0000044E, 5, 0.5);
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB0000044F, 0x44F, '盐水菜心', 10.67, 11, 0, 'yscx');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB00000450, 0x450, '湛江鸡', 26, 11, 0, 'zjj');
INSERT INTO `wireless_order_db`.`food_material` (`food_id`, `material_id`, `consumption`) VALUES (0xB00000450, 2, 1);
INSERT INTO `wireless_order_db`.`food_material` (`food_id`, `material_id`, `consumption`) VALUES (0xB00000450, 5, 0.5);
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB00000451, 0x451, '东波肉', 16, 11, 0, 'dpr');
INSERT INTO `wireless_order_db`.`food_material` (`food_id`, `material_id`, `consumption`) VALUES (0xB00000451, 4, 2);
INSERT INTO `wireless_order_db`.`food_material` (`food_id`, `material_id`, `consumption`) VALUES (0xB00000451, 5, 1.5);
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB00000452, 0x452, '红烧排骨', 32, 11, 1, 'hspg');
INSERT INTO `wireless_order_db`.`food_material` (`food_id`, `material_id`, `consumption`) VALUES (0xB00000452, 1, 1);
INSERT INTO `wireless_order_db`.`food_material` (`food_id`, `material_id`, `consumption`) VALUES (0xB00000452, 5, 1.5);
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`, `status`) VALUES (0xB00000453, 0x453, '清蒸桂花鱼', 29, 11, 0, 'qzghy', 1);
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`, `status`) VALUES (0xB00000454, 0x454, '鼓油王鹅肠', 24, 11, 0, 'jywec', 3);
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`, `status`) VALUES (0xB00000455, 0x455, '白灼猪肚', 22.5, 11, 0, 'bzzd', 7);
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB00000456, 0x456, '水煮鱼', 39, 11, 0, 'szy');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB00000457, 0x457, '酸菜鱼', 34, 11, 0, 'scy');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB00000458, 0x458, '鱼香肉丝', 16, 11, 0, 'yxrs');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB00000459, 0x459, '白爆鱼丁', 18, 11, 0, 'bbyd');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB0000045A, 0x45A, '白菜粉丝', 15, 11, 0, 'bcfs');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB0000045B, 0x45B, '白饭', 1.5, 11, 0, 'bf');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB0000045C, 0x45C, '鲍鱼龙须', 23.5, 11, 0, 'bylx');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB0000045D, 0x45D, '北菰炒面', 13, 11, 0, 'bgcm');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB0000045E, 0x45E, '北菰烩鸭丝', 19, 11, 0, 'bghys');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB0000045F, 0x45F, '北京烤鸭', 41, 11, 0, 'bjky');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB00000460, 0x460, '北京泡菜', 6, 11, 0, 'bjpc');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB00000461, 0x461, '比利时烩鸡', 69, 11, 0, 'blshj');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB00000462, 0x462, '碧绿生鱼球', 35, 11, 0, 'blsyq');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB00000463, 0x463, '冰冻荔枝', 23, 11, 0, 'bdlz');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB000004B0, 0x4B0, '冰淇淋', 8, 11, 0, 'bql');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB000004B1, 0x4B1, '冰肉莲蓉粽', 6.5, 11, 0, 'brlrz');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB000004B2, 0x4B2, '菠菜炒鸡蛋', 11, 11, 0, 'bccjd');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB000004B3, 0x4B3, '腊肠', 8.5, 11, 0, 'lc');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB000004B4, 0x4B4, '腊金银润', 10, 11, 0, 'ljyr');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB000004B5, 0x4B5, '辣椒肉丝', 10.5, 11, 0, 'ljrs');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB000004B6, 0x4B6, '辣蔬菜', 11.3, 11, 0, 'lsc');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB000004B7, 0x4B7, '辣汁鱼头', 22, 11, 0, 'lzyt');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB000004B8, 0x4B8, '辣子炒鸡丁', 16.5, 11, 0, 'lzcjd');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB000004B9, 0x4B9, '辣子炒肉丁', 17, 11, 0, 'lzcrd');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB000004BA, 0x4BA, '辣子肉丁', 16, 11, 0, 'lzrd');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB000004BB, 0x4BB, '粒驻鲍鱼', 79, 11, 2, 'lzby');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB000004BC, 0x4BC, '烂鸡鱼翅', 125, 11, 2, 'ljyc');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB000004BD, 0x4BD, '醪糟百子果羹', 36, 11, 0, 'ncbzgj');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB000004BE, 0x4BE, '老鸡蛋托黑鱼子', 35, 11, 0, 'ljdthyz');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB000004BF, 0x4BF, '烙饼', 6, 11, 0, 'lb');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB000004C0, 0x4C0, '冷茶肠', 11.5, 11, 0, 'lcc');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB000004C1, 0x4C1, '冷醋鱼', 13.5, 11, 0, 'lky');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB000004C2, 0x4C2, '冷火腿蔬菜', 7, 11, 0, 'lhtsc');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB000004C3, 0x4C3, '冷鸡冻', 23, 11, 0, 'ljd');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB000004C4, 0x4C4, '冷烤鸭', 16, 11, 0, 'lky');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB000004C5, 0x4C5, '冷辣白菜', 10, 11, 0, 'llbc');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB000004C6, 0x4C6, '冷牛舌', 14, 11, 0, 'lns');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB000004C7, 0x4C7, '冷阉鸡', 16, 11, 0, 'lyj');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB000004C8, 0x4C8, '荔脯芋香角', 16.5, 11, 0, 'lpyxj');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB000004C9, 0x4C9, '栗子鸡', 21, 11, 0, 'lzj');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (0xB000004CA, 0x4CA, '莲煎软饼', 3.5, 11, 0, 'ljrb');
COMMIT;
SET AUTOCOMMIT=1;
-- SELECT * FROM food;

-- -----------------------------------------------------
-- Insert order and the associated order_food history records
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`, `member_id`, `member`) VALUES (1, 11, 0x64, 0x20237AB8, 20110102, 214, 214, 5, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), '13694260535', '熊至明');
INSERT INTO `wireless_order_db`.`order_food_history` (`id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`, `kitchen`) VALUES (1, 1, 0x44C, 1.2, '京都骨', 23.53, NOW(), '张宁远', NULL, 1);
INSERT INTO `wireless_order_db`.`order_food_material_history`(`order_food_id`, `material_id`, `name`, `price`, `consumption`) VALUES (1, 100, '排骨', 18.5, 1);

-- the order detail to 京酱肉丝
INSERT INTO `wireless_order_db`.`order_food_history` (`id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (2, 1, 0x44D, 2, '京酱肉丝', 35.3, 20110411180911, '张宁远', NULL);
INSERT INTO `wireless_order_db`.`order_food_material_history`(`order_food_id`, `material_id`, `name`, `price`, `consumption`) VALUES (2, 103, '猪肉', 13.5, 1);
INSERT INTO `wireless_order_db`.`order_food_history` (`id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (3, 1, 0x44D, 2, '京酱肉丝', 35.3, 20110411191523, '张宁远', NULL);
INSERT INTO `wireless_order_db`.`order_food_material_history`(`order_food_id`, `material_id`, `name`, `price`, `consumption`) VALUES (3, 103, '猪肉', 13.5, 1);

-- the order detail to 白切鸡
INSERT INTO `wireless_order_db`.`order_food_history` (`id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (4, 1, 0x44E, 1, '白切鸡', 21.00, 20110411171200, '张宁远', NULL);
INSERT INTO `wireless_order_db`.`order_food_material_history`(`order_food_id`, `material_id`, `name`, `price`, `consumption`) VALUES (4, 101, '鸡肉', 13, 1);
INSERT INTO `wireless_order_db`.`order_food_history` (`id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (5, 1, 0x44E, -1, '白切鸡', 21.00, 20110411174521, '黄家声', '客人退菜');
INSERT INTO `wireless_order_db`.`order_food_material_history`(`order_food_id`, `material_id`, `name`, `price`, `consumption`) VALUES (5, 101, '鸡肉', 13, 1);

-- the order detail to 湛江鸡
INSERT INTO `wireless_order_db`.`order_food_history` (`id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`, `discount`) VALUES (6, 1, 0x450, 6, '湛江鸡', 26, 20110412170931, '黄家声', NULL, 0.98);
INSERT INTO `wireless_order_db`.`order_food_material_history`(`order_food_id`, `material_id`, `name`, `price`, `consumption`) VALUES (6, 101, '鸡肉', 13, 1);
INSERT INTO `wireless_order_db`.`order_food_history` (`id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`, `discount`) VALUES (7, 1, 0x450, -1, '湛江鸡', 26, 20110412170931, '黄家声', '客人退菜', 0.98);
INSERT INTO `wireless_order_db`.`order_food_material_history`(`order_food_id`, `material_id`, `name`, `price`, `consumption`) VALUES (7, 101, '鸡肉', 13, 1);

-- the order detail to 盐水菜心
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`, `discount`) VALUES (1, 0x44F, 2, '盐水菜心', 10.67, 20110412181511, '黄家声', NULL, 0.75);


INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`, `member_id`, `member`, `type`, `comment`) VALUES (2, 11, 0x65, 0x20237AB8, 20110203, 543, 540, 6, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), '13632654789', '刘天宁', 3, '会员卡结帐');
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (2, 0x451, 2.5, '东波肉', 16, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (2, 0x452, 2.2, '红烧排骨', 32, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (2, 0x453, 3.0, '清蒸桂花鱼', 29, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (2, 0x454, 2.6, '鼓油王鹅肠', 24, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (2, 0x455, 5, '白灼猪肚', 22.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`, `type`) VALUES (3, 11, 0x66, 0x20237AB8, 20110323, 231, 230, 10, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), 3);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (3, 0x456, 2, '水煮鱼', 39, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (3, 0x457, 4, '酸菜鱼', 34, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (3, 0x458, 1, '鱼香肉丝', 16, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (3, 0x459, 1, '白爆鱼丁', 18, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (3, 0x45A, 1, '白菜粉丝', 15, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`,  `custom_num`, `waiter`, `type`) VALUES (4, 11, 0x67, 0x20237AB8, 20110103, 564, 560, 5, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), 2);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (4, 0x45B, 1, '白饭', 1.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (4, 0x45C, 1, '鲍鱼龙须', 23.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (4, 0x45D, 1, '北菰炒面', 13, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (4, 0x45E, 1, '北菰烩鸭丝', 19, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (4, 0x45F, 1, '北京烤鸭', 41, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`, `type`) VALUES (5, 11, 0x68, 0x20237AB8, 20110403, 1297, 1290, 7, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), 2);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (5, 0x460, 1, '北京泡菜', 6, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (5, 0x461, 1, '比利时烩鸡', 69, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (5, 0x462, 1, '碧绿生鱼球', 35, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (5, 0x463, 1, '冰冻荔枝', 23, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (5, 0x4B0, 1, '冰淇淋', 8, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`, `type`, `comment`) VALUES (6, 11, 0x69, 0x20237AB8, 20110509, 213, 210, 8, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), 4, '挂账埋单');
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (6, 0x4B0, 1, '冰淇淋', 8, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (6, 0x4B1, 1, '冰肉莲蓉粽', 6.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (6, 0x4B2, 1, '菠菜炒鸡蛋', 11, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (6, 0x4B3, 1, '腊肠', 8.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (6, 0x4B4, 1, '腊金银润', 10, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`, `type`, `comment`) VALUES (7, 11, 0x70, 0x20237AB8, 20110421, 121, 120, 8, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), 4, '挂账埋单');
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (7, 0x4B5, 1, '辣椒肉丝', 10.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (7, 0x4B6, 1, '辣蔬菜', 11.3, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (7, 0x4B7, 1, '辣汁鱼头', 22, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (7, 0x4B8, 1, '辣子炒鸡丁', 16.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (7, 0x4B9, 1, '辣子炒肉丁', 17, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`, `type`) VALUES (8, 11, 0x69, 0x20237AB8, 20110529, 123.1, 120, 8, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), 5);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (8, 0x4B0, 1, '冰淇淋', 8, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (8, 0x4B1, 1, '冰肉莲蓉粽', 6.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (8, 0x4B2, 1, '菠菜炒鸡蛋', 11, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (8, 0x4B3, 1, '腊肠', 8.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (8, 0x4B4, 1, '腊金银润', 10, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`, `type`) VALUES (9, 11, 0x6A, 0x20237AB8, 20110502, 123, 120, 2, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), 5);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (9, 0x4B5, 1, '辣椒肉丝', 10.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (9, 0x4B6, 1, '辣蔬菜', 11.3, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (9, 0x4B7, 1, '辣汁鱼头', 22, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (9, 0x4B8, 1, '辣子炒鸡丁', 16.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (9, 0x4B9, 1, '辣子炒肉丁', 17, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`, `type`,  `category`, `comment`) VALUES (10, 11, 0x6B, 0x20237AB8, 20110223, 432, 430, 3, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), 1, 2, '中大外卖');
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (10, 0x4BA, 1, '辣子肉丁', 16, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (10, 0x4BB, 1, '粒驻鲍鱼', 79, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (10, 0x4BC, 1, '烂鸡鱼翅', 125, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (10, 0x4BD, 1, '醪糟百子果羹', 36, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (10, 0x4BE, 1, '老鸡蛋托黑鱼子', 35, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`, `type`, `category`, `comment`) VALUES (11, 11, 0x6C, 0x20237AB8, NOW(), 221, 220, 3, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), 1, 2, '车陂外卖');
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 0x4BF, 1.3, '烙饼', 6, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 0x4C0, 1, '冷茶肠', 4.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 0x4C1, 1, '冷醋鱼', 13.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 0x4C2, 1, '冷火腿蔬菜', 7, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 0x4C3, 1, '冷鸡冻', 23, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`, `type`) VALUES (12, 11, 0x6D, 0x20237AB8, NOW(), 221, 220, 8, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), 2);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (12, 0x4B0, 1, '冰淇淋', 8, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (12, 0x4B1, 2.5, '冰肉莲蓉粽', 6.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (12, 0x4B2, 1, '菠菜炒鸡蛋', 11, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (12, 0x4B3, 1, '腊肠', 8.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (12, 0x4B4, 1, '腊金银润', 10, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`) VALUES (13, 11, 0x6E, 0x20237AB8, NOW(), 343, 340, 2, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A));
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (13, 0x4B5, 1, '辣椒肉丝', 10.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (13, 0x4B6, 1, '辣蔬菜', 11.3, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (13, 0x457, 1, '酸菜鱼', 34, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (13, 0x4B8, 1, '辣子炒鸡丁', 16.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (13, 0x4B9, 1, '辣子炒肉丁', 17, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`) VALUES (14, 11, 0x6F, 0x20237AB8, NOW(), 123, 120, 3, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A));
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (14, 0x4BA, 1, '辣子肉丁', 16, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (14, 0x4BB, 1, '粒驻鲍鱼', 79, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (14, 0x45C, 0.5, '鲍鱼龙须', 23.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (14, 0x4BD, 2, '醪糟百子果羹', 36, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (14, 0x4BE, 1, '老鸡蛋托黑鱼子', 35, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`) VALUES (15, 11, 0x70, 0x20237AB8, NOW(), 212, 210, 3, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A));
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (15, 0x4BF, 1, '烙饼', 6, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (15, 0x450, 3, '湛江鸡', 26, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (15, 0x4C1, 1, '冷醋鱼', 13.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (15, 0x4C2, 1, '冷火腿蔬菜', 7, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (15, 0x4C3, 1, '冷鸡冻', 23, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`) VALUES (16, 11, 0x71, 0x20237AB8, NOW(), 311, 310,  3, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A));
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (16, 0x4BA, 1, '辣子肉丁', 16, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (16, 0x4BB, 1, '粒驻鲍鱼', 79, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (16, 0x4BC, 1, '烂鸡鱼翅', 125, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (16, 0x4BD, 1, '醪糟百子果羹', 36, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (16, 0x4BE, 1, '老鸡蛋托黑鱼子', 35, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`) VALUES (17, 11, 0x72, 0x20237AB8, NOW(), 333, 330, 3, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A));
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (17, 0x4BF, 1.3, '烙饼', 6, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (17, 0x4B0, 1, '冰淇淋', 8, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (17, 0x4B1, 1, '冰肉莲蓉粽', 6.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (17, 0x4B2, 1, '菠菜炒鸡蛋', 11, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (17, 0x4B3, 1, '腊肠', 8.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`) VALUES (18, 11, 0x73, 0x20237AB8, NOW(), 421, 420, 8, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A));
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (18, 0x4C0, 1, '冷茶肠', 4.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (18, 0x4B1, 2.5, '冰肉莲蓉粽', 6.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (18, 0x452, 1, '红烧排骨', 32, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (18, 0x4C3, 3, '冷鸡冻', 23, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (18, 0x4B4, 1, '腊金银润', 10, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`) VALUES (19, 11, 0x74, 0x20237AB8, NOW(), 111, 110, 2, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A));
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (19, 0x4B5, 1, '辣椒肉丝', 10.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (19, 0x4C6, 2, '冷牛舌', 14, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (19, 0x457, 1, '酸菜鱼', 34, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (19, 0x458, 3, '鱼香肉丝', 16, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (19, 0x4B9, 1, '辣子炒肉丁', 17, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`) VALUES (20, 11, 0x75, 0x20237AB8, NOW(), 302.25, 300, 3, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A));
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (20, 0x4CA, 1, '莲煎软饼', 3.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (20, 0x4BB, 2, '粒驻鲍鱼', 79, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (20, 0x45C, 0.5, '鲍鱼龙须', 23.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (20, 0x4BD, 2, '醪糟百子果羹', 36, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (20, 0x45E, 3, '北菰烩鸭丝', 19, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`) VALUES (21, 11, 0x76, 0x20237AB8, NOW(), 216, 210, 3, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A));
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (21, 0x45F, 1, '北京烤鸭', 41, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (21, 0x450, 3, '湛江鸡', 26, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (21, 0x4C5, 1, '冷辣白菜', 10, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (21, 0x452, 2, '红烧排骨', 32, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (21, 0x4C3, 1, '冷鸡冻', 23, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
COMMIT;
SET AUTOCOMMIT=1;

-- -----------------------------------------------------
-- Insert order and the associated order_food records
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
INSERT INTO `wireless_order_db`.`order` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`, `member_id`, `member`) VALUES (40, 11, 0x64, 0x20237AB8, NOW(), 214, 210, 5, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), '13694260535', '熊至明');
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (40, 0x44C, 1.2, '京都骨', 23.53, NOW(), '张宁远', NULL);

-- the order detail to 京酱肉丝
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (40, 0x44D, 2, '京酱肉丝', 35.3, 20110411180911, '张宁远', NULL);
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (40, 0x44D, 1, '京酱肉丝', 35.3, 20110411191523, '张宁远', NULL);

-- the order detail to 白切鸡
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (40, 0x44E, 1, '白切鸡', 21.00, 20110411171200, '张宁远', NULL);
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (40, 0x44E, -1, '白切鸡', 21.00, 20110411174521, '黄家声', '客人退菜');

-- the order detail to 盐水菜心
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (40, 0x44F, 2, '盐水菜心', 10.67, 20110412181511, '黄家声', NULL);

-- the order detail to 湛江鸡
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (40, 0x450, 5, '湛江鸡', 26, 20110412170931, '黄家声', NULL);
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (40, 0x450, -5, '湛江鸡', 26, 20110412170931, '黄家声', '客人退菜');

INSERT INTO `wireless_order_db`.`order` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`, `type`, `comment`) VALUES (41, 11, 0x65, 0x20237AB8, NOW(), 325.3, 320, 6, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), 2, '刷卡结帐');
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (41, 0x451, 2.5, '东波肉', 16, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (41, 0x452, 2.2, '红烧排骨', 32, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (41, 0x453, 3.0, '清蒸桂花鱼', 29, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (41, 0x454, 2.6, '鼓油王鹅肠', 24, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (41, 0x455, 5, '白灼猪肚', 22.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`, `type`) VALUES (42, 11, 0x66, 0x20237AB8, NOW(), 213.56, 210, 10, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), 3);
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (42, 0x456, 2, '水煮鱼', 39, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (42, 0x457, 4, '酸菜鱼', 34, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (42, 0x458, 1, '鱼香肉丝', 16, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (42, 0x459, 1, '白爆鱼丁', 18, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (42, 0x45A, 1, '白菜粉丝', 15, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `custom_num`, `waiter`) VALUES (43, 11, 0x67, 0x20237AB8, NOW(), 5, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A));
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (43, 0x45B, 1, '白饭', 1.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (43, 0x45C, 1, '鲍鱼龙须', 23.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (43, 0x45D, 1, '北菰炒面', 13, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (43, 0x45E, 1, '北菰烩鸭丝', 19, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (43, 0x45F, 1, '北京烤鸭', 41, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `custom_num`, `waiter`) VALUES (44, 11, 0x68, 0x20237AB8, NOW(), 7, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A));
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (44, 0x460, 1, '北京泡菜', 6, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (44, 0x461, 1, '比利时烩鸡', 69, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (44, 0x462, 1, '碧绿生鱼球', 35, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (44, 0x463, 1, '冰冻荔枝', 23, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (44, 0x4B0, 1, '冰淇淋', 8, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`,  `custom_num`, `waiter`) VALUES (45, 11, 0x6D, 0x20237AB8, NOW(),  8, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A));
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (45, 0x4B0, 1, '冰淇淋', 8, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (45, 0x4B1, 2.5, '冰肉莲蓉粽', 6.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (45, 0x4B2, 1, '菠菜炒鸡蛋', 11, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (45, 0x4B3, 1, '腊肠', 8.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (45, 0x4B4, 1, '腊金银润', 10, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`,  `custom_num`, `waiter`) VALUES (46, 11, 0x6E, 0x20237AB8, NOW(),  2, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A));
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (46, 0x4B5, 1, '辣椒肉丝', 10.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (46, 0x4B6, 1, '辣蔬菜', 11.3, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (46, 0x457, 1, '酸菜鱼', 34, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (46, 0x4B8, 1, '辣子炒鸡丁', 16.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (46, 0x4B9, 1, '辣子炒肉丁', 17, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`,  `custom_num`, `waiter`) VALUES (47, 11, 0x6F, 0x20237AB8, NOW(),  3, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A));
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (47, 0x4BA, 1, '辣子肉丁', 16, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (47, 0x4BB, 1, '粒驻鲍鱼', 79, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (47, 0x45C, 0.5, '鲍鱼龙须', 23.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (47, 0x4BD, 2, '醪糟百子果羹', 36, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (47, 0x4BE, 1, '老鸡蛋托黑鱼子', 35, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);

COMMIT;
SET AUTOCOMMIT=1;

-- -----------------------------------------------------
-- Insert staff and associated terminal info
-- -----------------------------------------------------
SET AUTOCOMMIT=0;

INSERT INTO `wireless_order_db`.`terminal` (`pin`, `restaurant_id`, `model_id`, `model_name`, `owner_name`) VALUES (1, 11, 0xFF, 'Staff', '张宁远');
INSERT INTO `wireless_order_db`.`staff` (`restaurant_id`, `terminal_id`, `alias_id`, `name`, `pwd`) VALUES (11, LAST_INSERT_ID(), 1000, '张宁远', md5('staff1@123'));

INSERT INTO `wireless_order_db`.`terminal` (`pin`, `restaurant_id`, `model_id`, `model_name`, `owner_name`) VALUES (2, 11, 0xFF, 'Staff', '李颖宜');
INSERT INTO `wireless_order_db`.`staff` (`restaurant_id`, `terminal_id`, `alias_id`, `name`, `pwd`) VALUES (11, LAST_INSERT_ID(), 1001, '李颖宜', md5('staff2@123'));

COMMIT;
SET AUTOCOMMIT=1;

