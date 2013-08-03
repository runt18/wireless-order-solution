package com.wireless.db.client.member;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.exception.BusinessException;
import com.wireless.exception.MemberError;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.client.MemberOperation;
import com.wireless.pojo.client.MemberOperation.ChargeType;
import com.wireless.pojo.client.MemberType;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;
import com.wireless.util.SQLUtil;

public class MemberDao {
	
	/**
	 * 
	 * @param dbCon
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static int getMemberCount(DBCon dbCon, Map<Object, Object> params) throws SQLException{
		int count = 0;
		String querySQL = "SELECT COUNT(M.member_id) "
				+ " FROM " + Params.dbName + ".member M " 
				+ " JOIN " + Params.dbName + ".member_type MT ON M.member_type_id = MT.member_type_id " 
				+ " WHERE 1=1 ";
		querySQL = SQLUtil.bindSQLParams(querySQL, params);
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		if(dbCon.rs != null && dbCon.rs.next()){
			count = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		return count;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public static int getMemberCount(Map<Object, Object> params) throws SQLException{
		DBCon dbCon = new DBCon();
		int count = 0;
		try{
			dbCon.connect();
			count = MemberDao.getMemberCount(dbCon, params);
		}finally{
			dbCon.disconnect();
		}
		return count;
	}
	
	
	/**
	 * Get member by extra condition
	 * @param dbCon
	 * @param params
	 * @return the list holding the  result
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Member> getMember(DBCon dbCon, Map<Object, Object> params) throws SQLException{
		List<Member> result = new ArrayList<Member>();
		String querySQL = " SELECT "
			+ " M.member_id, M.restaurant_id, M.point, M.used_point, "
			+ " M.base_balance, M.extra_balance, M.consumption_amount, M.used_balance,"
			+ " M.total_consumption, M.total_point, M.total_charge, " 
			+ " M.member_card, M.name AS member_name, M.sex, M.create_date, "
			+ " M.tele, M.mobile, M.birthday, M.id_card, M.company, M.taste_pref, M.taboo, M.contact_addr, M.comment, "
			+ " MT.member_type_id, MT.discount_id, MT.discount_type, MT.charge_rate, MT.exchange_rate, " 
			+ " MT.name AS member_type_name, MT.attribute, MT.initial_point "
			+ " FROM " 
			+ Params.dbName + ".member M " 
			+ " JOIN " + Params.dbName + ".member_type MT ON M.member_type_id = MT.member_type_id " 
			+ " WHERE 1=1 ";
		querySQL = SQLUtil.bindSQLParams(querySQL, params);
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		while(dbCon.rs.next()){
			Member member = new Member();
			member.setId(dbCon.rs.getInt("member_id"));
			member.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			member.setBaseBalance(dbCon.rs.getFloat("base_balance"));
			member.setExtraBalance(dbCon.rs.getFloat("extra_balance"));
			member.setUsedBalance(dbCon.rs.getFloat("used_balance"));
			member.setConsumptionAmount(dbCon.rs.getInt("consumption_amount"));
			member.setUsedPoint(dbCon.rs.getInt("used_point"));
			member.setPoint(dbCon.rs.getInt("point"));
			member.setTotalConsumption(dbCon.rs.getFloat("total_consumption"));
			member.setTotalPoint(dbCon.rs.getInt("total_point"));
			member.setTotalCharge(dbCon.rs.getFloat("total_charge"));
			member.setComment(dbCon.rs.getString("comment"));
			member.setMemberCard(dbCon.rs.getString("member_card"));
			member.setName(dbCon.rs.getString("member_name"));
			member.setSex(dbCon.rs.getInt("sex"));
			member.setCreateDate(dbCon.rs.getTimestamp("create_date").getTime());
			member.setTele(dbCon.rs.getString("tele"));
			member.setMobile(dbCon.rs.getString("mobile"));
			Timestamp ts;
			ts = dbCon.rs.getTimestamp("birthday");
			if(ts != null){
				member.setBirthday(ts.getTime());
			}
			member.setIdCard(dbCon.rs.getString("id_card"));
			member.setCompany(dbCon.rs.getString("company"));
			member.setTastePref(dbCon.rs.getString("taste_pref"));
			member.setTaboo(dbCon.rs.getString("taboo"));
			member.setContactAddress(dbCon.rs.getString("contact_addr"));
			
			MemberType memberType = new MemberType();
			memberType.setTypeId(dbCon.rs.getInt("member_type_id"));
			memberType.setDiscount(new Discount(dbCon.rs.getInt("discount_id")));
			memberType.setDiscountType(dbCon.rs.getInt("discount_type"));
			memberType.setExchangeRate(dbCon.rs.getFloat("exchange_rate"));
			memberType.setChargeRate(dbCon.rs.getFloat("charge_rate"));
			memberType.setName(dbCon.rs.getString("member_type_name"));
			memberType.setAttribute(dbCon.rs.getInt("attribute"));
			memberType.setInitialPoint(dbCon.rs.getInt("initial_point"));
			member.setMemberType(memberType);
			
			result.add(member);
		}
		return result;
	}
	
	/**
	 * Get member by extra condition
	 * @param dbCon
	 * @param params
	 * @return the list holding the  result
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Member> getMember(Map<Object, Object> params) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberDao.getMember(dbCon, params);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the member according to member id.
	 * @param dbCon
	 * @param id
	 * @return the member associated with this id
	 * @throws BusinessException
	 * 			Throws if the member to this id is NOT found.
	 * @throws SQLException
	 * 			Throws if failed to execute any SQL statement.
	 */
	public static Member getMemberById(int id) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberDao.getMemberById(dbCon, id);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the member according to member id.
	 * @param dbCon
	 * 			the database connection
	 * @param memberId
	 * 			the member id to search
	 * @return the member associated with this id
	 * @throws BusinessException
	 * 			throws if the member to this id is NOT found
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static Member getMemberById(DBCon dbCon, int memberId) throws BusinessException, SQLException{
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.member_id = " + memberId);
		List<Member> ml = MemberDao.getMember(dbCon, params);
		if(ml.isEmpty()){
			throw new BusinessException(MemberError.MEMBER_NOT_EXIST);
		}else{
			return ml.get(0);
		}
	}
	
