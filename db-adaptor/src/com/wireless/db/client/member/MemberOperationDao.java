package com.wireless.db.client.member;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.pojo.client.MemberOperation;
import com.wireless.util.DateUtil;
import com.wireless.util.SQLUtil;

public class MemberOperationDao {
	
	/**
	 * Insert a new member operation.
	 * 
	 * @param dbCon
	 *            the database connection
	 * @param mo
	 *            the member operation to insert
	 * @return the row count for the SQL statements 
	 * @throws SQLException
	 *             Throws if failed to execute any SQL statements.
	 */
	public static int insertMemberOperation(DBCon dbCon, MemberOperation mo) throws SQLException {
		
		//Build the operate date and sequence.
		Date now = new Date();
		mo.setOperateDate(now.getTime());
		mo.setOperateSeq(DateUtil.createMOSeq(now, mo.getOperationType()));
		
		String insertSQL = " INSERT INTO " +
						   Params.dbName + ".member_operation_today " +
						   "(" +
						   " restaurant_id, staff_id, staff_name, member_id, member_card_id, member_card_alias, " +
						   " operate_seq, operate_date, operate_type, pay_money, charge_type, charge_money, " +
						   " delta_base_money, delta_extra_money, delta_point, "	+
						   " remaining_base_money, remaining_extra_money, remaining_point, comment "	+
						   ")" +
						   " VALUES( " +
						   mo.getRestaurantID() + "," + 
						   mo.getStaffID() + "," +
						   "'" + mo.getStaffName() + "'," + 
						   mo.getMemberID() + "," + 
						   mo.getMemberCardID() + "," + 
						   mo.getMemberCardAlias() + "," +
						   "'" + mo.getOperateSeq() + "'," +
						   "'" + DateUtil.format(mo.getOperateDate()) + "'," + 
						   mo.getOperationType().getValue() + "," + 
						   mo.getConsumeMoney() + "," + 
						   mo.getChargeType().getValue() + "," + 
						   mo.getChargeMoney() + "," + 
						   mo.getDeltaBaseBalance() + "," + 
						   mo.getDeltaGiftBalance() + "," + 
						   mo.getDeltaPoint() + ","	+ 
						   mo.getRemainingBaseBalance() + "," + 
						   mo.getRemainingExtraBalance() + "," + 
						   mo.getRemainingPoint() + "," +
						   "'" + mo.getComment() + "'" + 
						   ") ";
		
		int count = dbCon.stmt.executeUpdate(insertSQL, Statement.RETURN_GENERATED_KEYS);
		//Get the generated id to this member operation. 
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		if(dbCon.rs.next()){
			mo.setId(dbCon.rs.getInt(1));
			return count;
		}else{
			throw new SQLException("The id of member operation is not generated successfully.");
		}
	}
	
