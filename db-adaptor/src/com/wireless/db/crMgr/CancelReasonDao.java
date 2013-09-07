package com.wireless.db.crMgr;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.pojo.crMgr.CancelReason;
import com.wireless.pojo.staffMgr.Staff;

public class CancelReasonDao {

	/**
	 * Get the cancel reason to a specified restaurant defined in {@link Staff} and other extra condition.
	 * @param staff 
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition to query SQL statement
	 * @param orderClause
	 * 			the order clause to query SQL statement
	 * @return an array containing the result to cancel reasons
	 * @throws SQLException
	 * 			throws if failed to execute the SQL statement
	 */
	public static List<CancelReason> getReasons(Staff staff, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getReasons(dbCon, staff, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the cancel reason to a specified restaurant defined in {@link Staff} and other extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff 
	 * 			the terminal
	 * @param extraCond
	 * 			the extra condition to query SQL statement
	 * @param orderClause
	 * 			the order clause to query SQL statement
	 * @return the list containing the result to cancel reasons
	 * @throws SQLException
	 * 			throws if failed to execute the SQL statement
	 */
	public static List<CancelReason> getReasons(DBCon dbCon, Staff staff, String extraCond, String orderClause) throws SQLException{
		String sql;
		sql = " SELECT " + 
			  " cancel_reason_id, reason, restaurant_id " +
			  " FROM " + Params.dbName + ".cancel_reason CR" +
			  " WHERE 1 = 1 " +
			  " AND CR.restaurant_id = " + staff.getRestaurantId() +
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
	 * @param staff
	 * 			the terminal
	 * @param reasonId
	 * 			the reason id to query
	 * @return the cancel reason to specific id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the cancel reason to this id is NOT found
	 */
	public static CancelReason getReasonById(Staff staff, int reasonId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getReasonById(dbCon, staff, reasonId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the cancel reason according to a specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the terminal
	 * @param reasonId
	 * 			the reason id to query
	 * @return the cancel reason to specific id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the cancel reason to this id is NOT found
	 */
	public static CancelReason getReasonById(DBCon dbCon, Staff staff, int reasonId) throws SQLException, BusinessException{
		List<CancelReason> result = getReasons(dbCon, staff, " AND CR.cancel_reason_id = " + reasonId, null);
		if(result.isEmpty()){
			throw new BusinessException("The cancel reason(id = " + reasonId + ") is NOT found.");
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Update a cancel reason.
	 * @param builder
	 * 			the builder to update a cancel reason
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the cancel reason to this id is NOT found
	 */
	public static void update(CancelReason.UpdateBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update a cancel reason.
	 * @param dbCon
	 * 			the database connection
	 * @param builder
	 * 			the builder to update a cancel reason
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the cancel reason to this id is NOT found
	 */
	public static void update(DBCon dbCon, CancelReason.UpdateBuilder builder) throws SQLException, BusinessException{
		String sql;
		CancelReason crToUpdate = builder.build();
		sql = " UPDATE " + Params.dbName + ".cancel_reason SET " +
			  " reason = " + crToUpdate.getReason() +
			  " WHERE cancel_reason_id = " + crToUpdate.getId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException("The cancel reason(id = " + crToUpdate.getId() + ") is NOT found.");
		}
	}
	
	/**
	 * Insert a new cancel reason.
	 * @param builder
	 * 			the builder to insert a new cancel reason
	 * @param staff
	 * 			the staff to perform this action
	 * @return the id to cancel reason just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(Staff staff, CancelReason.InsertBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert a new cancel reason.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to insert a new cancel reason
	 * @return the id to cancel reason just inserted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(DBCon dbCon, Staff staff, CancelReason.InsertBuilder builder) throws SQLException{
		CancelReason cr = builder.build();
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".cancel_reason" +
			  " (reason, restaurant_id) " +
			  " VALUES(" +
			  "'" + cr.getReason() + "'," +
			  cr.getRestaurantID() +
			  ")";
		
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		int cancelReasonId;
		if(dbCon.rs.next()){
			cancelReasonId = dbCon.rs.getInt(1);
		}else{
			throw new SQLException("Failed to generated the cancel reason id.");
		}
		return cancelReasonId;
	}
}
