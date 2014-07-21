package com.wireless.db.regionMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.RegionError;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.staffMgr.Staff;

public class RegionDao {

	/**
	 * Move the department up to another.  
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the move builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the department to move does NOT exist
	 */
	public static void move(DBCon dbCon, Staff staff, Region.MoveBuilder builder) throws SQLException, BusinessException{
		Region from = getById(dbCon, staff, builder.from());
		Region to = getById(dbCon, staff, builder.to());
		
		String sql;
		if(from.getDisplayId() < to.getDisplayId()){
			sql = " UPDATE " + Params.dbName + ".region SET " +
				  " display_id = display_id - 1 " +
				  " WHERE 1 = 1 " +
				  " AND display_id > " + from.getDisplayId() +
				  " AND display_id < " + to.getDisplayId() + 
				  " AND restaurant_id = " + staff.getRestaurantId();
			dbCon.stmt.executeUpdate(sql);
			
			sql = " UPDATE " + Params.dbName + ".region SET " +
				  " display_id = " + (to.getDisplayId() - 1) +
				  " WHERE retion_id = " + from.getId() + 
				  " AND restaurant_id = " + staff.getRestaurantId();
			dbCon.stmt.executeUpdate(sql);
			
		}else if(from.getDisplayId() > to.getDisplayId()){
			sql = " UPDATE " + Params.dbName + ".region SET " +
				  " display_id = display_id + 1 " +
				  " WHERE 1 = 1 " +
				  " AND display_id >= " + to.getDisplayId() +
				  " AND display_id < " + from.getDisplayId() +
				  " AND restaurant_id = " + staff.getRestaurantId();
			dbCon.stmt.executeUpdate(sql);
			
			sql = " UPDATE " + Params.dbName + ".region SET " +
				  " display_id = " + to.getDisplayId() +
				  " WHERE region_id = " + from.getId() +
				  " AND restaurant_id = " + staff.getRestaurantId();
			dbCon.stmt.executeUpdate(sql);
		}
		
	}
	
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
		Region region = builder.build();
		String sql;
		
		int displayId = 0;
		
		//Calculate the display id in case of normal kitchen.
		sql = " SELECT IFNULL(MAX(display_id), 0) + 1 FROM " + Params.dbName + ".region WHERE restaurant_id = " + staff.getRestaurantId();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			displayId = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		
		sql = " INSERT INTO " + Params.dbName + ".region " + 
			  " (restaurant_id, region_id, name, status, display_id) " +
			  " VALUES (" +
			  staff.getRestaurantId() + "," +
			  region.getId() + "," +
			  "'" + region.getName() + "'," +
			  region.getStatus().getVal() + "," +
			  displayId +
			  ")";
		dbCon.stmt.executeUpdate(sql);
		
