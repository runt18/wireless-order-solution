package com.wireless.db.menuMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.inventoryMgr.MaterialDao;
import com.wireless.db.tasteRef.TasteRefDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.FoodError;
import com.wireless.exception.MaterialError;
import com.wireless.exception.PlanError;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.inventoryMgr.MaterialCate;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.FoodStatistics;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.ppMgr.PricePlan;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.util.PinyinUtil;
import com.wireless.util.SQLUtil;

public class FoodDao {
	
	/**
	 * 
	 * @param dbCon
	 * @param term
	 * @param fb
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static int insertFoodBaisc(DBCon dbCon, Staff term, Food fb) throws BusinessException, SQLException{
		int count = 0;
		String insertSQL = "", querySQL = "";
		// 检查菜品是否存在
		querySQL = "SELECT count(food_alias) count FROM " + Params.dbName + ".food WHERE restaurant_id = " + fb.getRestaurantId() + " AND food_alias = " + fb.getAliasId();
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		while(dbCon.rs != null && dbCon.rs.next()){
			count = dbCon.rs.getInt("count");
			break;
		}
		if(count > 0){
			throw new BusinessException("操作失败,该编号菜品已经存在!");
		}
		// 新增菜品信息
		insertSQL = "INSERT INTO " + Params.dbName + ".food" 
				+ " ( food_alias, name, pinyin, restaurant_id, kitchen_id, kitchen_alias, status, taste_ref_type, food.desc, food.stock_status ) "
				+ "values("
				+ fb.getAliasId() + ", " 
				+ "'" + fb.getName() + "', " 
				+ "'" + fb.getPinyin() + "', " 
				+ fb.getRestaurantId() + ", " 
				+ fb.getKitchen().getId() + ", " 
				+ fb.getKitchen().getAliasId() + ", " 
				+ fb.getStatus() + ", " 
				+ Food.TasteRef.SMART.getVal() + ", "
				+ "'" + fb.getDesc() + "', "
				+ fb.getStockStatus().getVal()
				+ ")";
		count = dbCon.stmt.executeUpdate(insertSQL);
		// 获取新增菜品数据编号
		dbCon.rs = dbCon.stmt.executeQuery(SQLUtil.SQL_QUERY_LAST_INSERT_ID);
		if(dbCon.rs != null && dbCon.rs.next()){
			fb.setFoodId(dbCon.rs.getInt(1));
			dbCon.rs = null;
		}
		
		// 新增菜谱价格方案信息
		insertSQL = "INSERT INTO food_price_plan (restaurant_id, food_id, price_plan_id, unit_price)"
				  + " SELECT " + fb.getRestaurantId() + "," + fb.getFoodId() + ",price_plan_id," + fb.getPrice() + " FROM price_plan WHERE restaurant_id = " + fb.getRestaurantId();
		count = dbCon.stmt.executeUpdate(insertSQL);
		if(count == 0){
			throw new BusinessException(PlanError.PRICE_FOOD_INSERT);
		}
		
		// 处理库存
		if(fb.getStockStatus() == Food.StockStatus.NONE){
			// 无需处理
		}else if(fb.getStockStatus() == Food.StockStatus.GOOD){
			MaterialDao.insertGood(dbCon, term, (int)fb.getFoodId(), fb.getName());
		}else if(fb.getStockStatus() == Food.StockStatus.MATERIAL){
			// 无需处理
		}
		return count;
	}
	
	/**
	 * 
	 * @param term
	 * @param fb
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static void insertFoodBaisc(Staff term, Food fb) throws BusinessException, SQLException{		
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int count = insertFoodBaisc(dbCon, term, fb);
			dbCon.conn.commit();
			if(count == 0){
				throw new BusinessException(FoodError.INSERT_FAIL);
			}
		} catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;
		} catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		} finally {
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param term
	 * @param fb
	 * @param content
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static void insertFoodBaisc(Staff term, Food fb, String content) throws BusinessException, SQLException{
		FoodDao.insertFoodBaisc(term, fb);
		try{
			TasteRefDao.execByFood(FoodDao.getFoodById(term, fb.getFoodId()));
			FoodCombinationDao.updateFoodCombination(fb.getFoodId(), fb.getRestaurantId(), fb.getStatus(), content);
		} catch(Exception e){
			throw new BusinessException(FoodError.COMBO_UPDATE_FAIL);
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param fb
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static int updateFoodBaisc(DBCon dbCon, Staff term, Food fb) throws BusinessException, SQLException{
		Food old = MenuDao.getFoodById(dbCon, fb.getFoodId());
		int count = 0;
		String updateSQL = "", deleteSQL = "";
		// 修改当前活动价格方案信息 
		updateSQL = " UPDATE " + Params.dbName + ".food_price_plan SET " +
					" unit_price = " + fb.getPrice()  + 
					" ,commission = " + fb.getCommission() +
					" WHERE food_id = " + fb.getFoodId() +
					" AND price_plan_id = (SELECT price_plan_id FROM " + Params.dbName + ".price_plan WHERE restaurant_id = " + fb.getRestaurantId() + " AND status = " + PricePlan.Status.ACTIVITY.getVal() + ")";
		count = dbCon.stmt.executeUpdate(updateSQL);
		if(count == 0){
			throw new BusinessException(FoodError.UPDATE_PRICE_FAIL);
		}
		// 修改菜品基础信息
		updateSQL = "UPDATE " + Params.dbName + ".food "
				  + " SET name = '" + fb.getName() + "', "
				  + " pinyin = '"+ fb.getPinyin() + "', "
				  + " kitchen_id =  " + (fb.getKitchen().getId() < 0 ? null : fb.getKitchen().getId()) + ", " 
				  + " kitchen_alias = " + fb.getKitchen().getAliasId() + ", "
				  + " status =  " + fb.getStatus() + ", "
				  + " food.desc = " + (fb.getDesc() == null || fb.getDesc().trim().length() == 0 ? null : "'" + fb.getDesc() + "'") + ", "
				  + " food.stock_status = " + fb.getStockStatus().getVal()
				  +" WHERE restaurant_id=" + fb.getRestaurantId() + " and food_id = " + fb.getFoodId();
		
		count = dbCon.stmt.executeUpdate(updateSQL);
		if(count == 0){
			throw new BusinessException(FoodError.UPDATE_FAIL);
		}
		
		// 处理库存资料
		if(fb.getStockStatus() == Food.StockStatus.NONE){
			if(old.getStockStatus() == Food.StockStatus.NONE){
				// 无需处理
			}else if(old.getStockStatus() == Food.StockStatus.GOOD){
				// 删除商品出库关系
				String sql = "SELECT material_id FROM food_material WHERE food_id = " + fb.getFoodId();
				dbCon.rs = dbCon.stmt.executeQuery(sql);
				if(dbCon.rs.next()){
					int materialId = dbCon.rs.getInt("material_id");
					dbCon.rs.close();
					MaterialDao.delete(dbCon, materialId);
				}
				
				deleteSQL = "DELETE FROM food_material"
						  + " WHERE food_id = " + fb.getFoodId()
						  + " AND material_id NOT IN (SELECT material_id FROM material T1, material_cate T2 WHERE T1.cate_id = T2.cate_id AND T2.type = " + MaterialCate.Type.MATERIAL.getValue() + ")";
				dbCon.stmt.executeUpdate(deleteSQL);
				
				

			}else if(old.getStockStatus() == Food.StockStatus.MATERIAL){
				// 删除原料出库关系
				deleteSQL = "DELETE FROM food_material"
						  + " WHERE food_id = " + fb.getFoodId()
						  + " AND material_id NOT IN (SELECT material_id FROM material T1, material_cate T2 WHERE T1.cate_id = T2.cate_id AND T2.type = " + MaterialCate.Type.GOOD.getValue() + ")";
				dbCon.stmt.executeUpdate(deleteSQL);
			}
		}else if(fb.getStockStatus() == Food.StockStatus.GOOD){
			if(old.getStockStatus() == Food.StockStatus.NONE){
				// 添加新商品库存资料
				try{
					MaterialDao.insertGood(dbCon, term, (int)fb.getFoodId(), fb.getName());					
				}catch(BusinessException e){
					if(e.getErrCode() != MaterialError.GOOD_INSERT_FAIL){
						throw e;
					}
				}
			}else if(old.getStockStatus() == Food.StockStatus.GOOD){
				// 无需处理
			}else if(old.getStockStatus() == Food.StockStatus.MATERIAL){
				// 删除原料出库关系
				deleteSQL = "DELETE FROM food_material"
						  + " WHERE food_id = " + fb.getFoodId()
						  + " AND material_id NOT IN (SELECT material_id FROM material T1, material_cate T2 WHERE T1.cate_id = T2.cate_id AND T2.type = " + MaterialCate.Type.GOOD.getValue() + ")";
				dbCon.stmt.executeUpdate(deleteSQL);
				// 添加新商品库存资料
				try{
					MaterialDao.insertGood(dbCon, term, (int)fb.getFoodId(), fb.getName());					
				}catch(BusinessException e){
					if(e.getErrCode() != MaterialError.GOOD_INSERT_FAIL){
						throw e;
					}
				}
			}
		}else if(fb.getStockStatus() == Food.StockStatus.MATERIAL){
			if(old.getStockStatus() == Food.StockStatus.NONE){
				// 无需处理
			}else if(old.getStockStatus() == Food.StockStatus.GOOD){
				// 无需处理
			}else if(old.getStockStatus() == Food.StockStatus.MATERIAL){
				// 无需处理
			}
		}
		
		return count;
	}
	
	/**
	 * 
	 * @param term
	 * @param fb
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static void updateFoodBaisc(Staff term, Food fb) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int count = FoodDao.updateFoodBaisc(dbCon, term, fb);
			dbCon.conn.commit();
			if(count == 0){
				throw new BusinessException(FoodError.UPDATE_FAIL);
			}
		} catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;
		} catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		} finally {
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param fb
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static int deleteFood(DBCon dbCon, Food fb) throws BusinessException, SQLException{
		int count = 0;
		String querySQL = "", deleteSQL = "", tableIDList = "";
		
		// 验证删除菜品是否正在营业使用过程中
		querySQL = "SELECT A.id, A.table_id, A.table_alias, A.table_name, B.food_id, SUM(B.order_count) order_count "
				 + " FROM " + Params.dbName + ".order A, " + Params.dbName + ".order_food B"
				 + " WHERE A.id = B.order_id AND A.status = " + Order.Status.UNPAID.getVal() + " AND A.restaurant_id = " + fb.getRestaurantId()
				 + " GROUP BY B.order_id, B.food_id "
				 + " HAVING B.food_id = " + fb.getFoodId()
				 + " ORDER BY A.table_alias ";
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		int index = 0;
		while(dbCon.rs != null && dbCon.rs.next()){
			if(index >= 0 && index < 5){
				tableIDList += ((index > 0 ? "," : "") + dbCon.rs.getInt("table_alias"));
			}else{
				tableIDList += ".";
			}
			index++;
		}
		if(!tableIDList.trim().isEmpty()){
			throw new BusinessException(FoodError.DELETE_FAIL_IS_USED);
		}
		
		// delete food
		deleteSQL = "DELETE FROM " + Params.dbName + ".food WHERE food_id = " + fb.getFoodId() + " and restaurant_id = " + fb.getRestaurantId();
		count = dbCon.stmt.executeUpdate(deleteSQL);
		if(count == 0)
			throw new BusinessException(FoodError.DELETE_FAIL);
		
		// delete foodTaste
		deleteSQL = "DELETE FROM " + Params.dbName + ".food_taste_rank WHERE food_id = " + fb.getFoodId() + " and restaurant_id = " + fb.getRestaurantId();
		dbCon.stmt.executeUpdate(deleteSQL);
		deleteSQL = "DELETE FROM " + Params.dbName + ".food_taste WHERE food_id = " + fb.getFoodId() + " and restaurant_id = " + fb.getRestaurantId();
		dbCon.stmt.executeUpdate(deleteSQL);
		
		// delete foodCombination
		deleteSQL = "DELETE FROM " + Params.dbName + ".combo WHERE food_id = " + fb.getFoodId() + " and restaurant_id = " + fb.getRestaurantId();
		dbCon.stmt.executeUpdate(deleteSQL);
		
		// delete foodPricePlan
		deleteSQL = "DELETE FROM " + Params.dbName + ".food_price_plan WHERE food_id = " + fb.getFoodId() + " and restaurant_id = " + fb.getRestaurantId();
		count = dbCon.stmt.executeUpdate(deleteSQL);
		if(count == 0)
			throw new BusinessException(PlanError.PRICE_FOOD_DELETE);
		
		//delete material
		deleteSQL = "DELETE FROM material WHERE "
				  + " material_id IN (SELECT material_id FROM " + Params.dbName + ".food_material WHERE food_id = " + fb.getFoodId() + " AND restaurant_id = " + fb.getRestaurantId() + ") "
				  + " AND cate_id = (SELECT cate_id FROM " + Params.dbName + ".material_cate WHERE restaurant_id = " + fb.getRestaurantId() + " AND type = " + MaterialCate.Type.GOOD.getValue() + ") ";
		dbCon.stmt.executeUpdate(deleteSQL);
		
		//delete foodMaterial
		deleteSQL = "DELETE FROM " + Params.dbName + ".food_material WHERE food_id = " + fb.getFoodId() + " AND restaurant_id = " + fb.getRestaurantId();
		dbCon.stmt.executeUpdate(deleteSQL);
		
		return count;
	}
	
	/**
	 * 
	 * @param fb
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static void deleteFood(Food fb) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int count = FoodDao.deleteFood(dbCon, fb);
			dbCon.conn.commit();
			if(count == 0){
				throw new BusinessException(FoodError.DELETE_FAIL);
			}
		} catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		} finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param restaurantId
	 * @param foodId
	 * @param imgName
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static void updateFoodImageName(int restaurantId, int foodId, String imgName) throws BusinessException, SQLException{
		Food fb = new Food();
		fb.setRestaurantId(restaurantId);
		fb.setFoodId(foodId);
		fb.setImage(imgName);
		updateFoodImageName(fb);
	}
	
	/**
	 * 
	 * @param fb
	 * @throws BusinessException
	 * @throws SQLException
	 */
	public static void updateFoodImageName(Food fb) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			String updateSQL = "update " + Params.dbName + ".food set "
						       + " img = " + (fb.getImage() == null || fb.getImage().trim().length() == 0 ? null : "'" + fb.getImage() + "'") 
						       + " where food_id = " + fb.getFoodId() + " and restaurant_id = " + fb.getRestaurantId();
			
