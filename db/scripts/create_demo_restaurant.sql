SET NAMES utf8;

-- -----------------------------------------------------
-- Delete all the info before creating the demo	
-- -----------------------------------------------------
DELETE FROM wireless_order_db.order_food_history WHERE restaurant_id=11;
DELETE FROM wireless_order_db.temp_order_food_history WHERE order_id IN (SELECT id FROM wireless_order_db.order_history WHERE restaurant_id=11);
DELETE FROM wireless_order_db.order_history WHERE restaurant_id=11;

DELETE FROM wireless_order_db.order_food WHERE restaurant_id=11;
DELETE FROM wireless_order_db.order WHERE restaurant_id=11;

DELETE FROM wireless_order_db.staff WHERE restaurant_id=11;

DELETE FROM wireless_order_db.food_material WHERE restaurant_id=11;
DELETE FROM wireless_order_db.material_detail WHERE restaurant_id=11;
DELETE FROM wireless_order_db.material WHERE restaurant_id=11;
DELETE FROM wireless_order_db.supplier WHERE restaurant_id=11;
DELETE FROM wireless_order_db.terminal WHERE restaurant_id=11;
DELETE FROM wireless_order_db.table WHERE restaurant_id=11;
DELETE FROM wireless_order_db.region WHERE restaurant_id=11;
DELETE FROM wireless_order_db.food WHERE restaurant_id=11;
DELETE FROM wireless_order_db.taste WHERE restaurant_id=11;
DELETE FROM wireless_order_db.kitchen WHERE restaurant_id=11;
DELETE FROM wireless_order_db.department WHERE restaurant_id=11;

DELETE FROM wireless_order_db.member_charge WHERE member_id IN (SELECT id FROM wireless_order_db.member WHERE restaurant_id=11);
DELETE FROM wireless_order_db.member WHERE restaurant_id=11;

DELETE FROM wireless_order_db.setting WHERE restaurant_id=11;

DELETE FROM wireless_order_db.shift WHERE restaurant_id=11;

DELETE FROM wireless_order_db.restaurant WHERE id=11;

-- -----------------------------------------------------
-- Insert the demo restaruant
-- -----------------------------------------------------
SET AUTOCOMMIT=1;
INSERT INTO `wireless_order_db`.`restaurant` (`id`, `pwd`, `account`, `restaurant_name`, `record_alive`) VALUES ('11', MD5('demo@123'), 'demo', '演示餐厅', '0');
INSERT INTO `wireless_order_db`.`terminal` (`pin`, `restaurant_id`, `model_id`, `model_name`, `owner_name`, `gift_quota`) VALUES (1, 11, 0xFE, 'Admin', '管理员', 300);
INSERT INTO `wireless_order_db`.`setting` (`restaurant_id`) VALUES (11);
-- -----------------------------------------------------
-- Insert test taste perference bound to the demo restaurant
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
-- DELETE FROM wireless_order_db.taste WHERE restaurant_id=11;
INSERT INTO `wireless_order_db`.`taste` (`restaurant_id`, `alias_id`, `preference`, `price`) VALUES (11, 1, '加辣', 2.5);
INSERT INTO `wireless_order_db`.`taste` (`restaurant_id`, `alias_id`, `preference`, `price`) VALUES (11, 2, '少盐', 0);
INSERT INTO `wireless_order_db`.`taste` (`restaurant_id`, `alias_id`, `preference`, `price`) VALUES (11, 3, '少辣', 5.0);
INSERT INTO `wireless_order_db`.`taste` (`restaurant_id`, `alias_id`, `preference`, `price`, `category`, `calc`) VALUES (11, 4, '打包', 5.0, 1, 0);
INSERT INTO `wireless_order_db`.`taste` (`restaurant_id`, `alias_id`, `preference`, `price`, `category`, `calc`) VALUES (11, 5, '免葱', 5.0, 1, 0);
INSERT INTO `wireless_order_db`.`taste` (`restaurant_id`, `alias_id`, `preference`, `rate`, `category`, `calc`) VALUES (11, 6, '例牌', 0, 2, 1); 
INSERT INTO `wireless_order_db`.`taste` (`restaurant_id`, `alias_id`, `preference`, `rate`, `category`, `calc`) VALUES (11, 7, '中牌', 0.2, 2, 1);
INSERT INTO `wireless_order_db`.`taste` (`restaurant_id`, `alias_id`, `preference`, `rate`, `category`, `calc`) VALUES (11, 8, '大牌', 0.5, 2, 1);
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
-- Insert test table regions(bound to demo restaurant)
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
INSERT INTO `wireless_order_db`.`region` (`id`, `restaurant_id`, `alias_id`, `name`) VALUES (NULL, 11, 0, '大厅1楼');
INSERT INTO `wireless_order_db`.`region` (`id`, `restaurant_id`, `alias_id`, `name`) VALUES (NULL, 11, 1, '大厅2楼');
INSERT INTO `wireless_order_db`.`region` (`id`, `restaurant_id`, `alias_id`, `name`) VALUES (NULL, 11, 2, '包房');
INSERT INTO `wireless_order_db`.`region` (`id`, `restaurant_id`, `alias_id`, `name`) VALUES (NULL, 11, 3, 'VIP');
INSERT INTO `wireless_order_db`.`region` (`id`, `restaurant_id`, `alias_id`, `name`) VALUES (NULL, 11, 4, '区域5');
INSERT INTO `wireless_order_db`.`region` (`id`, `restaurant_id`, `alias_id`, `name`) VALUES (NULL, 11, 5, '区域6');
INSERT INTO `wireless_order_db`.`region` (`id`, `restaurant_id`, `alias_id`, `name`) VALUES (NULL, 11, 6, '区域7');
INSERT INTO `wireless_order_db`.`region` (`id`, `restaurant_id`, `alias_id`, `name`) VALUES (NULL, 11, 7, '区域8');
INSERT INTO `wireless_order_db`.`region` (`id`, `restaurant_id`, `alias_id`, `name`) VALUES (NULL, 11, 8, '区域9');
INSERT INTO `wireless_order_db`.`region` (`id`, `restaurant_id`, `alias_id`, `name`) VALUES (NULL, 11, 9, '区域10');
COMMIT;

