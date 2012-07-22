package com.wireless.db.tasteRef;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.QueryMenu;
import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;

public class TasteRef {
	
	private final static String TASTE_ID = "$(taste_id)";
	private final static String FOOD_ID = "$(food_id)";
	private final static String RESTAURANT_ID = "$(restaurant_id)";
	private final static String REF_COUNT = "$(ref_cnt)";
	
	private final static String QUERY_TASTE_SQL = 
			" SELECT ORDER_FOOD.taste_id, COUNT(ORDER_FOOD.taste_id) AS ref_cnt " +
			" FROM " +
			" (SELECT " + TASTE_ID + " AS taste_id, SUM(order_count) AS order_sum FROM " +
			Params.dbName + ".order_food_history " +
			" WHERE " +
			" food_id = " + FOOD_ID +
			" GROUP BY order_id, food_id, taste_id, taste2_id, taste3_id, taste_tmp_alias, is_temporary " +
			" HAVING order_sum > 0 ) AS ORDER_FOOD " +
			" WHERE ORDER_FOOD.taste_id IS NOT NULL " +
			" GROUP BY ORDER_FOOD.taste_id " +
			" HAVING ref_cnt > 0 ";
	
	private final static String INSERT_FOOD_TASTE_SQL = 
			" INSERT INTO " + Params.dbName + ".food_taste" +
			" (food_id, taste_id, restaurant_id, ref_cnt)" +
			" VALUES(" +
			FOOD_ID + ", " +
			TASTE_ID + ", " +
			RESTAURANT_ID + ", " +
			REF_COUNT + ")";
	
	/**
	 * Update the taste reference count to a specific food.
	 * @param food
	 * 			the food to be queried
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void execByFood(Food food) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			execByFood(dbCon, food);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the taste reference count to a specific food.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon
	 * 			the database connection
	 * @param food
	 * 			the food to be queried
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void execByFood(DBCon dbCon, Food food) throws SQLException{
		/**
		 * Get all taste reference record to this food.
		 */
		HashSet<TasteRefCnt> tasteRefByFood = getFoodTaste(dbCon, food);
		
		String sql;
		
		/**
		 * Delete the original taste reference record to this food.
		 */
		sql = " DELETE FROM " + Params.dbName + ".food_taste " +
			  " WHERE food_id=" + food.foodID;
		dbCon.stmt.executeUpdate(sql);
		