	/**
	 * Get the member according to card.
	 * @param dbCon
	 * @param term
	 * @param cardAlias
	 * @return the member owns the card
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws the member to this card does NOT exist
	 */
	public static Member getMemberByCard(DBCon dbCon, Staff term, String cardAlias) throws SQLException, BusinessException{
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + term.getRestaurantId() + " AND M.member_card = '" + cardAlias + "'");
		List<Member> ml = MemberDao.getMember(dbCon, params);
		if(ml.isEmpty()){
			throw new BusinessException(MemberError.MEMBER_NOT_EXIST);
		}else{
			return ml.get(0);
		}
	}
	
	/**
	 * Get the member according to card.
	 * @param term
	 * @param cardAlias
	 * @return the member owns the card
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws the member to this card does NOT exist
	 */
	public static Member getMemberByCard(Staff term, String cardAlias) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getMemberByCard(dbCon, term, cardAlias);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the member according to mobile
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the term
	 * @param mobile
	 * 			the mobile
	 * @return the member matched this mobile
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the member to this mobile does NOT exist
	 */
	public static Member getMemberByMobile(DBCon dbCon, Staff term, String mobile) throws SQLException, BusinessException{
		Map<Object, Object> params = new HashMap<Object, Object>();
		params.put(SQLUtil.SQL_PARAMS_EXTRA, " AND M.restaurant_id = " + term.getRestaurantId() + " AND M.mobile = '" + mobile + "'");
		List<Member> result = MemberDao.getMember(dbCon, params);
		if(result.isEmpty()){
			throw new BusinessException(MemberError.MEMBER_NOT_EXIST);
		}else{
			return result.get(0);
		}
	}
	
	/**
	 * Get the member according to mobile.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param mobile
	 * 			the mobile
	 * @return the member matched this mobile
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the member to this mobile does NOT exist
	 */
	public static Member getMemberByMobile(Staff term, String mobile) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getMemberByMobile(dbCon, term, mobile);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Check to see whether the member is valid before insert or update.
	 * @param dbCon
	 * @param memberToCheck
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the mobile to new member has been exist before
	 * 			throws if the card to new member has been exist before
	 * 			throws if the member type does NOT exist
	 */
	private static void checkValid(DBCon dbCon, Member memberToCheck) throws SQLException, BusinessException{
		String querySQL;
		// 检查手机号码
		querySQL = "SELECT member_id FROM " + Params.dbName + ".member "
				 + " WHERE restaurant_id = " + memberToCheck.getRestaurantId()
				 + " AND mobile = '" + memberToCheck.getMobile() + "'";
		dbCon.rs = dbCon.stmt.executeQuery(querySQL);
		if(dbCon.rs != null && dbCon.rs.next() && dbCon.rs.getInt(1) > 0){
			if(memberToCheck.getId() != dbCon.rs.getInt(1)){
				throw new BusinessException(MemberError.HAS_MOBLIE);
			}
		}
		dbCon.rs.close();
		// 检查会员卡号
		if(memberToCheck.hasMemberCard()){
			querySQL = "SELECT member_id FROM " + Params.dbName + ".member "
					 + " WHERE restaurant_id = " + memberToCheck.getRestaurantId()
					 + " AND member_card = '" + memberToCheck.getMemberCard() + "'";
			dbCon.rs = dbCon.stmt.executeQuery(querySQL);
			if(dbCon.rs != null && dbCon.rs.next() && dbCon.rs.getInt(1) > 0){
				if(memberToCheck.getId() != dbCon.rs.getInt(1)){
					throw new BusinessException(MemberError.HAS_MEMBER_CARD);
				}
			}
			dbCon.rs.close();
		}
		
	}
	
	/**
	 * Insert a new member.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param builder
	 * 			the builder to new member
	 * @return the id to member just generated
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the mobile to new member has been exist before
	 * 			throws if the card to new member has been exist before
	 * 			throws if the member type does NOT exist
	 */
	public static int insert(DBCon dbCon, Staff term, Member.InsertBuilder builder) throws SQLException, BusinessException{
		Member member = builder.build();

		checkValid(dbCon, member);
		
		String insertSQL = " INSERT INTO " + Params.dbName + ".member " 
			+ "(member_type_id, member_card, restaurant_id, name, sex, tele, mobile, birthday, " 
			+ " id_card, company, taste_pref, taboo, contact_addr, comment, create_date, point)" 
			+ " VALUES( " 
			+ member.getMemberType().getTypeId() 
			+ ",'" + member.getMemberCard() + "'"
			+ "," + member.getRestaurantId() 
			+ ",'" + member.getName() + "'" 
			+ "," + member.getSex().getVal() 
			+ ",'" + member.getTele() + "'" 
			+ ",'" + member.getMobile() + "'" 
			+ "," + (member.getBirthday() != 0 ? ("'" + DateUtil.format(member.getBirthday()) + "'") : "NULL") 
			+ ",'" + member.getIdCard() + "'" 
			+ ",'" + member.getCompany()+ "'"
			+ ",'" + member.getTastePref() + "'" 
			+ ",'" + member.getTaboo() + "'" 
			+ ",'" + member.getContactAddress() + "'"
			+ ",'" + member.getComment()+ "'"
			+ ",'" + DateUtil.format(member.getCreateDate()) + "'"
			+ "," + "(SELECT initial_point FROM member_type WHERE member_type_id = " + member.getMemberType().getTypeId() + ")" + 
		")";
		
		dbCon.stmt.executeUpdate(insertSQL, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		if(dbCon.rs.next()){
			return dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The id of member is not generated successfully.");
		}	
	}
	
	/**
	 * Insert a new member.
	 * @param term
	 * 			the terminal
	 * @param builder
	 * 			the builder to new member
	 * @return the id to member just generated
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below<br>
	 * 			the mobile to new member has been exist before<br>
	 * 			the card to new member has been exist before
	 */
	public static int insert(Staff term, Member.InsertBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberDao.insert(dbCon, term, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update a member.
	 * @param term
	 * 			the terminal
	 * @param builder
	 * 			the builder to update member
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below<br>
	 * 			the mobile to new member has been exist before<br>
	 * 			the card to new member has been exist before<br>
	 * 			the member to update does NOT exist
	 */
	public static void update(DBCon dbCon, Staff term, Member.UpdateBuilder builder) throws SQLException, BusinessException{
		Member member = builder.build();
		// 旧会员类型是充值属性, 修改为优惠属性时, 检查是否还有余额, 有则不允许修改
		Member old = MemberDao.getMemberById(member.getId());
		if(member.getMemberType().getAttribute() != old.getMemberType().getAttribute() 
				&& old.getMemberType().getAttribute() == MemberType.Attribute.CHARGE){
			if(old.getTotalBalance() > 0){
				throw new BusinessException(MemberError.UPDATE_FAIL_HAS_BALANCE);
			}
		}
		checkValid(dbCon, member);
		
		String updateSQL = " UPDATE " + Params.dbName + ".member SET " 
			+ " name = '" + member.getName() + "'"
			+ " ,mobile = " + "'" + member.getMobile() + "'" 
			+ " ,member_type_id = " + member.getMemberType().getTypeId()
			+ " ,member_card = '" + member.getMemberCard() + "'" 
			+ " ,tele = '" + member.getTele() + "'" 
			+ " ,sex = " + member.getSex().getVal() 
			+ " ,id_card = '" + member.getIdCard() + "'" 
			+ " ,birthday = " + (member.getBirthday() != 0 ? ("'" + DateUtil.format(member.getBirthday()) + "'") : "NULL") 
			+ " ,company = '" + member.getCompany() + "'" 
			+ " ,taste_pref = '" + member.getTastePref() + "'" 
			+ " ,taboo = '" + member.getTaboo() + "'" 
			+ " ,contact_addr = '" + member.getContactAddress() + "'" 
			+ " ,comment = '" + member.getComment() + "'" 
			+ " WHERE member_id = " + member.getId(); 
		
		if(dbCon.stmt.executeUpdate(updateSQL) == 0){
			throw new BusinessException(MemberError.UPDATE_FAIL);
		}
	}
	
	/**
	 * Update a member.
	 * @param term
	 * 			the terminal
	 * @param builder
	 * 			the builder to update member
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below<br>
	 * 			the mobile to new member has been exist before<br>
	 * 			the card to new member has been exist before<br>
	 * 			the member to update does NOT exist
	 */
	public static void update(Staff term, Member.UpdateBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			update(dbCon, term, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the member and associated member operation (today & history) according to id
	 * @param term
	 * 			the terminal
	 * @param memberId
	 * 			the member id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void deleteById(Staff term, int memberId) throws SQLException {
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			MemberDao.deleteById(dbCon, term, memberId);
			dbCon.conn.commit();
			
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
			
		}finally{
			dbCon.disconnect();
		}
	}

	/**
	 * Delete the member and associated member operation (today & history) according to id
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param memberId
	 * 			the member id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void deleteById(DBCon dbCon, Staff term, int memberId) throws SQLException{
		String sql;
		//Delete the member operation.
		sql = " DELETE FROM " + Params.dbName + ".member_operation WHERE member_id = " + memberId;
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the member operation history.
		sql = " DELETE FROM " + Params.dbName + ".member_operation_history WHERE member_id = " + memberId;
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the member
		sql = " DELETE FROM " + Params.dbName + ".member WHERE member_id = " + memberId;
		dbCon.stmt.executeUpdate(sql);
		
	}
	
	/**
	 * Perform the consume operation to a member.
	 * @param dbCon	
	 * 			the database connection
	 * @param term	
	 * @param memberId 
	 * 			the id to member account
	 * @param consumePrice	
	 * 			the price to consume
	 * @param payType
	 * 			the payment type referred to {@link Order.PayType}
	 * @param orderId
	 * 			the associated order id to this consumption
	 * @return the member operation to the consumption operation
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 * @throws BusinessException
	 *	 		throws if one of cases below occurred<br>
	 *			1 - the consume price exceeds total balance to this member account<br>
	 *			2 - the member account to consume is NOT found.
	 */
	public static MemberOperation consume(DBCon dbCon, Staff term, int memberId, float consumePrice, Order.PayType payType, int orderId) throws SQLException, BusinessException{
		
		Member member = getMemberById(dbCon, memberId);
		
		//Perform the consume operation and get the related member operation.
		MemberOperation mo = member.consume(consumePrice, payType);
		
		//Set the associate order id
		mo.setOrderId(orderId);
		
		//Insert the member operation to this consumption operation.
		MemberOperationDao.insert(dbCon, term, mo);
		
		//Update the base & extra balance and point.
		String sql = " UPDATE " + Params.dbName + ".member SET" 
			+ " consumption_amount = " + member.getConsumptionAmount() + "," 
			+ " used_balance = " + member.getUsedBalance() + "," 
			+ " base_balance = " + member.getBaseBalance() + "," 
			+ " extra_balance = " + member.getExtraBalance() + ","
			+ " total_consumption = " + member.getTotalConsumption() + ","
			+ " total_point = " + member.getTotalPoint() + "," 
			+ " point = " + member.getPoint()
			+ " WHERE member_id = " + memberId;
		dbCon.stmt.executeUpdate(sql);
		
		return mo;
	}
	
	/**
	 * Perform the consume operation to a member.
	 * @param term	
	 * @param memberId 
	 * 			the id to member account
	 * @param consumePrice	
	 * 			the price to consume
	 * @param payType
	 * 			the payment type referred to {@link Order.PayType}
	 * @param orderId
	 * 			the associated order id to this consumption
	 * @return the member operation to the consumption operation
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements.
	 * @throws BusinessException
	 *	 		throws if one of cases below occurred<br>
	 *			1 - the consume price exceeds total balance to this member account<br>
	 *			2 - the member account to consume is NOT found.
	 */
	public static MemberOperation consume(Staff term, int memberId, float consumePrice, Order.PayType payType, int orderId) throws SQLException, BusinessException{
		
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			MemberOperation mo = MemberDao.consume(dbCon, term, memberId, consumePrice, payType, orderId);
			dbCon.conn.commit();
			return mo;
			
		}catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;	
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Perform the charge operation to a member account.
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal 
	 * @param memberId
	 * 			the id of member account to be charged.
	 * @param chargeMoney
	 * 			the amount of charge money
	 * @param chargeType
	 * 			the charge type referred to {@link Member.ChargeType}
	 * @return the member operation to this charge.
	 * @throws BusinessException
	 * 			throw if the member id to search is NOT found 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 */
	public static MemberOperation charge(DBCon dbCon, Staff term, int memberId, float chargeMoney, ChargeType chargeType) throws BusinessException, SQLException{
		
		if(chargeMoney < 0){
			throw new IllegalArgumentException("The amount of charge money(amount = " + chargeMoney + ") must be more than zero");
		}
		
		Member member = getMemberById(dbCon, memberId);
		
		//Perform the charge operation and get the related member operation.
		MemberOperation mo = member.charge(chargeMoney, chargeType);
		
		//Insert the member operation to this charge operation.
		MemberOperationDao.insert(dbCon, term, mo);
		
		//Update the base & extra balance and point.
		String sql = " UPDATE " + Params.dbName + ".member SET" +
					 " base_balance = " + member.getBaseBalance() + ", " +
					 " extra_balance = " + member.getExtraBalance() + "," + 
					 " total_charge = " + member.getTotalCharge() + 
					 " WHERE member_id = " + memberId;
		dbCon.stmt.executeUpdate(sql);
		
		return mo;
	}
	
	/**
	 * Perform the charge operation to a member account.
	 * @param term
	 * 			the terminal 
	 * @param memberId
	 * 			the id of member account to be charged.
	 * @param chargeMoney
	 * 			the amount of charge money
	 * @param chargeType
	 * 			the charge type referred to {@link Member.ChargeType}
	 * @return the member operation to this charge.
	 * @throws BusinessException
	 * 			throw if the member id to search is NOT found 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 */
	public static MemberOperation charge(Staff term, int memberId, float chargeMoney, ChargeType chargeType) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			MemberOperation mo = MemberDao.charge(dbCon, term, memberId, chargeMoney, chargeType);
			dbCon.conn.commit();
			return mo;
			
		}catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;	
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Perform to point consumption
	 * @param dbCon
	 * 			the database connection
	 * @param term
	 * 			the terminal
	 * @param memberId
	 * 			the id to member to perform point consumption
	 * @param pointConsume
	 * 			the amount of point consumption
	 * @return the member operation to this point consumption 
	 * @throws BusinessException
	 * 			throws if point to consume exceeds the remaining
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static MemberOperation pointConsume(DBCon dbCon, Staff term, int memberId, int pointConsume) throws BusinessException, SQLException{
		
		if(pointConsume < 0){
			throw new IllegalArgumentException("The amount of point to consume(amount = " + pointConsume + ") must be more than zero");
		}
		
		Member member = getMemberById(dbCon, memberId);
		
		//Perform the point consumption and get the related member operation.
		MemberOperation mo = member.pointConsume(pointConsume);
				
		//Insert the member operation to this point consumption.
		MemberOperationDao.insert(dbCon, term, mo);
		
		//Update the point.
		String sql = " UPDATE " + Params.dbName + ".member SET" +
				     " used_point = " + member.getUsedPoint() + "," +
					 " point = " + member.getPoint() + 
					 " WHERE member_id = " + memberId;
		dbCon.stmt.executeUpdate(sql);
		
		return mo;
	}
	
	/**
	 * Perform to point consumption
	 * @param term
	 * 			the terminal
	 * @param memberId
	 * 			the id to member to perform point consumption
	 * @param pointConsume
	 * 			the amount of point consumption
	 * @return the member operation to this point consumption 
	 * @throws BusinessException
	 * 			throws if point to consume exceeds the remaining
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static MemberOperation pointConsume(Staff term, int memberId, int pointConsume) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			MemberOperation mo = MemberDao.pointConsume(dbCon, term, memberId, pointConsume);
			dbCon.conn.commit();
			return mo;
			
		}catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;	
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Perform to adjust the point to a specified member.
	 * @param term
	 * 			the terminal
	 * @param memberId
	 * 			the member to adjust point
	 * @param deltaPoint
	 * 			the amount of point to adjust
	 * @return the related member operation
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the member to this id is NOT found
	 */
	public static MemberOperation adjustPoint(Staff term, int memberId, int deltaPoint, Member.AdjustType adjust) throws SQLException, BusinessException{
		
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			MemberOperation mo = MemberDao.adjustPoint(dbCon, term, memberId, deltaPoint, adjust);
			dbCon.conn.commit();
			return mo;
			
		}catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;	
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Perform to adjust the point to a specified member.
	 * @param term
	 * 			the terminal
	 * @param memberId
	 * 			the member to adjust point
	 * @param deltaPoint
	 * 			the amount of point to adjust
	 * @return the related member operation
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the member to this id is NOT found
	 */
	public static MemberOperation adjustPoint(DBCon dbCon, Staff term, int memberId, int deltaPoint, Member.AdjustType adjust) throws SQLException, BusinessException{
		
		Member member = getMemberById(dbCon, memberId);
		
		if(adjust == Member.AdjustType.INCREASE){
			deltaPoint = Math.abs(deltaPoint);
		}else if(adjust == Member.AdjustType.REDUCE){
			deltaPoint = -Math.abs(deltaPoint);
		}
		
		//Perform the point adjust and get the related member operation.
		MemberOperation mo = member.adjustPoint(deltaPoint, adjust);
				
		//Insert the member operation to this point consumption.
		MemberOperationDao.insert(dbCon, term, mo);
		
		//Update the point.
		String sql = " UPDATE " + Params.dbName + ".member SET" +
					 " point = " + member.getPoint() + 
					 " WHERE member_id = " + memberId;
		
		dbCon.stmt.executeUpdate(sql);
		
		return mo;
		
	}
	
	/**
	 * Perform to adjust balance to a specified member. 
	 * @param term
	 * 			the terminal
	 * @param memberId
	 * 			the member to adjust balance
	 * @param deltaBalance
	 * 			the balance to adjust
	 * @return the related member operation 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the member to this id is NOT found
	 */
	public static MemberOperation adjustBalance(Staff term, int memberId, float deltaBalance) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			MemberOperation mo = MemberDao.adjustBalance(dbCon, term, memberId, deltaBalance);
			dbCon.conn.commit();
			return mo;
			
		}catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;	
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Perform to adjust balance to a specified member. 
	 * @param dbCon
	 * 			the database
	 * @param term
	 * 			the terminal
	 * @param memberId
	 * 			the member to adjust balance
	 * @param deltaBalance
	 * 			the balance to adjust
	 * @return the related member operation 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the member to this id is NOT found
	 */
	public static MemberOperation adjustBalance(DBCon dbCon, Staff term, int memberId, float deltaBalance) throws SQLException, BusinessException{
		Member member = getMemberById(dbCon, memberId);
		
		//Perform the point adjust and get the related member operation.
		MemberOperation mo = member.adjustBalance(deltaBalance);
				
		//Insert the member operation to this point consumption.
		MemberOperationDao.insert(dbCon, term, mo);
		
		//Update the point.
		String sql = " UPDATE " + Params.dbName + ".member SET" +
					 " base_balance = " + member.getBaseBalance() + "," +
					 " extra_balance =  " + member.getExtraBalance() + 
					 " WHERE member_id = " + memberId;
		
		dbCon.stmt.executeUpdate(sql);
		
		return mo;
	}

}
