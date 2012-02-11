package com.wireless.db;

import java.sql.SQLException;
import java.util.ArrayList;

import com.wireless.exception.BusinessException;
import com.wireless.protocol.Food;
import com.wireless.protocol.FoodMenu;
import com.wireless.protocol.Kitchen;
import com.wireless.protocol.Department;
import com.wireless.protocol.Taste;
import com.wireless.protocol.Terminal;

public class QueryMenu {

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
			
			Terminal term = VerifyPin.exec(dbCon, pin, model);
			
			return new FoodMenu(queryFoods(dbCon, term.restaurant_id, null, null), 
							    queryTastes(dbCon, term.restaurant_id, Taste.CATE_TASTE, null, null),
							    queryTastes(dbCon, term.restaurant_id, Taste.CATE_STYLE, null, null),
							    queryTastes(dbCon, term.restaurant_id, Taste.CATE_SPEC, null, null),
							    queryKitchens(dbCon, term.restaurant_id, null, null),
							    queryDepartments(dbCon, term.restaurant_id, null, null));
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the food information.
	 * @param pin the pin to this terminal
	 * @param model the model to this terminal
	 * @return the food menu holding all the information
	 * @throws BusinessException throws if either of cases below.<br>
	 * 							 - The terminal is NOT attache to any restaurant.<br>
	 * 							 - The terminal is expired.
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static Food[] execFoods(long pin, short model) throws BusinessException, SQLException{
		return execFoods(pin, model, null, null);
	}
	
	/**
	 * Get the food information.
	 * @param pin the pin to this terminal
	 * @param model the model to this terminal
	 * @param extraCondition the extra condition to SQL statement
	 * @param orderClause the order clause to the SQL statement
	 * @return the food menu holding all the information
	 * @throws BusinessException throws if either of cases below.<br>
	 * 							 - The terminal is NOT attache to any restaurant.<br>
	 * 							 - The terminal is expired.
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static Food[] execFoods(long pin, short model, String extraCondition, String orderClause) throws BusinessException, SQLException{
		
		DBCon dbCon = new DBCon();
		
		try{
			dbCon.connect();
			
			Terminal term = VerifyPin.exec(dbCon, pin, model);		
			
			return queryFoods(dbCon, term.restaurant_id, extraCondition, orderClause);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the Taste information.
	 * @param pin the pin to this terminal
	 * @param model the model to this terminal
	 * @return the food menu holding all the information
	 * @throws BusinessException throws if either of cases below.<br>
	 * 							 - The terminal is NOT attache to any restaurant.<br>
	 * 							 - The terminal is expired.
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static Taste[] execTastes(long pin, short model) throws BusinessException, SQLException {

		DBCon dbCon = new DBCon();
		
		try {
			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, pin, model);
			return queryTastes(dbCon, term.restaurant_id, Short.MIN_VALUE, null, null);

		} finally {
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the Taste information.
	 * @param pin the pin to this terminal
	 * @param model the model to this terminal
	 * @param extraCond the extra condition to SQL statement
	 * @param orderClause the order clause to SQL statement
	 * @return the food menu holding all the information
	 * @throws BusinessException throws if either of cases below.<br>
	 * 							 - The terminal is NOT attache to any restaurant.<br>
	 * 							 - The terminal is expired.
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static Taste[] execTastes(long pin, short model, String extraCond, String orderClause) throws BusinessException, SQLException {

		DBCon dbCon = new DBCon();
		
		try {
			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, pin, model);
			return queryTastes(dbCon, term.restaurant_id, Short.MIN_VALUE, extraCond, orderClause);

		} finally {
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the kitchen information.
	 * @param pin 
	 * 			the pin to this terminal
	 * @param model 
	 * 			the model to this terminal
	 * @return 
	 * 			the food menu holding all the information
	 * @throws BusinessException 
	 * 			throws if either of cases below.<br>
	 * 			- The terminal is NOT attache to any restaurant.<br>
	 * 			- The terminal is expired.
	 * @throws SQLException 
	 * 			throws if fail to execute any SQL statement
	 */
	public static Kitchen[] execKitchens(long pin, short model) throws BusinessException, SQLException{

		DBCon dbCon = new DBCon();
		
		try {
			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, pin, model);
			return queryKitchens(dbCon, term.restaurant_id, null, null);

		} finally {
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the kitchen information.
	 * @param pin 
	 * 			the pin to this terminal
	 * @param model 
	 * 			the model to this terminal
	 * @param extraCond
	 * 			the extra condition to SQL statement
	 * @param orderClause
	 * 			the order clause to SQL statement
	 * @return 
	 * 			the food menu holding all the information
	 * @throws BusinessException 
	 * 			throws if either of cases below.<br>
	 * 			- The terminal is NOT attache to any restaurant.<br>
	 * 			- The terminal is expired.
	 * @throws SQLException 
	 * 			throws if fail to execute any SQL statement
	 */
	public static Kitchen[] execKitchens(long pin, short model, String extraCond, String orderClause) throws BusinessException, SQLException{

		DBCon dbCon = new DBCon();
		
		try {
			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, pin, model);
			return queryKitchens(dbCon, term.restaurant_id, extraCond, orderClause);

		} finally {
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the super kitchen information.
	 * @param pin 
	 * 			the pin to this terminal
	 * @param model 
	 * 			the model to this terminal
	 * @return 
	 * 			the food menu holding all the information
	 * @throws BusinessException 
	 * 			throws if either of cases below.<br>
	 * 			- The terminal is NOT attache to any restaurant.<br>
	 * 			- The terminal is expired.
	 * @throws SQLException 
	 * 			throws if fail to execute any SQL statement
	 */
	public static Department[] execDepartments(long pin, short model) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		
		try {
			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, pin, model);
			return queryDepartments(dbCon, term.restaurant_id, null, null);

		} finally {
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the super kitchen information.
	 * @param pin 
	 * 			the pin to this terminal
	 * @param model 
	 * 			the model to this terminal
	 * @param extraCond
	 * 			the extra condition to SQL statement
	 * @param orderClause
	 * 			the order clause to SQL statement
	 * @return 
	 * 			the food menu holding all the information
	 * @throws BusinessException 
	 * 			throws if either of cases below.<br>
	 * 			- The terminal is NOT attache to any restaurant.<br>
	 * 			- The terminal is expired.
	 * @throws SQLException 
	 * 			throws if fail to execute any SQL statement
	 */
	public static Department[] execDepartments(long pin, short model, String extraCond, String orderClause) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		
		try {
			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, pin, model);
			return queryDepartments(dbCon, term.restaurant_id, extraCond, orderClause);

		} finally {
			dbCon.disconnect();
		}
	}
	
