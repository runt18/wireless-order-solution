package com.wireless.db.regionMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.staffMgr.Staff;

public class RegionDao {

	/**
	 * Insert a new region.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to insert a new region
	 * @return the id to region just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static short insert(Staff staff, Region.InsertBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert a new region.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to insert a new region
	 * @return the id to region just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static short insert(DBCon dbCon, Staff staff, Region.InsertBuilder builder) throws SQLException{
		Region regionToInsert = builder.build();
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".region " + 
			  " (restaurant_id, region_id, name) " +
			  " VALUES (" +
			  regionToInsert.getRestaurantId() + "," +
			  regionToInsert.getRegionId() + "," +
			  "'" + regionToInsert.getName() + "'" +
			  ")";
		dbCon.stmt.executeUpdate(sql);
		
		return regionToInsert.getRegionId();
	}
	
	/**
	 * Update a specified region.
	 * @param builder
	 * 			the region to update
	 * @throws SQLException
	 * 			if failed to execute any SQL statements
	 * @throws BusinessException
	 * 			if the region to update does NOT exist
	 */
	public static void update(Staff staff, Region.UpdateBuilder builder) throws SQLException, BusinessException {
		DBCon dbCon = new DBCon();
		try {
			
			dbCon.connect();
			update(dbCon, staff, builder);
			
		} finally {
			dbCon.disconnect();
		}
	}

	/**
	 * Update the region to a specified restaurant defined in {@link Staff}.
	 * @param dbCon
	 * 			the database connection
	 * @param builder
	 * 			the region to update
	 * @throws SQLException
	 * 			if failed to execute any SQL statements
	 * @throws BusinessException
	 * 			if the region to update does NOT exist
	 */
	public static void update(DBCon dbCon, Staff staff, Region.UpdateBuilder builder) throws SQLException, BusinessException{
		
		String sql;
		
		Region region = builder.build();
		
		sql = " UPDATE " + Params.dbName + ".region " + 
			  " SET name = '" + region.getName() + "'" +
			  " WHERE restaurant_id = " + staff.getRestaurantId() +
			  " AND region_id = " + region.getRegionId();
		
		if (dbCon.stmt.executeUpdate(sql) == 0) {
			throw new BusinessException("操作失败，修改区域信息失败了！！");
		}
	}
	
	/**
	 * Get regions to a specified restaurant defined in terminal {@link Staff} and other extra condition.
	 * @param staff
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the list holding the region result 
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static List<Region> getRegions(Staff staff, String extraCond, String orderClause) throws SQLException {
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getRegions(dbCon, staff, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	
	/**
	 * Get regions to a specified restaurant defined in terminal {@link Staff} and other extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the list holding the region result
	 * @throws SQLException
	 * 			if failed to execute any SQL statement
	 */
	public static List<Region> getRegions(DBCon dbCon, Staff staff, String extraCond, String orderClause) throws SQLException {
		List<Region> regions = new ArrayList<Region>();

		String sql = " SELECT " 
					+ " REGION.region_id, REGION.restaurant_id, REGION.name "
					+ " FROM " + Params.dbName + ".region REGION"
					+ " WHERE REGION.restaurant_id = " + staff.getRestaurantId() + " "
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
			
	}
	
}