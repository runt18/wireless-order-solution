package com.wireless.db.client.member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.client.MemberOperation;
import com.wireless.util.SQLUtil;

public class MemberOperationDao {

	/**
	 * 
	 * @param dbCon
	 * @param mo
	 * @return
	 * @throws Exception
	 */
	public static int insertMemberOperation(DBCon dbCon, MemberOperation mo) throws Exception {
		int count = 0;
		String insertSQL = "INSERT INTO member_operation_today "
					+ "("
					+ "restaurant_id, staff_id, staff_name, member_id, member_card_id, member_card_alias,"
					+ "operate_seq, operate_date, operater_type, pay_type, pay_money, charge_type, charge_money,"
					+ "delta_base_money, delta_gift_money, delta_point, "
					+ "remaining_base_money, remaining_gift_money, remaining_point, comment"
					+ ")"
					+ " VALUES("
					+ mo.getRestaurantID() + "," + mo.getStaffID() + ",'" + mo.getStaffName() + "'," + mo.getMemberID() + "," + mo.getMemberCardID() + "," + mo.getMemberCardAlias() + ","
					+ "'" + mo.getSep() + "',NOW()," + mo.getType() + "," + mo.getPayType() + "," + mo.getPayMoney() + "," + mo.getChargeType() + "," + mo.getChargeMoney() + ","
					+ mo.getDeltaBaseMoney() + "," + mo.getDeltaGiftMoney() + "," + mo.getDeltaPoint() + ","
					+ mo.getRemainingBaseMoney() + "," + mo.getRemainingGiftMoney() + "," + mo.getRemainingPoint() + ",'" + mo.getComment() + "'"
					+ ") ";
		count = dbCon.stmt.executeUpdate(insertSQL);
		return count;
	}
	
