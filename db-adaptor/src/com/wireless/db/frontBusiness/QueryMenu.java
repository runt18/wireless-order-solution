package com.wireless.db.frontBusiness;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.crMgr.CancelReasonDao;
import com.wireless.db.deptMgr.DepartmentDao;
import com.wireless.db.deptMgr.KitchenDao;
import com.wireless.db.distMgr.DiscountDao;
import com.wireless.db.tasteMgr.TasteDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.ppMgr.PricePlan;
import com.wireless.pojo.tasteMgr.Taste;
import com.wireless.protocol.Food;
import com.wireless.protocol.FoodMenu;
import com.wireless.protocol.FoodStatistics;
import com.wireless.protocol.Terminal;

public class QueryMenu {

	/**
	 * Get the food menu according to the specific restaurant.
	 * @param mRestaurantID
	 * 			The restaurant id.
	 * @return the food menu
	 * @throws SQLException
	 * 			Throws if fail to execute any SQL statement.
	 */
	public static FoodMenu exec(Terminal term) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, term);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the food menu according to the specific restaurant.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon
	 * 			The database connection.
	 * @param term
	 * 			The terminal to query.
	 * @return the food menu
	 * @throws SQLException
	 * 			Throws if fail to execute any SQL statement.
	 */
	public static FoodMenu exec(DBCon dbCon, Terminal term) throws SQLException{
		return new FoodMenu(queryFoods(dbCon, "AND FOOD.restaurant_id=" + term.restaurantID, null), 
			    			queryTastes(dbCon, term, Taste.Category.TASTE, null, null),
			    			queryTastes(dbCon, term, Taste.Category.STYLE, null, null),
			    			queryTastes(dbCon, term, Taste.Category.SPEC, null, null),
			    			queryKitchens(dbCon, term, " AND KITCHEN.type=" + Kitchen.Type.NORMAL.getVal(), null),
			    			queryDepartments(dbCon, term, " AND DEPT.type=" + Department.Type.NORMAL.getVal(), null),
			    			queryDiscounts(dbCon, term, null, null),
			    			queryCancelReasons(dbCon, term, null, null));
	}
	
	/**
	 * Get the food menu including three information below.<br>
	 * - Food<br>
	 * - Taste<br>
	 * - Kitchen
	 * @param pin the pin to this terminal
	 * @param model the model to this terminal
	 * @return the food menu holding all the information
	 * @throws BusinessException throws if either of cases below.<br>
	 * 							 - The terminal is NOT attache to any restaurant.<br>
	 * 							 - The terminal is expired.
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static FoodMenu exec(long pin, short model) throws BusinessException, SQLException{
		
		DBCon dbCon = new DBCon();
		
		try{
			dbCon.connect();
			
			return exec(dbCon, VerifyPin.exec(dbCon, pin, model));
			
		}finally{
			dbCon.disconnect();
		}
	}

	/**
	 * Query the food information according to the food table. 
	 * @param extraCondition
	 * 			the extra condition to SQL statement
	 * @param orderClause
	 * 			the order clause to SQL statement
	 * @return an array result
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 */
	public static Food[] queryPureFoods(String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return queryPureFoods(dbCon, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Query the food information according to the food table. 
	 * Note that the data base should be connected before invoking this method.
	 * @param dbCon
	 * 			the database connection
	 * @param extraCondition
	 * 			the extra condition to SQL statement
	 * @param orderClause
	 * 			the order clause to SQL statement
	 * @return an array result
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 */
	public static Food[] queryPureFoods(DBCon dbCon, String extraCondition, String orderClause) throws SQLException{
		ArrayList<Food> foods = new ArrayList<Food>();
        //get all the food information to this restaurant
		String sql = " SELECT " +
					 " FOOD.restaurant_id, FOOD.food_id, FOOD.food_alias, FOOD.stock_status, " +
					 " FOOD.name, FPP.unit_price, FOOD.kitchen_alias, FOOD.status, FOOD.pinyin, FOOD.taste_ref_type, " +
					 " FOOD.desc, FOOD.img, " +
					 " FOOD_STATISTICS.order_cnt, " +
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
					 Params.dbName + ".food_statistics FOOD_STATISTICS " +
					 " ON FOOD.food_id = FOOD_STATISTICS.food_id " +
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

			long foodID = dbCon.rs.getLong("food_id");
			int restaurantID = dbCon.rs.getInt("restaurant_id");
			
			foods.add(new Food(restaurantID,
	 				   		   foodID,
	 				   		   dbCon.rs.getInt("food_alias"),
	 				   		   dbCon.rs.getString("name"),
	 				   		   dbCon.rs.getFloat("unit_price"),
	 				   		   new FoodStatistics(dbCon.rs.getInt("order_cnt")),
	 				   		   dbCon.rs.getShort("status"),
	 				   		   dbCon.rs.getString("pinyin"),
	 				   		   null,
	 				   		   dbCon.rs.getShort("taste_ref_type"),
	 				   		   dbCon.rs.getString("desc"),
	 				   		   dbCon.rs.getString("img"),
	 				   		   new Kitchen.Builder(dbCon.rs.getShort("kitchen_alias"), 
	 				   				   			   dbCon.rs.getString("kitchen_name"), 
	 				   				   			   restaurantID)
										.setAllowTemp(dbCon.rs.getBoolean("is_allow_temp"))
										.setKitchenId(dbCon.rs.getLong("kitchen_id"))
										.setType(dbCon.rs.getShort("kitchen_type"))
										.setDept(new Department(dbCon.rs.getString("dept_name"), 
	 				   				    		   		  	    dbCon.rs.getShort("dept_id"), 
	 				   				    		   		  	    restaurantID,
	 				   				    		   		  	    Department.Type.valueOf(dbCon.rs.getShort("dept_type")))).build(),
	 				   		   Food.StockStatus.valueOf(dbCon.rs.getInt("stock_status"))));
//	 				   		   new Kitchen(restaurantID, 
//	 				   				       dbCon.rs.getString("kitchen_name"),
//	 				   				       dbCon.rs.getLong("kitchen_id"),
//	 				   				       dbCon.rs.getShort("kitchen_alias"),
//	 				   				       dbCon.rs.getBoolean("is_allow_temp"),
//	 				   				       dbCon.rs.getShort("kitchen_type"),
//	 				   				       new Department(dbCon.rs.getString("dept_name"), 
//	 				   				    		   		  dbCon.rs.getShort("dept_id"), 
//	 				   				    		   		  restaurantID,
//	 				   				    		   		  dbCon.rs.getShort("dept_type")))));
		}
		
		
		return foods.toArray(new Food[foods.size()]);
	}
	
	/**
	 * Query the food and its related information listed below.
	 * 1 - Popular taste to each food
	 * 2 - Child food details to the food in case of combo
	 * Since hash map would destroy the original order,
	 * using a comparator to sort the result, instead of an order clause in SQL statement, 
	 * @param dbCon
	 * 			the database connection
	 * @param extraCondition
	 * 			the extra condition to SQL statement
	 * @param foodComp
	 * 			the extra comparator to sort the result
	 * @return	an array result
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 */
	public static Food[] queryFoods(String extraCond, Comparator<Food> foodComp) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return queryFoods(dbCon, extraCond, foodComp);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Query the food and its related information listed below.
	 * 1 - Popular taste to each food
	 * 2 - Child food details to the food in case of combo
	 * Since hash map might destroy the original order,
	 * using a comparator to sort the result, instead of an order clause in SQL statement. 
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon
	 * 			the database connection
	 * @param extraCondition
	 * 			the extra condition to SQL statement
	 * @param foodComp
	 * 			the extra comparator to sort the result
	 * @return	an array result
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 */			
	public static Food[] queryFoods(final DBCon dbCon, String extraCondition, Comparator<Food> foodComp) throws SQLException{
		
		LinkedHashMap<Long, Map.Entry<Food, List<Taste>>> foodTasteMap = new LinkedHashMap<Long, Map.Entry<Food, List<Taste>>>();
		
        //get all the food information to this restaurant
		String sql = " SELECT " +
					 " FOOD.restaurant_id, FOOD.food_id, FOOD.food_alias, FOOD.stock_status, " +
					 " FOOD.name, FPP.unit_price, FOOD.status, FOOD.pinyin, FOOD.taste_ref_type, " +
					 " FOOD.desc, FOOD.img, " +
					 " FOOD_STATISTICS.order_cnt, " +
					 " KITCHEN.kitchen_id, KITCHEN.kitchen_alias, KITCHEN.name AS kitchen_name, " +
					 " KITCHEN.type AS kitchen_type, KITCHEN.is_allow_temp AS is_allow_temp, " +
					 " DEPT.dept_id, DEPT.name AS dept_name, DEPT.type AS dept_type, " +
					 " TASTE.taste_id, TASTE.taste_alias " +
					 " FROM " + 
					 Params.dbName + ".food FOOD " +
					 " INNER JOIN " + Params.dbName + ".price_plan PP " +
					 " ON FOOD.restaurant_id = PP.restaurant_id AND PP.status = " + PricePlan.Status.ACTIVITY.getVal() +
					 " INNER JOIN " + Params.dbName + ".food_price_plan FPP " +
					 " ON PP.price_plan_id = FPP.price_plan_id AND FOOD.food_id = FPP.food_id " +
					 " LEFT OUTER JOIN " +
					 Params.dbName + ".food_statistics FOOD_STATISTICS " +
					 " ON FOOD.food_id = FOOD_STATISTICS.food_id " +
					 " LEFT OUTER JOIN " +
					 Params.dbName + ".kitchen KITCHEN " +
					 " ON FOOD.kitchen_id = KITCHEN.kitchen_id " +
					 " LEFT OUTER JOIN " +
					 Params.dbName + ".department DEPT " +
					 " ON KITCHEN.dept_id = DEPT.dept_id AND KITCHEN.restaurant_id = DEPT.restaurant_id " +
					 " LEFT OUTER JOIN " +
					 Params.dbName + ".food_taste_rank FTR " +
					 " ON FOOD.food_id = FTR.food_id " +
					 " LEFT OUTER JOIN " +
					 Params.dbName + ".taste TASTE " +
					 " ON TASTE.taste_id = FTR.taste_id " +
					 " WHERE 1=1 " +
					 (extraCondition == null ? "" : extraCondition) + " " +
					 "ORDER BY FOOD.food_alias, FTR.rank"; 
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		while(dbCon.rs.next()){
			
			long foodID = dbCon.rs.getLong("food_id");
			int restaurantID = dbCon.rs.getInt("restaurant_id");
			
			Entry<Food, List<Taste>> entry = foodTasteMap.get(foodID);
			if(entry != null){
				entry.getValue().add(new Taste(dbCon.rs.getInt("taste_id"),
											   dbCon.rs.getInt("taste_alias"),
											   restaurantID));
				
				foodTasteMap.put(foodID, entry);
				
			}else{
				final List<Taste> tasteRefs = new ArrayList<Taste>();
				int tasteID = dbCon.rs.getInt("taste_id");
				if(tasteID != 0){
					tasteRefs.add(new Taste(dbCon.rs.getInt("taste_id"),
											dbCon.rs.getInt("taste_alias"),
											restaurantID));
				}
				
				final Food food = new Food(restaurantID,
			 			 				   foodID,
			 			 				   dbCon.rs.getInt("food_alias"),
			 			 				   dbCon.rs.getString("name"),
			 			 				   dbCon.rs.getFloat("unit_price"),
			 			 				   new FoodStatistics(dbCon.rs.getInt("order_cnt")),
			 			 				   dbCon.rs.getShort("status"),
			 			 				   dbCon.rs.getString("pinyin"),
			 			 				   null,
			 			 				   dbCon.rs.getShort("taste_ref_type"),
			 			 				   dbCon.rs.getString("desc"),
			 			 				   dbCon.rs.getString("img"),
			 			 				   new Kitchen.Builder(dbCon.rs.getShort("kitchen_alias"), dbCon.rs.getString("kitchen_name"), restaurantID)
														.setType(dbCon.rs.getShort("kitchen_type"))
														.setKitchenId(dbCon.rs.getLong("kitchen_id"))
														.setAllowTemp(dbCon.rs.getBoolean("is_allow_temp"))
														.setDept(new Department(dbCon.rs.getString("dept_name"), 
			 			 						   			   		  dbCon.rs.getShort("dept_id"), 
			 			 						   			   		  restaurantID,
			 			 						   			   		  Department.Type.valueOf(dbCon.rs.getShort("dept_type")))).build(),
			 			 				   Food.StockStatus.valueOf(dbCon.rs.getInt("stock_status")));
//			 			 				   new Kitchen(restaurantID, 
//			 			 						   	   dbCon.rs.getString("kitchen_name"),
//			 			 						   	   dbCon.rs.getLong("kitchen_id"),
//			 			 						   	   dbCon.rs.getShort("kitchen_alias"),
//			 			 						   	   dbCon.rs.getBoolean("is_allow_temp"),
//			 			 						   	   dbCon.rs.getShort("kitchen_type"),
//			 			 						   	   new Department(dbCon.rs.getString("dept_name"), 
//			 			 						   			   		  dbCon.rs.getShort("dept_id"), 
//			 			 						   			   		  restaurantID,
//			 			 						   			   		  dbCon.rs.getShort("dept_type"))));
				
				foodTasteMap.put(foodID, new Map.Entry<Food, List<Taste>>(){

					private Food mFood = food;
					private List<Taste> mTasteRefs = tasteRefs;
					
					@Override
					public Food getKey() {
						return mFood;
					}

					@Override
					public List<Taste> getValue() {
						return mTasteRefs;
					}

					@Override
					public List<Taste> setValue(List<Taste> value) {
						mTasteRefs = value;
						return mTasteRefs;
					}
					
				});
			}
		}
	
		dbCon.rs.close();
		
		Food[] result = new Food[foodTasteMap.size()];
		int i = 0;
		for(Entry<Food, List<Taste>> entry : foodTasteMap.values()){
			Food food = entry.getKey();
			food.setPopTastes(entry.getValue());
			
			/**
			 * Get the details if the food belongs to combo
			 */
			food.setChildFoods(queryComboByParent(dbCon, food));
			
			result[i++] = food; 
		}
		
		if(foodComp != null){
			Arrays.sort(result, foodComp);
		}
		
		return result;
	}
	
	/**
	 * Get the combo detail to a specific parent food.
	 * @param parent
	 * 			the parent food to query
	 * @return	a food list containing the child foods
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Food> queryComboByParent(Food parent) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return queryComboByParent(dbCon, parent);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the combo detail to a specific parent food.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon
	 * 			the database connection
	 * @param parent
	 * 			the parent food to query
	 * @return	a food list containing the child foods
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Food> queryComboByParent(DBCon dbCon, Food parent) throws SQLException{
		
		List<Food> childFoods = new ArrayList<Food>();
		
		if(parent.isCombo()){
			String sql;
			sql = " SELECT " +
				  " FOOD.restaurant_id, FOOD.food_id, FOOD.food_alias, FOOD.stock_status, " +
				  " FOOD.name, FPP.unit_price, FOOD.status, FOOD.pinyin, FOOD.taste_ref_type, " +
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
					
				long foodID = dbCon.rs.getLong("food_id");
				int restaurantID = dbCon.rs.getInt("restaurant_id");
					
				Food childFood = new Food(restaurantID,
						   				  foodID,
						   				  dbCon.rs.getInt("food_alias"),
						   				  dbCon.rs.getString("name"),
						   				  dbCon.rs.getFloat("unit_price"),
			 			 				  null,
						   				  dbCon.rs.getShort("status"),
						   				  dbCon.rs.getString("pinyin"),
						   				  null,
						   				  dbCon.rs.getShort("taste_ref_type"),
						   				  dbCon.rs.getString("desc"),
						   				  dbCon.rs.getString("img"),
						   				  new Kitchen.Builder(dbCon.rs.getShort("kitchen_alias"), dbCon.rs.getString("kitchen_name"), restaurantID)
													.setKitchenId(dbCon.rs.getLong("kitchen_id"))
													.setAllowTemp(dbCon.rs.getBoolean("is_allow_temp"))
													.setType(dbCon.rs.getShort("kitchen_type"))
													.setDept(new Department(dbCon.rs.getString("dept_name"), 
						   									  		 		dbCon.rs.getShort("dept_id"), 
						   									  		 		restaurantID,
						   									  		 		Department.Type.valueOf(dbCon.rs.getShort("dept_type")))).build(),
						   				  Food.StockStatus.valueOf(dbCon.rs.getInt("stock_status")));
				childFood.setAmount(dbCon.rs.getInt("amount"));
				childFoods.add(childFood);
			}				
			dbCon.rs.close();
			return childFoods;
				
		}else{
			return childFoods;
		}
	}
	
	private static Kitchen[] queryKitchens(DBCon dbCon, Terminal term, String extraCond, String orderClause) throws SQLException{
		//get all the kitchen information to this restaurant,
		List<Kitchen> kitchens = KitchenDao.getKitchens(dbCon, term, extraCond, orderClause);
		
		return kitchens.toArray(new Kitchen[kitchens.size()]);
		
	}
	
	private static Department[] queryDepartments(DBCon dbCon, Terminal term, String extraCond, String orderClause) throws SQLException{
		//get tall the super kitchen information to this restaurant
//		ArrayList<Department> departments = new ArrayList<Department>();
//		String sql = " SELECT dept_id, name, restaurant_id, type FROM " + Params.dbName + ".department DEPT " +
//					 " WHERE 1 = 1 " +
//					 (extraCond != null ? extraCond : "") + " " +
//					 (orderClause != null ? orderClause : "");
//		dbCon.rs = dbCon.stmt.executeQuery(sql);
//		while(dbCon.rs.next()){
//			departments.add(new Department(dbCon.rs.getString("name"),
//									   	   dbCon.rs.getShort("dept_id"),
//									   	   dbCon.rs.getInt("restaurant_id"),
//									   	   Department.Type.valueOf(dbCon.rs.getShort("type"))));
//			
//		}
//		dbCon.rs.close();
		
		List<Department> departments = DepartmentDao.getDepartments(dbCon, term, extraCond, orderClause);
		
		return departments.toArray(new Department[departments.size()]);
	}
	
	/**
	 * Query the specific taste information.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon 
	 * 			the database connection
	 * @param mRestaurantID 
	 * 			the restaurant id
	 * @param category 
	 * 			the category of taste to query, one the values below.<br>
	 * 	 	    - Taste.CATE_TASTE
	 *          - Taste.CATE_STYLE
	 *          - Taste.CATE_SPEC
	 * @return the taste information
	 * @throws SQLException 
	 * 			throws if fail to execute any SQL statement
	 */
	private static Taste[] queryTastes(DBCon dbCon, Terminal term, Taste.Category category, String extraCond, String orderClause) throws SQLException{

		List<Taste> tastes = TasteDao.getTasteByCategory(dbCon, term, category);
		
		return tastes.toArray(new Taste[tastes.size()]);
	}
	
	/**
	 * Get the discount and corresponding plan detail, along with the kitchen details.
	 * Note that the database should be connected before connected.
	 * @param dbCon
	 * 			The database connection.
	 * @param extraCond
	 * 			The extra condition.
	 * @param orderClause
	 * 			The order clause.
	 * @return The array holding the discount info matching the condition. 
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statement.
	 */
	private static Discount[] queryDiscounts(DBCon dbCon, Terminal term, String extraCond, String orderClause) throws SQLException{
//		String sql;
//		sql = " SELECT " +
//			  " DIST.discount_id, DIST.restaurant_id, DIST.name AS dist_name, DIST.level, DIST.status AS dist_status, " +
//			  " DIST_PLAN.dist_plan_id, DIST_PLAN.kitchen_id, DIST_PLAN.rate, " +
//			  " KITCHEN.name AS kitchen_name, KITCHEN.kitchen_alias, " +
//			  " CASE WHEN DIST_PLAN.discount_id IS NULL THEN '0' ELSE '1' END AS has_plan " +
//			  " FROM " + 
//			  Params.dbName + ".discount DIST " +
//			  " LEFT JOIN " +
//			  Params.dbName + ".discount_plan DIST_PLAN " +
//			  " ON DIST_PLAN.discount_id = DIST.discount_id " +
//			  " LEFT JOIN " +
//			  Params.dbName + ".kitchen KITCHEN " +
//			  " ON DIST_PLAN.kitchen_id = KITCHEN.kitchen_id " +
//			  " WHERE 1=1 " +
//			  (extraCond == null ? "" : extraCond) + " " +
//			  (orderClause == null ? "" : orderClause);
//		dbCon.rs = dbCon.stmt.executeQuery(sql);
//		
//		LinkedHashMap<PDiscount, List<PDiscountPlan>> discounts = new LinkedHashMap<PDiscount, List<PDiscountPlan>>();
//		
//		while(dbCon.rs.next()){
//			PDiscount discount = new PDiscount(dbCon.rs.getInt("discount_id"));
//			discount.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
//			discount.setName(dbCon.rs.getString("dist_name"));
//			discount.setLevel(dbCon.rs.getShort("level"));
//			discount.setStatus(dbCon.rs.getInt("dist_status"));
//
//			Kitchen kitchen = new Kitchen();
//			kitchen.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
//			kitchen.setId(dbCon.rs.getInt("kitchen_id"));
//			kitchen.setAliasId(dbCon.rs.getShort("kitchen_alias"));
//			kitchen.setName(dbCon.rs.getString("kitchen_name"));
//			
//			List<PDiscountPlan> plans = discounts.get(discount);
//			if(plans == null){				
//				plans = new LinkedList<PDiscountPlan>();
//			}
//			
//			float rate = dbCon.rs.getFloat("rate");
//			if(dbCon.rs.getBoolean("has_plan") && rate != 1){
//				plans.add(new PDiscountPlan(kitchen, rate));
//			}
//			discounts.put(discount, plans);
//		}
//		
//		for(Map.Entry<PDiscount, List<PDiscountPlan>> entry : discounts.entrySet()){
//			entry.getKey().setPlans(entry.getValue().toArray(new PDiscountPlan[entry.getValue().size()]));
//		}
//		
//		return discounts.keySet().toArray(new PDiscount[discounts.size()]);		
		List<Discount> discounts = DiscountDao.getDiscount(dbCon, term, extraCond, orderClause);
		return discounts.toArray(new Discount[discounts.size()]);
	}
	
	/**
	 * Get the cancel reason according to specific condition and order clause
	 * Note that the database should be connected before connected.
	 * @param dbCon
	 * 			The database connection.
	 * @param extraCond
	 * 			The extra condition.
	 * @param orderClause
	 * 			The order clause.
	 * @return The array holding the cancel reasons. 
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statement.
	 */
	private static CancelReason[] queryCancelReasons(DBCon dbCon, Terminal term, String extraCond, String orderClause) throws SQLException{
		List<CancelReason> result = CancelReasonDao.getReasons(dbCon, term, extraCond, orderClause);
		return result.toArray(new CancelReason[result.size()]);
	}
	
}
