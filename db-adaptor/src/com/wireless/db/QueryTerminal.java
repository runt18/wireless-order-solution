package com.wireless.db;

import java.sql.SQLException;
import java.util.ArrayList;

import com.wireless.protocol.Terminal;

public class QueryTerminal {

	/**
	 * Query the terminal to a specific restaurant.
	 * @param dbCon
	 * 			the database connection
	 * @param restaurantID
	 * 			the restaurant id to query
	 * @return
	 * 			return all the terminals to this restaurant and match the extra condition and order clause
	 * @throws SQLException
	 * 			throws if fail to execute the SQL statement
	 */
	public static Terminal[] exec(int restaurantID, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, restaurantID, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Query the terminal to a specific restaurant.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon
	 * 			the database connection
	 * @param restaurantID
	 * 			the restaurant id to query
	 * @param extraCond
	 * 			the extra condition to the SQL statement
	 * @param orderClause 
	 * 			the order clause to the SQL statement
	 * @return
	 * 			return all the terminals to this restaurant and match the extra condition and order clause
	 * @throws SQLException
	 * 			throws if fail to execute the SQL statement
	 */
	public static Terminal[] exec(DBCon dbCon, int restaurantID, String extraCond, String orderClause) throws SQLException{
		String sql = "SELECT * FROM " +  
     				 Params.dbName + ".terminal WHERE restaurant_id=" + restaurantID + " " +
					 (extraCond != null ? extraCond : " ") +
					 (orderClause != null ? orderClause : "");
		
		ArrayList<Terminal> terms = new ArrayList<Terminal>();
		dbCon.rs = dbCon.stmt.executeQuery(sql);	
		while(dbCon.rs.next()){
			Terminal terminal = new Terminal();
			terminal.restaurant_id = dbCon.rs.getInt("restaurant_id");
			terminal.expireDate = dbCon.rs.getDate("expire_date");
			terminal.owner = dbCon.rs.getString("owner_name");
			terminal.modelName = dbCon.rs.getString("model_name");
			float quota = dbCon.rs.getFloat("gift_quota");
			if(quota >= 0){
				terminal.setGiftQuota(quota);
			}
			terminal.setGiftAmount(dbCon.rs.getFloat("gift_amount"));
			terminal.modelID = dbCon.rs.getShort("model_id");
			terminal.pin = dbCon.rs.getInt("pin");
			terms.add(terminal);
		}
		return terms.toArray(new Terminal[terms.size()]);
	}
	
	/**
	 * Query the terminal according to the specific pin and model.
	 * @param pin
	 * 			the pin to this terminal
	 * @param model
	 * 			the model to this terminal
	 * @return	
	 * 			return the terminal instance, 
	 * 			return null if the terminal to query not exist 
	 * @throws SQLException
	 * 			throws if fail to execute the SQL statement
	 */
	public static Terminal exec(int pin, short model) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, pin, model);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Query the terminal according to the specific pin and model.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon
	 * 			the database connection
	 * @param pin
	 * 			the pin to this terminal
	 * @param model
	 * 			the model to this terminal
	 * @return	
	 * 			return the terminal instance,
	 * 			return null if the terminal to query not exist 
	 * @throws SQLException
	 * 			throws if fail to execute the SQL statement
	 */
	public static Terminal exec(DBCon dbCon, int pin, short model) throws SQLException{
		String sql = "SELECT restaurant_id, expire_date, owner_name, model_name, gift_quota, gift_amount FROM " +  
	     			Params.dbName + ".terminal WHERE pin=" + "0x" + Integer.toHexString(pin) +
	     			" AND model_id=" + model;
		dbCon.rs = dbCon.stmt.executeQuery(sql);		
		if(dbCon.rs.next()){
			Terminal terminal = new Terminal();
			terminal.restaurant_id = dbCon.rs.getInt("restaurant_id");
			terminal.expireDate = dbCon.rs.getDate("expire_date");
			terminal.owner = dbCon.rs.getString("owner_name");
			terminal.modelName = dbCon.rs.getString("model_name");
			float quota = dbCon.rs.getFloat("gift_quota");
			if(quota >= 0){
				terminal.setGiftQuota(quota);
			}
			terminal.setGiftAmount(dbCon.rs.getFloat("gift_amount"));
			terminal.modelID = model;
			terminal.pin = pin;
			return terminal;
		}else{
			return null;
		}
	}
	
}
