package com.wireless.db;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.wireless.exception.BusinessException;
import com.wireless.protocol.ErrorCode;
import com.wireless.protocol.Table;
import com.wireless.protocol.Terminal;

public class QueryTable {
	
	/**
	 * Get all the table information to restaurant that the terminal is attached to. 
	 * @param pin the pin to this terminal
	 * @param model the model to this terminal
	 * @return An array holding all the table information
	 * @throws BusinessException throws if the terminal is NOT attached to any restaurant
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static Table[] exec(int pin, short model) throws BusinessException, SQLException{		

		DBCon dbCon = new DBCon();

		try {   
			dbCon.connect();
			return exec(dbCon, pin, model, null, null);			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get all the table information to restaurant that the terminal is attached to. 
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon 
	 * 			the database connection
	 * @param pin 
	 * 			the pin to this terminal
	 * @param model 
	 * 			the model to this terminal
	 * @param extraCond
	 * 			the extra condition to the SQL statement
	 * @param orderClause
	 * 			the order clause to the SQL statement
	 * @return An array holding all the table information
	 * @throws BusinessException 
	 * 			throws if the terminal is NOT attached to any restaurant
	 * @throws SQLException 
	 * 			throws if fail to execute any SQL statement
	 */
	public static Table[] exec(int pin, short model, String extraCond, String orderClause) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, pin, model, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get all the table information to restaurant that the terminal is attached to. 
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon 
	 * 			the database connection
	 * @param pin 
	 * 			the pin to this terminal
	 * @param model 
	 * 			the model to this terminal
	 * @param extraCond
	 * 			the extra condition to the SQL statement
	 * @param orderClause
	 * 			the order clause to the SQL statement
	 * @return An array holding all the table information
	 * @throws BusinessException 
	 * 			throws if the terminal is NOT attached to any restaurant
	 * @throws SQLException 
	 * 			throws if fail to execute any SQL statement
	 */
	public static Table[] exec(DBCon dbCon, int pin, short model, String extraCond, String orderClause) throws BusinessException, SQLException{
		
		Terminal term = VerifyPin.exec(dbCon, pin, model);
		
		ArrayList<Table> tables = new ArrayList<Table>();
		
		//get the tables
		String sql = "SELECT * FROM " + Params.dbName + 
					 ".table WHERE restaurant_id=" +
					 term.restaurant_id + " " +
					 (extraCond != null ? extraCond : "") +
					 (orderClause != null ? orderClause : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			Table table = new Table();
			table.restaurantID = term.restaurant_id;
			table.alias_id = dbCon.rs.getInt("alias_id");
			table.name = dbCon.rs.getString("name");
			table.setMinimumCost(dbCon.rs.getFloat("minimum_cost"));
			table.custom_num = dbCon.rs.getShort("custom_num");
			table.category = dbCon.rs.getShort("category");
			table.status = dbCon.rs.getShort("status");
			table.regionID = dbCon.rs.getShort("region_id");
			table.setServiceRate(dbCon.rs.getFloat("service_rate"));
			tables.add(table);
		}
		dbCon.rs.close();


		Collections.sort(tables, new Comparator<Table>(){

			public int compare(Table table1, Table table2) {
				if(table1.alias_id > table2.alias_id){
					return 1;
				}else if(table1.alias_id < table2.alias_id){
					return -1;
				}else{
					return 0;
				}
			}
			
		});
		return tables.toArray(new Table[tables.size()]);
	}
	
	/**
	 * Get the table information according to specific alias id.
	 * @param pin the pin to this terminal
	 * @param model the model to this terminal
	 * @param tableID the table alias id to query
	 * @return the table information to specific alias id
	 * @throws BusinessException throws if either of cases below.<br>
	 * 							 - The terminal is NOT attached to any restaurant.<br>
	 * 							 - The table alias id to query does NOT exist.
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static Table exec(int pin, short model, int tableID) throws BusinessException, SQLException{
		
		DBCon dbCon = new DBCon();		
		
		try{
			dbCon.connect();

			return exec(dbCon, pin, model, tableID);
			
		}finally{
			dbCon.disconnect();
		}
		
	}
	
	/**
	 * Get the table information according the specific table alias id and restaurant id.
	 * Assure the terminal with this pin is NOT expired before invoking this method.
	 * Note that the database should be connected before invoking this method
	 * @param dbCon the database connection
	 * @param pin the pin to this terminal
	 * @param model the model to this terminal
	 * @param tableID the table alias id to query
	 * @return the table information
	 * @throws BusinessException throws if the table to query does NOT exist
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static Table exec(DBCon dbCon, int pin, short model, int tableID) throws BusinessException, SQLException{
		return exec(dbCon, pin, model, tableID, null, null);
	}
	
	/**
	 * Get the table information according the specific table alias id and restaurant id.
	 * Assure the terminal with this pin is NOT expired before invoking this method.
	 * @param dbCon the database connection
	 * @param pin the pin to this terminal
	 * @param model the model to this terminal
	 * @param tableID the table alias id to query
	 * @param extraCond the extra condition to the SQL statement
	 * @param orderClause the order clause to the SQL statement
	 * @return the table information
	 * @throws BusinessException throws if the table to query does NOT exist
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static Table exec(int pin, short model, int tableID, String extraCond, String orderClause) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();		
		
		try{
			dbCon.connect();

			return exec(dbCon, pin, model, tableID, extraCond, orderClause);
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the table information according the specific table alias id and restaurant id.
	 * Assure the terminal with this pin is NOT expired before invoking this method.
	 * Note that the database should be connected before invoking this method
	 * @param dbCon the database connection
	 * @param pin the pin to this terminal
	 * @param model the model to this terminal
	 * @param tableID the table alias id to query
	 * @param extraCond the extra condition to the SQL statement
	 * @param orderClause the order clause to the SQL statement
	 * @return the table information
	 * @throws BusinessException throws if the table to query does NOT exist
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static Table exec(DBCon dbCon, int pin, short model, int tableID, String extraCond, String orderClause) throws BusinessException, SQLException{
		
		Terminal term = VerifyPin.exec(dbCon, pin, model);
		
		//get the tables
		String sql = "SELECT * FROM " + Params.dbName + 
					 ".table WHERE restaurant_id=" +
					 term.restaurant_id + " AND alias_id=" +
					 tableID + " " +
					 (extraCond != null ? extraCond : " ") +
					 (orderClause != null ? orderClause : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		Table table = new Table();
		if(dbCon.rs.next()){
			table.restaurantID = term.restaurant_id;
			table.alias_id = tableID;
			table.name = dbCon.rs.getString("name");
			table.setMinimumCost(dbCon.rs.getFloat("minimum_cost"));
			table.custom_num = dbCon.rs.getShort("custom_num");
			table.category = dbCon.rs.getShort("category");
			table.status = dbCon.rs.getShort("status");
			table.regionID = dbCon.rs.getShort("region_id");
			table.setServiceRate(dbCon.rs.getFloat("service_rate"));
		}else{
			throw new BusinessException("The table(alias_id=" + tableID + ") to query does NOT exist.", ErrorCode.TABLE_NOT_EXIST);
		}
		dbCon.rs.close();

		return table;
		
	}
	
}