			int count = dbCon.stmt.executeUpdate(updateSQL);
			if(count <= 0){
				throw new BusinessException("操作失败,未更新编号为" + fb.getFoodId() + "的菜品图片信息!");
			}
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param fb
	 * @return
	 * @throws SQLException
	 */
	public static Food getFoodBasicImage(Food fb) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			String selectSQL = "select A.img" 
						       + " from " + Params.dbName + ".food A"
						       + " where A.food_id = " + fb.getFoodId() + " and A.restaurant_id = " + fb.getRestaurantId();
			dbCon.rs = dbCon.stmt.executeQuery(selectSQL);
			if(dbCon.rs != null && dbCon.rs.next()){
				fb.setImage(dbCon.rs.getString("img"));
			}
		}finally{
			dbCon.disconnect();
		}
		return fb;
	}

	/**
	 * Get the food and its related information to the specified restaurant and id as below.
	 * 1 - Popular taste to each food
	 * 2 - Child food details to the food in case of combo
	 * @param dbCon
	 * 			the database connection
	 * @param terminal
	 * 			the terminal
	 * @param foodAlias
	 * 			the food alias to query
	 * @return	the food to the specified restaurant and id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 * @throws BusinessException
	 * 			throws if the food to specified restaurant and id is NOT found
	 */	
	public static Food getFoodById(DBCon dbCon, Staff term, long foodId) throws SQLException, BusinessException{
		List<Food> result = FoodDao.getFoods(dbCon, term, " AND FOOD.food_id = " + foodId, null);
		if(result.isEmpty()){
			throw new BusinessException("The food(food_id = " + foodId + ",restaurant_id = " + term.getRestaurantId() + ") is NOT found.");
		}else{
			return result.get(0);
		}
	}

	/**
	 * Get the food and its related information to the specified restaurant and id as below.
	 * 1 - Popular taste to each food
	 * 2 - Child food details to the food in case of combo
	 * @param terminal
	 * 			the terminal
	 * @param foodAlias
	 * 			the food alias to query
	 * @return	the food to the specified restaurant and id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 * @throws BusinessException
	 * 			throws if the food to specified restaurant and id is NOT found
	 */
	public static Food getFoodById(Staff term, long foodId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getFoodById(dbCon, term, foodId);
		}finally{
			dbCon.disconnect();
		}
	}

	/**
	 * Get the food and its related information to the specified restaurant and alias id as below.
	 * 1 - Popular taste to each food
	 * 2 - Child food details to the food in case of combo
	 * @param dbCon
	 * 			the database connection
	 * @param terminal
	 * 			the terminal
	 * @param foodAlias
	 * 			the food alias to query
	 * @return	the food to the specified restaurant and alias
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 * @throws BusinessException
	 * 			throws if the food to specified restaurant and alias id is NOT found
	 */	
	public static Food getFoodByAlias(DBCon dbCon, Staff term, int foodAlias) throws SQLException, BusinessException{
		List<Food> result = FoodDao.getFoods(dbCon, term, " AND FOOD.food_alias = " + foodAlias, null);
		if(result.isEmpty()){
			throw new BusinessException("The food(alias_id = " + foodAlias + ",restaurant_id = " + term.getRestaurantId() + ") is NOT found.");
		}else{
			return result.get(0);
		}
	}

	/**
	 * Get the food and its related information to the specified restaurant and alias id as below.
	 * 1 - Popular taste to each food
	 * 2 - Child food details to the food in case of combo
	 * @param terminal
	 * 			the terminal
	 * @param foodAlias
	 * 			the food alias to query
	 * @return	the food to the specified restaurant and alias
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 * @throws BusinessException
	 * 			throws if the food to specified restaurant and alias id is NOT found
	 */	
	public static Food getFoodByAlias(Staff term, int foodAlias) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getFoodByAlias(dbCon, term, foodAlias);
		}finally{
			dbCon.disconnect();
		}
	}

	/**
	 * Get the combo detail to a specific parent food.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param parent
	 * 			the parent food to query
	 * @return	a food list containing the child foods
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	private static List<Food> getComboByParent(DBCon dbCon, Staff staff, Food parent) throws SQLException{
		
		List<Food> childFoods = new ArrayList<Food>();
		
		if(parent.isCombo()){
			String sql;
			sql = " SELECT " +
				  " FOOD.restaurant_id, FOOD.food_id, FOOD.food_alias, FOOD.stock_status, " +
				  " FOOD.name, FPP.unit_price, FPP.commission, FOOD.status, FOOD.pinyin, FOOD.taste_ref_type, " +
				  " FOOD.desc, FOOD.img, " +
				  " KITCHEN.kitchen_id, KITCHEN.kitchen_alias, KITCHEN.name AS kitchen_name, " +
				  " KITCHEN.type AS kitchen_type, KITCHEN.is_allow_temp AS is_allow_temp, " +
				  " DEPT.dept_id, DEPT.name AS dept_name, DEPT.type AS dept_type, " +
				  " COMBO.amount " +
				  " FROM " +
				  Params.dbName + ".food FOOD " + 
			 	  " INNER JOIN " + Params.dbName + ".price_plan PP " +
			 	  " ON FOOD.restaurant_id = PP.restaurant_id AND PP.status = " + PricePlan.Status.ACTIVITY.getVal() +
			 	  " INNER JOIN " + Params.dbName + ".food_price_plan FPP " +
			 	  " ON PP.price_plan_id = FPP.price_plan_id AND FOOD.food_id = FPP.food_id " +
				  " INNER JOIN " +
				  Params.dbName + ".combo COMBO " +
				  " ON FOOD.food_id = COMBO.sub_food_id " + 
				  " LEFT OUTER JOIN " +
				  Params.dbName + ".kitchen KITCHEN " +
				  " ON FOOD.kitchen_id = KITCHEN.kitchen_id " +
				  " LEFT OUTER JOIN " +
				  Params.dbName + ".department DEPT " +
				  " ON KITCHEN.dept_id = DEPT.dept_id AND KITCHEN.restaurant_id = DEPT.restaurant_id " +
				  " WHERE COMBO.food_id = " + parent.getFoodId();
				
			dbCon.rs = dbCon.stmt.executeQuery(sql);
				
			while(dbCon.rs.next()){
					
				int foodId = dbCon.rs.getInt("food_id");
				int restaurantId = dbCon.rs.getInt("restaurant_id");
				
				Food childFood = new Food(foodId);
				childFood.setRestaurantId(restaurantId);
				childFood.setAliasId(dbCon.rs.getInt("food_alias"));
				childFood.setName(dbCon.rs.getString("name"));
				
				//Generate the pinyin to each food
				childFood.setPinyin(PinyinUtil.cn2Spell(childFood.getName()));
				childFood.setPinyinShortcut(PinyinUtil.cn2FirstSpell(childFood.getName()));
				
				childFood.setPrice(dbCon.rs.getFloat("unit_price"));
				childFood.setCommission(dbCon.rs.getFloat("commission"));
				childFood.setStatistics(new FoodStatistics(dbCon.rs.getInt("order_amount")));
				childFood.setStatus(dbCon.rs.getShort("status"));
				childFood.setTasteRefType(dbCon.rs.getShort("taste_ref_type"));
				childFood.setDesc(dbCon.rs.getString("desc"));
				childFood.setImage(dbCon.rs.getString("img"));
				childFood.setKitchen( new Kitchen.Builder(dbCon.rs.getShort("kitchen_alias"), 
		 				   				   		  dbCon.rs.getString("kitchen_name"), 
		 				   				   		  restaurantId)
											.setAllowTemp(dbCon.rs.getBoolean("is_allow_temp"))
											.setKitchenId(dbCon.rs.getLong("kitchen_id"))
											.setType(dbCon.rs.getShort("kitchen_type"))
											.setDept(new Department(dbCon.rs.getString("dept_name"), 
		 				   				    		   		  	    dbCon.rs.getShort("dept_id"), 
		 				   				    		   		  	    restaurantId,
		 				   				    		   		  	    Department.Type.valueOf(dbCon.rs.getShort("dept_type"))))
		 				   				     .build());
				childFood.setStockStatus(dbCon.rs.getInt("stock_status"));
				
				childFood.setAmount(dbCon.rs.getInt("amount"));
				
				childFoods.add(childFood);
			}				
			dbCon.rs.close();
			return childFoods;
				
		}else{
			return childFoods;
		}
	}

	/**
	 * Get the combo detail to a specific parent food.
	 * @param parent
	 * 			the parent food to query
	 * @param staff
	 * 			the staff to perform this action
	 * @return	a food list containing the child foods
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Food> getComboByParent(Staff staff, Food parent) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getComboByParent(dbCon, staff, parent);
		}finally{
			dbCon.disconnect();
		}
	}

	/**
	 * Query the food according to extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param extraCondition
	 * 			the extra condition to SQL statement
	 * @param orderClause
	 * 			the order clause to SQL statement
	 * @return an array result to foods
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 */
	public static List<Food> getPureFoods(DBCon dbCon, String extraCondition, String orderClause) throws SQLException{
		List<Food> foods = new ArrayList<Food>();
	    //get all the food information to this restaurant
		String sql = " SELECT " +
					 " FOOD.restaurant_id, FOOD.food_id, FOOD.food_alias, FOOD.stock_status, " +
					 " FOOD.name, FPP.unit_price, FPP.commission, FOOD.kitchen_alias, FOOD.status, FOOD.taste_ref_type, " +
					 " FOOD.desc, FOOD.img, " +
					 " FOOD.order_amount, " +
					 " KITCHEN.kitchen_id, KITCHEN.kitchen_alias, KITCHEN.name AS kitchen_name, " +
					 " KITCHEN.type AS kitchen_type , KITCHEN.is_allow_temp AS is_allow_temp, " +
					 " DEPT.dept_id, DEPT.name AS dept_name, DEPT.type AS dept_type " +
					 " FROM " + 
					 Params.dbName + ".food FOOD " +
					 " INNER JOIN " + Params.dbName + ".price_plan PP " +
					 " ON FOOD.restaurant_id = PP.restaurant_id AND PP.status = " + PricePlan.Status.ACTIVITY.getVal() +
					 " INNER JOIN " + Params.dbName + ".food_price_plan FPP " +
					 " ON PP.price_plan_id = FPP.price_plan_id AND FOOD.food_id = FPP.food_id " +
					 " LEFT OUTER JOIN " +
					 Params.dbName + ".kitchen KITCHEN " +
					 " ON FOOD.kitchen_id = KITCHEN.kitchen_id " +
					 " LEFT OUTER JOIN " +
					 Params.dbName + ".department DEPT " +
					 " ON KITCHEN.dept_id = DEPT.dept_id AND KITCHEN.restaurant_id = DEPT.restaurant_id " +
					 " WHERE 1=1 " +
					 (extraCondition == null ? "" : extraCondition) + " " +
					 (orderClause == null ? "" : orderClause); 
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
	
			int foodId = dbCon.rs.getInt("food_id");
			int restaurantId = dbCon.rs.getInt("restaurant_id");
			
			Food f = new Food(foodId);
			f.setRestaurantId(restaurantId);
			f.setAliasId(dbCon.rs.getInt("food_alias"));
			f.setName(dbCon.rs.getString("name"));
			
			//Generate the pinyin to each food
			f.setPinyin(PinyinUtil.cn2Spell(f.getName()));
			f.setPinyinShortcut(PinyinUtil.cn2FirstSpell(f.getName()));
			
			f.setPrice(dbCon.rs.getFloat("unit_price"));
			f.setCommission(dbCon.rs.getFloat("commission"));
			f.setStatistics(new FoodStatistics(dbCon.rs.getInt("order_amount")));
			f.setStatus(dbCon.rs.getShort("status"));
			f.setTasteRefType(dbCon.rs.getShort("taste_ref_type"));
			f.setDesc(dbCon.rs.getString("desc"));
			f.setImage(dbCon.rs.getString("img"));
			f.setKitchen( new Kitchen.Builder(dbCon.rs.getShort("kitchen_alias"), 
	 				   				   		  dbCon.rs.getString("kitchen_name"), 
	 				   				   		  restaurantId)
										.setAllowTemp(dbCon.rs.getBoolean("is_allow_temp"))
										.setKitchenId(dbCon.rs.getLong("kitchen_id"))
										.setType(dbCon.rs.getShort("kitchen_type"))
										.setDept(new Department(dbCon.rs.getString("dept_name"), 
	 				   				    		   		  	    dbCon.rs.getShort("dept_id"), 
	 				   				    		   		  	    restaurantId,
	 				   				    		   		  	    Department.Type.valueOf(dbCon.rs.getShort("dept_type"))))
	 				   				     .build());
			f.setStockStatus(dbCon.rs.getInt("stock_status"));
			
			foods.add(f);
		}
		
		dbCon.rs.close();
		
		return foods;
	}

	/**
	 * Query the food according to extra condition.
	 * @param extraCondition
	 * 			the extra condition to SQL statement
	 * @param orderClause
	 * 			the order clause to SQL statement
	 * @return an array result to foods
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 */
	public static List<Food> getPureFoods(String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getPureFoods(dbCon, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}

	/**
	 * Query the foods to the specified restaurant defined in terminal {@link Staff} according to extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal 
	 * @param extraCondition
	 * 			the extra condition to SQL statement
	 * @param orderClause
	 * 			the order clause to SQL statement
	 * @return an array result to foods
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 */
	public static List<Food> getPureFoods(DBCon dbCon, Staff term, String extraCond, String orderClause) throws SQLException{
		return getPureFoods(dbCon, " AND FOOD.restaurant_id = " + term.getRestaurantId() + " " + (extraCond != null ? extraCond : ""), orderClause);
	}

	/**
	 * Query the foods to the specified restaurant defined in terminal {@link Staff} according to extra condition.
	 * @param term
	 * 			the terminal 
	 * @param extraCondition
	 * 			the extra condition to SQL statement
	 * @param orderClause
	 * 			the order clause to SQL statement
	 * @return an array result to foods
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 */
	public static List<Food> getPureFoods(Staff term, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getPureFoods(dbCon, term, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}

	/**
	 * Get the pure food to the specified restaurant and alias id.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param foodAlias
	 * 			the food alias to query
	 * @return the food to specified restaurant and alias id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the food to specified restaurant and alias id is NOT found
	 * 			
	 */
	public static Food getPureFoodByAlias(DBCon dbCon, Staff term, int foodAlias) throws SQLException, BusinessException{
		List<Food> result = getPureFoods(dbCon, term, " AND FOOD.food_alias = " + foodAlias, null);
		if(result.isEmpty()){
			throw new BusinessException("The food(alias_id = " + foodAlias + ",restaurant_id = " + term.getRestaurantId() + ") is NOT found.");
		}else{
			return result.get(0);
		}
	}

	/**
	 * Get the pure food to the specified restaurant and alias id.
	 * @param term
	 * 			the terminal
	 * @param foodAlias
	 * 			the food alias to query
	 * @return the food to specified restaurant and alias id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			
	 */
	public static Food getPureFoodByAlias(Staff term, int foodAlias) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getPureFoodByAlias(dbCon, term, foodAlias);
		}finally{
			dbCon.disconnect();
		}
	}

	/**
	 * Get the pure food to the specified restaurant and id.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param foodId
	 * 			the food id to query
	 * @return the food to specified restaurant and id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the food to specified restaurant and id is NOT found
	 * 			
	 */
	public static Food getPureFoodById(DBCon dbCon, Staff term, long foodId) throws SQLException, BusinessException{
		List<Food> result = getPureFoods(dbCon, term, " AND FOOD.food_id = " + foodId, null);
		if(!result.isEmpty()){
			return result.get(0);
		}else{
			throw new BusinessException("The food(food_id = " + foodId + ",restaurant_id = " + term.getRestaurantId() + ") is NOT found.");
		}
	}

	/**
	 * Get the pure food to the specified restaurant and id.
	 * @param term
	 * 			the terminal
	 * @param foodId
	 * 			the food id to query
	 * @return the food to specified restaurant and id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the food to specified restaurant and id is NOT found
	 * 			
	 */
	public static Food getPureFoodById(Staff term, int foodId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getPureFoodById(dbCon, term, foodId);
		}finally{
			dbCon.disconnect();
		}
	}

	/**
	 * Get the food and its related information to the specified restaurant defined in terminal {@link Staff} as below.
	 * 1 - Popular taste to each food
	 * 2 - Child food details to the food in case of combo
	 * @param dbCon
	 * 			the database connection
	 * @param terminal
	 * 			the staff to perform this action
	 * @param extraCondition
	 * 			the extra condition to SQL statement
	 * @param order clause
	 * 			the order clause to SQL statement
	 * @return	an array result
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 */			
	public static List<Food> getFoods(DBCon dbCon, Staff staff, String extraCondition, String orderClause) throws SQLException{

		//Using link hash map to keep original order after retrieving the foods by order clause defined in SQL statement.
		Map<Integer, Food> foods = new LinkedHashMap<Integer, Food>();
		
		if(orderClause == null){
			orderClause = " ORDER BY FOOD.food_alias ";
		}
		//Get the basic detail to each food.
		List<Food> pureFoods = getPureFoods(dbCon, staff, extraCondition, orderClause);

		StringBuilder foodCond = new StringBuilder();

		for(Food f : pureFoods){
			foods.put(f.getFoodId(), f);
			if(foodCond.length() == 0){
				foodCond.append(f.getFoodId());
			}else{
				foodCond.append(",").append(f.getFoodId());
			}
			
			//Generate the pinyin to each food
			f.setPinyin(PinyinUtil.cn2Spell(f.getName()));
			f.setPinyinShortcut(PinyinUtil.cn2FirstSpell(f.getName()));
		}
		
		//Get the associated popular tastes to each food.
		String sql;
		sql = " SELECT " +
		      " FTR.food_id, " + 
			  " TASTE.taste_id, TASTE.restaurant_id " +
			  " FROM " + Params.dbName + ".food_taste_rank FTR " +
			  " LEFT JOIN " + Params.dbName + ".taste TASTE " +
			  " ON TASTE.taste_id = FTR.taste_id " + 
			  " WHERE " + " FTR.food_id IN(" + foodCond + ")" +
			  " ORDER BY FTR.rank ";
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		while(dbCon.rs.next()){
			Food f = foods.get(dbCon.rs.getLong("food_id"));
			if(f != null){
				Taste t = new Taste(dbCon.rs.getInt("taste_id"));
				t.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
				f.addPopTaste(t);
			}
		}
		
		dbCon.rs.close();
		
		//Get the combo detail to each food if belongs to combo. 
		for(Entry<Integer, Food> entry : foods.entrySet()){
			if(entry.getValue().isCombo()){
				entry.getValue().setChildFoods(getComboByParent(dbCon, staff, entry.getValue()));
			}
		}
		
		return new ArrayList<Food>(foods.values());
		
	}

	/**
	 * Get the food and its related information to the specified restaurant defined in terminal {@link Staff} as below.
	 * 1 - Popular taste to each food
	 * 2 - Child food details to the food in case of combo
	 * @param terminal
	 * 			the terminal
	 * @param extraCondition
	 * 			the extra condition to SQL statement
	 * @param order clause
	 * 			the order clause to SQL statement
	 * @return	an array result
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 */	
	public static List<Food> getFoods(Staff term, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getFoods(dbCon, term, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Make the specified food to be sell out according to food alias.
	 * @param term
	 * 			the terminal
	 * @param foodAlias
	 * 			the food alias
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void makeSellOutByAlias(Staff term, int foodAlias) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			makeSellOutByAlias(dbCon, term, foodAlias);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Make the specified food to be sell out according to food alias.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param foodAlias
	 * 			the food alias
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void makeSellOutByAlias(DBCon dbCon, Staff term, int foodAlias) throws SQLException{
		String sql;
		
		sql = " UPDATE " + Params.dbName + ".food SET " +
			  " status = status | " + Food.SELL_OUT + 
			  " WHERE restaurant_id = " + term.getRestaurantId() + 
			  " AND food_alias = " + foodAlias;
		
		dbCon.stmt.executeUpdate(sql);
	}
	
	/**
	 * Make the specified food to be on sale according to food alias.
	 * @param term
	 * 			the terminal
	 * @param foodAlias
	 * 			the food alias
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void makeOnSaleByAlias(Staff term, int foodAlias) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			makeOnSaleByAlias(dbCon, term, foodAlias);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Make the specified food to be on sale according to food alias.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param foodAlias
	 * 			the food alias
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void makeOnSaleByAlias(DBCon dbCon, Staff term, int foodAlias) throws SQLException{
		String sql;
		
		sql = " UPDATE " + Params.dbName + ".food SET " +
			  " status = status & ~" + Food.SELL_OUT + 
			  " WHERE restaurant_id = " + term.getRestaurantId() + 
			  " AND food_alias = " + foodAlias;
		
		dbCon.stmt.executeUpdate(sql);
	}
}
