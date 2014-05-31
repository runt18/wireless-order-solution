package com.wireless.db.system;

import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.RestaurantError;
import com.wireless.pojo.billStatistics.HourRange;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.system.BusinessHour;

public class BusinessHourDao {

	/**
	 * 
	 * @param dbCon
	 * @param insert
	 * @return
	 * @throws SQLException
	 */
	public static int insert(BusinessHour.InsertBuilder insert) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, insert);
		}finally{
			dbCon.disconnect();
		}
	}
	public static int insert(DBCon dbCon, BusinessHour.InsertBuilder insert) throws SQLException{
		BusinessHour bh = insert.build();
		String insertSQl = "INSERT INTO business_hour ( name, opening, ending, restaurant_id) "
				+ "VALUES("
				+ "'" + bh.getName() + "',"
				+ "'" + bh.getOpeningFormat() + "',"
				+ "'" + bh.getEndingFormat() + "',"
				+ bh.getRestaurantId()
				+ ")";
		dbCon.stmt.executeUpdate(insertSQl, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		if(dbCon.rs.next()) bh.setId(dbCon.rs.getInt(1));
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
	public static void update(BusinessHour.UpdateBuilder update) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, update);
		}finally{
			dbCon.disconnect();
		}
	}
	public static void update(DBCon dbCon, BusinessHour.UpdateBuilder update) throws SQLException, BusinessException{
		BusinessHour bh = update.build();
		String insertSQl = "UPDATE business_hour SET "
				+ " name='" + bh.getName() + "',"
				+ " opening='" + bh.getOpeningFormat() + "',"
				+ " ending='" + bh.getEndingFormat() + "'"
				+ " WHERE id = " + bh.getId();
		if(dbCon.stmt.executeUpdate(insertSQl) == 0){
			throw new BusinessException(RestaurantError.BUSINESS_NOT_FOUND);
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
			throw new BusinessException(RestaurantError.BUSINESS_NOT_FOUND);
		}
	}
	
	
	/**
	 * Get the businessHours according to terminal and extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return	the the list holding the table result 
	 * @throws SQLException
	 * 			if failed to execute any SQL Statement 
	 * @throws ParseException 
	 */
	public static List<BusinessHour> getBusinessHours(Staff term, String extraCond) throws SQLException, BusinessException, ParseException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getBusinessHours(dbCon, term, extraCond, null);
		}finally{
			dbCon.disconnect();
		}
	}
	public static List<BusinessHour> getBusinessHours(DBCon dbCon, Staff term, String extraCond, String orderClause) throws SQLException, ParseException{
		List<BusinessHour> bhs = new ArrayList<BusinessHour>();
		try{
			dbCon.connect();
			String sql = "SELECT" +
						" id,restaurant_id,name,opening,ending " +
						" FROM " + Params.dbName + ".business_hour " +
						" WHERE restaurant_id = " + term.getRestaurantId() + " " +
						(extraCond == null ? "" : extraCond) +
						(orderClause == null ? "" : orderClause);
			
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			
			while(dbCon.rs.next()){
				HourRange hr = new HourRange(dbCon.rs.getString("opening"), dbCon.rs.getString("ending"));
				
				BusinessHour bh = new BusinessHour();
				bh.setId(dbCon.rs.getInt("id"));
				bh.setName(dbCon.rs.getString("name"));
				bh.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
				bh.setEnding(hr.getEndingTime());
				bh.setOpening(hr.getOpeningTime());
				
				bhs.add(bh);
			}
			
			dbCon.rs.close();
			return bhs;
		}finally{
			dbCon.disconnect();
		}
	}

}
