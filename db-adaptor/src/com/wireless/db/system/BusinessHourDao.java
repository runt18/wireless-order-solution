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

	/**
	 * 
	 * @param dbCon
	 * @param builder
	 * @return
	 * @throws SQLException
	 */
	public static int insert(Staff staff, BusinessHour.InsertBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static int insert(DBCon dbCon, Staff staff, BusinessHour.InsertBuilder builder) throws SQLException{
		BusinessHour bh = builder.build();
		String insertSQl = " INSERT INTO business_hour ( name, opening, ending, restaurant_id) "
				+ "VALUES("
				+ "'" + bh.getName() + "',"
				+ "'" + bh.getOpeningFormat() + "',"
				+ "'" + bh.getEndingFormat() + "',"
				+ staff.getRestaurantId()
				+ ")";
		dbCon.stmt.executeUpdate(insertSQl, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		if(dbCon.rs.next()){
			bh.setId(dbCon.rs.getInt(1));
		}
		return bh.getId();
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param update
	 * @return
	 * @throws SQLException
	 * @throws BusinessException 
	 */
	public static void update(Staff staff, BusinessHour.UpdateBuilder update) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, staff, update);
		}finally{
			dbCon.disconnect();
		}
	}
	
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
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param supplierId
	 * 			the supplier_id of supplier
	 * @throws SQLException
	 *			if failed to execute any SQL Statement  			
	 * @throws BusinessException
	 * 			if the supplier to delete does not exist
	 */
	public static void delete(int id) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			delete(dbCon, id);
		}finally{
			dbCon.disconnect();
		}
	}
	
	public static void delete(DBCon dbCon, int id) throws SQLException, BusinessException{
		delete(dbCon," AND id = " + id);
	}
	
	private static void delete(DBCon dbCon, String extraCond) throws SQLException, BusinessException{
		String sql;
		sql = " DELETE FROM " + Params.dbName + ".business_hour " +
			  " WHERE 1=1 " +
			  (extraCond != null ? extraCond : "");
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(RestaurantError.BUSINESS_HOUR_NOT_FOUND);
		}
	}
	
	/**
	 * Get the businessHours to specific restaurant.
	 * @param staff
	 * 			the staff to perform this action
	 * @throws SQLException
	 * 			if failed to execute any SQL Statement 
	 */
	public static List<BusinessHour> get(Staff staff) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return get(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the businessHours to specific restaurant.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @throws SQLException
	 * 			if failed to execute any SQL Statement 
	 */
	public static List<BusinessHour> get(DBCon dbCon, Staff staff) throws SQLException{
		return getByCond(dbCon, staff, null, null);
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
	private static List<BusinessHour> getByCond(DBCon dbCon, Staff staff, String extraCond, String orderClause) throws SQLException{
		List<BusinessHour> result = new ArrayList<BusinessHour>();
		String sql;
		sql = " SELECT " +
			  " id, restaurant_id, name, opening, ending " +
			  " FROM " + Params.dbName + ".business_hour " +
			  " WHERE restaurant_id = " + staff.getRestaurantId() + " " +
			  (extraCond == null ? "" : extraCond) + " " +
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
