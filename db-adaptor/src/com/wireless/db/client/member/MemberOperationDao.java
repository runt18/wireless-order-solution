package com.wireless.db.client.member;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.wireless.db.DBCon;
import com.wireless.pojo.client.MemberOperation;

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
	 * @param mp
	 * @return
	 * @throws Exception
	 */
	public static int deleteMemberOperation(DBCon dbCon, MemberOperation mp) throws Exception {
		int count = 0;
		
		return count;
	}
	
	/**
	 * 
	 * @param mp
	 * @return
	 * @throws Exception
	 */
	public static int deleteMemberOperation(MemberOperation mp) throws Exception {
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			count = MemberOperationDao.deleteMemberOperation(dbCon, mp);
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
	public static List<MemberOperation> getMemberOperation(DBCon dbCon, Map<Object, Object> params) throws Exception{
		List<MemberOperation> list = new ArrayList<MemberOperation>();
		
		return list;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static List<MemberOperation> getMemberOperation(Map<Object, Object> params) throws Exception{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberOperationDao.getMemberOperation(dbCon, params);
		}finally{
			dbCon.disconnect();
		}
	}
}
