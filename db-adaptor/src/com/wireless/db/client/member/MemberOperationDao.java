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
import com.wireless.exception.BusinessException;
import com.wireless.exception.MemberError;
import com.wireless.exception.ProtocolError;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.client.MemberOperation;
import com.wireless.pojo.client.MemberOperation.OperationType;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.util.DateUtil.Pattern;
import com.wireless.protocol.Terminal;
import com.wireless.util.SQLUtil;

public class MemberOperationDao {
	
	/**
	 * Insert a new member operation.
	 * 
	 * @param dbCon
	 *            the database connection
	 * @param term
	 * 			  the terminal
	 * @param mo
	 *            the member operation to insert
	 * @return the row count for the SQL statements 
	 * @throws SQLException
	 *             throws if failed to execute any SQL statements
	 */
	public static int insert(DBCon dbCon, Terminal term, MemberOperation mo) throws SQLException {
		
		//Build the operate date and sequence.
		Date now = new Date();
		mo.setOperateDate(now.getTime());
		mo.setOperateSeq(mo.getOperationType().getPrefix().concat(DateUtil.format(now, Pattern.MO_SEQ.getPattern())));
		
		mo.setRestaurantId(term.restaurantID);
		mo.setStaffID(term.id);
		mo.setStaffName(term.owner);
		
		String insertSQL = " INSERT INTO " +
						   Params.dbName + ".member_operation " +
						   "(" +
						   " restaurant_id, staff_id, staff_name, member_id, member_card, member_name, member_mobile," +
						   " operate_seq, operate_date, operate_type, pay_type, pay_money, order_id, charge_type, charge_money, " +
						   " delta_base_money, delta_extra_money, delta_point, "	+
						   " remaining_base_money, remaining_extra_money, remaining_point, comment "	+
						   ")" +
						   " VALUES( " +
						   mo.getRestaurantId() + "," + 
						   mo.getStaffID() + "," +
						   "'" + mo.getStaffName() + "'," + 
						   mo.getMemberId() + "," +
						   "'" + mo.getMemberCard() + "'," + 
						   "'" + mo.getMemberName() + "'," +
						   "'" + mo.getMemberMobile() + "'," +
						   "'" + mo.getOperateSeq() + "'," +
						   "'" + DateUtil.format(mo.getOperateDate()) + "'," + 
						   mo.getOperationType().getValue() + "," + 
						   (mo.getOperationType() == OperationType.CONSUME ? mo.getPayType().getVal() : "NULL") + "," +
						   (mo.getOperationType() == OperationType.CONSUME ? mo.getPayMoney() : "NULL") + "," + 
						   (mo.getOperationType() == OperationType.CONSUME ? mo.getOrderId() : "NULL") + "," +
						   (mo.getOperationType() == OperationType.CHARGE ? mo.getChargeType().getValue() : "NULL") + "," + 
						   (mo.getOperationType() == OperationType.CHARGE ? mo.getChargeMoney() : "NULL") + "," + 
						   mo.getDeltaBaseMoney() + "," + 
						   mo.getDeltaExtraMoney() + "," + 
						   mo.getDeltaPoint() + ","	+ 
						   mo.getRemainingBaseMoney() + "," + 
						   mo.getRemainingExtraMoney() + "," + 
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
	 * @return the row count for the SQL statements 
	 * @throws SQLException
	 *             Throws if failed to execute any SQL statements.
	 */
	public static int insert(Terminal term, MemberOperation mo) throws SQLException {
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberOperationDao.insert(dbCon, term, mo);
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
	public static int deleteById(DBCon dbCon, int id) throws SQLException {
		int count = 0;
		String deleteSQL = " DELETE FROM " +
					       Params.dbName + ".member_operation " + 
					       " WHERE id = " + id; 
		count = dbCon.stmt.executeUpdate(deleteSQL);
		return count;
	}
	
	/**
	 * 
	 * @param id
	 * @param restaurantID
	 * @return
	 * @throws SQLException
	 */
	public static int deleteById(int id) throws SQLException {
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return deleteById(dbCon, id);
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
	public static int getTodayCount(DBCon dbCon, Map<Object, Object> params) throws SQLException{
		int count = 0;
		String querySQL = SQLUtil.bindSQLParams("SELECT count(A.id) FROM member_operation A LEFT JOIN member B ON A.member_id = B.member_id WHERE 1=1 ", params);
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		if(dbCon.rs != null && dbCon.rs.next()){
			count = dbCon.rs.getInt(1);
		}
		return count;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static int getTodayCount(Map<Object, Object> params) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberOperationDao.getTodayCount(dbCon, params);
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
	public static List<MemberOperation> getToday(DBCon dbCon, Map<Object, Object> params) throws SQLException{
		List<MemberOperation> list = new ArrayList<MemberOperation>();
		String querySQL = "SELECT"
						+ " MO.id, MO.restaurant_id, MO.staff_id, MO.staff_name, " 
						+ " MO.member_id, MO.member_card, MO.member_name, MO.member_mobile, "
						+ " MO.operate_seq, MO.operate_date, MO.operate_type, MO.pay_type, MO.pay_money, MO.order_id, MO.charge_type, MO.charge_money,"
						+ " MO.delta_base_money, MO.delta_extra_money, MO.delta_point, "
						+ " MO.remaining_base_money, MO.remaining_extra_money, MO.remaining_point, MO.comment, "
						+ " M.member_type_id "
						+ " FROM member_operation MO LEFT JOIN member M ON MO.member_id = M.member_id "
						+ " WHERE 1=1 ";
		querySQL = SQLUtil.bindSQLParams(querySQL, params);
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		while(dbCon.rs.next()){
			MemberOperation mo = new MemberOperation();
			mo.setId(dbCon.rs.getInt("id"));
			mo.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			mo.setStaffID(dbCon.rs.getInt("staff_id"));
			mo.setStaffName(dbCon.rs.getString("staff_name"));
			mo.setMemberId(dbCon.rs.getInt("member_id"));
			mo.setMemberName(dbCon.rs.getString("member_name"));
			mo.setMemberMobile(dbCon.rs.getString("member_mobile"));
			mo.setMemberCard(dbCon.rs.getString("member_card"));
			mo.setOperateSeq(dbCon.rs.getString("operate_seq"));
			mo.setOperateDate(dbCon.rs.getTimestamp("operate_date").getTime());
			mo.setOperationType(dbCon.rs.getShort("operate_type"));
			if(mo.getOperationType() == OperationType.CONSUME){
				mo.setPayType(Order.PayType.valueOf(dbCon.rs.getShort("pay_type")));
				mo.setPayMoney(dbCon.rs.getFloat("pay_money"));
				mo.setOrderId(dbCon.rs.getInt("order_id"));
			}
			if(mo.getOperationType() == OperationType.CHARGE){
				mo.setChargeType(dbCon.rs.getShort("charge_type"));
				mo.setChargeMoney(dbCon.rs.getFloat("charge_money"));
			}
			mo.setDeltaBaseMoney(dbCon.rs.getFloat("delta_base_money"));
			mo.setDeltaExtraMoney(dbCon.rs.getFloat("delta_extra_money"));
			mo.setDeltaPoint(dbCon.rs.getInt("delta_point"));
			mo.setRemainingBaseMoney(dbCon.rs.getFloat("remaining_base_money"));
			mo.setRemainingExtraMoney(dbCon.rs.getFloat("remaining_extra_money"));
			mo.setRemainingPoint(dbCon.rs.getInt("remaining_point"));
			mo.setComment(dbCon.rs.getString("comment"));
			
			list.add(mo);
		}
		return list;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static List<MemberOperation> getToday(Map<Object, Object> params) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberOperationDao.getToday(dbCon, params);
		}finally{
			dbCon.disconnect();
		}
	}

	/**
	 * Get all the today member operation to a specified member 
	 * @param dbCon
	 * 			the database connection
	 * @param memberId
	 * 			the member operation to this member id
	 * @return the list holding the member operation result
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<MemberOperation> getTodayByMemberId(DBCon dbCon, int memberId) throws SQLException{
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND MO.member_id = " + memberId);
		return MemberOperationDao.getToday(dbCon, params);
	}
	
	/**
	 * Get all the today member operation to a specified member 
	 * @param memberId
	 * 			the member operation to this member id
	 * @return the list holding the member operation result
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<MemberOperation> getTodayByMemberId(int memberId) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getTodayByMemberId(dbCon, memberId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get a today member operation according to specified id.
	 * @param dbCon
	 * 			the database connection
	 * @param memberOperationId
	 * 			the id to member operation
	 * @return the member operation to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the member operation to this id does NOT exist
	 */
	public static MemberOperation getTodayById(DBCon dbCon, int memberOperationId) throws SQLException, BusinessException{
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND MO.id = " + memberOperationId);
		List<MemberOperation> list = MemberOperationDao.getToday(dbCon, params);
		if(list.isEmpty()){
			throw new BusinessException("The member operation(id = " + memberOperationId + ") is NOT found.");
		}else{
			return list.get(0);
		}
	}
	
	/**
	 * Get a today member operation according to specified id.
	 * @param memberOperationId
	 * 			the id to member operation
	 * @return the member operation to this id
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the member operation to this id does NOT exist
	 */
	public static MemberOperation getTodayById(int memberOperationID) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberOperationDao.getTodayById(dbCon, memberOperationID);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the member operation to an order id.
	 * @param dbCon
	 * 			the database connection
	 * @param orderId
	 * 			the order id associated with member operation which wants to get
	 * @return the member operation associated with this order id, return null if the member operation to this order is NOT found. 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements.
	 * @throws BusinessException
	 * 			throws if the order does NOT exist or the member operation is NOT found.
	 */
	public static MemberOperation getTodayByOrderId(DBCon dbCon, int orderId) throws SQLException, BusinessException{
		String sql;
		sql = " SELECT member_operation_id, CASE WHEN (member_operation_id IS NULL) THEN 0 ELSE 1 END AS has_mo FROM " + Params.dbName + ".order WHERE id = " + orderId;
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		
		try{
			int memberOperationId;
			if(dbCon.rs.next()){
				if(dbCon.rs.getBoolean("has_mo")){
					memberOperationId = dbCon.rs.getInt("member_operation_id");
				}else{
					throw new BusinessException(MemberError.OPERATION_SEARCH);
				}
			}else{
				throw new BusinessException(ProtocolError.ORDER_NOT_EXIST);
			}
			
			return getTodayById(memberOperationId);
			
		}finally{
			dbCon.rs.close();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static int getHistoryCount(DBCon dbCon, Map<Object, Object> params) throws SQLException{
		int count = 0;
		String querySQL = SQLUtil.bindSQLParams("SELECT count(A.id) FROM member_operation_history A LEFT JOIN member B ON A.member_id = B.member_id WHERE 1=1 ", params);
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		if(dbCon.rs != null && dbCon.rs.next()){
			count = dbCon.rs.getInt(1);
		}
		return count;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static int getHistoryCount(Map<Object, Object> params) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberOperationDao.getHistoryCount(dbCon, params);
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
	public static List<MemberOperation> getHistory(DBCon dbCon, Map<Object, Object> params) throws SQLException{
		List<MemberOperation> list = new ArrayList<MemberOperation>();
		String querySQL = "SELECT"
						+ " A.id, A.restaurant_id, A.staff_id, A.staff_name, A.member_id, A.member_card, "
						+ " A.operate_seq, A.operate_date, A.operate_type, A.pay_type, A.pay_money, A.order_id, A.charge_type, A.charge_money,"
						+ " A.delta_base_money, A.delta_extra_money, A.delta_point, "
						+ " A.remaining_base_money, A.remaining_extra_money, A.remaining_point, A.comment, "
						+ " B.member_type_id "
						+ " FROM member_operation_history A LEFT JOIN member B ON A.member_id = B.member_id "
						+ " WHERE 1=1 ";
		querySQL = SQLUtil.bindSQLParams(querySQL, params);
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		while(dbCon.rs.next()){
			MemberOperation item = new MemberOperation();
			item.setId(dbCon.rs.getInt("id"));
			item.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			item.setStaffID(dbCon.rs.getInt("staff_id"));
			item.setStaffName(dbCon.rs.getString("staff_name"));
			item.setMemberId(dbCon.rs.getInt("member_id"));
			item.setMemberCard(dbCon.rs.getString("member_card"));
			item.setOperateSeq(dbCon.rs.getString("operate_seq"));
			item.setOperateDate(dbCon.rs.getTimestamp("operate_date").getTime());
			item.setOperationType(dbCon.rs.getShort("operate_type"));
			if(item.getOperationType() == OperationType.CONSUME){
				item.setPayType(Order.PayType.valueOf(dbCon.rs.getShort("pay_type")));
				item.setPayMoney(dbCon.rs.getFloat("pay_money"));
				item.setOrderId(dbCon.rs.getInt("order_id"));
			}
			if(item.getOperationType() == OperationType.CHARGE){
				item.setChargeType(dbCon.rs.getShort("charge_type"));
				item.setChargeMoney(dbCon.rs.getFloat("charge_money"));
			}
			item.setDeltaBaseMoney(dbCon.rs.getFloat("delta_base_money"));
			item.setDeltaExtraMoney(dbCon.rs.getFloat("delta_extra_money"));
			item.setDeltaPoint(dbCon.rs.getInt("delta_point"));
			item.setRemainingBaseMoney(dbCon.rs.getFloat("remaining_base_money"));
			item.setRemainingExtraMoney(dbCon.rs.getFloat("remaining_extra_money"));
			item.setRemainingPoint(dbCon.rs.getInt("remaining_point"));
			item.setComment(dbCon.rs.getString("comment"));
			
			list.add(item);
			item = null;
		}
		return list;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static List<MemberOperation> getHistory(Map<Object, Object> params) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberOperationDao.getHistory(dbCon, params);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get all the history member operation to a specified member 
	 * @param dbCon
	 * 			the database connection
	 * @param memberId
	 * 			the member operation to this member id
	 * @return the list holding the member operation result
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<MemberOperation> getHistoryByMemberId(DBCon dbCon, int memberId) throws SQLException{
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND A.member_id = " + memberId);
		return MemberOperationDao.getHistory(dbCon, params);
	}
	
	/**
	 * Get all the history member operation to a specified member 
	 * @param memberId
	 * 			the member operation to this member id
	 * @return the list holding the member operation result
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<MemberOperation> getHistoryByMemberId(int memberId) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getHistoryByMemberId(dbCon, memberId);
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
	public static MemberOperation getHistoryById(DBCon dbCon, int memberOperationID) throws SQLException{
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND A.id = " + memberOperationID);
		List<MemberOperation> list = MemberOperationDao.getHistory(dbCon, params);
		if(list.isEmpty()){
			return null;
		}else{
			return list.get(0);
		}
	}
	
	/**
	 * 
	 * @param memberOperationID
	 * @return
	 * @throws SQLException
	 */
	public static MemberOperation getHistoryById(int memberOperationID) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberOperationDao.getHistoryById(dbCon, memberOperationID);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 合并数据集
	 * @param list
	 * @return
	 */
	static List<MemberOperation> calcConsumeSummary(List<MemberOperation> list){
		List<MemberOperation> calcContent = null;
		MemberOperation tempCalc = null, tempSource = null;
		if(list != null && !list.isEmpty()){
			calcContent = new ArrayList<MemberOperation>();
			calcContent.add(list.get(0));
			list.remove(0);
			boolean has = false;
			for(int i = 0; i < list.size(); i++){
				has = false;
				tempSource = list.get(i);
				for(int k = 0; k < calcContent.size(); k++){
					tempCalc = calcContent.get(k);
					if(tempCalc.getOperateDate() == tempSource.getOperateDate()
							&& tempCalc.getMemberId() == tempSource.getMemberId()
							&& tempCalc.getMemberCard().equals(tempSource.getMemberCard())){
						if(tempSource.getOperationType() == MemberOperation.OperationType.CHARGE){
//							tempCalc.setDeltaBaseMoney(tempCalc.getDeltaBaseMoney() + tempSource.getDeltaBaseMoney());
//							tempCalc.setDeltaExtraMoney(tempCalc.getDeltaExtraMoney() + tempSource.getDeltaExtraMoney());
							tempCalc.setChargeMoney(tempCalc.getChargeMoney() + tempSource.getChargeMoney());
						}else if(tempSource.getOperationType() == MemberOperation.OperationType.CONSUME){
//							tempCalc.setDeltaBaseMoney(tempCalc.getDeltaBaseMoney() - tempSource.getDeltaBaseMoney());
//							tempCalc.setDeltaExtraMoney(tempCalc.getDeltaExtraMoney() - tempSource.getDeltaExtraMoney());
							tempCalc.setPayMoney(tempCalc.getPayMoney() + tempSource.getPayMoney());
						}
						tempCalc.setDeltaPoint(tempCalc.getDeltaPoint() + tempSource.getDeltaPoint());
						has = true;
					}
				}
				if(!has){
					calcContent.add(tempSource);
				}
			}
		}
		return calcContent;
	}
	
	/**
	 * 
	 * @param dbCon
	 * 	the params is must
	 * @param restaurantID
	 * 	the params is must
	 * @param duty
	 * 	the params is optional
	 * @param moType
	 * 	the params is optional
	 * @param memberCardAlias
	 * 	the params is optional
	 * @return
	 * @throws SQLException
	 */
	public static List<MemberOperation> getMemberConsumeSummaryByHistory(DBCon dbCon, int restaurantID, DutyRange duty, MemberOperation.OperationType moType, String memberCardAlias) throws SQLException{
		List<MemberOperation> list = new ArrayList<MemberOperation>();
		MemberOperation item = null;
		String querySQL = "SELECT "
						+ " DATE_FORMAT(MOH.operate_date, \"%Y-%m-%d\") operate_date, MOH.member_card, MOH.member_id,"
						+ " SUM(MOH.pay_money) pay_money, SUM(MOH.charge_money) charge_money,"
						+ " MOH.operate_type, SUM(MOH.delta_base_money) delta_base_money, SUM(MOH.delta_extra_money) delta_extra_money,sum(MOH.delta_point) delta_point"
						+ " FROM wireless_order_db.member_operation_history MOH "
						+ " WHERE MOH.restaurant_id = " + restaurantID
						+ (duty != null ? " AND operate_date >= '" + duty.getOnDutyFormat() + "' AND operate_date <= '" + duty.getOffDutyFormat() + "' " : "")
						+ (moType != null ? " AND operate_type = " + moType.getValue() : "")
						+ (memberCardAlias != null ? " AND member_card_alias like '%" + memberCardAlias.trim() + "%'" : "")
						+ " GROUP BY DATE_FORMAT(MOH.operate_date, \"%Y-%m-%d\"), MOH.member_card_id, MOH.operate_type "
						+ " ORDER BY MOH.operate_date, MOH.member_card_id ";
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		while(dbCon.rs != null && dbCon.rs.next()){
			item = new MemberOperation();
			item.setOperateDate(dbCon.rs.getTimestamp("operate_date").getTime());
			item.setMemberCard(dbCon.rs.getString("member_card"));
			item.setMemberId(dbCon.rs.getInt("member_id"));
			item.setOperationType(dbCon.rs.getInt("operate_type"));
			item.setPayMoney(dbCon.rs.getFloat("pay_money"));
			item.setChargeMoney(dbCon.rs.getFloat("charge_money"));
			item.setDeltaBaseMoney(dbCon.rs.getFloat("delta_base_money"));
			item.setDeltaExtraMoney(dbCon.rs.getFloat("delta_extra_money"));
			item.setDeltaPoint(dbCon.rs.getInt("delta_point"));
			
			list.add(item);
			item = null;
		}
		list = calcConsumeSummary(list);
		return list;
	}
	
	/**
	 * 	
	 * @param restaurantID
	 * 	the params is must
	 * @param duty
	 * 	the params is optional
	 * @param moType
	 * 	the params is optional
	 * @param memberCardAlias
	 * 	the params is optional
	 * @return
	 * @throws SQLException
	 */
	public static List<MemberOperation> getMemberConsumeSummaryByHistory(int restaurantID, DutyRange duty, MemberOperation.OperationType moType, String memberCardAlias) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberOperationDao.getMemberConsumeSummaryByHistory(dbCon, restaurantID, duty, moType, memberCardAlias);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * 
	 * @param dbCon
	 * 	the params is must
	 * @param restaurantID
	 * 	the params is must
	 * @param moType
	 * 	the params is optional
	 * @param memberCardAlias
	 * 	the params is optional
	 * @return
	 * @throws SQLException
	 */
	public static List<MemberOperation> getMemberConsumeSummaryByToday(DBCon dbCon, int restaurantID, MemberOperation.OperationType moType, String memberCardAlias) throws SQLException{
		List<MemberOperation> list = new ArrayList<MemberOperation>();
		MemberOperation item = null;
		String querySQL = "SELECT "
						+ " DATE_FORMAT(MO.operate_date, \"%Y-%m-%d\") operate_date, MO.member_card, MO.member_id,"
						+ " SUM(MO.pay_money) pay_money, SUM(MO.charge_money) charge_money,"
						+ " MO.operate_type, SUM(MO.delta_base_money) delta_base_money, SUM(MO.delta_extra_money) delta_extra_money,sum(MO.delta_point) delta_point"
						+ " FROM wireless_order_db.member_operation MO "
						+ " WHERE MO.restaurant_id = " + restaurantID
						+ (moType != null ? " AND operate_type = " + moType.getValue() : "")
						+ (memberCardAlias != null ? " AND member_card_alias like '%" + memberCardAlias.trim() + "%'" : "")
						+ " GROUP BY DATE_FORMAT(MO.operate_date, \"%Y-%m-%d\"), MO.member_card_id, MO.operate_type "
						+ " ORDER BY MO.operate_date, MO.member_card_id ";
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		while(dbCon.rs != null && dbCon.rs.next()){
			item = new MemberOperation();
			item.setOperateDate(dbCon.rs.getTimestamp("operate_date").getTime());
			item.setMemberCard(dbCon.rs.getString("member_card"));
			item.setMemberId(dbCon.rs.getInt("member_id"));
			item.setOperationType(dbCon.rs.getInt("operate_type"));
			item.setPayMoney(dbCon.rs.getFloat("pay_money"));
			item.setChargeMoney(dbCon.rs.getFloat("charge_money"));
			item.setDeltaBaseMoney(dbCon.rs.getFloat("delta_base_money"));
			item.setDeltaExtraMoney(dbCon.rs.getFloat("delta_extra_money"));
			item.setDeltaPoint(dbCon.rs.getInt("delta_point"));
			
			list.add(item);
			item = null;
		}
		list = calcConsumeSummary(list);
		return list;
	}
	
	/**
	 * 
	 * @param restaurantID
	 * 	the params is must
	 * @param moType
	 * 	the params is optional
	 * @param memberCardAlias
	 * 	the params is optional
	 * @return
	 * @throws SQLException
	 */
	public static List<MemberOperation> getMemberConsumeSummaryByToday(int restaurantID, MemberOperation.OperationType moType, String memberCardAlias) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberOperationDao.getMemberConsumeSummaryByToday(dbCon, restaurantID, moType, memberCardAlias);
		}finally{
			dbCon.disconnect();
		}
	}
}
