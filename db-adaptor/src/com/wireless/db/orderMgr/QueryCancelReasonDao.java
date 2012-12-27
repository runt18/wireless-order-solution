package com.wireless.db.orderMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.protocol.CancelReason;

public class QueryCancelReasonDao {

	/**
	 * Get the cancel reason according to the specific condition.
	 * @param extraCond
	 * 			the extra condition to query SQL statement
	 * @param orderClause
	 * 			the order clause to query SQL statement
	 * @return an array containing the result to cancel reasons
	 * @throws SQLException
	 * 			throws if failed to execute the SQL statement
	 */
	public static CancelReason[] exec(String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return exec(dbCon, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the cancel reason according to the specific condition.
	 * Note that the database should be connected before invoking this method.
	 * @param dbCon
	 * 			the database connection
	 * @param extraCond
	 * 			the extra condition to query SQL statement
	 * @param orderClause
	 * 			the order clause to query SQL statement
	 * @return an array containing the result to cancel reasons
	 * @throws SQLException
	 * 			throws if failed to execute the SQL statement
	 */
	public static CancelReason[] exec(DBCon dbCon, String extraCond, String orderClause) throws SQLException{
		String sql;
		sql = " SELECT " + 
			  " cancel_reason_id, reason, restaurant_id " +
			  " FROM " + Params.dbName + ".cancel_reason CR" +
			  " WHERE 1 = 1 " +
			  (extraCond != null ? extraCond : "") + " " +
			  (orderClause != null ? orderClause : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		List<CancelReason> cancelReasons = new ArrayList<CancelReason>();
		while(dbCon.rs.next()){
			cancelReasons.add(new CancelReason(dbCon.rs.getInt("cancel_reason_id"),
											   dbCon.rs.getString("reason"),
											   dbCon.rs.getInt("restaurant_id")));
		}
		dbCon.rs.close();
		
		return cancelReasons.toArray(new CancelReason[cancelReasons.size()]);
	}
}
