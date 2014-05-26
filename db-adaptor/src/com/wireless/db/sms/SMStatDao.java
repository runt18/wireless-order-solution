package com.wireless.db.sms;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.SMSError;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.sms.SMSDetail;
import com.wireless.pojo.sms.SMStat;
import com.wireless.pojo.staffMgr.Staff;

public class SMStatDao {

	/**
	 * Insert a new SMS state.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to insert a new SMS state
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void insert(Staff staff, SMStat.InsertBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			insert(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert a new SMS state.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to insert a new SMS state
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void insert(DBCon dbCon, Staff staff, SMStat.InsertBuilder builder) throws SQLException{
		SMStat stat = builder.build();
		String sql;
		sql = " SELECT COUNT(*) FROM " + Params.dbName + ".sms_stat WHERE restaurant_id = " + stat.getRestaurantId();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			if(dbCon.rs.getInt(1) == 0){
				sql = " INSERT INTO " + Params.dbName + ".sms_stat (restaurant_id) VALUES (" + stat.getRestaurantId() + ")";
				dbCon.stmt.executeUpdate(sql);
			}
		}
		dbCon.rs.close();
	}
	
	/**
	 * Get the SMS state to specific restaurant.
	 * @param staff
	 * 			the staff to perform this action
	 * @return the SMS state to this restaurant
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the SMS state to this restaurant does NOT exist
	 */
	public static SMStat get(Staff staff) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return get(dbCon, staff);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the SMS state to specific restaurant.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the SMS state to this restaurant
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the SMS state to this restaurant does NOT exist
	 */
	public static SMStat get(DBCon dbCon, Staff staff) throws SQLException, BusinessException{
		String sql;
		sql = " SELECT * FROM " + Params.dbName + ".sms_stat " +
			  " WHERE 1 = 1 " +
			  " AND restaurant_id = " + staff.getRestaurantId();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		SMStat stat = null;
		if(dbCon.rs.next()){
			stat = new SMStat(dbCon.rs.getInt("restaurant_id"));
			stat.setChargeUsed(dbCon.rs.getInt("charge_used"));
			stat.setConsumptionUsed(dbCon.rs.getInt("consumption_used"));
			stat.setVerificationUsed(dbCon.rs.getInt("verification_used"));
			stat.setTotalUsed(dbCon.rs.getInt("total_used"));
			stat.setRemaining(dbCon.rs.getInt("remaining"));
		}else{
			throw new BusinessException(SMSError.SMS_STAT_NOT_EXIST);
		}
		dbCon.rs.close();
		
		return stat;
	}
	
