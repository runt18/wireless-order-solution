package com.wireless.db.crMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.staffMgr.Staff;

public class CancelReasonDao {

	/**
	 * Get the cancel reason to a specified restaurant defined in {@link Staff} and other extra condition.
	 * @param term 
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition to query SQL statement
	 * @param orderClause
	 * 			the order clause to query SQL statement
	 * @return an array containing the result to cancel reasons
	 * @throws SQLException
	 * 			throws if failed to execute the SQL statement
	 */
	public static List<CancelReason> getReasons(Staff term, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getReasons(dbCon, term, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the cancel reason to a specified restaurant defined in {@link Staff} and other extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param term 
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition to query SQL statement
	 * @param orderClause
	 * 			the order clause to query SQL statement
	 * @return the list containing the result to cancel reasons
	 * @throws SQLException
	 * 			throws if failed to execute the SQL statement
	 */
	public static List<CancelReason> getReasons(DBCon dbCon, Staff term, String extraCond, String orderClause) throws SQLException{
		String sql;
		sql = " SELECT " + 
			  " cancel_reason_id, reason, restaurant_id " +
			  " FROM " + Params.dbName + ".cancel_reason CR" +
			  " WHERE 1 = 1 " +
			  " AND CR.restaurant_id = " + term.getRestaurantId() +
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
		
		return cancelReasons;
	}
	
	/**
	 * Get the cancel reason according to a specific id.
	 * @param term
	 * 			the terminal
	 * @param reasonId
	 * 			the reason id to query
	 * @return the cancel reason to specific id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the cancel reason to this id is NOT found
	 */
	public static CancelReason getReasonById(Staff term, int reasonId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getReasonById(dbCon, term, reasonId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the cancel reason according to a specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param reasonId
	 * 			the reason id to query
	 * @return the cancel reason to specific id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the cancel reason to this id is NOT found
	 */
	public static CancelReason getReasonById(DBCon dbCon, Staff term, int reasonId) throws SQLException, BusinessException{
		List<CancelReason> result = getReasons(dbCon, term, " AND CR.cancel_reason_id = " + reasonId, null);
		if(result.isEmpty()){
			throw new BusinessException("The cancel reason(id = " + reasonId + ") is NOT found.");
		}else{
			return result.get(0);
		}
	}
}
