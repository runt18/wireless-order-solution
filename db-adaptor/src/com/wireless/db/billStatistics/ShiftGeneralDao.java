package com.wireless.db.billStatistics;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.billStatistics.ShiftGeneral;
import com.wireless.pojo.staffMgr.Staff;

public class ShiftGeneralDao {

	/**
	 * Get the today shift general.
	 * @param staff
	 * 			the staff to perform this action
	 * @return the shift general to today
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<ShiftGeneral> getToday(Staff staff) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getToday(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the today shift general.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the shift general to today
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<ShiftGeneral> getToday(DBCon dbCon, Staff staff) throws SQLException{
		String sql;
		sql = " SELECT name, on_duty, off_duty " +
			  " FROM " +
			  " ( "	+
			  " (SELECT '全天' AS name, (SELECT IFNULL(MAX(off_duty), '1970-01-01 00:00:00') FROM " + Params.dbName + ".daily_settle_history WHERE restaurant_id = " + staff.getRestaurantId() + ") AS on_duty, NOW() AS off_duty) " +
			  " UNION ALL" +
			  " (SELECT name, on_duty, off_duty FROM " + Params.dbName + ".shift WHERE restaurant_id = " + staff.getRestaurantId() + " ORDER BY off_duty)" +
			  " UNION ALL " +
			  " (SELECT * FROM (SELECT '本班次' AS name, (SELECT off_duty FROM " + Params.dbName + ".shift WHERE restaurant_id = " + staff.getRestaurantId() + " ORDER BY off_duty DESC LIMIT 0,1) AS on_duty, NOW() AS off_duty) TT WHERE on_duty IS NOT NULL)) T ";
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		List<ShiftGeneral> result = new ArrayList<ShiftGeneral>();
		while(dbCon.rs.next()){
			ShiftGeneral item = new ShiftGeneral(0);
			item.setStaffName(dbCon.rs.getString("name"));
			item.setOnDuty(dbCon.rs.getTimestamp("on_duty").getTime());
			item.setOffDuty(dbCon.rs.getTimestamp("off_duty").getTime());
			result.add(item);
		}
		dbCon.rs.close();
		
		return Collections.unmodifiableList(result);
	}
	
	/**
	 * Get the history shift general according to date range. 
	 * @param staff
	 * 			the staff to perform this action
	 * @param onDuty
	 * 			the on duty
	 * @param offDuty
	 * 			the off duty
	 * @return the shift general among the range.
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<ShiftGeneral> getByRange(Staff staff, String onDuty, String offDuty) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByRange(dbCon, staff, onDuty, offDuty);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the history shift general according to date range. 
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param onDuty
	 * 			the on duty
	 * @param offDuty
	 * 			the off duty
	 * @return the shift general among the range.
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<ShiftGeneral> getByRange(DBCon dbCon, Staff staff, String onDuty, String offDuty) throws SQLException{
		return getByCond(dbCon, staff, " AND SH.off_duty BETWEEN '" + onDuty + "' AND '" + offDuty + "'", null);
	}
	
	private static List<ShiftGeneral> getByCond(DBCon dbCon, Staff staff, String extraCond, String orderClause) throws SQLException{
		String sql;
		sql = " SELECT " + 
			  " SH.restaurant_id, SH.id, SH.name, SH.on_duty, SH.off_duty " + 
			  " FROM shift_history SH "	+
			  " WHERE 1 = 1 " +
			  " AND SH.restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond : " ") +
			  (orderClause != null ? orderClause : "");
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		List<ShiftGeneral> result = new ArrayList<ShiftGeneral>();
		while(dbCon.rs.next()){
			ShiftGeneral shiftGeneral = new ShiftGeneral(dbCon.rs.getInt("id"));
			shiftGeneral.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			shiftGeneral.setStaffName(dbCon.rs.getString("name"));
			shiftGeneral.setOnDuty(dbCon.rs.getTimestamp("on_duty").getTime());
			shiftGeneral.setOffDuty(dbCon.rs.getTimestamp("off_duty").getTime());
			result.add(shiftGeneral);
		}
		dbCon.rs.close();
		
		return Collections.unmodifiableList(result);
	}
}
