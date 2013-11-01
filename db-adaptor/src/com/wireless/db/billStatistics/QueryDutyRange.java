package com.wireless.db.billStatistics;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.SystemError;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.util.DateType;
import com.wireless.util.SQLUtil;

public class QueryDutyRange {
	
	/**
	 * Get the records to daily settle history whose off duty is between on and off duty(two input parameters),
	 * The on duty to duty range is the earliest date of those daily settle history record,
	 * and the off duty to duty range is the latest date.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the terminal to query
	 * @param onDuty
	 * 			the on duty
	 * @param offDuty
	 * 			the off duty 
	 * @return	the result to duty range,
	 * 			return null if no corresponding daily settle record exist within this period
	 * @throws SQLException
	 * 			throws if any error occurred while execute any SQL statements.
	 */
	public static DutyRange exec(DBCon dbCon, Staff staff, String onDuty, String offDuty) throws SQLException{
		try{
			String sql;
			sql = " SELECT MIN(on_duty) AS on_duty, MAX(off_duty) AS off_duty FROM "
					+ Params.dbName
					+ ".daily_settle_history "
					+ " WHERE "
					+ " restaurant_id = "
					+ staff.getRestaurantId()
					+ " AND "
					+ " off_duty BETWEEN "
					+ "'" + onDuty + "'"
					+ " AND "
					+ "'" + offDuty + "'"  
					+ " GROUP BY restaurant_id ";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				
				java.sql.Timestamp onDutyTimeStamp = dbCon.rs.getTimestamp("on_duty");
				String onDutyString;
				onDutyString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(onDutyTimeStamp.getTime());
				
				java.sql.Timestamp offDutyTimeStamp = dbCon.rs.getTimestamp("off_duty");
				String offDutyString;
				offDutyString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(offDutyTimeStamp.getTime());
				
				return new DutyRange(onDutyString, offDutyString);
				
			}else{
				return null;
			}
			
		}finally{
			dbCon.rs.close();
		}
	}
	
	/**
	 * 
	 * @param pin
	 * @param onDuty
	 * @param offDuty
	 * @return
	 * @throws Exception
	 */
	public static DutyRange exec(int staffId, String onDuty, String offDuty) throws Exception{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return QueryDutyRange.exec(dbCon, StaffDao.getStaffById(dbCon, staffId), onDuty, offDuty);
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param pin
	 * @return
	 * @throws Exception
	 */
	public static List<DutyRange> getDutyRangeByToday(DBCon dbCon, int staffId) throws Exception{
		List<DutyRange> result = new ArrayList<DutyRange>();
		
		Staff staff = StaffDao.getStaffById(dbCon, staffId);
//		String selectSQL = "SELECT name, DATE_FORMAT(on_duty,'%Y-%m-%d %T') AS on_duty, DATE_FORMAT(off_duty,'%Y-%m-%d %T') AS off_duty "
//						+ " FROM "
//						+ " ("
//						+ " (SELECT '全天' AS name, (SELECT IFNULL(MAX(off_duty), '1970-01-01 00:00:00') FROM " + Params.dbName + ".daily_settle_history WHERE restaurant_id = " + term.restaurantID + ") AS on_duty, NOW() AS off_duty) "
//						+ " UNION ALL"
//						+ " (SELECT name, on_duty, off_duty FROM " + Params.dbName + ".shift WHERE restaurant_id = " + term.restaurantID + " ORDER BY off_duty)"
//						+ " UNION ALL"
//						+ " (SELECT * FROM (SELECT '本班次' AS name, (SELECT off_duty FROM " + Params.dbName + ".shift WHERE restaurant_id = " + term.restaurantID + " ORDER BY off_duty DESC LIMIT 0,1) AS on_duty, NOW() AS off_duty) TT WHERE on_duty IS NOT NULL) "
//						+ " ) "
//						+ " T";
		
		String querySQL = "SELECT name, on_duty, off_duty "
				+ " FROM "
				+ " ("
				+ " (SELECT '全天' AS name, (SELECT IFNULL(MAX(off_duty), '1970-01-01 00:00:00') FROM " + Params.dbName + ".daily_settle_history WHERE restaurant_id = " + staff.getRestaurantId() + ") AS on_duty, NOW() AS off_duty) "
				+ " UNION ALL"
				+ " (SELECT name, on_duty, off_duty FROM " + Params.dbName + ".shift WHERE restaurant_id = " + staff.getRestaurantId() + " ORDER BY off_duty)"
				+ " UNION ALL"
				+ " (SELECT * FROM (SELECT '本班次' AS name, (SELECT off_duty FROM " + Params.dbName + ".shift WHERE restaurant_id = " + staff.getRestaurantId() + " ORDER BY off_duty DESC LIMIT 0,1) AS on_duty, NOW() AS off_duty) TT WHERE on_duty IS NOT NULL) "
				+ " ) "
				+ " T";
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		while(dbCon.rs.next()){
			DutyRange item = new DutyRange();
			item.setStaff(new Staff(0, dbCon.rs.getString("name")));
			item.setOnDuty(dbCon.rs.getTimestamp("on_duty").getTime());
			item.setOffDuty(dbCon.rs.getTimestamp("off_duty").getTime());
			
			result.add(item);
		}
		dbCon.rs.close();
		
		return result;
	}
	
	/**
	 * 
	 * @param staffId
	 * @return
	 * @throws Exception
	 */
	public static List<DutyRange> getDutyRangeByToday(int staffId) throws Exception{
		DBCon dbCon = new DBCon();
		List<DutyRange> list = null;
		try{
			dbCon.connect();
			list = QueryDutyRange.getDutyRangeByToday(dbCon, staffId);
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return list;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param params
	 * @return
	 * @throws Exception
	 */
	private static List<DutyRange> getDutyRangeByHistory(DBCon dbCon, Map<Object, Object> params) throws Exception {
		List<DutyRange> list = new ArrayList<DutyRange>();
		DutyRange item = null;
		
		String querySQL = "SELECT SH.restaurant_id, SH.id, SH.name, SH.on_duty, SH.off_duty"
						+ " FROM shift_history SH"
						+ " WHERE 1=1 ";
		querySQL = SQLUtil.bindSQLParams(querySQL, params);
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		while(dbCon.rs != null && dbCon.rs.next()){
			item = new DutyRange();
			item.setOnDuty(dbCon.rs.getTimestamp("on_duty").getTime());
			item.setOffDuty(dbCon.rs.getTimestamp("off_duty").getTime());
			
			list.add(item);
			item = null;
		}
		
		return list;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static List<DutyRange> getDutyRangeByHistory(Map<Object, Object> params) throws Exception{
		DBCon dbCon = new DBCon();
		List<DutyRange> list = null;
		try{
			dbCon.connect();
			list = QueryDutyRange.getDutyRangeByHistory(dbCon, params);
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return list;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static List<DutyRange> getDutyRange(DBCon dbCon, Map<Object, Object> params) throws Exception{
		List<DutyRange> list = null;
		if(DateType.getType(params) == DateType.TODAY){
			Object pin = params.get("pin");
			if(pin == null){
				throw new BusinessException(SystemError.NOT_FIND_RESTAURANTID);
			}
			list = QueryDutyRange.getDutyRangeByToday(Integer.valueOf(pin.toString()));
		}else if(DateType.getType(params) == DateType.HISTORY){
			Object restaurantID, onDuty, offDuty;
			restaurantID = params.get("restaurantID");
			onDuty = params.get("onDuty");
			offDuty = params.get("offDuty");
			String extra = "";
			if(restaurantID == null){
				throw new BusinessException(SystemError.NOT_FIND_RESTAURANTID);
			}
			extra += (" AND SH.restaurant_id = " + restaurantID.toString());
			extra += (" AND SH.on_duty >= '" + onDuty.toString() + "'");
			extra += (" AND SH.off_duty <= '" + offDuty.toString() + "'");
			
			params.put(SQLUtil.SQL_PARAMS_EXTRA, extra);
			
			list = QueryDutyRange.getDutyRangeByHistory(dbCon, params);
		}
		return list;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static List<DutyRange> getDutyRange(Map<Object, Object> params) throws Exception{
		DBCon dbCon = new DBCon();
		List<DutyRange> list = null;
		try{
			if(params == null || !DateType.hasType(params)){
				return null;
			}
			dbCon.connect();
			list = QueryDutyRange.getDutyRange(dbCon, params);
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return list;
	}
	
	
}
