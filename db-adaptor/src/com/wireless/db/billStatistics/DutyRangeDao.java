package com.wireless.db.billStatistics;

import java.sql.SQLException;
import java.text.SimpleDateFormat;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.staffMgr.Staff;

public class DutyRangeDao {
	
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
	 * 			throws if any error occurred while execute any SQL statements
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
					+ " off_duty >= "
					+ "'" + onDuty + "'"
					+ " AND off_duty <= "
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
	 * Get the records to daily settle history whose off duty is between on and off duty(two input parameters),
	 * The on duty to duty range is the earliest date of those daily settle history record,
	 * and the off duty to duty range is the latest date.
	 * @param staff
	 * 			the terminal to query
	 * @param onDuty
	 * 			the on duty
	 * @param offDuty
	 * 			the off duty 
	 * @return	the result to duty range,
	 * 			return null if no corresponding daily settle record exist within this period
	 * @throws SQLException
	 * 			throws if any error occurred while execute any SQL statements
	 */
	public static DutyRange exec(Staff staff, String onDuty, String offDuty) throws Exception{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return DutyRangeDao.exec(dbCon, staff, onDuty, offDuty);
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
}
