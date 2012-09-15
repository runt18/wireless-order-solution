package com.wireless.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.wireless.exception.BusinessException;
import com.wireless.protocol.Department;
import com.wireless.protocol.Food;
import com.wireless.protocol.FoodMenu;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.Taste;
import com.wireless.protocol.Terminal;

public class QueryMenu {

	/**
	 * Get the food menu according to the specific restaurant.
	 * @param restaurantID
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
			    			queryTastes(dbCon, Taste.CATE_TASTE, "AND restaurant_id=" + term.restaurantID, null),
			    			queryTastes(dbCon, Taste.CATE_STYLE, "AND restaurant_id=" + term.restaurantID, null),
			    			queryTastes(dbCon, Taste.CATE_SPEC, "AND restaurant_id=" + term.restaurantID, null),
			    			queryKitchens(dbCon, "AND KITCHEN.restaurant_id=" + term.restaurantID, null),
			    			queryDepartments(dbCon, "AND DEPT.restaurant_id=" + term.restaurantID, null));
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
					 " FOOD.restaurant_id, FOOD.food_id, FOOD.food_alias, " +
					 " FOOD.name, FOOD.unit_price, FOOD.kitchen_alias, FOOD.status, FOOD.pinyin, FOOD.taste_ref_type, " +
					 " FOOD.desc, FOOD.img, " +
					 " KITCHEN.dept_id, KITCHEN.kitchen_id, KITCHEN.kitchen_alias, KITCHEN.name AS kitchen_name " +
					 " FROM " + 
					 Params.dbName + ".food FOOD " +
					 " LEFT OUTER JOIN " +
					 Params.dbName + ".kitchen KITCHEN " +
					 " ON FOOD.kitchen_id = KITCHEN.kitchen_id " +
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
	 				   		   dbCon.rs.getShort("status"),
	 				   		   dbCon.rs.getString("pinyin"),
	 				   		   dbCon.rs.getShort("taste_ref_type"),
	 				   		   dbCon.rs.getString("desc"),
	 				   		   dbCon.rs.getString("img"),
	 				   		   new Kitchen(restaurantID, 
	 				   				       dbCon.rs.getString("kitchen_name"),
	 				   				       dbCon.rs.getLong("kitchen_id"),
	 				   				       dbCon.rs.getShort("kitchen_alias"),
	 				   				       new Department(null, dbCon.rs.getShort("dept_id"), restaurantID))));
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
					 " FOOD.restaurant_id, FOOD.food_id, FOOD.food_alias, " +
					 " FOOD.name, FOOD.unit_price, FOOD.status, FOOD.pinyin, FOOD.taste_ref_type, " +
					 " FOOD.desc, FOOD.img, " +
					 " KITCHEN.dept_id, KITCHEN.kitchen_id, KITCHEN.kitchen_alias, KITCHEN.name AS kitchen_name, " +
					 " TASTE.taste_id, TASTE.taste_alias " +
					 " FROM " + 
					 Params.dbName + ".food FOOD " +
					 " LEFT OUTER JOIN " +
					 Params.dbName + ".kitchen KITCHEN " +
					 " ON FOOD.kitchen_id = KITCHEN.kitchen_id " +
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
			 			 				   dbCon.rs.getShort("status"),
			 			 				   dbCon.rs.getString("pinyin"),
			 			 				   dbCon.rs.getShort("taste_ref_type"),
			 			 				   dbCon.rs.getString("desc"),
			 			 				   dbCon.rs.getString("img"),
			 			 				   new Kitchen(restaurantID, 
			 			 						   	   dbCon.rs.getString("kitchen_name"),
			 			 						   	   dbCon.rs.getLong("kitchen_id"),
			 			 						   	   dbCon.rs.getShort("kitchen_alias"),
			 			 						   	   new Department(null, dbCon.rs.getShort("dept_id"), restaurantID)));
				
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
			List<Taste> tasteRefs = entry.getValue();
			food.popTastes = tasteRefs.toArray(new Taste[tasteRefs.size()]);
			
			/**
			 * Get the details if the food belongs to combo
			 */
			food.childFoods = queryComboByParent(dbCon, food);
			
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
	 * @return	Return a food array containing the child foods.
	 * 			Return null if the parent if NOT combo.
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static Food[] queryComboByParent(Food parent) throws SQLException{
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
	 * @return	Return a food array containing the child foods.
	 * 			Return null if the parent if NOT combo.
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static Food[] queryComboByParent(DBCon dbCon, Food parent) throws SQLException{
		if(parent.isCombo()){
			String sql;
			sql = " SELECT " +
				  " FOOD.restaurant_id, FOOD.food_id, FOOD.food_alias, " +
				  " FOOD.name, FOOD.unit_price, FOOD.status, FOOD.pinyin, FOOD.taste_ref_type, " +
				  " FOOD.desc, FOOD.img, " +
				  " KITCHEN.dept_id, KITCHEN.kitchen_id, KITCHEN.kitchen_alias, KITCHEN.name AS kitchen_name, " +
				  " COMBO.amount " +
				  " FROM " +
				  Params.dbName + ".food FOOD " + 
				  " INNER JOIN " +
				  Params.dbName + ".combo COMBO " +
				  " ON FOOD.food_id = COMBO.sub_food_id " + 
				  " LEFT OUTER JOIN " +
				  Params.dbName + ".kitchen KITCHEN " +
				  " ON FOOD.kitchen_id = KITCHEN.kitchen_id " +
				  " WHERE COMBO.food_id = " + parent.foodID;
				
			dbCon.rs = dbCon.stmt.executeQuery(sql);
				
			ArrayList<Food> childFoods = new ArrayList<Food>();
			while(dbCon.rs.next()){
					
				long foodID = dbCon.rs.getLong("food_id");
				int restaurantID = dbCon.rs.getInt("restaurant_id");
					
				Food childFood = new Food(restaurantID,
						   				  foodID,
						   				  dbCon.rs.getInt("food_alias"),
						   				  dbCon.rs.getString("name"),
						   				  dbCon.rs.getFloat("unit_price"),
						   				  dbCon.rs.getShort("status"),
						   				  dbCon.rs.getString("pinyin"),
						   				  dbCon.rs.getShort("taste_ref_type"),
						   				  dbCon.rs.getString("desc"),
						   				  dbCon.rs.getString("img"),
						   				  new Kitchen(restaurantID, 
						   							  dbCon.rs.getString("kitchen_name"),
						   							  dbCon.rs.getLong("kitchen_id"),
						   							  dbCon.rs.getShort("kitchen_alias"),
						   							  new Department(null, dbCon.rs.getShort("dept_id"), restaurantID)));
				childFood.amount = dbCon.rs.getInt("amount");
				childFoods.add(childFood);
			}				
			dbCon.rs.close();
			return childFoods.toArray(new Food[childFoods.size()]);
				
		}else{
			return null;
		}
	}
	

	
	public static Kitchen[] queryKitchens(String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return queryKitchens(dbCon, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static Kitchen[] queryKitchens(DBCon dbCon, String extraCond, String orderClause) throws SQLException{
		//get all the kitchen information to this restaurant,
		ArrayList<Kitchen> kitchens = new ArrayList<Kitchen>();
		String sql = " SELECT restaurant_id, kitchen_id, kitchen_alias, name, discount, discount_2, discount_3, " +
					 " member_discount_1, member_discount_2, member_discount_3, " +
					 " dept_id FROM " + 
			  		 Params.dbName + ".kitchen KITCHEN " +
			  		 " WHERE 1=1 " + 
			  		 (extraCond == null ? "" : extraCond) + " " +
			  		 (orderClause == null ? "" : orderClause);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			kitchens.add(new Kitchen(dbCon.rs.getInt("restaurant_id"),
									 dbCon.rs.getString("name"),
									 dbCon.rs.getLong("kitchen_id"),
									 dbCon.rs.getShort("kitchen_alias"),
									 new Department("", dbCon.rs.getShort("dept_id"), 0),
									 (byte)(dbCon.rs.getFloat("discount") * 100),
									 (byte)(dbCon.rs.getFloat("discount_2") * 100),
									 (byte)(dbCon.rs.getFloat("discount_3") * 100),
									 (byte)(dbCon.rs.getFloat("member_discount_1") * 100),
									 (byte)(dbCon.rs.getFloat("member_discount_2") * 100),
									 (byte)(dbCon.rs.getFloat("member_discount_3") * 100)));
		}
		dbCon.rs.close();
		
		return kitchens.toArray(new Kitchen[kitchens.size()]);
	}
	
	public static Department[] queryDepartments(DBCon dbCon, String extraCond, String orderClause) throws SQLException{
		//get tall the super kitchen information to this restaurant
		ArrayList<Department> departments = new ArrayList<Department>();
		String sql = " SELECT dept_id, name, restaurant_id FROM " + Params.dbName + ".department DEPT " +
					 " WHERE 1=1 " +
					 (extraCond != null ? extraCond : "") + " " +
					 (orderClause != null ? orderClause : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			departments.add(new Department(dbCon.rs.getString("name"),
									   	   dbCon.rs.getShort("dept_id"),
									   	   dbCon.rs.getInt("restaurant_id")));
		}
		dbCon.rs.close();
		
		return departments.toArray(new Department[departments.size()]);
	}
	
	public static Taste[] queryTastes(short category, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return queryTastes(dbCon, category, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Query the specific taste information.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon 
	 * 			the database connection
	 * @param restaurantID 
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
	public static Taste[] queryTastes(DBCon dbCon, short category, String extraCond, String orderClause) throws SQLException{

		String sql = " SELECT " +
					 " taste_id, taste_alias, restaurant_id, preference, " +
					 " category, calc, rate, price, type " +
					 " FROM " + 
					 Params.dbName + ".taste " +
				     " WHERE 1=1 " +
					 (category == Taste.CATE_ALL ? "" : " AND category=" + category) + " " +
					 (extraCond == null ? "" : extraCond) + " " +
					 (orderClause == null ? "" : orderClause);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		ArrayList<Taste> tastes = new ArrayList<Taste>();
		while(dbCon.rs.next()){
			Taste taste = new Taste(dbCon.rs.getInt("taste_id"),
								    dbCon.rs.getInt("taste_alias"), 
								    dbCon.rs.getInt("restaurant_id"),
									dbCon.rs.getString("preference"),
									dbCon.rs.getShort("category"),
									dbCon.rs.getShort("calc"),
									new Float(dbCon.rs.getFloat("rate")),
									new Float(dbCon.rs.getFloat("price")),
									dbCon.rs.getShort("type"));
			tastes.add(taste);
		}
		dbCon.rs.close();
		
		return tastes.toArray(new Taste[tastes.size()]);
	}
	
}