-- -----------------------------------------------------
-- Insert test tables(bound to demo restaurant, range from 100 to 145, 200 to 205)
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
-- belong to VIP
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`, `name`, `minimum_cost`) VALUES (NULL, 100, 3, 11, '菊花厅', 150);
-- belong to 包房
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`, `name`, `minimum_cost`) VALUES (NULL, 101, 2, 11, '水仙厅', 300.50);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`, `name`, `minimum_cost`) VALUES (NULL, 102, 2, 11, '兰花厅', 500);
-- belong to 大厅1楼
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 103, 0, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 104, 0, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 105, 0, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 106, 0, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 107, 0, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 108, 0, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 109, 0, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 110, 0, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 111, 0, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 112, 0, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 113, 0, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 114, 0, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 115, 0, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 116, 0, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 117, 0, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 118, 0, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 119, 0, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 120, 0, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 121, 0, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 122, 0, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 123, 0, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 124, 0, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 125, 0, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 126, 0, 11);
-- belong to 大厅2楼
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 200, 1, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 201, 1, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 202, 1, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 203, 1, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 204, 1, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 205, 1, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 206, 1, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 207, 1, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 208, 1, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 209, 1, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 210, 1, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 211, 1, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 212, 1, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 213, 1, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 214, 1, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 215, 1, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 216, 1, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 217, 1, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 218, 1, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 219, 1, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 220, 1, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 221, 1, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 222, 1, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 223, 1, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 224, 1, 11);
INSERT INTO `wireless_order_db`.`table` (`id`, `alias_id`, `region`, `restaurant_id`) VALUES (NULL, 225, 1, 11);

COMMIT;

-- -----------------------------------------------------
-- Insert super kitchen records, note that each restaruant has ten super kitchens
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
INSERT INTO `wireless_order_db`.`department` (`restaurant_id`, `dept_id`, `name`) VALUES (11, 0, '仓管部');
INSERT INTO `wireless_order_db`.`department` (`restaurant_id`, `dept_id`, `name`) VALUES (11, 1, '中厨部');
INSERT INTO `wireless_order_db`.`department` (`restaurant_id`, `dept_id`, `name`) VALUES (11, 2, '点心部');
INSERT INTO `wireless_order_db`.`department` (`restaurant_id`, `dept_id`, `name`) VALUES (11, 3, '部门3');
INSERT INTO `wireless_order_db`.`department` (`restaurant_id`, `dept_id`, `name`) VALUES (11, 4, '部门4');
INSERT INTO `wireless_order_db`.`department` (`restaurant_id`, `dept_id`, `name`) VALUES (11, 5, '部门5');
INSERT INTO `wireless_order_db`.`department` (`restaurant_id`, `dept_id`, `name`) VALUES (11, 6, '部门6');
INSERT INTO `wireless_order_db`.`department` (`restaurant_id`, `dept_id`, `name`) VALUES (11, 7, '部门7');
INSERT INTO `wireless_order_db`.`department` (`restaurant_id`, `dept_id`, `name`) VALUES (11, 8, '部门8');
INSERT INTO `wireless_order_db`.`department` (`restaurant_id`, `dept_id`, `name`) VALUES (11, 9, '部门9');
INSERT INTO `wireless_order_db`.`department` (`restaurant_id`, `dept_id`, `name`) VALUES (11, 10, '部门10');

COMMIT;

-- -----------------------------------------------------
-- Insert kitchen records, note that each restaruant has ten kitchens
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
INSERT INTO `wireless_order_db`.`kitchen` (`restaurant_id`, `dept_id`, `alias_id`, `name`, `discount`, `member_discount_1`, `member_discount_2`) VALUES (11, 0, 0, '明档', 0.9, 1.00, 1.00);
INSERT INTO `wireless_order_db`.`kitchen` (`restaurant_id`, `dept_id`, `alias_id`, `name`, `discount`, `member_discount_1`, `member_discount_2`) VALUES (11, 0, 1, '烧味', 0.95, 0.9, 0.9);
INSERT INTO `wireless_order_db`.`kitchen` (`restaurant_id`, `dept_id`, `alias_id`, `name`, `discount`, `member_discount_1`, `member_discount_2`) VALUES (11, 1, 2, '海鲜', 1.00, 1.00, 1.00);
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
-- Insert supplier records
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
INSERT INTO `wireless_order_db`.`supplier` (`supplier_id`, `restaurant_id`, `supplier_alias`, `name`, `tele`, `addr`, `contact`) VALUES (1, 11, 1, '中联肉品', '020-85412369', '广州市番禺区', '陈生');
INSERT INTO `wireless_order_db`.`supplier` (`supplier_id`, `restaurant_id`, `supplier_alias`, `name`, `tele`, `addr`, `contact`) VALUES (2, 11, 2, '联和肉品', '020-84125987', '广州市东埔镇', '李生');
COMMIT;
SET AUTOCOMMIT=1;

-- -----------------------------------------------------
-- Insert material records
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
INSERT INTO `wireless_order_db`.`material` (`restaurant_id`, `supplier_id`, `material_id`, `material_alias`, `name`, `stock`, `price`, `warning_threshold`, `danger_threshold`) VALUES (11, 1, 100, 100, '排骨', 0, 18.5, 10.5, 5.5);
INSERT INTO `wireless_order_db`.`material` (`restaurant_id`, `supplier_id`, `material_id`, `material_alias`, `name`, `stock`, `price`, `warning_threshold`, `danger_threshold`) VALUES (11, 1, 101, 101, '鸡肉', 0, 13, 22, 14);
INSERT INTO `wireless_order_db`.`material` (`restaurant_id`, `supplier_id`, `material_id`, `material_alias`, `name`, `stock`, `price`, `warning_threshold`, `danger_threshold`) VALUES (11, 1, 102, 102, '牛肉', 0, 16, 27, 13);
INSERT INTO `wireless_order_db`.`material` (`restaurant_id`, `supplier_id`, `material_id`, `material_alias`, `name`, `stock`, `price`, `warning_threshold`, `danger_threshold`) VALUES (11, 2, 103, 103, '猪肉', 0, 13.5, 27, 13);
INSERT INTO `wireless_order_db`.`material` (`restaurant_id`, `supplier_id`, `material_id`, `material_alias`, `name`, `stock`, `price`, `warning_threshold`, `danger_threshold`) VALUES (11, 2, 104, 104, '酱汁', 0, 1, 30, 20);
COMMIT;
SET AUTOCOMMIT=1;

-- -----------------------------------------------------
-- Insert material records
-- -----------------------------------------------------
SET AUTOCOMMIT=0;

COMMIT;
SET AUTOCOMMIT=1;

-- -----------------------------------------------------
-- Insert test food menu(bound to demo restaurant, range from 1100 to 1150, 1200 to 1250)
-- Insert consumption between food and material
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
-- DELETE FROM wireless_order_db.food WHERE restaurant_id=11;
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`, `status`) VALUES (1, 0x44C, '京都骨', 23.53, 11, 1, 'jdg', 1);
INSERT INTO `wireless_order_db`.`food_material` (`restaurant_id`, `food_id`, `material_id`, `consumption`) VALUES (11, 0x44C, 100, 1);
INSERT INTO `wireless_order_db`.`food_material` (`restaurant_id`, `food_id`, `material_id`, `consumption`) VALUES (11, 0x44C, 104, 1);
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`, `status`) VALUES (2, 0x44D, '京酱肉丝', 35.3, 11, 0, 'jjrs', 2);
INSERT INTO `wireless_order_db`.`food_material` (`restaurant_id`, `food_id`, `material_id`, `consumption`) VALUES (11, 0x44D, 103, 1);
INSERT INTO `wireless_order_db`.`food_material` (`restaurant_id`, `food_id`, `material_id`, `consumption`) VALUES (11, 0x44D, 104, 1.5);
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`,`status`) VALUES (3, 0x44E, '白切鸡', 21.00, 11, 0, 'bqj', 4);
INSERT INTO `wireless_order_db`.`food_material` (`restaurant_id`, `food_id`, `material_id`, `consumption`) VALUES (11, 0x44E, 101, 1);
INSERT INTO `wireless_order_db`.`food_material` (`restaurant_id`, `food_id`, `material_id`, `consumption`) VALUES (11, 0x44E, 104, 0.5);
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`, `status`) VALUES (4, 0x44F, '盐水菜心', 10.67, 11, 0, 'yscx', 8);
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`, `status`) VALUES (5, 0x450, '湛江鸡', 26, 11, 0, 'zjj', 8);
INSERT INTO `wireless_order_db`.`food_material` (`restaurant_id`, `food_id`, `material_id`, `consumption`) VALUES (11, 0x450, 101, 1);
INSERT INTO `wireless_order_db`.`food_material` (`restaurant_id`, `food_id`, `material_id`, `consumption`) VALUES (11, 0x450, 104, 0.5);
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (6, 0x451, '东波肉', 16, 11, 0, 'dpr');
INSERT INTO `wireless_order_db`.`food_material` (`restaurant_id`, `food_id`, `material_id`, `consumption`) VALUES (11, 0x451, 103, 2);
INSERT INTO `wireless_order_db`.`food_material` (`restaurant_id`, `food_id`, `material_id`, `consumption`) VALUES (11, 0x451, 104, 1.5);
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`, `status`) VALUES (7, 0x452, '红烧排骨', 32, 11, 1, 'hspg', 1);
INSERT INTO `wireless_order_db`.`food_material` (`restaurant_id`, `food_id`, `material_id`, `consumption`) VALUES (11, 0x452, 100, 1);
INSERT INTO `wireless_order_db`.`food_material` (`restaurant_id`, `food_id`, `material_id`, `consumption`) VALUES (11, 0x452, 104, 1.5);
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`, `status`) VALUES (8, 0x453, '清蒸桂花鱼', 29, 11, 0, 'qzghy', 1);
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`, `status`) VALUES (9, 0x454, '鼓油王鹅肠', 24, 11, 0, 'jywec', 3);
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`, `status`) VALUES (10, 0x455, '白灼猪肚', 22.5, 11, 0, 'bzzd', 7);
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (11, 0x456, '水煮鱼', 39, 11, 0, 'szy');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (12, 0x457, '酸菜鱼', 34, 11, 0, 'scy');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (13, 0x458, '鱼香肉丝', 16, 11, 0, 'yxrs');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (14, 0x459, '白爆鱼丁', 18, 11, 0, 'bbyd');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (15, 0x45A, '白菜粉丝', 15, 11, 0, 'bcfs');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (16, 0x45B, '白饭', 1.5, 11, 0, 'bf');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (17, 0x45C, '鲍鱼龙须', 23.5, 11, 0, 'bylx');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (18, 0x45D, '北菰炒面', 13, 11, 0, 'bgcm');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (19, 0x45E, '北菰烩鸭丝', 19, 11, 0, 'bghys');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (20, 0x45F, '北京烤鸭', 41, 11, 0, 'bjky');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (21, 0x460, '北京泡菜', 6, 11, 0, 'bjpc');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (22, 0x461, '比利时烩鸡', 69, 11, 0, 'blshj');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (23, 0x462, '碧绿生鱼球', 35, 11, 0, 'blsyq');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (24, 0x463, '冰冻荔枝', 23, 11, 0, 'bdlz');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (25, 0x4B0, '冰淇淋', 8, 11, 0, 'bql');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (26, 0x4B1, '冰肉莲蓉粽', 6.5, 11, 0, 'brlrz');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (27, 0x4B2, '菠菜炒鸡蛋', 11, 11, 0, 'bccjd');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (28, 0x4B3, '腊肠', 8.5, 11, 0, 'lc');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (29, 0x4B4, '腊金银润', 10, 11, 0, 'ljyr');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (30, 0x4B5, '辣椒肉丝', 10.5, 11, 0, 'ljrs');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (31, 0x4B6, '辣蔬菜', 11.3, 11, 0, 'lsc');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (32, 0x4B7, '辣汁鱼头', 22, 11, 0, 'lzyt');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (33, 0x4B8, '辣子炒鸡丁', 16.5, 11, 0, 'lzcjd');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (34, 0x4B9, '辣子炒肉丁', 17, 11, 0, 'lzcrd');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (35, 0x4BA, '辣子肉丁', 16, 11, 0, 'lzrd');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (36, 0x4BB, '粒驻鲍鱼', 79, 11, 2, 'lzby');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (37, 0x4BC, '烂鸡鱼翅', 125, 11, 2, 'ljyc');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (38, 0x4BD, '醪糟百子果羹', 36, 11, 0, 'ncbzgj');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (39, 0x4BE, '老鸡蛋托黑鱼子', 35, 11, 0, 'ljdthyz');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (40, 0x4BF, '烙饼', 6, 11, 0, 'lb');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (41, 0x4C0, '冷茶肠', 11.5, 11, 0, 'lcc');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (42, 0x4C1, '冷醋鱼', 13.5, 11, 0, 'lky');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (43, 0x4C2, '冷火腿蔬菜', 7, 11, 0, 'lhtsc');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (44, 0x4C3, '冷鸡冻', 23, 11, 0, 'ljd');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (45, 0x4C4, '冷烤鸭', 16, 11, 0, 'lky');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (46, 0x4C5, '冷辣白菜', 10, 11, 0, 'llbc');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (47, 0x4C6, '冷牛舌', 14, 11, 0, 'lns');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (48, 0x4C7, '冷阉鸡', 16, 11, 0, 'lyj');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (49, 0x4C8, '荔脯芋香角', 16.5, 11, 0, 'lpyxj');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (50, 0x4C9, '栗子鸡', 21, 11, 0, 'lzj');
INSERT INTO `wireless_order_db`.`food` (`id`, `alias_id`, `name`, `unit_price`, `restaurant_id`, `kitchen`, `pinyin`) VALUES (51, 0x4CA, '莲煎软饼', 3.5, 11, 0, 'ljrb');
COMMIT;
SET AUTOCOMMIT=1;
-- SELECT * FROM food;

