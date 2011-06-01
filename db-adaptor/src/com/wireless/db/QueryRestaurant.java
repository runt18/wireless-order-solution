package com.wireless.db;

import java.sql.SQLException;

import com.wireless.exception.BusinessException;
import com.wireless.protocol.Restaurant;
import com.wireless.protocol.Terminal;

public class QueryRestaurant {
	
	/**
	 * Get the restaurant information that the terminal is attached to.
	 * @param pin the pin to this terminal
	 * @param model the model to this terminal
	 * @return The restaurant info as below.<br>
	 * 		   - The restaurant name.
	 * 		   - The restaurant billboard info.
	 * 		   - The owner name to this terminal
	 * @throws BusinessException throws if either of cases below.<br>
	 * 							 - The terminal is NOT attached to any restaurant.<br>
	 * 							 - The terminal is expired.
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static Restaurant exec(int pin, short model) throws BusinessException, SQLException{ 

		DBCon dbCon = new DBCon();
		
		try{
			dbCon.connect();
			
			Terminal term = VerifyPin.exec(dbCon, pin, model);
			
			Restaurant restaurant = exec(dbCon, term.restaurant_id);
			restaurant.owner = term.owner;
			
			return restaurant;
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the restaurant information according to specific restaurant id.
	 * @param restaurantID the restaurant id to query
	 * @return the restaurant information
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static Restaurant exec(int restaurantID) throws SQLException{
		DBCon dbCon = new DBCon();
		
		try{
			dbCon.connect();
			return exec(dbCon, restaurantID);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the restaurant information according to specific restaurant id.
	 * Note that this method should be invoked before connecting the database 
	 * @param dbCon the database connection 
	 * @param restaurantID the restaurant id to query
	 * @return the restaurant information
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	static Restaurant exec(DBCon dbCon, int restaurantID) throws SQLException{
		Restaurant restaurant = new Restaurant();
		String sql = "SELECT restaurant_name, restaurant_info, tele1, tele2, address FROM " + Params.dbName + "." +
					 "restaurant WHERE id=" + restaurantID; 
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			restaurant.name = dbCon.rs.getString("restaurant_name");
			restaurant.info = dbCon.rs.getString("restaurant_info");
			restaurant.tele_1 = dbCon.rs.getString("tele1");
			restaurant.tele_2 = dbCon.rs.getString("tele2");
			restaurant.addr = dbCon.rs.getString("address");
		}
		/**
		* if the corresponding info not be found, then get the root's info as common
		*/
		if(restaurant.info.isEmpty()){
			sql = "SELECT restaurant_info FROM " + Params.dbName + "." +
					"restaurant WHERE id=" + Restaurant.ADMIN;
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				restaurant.info = dbCon.rs.getString(1);
			}
		}			
		return restaurant;
	}
	
}
