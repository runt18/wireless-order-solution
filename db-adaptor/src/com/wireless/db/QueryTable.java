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
	public static Table[] exec(long pin, short model) throws BusinessException, SQLException{		

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
	public static Table[] exec(long pin, short model, String extraCond, String orderClause) throws BusinessException, SQLException{
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
	public static Table[] exec(DBCon dbCon, long pin, short model, String extraCond, String orderClause) throws BusinessException, SQLException{
		
		return exec(dbCon, VerifyPin.exec(dbCon, pin, model), extraCond, orderClause);
		
	}
	
	/**
	 * Get all the table information to restaurant that the terminal is attached to. 
	 * @param term 
	 * 			the terminal that restaurant is attached to
	 * @return An array holding all the table information
	 * @throws BusinessException 
	 * 			throws if the terminal is NOT attached to any restaurant
	 * @throws SQLException 
	 * 			throws if fail to execute any SQL statement
	 */
	public static Table[] exec(Terminal term) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, term, null, null);
		}finally{
			dbCon.disconnect();
		}
	}	
	
	/**
	 * Get all the table information to restaurant that the terminal is attached to. 
	 * @param term 
	 * 			the terminal that restaurant is attached to
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
	public static Table[] exec(Terminal term, String extraCond, String orderClause) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, term, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get all the table information to restaurant that the terminal is attached to. 
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon 
	 * 			the database connection
	 * @param term 
	 * 			the terminal that restaurant is attached to
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
	public static Table[] exec(DBCon dbCon, Terminal term, String extraCond, String orderClause) throws BusinessException, SQLException{
		
		ArrayList<Table> tables = new ArrayList<Table>();
		
		//get the tables
		String sql = "SELECT * FROM " + Params.dbName + 
					 ".table WHERE restaurant_id=" +
					 term.restaurantID + " " +
					 (extraCond != null ? extraCond : "") +
					 (orderClause != null ? orderClause : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			Table table = new Table();
			table.restaurantID = term.restaurantID;
			table.tableID = dbCon.rs.getInt("table_id");
			table.aliasID = dbCon.rs.getInt("table_alias");
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
				if(table1.aliasID > table2.aliasID){
					return 1;
				}else if(table1.aliasID < table2.aliasID){
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
	 * @param tableAlias the table alias id to query
	 * @return the table information to specific alias id
	 * @throws BusinessException throws if either of cases below.<br>
	 * 							 - The terminal is NOT attached to any restaurant.<br>
	 * 							 - The table alias id to query does NOT exist.
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static Table exec(long pin, short model, int tableAlias) throws BusinessException, SQLException{
		
		DBCon dbCon = new DBCon();		
		
		try{
			dbCon.connect();

			return exec(dbCon, pin, model, tableAlias);
			
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
	 * @param tableAlias the table alias id to query
	 * @return the table information
	 * @throws BusinessException throws if the table to query does NOT exist
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static Table exec(DBCon dbCon, long pin, short model, int tableAlias) throws BusinessException, SQLException{
		return exec(dbCon, pin, model, tableAlias, null, null);
	}
	
	/**
	 * Get the table information according the specific table alias id and restaurant id.
	 * Assure the terminal with this pin is NOT expired before invoking this method.
	 * @param dbCon the database connection
	 * @param pin the pin to this terminal
	 * @param model the model to this terminal
	 * @param tableAlias the table alias id to query
	 * @param extraCond the extra condition to the SQL statement
	 * @param orderClause the order clause to the SQL statement
	 * @return the table information
	 * @throws BusinessException throws if the table to query does NOT exist
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static Table exec(long pin, short model, int tableAlias, String extraCond, String orderClause) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();		
		
		try{
			dbCon.connect();

			return exec(dbCon, pin, model, tableAlias, extraCond, orderClause);
			
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
	 * @param tableAlias the table alias id to query
	 * @param extraCond the extra condition to the SQL statement
	 * @param orderClause the order clause to the SQL statement
	 * @return the table information
	 * @throws BusinessException throws if the table to query does NOT exist
	 * @throws SQLException throws if fail to execute any SQL statement
	 */
	public static Table exec(DBCon dbCon, long pin, short model, int tableAlias, String extraCond, String orderClause) throws BusinessException, SQLException{
		
		Terminal term = VerifyPin.exec(dbCon, pin, model);
		
		return exec(dbCon, term, tableAlias, extraCond, orderClause);
		
	}
	
	/**
	 * Get the table according to the specific restaurant and table id
	 * @param terminal
	 * 			the terminal to query
	 * @param tableAlias
	 * 			the table id
	 * @return the table information
	 * @throws BusinessException
	 * 			throws if the table to query does NOT exist
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 */
	public static Table exec(Terminal term, int tableAlias) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, term, tableAlias, null, null);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the table according to the specific restaurant and table id
	 * @param terminal
	 * 			the terminal to query
	 * @param tableAlias
	 * 			the table id
	 * @param extraCond
	 * 			the extra condition to SQL statement
	 * @param orderClause
	 * 			the order clause to SQL statement
	 * @return the table information
	 * @throws BusinessException
	 * 			throws if the table to query does NOT exist
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 */
	public static Table exec(Terminal term, int tableAlias, String extraCond, String orderClause) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, term, tableAlias, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the table according to the specific restaurant and table id.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon
	 * 			the database connection
	 * @param terminal
	 * 			the terminal to query
	 * @param tableAlias
	 * 			the table id
	 * @return the table information
	 * @throws BusinessException
	 * 			throws if the table to query does NOT exist
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 */
	public static Table exec(DBCon dbCon, Terminal term, int tableAlias) throws BusinessException, SQLException{
		return exec(dbCon, term, tableAlias, null, null);
	}
	
	/**
	 * Get the table according to the specific restaurant and table id.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon
	 * 			the database connection
	 * @param terminal
	 * 			the terminal to query
	 * @param tableAliasID
	 * 			the table id
	 * @param extraCond
	 * 			the extra condition to SQL statement
	 * @param orderClause
	 * 			the order clause to SQL statement
	 * @return the table information
	 * @throws BusinessException
	 * 			throws if the table to query does NOT exist
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 */
	public static Table exec(DBCon dbCon, Terminal term, int tableAliasID, String extraCond, String orderClause) throws BusinessException, SQLException{
		//get the tables
		String sql = "SELECT * FROM " + Params.dbName + 
					 ".table WHERE restaurant_id=" +
					 term.restaurantID + " AND table_alias=" +
					 tableAliasID + " " +
					 (extraCond != null ? extraCond : " ") +
					 (orderClause != null ? orderClause : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		Table table = new Table();
		if(dbCon.rs.next()){
			table.restaurantID = term.restaurantID;
			table.tableID = dbCon.rs.getInt("table_id");
			table.aliasID = tableAliasID;
			table.name = dbCon.rs.getString("name");
			table.setMinimumCost(dbCon.rs.getFloat("minimum_cost"));
			table.custom_num = dbCon.rs.getShort("custom_num");
			table.category = dbCon.rs.getShort("category");
			table.status = dbCon.rs.getShort("status");
			table.regionID = dbCon.rs.getShort("region_id");
			table.setServiceRate(dbCon.rs.getFloat("service_rate"));
		}else{
			throw new BusinessException("The table(alias_id=" + tableAliasID + ", restaurant_id=" + term.restaurantID + ") to query does NOT exist.", ErrorCode.TABLE_NOT_EXIST);
		}
		dbCon.rs.close();
		
		return table;
	}
	
}

