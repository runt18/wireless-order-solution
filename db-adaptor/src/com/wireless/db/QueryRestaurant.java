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
	 * 							 - The terminal is expired.<br>
	 * 							 - The restaurant this terminal attached to does NOT exist.
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static Restaurant exec(long pin, short model) throws BusinessException, SQLException{ 

		DBCon dbCon = new DBCon();
		
		try{
			dbCon.connect();
			
			Terminal term = VerifyPin.exec(dbCon, pin, model);
			
			Restaurant restaurant = exec(dbCon, term.restaurantID);
			restaurant.owner = term.owner;
			
			return restaurant;
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the restaurant information according to specific restaurant id.
	 * @param term 
	 * 			the terminal to query
	 * @return the restaurant information
	 * @throws BusinessException 
	 * 			throws if the restaurant does NOT exist
	 * @throws SQLException 
	 * 			throws if fail to execute any SQL statement
	 */
	public static Restaurant exec(Terminal term) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		
		try{
			dbCon.connect();
			Restaurant restaurant = exec(dbCon, term.restaurantID);
			restaurant.owner = term.owner;
			return restaurant;
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the restaurant information according to specific restaurant id.
	 * @param restaurantID 
	 * 			the restaurant id
	 * @return the restaurant information
	 * @throws BusinessException 
	 * 			throws if the restaurant does NOT exist
	 * @throws SQLException 
	 * 			throws if fail to execute any SQL statement
	 */
	public static Restaurant exec(int restaurantID) throws BusinessException, SQLException{
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
	 * @param dbCon 
	 * 			the database connection 
	 * @param restaurantID 
	 * 			the restaurant id
	 * @return the restaurant information
	 * @throws BusinessException 
	 * 			throws if the restaurant does NOT exist
	 * @throws SQLException 
	 * 			throws if fail to execute any SQL statement
	 */
	public static Restaurant exec(DBCon dbCon, int restaurantID) throws BusinessException, SQLException{
		Restaurant restaurant = new Restaurant();
		String sql = "SELECT restaurant_name, restaurant_info, tele1, tele2, address, pwd, pwd2, pwd3, pwd4, pwd5 FROM " + Params.dbName + "." +
					 "restaurant WHERE id=" + restaurantID; 
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			restaurant.id = restaurantID;
			restaurant.name = dbCon.rs.getString("restaurant_name");
			restaurant.info = dbCon.rs.getString("restaurant_info");
			restaurant.tele_1 = dbCon.rs.getString("tele1");
			restaurant.tele_2 = dbCon.rs.getString("tele2");
			restaurant.addr = dbCon.rs.getString("address");
			restaurant.pwd = dbCon.rs.getString("pwd");
			restaurant.pwd2 = dbCon.rs.getString("pwd2");
			restaurant.pwd3 = dbCon.rs.getString("pwd3");
			restaurant.pwd4 = dbCon.rs.getString("pwd4");
			restaurant.pwd5 = dbCon.rs.getString("pwd5");
		}else{
			throw new BusinessException("The restaurant(id=" + restaurantID + ") does NOT exist.");
		}
		dbCon.rs.close();
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
			dbCon.rs.close();
		}		

		dbCon.rs.close();
		return restaurant;
	}
	
}
