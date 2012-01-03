package com.wireless.db;

import java.sql.SQLException;
import java.util.ArrayList;

import com.wireless.exception.BusinessException;
import com.wireless.protocol.Region;
import com.wireless.protocol.Terminal;

public class QueryRegion {
	
	/**
	 * Get the regions that this terminal attached to.
	 * @param pin 
	 * 			the pin value to this terminal
	 * @param model
	 * 			the model value to this terminal
	 * @return the regions that the terminal attached to
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if either of cases below.<br>
	 * 			- The terminal is NOT attached to any restaurant.<br>
	 * 			- The terminal is expired.<br>
	 * 			- The restaurant this terminal attached to does NOT exist.
	 */
	public static Region[] exec(int pin, short model) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, pin, model);
			return exec(dbCon, term.restaurant_id, null, null);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the regions that this terminal attached to.
	 * @param extraCond
	 * 			the extra condition to the SQL statement
	 * @param orderClause
	 * 			the order clause to the SQL statement
	 * @param pin 
	 * 			the pin value to this terminal
	 * @param model
	 * 			the model value to this terminal
	 * @return the regions that the terminal attached to
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if either of cases below.<br>
	 * 			- The terminal is NOT attached to any restaurant.<br>
	 * 			- The terminal is expired.<br>
	 * 			- The restaurant this terminal attached to does NOT exist.
	 */
	public static Region[] exec(int pin, short model, String extraCond, String orderClause) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, pin, model);
			return exec(dbCon, term.restaurant_id, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the regions according to a specific restaurant id.
	 * @param restaurantID 
	 * 			the restaurant id
	 * @return the regions to this restaurant
	 * @throws SQLException 
	 * 			throws if any error occurred while execute the SQL statement
	 */
	public static Region[] exec(int restaurantID) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, restaurantID, null, null);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the regions according to a specific restaurant id.
	 * @param extraCond
	 * 			the extra condition to the SQL statement
	 * @param orderClause
	 * 			the order clause to the SQL statement
	 * @param restaurantID 
	 * 			the restaurant id
	 * @return the regions to this restaurant
	 * @throws SQLException 
	 * 			throws if any error occurred while execute the SQL statement
	 */
	public static Region[] exec(int restaurantID, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, restaurantID, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the regions according to a specific restaurant id.
	 * Note that the database should be connected before invoking this method.
	 * @param restaurantID 
	 * 			the restaurant id
	 * @param dbCon
	 * 			the database connection
	 * @return the regions to this restaurant
	 * @throws SQLException 
	 * 			throws if any error occurred while execute the SQL statement
	 */
	public static Region[] exec(DBCon dbCon, int restaurantID) throws SQLException{		
		return exec(dbCon, restaurantID, null, null);
	}
	
	/**
	 * Get the regions according to a specific restaurant id.
	 * Note that the database should be connected before invoking this method.
	 * @param restaurantID 
	 * 			the restaurant id
	 * @param dbCon
	 * 			the database connection
	 * @param extraCond
	 * 			the extra condition for the SQL statement
	 * @param orderClause
	 * 			
	 * @return the regions to this restaurant
	 * @throws SQLException 
	 * 			throws if any error occurred while execute the SQL statement
	 */
	public static Region[] exec(DBCon dbCon, int restaurantID, String extraCond, String orderClause) throws SQLException{
		
		ArrayList<Region> regions = new ArrayList<Region>();
		
		String sql = "SELECT * FROM " + Params.dbName +
			 		 ".region WHERE restaurant_id=" + restaurantID +
			 		 (extraCond == null ? "" : extraCond) +
			 		 (orderClause == null ? "" : orderClause);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			regions.add(new Region(dbCon.rs.getShort("region_id"),
								   dbCon.rs.getString("name")));
		}
		dbCon.rs.close();
		
		return regions.toArray(new Region[regions.size()]);
	}
	
	/**
	 * Get the region to a specific restaurant and table
	 * @param dbCon
	 * 			the database connection
	 * @param restaurantID
	 * 			the restaurant id
	 * @param tableID
	 * 			the table id
	 * @return the region information to a specific restaurant and table
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the table does NOT belong to any region
	 */
	public static Region exec(int restaurantID, int tableID) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, restaurantID, tableID);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the region to a specific restaurant and table
	 * @param dbCon
	 * 			the database connection
	 * @param restaurantID
	 * 			the restaurant id
	 * @param tableID
	 * 			the table id
	 * @return the region information to a specific restaurant and table
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the table does NOT belong to any region
	 */
	public static Region exec(DBCon dbCon, int restaurantID, int tableID) throws SQLException, BusinessException{
		String sql = "SELECT * FROM " + Params.dbName + 
					 ".region WHERE restaurant_id=" + restaurantID +
					 " AND region_id=" +
					 "(SELECT region_id FROM " + Params.dbName + ".table WHERE restaurant_id=" + restaurantID +
					 " AND alias_id=" + tableID + ")";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		if(dbCon.rs.next()){
			return new Region(dbCon.rs.getShort("region_id"),
							  dbCon.rs.getString("name"));
		}else{
			throw new BusinessException("The table(id=" + tableID + ") to restaurant(id=" + restaurantID + ") does NOT belong to any region.");
		}
	}
}
