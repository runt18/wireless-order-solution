package com.wireless.db.billStatistics;

import java.sql.SQLException;
import java.text.SimpleDateFormat;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.protocol.Terminal;

public class QueryDutyRange {
	
	/**
	 * Get the records to daily settle history whose off duty is between on and off duty(two input parameters),
	 * The on duty to duty range is the earliest date of those daily settle history record,
	 * and the off duty to duty range is the latest date.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal to query
	 * @param onDuty
	 * 			the on duty
	 * @param offDuty
	 * 			the off duty 
	 * @return	
	 * 			the result to duty range,
	 * 			return null if no corresponding daily settle record exist within this period
	 * @throws SQLException
	 * 			throws if any error occurred while execute any SQL statements.
	 */
	public static DutyRange exec(DBCon dbCon, Terminal term, String onDuty, String offDuty) throws SQLException{
		try{
			String sql;
			sql = " SELECT MIN(on_duty) AS on_duty, MAX(off_duty) AS off_duty FROM "
					+ Params.dbName
					+ ".daily_settle_history "
					+ " WHERE "
					+ " restaurant_id = "
					+ term.restaurantID
					+ " AND "
					+ " off_duty BETWEEN "
					+ "'" + onDuty + "'"
					+ " AND "
					+ "'" + offDuty + "'";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			if(dbCon.rs.next()){
				
				java.sql.Timestamp onDutyTimeStamp = dbCon.rs.getTimestamp("on_duty");
				String onDutyString;
				if(onDutyTimeStamp == null){
					return null;
				}else{
					onDutyString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(onDutyTimeStamp.getTime());
				}
				
				java.sql.Timestamp offDutyTimeStamp = dbCon.rs.getTimestamp("off_duty");
				String offDutyString;
				if(offDutyTimeStamp == null){
					return null;
				}else{
					offDutyString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(offDutyTimeStamp.getTime());
				}			
				
				return new DutyRange(onDutyString, offDutyString);
				
			}else{
				return null;
			}
			
		}finally{
			dbCon.rs.close();
		}
	}
	
}
