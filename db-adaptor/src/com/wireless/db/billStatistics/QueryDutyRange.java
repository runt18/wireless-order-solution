package com.wireless.db.billStatistics;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.VerifyPin;
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
	
	/**
	 * 
	 * @param restaurantID
	 * @return
	 * @throws Exception
	 */
	public static DutyRange[] getDutyRangeByNow(long pin) throws Exception{
		DBCon dbCon = new DBCon();
		List<DutyRange> list = new ArrayList<DutyRange>();
		DutyRange item = null;
		try{
			dbCon.connect();
			Terminal term = VerifyPin.exec(pin, Terminal.MODEL_STAFF);
			String selectSQL = "SELECT name, DATE_FORMAT(on_duty,'%Y-%m-%d %T') AS on_duty, DATE_FORMAT(off_duty,'%Y-%m-%d %T') AS off_duty "
							+ " FROM "
							+ " ("
							+ " (SELECT '全天' AS name, IFNULL(MAX(off_duty), '1970-01-01 00:00:00') FROM " + Params.dbName + ".daily_settle_history WHERE restaurant_id = " + term.restaurantID + ") AS on_duty, NOW() AS off_duty) "
							+ " UNION ALL"
							+ " (SELECT name, on_duty, off_duty FROM " + Params.dbName + ".shift WHERE restaurant_id = " + term.restaurantID + " ORDER BY off_duty)"
							+ " UNION ALL"
							+ " (SELECT * FROM (SELECT '本班次' AS name, (SELECT off_duty FROM " + Params.dbName + ".shift WHERE restaurant_id = " + term.restaurantID + " ORDER BY off_duty DESC LIMIT 0,1) AS on_duty, NOW() AS off_duty) TT WHERE on_duty IS NOT NULL) "
							+ " ) "
							+ " T";
			
			dbCon.rs = dbCon.stmt.executeQuery(selectSQL);
			
			while(dbCon.rs != null && dbCon.rs.next()){
				item = new DutyRange();
				item.setName(dbCon.rs.getString("name"));
				item.setOnDuty(dbCon.rs.getString("on_duty"));
				item.setOffDuty(dbCon.rs.getString("off_duty"));
				
				list.add(item);
				item = null;
			}
		}catch(Exception e){
			throw e;
		}finally{
			dbCon.disconnect();
		}
		return list.toArray(new DutyRange[list.size()]);
	}
	
}