	/**
	 * 
	 * @param mo
	 * @return
	 * @throws Exception
	 */
	public static int insertMemberOperation(MemberOperation mo) throws Exception {
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			count = MemberOperationDao.insertMemberOperation(dbCon, mo);
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param mo
	 * @return
	 * @throws Exception
	 */
	public static int deleteMemberOperation(DBCon dbCon, MemberOperation mo) throws Exception {
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
	 * @throws Exception
	 */
	public static int deleteMemberOperation(MemberOperation mo) throws Exception {
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
	 * @throws Exception
	 */
	public static void deleteMemberOperation(DBCon dbCon, List<MemberOperation> mol) throws Exception {
		for(MemberOperation temp : mol){
			MemberOperationDao.deleteMemberOperation(dbCon, temp);
		}
	}
	
	/**
	 * 
	 * @param mol
	 * @throws Exception
	 */
	public static void deleteMemberOperation(List<MemberOperation> mol) throws Exception {
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			for(MemberOperation temp : mol){
				MemberOperationDao.deleteMemberOperation(dbCon, temp);
			}
			dbCon.conn.commit();
		}catch(Exception e){
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
	 * @throws Exception
	 */
	public static int deleteMemberOperationById(DBCon dbCon, int id, int restaurantID) throws Exception {
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
	 * @throws Exception
	 */
	public static int deleteMemberOperationById(int id, int restaurantID) throws Exception {
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
	 * @throws Exception
	 */
	public static List<MemberOperation> getMemberOperationByToday(DBCon dbCon, Map<Object, Object> params) throws Exception{
		List<MemberOperation> list = new ArrayList<MemberOperation>();
		MemberOperation item = null;
		Member tempMember = null;
		Object hasMemberDetail = null;
		if(params != null)
			hasMemberDetail = params.get(Member.class);
		String querySQL = "SELECT"
						+ " A.id, A.restaurant_id, A.staff_id, A.staff_name, A.member_id, A.member_card_id, A.member_card_alias,"
						+ " A.operate_seq, A.operate_date, A.operate_type, A.pay_type, A.pay_money, A.charge_type, A.charge_money,"
						+ " A.delta_base_money, A.delta_gift_money, A.delta_point, "
						+ " A.remaining_base_money, A.remaining_gift_money, A.remaining_point, A.comment"
						+ " FROM member_operation_today A"
						+ " WHERE 1=1 ";
		querySQL = SQLUtil.bindSQLParams(querySQL, params);
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		while(dbCon.rs != null && dbCon.rs.next()){
			item = new MemberOperation();
			item.setId(dbCon.rs.getInt("id"));
			item.setRestaurantID(dbCon.rs.getInt("restaurant_id"));
			item.setStaffID(dbCon.rs.getInt("staff_id"));
			item.setStaffName(dbCon.rs.getString("staff_name"));
			item.setMemberID(dbCon.rs.getInt("member_id"));
			item.setMemberCardID(dbCon.rs.getInt("member_card_id"));
			item.setMemberCardAlias(dbCon.rs.getString("member_card_alias"));
			item.setSep(dbCon.rs.getString("operate_seq"));
			item.setData(dbCon.rs.getTimestamp("operate_date").getTime());
			item.setType(dbCon.rs.getShort("operate_type"));
			item.setPayType(dbCon.rs.getShort("pay_type"));
			item.setPayMoney(dbCon.rs.getFloat("pay_money"));
			item.setChargeType(dbCon.rs.getShort("charge_type"));
			item.setChargeMoney(dbCon.rs.getFloat("charge_money"));
			item.setDeltaBaseMoney(dbCon.rs.getFloat("delta_base_money"));
			item.setDeltaGiftMoney(dbCon.rs.getFloat("delta_gift_money"));
			item.setDeltaPoint(dbCon.rs.getInt("delta_point"));
			item.setRemainingBaseMoney(dbCon.rs.getFloat("remaining_base_money"));
			item.setRemainingGiftMoney(dbCon.rs.getFloat("remaining_gift_money"));
			item.setRemainingPoint(dbCon.rs.getInt("remaining_point"));
			item.setComment(dbCon.rs.getString("comment"));
			
			if(hasMemberDetail != null && hasMemberDetail instanceof Boolean && Boolean.valueOf(hasMemberDetail.toString())){
				tempMember = MemberDao.getMember(item.getMemberID());
				item.setMemberDetail(tempMember);
			}
			
			list.add(item);
			item = null;
		}
		return list;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static List<MemberOperation> getMemberOperationByToday(Map<Object, Object> params) throws Exception{
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
	 * @throws Exception
	 */
	public static MemberOperation getMemberOperationByToday(DBCon dbCon, int memberOperationID) throws Exception{
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
	 * @throws Exception
	 */
	public static MemberOperation getMemberOperationByToday(int memberOperationID) throws Exception{
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
	 * @throws Exception
	 */
	public static List<MemberOperation> getMemberOperationByHistory(DBCon dbCon, Map<Object, Object> params) throws Exception{
		List<MemberOperation> list = new ArrayList<MemberOperation>();
		MemberOperation item = null;
		Member tempMember = null;
		Object hasMemberDetail = null;
		if(params != null)
			hasMemberDetail = params.get(Member.class);
		String querySQL = "SELECT"
						+ " A.id, A.restaurant_id, A.staff_id, A.staff_name, A.member_id, A.member_card_id, A.member_card_alias,"
						+ " A.operate_seq, A.operate_date, A.operate_type, A.pay_type, A.pay_money, A.charge_type, A.charge_money,"
						+ " A.delta_base_money, A.delta_gift_money, A.delta_point, "
						+ " A.remaining_base_money, A.remaining_gift_money, A.remaining_point, A.comment"
						+ " FROM member_operation_history A"
						+ " WHERE 1=1 ";
		querySQL = SQLUtil.bindSQLParams(querySQL, params);
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		while(dbCon.rs != null && dbCon.rs.next()){
			item = new MemberOperation();
			item.setId(dbCon.rs.getInt("id"));
			item.setRestaurantID(dbCon.rs.getInt("restaurant_id"));
			item.setStaffID(dbCon.rs.getInt("staff_id"));
			item.setStaffName(dbCon.rs.getString("staff_name"));
			item.setMemberID(dbCon.rs.getInt("member_id"));
			item.setMemberCardID(dbCon.rs.getInt("member_card_id"));
			item.setMemberCardAlias(dbCon.rs.getString("member_card_alias"));
			item.setSep(dbCon.rs.getString("operate_seq"));
			item.setData(dbCon.rs.getTimestamp("operate_date").getTime());
			item.setType(dbCon.rs.getShort("operate_type"));
			item.setPayType(dbCon.rs.getShort("pay_type"));
			item.setPayMoney(dbCon.rs.getFloat("pay_money"));
			item.setChargeType(dbCon.rs.getShort("charge_type"));
			item.setChargeMoney(dbCon.rs.getFloat("charge_money"));
			item.setDeltaBaseMoney(dbCon.rs.getFloat("delta_base_money"));
			item.setDeltaGiftMoney(dbCon.rs.getFloat("delta_gift_money"));
			item.setDeltaPoint(dbCon.rs.getInt("delta_point"));
			item.setRemainingBaseMoney(dbCon.rs.getFloat("remaining_base_money"));
			item.setRemainingGiftMoney(dbCon.rs.getFloat("remaining_gift_money"));
			item.setRemainingPoint(dbCon.rs.getInt("remaining_point"));
			item.setComment(dbCon.rs.getString("comment"));
			
			if(hasMemberDetail != null && hasMemberDetail instanceof Boolean && Boolean.valueOf(hasMemberDetail.toString())){
				tempMember = MemberDao.getMember(item.getMemberID());
				item.setMemberDetail(tempMember);
			}
			
			list.add(item);
			item = null;
		}
		return list;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static List<MemberOperation> getMemberOperationByHistory(Map<Object, Object> params) throws Exception{
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
	 * @throws Exception
	 */
	public static MemberOperation getMemberOperationByHistory(DBCon dbCon, int memberOperationID) throws Exception{
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
	 * @throws Exception
	 */
	public static MemberOperation getMemberOperationByHistory(int memberOperationID) throws Exception{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberOperationDao.getMemberOperationByHistory(dbCon, memberOperationID);
		}finally{
			dbCon.disconnect();
		}
	}
	
}
