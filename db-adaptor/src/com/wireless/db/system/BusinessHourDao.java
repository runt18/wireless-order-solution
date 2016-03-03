package com.wireless.db.system;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.RestaurantError;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.system.BusinessHour;

public class BusinessHourDao {

	public static class ExtraCond {
		private int id;
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		@Override
		public String toString(){
			final StringBuilder extraCond = new StringBuilder();
			if(id != 0){
				extraCond.append(" AND id = " + id);
			}
			return extraCond.toString();
		}
	}
	
	/**
	 * Insert the business hour according to specific builder {@link BusinessHour#InsertBuilder}.
	 * @param builder
	 * 			the insert builder
	 * @param staff
	 * 			the staff to perform this action
	 * @return the id to business hour just insert
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(Staff staff, BusinessHour.InsertBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert the business hour according to specific builder {@link BusinessHour#InsertBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the insert builder
	 * @return the id to business hour just insert
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(DBCon dbCon, Staff staff, BusinessHour.InsertBuilder builder) throws SQLException{
		BusinessHour bh = builder.build();
		String sql = " INSERT INTO business_hour ( name, opening, ending, restaurant_id) "
				+ "VALUES("
				+ "'" + bh.getName() + "',"
				+ "'" + bh.getOpeningFormat() + "',"
				+ "'" + bh.getEndingFormat() + "',"
				+ staff.getRestaurantId()
				+ ")";
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		if(dbCon.rs.next()){
			bh.setId(dbCon.rs.getInt(1));
		}
		return bh.getId();
	}
	
	/**
	 * Update the business hour according to specific builder {@link BusinessHour#UpdateBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the business hour to update does NOT exist
	 */
	public static void update(Staff staff, BusinessHour.UpdateBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the business hour according to specific builder {@link BusinessHour#UpdateBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the business hour to update does NOT exist
	 */
	public static void update(DBCon dbCon, Staff staff, BusinessHour.UpdateBuilder update) throws SQLException, BusinessException{
		BusinessHour bh = update.build();
		String sql;
		sql = " UPDATE " + Params.dbName + ".business_hour SET " +
			  " id = " + bh.getId() + 
			  " ,name = '" + bh.getName() + "'" +
			  " ,opening = '" + bh.getOpeningFormat() + "'" +
			  " ,ending = '" + bh.getEndingFormat() + "'" +
			  " WHERE id = " + bh.getId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(RestaurantError.BUSINESS_HOUR_NOT_FOUND);
		}
	}

	/**
	 * Delete businessHour by id. 
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the id to business hour 
	 * @throws SQLException
	 *			if failed to execute any SQL Statement  			
	 * @throws BusinessException
	 * 			if the supplier to delete does NOT exist
	 */
	public static void deleteById(Staff staff, int id) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			deleteById(dbCon, staff, id);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete businessHour by id. 
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param id
	 * 			the id to business hour 
	 * @throws SQLException
	 *			if failed to execute any SQL Statement  			
	 * @throws BusinessException
	 * 			if the supplier to delete does NOT exist
	 */
	public static void deleteById(DBCon dbCon, Staff staff, int id) throws SQLException, BusinessException{
		if(deleteByCond(dbCon, staff, new ExtraCond().setId(id)) == 0){
			throw new BusinessException(RestaurantError.BUSINESS_HOUR_NOT_FOUND);
		}
	}
	
	/**
	 * Delete the business hour according to specific extra condition {@link ExtraCond}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the amount to business hour deleted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int deleteByCond(Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return deleteByCond(dbCon, staff, extraCond);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the business hour according to specific extra condition {@link ExtraCond}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @return the amount to business hour deleted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	private static int deleteByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException{
		int amount = 0;
		for(BusinessHour bh : getByCond(dbCon, staff, extraCond, null)){
			String sql;
			sql = " DELETE FROM " + Params.dbName + ".business_hour " +
				  " WHERE id = " + bh.getId();
			if(dbCon.stmt.executeUpdate(sql) != 0){
				amount++;
			}
		}
		return amount;
	}
	
	/**
	 * Get the businessHours according extra condition.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @throws SQLException
	 * 			if failed to execute any SQL Statement 
	 */
	public static List<BusinessHour> getByCond(Staff staff, ExtraCond extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCond(dbCon, staff, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the businessHours according extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @throws SQLException
	 * 			if failed to execute any SQL Statement 
	 */
	public static List<BusinessHour> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond, String orderClause) throws SQLException{
		final List<BusinessHour> result = new ArrayList<BusinessHour>();
		String sql;
		sql = " SELECT " +
			  " id, restaurant_id, name, opening, ending " +
			  " FROM " + Params.dbName + ".business_hour " +
			  " WHERE restaurant_id = " + staff.getRestaurantId() + " " +
			  (extraCond == null ? "" : extraCond.toString()) + " " +
			  (orderClause == null ? "" : orderClause);
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		while(dbCon.rs.next()){
			BusinessHour bh = new BusinessHour(dbCon.rs.getInt("id"));
			bh.setName(dbCon.rs.getString("name"));
			bh.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			bh.setEnding(dbCon.rs.getTime("ending").getTime());
			bh.setOpening(dbCon.rs.getTime("opening").getTime());
			result.add(bh);
		}
		
		dbCon.rs.close();
		return result;
	}

}