-- -----------------------------------------------------
-- Insert order and the associated order_food history records
-- -----------------------------------------------------
SET AUTOCOMMIT=0;
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `table_name`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`, `member_id`, `member`) VALUES (1, 11, 0x64, '菊花厅', 0x20237AB8, 20110102, 214, 214, 5, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), '13694260535', '熊至明');
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`, `kitchen`) VALUES (11, 1, 0x44C, 1.2, '京都骨', 23.53, NOW(), '张宁远', NULL, 1);

-- the order detail to 京酱肉丝
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 1, 0x44D, 2, '京酱肉丝', 35.3, 20110411180911, '张宁远', NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 1, 0x44D, 2, '京酱肉丝', 35.3, 20110411191523, '张宁远', NULL);

-- the order detail to 白切鸡
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 1, 0x44E, 1, '白切鸡', 21.00, 20110411171200, '张宁远', NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 1, 0x44E, -1, '白切鸡', 21.00, 20110411174521, '黄家声', '客人退菜');

-- the order detail to 湛江鸡
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`, `discount`) VALUES (11, 1, 0x450, 6, '湛江鸡', 26, 20110412170931, '黄家声', NULL, 0.98);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`, `discount`) VALUES (11, 1, 0x450, -1, '湛江鸡', 26, 20110412170931, '黄家声', '客人退菜', 0.98);

