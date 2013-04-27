package com.wireless.db.regionMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.protocol.Terminal;

public class RegionDao {

	/**
	 * Update a specified region.
	 * @param region
	 * 			the region to update
	 * @throws SQLException
	 * 			if failed to execute any SQL statements
	 * @throws BusinessException
	 * 			if the region to update does NOT exist
	 */
	public static void update(Terminal term, Region region) throws SQLException, BusinessException {
		DBCon dbCon = new DBCon();
		try {
			
			dbCon.connect();
			update(dbCon, term, region);
			
		} finally {
			dbCon.disconnect();
		}
	}

	/**
	 * Update the region to a specified restaurant defined in {@link Terminal}.
	 * @param dbCon
	 * 			the database connection
	 * @param region
	 * 			the region to update
	 * @throws SQLException
	 * 			if failed to execute any SQL statements
	 * @throws BusinessException
	 * 			if the region to update does NOT exist
	 */
	public static void update(DBCon dbCon, Terminal term, Region region) throws SQLException, BusinessException{
		
		String sql;
		
		sql = " UPDATE " + Params.dbName + ".region " + 
			  " SET name = '" + region.getName() + "'" +
			  " WHERE restaurant_id = " + term.restaurantID +
			  " AND region_id = " + region.getRegionId();
		
		if (dbCon.stmt.executeUpdate(sql) == 0) {
			throw new BusinessException("操作失败，修改区域信息失败了！！");
		}
	}
	
	/**
	 * Get regions to a specified restaurant defined in terminal {@link Terminal} and other extra condition.
	 * @param term
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the list holding the region result
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static List<Region> getRegions(Terminal term, String extraCond, String orderClause) throws SQLException {
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getRegions(dbCon, term, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	
	/**
	 * Get regions to a specified restaurant defined in terminal {@link Terminal} and other extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the list holding the region result
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static List<Region> getRegions(DBCon dbCon, Terminal term, String extraCond, String orderClause) throws SQLException {
		List<Region> regions = new ArrayList<Region>();
		try {
			dbCon.connect();

			String sql = " SELECT " 
						+ " REGION.region_id, REGION.restaurant_id, REGION.name "
						+ " FROM " + Params.dbName + ".region REGION"
						+ " WHERE REGION.restaurant_id = " + term.restaurantID + " "
						+ (extraCond == null ? "" : extraCond)
						+ (orderClause == null ? "" : orderClause);
			dbCon.rs = dbCon.stmt.executeQuery(sql);

			while (dbCon.rs.next()) {
				regions.add(new Region(dbCon.rs.getShort("region_id"), 
									   dbCon.rs.getString("name"), 
									   dbCon.rs.getInt("restaurant_id")));
			}
			
			dbCon.rs.close();
			
			return regions;
			
		} finally {
			dbCon.disconnect();
		}
	}
	
}