		/**
		 * Update the new taste reference record to this food
		 */
		dbCon.stmt.clearBatch();
		for(TasteRefCnt t : tasteRefByFood){
			sql = INSERT_FOOD_TASTE_SQL.replace(FOOD_ID, Long.toString(food.foodID))
			 						   .replace(TASTE_ID, Integer.toString(t.tasteID))
									   .replace(RESTAURANT_ID, Integer.toString(food.restaurantID))
									   .replace(REF_COUNT, Integer.toString(t.refCnt));
			dbCon.stmt.addBatch(sql);
		}
		dbCon.stmt.executeBatch();
	}
	
	/**
	 * Calculate the taste reference count to all the foods with smart taste reference.
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */	
	public static void exec() throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			exec(dbCon);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the taste reference count to all the foods with smart taste reference.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon
	 * 			the database connection
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void exec(DBCon dbCon) throws SQLException{
		/**
		 * Get all the foods whose taste reference type is smart
		 */
		Food[] foods = QueryMenu.queryFoods("AND FOOD.taste_ref_type=" + Food.TASTE_SMART_REF, null);
		
		/**
		 * The hash map to store the taste reference count to all the foods
		 */
		HashMap<Food, HashSet<TasteRefCnt>> foodTasteRef = new HashMap<Food, HashSet<TasteRefCnt>>();
		
		for(Food food : foods){

			HashSet<TasteRefCnt> tasteRefByFood = getFoodTaste(dbCon, food);
			
			foodTasteRef.put(food, tasteRefByFood);		

		}	
		
		/**
		 * Write the food taste to db & update the kitchen and department taste
		 */
		storeFoodTaste(dbCon, foodTasteRef);	
	
		
	}
	
	/**
	 * Calculate the taste reference count to a specific food.
	 * @param dbCon
	 * 			the database connection
	 * @param food
	 * 			the food to be queried
	 * @return
	 * 			the hash set holding the taste reference count to this food
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	private static HashSet<TasteRefCnt> getFoodTaste(DBCon dbCon, Food food) throws SQLException{
		
		String sqlTmp = QUERY_TASTE_SQL.replace(FOOD_ID, Long.toString(food.foodID));
		
		HashMap<Integer, Integer> tasteRef = new HashMap<Integer, Integer>();
		
		/**
		 * Get the reference count to taste_id
		 */
		String sql = sqlTmp.replace(TASTE_ID, "taste_id");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			int tasteID = dbCon.rs.getInt("taste_id");
			int refCnt = dbCon.rs.getInt("ref_cnt");
			Integer oriRefCnt = tasteRef.get(tasteID);
			if(oriRefCnt != null){
				refCnt += oriRefCnt;
				tasteRef.put(tasteID, refCnt);
			}else{
				tasteRef.put(tasteID, refCnt);
			}
		}
		dbCon.rs.close();
		
		/**
		 * Get the reference count to taste2_id
		 */
		sql = sqlTmp.replace(TASTE_ID, "taste2_id");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			int tasteID = dbCon.rs.getInt("taste_id");
			int refCnt = dbCon.rs.getInt("ref_cnt");
			Integer oriRefCnt = tasteRef.get(tasteID);
			if(oriRefCnt != null){
				refCnt += oriRefCnt;
				tasteRef.put(tasteID, refCnt);
			}else{
				tasteRef.put(tasteID, refCnt);
			}
		}
		dbCon.rs.close();
		
		/**
		 * Get the reference count to taste3_id
		 */
		sql = sqlTmp.replace(TASTE_ID, "taste3_id");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			int tasteID = dbCon.rs.getInt("taste_id");
			int refCnt = dbCon.rs.getInt("ref_cnt");
			Integer oriRefCnt = tasteRef.get(tasteID);
			if(oriRefCnt != null){
				refCnt += oriRefCnt;
				tasteRef.put(tasteID, refCnt);
			}else{
				tasteRef.put(tasteID, refCnt);
			}
		}
		dbCon.rs.close();
		
		HashSet<TasteRefCnt> tasteRefByFood = new HashSet<TasteRefCnt>();
		
		for(Map.Entry<Integer, Integer> entry : tasteRef.entrySet()){
			tasteRefByFood.add(new TasteRefCnt(entry.getKey(), entry.getValue()));
		}
		
		return tasteRefByFood;
	}
	
	/**
	 * Write the food taste result to db.
	 * @param dbCon
	 * @param foodTasteRef
	 * @throws SQLException
	 */
	private static void storeFoodTaste(DBCon dbCon, HashMap<Food, HashSet<TasteRefCnt>> foodTasteRef) throws SQLException{
		
		String sql;
		
		/**
		 * Delete the food taste record whose associated record in food table is NOT exist
		 */
		sql = " DELETE FROM " + Params.dbName + ".food_taste" +
			  " WHERE food_id NOT IN (" +
			  " SELECT food_id FROM " + Params.dbName + ".food) ";
		dbCon.stmt.executeUpdate(sql);
		
		/**
		 * Delete the food taste record whose reference type is smart
		 */
		sql = " DELETE FROM " + Params.dbName + ".food_taste" +
			  " WHERE food_id IN (" +
			  " SELECT food_id FROM " + Params.dbName + ".food " +
			  " WHERE taste_ref_type=" + Food.TASTE_SMART_REF + ")";
		dbCon.stmt.executeUpdate(sql);
		
		dbCon.stmt.clearBatch();
		for(Map.Entry<Food, HashSet<TasteRefCnt>> entry : foodTasteRef.entrySet()){
			for(TasteRefCnt t : entry.getValue()){
				sql = INSERT_FOOD_TASTE_SQL.replace(FOOD_ID, Long.toString(entry.getKey().foodID))
				 						   .replace(TASTE_ID, Integer.toString(t.tasteID))
										   .replace(RESTAURANT_ID, Integer.toString(entry.getKey().restaurantID))
										   .replace(REF_COUNT, Integer.toString(t.refCnt));
				dbCon.stmt.addBatch(sql);
			}
		}
		dbCon.stmt.executeBatch();			
		
		/**
		 * Delete all the kitchen taste before updating.
		 */
		sql = " DELETE FROM " + Params.dbName + ".kitchen_taste";
		dbCon.stmt.executeUpdate(sql);
		
		/**
		 * Update the kitchen taste
		 */
		sql = " INSERT INTO " + Params.dbName + ".kitchen_taste" +
			  " (kitchen_id, taste_id, restaurant_id, ref_cnt) " +
			  " SELECT FOOD.kitchen_id, FOOD_TASTE.taste_id, MAX(FOOD.restaurant_id), SUM(FOOD_TASTE.ref_cnt) " +
			  " FROM " + 
			  Params.dbName + ".food_taste FOOD_TASTE, " +
			  Params.dbName + ".food FOOD " +
			  " WHERE " +
			  " FOOD_TASTE.food_id = FOOD.food_id " +
			  " AND " +
			  " FOOD.kitchen_alias <> " + Kitchen.KITCHEN_NULL +
			  " GROUP BY FOOD.kitchen_id, FOOD_TASTE.taste_id ";
		dbCon.stmt.executeUpdate(sql);
		
		/**
		 * Delete all the department taste before updating.
		 */
		sql = " DELETE FROM " + Params.dbName + ".dept_taste";
		dbCon.stmt.executeUpdate(sql);
		
		/**
		 * Update the department taste
		 */
		sql = " INSERT INTO " + Params.dbName + ".dept_taste" +
			  " (dept_id, taste_id, restaurant_id, ref_cnt) " +
			  " SELECT KITCHEN.kitchen_id, KITCHEN_TASTE.taste_id, MAX(KITCHEN.restaurant_id), SUM(KITCHEN_TASTE.ref_cnt) " +
			  " FROM " + 
			  Params.dbName + ".kitchen_taste KITCHEN_TASTE, " +
			  Params.dbName + ".kitchen KITCHEN " +
			  " WHERE " +
			  " KITCHEN_TASTE.kitchen_id = KITCHEN.kitchen_id " +
			  " GROUP BY KITCHEN.dept_id, KITCHEN_TASTE.taste_id ";
		dbCon.stmt.executeUpdate(sql);
	}	

}


class TasteRefCnt{
	public int tasteID;
	public int refCnt;
	
	public TasteRefCnt(int tasteID, int refCnt){
		this.tasteID = tasteID;
		this.refCnt = refCnt;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof TasteRefCnt)){
			return false;
		}else{
			return tasteID == ((TasteRefCnt)obj).tasteID;
		}
	}
	
	@Override
	public int hashCode(){
		return new Integer(tasteID).hashCode();
	}

}