-- the order detail to 盐水菜心
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`, `discount`) VALUES (11, 1, 0x44F, 2, '盐水菜心', 10.67, 20110412181511, '黄家声', NULL, 0.75);


INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `table_name`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`, `member_id`, `member`, `type`, `comment`) VALUES (2, 11, 0x65, '水仙厅', 0x20237AB8, 20110203, 543, 540, 6, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), '13632654789', '刘天宁', 3, '会员卡结帐');
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 2, 0x451, 2.5, '东波肉', 16, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 2, 0x452, 2.2, '红烧排骨', 32, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 2, 0x453, 3.0, '清蒸桂花鱼', 29, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 2, 0x454, 2.6, '鼓油王鹅肠', 24, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 2, 0x455, 5, '白灼猪肚', 22.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `table_name`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`, `type`) VALUES (3, 11, 0x66, '兰花厅', 0x20237AB8, 20110323, 231, 230, 10, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), 3);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 3, 0x456, 2, '水煮鱼', 39, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 3, 0x457, 4, '酸菜鱼', 34, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 3, 0x458, 1, '鱼香肉丝', 16, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 3, 0x459, 1, '白爆鱼丁', 18, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 3, 0x45A, 1, '白菜粉丝', 15, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`,  `custom_num`, `waiter`, `type`) VALUES (4, 11, 0x67, 0x20237AB8, 20110103, 564, 560, 5, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), 2);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 4, 0x45B, 1, '白饭', 1.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 4, 0x45C, 1, '鲍鱼龙须', 23.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 4, 0x45D, 1, '北菰炒面', 13, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 4, 0x45E, 1, '北菰烩鸭丝', 19, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 4, 0x45F, 1, '北京烤鸭', 41, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`, `type`) VALUES (5, 11, 0x68, 0x20237AB8, 20110403, 1297, 1290, 7, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), 2);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 5, 0x460, 1, '北京泡菜', 6, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 5, 0x461, 1, '比利时烩鸡', 69, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 5, 0x462, 1, '碧绿生鱼球', 35, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 5, 0x463, 1, '冰冻荔枝', 23, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 5, 0x4B0, 1, '冰淇淋', 8, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`, `type`, `comment`) VALUES (6, 11, 0x69, 0x20237AB8, 20110509, 213, 210, 8, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), 4, '挂账埋单');
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 6, 0x4B0, 1, '冰淇淋', 8, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 6, 0x4B1, 1, '冰肉莲蓉粽', 6.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 6, 0x4B2, 1, '菠菜炒鸡蛋', 11, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 6, 0x4B3, 1, '腊肠', 8.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 6, 0x4B4, 1, '腊金银润', 10, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`, `type`, `comment`) VALUES (7, 11, 0x70, 0x20237AB8, 20110421, 121, 120, 8, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), 4, '挂账埋单');
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 7, 0x4B5, 1, '辣椒肉丝', 10.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 7, 0x4B6, 1, '辣蔬菜', 11.3, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 7, 0x4B7, 1, '辣汁鱼头', 22, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 7, 0x4B8, 1, '辣子炒鸡丁', 16.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 7, 0x4B9, 1, '辣子炒肉丁', 17, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`, `type`) VALUES (8, 11, 0x69, 0x20237AB8, 20110529, 123.1, 120, 8, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), 5);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 8, 0x4B0, 1, '冰淇淋', 8, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 8, 0x4B1, 1, '冰肉莲蓉粽', 6.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 8, 0x4B2, 1, '菠菜炒鸡蛋', 11, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 8, 0x4B3, 1, '腊肠', 8.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 8, 0x4B4, 1, '腊金银润', 10, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`, `type`) VALUES (9, 11, 0x6A, 0x20237AB8, 20110502, 123, 120, 2, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), 5);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 9, 0x4B5, 1, '辣椒肉丝', 10.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 9, 0x4B6, 1, '辣蔬菜', 11.3, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 9, 0x4B7, 1, '辣汁鱼头', 22, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 9, 0x4B8, 1, '辣子炒鸡丁', 16.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 9, 0x4B9, 1, '辣子炒肉丁', 17, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`, `type`,  `category`, `comment`) VALUES (10, 11, 0x6B, 0x20237AB8, 20110223, 432, 430, 3, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), 1, 2, '中大外卖');
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 10, 0x4BA, 1, '辣子肉丁', 16, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 10, 0x4BB, 1, '粒驻鲍鱼', 79, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 10, 0x4BC, 1, '烂鸡鱼翅', 125, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 10, 0x4BD, 1, '醪糟百子果羹', 36, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 10, 0x4BE, 1, '老鸡蛋托黑鱼子', 35, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`, `type`, `category`, `comment`) VALUES (11, 11, 0x6C, 0x20237AB8, NOW(), 221, 220, 3, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), 1, 2, '车陂外卖');
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 11, 0x4BF, 1.3, '烙饼', 6, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 11, 0x4C0, 1, '冷茶肠', 4.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 11, 0x4C1, 1, '冷醋鱼', 13.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 11, 0x4C2, 1, '冷火腿蔬菜', 7, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 11, 0x4C3, 1, '冷鸡冻', 23, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`, `type`) VALUES (12, 11, 0x6D, 0x20237AB8, NOW(), 221, 220, 8, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), 2);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 12, 0x4B0, 1, '冰淇淋', 8, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 12, 0x4B1, 2.5, '冰肉莲蓉粽', 6.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 12, 0x4B2, 1, '菠菜炒鸡蛋', 11, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 12, 0x4B3, 1, '腊肠', 8.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 12, 0x4B4, 1, '腊金银润', 10, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`) VALUES (13, 11, 0x6E, 0x20237AB8, NOW(), 343, 340, 2, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A));
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 13, 0x4B5, 1, '辣椒肉丝', 10.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 13, 0x4B6, 1, '辣蔬菜', 11.3, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 13, 0x457, 1, '酸菜鱼', 34, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 13, 0x4B8, 1, '辣子炒鸡丁', 16.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 13, 0x4B9, 1, '辣子炒肉丁', 17, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`) VALUES (14, 11, 0x6F, 0x20237AB8, NOW(), 123, 120, 3, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A));
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 14, 0x4BA, 1, '辣子肉丁', 16, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 14, 0x4BB, 1, '粒驻鲍鱼', 79, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 14, 0x45C, 0.5, '鲍鱼龙须', 23.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 14, 0x4BD, 2, '醪糟百子果羹', 36, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 14, 0x4BE, 1, '老鸡蛋托黑鱼子', 35, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`) VALUES (15, 11, 0x70, 0x20237AB8, NOW(), 212, 210, 3, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A));
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 15, 0x4BF, 1, '烙饼', 6, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 15, 0x450, 3, '湛江鸡', 26, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 15, 0x4C1, 1, '冷醋鱼', 13.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 15, 0x4C2, 1, '冷火腿蔬菜', 7, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 15, 0x4C3, 1, '冷鸡冻', 23, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`) VALUES (16, 11, 0x71, 0x20237AB8, NOW(), 311, 310,  3, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A));
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 16, 0x4BA, 1, '辣子肉丁', 16, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 16, 0x4BB, 1, '粒驻鲍鱼', 79, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 16, 0x4BC, 1, '烂鸡鱼翅', 125, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 16, 0x4BD, 1, '醪糟百子果羹', 36, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 16, 0x4BE, 1, '老鸡蛋托黑鱼子', 35, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`) VALUES (17, 11, 0x72, 0x20237AB8, NOW(), 333, 330, 3, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A));
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 17, 0x4BF, 1.3, '烙饼', 6, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 17, 0x4B0, 1, '冰淇淋', 8, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 17, 0x4B1, 1, '冰肉莲蓉粽', 6.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 17, 0x4B2, 1, '菠菜炒鸡蛋', 11, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 17, 0x4B3, 1, '腊肠', 8.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`) VALUES (18, 11, 0x73, 0x20237AB8, NOW(), 421, 420, 8, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A));
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 18, 0x4C0, 1, '冷茶肠', 4.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 18, 0x4B1, 2.5, '冰肉莲蓉粽', 6.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 18, 0x452, 1, '红烧排骨', 32, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 18, 0x4C3, 3, '冷鸡冻', 23, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 18, 0x4B4, 1, '腊金银润', 10, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`) VALUES (19, 11, 0x74, 0x20237AB8, NOW(), 111, 110, 2, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A));
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 19, 0x4B5, 1, '辣椒肉丝', 10.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 19, 0x4C6, 2, '冷牛舌', 14, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 19, 0x457, 1, '酸菜鱼', 34, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 19, 0x458, 3, '鱼香肉丝', 16, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 19, 0x4B9, 1, '辣子炒肉丁', 17, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`) VALUES (20, 11, 0x75, 0x20237AB8, NOW(), 302.25, 300, 3, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A));
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 20, 0x4CA, 1, '莲煎软饼', 3.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 20, 0x4BB, 2, '粒驻鲍鱼', 79, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 20, 0x45C, 0.5, '鲍鱼龙须', 23.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 20, 0x4BD, 2, '醪糟百子果羹', 36, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 20, 0x45E, 3, '北菰烩鸭丝', 19, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_history` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`) VALUES (21, 11, 0x76, 0x20237AB8, NOW(), 216, 210, 3, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A));
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 21, 0x45F, 1, '北京烤鸭', 41, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 21, 0x450, 3, '湛江鸡', 26, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 21, 0x4C5, 1, '冷辣白菜', 10, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 21, 0x452, 2, '红烧排骨', 32, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food_history` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 21, 0x4C3, 1, '冷鸡冻', 23, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
COMMIT;
SET AUTOCOMMIT=1;

-- -----------------------------------------------------
-- Insert the info from order_food_history to temp_order_food_history
-- -----------------------------------------------------
INSERT INTO `wireless_order_db`.temp_order_food_history(order_id,food_id,taste_id,taste_id2,taste_id3,is_temporary,`name`,taste,order_count,unit_price,taste_price,discount,food_status,kitchen,waiter) 
SELECT 
`wireless_order_db`.`order_food_history`.`order_id` AS `order_id`,
`wireless_order_db`.`order_food_history`.`food_id` AS `food_id`,
`wireless_order_db`.`order_food_history`.`taste_id` AS `taste_id`,
`wireless_order_db`.`order_food_history`.`taste_id2` AS `taste_id2`,
`wireless_order_db`.`order_food_history`.`taste_id3` AS `taste_id3`,
`wireless_order_db`.`order_food_history`.`is_temporary` AS `is_temporary`,
`wireless_order_db`.`order_food_history`.`name` AS `name`,
`wireless_order_db`.`order_food_history`.`taste` AS `taste`,
sum(`wireless_order_db`.`order_food_history`.`order_count`) AS `order_count`,
max(`wireless_order_db`.`order_food_history`.`unit_price`) AS `unit_price`,
max(`wireless_order_db`.`order_food_history`.`taste_price`) AS `taste_price`,
max(`wireless_order_db`.`order_food_history`.`discount`) AS `discount`,
max(`wireless_order_db`.`order_food_history`.`food_status`) AS `food_status`,
max(`wireless_order_db`.`order_food_history`.`kitchen`) AS `kitchen`,
max(`wireless_order_db`.`order_food_history`.`waiter`) AS `waiter`
FROM `wireless_order_db`.`order_food_history` 
GROUP BY 
`wireless_order_db`.`order_food_history`.`order_id`,
`wireless_order_db`.`order_food_history`.`food_id`,
`wireless_order_db`.`order_food_history`.`taste_id`,
`wireless_order_db`.`order_food_history`.`taste_id2`,
`wireless_order_db`.`order_food_history`.`taste_id3`,
`wireless_order_db`.`order_food_history`.`is_temporary`
HAVING (SUM(`wireless_order_db`.`order_food_history`.`order_count`) > 0);

-- -----------------------------------------------------
-- Insert order and the associated order_food records
-- -----------------------------------------------------
SET AUTOCOMMIT=0;

INSERT INTO `wireless_order_db`.`order` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`, `member_id`, `member`) VALUES (40, 11, 0x64, 0x20237AB8, NOW(), 214, 210, 5, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), '13694260535', '熊至明');
INSERT INTO `wireless_order_db`.`order_food` (`id`, `restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (400, 11, 40, 0x44C, 1.2, '京都骨', 23.53, NOW(), '张宁远', NULL);

-- the order detail to 京酱肉丝
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 40, 0x44D, 2, '京酱肉丝', 35.3, 20110411180911, '张宁远', NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 40, 0x44D, 1, '京酱肉丝', 35.3, 20110411191523, '张宁远', NULL);

-- the order detail to 白切鸡
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 40, 0x44E, 1, '白切鸡', 21.00, 20110411171200, '张宁远', NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 40, 0x44E, -1, '白切鸡', 21.00, 20110411174521, '黄家声', '客人退菜');

-- the order detail to 盐水菜心
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 40, 0x44F, 2, '盐水菜心', 10.67, 20110412181511, '黄家声', NULL);

-- the order detail to 湛江鸡
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 40, 0x450, 5, '湛江鸡', 26, 20110412170931, '黄家声', NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 40, 0x450, -5, '湛江鸡', 26, 20110412170931, '黄家声', '客人退菜');

INSERT INTO `wireless_order_db`.`order` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`, `type`, `comment`) VALUES (41, 11, 0x65, 0x20237AB8, NOW(), 325.3, 320, 6, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), 2, '刷卡结帐');
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 41, 0x451, 2.5, '东波肉', 16, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 41, 0x452, 2.2, '红烧排骨', 32, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 41, 0x453, 3.0, '清蒸桂花鱼', 29, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 41, 0x454, 2.6, '鼓油王鹅肠', 24, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 41, 0x455, 5, '白灼猪肚', 22.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `total_price`, `total_price_2`, `custom_num`, `waiter`, `type`) VALUES (42, 11, 0x66, 0x20237AB8, NOW(), 213.56, 210, 10, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), 3);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 42, 0x456, 2, '水煮鱼', 39, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 42, 0x457, 4, '酸菜鱼', 34, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 42, 0x458, 1, '鱼香肉丝', 16, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 42, 0x459, 1, '白爆鱼丁', 18, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 42, 0x45A, 1, '白菜粉丝', 15, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `custom_num`, `waiter`) VALUES (43, 11, 0x67, 0x20237AB8, NOW(), 5, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A));
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 43, 0x45B, 1, '白饭', 1.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 43, 0x45C, 1, '鲍鱼龙须', 23.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 43, 0x45D, 1, '北菰炒面', 13, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 43, 0x45E, 1, '北菰烩鸭丝', 19, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 43, 0x45F, 1, '北京烤鸭', 41, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`, `custom_num`, `waiter`) VALUES (44, 11, 0x68, 0x20237AB8, NOW(), 7, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A));
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 44, 0x460, 1, '北京泡菜', 6, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 44, 0x461, 1, '比利时烩鸡', 69, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 44, 0x462, 1, '碧绿生鱼球', 35, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 44, 0x463, 1, '冰冻荔枝', 23, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 44, 0x4B0, 1, '冰淇淋', 8, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`,  `custom_num`, `waiter`) VALUES (45, 11, 0x6D, 0x20237AB8, NOW(),  8, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A));
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 45, 0x4B0, 1, '冰淇淋', 8, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 45, 0x4B1, 2.5, '冰肉莲蓉粽', 6.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 45, 0x4B2, 1, '菠菜炒鸡蛋', 11, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 45, 0x4B3, 1, '腊肠', 8.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 45, 0x4B4, 1, '腊金银润', 10, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`,  `custom_num`, `waiter`) VALUES (46, 11, 0x6E, 0x20237AB8, NOW(),  2, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A));
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 46, 0x4B5, 1, '辣椒肉丝', 10.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 46, 0x4B6, 1, '辣蔬菜', 11.3, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 46, 0x457, 1, '酸菜鱼', 34, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 46, 0x4B8, 1, '辣子炒鸡丁', 16.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 46, 0x4B9, 1, '辣子炒肉丁', 17, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
-- the record for 拼台(111 + 202)
INSERT INTO `wireless_order_db`.`order` (`id`, `restaurant_id`, `table_id`, `terminal_pin`, `order_date`,  `custom_num`, `waiter`, `table2_id`, `table2_name`, `category`) VALUES (47, 11, 0x6F, 0x20237AB8, NOW(),  3, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), 202, '', 4);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 47, 0x4BA, 1, '辣子肉丁', 16, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 47, 0x4BB, 1, '粒驻鲍鱼', 79, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 47, 0x45C, 0.5, '鲍鱼龙须', 23.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 47, 0x4BD, 2, '醪糟百子果羹', 36, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 47, 0x4BE, 1, '老鸡蛋托黑鱼子', 35, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
-- the record for 外卖
INSERT INTO `wireless_order_db`.`order` (`id`, `restaurant_id`, `category`, `table_id`, `terminal_pin`, `order_date`,  `custom_num`, `waiter`) VALUES (48, 11, 2, 205, 0x20237AB8, NOW(),  2, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A));
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 48, 0x4B5, 1, '辣椒肉丝', 10.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 48, 0x4B6, 1, '辣蔬菜', 11.3, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 48, 0x457, 1, '酸菜鱼', 34, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
-- the record for 并台(并100)
INSERT INTO `wireless_order_db`.`order` (`id`, `restaurant_id`, `category`, `table_id`, `table_name`, `terminal_pin`, `order_date`,  `custom_num`, `waiter`) VALUES (49, 11, 3, 206, '并100', 0x20237AB8, NOW(),  8, (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A));
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 49, 0x4B0, 1, '冰淇淋', 8, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 49, 0x4B1, 2.5, '冰肉莲蓉粽', 6.5, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);
INSERT INTO `wireless_order_db`.`order_food` (`restaurant_id`, `order_id`, `food_id`, `order_count`, `name`,`unit_price`, `order_date`, `waiter`, `comment`) VALUES (11, 49, 0x4B2, 1, '菠菜炒鸡蛋', 11, NOW(), (SELECT owner_name FROM wireless_order_db.terminal WHERE pin=0x2100000A), NULL);

COMMIT;
SET AUTOCOMMIT=1;

-- -----------------------------------------------------
-- Insert staff and associated terminal info
-- -----------------------------------------------------
SET AUTOCOMMIT=0;

INSERT INTO `wireless_order_db`.`terminal` (`pin`, `restaurant_id`, `model_id`, `model_name`, `owner_name`) VALUES (1, 11, 0xFF, 'Staff', '张宁远');
INSERT INTO `wireless_order_db`.`staff` (`restaurant_id`, `terminal_id`, `alias_id`, `name`, `pwd`) VALUES (11, LAST_INSERT_ID(), 1000, '张宁远', md5('1'));

INSERT INTO `wireless_order_db`.`terminal` (`pin`, `restaurant_id`, `model_id`, `model_name`, `owner_name`, `gift_quota`) VALUES (2, 11, 0xFF, 'Staff', '李颖宜', 100);
INSERT INTO `wireless_order_db`.`staff` (`restaurant_id`, `terminal_id`, `alias_id`, `name`, `pwd`) VALUES (11, LAST_INSERT_ID(), 1001, '李颖宜', md5('2'));

COMMIT;
SET AUTOCOMMIT=1;

-- -----------------------------------------------------
-- Insert shift record
-- -----------------------------------------------------
SET AUTOCOMMIT=0;

INSERT INTO `wireless_order_db`.`shift` (`restaurant_id`, `name`, `on_duty`, `off_duty`) VALUES (11, '李颖宜', 20110501000000, NOW());

COMMIT;
SET AUTOCOMMIT=1;