	private static Food[] queryFoods(DBCon dbCon, int restaurantID, String extraCondition, String orderClause) throws SQLException{
		ArrayList<Food> foods = new ArrayList<Food>();
        //get all the food information to this restaurant
		String sql = "SELECT id, alias_id, name, unit_price, kitchen, status, pinyin FROM " + 
					 Params.dbName + ".food WHERE restaurant_id=" + restaurantID + " " +
					 (extraCondition == null ? "" : extraCondition) + " " +
					 (orderClause == null ? "" : orderClause); 
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			Food food = new Food(dbCon.rs.getLong("id"),
								 dbCon.rs.getInt("alias_id"),
								 dbCon.rs.getString("name"),
								 new Float(dbCon.rs.getFloat("unit_price")),
								 dbCon.rs.getShort("kitchen"),
								 dbCon.rs.getShort("status"),
								 dbCon.rs.getString("pinyin"));
			foods.add(food);
		}
	
		dbCon.rs.close();
		
		return foods.toArray(new Food[foods.size()]);
	}
	
	private static Kitchen[] queryKitchens(DBCon dbCon, int restaurantID, String extraCond, String orderClause) throws SQLException{
		//get all the kitchen information to this restaurant,
		ArrayList<Kitchen> kitchens = new ArrayList<Kitchen>();
		String sql = "SELECT alias_id, name, discount, discount_2, discount_3, " +
					 "member_discount_1, member_discount_2, member_discount_3, " +
					 "dept_id FROM " + 
			  		 Params.dbName + ".kitchen WHERE restaurant_id=" + 
			  		 restaurantID + " " +
			  		 (extraCond == null ? "" : extraCond) + " " +
			  		 (orderClause == null ? "" : orderClause);
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			kitchens.add(new Kitchen(dbCon.rs.getString("name"),
									 dbCon.rs.getShort("alias_id"),
									 dbCon.rs.getShort("dept_id"),
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
	
	private static Department[] queryDepartments(DBCon dbCon, int restaurantID, String extraCond, String orderClause) throws SQLException{
		//get tall the super kitchen information to this restaurant
		ArrayList<Department> sKitchens = new ArrayList<Department>();
		String sql = "SELECT dept_id, name FROM " + Params.dbName + ".department WHERE " +
					 " restaurant_id=" + restaurantID + " " +
					 (extraCond != null ? extraCond : "") + " " +
					 (orderClause != null ? orderClause : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			sKitchens.add(new Department(dbCon.rs.getString("name"),
									   dbCon.rs.getShort("dept_id")));
		}
		dbCon.rs.close();
		
		return sKitchens.toArray(new Department[sKitchens.size()]);
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
	private static Taste[] queryTastes(DBCon dbCon, int restaurantID, short category, String extraCond, String orderClause) throws SQLException{
		//Get the taste preferences to this restaurant sort by alias id in ascend order.
		//The lower alias id, the more commonly this preference used.
		//Put the most commonly used taste preference in first position 
		String sql = "SELECT alias_id, preference, price, category, rate, calc FROM " + Params.dbName + ".taste WHERE restaurant_id=" + 
					 restaurantID + 
					 (category < 0 ? "" : " AND category=" + category) + " " +
					 (extraCond == null ? "" : extraCond) + " " +
					 (orderClause == null ? "" : orderClause);
					 //" ORDER BY alias_id";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		ArrayList<Taste> tastes = new ArrayList<Taste>();
		while(dbCon.rs.next()){
			Taste taste = new Taste(dbCon.rs.getInt("alias_id"), 
									dbCon.rs.getString("preference"),
									dbCon.rs.getShort("category"),
									dbCon.rs.getShort("calc"),
									new Float(dbCon.rs.getFloat("rate")),
									new Float(dbCon.rs.getFloat("price")));
			tastes.add(taste);
		}
		dbCon.rs.close();
		
		return tastes.toArray(new Taste[tastes.size()]);
	}
	
}