	/**
	 * Get the SMS details.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the SMS detail
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<SMSDetail> getDetails(Staff staff, String extraCond, String orderClause) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getDetails(dbCon, staff, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the SMS details.
	 * @param dbCon
	 * 			the database connection.
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the SMS detail
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<SMSDetail> getDetails(DBCon dbCon, Staff staff, String extraCond, String orderClause) throws SQLException{
		List<SMSDetail> result = new ArrayList<SMSDetail>();
		
		String sql;
		sql = " SELECT * FROM " + Params.dbName + ".sms_detail WHERE 1 = 1 " +
			  " AND restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond : " ") +
			  (orderClause != null ? orderClause : "");
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			SMSDetail detail = new SMSDetail(dbCon.rs.getInt("id"));
			detail.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			detail.setDelta(dbCon.rs.getInt("delta"));
			detail.setOperation(SMSDetail.Operation.valueOf(dbCon.rs.getInt("operation")));
			detail.setModified(dbCon.rs.getTimestamp("modified").getTime());
			detail.setRemaining(dbCon.rs.getInt("remaining"));
			detail.setStaff(dbCon.rs.getString("staff_name"));
			result.add(detail);
		}
		dbCon.rs.close();
		
		return result;
	}
	
	/**
	 * Update the SMS state.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the corresponding SMS state does NOT exist
	 */
	public static void update(Staff staff, SMStat.UpdateBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			update(dbCon, staff, builder);
			dbCon.conn.commit();
			
		}catch(SQLException | BusinessException e){
			dbCon.conn.rollback();
			throw e;
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update the SMS state.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the update builder
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the corresponding SMS state does NOT exist
	 */
	public static void update(DBCon dbCon, Staff staff, SMStat.UpdateBuilder builder) throws SQLException, BusinessException{
		SMStat state = SMStatDao.get(dbCon, staff);
		SMSDetail detail;
		if(builder.getOperation() == SMSDetail.Operation.USE_VERIFY){
			detail = state.use4Verify(builder.getAmount());
			
		}else if(builder.getOperation() == SMSDetail.Operation.USE_CHARGE){
			detail = state.use4Charge(builder.getAmount());
			
		}else if(builder.getOperation() == SMSDetail.Operation.USE_CONSUME){
			detail = state.use4Consume(builder.getAmount());
			
		}else if(builder.getOperation() == SMSDetail.Operation.ADD){
			detail = state.add(builder.getAmount());
			
		}else if(builder.getOperation() == SMSDetail.Operation.DEDUCT){
			detail = state.deduct(builder.getAmount());
			
		}else{
			throw new IllegalArgumentException();
		}
		
		String sql;
		//Update the sms state
		sql = " UPDATE " + Params.dbName + ".sms_stat SET " +
			  " restaurant_id = " + builder.getRestaurantId() +
			  " ,total_used = " + state.getTotalUsed() +
			  " ,verification_used = " + state.getVerificationUsed() + 
			  " ,consumption_used = " + state.getConsumptionUsed() +
			  " ,charge_used = " + state.getChargeUsed() +
			  " ,remaining = " + state.getRemaining() +
			  " WHERE restaurant_id = " + builder.getRestaurantId();
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(SMSError.SMS_STAT_NOT_EXIST);
		}

		//Insert the sms detail.
		sql = " INSERT INTO " + Params.dbName + ".sms_detail " +
			  "(restaurant_id, modified, operation, delta, remaining, staff_id, staff_name)" +
			  " VALUES(" +
			  builder.getRestaurantId() + "," +
			  " NOW()," +
			  detail.getOperation().getVal() + "," +
			  detail.getDelta() + "," +
			  detail.getRemaining() + "," +
			  staff.getId() + "," +
			  "'" + staff.getName() + "'" +
			  ")";
		dbCon.stmt.executeUpdate(sql);
	}
	
	/**
	 * Delete the SMS state and associated details.
	 * @param staff
	 * 			the staff to perform this action
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void delete(Staff staff) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			delete(dbCon, staff);
			dbCon.conn.commit();
			
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the SMS state and associated details.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void delete(DBCon dbCon, Staff staff) throws SQLException{
		String sql;
		sql = " DELETE FROM " + Params.dbName + ".sms_stat WHERE restaurant_id = " + staff.getRestaurantId();
		dbCon.stmt.executeUpdate(sql);
		sql = " DELETE FROM " + Params.dbName + ".sms_detail WHERE restaurant_id = " + staff.getRestaurantId();
		dbCon.stmt.executeUpdate(sql);
	}
	
	/**
	 * Sweep the sms detail records which have been expired.
	 * @param dbCon
	 * 			the database connection
	 * @return the amount of sms detail records to sweep
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int sweep(DBCon dbCon) throws SQLException{
		// Delete the SMS details which has been expired.
		String sql;
		sql = " DELETE SMS_D FROM " + 
			  Params.dbName + ".sms_detail AS SMS_D, " +
			  Params.dbName + ".restaurant AS REST " +
			  " WHERE 1 = 1 " +
			  " AND REST.id > " + Restaurant.RESERVED_7 +
			  " AND SMS_D.restaurant_id = REST.id " +
			  " AND UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(SMS_D.modified) > REST.record_alive ";
		return dbCon.stmt.executeUpdate(sql);
	}
}
