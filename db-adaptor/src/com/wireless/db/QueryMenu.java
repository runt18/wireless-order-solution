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
	
	public static Food[] queryFoods(String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return queryFoods(dbCon, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static Food[] queryFoods(DBCon dbCon, String extraCondition, String orderClause) throws SQLException{
		ArrayList<Food> foods = new ArrayList<Food>();
        //get all the food information to this restaurant
		String sql = " SELECT " +
					 " FOOD.restaurant_id, FOOD.food_id, FOOD.food_alias, " +
					 " FOOD.name, FOOD.unit_price, FOOD.kitchen_alias, FOOD.status, FOOD.pinyin, FOOD.taste_ref_type, " +
					 " KITCHEN.dept_id, KITCHEN.kitchen_id " +
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
			Food food = new Food(dbCon.rs.getInt("restaurant_id"),
								 dbCon.rs.getLong("food_id"),
								 dbCon.rs.getInt("food_alias"),
								 dbCon.rs.getString("name"),
								 new Float(dbCon.rs.getFloat("unit_price")),
								 dbCon.rs.getShort("dept_id"),
								 dbCon.rs.getLong("kitchen_id"),
								 dbCon.rs.getShort("kitchen_alias"),
								 dbCon.rs.getShort("status"),
								 dbCon.rs.getString("pinyin"),
								 dbCon.rs.getShort("taste_ref_type"));
			food.tasteRefType =  dbCon.rs.getLong("taste_ref_type");
			foods.add(food);
		}
	
		dbCon.rs.close();
		
		return foods.toArray(new Food[foods.size()]);
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

		String sql = " SELECT * FROM " + Params.dbName + ".taste " +
				     " WHERE 1=1 " +
					 (category < 0 ? "" : " AND category=" + category) + " " +
					 (extraCond == null ? "" : extraCond) + " " +
					 (orderClause == null ? "" : orderClause);
					 //" ORDER BY alias_id";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		ArrayList<Taste> tastes = new ArrayList<Taste>();
		while(dbCon.rs.next()){
			Taste taste = new Taste(dbCon.rs.getInt("taste_id"),
								    dbCon.rs.getInt("taste_alias"), 
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
