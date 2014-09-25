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
import com.wireless.exception.BusinessException;
import com.wireless.exception.FoodError;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.menuMgr.ComboFood;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Department.DeptId;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.menuMgr.FoodStatistics;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.util.PinyinUtil;

public class FoodDao {
	
	public static class ExtraCond4Combo{
		private final int parentId;
		
		private int childId;
		
		public ExtraCond4Combo(int parentId){
			this.parentId = parentId;
		}
		
		public ExtraCond4Combo setChildId(int childId){
			this.childId = childId;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			extraCond.append(" AND combo.food_id = " + parentId);
			if(childId > 0){
				extraCond.append(" AND combo.sub_food_id = " + childId);
			}
			return extraCond.toString();
		}
	}
	
	/**
	 * Check to see whether the food alias is duplicated.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param f
	 * 			the food to check			
	 * @return true if the food alias is duplicated, otherwise false
	 * @param foodAlias
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	private static boolean isAliasDuplicated(DBCon dbCon, Staff staff, Food f) throws SQLException, BusinessException{
		String sql;
		sql = " SELECT COUNT(*) FROM " + Params.dbName + ".food WHERE 1 = 1 " + 
			  " AND restaurant_id = " + staff.getRestaurantId() + 
			  " AND food_alias = " + f.getAliasId() +
			  " AND food_id <> " + f.getFoodId();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		boolean isDuplicated = false;
		if(dbCon.rs.next()){
			isDuplicated = (dbCon.rs.getInt(1) != 0);
		}
		dbCon.rs.close();
		
		return isDuplicated;
	}
	
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
	 * 			throws if the food alias is duplicated
	 */
	public static int insert(DBCon dbCon, Staff staff, Food.InsertBuilder builder) throws SQLException, BusinessException{
		String sql;
		Food f = builder.build();

		//Check to see whether the alias is duplicated.
		if(builder.isAliasChanged()){
			if(isAliasDuplicated(dbCon, staff, f)){
				throw new BusinessException(FoodError.DUPLICATED_FOOD_ALIAS);
			}
		}
		
		sql = " INSERT INTO " + Params.dbName + ".food" +
			  " (`name`, `food_alias`, `price`, `commission`, `restaurant_id`, `kitchen_id`, `status`, `desc`, `stock_status`) VALUES ( " +
			  "'" + f.getName() + "'," +
			  f.getAliasId() + "," +
			  f.getPrice() + "," +
			  f.getCommission() + "," +
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
			  " WHERE OF.food_id = " + foodId +
			  " LIMIT 1 ";
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
	 * 			throws if cases below 
	 * 			<li>the food to update does NOT exist
	 * 			<li>the food alias is duplicated
	 */
	public static void update(DBCon dbCon, Staff staff, Food.UpdateBuilder builder) throws SQLException, BusinessException{
		String sql;
		Food f = builder.build();
		
		//Check to see whether the alias is duplicated.
		if(builder.isAliasChanged() && f.getAliasId() != 0){
			if(isAliasDuplicated(dbCon, staff, f)){
				throw new BusinessException(FoodError.DUPLICATED_FOOD_ALIAS);
			}
		}
		
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
		f.setTemp(false);
		
		//Delete the food material relationship if cancel the stock status
		if(f.getStockStatus() == Food.StockStatus.NONE){
			int materialId = -1;
			sql = "SELECT material_id FROM "+ Params.dbName + ".food_material WHERE food_id = " + f.getFoodId();
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				materialId = dbCon.rs.getInt(1);
			}
			dbCon.rs.close();
			if(materialId > -1){
				MaterialDao.delete(materialId);
			}
			sql = " DELETE FROM " + Params.dbName + ".food_material WHERE food_id = " + f.getFoodId();
			dbCon.stmt.executeUpdate(sql);

		}else if(f.getStockStatus() == Food.StockStatus.GOOD){
			if(!MaterialDao.checkMaterialFoodEx(dbCon, f.getFoodId())){
				MaterialDao.insertGood(dbCon, staff, f.getFoodId(), f.getName());
			}
		}

		sql = " UPDATE " + Params.dbName + ".food SET " +
			  " food_id = " + f.getFoodId() +
			  " ,status = " + f.getStatus() +
			  (builder.isAliasChanged() ? ",food_alias = " + f.getAliasId() : "") +
			  (builder.isNameChanged() ? ",name = '" + f.getName() + "'" : "") +
			  (builder.isKitchenChanged() ? ",kitchen_id = " + f.getKitchen().getId() : "") +
			  (builder.isPriceChanged() ? ",price = " + f.getPrice() : "") +
			  (builder.isCommissionChanged() ? ",commission = " + f.getCommission() : "") +
			  (builder.isStockChanged() ? ",stock_status = " + f.getStockStatus().getVal() : "") +
			  (builder.isDescChanged() ? ",`desc` = '" + f.getDesc() + "'" : "") +
			  (builder.isImageChanged() ? ",img = '" + f.getImage() + "'" : "") +
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
	 * 			throws if either cases below<br>
	 * 			<li>the parent or any child food does NOT exist
	 * 			<li>and child food belongs to combo
	 */
	public static void buildCombo(DBCon dbCon, Staff staff, Food.ComboBuilder builder) throws SQLException, BusinessException{
		Food f = builder.build();
		
		Food parent = getPureById(dbCon, staff, f.getFoodId());
		for(ComboFood child : f.getChildFoods()){
			if(child.asFood().isCombo()){
				throw new BusinessException(FoodError.CHILD_FOOD_CAN_NOT_BE_COMBO);
			}
			parent.addChildFood(child);
		}
		
		String sql;
		sql = " DELETE FROM " + Params.dbName + ".combo WHERE food_id = " + parent.getFoodId();
		dbCon.stmt.executeUpdate(sql);
		
		if(parent.hasChildFoods()){
			update(dbCon, staff, new Food.UpdateBuilder(parent.getFoodId()).setCombo(true));
			
			for(ComboFood child : parent.getChildFoods()){
				sql = " INSERT INTO " + Params.dbName + ".combo" +
					  " (`food_id`, `sub_food_id`, `restaurant_id`, `amount`) VALUES(" +
					  parent.getFoodId() + "," +
					  child.asFood().getFoodId() + "," +
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
	 * Get the food and its related information to the specified restaurant and id as below.
	 * 1 - Popular taste to each food
	 * 2 - Child food details to the food in case of combo
	 * @param dbCon
	 * 			the database connection
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
	public static Food getById(DBCon dbCon, Staff staff, int foodId) throws SQLException, BusinessException{
		List<Food> result = FoodDao.getByCond(dbCon, staff, " AND FOOD.food_id = " + foodId, null);
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
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond4Combo}
	 * @return	a food list containing the child foods
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<ComboFood> getComboByCond(DBCon dbCon, Staff staff, ExtraCond4Combo extraCond) throws SQLException{
		
		List<ComboFood> result = new ArrayList<ComboFood>();
		
		String sql;

		sql = " SELECT sub_food_id FROM " + Params.dbName + ".combo WHERE 1 = 1 " + extraCond;
		
		for(Food subFood : getPureByCond(dbCon, staff, " AND FOOD.food_id IN (" + sql + ")", null)){
			
			sql = " SELECT amount FROM " + Params.dbName + ".combo WHERE food_id = " + extraCond.parentId + " AND sub_food_id = " + subFood.getFoodId();
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			int amount;
			if(dbCon.rs.next()){
				amount = dbCon.rs.getInt("amount");
			}else{
				amount = 0;
			}
			dbCon.rs.close();
			
			result.add(new ComboFood(subFood, amount));
		}
		
		return result;
				
	}

	/**
	 * Get the child foods to a specific parent food.
	 * @param parentId
	 * 			the id to parent food
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond4Combo}
	 * @return	a food list containing the child foods
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<ComboFood> getComboByCond(Staff staff, ExtraCond4Combo extraCond) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getComboByCond(dbCon, staff, extraCond);
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
					 " FOOD.name, FOOD.price, FOOD.commission, FOOD.status, " +
					 " FOOD.desc, FOOD.img, " +
					 " FOOD.order_amount, " +
					 " KITCHEN.kitchen_id, KITCHEN.display_id AS kitchen_display_id, KITCHEN.name AS kitchen_name, " +
					 " KITCHEN.type AS kitchen_type , KITCHEN.is_allow_temp AS is_allow_temp, " +
					 " DEPT.dept_id, DEPT.name AS dept_name, DEPT.type AS dept_type, DEPT.display_id AS dept_display_id " +
					 " FROM " + 
					 Params.dbName + ".food FOOD " +
					 " JOIN " +
					 Params.dbName + ".kitchen KITCHEN " +
					 " ON FOOD.kitchen_id = KITCHEN.kitchen_id " +
					 " JOIN " +
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
			
			f.setPrice(dbCon.rs.getFloat("price"));
			f.setCommission(dbCon.rs.getFloat("commission"));
			f.setStatistics(new FoodStatistics(dbCon.rs.getInt("order_amount")));
			f.setStatus(dbCon.rs.getShort("status"));
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
	 * Query the foods to the specified restaurant defined in staff {@link Staff} according to extra condition.
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
	 * Get the food and its related information to the specified restaurant defined in staff {@link Staff} as below.
	 * 1 - Popular taste to each food
	 * 2 - Child food details to the food in case of combo
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition to SQL statement
	 * @param order clause
	 * 			the order clause to SQL statement
	 * @return	an array result
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 */			
	public static List<Food> getByCond(DBCon dbCon, Staff staff, String extraCond, String orderClause) throws SQLException{

		//Using link hash map to keep original order after retrieving the foods by order clause defined in SQL statement.
		Map<Integer, Food> foods = new LinkedHashMap<Integer, Food>();
		
		if(orderClause == null){
			orderClause = " ORDER BY FOOD.food_alias, FOOD.food_id ";
		}
		//Get the basic detail to each food.
		List<Food> pureFoods = getPureByCond(dbCon, staff, extraCond, orderClause);

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
				  " JOIN " + Params.dbName + ".taste TASTE " +
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
				entry.getValue().setChildFoods(getComboByCond(dbCon, staff, new ExtraCond4Combo(entry.getValue().getFoodId())));
			}
		}
		
		return new ArrayList<Food>(foods.values());
		
	}

	/**
	 * Get the food and its related information to the specified restaurant defined in staff {@link Staff} as below.
	 * 1 - Popular taste to each food
	 * 2 - Child food details to the food in case of combo
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
	public static List<Food> getByCond(Staff staff, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the food which not be the good
	 * @param staff
	 * @param extraCond
	 * @param otherClause
	 * @return
	 * @throws SQLException
	 */
	public static List<Food> selectToBeFood(Staff staff, String extraCond, String otherClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return selectToBeFood(dbCon, staff, extraCond, otherClause);
		}finally{
			dbCon.disconnect();
		}
		
	}
	
	public static List<Food> selectToBeFood(DBCon dbCon, Staff staff, String extraCond, String otherClause) throws SQLException{
		List<Food> foods = new ArrayList<>();
		
		String sql = "SELECT F.food_id, F.name FROM " + Params.dbName + ".food F WHERE F.restaurant_id = " + staff.getRestaurantId()
					+  (extraCond == null ? "" : extraCond) + " " 
					+ " AND F.food_id  NOT IN (SELECT FM.food_id FROM " + Params.dbName + ".food_material FM WHERE FM.restaurant_id = " + staff.getRestaurantId() 
					+ " AND FM.food_id = F.food_id)";
		
		try{
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			
			while (dbCon.rs != null && dbCon.rs.next()) {
				Food f = new Food(dbCon.rs.getInt("food_id"));
				f.setName(dbCon.rs.getString("name"));
				foods.add(f);
			}
		}catch(SQLException e){
			e.printStackTrace();
			throw new SQLException("Failed to get the food");
		}
		return foods;
	}	
	
	/**
	 * Get the food content By materialId
	 * @param staff
	 * @param foodId
	 * @return
	 * @throws SQLException
	 */
	public static Food relativeToFood(Staff staff, int materialId) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return relativeToFood(dbCon, staff, materialId);
		}finally{
			dbCon.disconnect();
		}
		
	}	
	
	public static Food relativeToFood(DBCon dbCon, Staff staff, int materialId) throws SQLException{
		Food food = null;
		
		String sql = "SELECT FM.food_id,  F.name AS foodName, F.kitchen_id, K.name AS kitchenName FROM food_material FM "
					+ " JOIN food F ON FM.food_id = F.food_id " 
					+ " JOIN kitchen K ON K.kitchen_id = F.kitchen_id " 
					+ " WHERE FM.restaurant_id = " + staff.getRestaurantId() 
					+ " AND FM.material_id = " + materialId;
		
		try{
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			
			while (dbCon.rs != null && dbCon.rs.next()) {
				food = new Food(dbCon.rs.getInt("food_id"));
				food.setName(dbCon.rs.getString("foodName"));
				Kitchen k = new Kitchen(dbCon.rs.getInt("kitchen_id"));
				k.setName(dbCon.rs.getString("kitchenName"));
				food.setKitchen(k);
			}
		}catch(SQLException e){
			e.printStackTrace();
			throw new SQLException("Failed to get the food");
		}
		return food;
	}	
	
	
	
}
