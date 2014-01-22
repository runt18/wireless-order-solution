package com.wireless.db.menuMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mysql.jdbc.Statement;
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
import com.wireless.pojo.menuMgr.Department.DeptId;
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
	 * Insert a new food according to specific builder.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to insert a new food
	 * @return the id to food just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static int insert(Staff staff, Food.InsertBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int foodId = insert(dbCon, staff, builder);
			dbCon.conn.commit();
			return foodId;
			
		}catch(Exception e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert a new food according to specific builder.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to insert a new food
	 * @return the id to food just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 */
	public static int insert(DBCon dbCon, Staff staff, Food.InsertBuilder builder) throws SQLException, BusinessException{
		String sql;
		Food f = builder.build();
		sql = " INSERT INTO " + Params.dbName + ".food" +
			  " (`name`, `food_alias`, `restaurant_id`, `kitchen_id`, `status`, `desc`, `stock_status`) VALUES ( " +
			  "'" + f.getName() + "'," +
			  f.getAliasId() + "," +
			  staff.getRestaurantId() + "," +
			  f.getKitchen().getId() + "," +
			  f.getStatus() + "," +
			  (f.hasDesc() ? "'" + f.getDesc() + "'" : "NULL") + "," +
			  f.getStockStatus().getVal() + 
			  ")";
		
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		int foodId = 0;
		if(dbCon.rs.next()){
			foodId = dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The food id is NOT generated successfully.");
		}
		dbCon.rs.close();
		
		//FIXME 新增菜谱价格方案信息
		sql = " INSERT INTO " + Params.dbName + ".food_price_plan " + 
			  " (`restaurant_id`, `food_id`, `price_plan_id`, `unit_price`, `commission`) " +
			  " SELECT " + staff.getRestaurantId() + "," + foodId + ",price_plan_id," + f.getPrice() + ", " + f.getCommission() + 
			  " FROM " + Params.dbName + ".price_plan WHERE restaurant_id = " + staff.getRestaurantId();
		dbCon.stmt.executeUpdate(sql);
		
		//设为商品出库时，在库存中增加这条菜品的商品记录
		if(f.getStockStatus() == Food.StockStatus.GOOD){
			MaterialDao.insertGood(dbCon, staff, foodId, f.getName());
		}
		return foodId;
	}
	
	/**
	 * Delete the food to a specific id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param foodId
	 * 			the food id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			<li>throws if the food is still used by any order which is unpaid<br>
	 * 			<li>throws if the food is the sub to any combo<br>
	 * 			<li>throws if if the food has stock status
	 */
	public static void delete(Staff staff, int foodId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			delete(dbCon, staff, foodId);
			dbCon.conn.commit();
			
		}catch(Exception e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the food to a specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param foodId
	 * 			the food id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			<li>throws if the food is still used by any order which is unpaid<br>
	 * 			<li>throws if the food is the sub to any combo<br>
	 * 			<li>throws if if the food has stock status
	 */
	public static void delete(DBCon dbCon, Staff staff, int foodId) throws SQLException, BusinessException{
		String sql;
		
		//Check to see whether the food is used by unpaid order.
		sql = " SELECT food_id " +
			  " FROM " + Params.dbName + ".order_food OF " +
			  " JOIN " + Params.dbName + ".order O ON 1 = 1 " + 
			  " AND OF.order_id = O.id " +
			  " AND O.restaurant_id = " + staff.getRestaurantId() +
			  " AND O.status = " + Order.Status.UNPAID.getVal() +
			  " WHERE OF.food_id = " + foodId;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			throw new BusinessException(FoodError.FOOD_IN_USED);
		}
		dbCon.rs.close();
		
		//Check to see whether the food is the sub to any combo.
		sql = " SELECT sub_food_id FROM " + Params.dbName + ".combo WHERE 1 = 1 " +
			  " AND restaurant_id = " + staff.getRestaurantId() +
			  " AND sub_food_id = " + foodId +
			  " LIMIT 1 ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			throw new BusinessException(FoodError.DELETE_FAIL_SINCE_COMBO_SUB_FOOD);
		}
		dbCon.rs.close();
		
		//Check to see whether the food has stock status
		sql = " SELECT stock_status FROM " + Params.dbName + ".food WHERE food_id = " + foodId;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			if(dbCon.rs.getInt("stock_status") != Food.StockStatus.NONE.getVal()){
				throw new BusinessException(FoodError.DELETE_FAIL_SINCE_STILL_STOCK);
			}
		}
		dbCon.rs.close();
		
		//Delete the associated combo info
		sql = " DELETE FROM " + Params.dbName + ".combo WHERE food_id = " + foodId;
		dbCon.stmt.executeUpdate(sql);

		//FIXME Delete the associated food price plan
		sql = " DELETE FROM " + Params.dbName + ".food_price_plan WHERE food_id = " + foodId;
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the food info
		sql = " DELETE FROM " + Params.dbName + ".food WHERE food_id = " + foodId;
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(FoodError.FOOD_NOT_EXIST);
		}
	}
	
	/**
	 * Update the food according to specific builder.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to update a food
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the food to update does NOT exist
	 */
	public static void update(Staff staff, Food.UpdateBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			update(dbCon, staff, builder);
			dbCon.conn.commit();
		}catch(Exception e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the food according to specific builder.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to update a food
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the food to update does NOT exist
	 */
	public static void update(DBCon dbCon, Staff staff, Food.UpdateBuilder builder) throws SQLException, BusinessException{
		String sql;
		Food f = builder.build();
		
		//Compare the original status against the new and set the status bit if changed.
		sql = " SELECT status FROM " + Params.dbName + ".food WHERE food_id = " + f.getFoodId();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			f.setStatus(dbCon.rs.getInt("status"));
		}else{
			throw new BusinessException(FoodError.FOOD_NOT_EXIST);
		}
		dbCon.rs.close();
		
		if(builder.isCurPriceChanged()){
			f.setCurPrice(builder.isCurPrice());
		}
		if(builder.isGiftChanged()){
			f.setGift(builder.isGift());
		}
		if(builder.isSelloutChanged()){
			f.setSellOut(builder.isSellout());
		}
		if(builder.isRecommendChanged()){
			f.setRecommend(builder.isRecommend());
		}
		if(builder.isSpecialChanged()){
			f.setSpecial(builder.isSpecial());
		}
		if(builder.isHotChanged()){
			f.setHot(builder.isHot());
		}
		if(builder.isWeightChanged()){
			f.setWeigh(builder.isWeight());
		}
		if(builder.isCommissionChanged()){
			f.setCommission(builder.isCommission());
		}
		if(builder.isComboChanged()){
			f.setCombo(builder.isCombo());
		}
		
		//Delete the food material relationship if cancel the stock status
		if(builder.isStockChanged() && f.getStockStatus() == Food.StockStatus.NONE){
			sql = " DELETE FROM " + Params.dbName + ".food_material WHERE food_id = " + f.getFoodId();
			dbCon.stmt.executeUpdate(sql);
		}

		//FIXME 修改当前活动价格方案信息 
		sql = " UPDATE " + Params.dbName + ".food_price_plan SET " +
			  " unit_price = " + f.getPrice()  + 
			  " ,commission = " + (f.isCommission() ? f.getCommission() : 0) +
			  " WHERE food_id = " + f.getFoodId() +
			  " AND price_plan_id = (SELECT price_plan_id FROM " + Params.dbName + ".price_plan WHERE restaurant_id = " + staff.getRestaurantId() + " AND status = " + PricePlan.Status.ACTIVITY.getVal() + ")";
		dbCon.stmt.executeUpdate(sql);
		
		sql = " UPDATE " + Params.dbName + ".food SET " +
			  " food_id = " + f.getFoodId() +
			  " ,status = " + f.getStatus() +
			  (builder.isAliasChanged() ? ",food_alias = " + f.getAliasId() : "") +
			  (builder.isNameChanged() ? ",name = '" + f.getName() + "'" : "") +
			  (builder.isKitchenChanged() ? ",kitchen_id = " + f.getKitchen().getId() : "") +
			  //(builder.isPriceChanged() ? ",price = " + f.getPrice() : "") +
			  (builder.isStockChanged() ? ",stock_status = " + f.getStockStatus().getVal() : "") +
			  (builder.isDescChanged() ? ",`desc` = '" + f.getDesc() + "'" : "") +
			  " WHERE food_id = " + f.getFoodId();
		
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(FoodError.FOOD_NOT_EXIST);
		}

	}
	
	/**
	 * Build the combo according to a specific builder.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the combo builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the parent or any child food does NOT exist
	 */
	public static void buildCombo(Staff staff, Food.ComboBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			buildCombo(dbCon, staff, builder);
			dbCon.conn.commit();
		}catch(Exception e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Build the combo according to a specific builder.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the combo builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the parent or any child food does NOT exist
	 */
	public static void buildCombo(DBCon dbCon, Staff staff, Food.ComboBuilder builder) throws SQLException, BusinessException{
		Food f = builder.build();
		
		Food parent = getPureById(dbCon, staff, f.getFoodId());
		for(Food child : f.getChildFoods()){
			parent.addChildFood(getPureById(dbCon, staff, child.getFoodId()));
		}
		
		String sql;
		sql = " DELETE FROM " + Params.dbName + ".combo WHERE food_id = " + parent.getFoodId();
		dbCon.stmt.executeUpdate(sql);
		
		if(parent.hasChildFoods()){
			update(dbCon, staff, new Food.UpdateBuilder(parent.getFoodId()).setCombo(true));
			
			for(Food child : parent.getChildFoods()){
				sql = " INSERT INTO " + Params.dbName + ".combo" +
					  " (`food_id`, `sub_food_id`, `restaurant_id`, `amount`) VALUES(" +
					  parent.getFoodId() + "," +
					  child.getFoodId() + "," +
					  staff.getRestaurantId() + "," +
					  child.getAmount() +
					  ")";
				dbCon.stmt.executeUpdate(sql);
			}
		}else{
			update(dbCon, staff, new Food.UpdateBuilder(parent.getFoodId()).setCombo(false));
		}

	}
	
	/**
	 * @deprecated
	 * @param dbCon
	 * @param term
	 * @param fb
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	private static int insertFoodBaisc(DBCon dbCon, Staff term, Food fb) throws BusinessException, SQLException{
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
				+ " ( food_alias, name, pinyin, restaurant_id, kitchen_id, status, taste_ref_type, food.desc, food.stock_status ) "
				+ "values("
				+ fb.getAliasId() + ", " 
				+ "'" + fb.getName() + "', " 
				+ "'" + fb.getPinyin() + "', " 
				+ fb.getRestaurantId() + ", " 
				+ fb.getKitchen().getId() + ", " 
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
		insertSQL = "INSERT INTO food_price_plan (restaurant_id, food_id, price_plan_id, unit_price, commission)"
				  + " SELECT " + fb.getRestaurantId() + "," + fb.getFoodId() + ",price_plan_id," + fb.getPrice() + ", " + fb.getCommission() + " FROM price_plan WHERE restaurant_id = " + fb.getRestaurantId();
		count = dbCon.stmt.executeUpdate(insertSQL);
		if(count == 0){
			throw new BusinessException(PlanError.PRICE_FOOD_INSERT);
		}
		
		// 处理库存
		if(fb.getStockStatus() == Food.StockStatus.NONE){
			// 无需处理
		}else if(fb.getStockStatus() == Food.StockStatus.GOOD){
			MaterialDao.insertGood(dbCon, term, fb.getFoodId(), fb.getName());
		}else if(fb.getStockStatus() == Food.StockStatus.MATERIAL){
			// 无需处理
		}
		return count;
	}
	
	/**
	 * @deprecated
	 * @param term
	 * @param fb
	 * @throws BusinessException
	 * @throws SQLException
	 */
	private static void insertFoodBaisc(Staff term, Food fb) throws BusinessException, SQLException{		
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
	 * @deprecated
	 * @param term
	 * @param fb
	 * @param content
	 * @throws BusinessException
	 * @throws SQLException
	 */
	private static void insertFoodBaisc(Staff term, Food fb, String content) throws BusinessException, SQLException{
		FoodDao.insertFoodBaisc(term, fb);
		try{
			TasteRefDao.execByFood(FoodDao.getById(term, fb.getFoodId()));
			FoodCombinationDao.updateFoodCombination(fb.getFoodId(), fb.getRestaurantId(), fb.getStatus(), content);
		} catch(Exception e){
			throw new BusinessException(FoodError.COMBO_UPDATE_FAIL);
		}
	}
	
	/**
	 * @deprecated
	 * @param dbCon
	 * @param fb
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	private static int updateFoodBaisc(DBCon dbCon, Staff term, Food fb) throws BusinessException, SQLException{
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
	 * @deprecated
	 * @param term
	 * @param fb
	 * @throws BusinessException
	 * @throws SQLException
	 */
	private static void updateFoodBaisc(Staff term, Food fb) throws BusinessException, SQLException{
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
	 * @deprecated
	 * @param dbCon
	 * @param fb
	 * @return
	 * @throws BusinessException
	 * @throws SQLException
	 */
	private static int deleteFood(DBCon dbCon, Food fb) throws BusinessException, SQLException{
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
			throw new BusinessException(FoodError.FOOD_IN_USED);
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
	 * @deprecated
	 * @param fb
	 * @throws BusinessException
	 * @throws SQLException
	 */
	private static void deleteFood(Food fb) throws BusinessException, SQLException{
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
		Food fb = new Food(foodId);
		fb.setRestaurantId(restaurantId);
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
	 * @deprecated
	 * @param fb
	 * @return
	 * @throws SQLException
	 */
	private static Food getFoodBasicImage(Food fb) throws SQLException{
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
	 * @param foodId
	 * 			the food id to query
	 * @return	the food to the specified restaurant and id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 * @throws BusinessException
	 * 			throws if the food to specified restaurant and id is NOT found
	 */	
	public static Food getById(DBCon dbCon, Staff term, int foodId) throws SQLException, BusinessException{
		List<Food> result = FoodDao.getByCond(dbCon, term, " AND FOOD.food_id = " + foodId, null);
		if(result.isEmpty()){
			throw new BusinessException(FoodError.FOOD_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}

	/**
	 * Get the food and its related information to the specified restaurant and id as below.
	 * 1 - Popular taste to each food
	 * 2 - Child food details to the food in case of combo
	 * @param staff
	 * 			the staff to perform this action
	 * @param foodId
	 * 			the food id to query
	 * @return	the food to the specified restaurant and id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 * @throws BusinessException
	 * 			throws if the food to specified restaurant and id is NOT found
	 */
	public static Food getById(Staff staff, int foodId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, staff, foodId);
		}finally{
			dbCon.disconnect();
		}
	}

	/**
	 * Get the child foods to a specific parent food.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param parentId
	 * 			the id parent food 
	 * @return	a food list containing the child foods
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	private static List<Food> getChildrenByParent(DBCon dbCon, Staff staff, int parentId) throws SQLException{
		
		List<Food> childFoods = new ArrayList<Food>();
		
		String sql;
		sql = " SELECT " +
			  " FOOD.restaurant_id, FOOD.food_id, FOOD.food_alias, FOOD.stock_status, " +
			  " FOOD.name, FPP.unit_price, FPP.commission, FOOD.status, FOOD.pinyin, FOOD.taste_ref_type, " +
			  " FOOD.desc, FOOD.img, " +
			  " KITCHEN.kitchen_id, KITCHEN.name AS kitchen_name, KITCHEN.display_id AS kitchen_display_id, " +
			  " KITCHEN.type AS kitchen_type, KITCHEN.is_allow_temp AS is_allow_temp, " +
			  " DEPT.dept_id, DEPT.name AS dept_name, DEPT.type AS dept_type, DEPT.display_id AS dept_display_id, " +
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
			  " WHERE COMBO.food_id = " + parentId;
			
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
			childFood.setStatus(dbCon.rs.getShort("status"));
			childFood.setTasteRefType(dbCon.rs.getShort("taste_ref_type"));
			childFood.setDesc(dbCon.rs.getString("desc"));
			childFood.setImage(dbCon.rs.getString("img"));
			childFood.setKitchen(new Kitchen.QueryBuilder(dbCon.rs.getInt("kitchen_id"), dbCon.rs.getString("kitchen_name")) 
	 				   				   		.setRestaurantId(restaurantId)
	 				   				   		.setDisplayId(dbCon.rs.getInt("kitchen_display_id"))
	 				   				   		.setAllowTemp(dbCon.rs.getBoolean("is_allow_temp"))
	 				   				   		.setType(dbCon.rs.getShort("kitchen_type"))
	 				   				   		.setDept(new Department(dbCon.rs.getString("dept_name"), 
	 				   				    		   		  	    dbCon.rs.getShort("dept_id"), 
	 				   				    		   		  	    restaurantId,
	 				   				    		   		  	    Department.Type.valueOf(dbCon.rs.getShort("dept_type")),
	 				   				    		   		  	    dbCon.rs.getInt("dept_display_id")))
	 				   				     .build());
			childFood.setStockStatus(dbCon.rs.getInt("stock_status"));
			
			childFood.setAmount(dbCon.rs.getInt("amount"));
			
			childFoods.add(childFood);
		}				
		dbCon.rs.close();
		return childFoods;
				
	}

	/**
	 * Get the child foods to a specific parent food.
	 * @param parentId
	 * 			the id to parent food
	 * @param staff
	 * 			the staff to perform this action
	 * @return	a food list containing the child foods
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Food> getChildrenByParent(Staff staff, int parentId) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getChildrenByParent(dbCon, staff, parentId);
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
	public static List<Food> getPureByCond(DBCon dbCon, String extraCondition, String orderClause) throws SQLException{
		List<Food> foods = new ArrayList<Food>();
	    //get all the food information to this restaurant
		String sql = " SELECT " +
					 " FOOD.restaurant_id, FOOD.food_id, FOOD.food_alias, FOOD.stock_status, " +
					 " FOOD.name, FPP.unit_price, FPP.commission, FOOD.status, FOOD.taste_ref_type, " +
					 " FOOD.desc, FOOD.img, " +
					 " FOOD.order_amount, " +
					 " KITCHEN.kitchen_id, KITCHEN.display_id AS kitchen_display_id, KITCHEN.name AS kitchen_name, " +
					 " KITCHEN.type AS kitchen_type , KITCHEN.is_allow_temp AS is_allow_temp, " +
					 " DEPT.dept_id, DEPT.name AS dept_name, DEPT.type AS dept_type, DEPT.display_id AS dept_display_id " +
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
			f.setKitchen(new Kitchen.QueryBuilder(dbCon.rs.getInt("kitchen_id"), dbCon.rs.getString("kitchen_name")) 
									.setDisplayId(dbCon.rs.getInt("kitchen_display_id"))
	 				   				.setRestaurantId(restaurantId)
									.setAllowTemp(dbCon.rs.getBoolean("is_allow_temp"))
									.setType(dbCon.rs.getShort("kitchen_type"))
									.setDept(new Department(dbCon.rs.getString("dept_name"), 
 				   				    		   		  	    dbCon.rs.getShort("dept_id"), 
 				   				    		   		  	    restaurantId,
 				   				    		   		  	    Department.Type.valueOf(dbCon.rs.getShort("dept_type")),
 				   				    		   		  	    dbCon.rs.getInt("dept_display_id")))
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
	public static List<Food> getPureByCond(String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getPureByCond(dbCon, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}

	/**
	 * Query the foods to the specified restaurant defined in staff {@link Staff} according to extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action 
	 * @param extraCondition
	 * 			the extra condition to SQL statement
	 * @param orderClause
	 * 			the order clause to SQL statement
	 * @return an array result to foods
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 */
	public static List<Food> getPureByCond(DBCon dbCon, Staff staff, String extraCond, String orderClause) throws SQLException{
		return getPureByCond(dbCon, " AND FOOD.restaurant_id = " + staff.getRestaurantId() + " " + (extraCond != null ? extraCond : ""), orderClause);
	}

	/**
	 * Query the foods to the specified restaurant defined in terminal {@link Staff} according to extra condition.
	 * @param staff
	 * 			the staff to perform this action 
	 * @param extraCondition
	 * 			the extra condition to SQL statement
	 * @param orderClause
	 * 			the order clause to SQL statement
	 * @return an array result to foods
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 */
	public static List<Food> getPureByCond(Staff staff, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getPureByCond(dbCon, staff, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}

	/**
	 * Get the pure foods to specific restaurant defined in staff {@link Staff}.
	 * @param staff
	 * 			the staff to perform this action
	 * @return the foods to specific restaurant
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Food> getPureFoods(Staff staff) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getPureFoods(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the pure foods to specific restaurant defined in staff {@link Staff}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the foods to specific restaurant
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Food> getPureFoods(DBCon dbCon, Staff staff) throws SQLException{
		return getPureByCond(dbCon, staff, null, null);
	}
	
	/**
	 * Get the pure food to the specified restaurant and id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param foodId
	 * 			the food id to query
	 * @return the food to specified restaurant and id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the food to specified restaurant and id is NOT found
	 * 			
	 */
	public static Food getPureById(DBCon dbCon, Staff staff, int foodId) throws SQLException, BusinessException{
		List<Food> result = getPureByCond(dbCon, staff, " AND FOOD.food_id = " + foodId, null);
		if(!result.isEmpty()){
			return result.get(0);
		}else{
			throw new BusinessException(FoodError.FOOD_NOT_EXIST);
		}
	}

	/**
	 * Get the pure food to the specified restaurant and id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param foodId
	 * 			the food id to query
	 * @return the food to specified restaurant and id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the food to specified restaurant and id is NOT found
	 * 			
	 */
	public static Food getPureById(Staff staff, int foodId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getPureById(dbCon, staff, foodId);
		}finally{
			dbCon.disconnect();
		}
	}

	/**
	 * Get pure foods to specific department.
	 * @param staff
	 * 			the staff to perform this action
	 * @param deptId
	 * 			the department id
	 * @return the foods to specific department
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Food> getPureByDept(Staff staff, DeptId deptId) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getPureByDept(dbCon, staff, deptId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get pure foods to specific department.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param deptId
	 * 			the department id
	 * @return the foods to specific department
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Food> getPureByDept(DBCon dbCon, Staff staff, DeptId deptId) throws SQLException{
		return getPureByCond(dbCon, staff, " AND DEPT.dept_id = " + deptId.getVal() + " AND DEPT.restaurant_id = " + staff.getRestaurantId(), null);
	}
	
	/**
	 * Get pure foods to specific kitchen.
	 * @param staff
	 * 			the staff to perform this action
	 * @param kitchenId
	 * 			the kitchen id
	 * @return the foods to specific kitchen
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Food> getPureByKitchen(Staff staff, int kitchenId) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getPureByKitchen(dbCon, staff, kitchenId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get pure foods to specific kitchen.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param kitchenId
	 * 			the kitchen id
	 * @return the foods to specific kitchen
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Food> getPureByKitchen(DBCon dbCon, Staff staff, int kitchenId) throws SQLException{
		return getPureByCond(dbCon, staff, " AND KITCHEN.kitchen_id = " + kitchenId, null);
	}
	
	/**
	 * Get pure foods like the specific name.
	 * @param staff
	 * 			the staff to perform this action
	 * @param name
	 * 			the name
	 * @return the food like the specific name
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Food> getPureByName(Staff staff, String name) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getPureByName(dbCon, staff, name);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get pure foods like the specific name.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param name
	 * 			the name
	 * @return the food like the specific name
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Food> getPureByName(DBCon dbCon, Staff staff, String name) throws SQLException{
		return getPureByCond(dbCon, staff, " AND FOOD.name LIKE %" + name + "%", null);
	}
	
	/**
	 * Get the food and its related information to the specified restaurant defined in terminal {@link Staff} as below.
	 * 1 - Popular taste to each food
	 * 2 - Child food details to the food in case of combo
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCondition
	 * 			the extra condition to SQL statement
	 * @param order clause
	 * 			the order clause to SQL statement
	 * @return	an array result
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 */			
	public static List<Food> getByCond(DBCon dbCon, Staff staff, String extraCondition, String orderClause) throws SQLException{

		//Using link hash map to keep original order after retrieving the foods by order clause defined in SQL statement.
		Map<Integer, Food> foods = new LinkedHashMap<Integer, Food>();
		
		if(orderClause == null){
			orderClause = " ORDER BY FOOD.food_id ";
		}
		//Get the basic detail to each food.
		List<Food> pureFoods = getPureByCond(dbCon, staff, extraCondition, orderClause);

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
		
		if(foodCond.length() > 0){
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
				Food f = foods.get(dbCon.rs.getInt("food_id"));
				if(f != null){
					Taste t = new Taste(dbCon.rs.getInt("taste_id"));
					t.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
					f.addPopTaste(t);
				}
			}
			
			dbCon.rs.close();
		}
		
		//Get the combo detail to each food if belongs to combo. 
		for(Entry<Integer, Food> entry : foods.entrySet()){
			if(entry.getValue().isCombo()){
				entry.getValue().setChildFoods(getChildrenByParent(dbCon, staff, entry.getValue().getFoodId()));
			}
		}
		
		return new ArrayList<Food>(foods.values());
		
	}

	/**
	 * Get the food and its related information to the specified restaurant defined in staff {@link Staff} as below.
	 * 1 - Popular taste to each food
	 * 2 - Child food details to the food in case of combo
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
	public static List<Food> getByCond(Staff staff, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
}
