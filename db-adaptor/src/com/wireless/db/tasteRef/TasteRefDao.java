package com.wireless.db.tasteRef;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.menuMgr.FoodDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.dishesOrder.Food;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.tasteMgr.TasteGroup;
import com.wireless.protocol.Terminal;

public class TasteRefDao {
	
	private final static int TASTE_REF_NUM = 10;
	
	private final static String TASTE_ID = "$(taste_id)";
	private final static String FOOD_ID = "$(food_id)";
	private final static String RESTAURANT_ID = "$(restaurant_id)";
	private final static String REF_COUNT = "$(ref_cnt)";
	private final static String TASTE_RANK = "$(taste_rank)";
	
	private final static String QUERY_TASTE_SQL = 
			" SELECT A.food_id, A.taste_id, COUNT(*) AS ref_cnt " +
			" FROM " +
			" ( " +
			" SELECT " + 
			" OFH.order_id, OFH.food_id, NTGH.taste_id " +
			" FROM " +
			Params.dbName + ".normal_taste_group_history NTGH " +
			" JOIN " +
			Params.dbName + ".taste_group_history TGH " +
			" ON " + " NTGH.normal_taste_group_id = TGH.normal_taste_group_id " + " AND " + " NTGH.normal_taste_group_id <> " + TasteGroup.EMPTY_NORMAL_TASTE_GROUP_ID +
			" JOIN " +
			Params.dbName + ".order_food_history OFH " +
			" ON " + 
			" TGH.taste_group_id = OFH.taste_group_id " + " AND " + " OFH.is_temporary = 0 " + 
			" AND " + 
			" OFH.food_id IN( " + FOOD_ID + ")" + " AND " + " TGH.taste_group_id <> " + TasteGroup.EMPTY_TASTE_GROUP_ID +
			" GROUP BY " + " OFH.order_id, OFH.food_id, NTGH.taste_id " +
			" HAVING " + " SUM(OFH.order_count) > 0 " +
			" ) AS A " +
			" GROUP BY " + " A.food_id, A.taste_id ";
	
	private final static String INSERT_FOOD_TASTE_SQL = 
			" INSERT INTO " + Params.dbName + ".food_taste" +
			" (food_id, taste_id, restaurant_id, ref_cnt) " +
			" VALUES(" +
			FOOD_ID + ", " +
			TASTE_ID + ", " +
			RESTAURANT_ID + ", " +
			REF_COUNT + ")";
	
	private final static String INSERT_FOOD_TASTE_RANK_SQL =
			" INSERT INTO " + Params.dbName + ".food_taste_rank" +
			" (food_id, taste_id, restaurant_id, rank) " +
			" VALUES(" +
			FOOD_ID + ", " +
			TASTE_ID + ", " +
			RESTAURANT_ID + ", " +
			TASTE_RANK + ")";
	
