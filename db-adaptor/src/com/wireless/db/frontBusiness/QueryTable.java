package com.wireless.db.frontBusiness;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.ProtocolError;
import com.wireless.protocol.PTable;
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
	public static PTable[] exec(long pin, short model) throws BusinessException, SQLException{		

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
	public static PTable[] exec(long pin, short model, String extraCond, String orderClause) throws BusinessException, SQLException{
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
	public static PTable[] exec(DBCon dbCon, long pin, short model, String extraCond, String orderClause) throws BusinessException, SQLException{
		
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
	public static PTable[] exec(Terminal term) throws BusinessException, SQLException{
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
	public static PTable[] exec(Terminal term, String extraCond, String orderClause) throws BusinessException, SQLException{
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
	public static PTable[] exec(DBCon dbCon, Terminal term, String extraCond, String orderClause) throws BusinessException, SQLException{
		
		ArrayList<PTable> tables = new ArrayList<PTable>();
		
		//get the tables
		String sql = "SELECT * FROM " + Params.dbName + 
					 ".table WHERE restaurant_id=" +
					 term.restaurantID + " " +
					 (extraCond != null ? extraCond : "") +
					 (orderClause != null ? orderClause : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			PTable table = new PTable();
			table.setRestaurantId(term.restaurantID);
			table.setTableId(dbCon.rs.getInt("table_id"));
			table.setAliasId(dbCon.rs.getInt("table_alias"));
			table.setName(dbCon.rs.getString("name"));
			table.setMinimumCost(dbCon.rs.getFloat("minimum_cost"));
			table.setCustomNum(dbCon.rs.getShort("custom_num"));
			table.setCategory(dbCon.rs.getShort("category"));
			table.setStatus(dbCon.rs.getShort("status"));
			table.regionID = dbCon.rs.getShort("region_id");
			table.setServiceRate(dbCon.rs.getFloat("service_rate"));
			tables.add(table);
		}
		dbCon.rs.close();


		Collections.sort(tables, new Comparator<PTable>(){

			public int compare(PTable table1, PTable table2) {
				if(table1.getAliasId() > table2.getAliasId()){
					return 1;
				}else if(table1.getAliasId() < table2.getAliasId()){
					return -1;
				}else{
					return 0;
				}
			}
			
		});
		return tables.toArray(new PTable[tables.size()]);
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
	public static PTable exec(long pin, short model, int tableAlias) throws BusinessException, SQLException{
		
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
	public static PTable exec(DBCon dbCon, long pin, short model, int tableAlias) throws BusinessException, SQLException{
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
	public static PTable exec(long pin, short model, int tableAlias, String extraCond, String orderClause) throws BusinessException, SQLException{
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
	public static PTable exec(DBCon dbCon, long pin, short model, int tableAlias, String extraCond, String orderClause) throws BusinessException, SQLException{
		
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
	public static PTable exec(Terminal term, int tableAlias) throws BusinessException, SQLException{
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
	public static PTable exec(Terminal term, int tableAlias, String extraCond, String orderClause) throws BusinessException, SQLException{
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
	public static PTable exec(DBCon dbCon, Terminal term, int tableAlias) throws BusinessException, SQLException{
		return exec(dbCon, term, tableAlias, null, null);
	}
	
	/**
	 * Get the table according to the specific restaurant and table id.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon
	 * 			the database connection
	 * @param terminal
	 * 			the terminal to query
	 * @param tableAlias
	 * 			the table alias
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
	public static PTable exec(DBCon dbCon, Terminal term, int tableAlias, String extraCond, String orderClause) throws BusinessException, SQLException{
		//get the tables
		String sql = " SELECT " +
					 " * " +
					 " FROM " + Params.dbName + ".table " +
					 " WHERE " +
					 " restaurant_id = " + term.restaurantID + 
					 " AND " +
					 " table_alias = " + tableAlias + 
					 (extraCond != null ? extraCond : " ") +
					 (orderClause != null ? orderClause : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		PTable table = new PTable();
		if(dbCon.rs.next()){
			table.setRestaurantId(term.restaurantID);
			table.setTableId(dbCon.rs.getInt("table_id"));
			table.setAliasId(tableAlias);
			table.setName(dbCon.rs.getString("name"));
			table.setMinimumCost(dbCon.rs.getFloat("minimum_cost"));
			table.setCustomNum(dbCon.rs.getShort("custom_num"));
			table.setCategory(dbCon.rs.getShort("category"));
			table.setStatus(dbCon.rs.getShort("status"));
			table.regionID = dbCon.rs.getShort("region_id");
			table.setServiceRate(dbCon.rs.getFloat("service_rate"));
		}else{
			throw new BusinessException("The table(alias_id=" + tableAlias + ", restaurant_id=" + term.restaurantID + ") to query does NOT exist.", ProtocolError.TABLE_NOT_EXIST);
		}
		dbCon.rs.close();
		
		return table;
	}
	
}