		return region.getId();
	}
	
	/**
	 * Add the region from the idle pool.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to add region
	 * @return the region id to add
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			<li>throws if insufficient idle region
	 * 			<li>throws if the region to add 
	 */
	public static short add(Staff staff, Region.AddBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return add(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Add the region from the idle pool.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to add region
	 * @return the region id to add
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			<li>throws if insufficient idle region
	 * 			<li>throws if the region to add 
	 */
	public static short add(DBCon dbCon, Staff staff, Region.AddBuilder builder) throws SQLException, BusinessException{
		String sql;
		//Check to see whether any unused kitchens exist.
		sql = " SELECT region_id FROM " + Params.dbName + ".region " +
			  " WHERE 1 = 1 " +
			  " AND status = " + Region.Status.IDLE.getVal() + 
			  " AND restaurant_id = " + staff.getRestaurantId() +
			  " ORDER BY region_id LIMIT 1 ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		short regionId = 0;
		if(dbCon.rs.next()){
			regionId = dbCon.rs.getShort(1);
		}else{
			throw new BusinessException(RegionError.INSUFFICIENT_IDLE_REGION);
		}
		dbCon.rs.close();
		
		Region region = builder.build();
		sql = " UPDATE " + Params.dbName + ".region SET " +
			  " region_id = " + regionId +
			  " ,name = '" + region.getName() + "'" +
			  " ,status = " + Region.Status.BUSY.getVal() +
			  " WHERE 1 = 1 " +
			  " AND region_id = " + regionId +
			  " AND restaurant_id = " + staff.getRestaurantId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(RegionError.REGION_NOT_EXIST);
		}
		
		return regionId;
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
			  " AND region_id = " + region.getId();
		
		if (dbCon.stmt.executeUpdate(sql) == 0) {
			throw new BusinessException(RegionError.REGION_NOT_EXIST);
		}
	}
	
	/**
	 * Remove a region and put it back to idle pool.
	 * @param staff
	 * 			the staff to perform this action
	 * @param deptId
	 * 			the region id to remove
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			<li>throws if the region is NOT empty
	 * 			<li>throws if the region to remove does NOT exist
	 */
	public static void remove(Staff staff, int regionId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			remove(dbCon, staff, regionId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Remove a region and put it back to idle pool.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param deptId
	 * 			the region id to remove
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			<li>throws if the region is NOT empty
	 * 			<li>throws if the region to remove does NOT exist
	 */
	public static void remove(DBCon dbCon, Staff staff, int regionId) throws SQLException, BusinessException{
		String sql;
		
		//Check to see whether the region contains any table.
		sql = " SELECT table_id FROM " + Params.dbName + ".table WHERE " +
			  " restaurant_id = " + staff.getRestaurantId() + 
			  " AND region_id = " + regionId +
			  " LIMIT 1 ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			throw new BusinessException(RegionError.REGION_NOT_EMPTY);
		}
		dbCon.rs.close();
		
		//Delete the print scheme associated with this region.
		sql = " DELETE FROM " + Params.dbName + ".func_region WHERE 1 = 1 " +
			  " AND region_id = " + regionId +
			  " AND restaurant_id = " + staff.getRestaurantId();
		dbCon.stmt.executeUpdate(sql);
		
		//Update the department status to idle.
		sql = " UPDATE " + Params.dbName + ".region SET " +
			  " region_id = " + regionId +
			  " ,status = " + Region.Status.IDLE.getVal() +
			  " WHERE 1 = 1 " +
			  " AND restaurant_id = " + staff.getRestaurantId() +
			  " AND region_id = " + regionId; 
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(RegionError.REGION_NOT_EXIST);
		}
	}
	
	/**
	 * Get the region to specific id
	 * @param staff
	 * 			the staff to perform this action
	 * @param regionId
	 * 			the region id
	 * @return the region to this specific id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the region to this id does NOT exist
	 */
	public static Region getById(Staff staff, int regionId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, staff, regionId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the region to specific id
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param regionId
	 * 			the region id
	 * @return the region to this specific id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the region to this id does NOT exist
	 */
	public static Region getById(DBCon dbCon, Staff staff, int regionId) throws SQLException, BusinessException{
		List<Region> result = getByCond(dbCon, staff, " AND REGION.region_id = " + regionId, null);
		if(result.isEmpty()){
			throw new BusinessException(RegionError.REGION_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Get all the regions to specific restaurant.
	 * @param staff
	 * 			the staff to perform this action
	 * @return the regions to this restaurant
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Region> getAll(Staff staff) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getAll(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get all the regions to specific restaurant.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the regions to this restaurant
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Region> getAll(DBCon dbCon, Staff staff) throws SQLException{
		return getByCond(dbCon, staff, null, null);
	}
	
	/**
	 * Get the region according to specific status.
	 * @param staff
	 * 			the staff to perform this action
	 * @param status
	 * 			the status {@link Status}
	 * @return the regions to specific status
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Region> getByStatus(Staff staff, Region.Status status) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByStatus(dbCon, staff, status);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the region according to specific status.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param status
	 * 			the status {@link Status}
	 * @return the regions to specific status
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Region> getByStatus(DBCon dbCon, Staff staff, Region.Status status) throws SQLException{
		return getByCond(dbCon, staff, " AND REGION.status = " + status.getVal(), null);
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
	private static List<Region> getByCond(DBCon dbCon, Staff staff, String extraCond, String orderClause) throws SQLException {
		List<Region> result = new ArrayList<Region>();

		String sql = " SELECT " +
				     " REGION.region_id, REGION.restaurant_id, REGION.name, REGION.status, REGION.display_id " +
				     " FROM " + Params.dbName + ".region REGION" +
				     " WHERE REGION.restaurant_id = " + staff.getRestaurantId() + " " +
				     (extraCond == null ? "" : extraCond) +
				     (orderClause == null ? " ORDER BY display_id " : orderClause);
		dbCon.rs = dbCon.stmt.executeQuery(sql);

		while (dbCon.rs.next()) {
			Region region = new Region(dbCon.rs.getShort("region_id"));
			region.setName(dbCon.rs.getString("name"));
			region.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			region.setStatus(Region.Status.valueOf(dbCon.rs.getInt("status")));
			region.setDisplayId(dbCon.rs.getInt("display_id"));
			
			result.add(region);
		}
		
		dbCon.rs.close();
		
		return result;
			
	}
	
}