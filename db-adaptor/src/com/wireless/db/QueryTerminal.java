package com.wireless.db;

import java.sql.SQLException;
import java.util.ArrayList;

import com.wireless.protocol.Terminal;

public class QueryTerminal {

	public final static int QUERY_ALL_TERM = 0;
	public final static int QUERY_REAL_TERM = 1;
	public final static int QUERY_VIRTUAL_TERM = 2;
	
	/**
	 * Query the terminal to a specific restaurant.
	 * @param dbCon
	 * 			the database connection
	 * @param restaurantID
	 * 			the restaurant id to query
	 * @param type
	 * 			one of three values below<br>
	 * 			QUERY_ALL_TERM<br>
	 * 			QUERY_REAL_TERM<br>
	 * 			QUERY_VIRTUAL_TERM
	 * @param extraCond
	 * 			the extra condition to the SQL statement
	 * @param orderClause 
	 * 			the order clause to the SQL statement
	 * @return
	 * 			return all the terminals to this restaurant and match the extra condition and order clause
	 * @throws SQLException
	 * 			throws if fail to execute the SQL statement
	 */
	public static Terminal[] exec(int restaurantID, int type, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, type, restaurantID, extraCond, orderClause);
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
	 * @param type
	 * 			one of three values below<br>
	 * 			QUERY_ALL_TERM<br>
	 * 			QUERY_REAL_TERM<br>
	 * 			QUERY_VIRTUAL_TERM
	 * @param extraCond
	 * 			the extra condition to the SQL statement
	 * @param orderClause 
	 * 			the order clause to the SQL statement
	 * @return
	 * 			return all the terminals to this restaurant and match the extra condition and order clause
	 * @throws SQLException
	 * 			throws if fail to execute the SQL statement
	 */
	public static Terminal[] exec(DBCon dbCon, int restaurantID, int type, String extraCond, String orderClause) throws SQLException{
		String extraType = null;
		if(type == QUERY_REAL_TERM){
			extraType = " AND model_id < 127 ";
		}else if(type == QUERY_VIRTUAL_TERM){
			extraType = " AND model_id > 127 ";
		}else{
			extraType = "";
		}
		String sql = "SELECT * FROM " +  
     				 Params.dbName + ".terminal WHERE restaurant_id=" + restaurantID + 
     				 extraType + " " +
					 (extraCond != null ? extraCond : " ") +
					 (orderClause != null ? orderClause : "");
		
		ArrayList<Terminal> terms = new ArrayList<Terminal>();
		dbCon.rs = dbCon.stmt.executeQuery(sql);	
		while(dbCon.rs.next()){
			Terminal terminal = new Terminal();
			terminal.id = dbCon.rs.getLong("terminal_id");
			terminal.restaurantID = dbCon.rs.getInt("restaurant_id");
			terminal.expireDate = dbCon.rs.getDate("expire_date");
			terminal.owner = dbCon.rs.getString("owner_name");
			terminal.modelName = dbCon.rs.getString("model_name");
			float quota = dbCon.rs.getFloat("gift_quota");
			if(quota >= 0){
				terminal.setGiftQuota(quota);
			}
			terminal.setGiftAmount(dbCon.rs.getFloat("gift_amount"));
			terminal.modelID = dbCon.rs.getShort("model_id");
			terminal.pin = dbCon.rs.getLong("pin");
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
	public static Terminal exec(long pin, short model) throws SQLException{
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
	public static Terminal exec(DBCon dbCon, long pin, short model) throws SQLException{
		String sql = " SELECT * FROM " +  
	     			Params.dbName + ".terminal " +
	     			" WHERE " +
	     			" pin=" + "0x" + Long.toHexString(pin) +
	     			" AND " +
	     			" (model_id=" + model +
	     			" OR " +
	     			" model_id=" + Terminal.MODEL_ADMIN + ")";
		dbCon.rs = dbCon.stmt.executeQuery(sql);		
		if(dbCon.rs.next()){
			Terminal terminal = new Terminal();
			terminal.id = dbCon.rs.getLong("terminal_id");
			terminal.restaurantID = dbCon.rs.getInt("restaurant_id");
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