	/**
	 * Insert a new member operation.
	 * 
	 * @param dbCon
	 *            the database connection
	 * @param mo
	 *            the member operation to insert
	 * @return the id generated by the member operation to insert
	 * @throws SQLException
	 *             Throws if failed to execute any SQL statements.
	 */
	public static int insertMemberOperation(MemberOperation mo) throws SQLException {
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberOperationDao.insertMemberOperation(dbCon, mo);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param mo
	 * @return
	 * @throws SQLException
	 */
	public static int deleteMemberOperation(DBCon dbCon, MemberOperation mo) throws SQLException {
		int count = 0;
		String deleteSQL = "DELETE FROM member_operation_today "
						+ " WHERE id = " + mo.getId() + " AND restaurant_id = " + mo.getRestaurantID();
		count = dbCon.stmt.executeUpdate(deleteSQL);
		return count;
	}
	
	/**
	 * 
	 * @param mo
	 * @return
	 * @throws SQLException
	 */
	public static int deleteMemberOperation(MemberOperation mo) throws SQLException {
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			count = MemberOperationDao.deleteMemberOperation(dbCon, mo);
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param mol
	 * @throws SQLException
	 */
	public static void deleteMemberOperation(DBCon dbCon, List<MemberOperation> mol) throws SQLException {
		for(MemberOperation temp : mol){
			MemberOperationDao.deleteMemberOperation(dbCon, temp);
		}
	}
	
	/**
	 * 
	 * @param mol
	 * @throws SQLException
	 */
	public static void deleteMemberOperation(List<MemberOperation> mol) throws SQLException {
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			for(MemberOperation temp : mol){
				MemberOperationDao.deleteMemberOperation(dbCon, temp);
			}
			dbCon.conn.commit();
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param id
	 * @param restaurantID
	 * @return
	 * @throws SQLException
	 */
	public static int deleteMemberOperationById(DBCon dbCon, int id, int restaurantID) throws SQLException {
		int count = 0;
		MemberOperation temp = new MemberOperation();
		temp.setId(id);
		temp.setRestaurantID(restaurantID);
		count = MemberOperationDao.deleteMemberOperation(dbCon, temp);
		return count;
	}
	
	/**
	 * 
	 * @param id
	 * @param restaurantID
	 * @return
	 * @throws SQLException
	 */
	public static int deleteMemberOperationById(int id, int restaurantID) throws SQLException {
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			count = deleteMemberOperationById(dbCon, id, restaurantID);
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static List<MemberOperation> getMemberOperationByToday(DBCon dbCon, Map<Object, Object> params) throws SQLException{
		List<MemberOperation> list = new ArrayList<MemberOperation>();
		String querySQL = "SELECT"
						+ " A.id, A.restaurant_id, A.staff_id, A.staff_name, A.member_id, A.member_card_id, A.member_card_alias,"
						+ " A.operate_seq, A.operate_date, A.operate_type, A.pay_money, A.charge_type, A.charge_money,"
						+ " A.delta_base_money, A.delta_extra_money, A.delta_point, "
						+ " A.remaining_base_money, A.remaining_extra_money, A.remaining_point, A.comment"
						+ " FROM member_operation_today A"
						+ " WHERE 1=1 ";
		querySQL = SQLUtil.bindSQLParams(querySQL, params);
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		while(dbCon.rs != null && dbCon.rs.next()){
			MemberOperation item = new MemberOperation();
			item.setId(dbCon.rs.getInt("id"));
			item.setRestaurantID(dbCon.rs.getInt("restaurant_id"));
			item.setStaffID(dbCon.rs.getInt("staff_id"));
			item.setStaffName(dbCon.rs.getString("staff_name"));
			item.setMemberID(dbCon.rs.getInt("member_id"));
			item.setMemberCardID(dbCon.rs.getInt("member_card_id"));
			item.setMemberCardAlias(dbCon.rs.getString("member_card_alias"));
			item.setOperateSeq(dbCon.rs.getString("operate_seq"));
			item.setOperateDate(dbCon.rs.getTimestamp("operate_date").getTime());
			item.setOperationType(dbCon.rs.getShort("operate_type"));
			item.setPayMoney(dbCon.rs.getFloat("pay_money"));
			item.setChargeType(dbCon.rs.getShort("charge_type"));
			item.setChargeMoney(dbCon.rs.getFloat("charge_money"));
			item.setDeltaBaseBalance(dbCon.rs.getFloat("delta_base_money"));
			item.setDeltaExtraBalance(dbCon.rs.getFloat("delta_extra_money"));
			item.setDeltaPoint(dbCon.rs.getInt("delta_point"));
			item.setRemainingBaseBalance(dbCon.rs.getFloat("remaining_base_money"));
			item.setRemainingExtraBalance(dbCon.rs.getFloat("remaining_extra_money"));
			item.setRemainingPoint(dbCon.rs.getInt("remaining_point"));
			item.setComment(dbCon.rs.getString("comment"));
			
			list.add(item);
		}
		return list;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static List<MemberOperation> getMemberOperationByToday(Map<Object, Object> params) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberOperationDao.getMemberOperationByToday(dbCon, params);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param memberOperationID
	 * @return
	 * @throws SQLException
	 */
	public static MemberOperation getMemberOperationByToday(DBCon dbCon, int memberOperationID) throws SQLException{
		List<MemberOperation> list = null;
		MemberOperation item = null;
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND A.id = " + memberOperationID);
		list = MemberOperationDao.getMemberOperationByToday(dbCon, params);
		if(list != null && list.size() > 0){
			item = list.get(0);
		}
		params.clear();
		params = null;
		return item;
	}
	
	/**
	 * 
	 * @param memberOperationID
	 * @return
	 * @throws SQLException
	 */
	public static MemberOperation getMemberOperationByToday(int memberOperationID) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberOperationDao.getMemberOperationByToday(dbCon, memberOperationID);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static List<MemberOperation> getMemberOperationByHistory(DBCon dbCon, Map<Object, Object> params) throws SQLException{
		List<MemberOperation> list = new ArrayList<MemberOperation>();
		String querySQL = "SELECT"
						+ " A.id, A.restaurant_id, A.staff_id, A.staff_name, A.member_id, A.member_card_id, A.member_card_alias,"
						+ " A.operate_seq, A.operate_date, A.operate_type, A.pay_money, A.charge_type, A.charge_money,"
						+ " A.delta_base_money, A.delta_extra_money, A.delta_point, "
						+ " A.remaining_base_money, A.remaining_extra_money, A.remaining_point, A.comment"
						+ " FROM member_operation_history A"
						+ " WHERE 1=1 ";
		querySQL = SQLUtil.bindSQLParams(querySQL, params);
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		while(dbCon.rs != null && dbCon.rs.next()){
			MemberOperation item = new MemberOperation();
			item.setId(dbCon.rs.getInt("id"));
			item.setRestaurantID(dbCon.rs.getInt("restaurant_id"));
			item.setStaffID(dbCon.rs.getInt("staff_id"));
			item.setStaffName(dbCon.rs.getString("staff_name"));
			item.setMemberID(dbCon.rs.getInt("member_id"));
			item.setMemberCardID(dbCon.rs.getInt("member_card_id"));
			item.setMemberCardAlias(dbCon.rs.getString("member_card_alias"));
			item.setOperateSeq(dbCon.rs.getString("operate_seq"));
			item.setOperateDate(dbCon.rs.getTimestamp("operate_date").getTime());
			item.setOperationType(dbCon.rs.getShort("operate_type"));
			item.setPayMoney(dbCon.rs.getFloat("pay_money"));
			item.setChargeType(dbCon.rs.getShort("charge_type"));
			item.setChargeMoney(dbCon.rs.getFloat("charge_money"));
			item.setDeltaBaseBalance(dbCon.rs.getFloat("delta_base_money"));
			item.setDeltaExtraBalance(dbCon.rs.getFloat("delta_extra_money"));
			item.setDeltaPoint(dbCon.rs.getInt("delta_point"));
			item.setRemainingBaseBalance(dbCon.rs.getFloat("remaining_base_money"));
			item.setRemainingExtraBalance(dbCon.rs.getFloat("remaining_extra_money"));
			item.setRemainingPoint(dbCon.rs.getInt("remaining_point"));
			item.setComment(dbCon.rs.getString("comment"));
			
			list.add(item);
		}
		return list;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static List<MemberOperation> getMemberOperationByHistory(Map<Object, Object> params) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberOperationDao.getMemberOperationByHistory(dbCon, params);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param memberOperationID
	 * @return
	 * @throws SQLException
	 */
	public static MemberOperation getMemberOperationByHistory(DBCon dbCon, int memberOperationID) throws SQLException{
		List<MemberOperation> list = null;
		MemberOperation item = null;
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND A.id = " + memberOperationID);
		list = MemberOperationDao.getMemberOperationByHistory(dbCon, params);
		if(list != null && list.size() > 0){
			item = list.get(0);
		}
		params.clear();
		params = null;
		return item;
	}
	
	/**
	 * 
	 * @param memberOperationID
	 * @return
	 * @throws SQLException
	 */
	public static MemberOperation getMemberOperationByHistory(int memberOperationID) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberOperationDao.getMemberOperationByHistory(dbCon, memberOperationID);
		}finally{
			dbCon.disconnect();
		}
	}
	
}