	/**
	 * Update the taste reference count to a specific food.
	 * @param food
	 * 			the food to be queried
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the taste reference type to food is NOT smart
	 */
	public static void execByFood(Food food) throws SQLException, BusinessException{
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
	 * @throws BusinessException
	 * 			throws if the taste reference type to food is NOT smart
	 */
	public static void execByFood(DBCon dbCon, Food food) throws SQLException, BusinessException{
		
		if(!food.isTasteSmartRef()){
			throw new BusinessException("");
		}
		
		/**
		 * Get all taste reference record to this food.
		 */
		HashSet<TasteRefCnt> tasteRefByFood = getFoodTaste(dbCon, food);
		
		String sql;
		
		/**
		 * Delete the original taste reference record to this food.
		 */
		sql = " DELETE FROM " + Params.dbName + ".food_taste " +
			  " WHERE food_id=" + food.getFoodId();
		dbCon.stmt.executeUpdate(sql);
		
		/**
		 * Update the new taste reference record to this food
		 */
		dbCon.stmt.clearBatch();
		for(TasteRefCnt t : tasteRefByFood){
			sql = INSERT_FOOD_TASTE_SQL.replace(FOOD_ID, Long.toString(food.getFoodId()))
			 						   .replace(TASTE_ID, Integer.toString(t.tasteID))
									   .replace(RESTAURANT_ID, Integer.toString(food.getRestaurantId()))
									   .replace(REF_COUNT, Integer.toString(t.refCnt));
			dbCon.stmt.addBatch(sql);
		}
		dbCon.stmt.executeBatch();
		
		/**
		 * Get the taste reference count to the kitchen this food belongs to.
		 * Append the most popular kitchen taste to the tail of food taste reference.
		 */
		for(Set<TasteRefCnt> refCnt : getTasteRefByKitchen(dbCon, "AND KITCHEN_TASTE.kitchen_id = " + food.getKitchen().getId(), null).values()){
			tasteRefByFood.addAll(refCnt);
		}		
		
		/**
		 * Get the taste reference count to the department this food belongs to.
		 * Append the most popular department taste to the tail of food taste reference.
		 */
		for(Set<TasteRefCnt> refCnt : getTasteRefByDept(dbCon, 
														" AND DEPT_TASTE.dept_id = " + food.getKitchen().getDept().getId() +
														" AND DEPT_TASTE.restaurant_id = " + food.getRestaurantId(), null).values()){
			tasteRefByFood.addAll(refCnt);
		}
		
		/**
		 * Sort the taste reference to this food.
		 */
		TreeSet<TasteRefCnt> sortedTasteRef = new TreeSet<TasteRefCnt>(tasteRefByFood);
		
		/**
		 * Delete the original food taste rank record to this food.
		 */
		sql = " DELETE FROM " + Params.dbName + ".food_taste_rank" +
			  " WHERE food_id = " + food.getFoodId();
		dbCon.stmt.executeUpdate(sql);
		
		dbCon.stmt.clearBatch();
		int tasteRank = 1;
		for(TasteRefCnt t : sortedTasteRef){
			if(tasteRank <= TASTE_REF_NUM){
				sql = INSERT_FOOD_TASTE_RANK_SQL.replace(FOOD_ID, Long.toString(food.getFoodId()))
											    .replace(TASTE_ID, Integer.toString(t.tasteID))
			 						   			.replace(RESTAURANT_ID, Integer.toString(food.getRestaurantId()))
			 						   			.replace(TASTE_RANK, Integer.toString(tasteRank++));
				dbCon.stmt.addBatch(sql);
			}else{
				break;
			}
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
		List<Food> foods = FoodDao.getPureFoods("AND FOOD.taste_ref_type=" + Food.TasteRef.SMART.getVal(), null);
		
		if(!foods.isEmpty()){
			
			Comparator<Food> foodComp = new Comparator<Food>(){
				@Override
				public int compare(Food arg0, Food arg1) {
					if(arg0.getFoodId() > arg1.getFoodId()){
						return 1;
					}else if(arg0.getFoodId() < arg1.getFoodId()){
						return -1;
					}else{
						return 0;
					}
				}			
			};
		
			Collections.sort(foods, foodComp);
			
			/**
			 * The hash map to store the taste reference count to all the foods
			 */
			HashMap<Food, Set<TasteRefCnt>> foodTasteRef = new HashMap<Food, Set<TasteRefCnt>>();

			//Combine the food id to condition string.
			StringBuilder foodIdCond = new StringBuilder();
			for(Food food : foods){
				
				foodTasteRef.put(food, new HashSet<TasteRefCnt>());
				
				if(foodIdCond.length() == 0){
					foodIdCond.append(food.getFoodId());
				}else{
					foodIdCond.append("," + food.getFoodId());
				}
			}		

			String sqlTmp = QUERY_TASTE_SQL.replace(FOOD_ID, foodIdCond);		
			
			dbCon.rs = dbCon.stmt.executeQuery(sqlTmp);

			while(dbCon.rs.next()){
				int index = Collections.binarySearch(foods, new Food(dbCon.rs.getInt("food_id"), 0, 0), foodComp);
				if(index >= 0){
					Set<TasteRefCnt> tasteRefByFood = foodTasteRef.get(foods.get(index));
					if(tasteRefByFood != null){
						tasteRefByFood.add(new TasteRefCnt(dbCon.rs.getInt("taste_id"), TasteRefCnt.TASTE_BY_FOOD, dbCon.rs.getInt("ref_cnt")));
					}
				}
			}
			dbCon.rs.close();
			
			/**
			 * Write the food taste to db & update the kitchen and department taste
			 */
			storeFoodTaste(dbCon, foodTasteRef);	

		}	
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
		
		String sqlTmp = QUERY_TASTE_SQL.replace(FOOD_ID, Long.toString(food.getFoodId()));		
		
		dbCon.rs = dbCon.stmt.executeQuery(sqlTmp);
		
		HashSet<TasteRefCnt> tasteRefByFood = new HashSet<TasteRefCnt>();
		while(dbCon.rs.next()){
			tasteRefByFood.add(new TasteRefCnt(dbCon.rs.getInt("taste_id"), TasteRefCnt.TASTE_BY_FOOD, dbCon.rs.getInt("ref_cnt")));
		}
		dbCon.rs.close();
		
		return tasteRefByFood;
	}
	
	/**
	 * Write the food taste result to db.
	 * @param dbCon
	 * @param foodTasteRef
	 * @throws SQLException
	 */
	private static void storeFoodTaste(DBCon dbCon, HashMap<Food, Set<TasteRefCnt>> foodTasteRef) throws SQLException{
		
		String sql;
		
		/**
		 * Delete all the food taste before updating.
		 */
		sql = " DELETE FROM " + Params.dbName + ".food_taste";
		dbCon.stmt.executeUpdate(sql);
		

		dbCon.stmt.clearBatch();
		for(Map.Entry<Food, Set<TasteRefCnt>> entry : foodTasteRef.entrySet()){
			for(TasteRefCnt t : entry.getValue()){
				sql = INSERT_FOOD_TASTE_SQL.replace(FOOD_ID, Long.toString(entry.getKey().getFoodId()))
				 						   .replace(TASTE_ID, Integer.toString(t.tasteID))
										   .replace(RESTAURANT_ID, Integer.toString(entry.getKey().getRestaurantId()))
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
			  " SELECT KITCHEN.dept_id, KITCHEN_TASTE.taste_id, MAX(KITCHEN.restaurant_id), SUM(KITCHEN_TASTE.ref_cnt) " +
			  " FROM " + 
			  Params.dbName + ".kitchen_taste KITCHEN_TASTE, " +
			  Params.dbName + ".kitchen KITCHEN " +
			  " WHERE " +
			  " KITCHEN_TASTE.kitchen_id = KITCHEN.kitchen_id " +
			  " GROUP BY KITCHEN.dept_id, KITCHEN_TASTE.taste_id ";
		dbCon.stmt.executeUpdate(sql);		
		

		/**
		 * Get the taste reference count to each kitchens.
		 */
		HashMap<Kitchen, Set<TasteRefCnt>> kitchenTasteRef = getTasteRefByKitchen(dbCon, null, null);
		
		/**
		 * Get the taste reference count to each departments.
		 */
		HashMap<Department, Set<TasteRefCnt>> deptTasteRef = getTasteRefByDept(dbCon, null, null);
		
		for(Map.Entry<Food, Set<TasteRefCnt>> entry : foodTasteRef.entrySet()){
			Set<TasteRefCnt> kitchenTaste = kitchenTasteRef.get(entry.getKey().getKitchen());
			if(kitchenTaste != null){
				/**
				 * Append the most popular kitchen taste this food belongs to.
				 */
				entry.getValue().addAll(kitchenTaste);				
			}
			
			Set<TasteRefCnt> deptTaste = deptTasteRef.get(entry.getKey().getKitchen().getDept());
			if(deptTaste != null){
				/**
				 * Append the most popular department taste this food belongs to.
				 */
				entry.getValue().addAll(deptTaste);
			}
			
			/**
			 * Sort the taste reference to this food.
			 */
			entry.setValue(new TreeSet<TasteRefCnt>(entry.getValue()));
		}
		
		/**
		 * Delete the food taste rank record whose associated record in food table is NOT exist
		 */
		sql = " DELETE FROM " + Params.dbName + ".food_taste_rank" +
			  " WHERE food_id NOT IN (" +
			  " SELECT food_id FROM " + Params.dbName + ".food) ";
		dbCon.stmt.executeUpdate(sql);
		
		/**
		 * Delete the food taste rank record whose reference type is smart
		 */
		sql = " DELETE FROM " + Params.dbName + ".food_taste_rank" +
			  " WHERE food_id IN (" +
			  " SELECT food_id FROM " + Params.dbName + ".food " +
			  " WHERE taste_ref_type=" + Food.TasteRef.SMART.getVal() + ")";
		dbCon.stmt.executeUpdate(sql);
		
	
		dbCon.stmt.clearBatch();
		for(Map.Entry<Food, Set<TasteRefCnt>> entry : foodTasteRef.entrySet()){
			int tasteRank = 1;
			for(TasteRefCnt t : entry.getValue()){				
				if(tasteRank <= TASTE_REF_NUM){
					sql = INSERT_FOOD_TASTE_RANK_SQL.replace(FOOD_ID, Long.toString(entry.getKey().getFoodId()))
												    .replace(TASTE_ID, Integer.toString(t.tasteID))
				 						   			.replace(RESTAURANT_ID, Integer.toString(entry.getKey().getRestaurantId()))
				 						   			.replace(TASTE_RANK, Integer.toString(tasteRank++));
					dbCon.stmt.addBatch(sql);
				}else{
					break;
				}
			}
		}
		dbCon.stmt.executeBatch();
		
	}
	/**
	 * 批量更新
	 * @param modTastes
	 */
	public static boolean update(Terminal terminal,String modTastes){
		try{
			DBCon dbCon = new DBCon();
			dbCon.connect();
			String[] records = modTastes.split("record_separator");//记录
			dbCon.connect();
			for(int i = 0;i < records.length; i++){
				String[] fields = records[i].split("field_separator");
				String tID = fields[0];
				String tAlias = fields[1];
				String tName = fields[2];
				String tPrice = fields[3];
				String tRate = fields[4];
				String tCategory = fields[5];
				String sql = "UPDATE "+Params.dbName+".taste SET "+Params.dbName+".taste.preference = '"+tName+"',"+Params.dbName+".taste.rate="+tRate+","+Params.dbName+".taste.calc = "+tCategory+","+Params.dbName+".taste.price = "+tPrice+","+Params.dbName+".taste.taste_alias = "+tAlias+" WHERE "+Params.dbName+".taste.restaurant_id = "+terminal.restaurantID+" AND "+Params.dbName+".taste.taste_id = "+tID;
				dbCon.stmt.addBatch(sql);
			}
			dbCon.stmt.executeBatch();
			dbCon.disconnect();
			return true;
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * Get the kitchen taste reference count.
	 * @param dbCon
	 * 			the database connection
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	private static HashMap<Kitchen, Set<TasteRefCnt>> getTasteRefByKitchen(DBCon dbCon, String extraCond, String orderClause) throws SQLException{
		
		String sql;
		
		/**
		 * Get the taste reference count to one or more kitchens.
		 */
		sql = " SELECT " +
			  " KITCHEN_TASTE.kitchen_id, KITCHEN_TASTE.taste_id, KITCHEN_TASTE.restaurant_id, KITCHEN_TASTE.ref_cnt, " +
			  " KITCHEN.kitchen_alias " +
			  " FROM " + 
			  Params.dbName + ".kitchen_taste KITCHEN_TASTE, " +
			  Params.dbName + ".kitchen KITCHEN " +
			  " WHERE KITCHEN_TASTE.kitchen_id = KITCHEN.kitchen_id " +
			  (extraCond != null ? extraCond : " ") +
			  (orderClause != null ? orderClause : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		HashMap<Kitchen, Set<TasteRefCnt>> result = new HashMap<Kitchen, Set<TasteRefCnt>>();
		while(dbCon.rs.next()){
			Kitchen kitchen = new Kitchen.Builder(dbCon.rs.getShort("kitchen_alias"), null, dbCon.rs.getInt("restaurant_id"))
								.setKitchenId(dbCon.rs.getLong("kitchen_id"))
								.build();
//			Kitchen kitchen = new Kitchen(dbCon.rs.getInt("restaurant_id"),
//										  "",
//										  dbCon.rs.getLong("kitchen_id"),
//										  dbCon.rs.getShort("kitchen_alias"),
//										  false,
//										  Kitchen.TYPE_NORMAL,
//										  null);
			
			TasteRefCnt tasteRef = new TasteRefCnt(dbCon.rs.getInt("taste_id"),
				    							   TasteRefCnt.TASTE_BY_KITCHEN,
				    							   dbCon.rs.getInt("ref_cnt"));
			
			Set<TasteRefCnt> kitchenTaste = result.get(kitchen);
			if(kitchenTaste != null){
				kitchenTaste.add(tasteRef);
				result.put(kitchen, kitchenTaste);
			}else{
				kitchenTaste = new HashSet<TasteRefCnt>();
				kitchenTaste.add(tasteRef);
				result.put(kitchen, kitchenTaste);
			}
		}
		dbCon.rs.close();
		
		return result;
	}
	
	/**
	 * Get the department taste reference count.
	 * @param dbCon
	 * 			the database connection
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	private static HashMap<Department, Set<TasteRefCnt>> getTasteRefByDept(DBCon dbCon, String extraCond, String orderClause) throws SQLException{
		
		String sql;
		
		/**
		 * Get the taste reference count to one or more kitchens.
		 */
		sql = " SELECT " +
			  " DEPT_TASTE.dept_id, DEPT_TASTE.taste_id, DEPT_TASTE.restaurant_id, DEPT_TASTE.ref_cnt " +
			  " FROM " + 
			  Params.dbName + ".dept_taste DEPT_TASTE " +
			  " WHERE 1=1 " +
			  (extraCond != null ? extraCond : " ") +
			  (orderClause != null ? orderClause : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		HashMap<Department, Set<TasteRefCnt>> result = new HashMap<Department, Set<TasteRefCnt>>();
		while(dbCon.rs.next()){
			Department dept = new Department("", dbCon.rs.getShort("dept_id"), dbCon.rs.getInt("restaurant_id"), Department.Type.NORMAL);
			
			TasteRefCnt tasteRef = new TasteRefCnt(dbCon.rs.getInt("taste_id"),
				    							   TasteRefCnt.TASTE_BY_DEPT,
				    							   dbCon.rs.getInt("ref_cnt"));
			
			Set<TasteRefCnt> deptTaste = result.get(dept);
			if(deptTaste != null){
				deptTaste.add(tasteRef);
				result.put(dept, deptTaste);
			}else{
				deptTaste = new HashSet<TasteRefCnt>();
				deptTaste.add(tasteRef);
				result.put(dept, deptTaste);
			}
		}
		dbCon.rs.close();
		
		return result;
	}
	
}

class TasteRefCnt implements Comparable<TasteRefCnt>{
	
	final static int TASTE_BY_FOOD = 2;
	final static int TASTE_BY_KITCHEN = 1;
	final static int TASTE_BY_DEPT = 0;
	
	int tasteID;
	int cate = TASTE_BY_FOOD;
	int refCnt;
	
	public TasteRefCnt(int tasteID, int cate, int refCnt){
		this.tasteID = tasteID;
		this.cate = cate;
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
		return tasteID * 31 + 17;
	}

	/**
	 * The rule to sort the taste reference count as below.
	 * If the category is the same, the more the reference count, the higher the rank.
	 * If the category is NOT the same, the level is as below.
	 * TASTE_BY_FOOD > TASTE_BY_KITCHEN > TASTE_BY_DEPT
	 */
	@Override
	public int compareTo(TasteRefCnt o) {
		if(cate == o.cate){
			if(refCnt > o.refCnt){
				return -1;
			}else if(refCnt < o.refCnt){
				return 1;
			}else{
				return tasteID == o.tasteID ? 0 : (tasteID > o.tasteID ? -1 : 1);
			}
		}else{
			if(cate > o.cate){
				return -1;
			}else if(cate < o.cate){
				return 1;
			}else{
				return tasteID == o.tasteID ? 0 : (tasteID > o.tasteID ? -1 : 1);
			}
		}
	}
}