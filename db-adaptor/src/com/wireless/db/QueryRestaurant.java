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
		Terminal term = VerifyPin.exec(pin, model);
		DBCon dbCon = new DBCon();
		
		try{
			dbCon.connect();
			
			Restaurant restaurant = new Restaurant();
			restaurant.owner = term.owner;
			
			String sql = "SELECT restaurant_name, restaurant_info FROM " + Params.dbName + "." +
						"restaurant WHERE id=(" + "SELECT restaurant_id FROM " + Params.dbName + 
						".terminal WHERE pin=" + pin + ")";
						
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				restaurant.name = dbCon.rs.getString("restaurant_name");
				restaurant.info = dbCon.rs.getString("restaurant_info");
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
			
		}finally{
			dbCon.disconnect();
		}
	}
}
