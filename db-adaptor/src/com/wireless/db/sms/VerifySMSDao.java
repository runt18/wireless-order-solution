package com.wireless.db.sms;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.SMSError;
import com.wireless.pojo.sms.VerifySMS;
import com.wireless.pojo.util.DateUtil;

public class VerifySMSDao {

	/**
	 * Insert the new verification sms.
	 * @param builder
	 * 			the builder to verification sms
	 * @return the id to verification sms just generated 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(VerifySMS.InsertBuilder builder) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return insert(dbCon, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert the new verification sms.
	 * @param dbCon
	 * 			the database connection
	 * @param builder
	 * 			the builder to verification sms
	 * @return the id to verification sms just generated 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static int insert(DBCon dbCon, VerifySMS.InsertBuilder builder) throws SQLException{
		VerifySMS sms = builder.build();
		
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".verify_sms " +
			  " (`code`, `created`, `expired`) " +
			  " VALUES( " +
			  sms.getCode() + "," +
			  "'" + DateUtil.format(sms.getCreated(), DateUtil.Pattern.DATE_TIME) + "'," +
			  "'" + DateUtil.format(sms.getExpired(), DateUtil.Pattern.DATE_TIME) + "'" +
			  ")";
		
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		if(dbCon.rs.next()){
			return dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The id of verfication sms is not generated successfully.");
		}
	}
	
	/**
	 * Get the verification sms according to a specific id.
	 * @param smsId
	 * 			the id to sms
	 * @return the verification sms to a specific id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the verification sms to this specific id does NOT exist
	 */
	public static VerifySMS getById(int smsId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getById(dbCon, smsId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the verification sms according to a specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param smsId
	 * 			the id to sms
	 * @return the verification sms to a specific id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the verification sms to this specific id does NOT exist
	 */
	public static VerifySMS getById(DBCon dbCon, int smsId) throws SQLException, BusinessException{
		List<VerifySMS> result = getByCond(dbCon, " AND sms_id = " + smsId, null);
		if(result.isEmpty()){
			throw new BusinessException(SMSError.VERIFICATION_SMS_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	private static List<VerifySMS> getByCond(DBCon dbCon, String extraCond, String orderClause) throws SQLException{
		String sql;
		sql = " SELECT " + 
			  " sms_id, code, created, expired " + " FROM " + Params.dbName + ".verify_sms " +
			  " WHERE 1 = 1 " +
			  (extraCond != null ? extraCond : "") + " " +
			  (orderClause != null ? orderClause : "");
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		List<VerifySMS> result = new ArrayList<VerifySMS>();
		while(dbCon.rs.next()){
			VerifySMS sms = new VerifySMS(dbCon.rs.getInt("sms_id"));
			sms.setCode(dbCon.rs.getInt("code"));
			sms.setCreated(dbCon.rs.getTimestamp("created").getTime());
			sms.setExpired(dbCon.rs.getTimestamp("expired").getTime());
			result.add(sms);
		}
		dbCon.rs.close();
		
		return Collections.unmodifiableList(result);
	}
	
	/**
	 * Delete a verification sms according to a specific id.
	 * @param smsId
	 * 			the sms id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void delete(int smsId) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			delete(dbCon, smsId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete a verification sms according to a specific id.
	 * @param dbCon
	 * 			the database connection
	 * @param smsId
	 * 			the sms id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void delete(DBCon dbCon, int smsId) throws SQLException{
		String sql;
		sql = " DELETE FROM " + Params.dbName + ".verify_sms WHERE sms_id = " + smsId;
		dbCon.stmt.executeUpdate(sql);
	}
	
	/**
	 * Verify to see if the sms is valid or not. 
	 * @param builder
	 * 			the builder of sms to verify 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the sms to verify is invalid
	 */
	public static void verify(VerifySMS.VerifyBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			verify(dbCon, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Verify to see if the sms is valid or not. 
	 * @param dbCon
	 * 			the database connection
	 * @param builder
	 * 			the builder of sms to verify 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the sms to verify is invalid<br>
	 * 			throws if the sms has been expired<br>
	 * 			throws if the code is NOT matched<br>
	 */
	public static void verify(DBCon dbCon, VerifySMS.VerifyBuilder builder) throws SQLException, BusinessException{
		VerifySMS verifySms = builder.build();
		VerifySMS basicSms = getById(dbCon, verifySms.getId());
		if(verifySms.getCreated() > basicSms.getExpired()){
			throw new BusinessException(SMSError.VERIFICATION_SMS_EXPIRED);
		}else if(verifySms.getCode() != basicSms.getCode()){
			throw new BusinessException(SMSError.VERIFICATION_CODE_NOT_MATCH);
		}
	}
}
