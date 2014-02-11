package com.wireless.db.billStatistics;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.pojo.billStatistics.ShiftGeneral;
import com.wireless.pojo.staffMgr.Staff;

public class DailyGeneralDao {
	
	/**
	 * Get the history daily general according to date range. 
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
	 * Get the history daily general according to date range. 
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
		return getByCond(dbCon, staff, " AND DSH.off_duty BETWEEN '" + onDuty + "' AND '" + offDuty + "'", null);
	}
	
	private static List<ShiftGeneral> getByCond(DBCon dbCon, Staff staff, String extraCond, String orderClause) throws SQLException{
		String sql;
		sql = " SELECT " + 
			  " DSH.restaurant_id, DSH.id, DSH.name, DSH.on_duty, DSH.off_duty " + 
			  " FROM daily_settle_history DSH "	+
			  " WHERE 1 = 1 " +
			  " AND DSH.restaurant_id = " + staff.getRestaurantId() + " " +
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
