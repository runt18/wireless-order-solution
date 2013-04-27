package com.wireless.db.frontBusiness;

import java.sql.SQLException;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.regionMgr.RegionDao;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.regionMgr.Region;
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
	public static Region[] exec(long pin, short model) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, pin, model);
			return exec(dbCon, term, null, null);
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
	public static Region[] exec(long pin, short model, String extraCond, String orderClause) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			Terminal term = VerifyPin.exec(dbCon, pin, model);
			return exec(dbCon, term, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the regions according to a specific restaurant id.
	 * @param term 
	 * 			the terminal to query region
	 * @return the regions to this restaurant
	 * @throws SQLException 
	 * 			throws if any error occurred while execute the SQL statement
	 */
	public static Region[] exec(Terminal term) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, term, null, null);
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
	 * @param term 
	 * 			the terminal to query region
	 * @return the regions to this restaurant
	 * @throws SQLException 
	 * 			throws if any error occurred while execute the SQL statement
	 */
	public static Region[] exec(Terminal term, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, term, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the regions according to a specific restaurant id.
	 * Note that the database should be connected before invoking this method.
	 * @param term 
	 * 			the terminal to query region
	 * @param dbCon
	 * 			the database connection
	 * @return the regions to this restaurant
	 * @throws SQLException 
	 * 			throws if any error occurred while execute the SQL statement
	 */
	public static Region[] exec(DBCon dbCon, Terminal term) throws SQLException{		
		return exec(dbCon, term, null, null);
	}
	
	/**
	 * Get the regions according to a specific restaurant id.
	 * Note that the database should be connected before invoking this method.
	 * @param term 
	 * 			the terminal to query region
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
	public static Region[] exec(DBCon dbCon, Terminal term, String extraCond, String orderClause) throws SQLException{

		List<Region> result = RegionDao.getRegions(dbCon, term, extraCond, orderClause);
		
		return result.toArray(new Region[result.size()]);
		
//		ArrayList<PRegion> regions = new ArrayList<PRegion>();
//		
//		String sql = " SELECT " +
//					 " region_id, restaurant_id, name " +
//					 " FROM " + Params.dbName + ".region " +
//					 " WHERE restaurant_id = " + term.restaurantID +
//			 		 (extraCond == null ? "" : extraCond) +
//			 		 (orderClause == null ? "" : orderClause);
//		
//		dbCon.rs = dbCon.stmt.executeQuery(sql);
//		while(dbCon.rs.next()){
//			regions.add(new PRegion(dbCon.rs.getShort("region_id"),
//								   dbCon.rs.getString("name"),
//								   dbCon.rs.getInt("restaurant_id")));
//		}
//		dbCon.rs.close();
//		
//		return regions.toArray(new PRegion[regions.size()]);
	}
	
	/**
	 * Get the region to a specific restaurant and table
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal to query
	 * @param tableID
	 * 			the table id
	 * @return the region information to a specific restaurant and table
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the table does NOT belong to any region
	 */
	public static Region exec(Terminal term, int tableID) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return execByTbl(dbCon, term, tableID);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the region to a specific restaurant and table
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal to query region
	 * @param tableID
	 * 			the table id
	 * @return the region information to a specific restaurant and table
	 * @throws SQLException
	 * 			throws if fail to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the table does NOT belong to any region
	 */
	public static Region execByTbl(DBCon dbCon, Terminal term, int tableID) throws SQLException, BusinessException{
//		String sql = " SELECT " +
//					 " region_id, restaurant_id, name " +
//					 " FROM " + Params.dbName + ".region " +
//					 " WHERE restaurant_id = " + term.restaurantID +
//					 " AND region_id=" +
//					 "(SELECT region_id FROM " + Params.dbName + ".table WHERE restaurant_id=" + term.restaurantID +
//					 " AND table_alias=" + tableID + ")";
//		dbCon.rs = dbCon.stmt.executeQuery(sql);
//		
//		if(dbCon.rs.next()){
//			return new PRegion(dbCon.rs.getShort("region_id"),
//							  dbCon.rs.getString("name"),
//							  dbCon.rs.getInt("restaurant_id"));
//		}else{
//			throw new BusinessException("The table(id=" + tableID + ", restaurant_id=" + term.restaurantID + ") does NOT belong to any region.");
//		}
		
		List<Region> result = RegionDao.getRegions(dbCon, term, 
							 					   " AND REGION.region_id = " +
							 					   " (SELECT region_id FROM " + Params.dbName + ".table WHERE restaurant_id = " + term.restaurantID +
							 					   " AND table_alias = " + tableID + ")", 
							 					   null);
		if(!result.isEmpty()){
			return result.get(0);
		}else{
			throw new BusinessException("The table(id=" + tableID + ", restaurant_id=" + term.restaurantID + ") does NOT belong to any region.");
		}
		
	}
}
