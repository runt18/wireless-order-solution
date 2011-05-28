package com.wireless.db;

import java.sql.SQLException;
import java.util.ArrayList;

import com.wireless.exception.BusinessException;
import com.wireless.protocol.Food;
import com.wireless.protocol.FoodMenu;
import com.wireless.protocol.Kitchen;
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
	public static FoodMenu exec(int pin, short model) throws BusinessException, SQLException{
		
		DBCon dbCon = new DBCon();
		
		try{
			dbCon.connect();
			
			Terminal term = VerifyPin.exec(dbCon, pin, model);
			
			return new FoodMenu(queryFoods(dbCon, term.restaurant_id), 
							    queryTastes(dbCon, term.restaurant_id),
							    queryKitchens(dbCon, term.restaurant_id));
			
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
	public static Food[] execFoods(int pin, short model) throws BusinessException, SQLException{
		
		DBCon dbCon = new DBCon();
		
		try{
			dbCon.connect();
			
			Terminal term = VerifyPin.exec(dbCon, pin, model);		
			
			return queryFoods(dbCon, term.restaurant_id);
			
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
	public static Taste[] execTastes(int pin, short model) throws BusinessException, SQLException {

		DBCon dbCon = new DBCon();
		
		try {
			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, pin, model);
			return queryTastes(dbCon, term.restaurant_id);

		} finally {
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the kitchen information.
	 * @param pin the pin to this terminal
	 * @param model the model to this terminal
	 * @return the food menu holding all the information
	 * @throws BusinessException throws if either of cases below.<br>
	 * 							 - The terminal is NOT attache to any restaurant.<br>
	 * 							 - The terminal is expired.
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static Kitchen[] execKitchens(int pin, short model) throws BusinessException, SQLException{

		DBCon dbCon = new DBCon();
		
		try {
			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, pin, model);
			return queryKitchens(dbCon, term.restaurant_id);

		} finally {
			dbCon.disconnect();
		}
	}
	
	private static Food[] queryFoods(DBCon dbCon, int restaurantID) throws SQLException{
		ArrayList<Food> foods = new ArrayList<Food>();
        //get all the food information to this restaurant
		String sql = "SELECT alias_id, name, unit_price, kitchen, status, pinyin FROM " + 
					 Params.dbName + ".food WHERE restaurant_id=" + restaurantID +
					 " AND enabled=1";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			Food food = new Food(dbCon.rs.getShort("alias_id"),
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
	
	private static Kitchen[] queryKitchens(DBCon dbCon, int restaurantID) throws SQLException{
		//get all the kitchen information to this restaurant,
		ArrayList<Kitchen> kitchens = new ArrayList<Kitchen>();
		String sql = "SELECT alias_id, name, discount, discount_2, discount_3, member_discount_1, member_discount_2, member_discount_3 FROM " + 
			  		 Params.dbName + ".kitchen WHERE restaurant_id=" + 
			  		 restaurantID;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			kitchens.add(new Kitchen(dbCon.rs.getString("name"),
									 dbCon.rs.getShort("alias_id"),
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
	
	private static Taste[] queryTastes(DBCon dbCon, int restaurantID) throws SQLException{
		//Get the taste preferences to this restaurant sort by alias id in ascend order.
		//The lower alias id, the more commonly this preference used.
		//Put the most commonly used taste preference in first position 
		String sql = "SELECT alias_id, preference, price FROM " + Params.dbName + ".taste WHERE restaurant_id=" + 
					 restaurantID + 
					 " ORDER BY alias_id";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		ArrayList<Taste> tastes = new ArrayList<Taste>();
		while(dbCon.rs.next()){
			Taste taste = new Taste(dbCon.rs.getShort("alias_id"), 
									dbCon.rs.getString("preference"),
									new Float(dbCon.rs.getFloat("price")));
			tastes.add(taste);
		}
		dbCon.rs.close();
		
		return tastes.toArray(new Taste[tastes.size()]);
	}
	
}
