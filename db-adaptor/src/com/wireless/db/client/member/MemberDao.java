package com.wireless.db.client.member;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.promotion.CouponDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.weixin.member.WeixinMemberDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.MemberError;
import com.wireless.exception.ModuleError;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.client.MemberComment.CommitBuilder;
import com.wireless.pojo.client.MemberOperation;
import com.wireless.pojo.client.MemberOperation.ChargeType;
import com.wireless.pojo.client.MemberType;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.distMgr.Discount;
import com.wireless.pojo.distMgr.Discount.Type;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.promotion.Coupon;
import com.wireless.pojo.restaurantMgr.Module;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateUtil;
import com.wireless.util.SQLUtil;

public class MemberDao {
	
	public static class IdleExtraCond implements Jsonable{
		
		private static final ExtraCond mExtraCond = new ExtraCond();
		
		public static ExtraCond instance(){
			return mExtraCond;
		}
		
		private IdleExtraCond(){
			//活跃会员的条件：3个月内消费不足3次
			Calendar c = Calendar.getInstance();
			c.add(Calendar.MONTH, -3);
			mExtraCond.range = new DutyRange(c.getTime().getTime(), System.currentTimeMillis());
			
			mExtraCond.maxConsumeAmount = 3;
		}

		@Override
		public JsonMap toJsonMap(int flag) {
			// TODO 
			return null;
		}

		@Override
		public void fromJsonMap(JsonMap jsonMap, int flag) {
		}
	}
	
	public static class ActiveExtraCond implements Jsonable{
		
		private static final ExtraCond mExtraCond = new ExtraCond();
		
		public static ExtraCond instance(){
			return mExtraCond;
		}
		
		private ActiveExtraCond(){
			//活跃会员的条件：3个月内消费5次
			Calendar c = Calendar.getInstance();
			c.add(Calendar.MONTH, -3);
			mExtraCond.range = new DutyRange(c.getTime().getTime(), System.currentTimeMillis());
			
			mExtraCond.minConsumeAmount = 5;

		}

		@Override
		public JsonMap toJsonMap(int flag) {
			// TODO 
			return null;
		}

		@Override
		public void fromJsonMap(JsonMap jsonMap, int flag) {
			
		}
	}
	
	public static class ExtraCond{
		private int id;
		private String card;
		private String mobile;
		private String mobileOrCard;
		private String name;
		private String fuzzy;
		private int memberTypeId;
		private int minConsumeAmount;
		private int maxConsumeAmount;
		private int minTotalConsume;
		private int maxTotalConsume;
		private DutyRange range;
		
		public ExtraCond setId(int id){
			this.id = id;
			return this;
		}
		
		public ExtraCond setCard(String card){
			this.card = card;
			return this;
		}
		
		public ExtraCond setMobile(String mobile){
			this.mobile = mobile;
			return this;
		} 
		
		public ExtraCond setMobileOrCard(String mobileOrCard){
			this.mobileOrCard = mobileOrCard;
			return this;
		} 
		
		public ExtraCond setFuzzyName(String fuzzy){
			this.fuzzy = fuzzy;
			return this;
		}
		
		public ExtraCond setMemberType(int typeId){
			this.memberTypeId = typeId;
			return this;
		}
		
		public ExtraCond greaterConsume(int amount){
			this.minConsumeAmount = amount;
			this.maxConsumeAmount = 0;
			return this;
		}
		
		public ExtraCond lessConsume(int amount){
			this.maxConsumeAmount = amount;
			this.minConsumeAmount = 0;
			return this;
		}
		
		public ExtraCond setConsumeRange(int min, int max){
			this.minConsumeAmount = min;
			this.maxConsumeAmount = max;
			return this;
		}
		
		public ExtraCond greaterTotalConsume(int totalConsume){
			this.minTotalConsume = totalConsume;
			this.maxTotalConsume = 0;
			return this;
		}
		
		public ExtraCond lessTotalConsume(int totalConsume){
			this.maxTotalConsume = totalConsume;
			this.minTotalConsume = 0;
			return this;
		}
		
		public ExtraCond setTotalConsume(int min, int max){
			this.minTotalConsume = min;
			this.maxTotalConsume = max;
			return this;
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(fuzzy != null){
				extraCond.append(" AND (M.name LIKE '%" + fuzzy + "%' OR M.member_card LIKE '%" + fuzzy + "%' OR M.mobile LIKE '%" + fuzzy + "%')");
			}
			if(id > 0){
				extraCond.append(" AND M.member_id = " + id);
			}
			if(card != null){
				extraCond.append(" AND M.member_card_crc = CRC32('" + card + "') AND M.member_card = '" + card + "'");
			}
			if(mobile != null){
				extraCond.append(" AND M.mobile_crc = CRC32('" + mobile + "') AND M.mobile = '" + mobile + "'");
			}
			if(mobileOrCard != null){
				extraCond.append(" AND (M.mobile = '" + mobileOrCard + "' OR M.member_card = '" + mobileOrCard + "')");
			}
			
			if(name != null){
				extraCond.append(" AND M.name = '" + name + "'");
			}
			if(memberTypeId > 0){
				extraCond.append(" AND MT.member_type_id = " + memberTypeId);
			}
			
			if(range != null){
				
				StringBuilder havingCond = new StringBuilder();
				if(minConsumeAmount > 0 && maxConsumeAmount == 0){
					havingCond.append(" AND COUNT(*) >= " + minConsumeAmount);
				}else if(maxConsumeAmount > 0 && minConsumeAmount == 0){
					havingCond.append(" AND COUNT(*) <= " + maxConsumeAmount);
				}else if(maxConsumeAmount > 0 && minConsumeAmount > 0){
					havingCond.append(" AND COUNT(*) BETWEEN " + minConsumeAmount + " AND " + maxConsumeAmount);
				}
				
				if(minTotalConsume > 0 && maxTotalConsume == 0){
					havingCond.append(" AND SUM(pay_money) >= " + minConsumeAmount);
				}else if(maxTotalConsume > 0 && minTotalConsume == 0){
					havingCond.append(" AND SUM(pay_money) <= " + maxConsumeAmount);
				}else if(maxTotalConsume > 0 && minTotalConsume > 0){
					havingCond.append(" AND SUM(pay_money) BETWEEN " + minTotalConsume + " AND " + maxTotalConsume);
				}
				String sql;
				sql = " SELECT member_id FROM " + Params.dbName + ".member_operation_history " +
					  " WHERE 1 = 1 " +
					  " AND operate_type = " + MemberOperation.OperationType.CONSUME.getValue() + 
					  " AND operate_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
					  " GROUP BY member_id " +
					  " HAVING 1 = 1 " +
					  havingCond.toString();
				
				extraCond.append(" AND M.member_id IN ( " + sql + ")");
				
			}else{
				if(minConsumeAmount > 0 && maxConsumeAmount == 0){
					extraCond.append(" AND M.consumption_amount >= " + minConsumeAmount);
				}else if(maxConsumeAmount > 0 && minConsumeAmount == 0){
					extraCond.append(" AND M.consumption_amount <= " + maxConsumeAmount);
				}else if(maxConsumeAmount > 0 && minConsumeAmount > 0){
					extraCond.append(" AND M.consumption_amount BETWEEN " + minConsumeAmount + " AND " + maxConsumeAmount);
				}
				
				if(minTotalConsume > 0 && maxTotalConsume == 0){
					extraCond.append(" AND M.total_consumption >= " + minConsumeAmount);
				}else if(maxTotalConsume > 0 && minTotalConsume == 0){
					extraCond.append(" AND M.total_consumption <= " + maxConsumeAmount);
				}else if(maxTotalConsume > 0 && minTotalConsume > 0){
					extraCond.append(" AND M.total_consumption BETWEEN " + minTotalConsume + " AND " + maxTotalConsume);
				}
				
			}
			
			return extraCond.toString();
		}
	}
	
	public final static class UpgradeResult{
		private final int elapsedTime;
		private final int memberAmount;
		private final int memberUpgradeAmount;
		
		UpgradeResult(int elapsedTime, int memberAmount, int memberUpgradeAmount) {
			this.elapsedTime = elapsedTime;
			this.memberAmount = memberAmount;
			this.memberUpgradeAmount = memberUpgradeAmount;
		}
		
		public int getElapsedTime(){
			return this.elapsedTime;
		}
		
		public int getMemberAmount(){
			return this.memberAmount;
		}
		
		@Override
		public String toString(){
			return "The calculation to " + memberUpgradeAmount + "/" + memberAmount + " member(s) upgrade takes " + elapsedTime + " sec.";
		}
	}
	
	public final static class CalcRecommendResult{
		private final int elapsedTime;
		
		public CalcRecommendResult(int elapsedTime) {
			this.elapsedTime = elapsedTime;
		}
		
		public int getElapsedTime(){
			return this.elapsedTime;
		}
		
		@Override
		public String toString(){
			return "The calculation to member recommended foods takes " + elapsedTime + " sec.";
		}
	}
	
	public final static class CalcFavorResult{
		private final int elapsedTime;
		
		public CalcFavorResult(int elapsedTime) {
			this.elapsedTime = elapsedTime;
		}
		
		public int getElapsedTime(){
			return this.elapsedTime;
		}
		
		@Override
		public String toString(){
			return "The calculation to member favor foods takes " + elapsedTime + " sec.";
		}
	}
	
	public final static class MemberRank{
		private final int total;
		private final int rank;
		MemberRank(int total, int rank){
			this.total = total;
			this.rank = rank;
		}
		
		public int getTotal(){
			return this.total;
		}
		
		public int getRank(){
			return this.rank;
		}
		
		@Override
		public String toString(){
			return "member rank(total=" + total + ",rank=" + rank + ")";
		}
	}
	
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
				+ " FROM (" + Params.dbName + ".member M " 
				+ " JOIN " + Params.dbName + ".member_type MT ON M.member_type_id = MT.member_type_id) " 
				+ " LEFT JOIN " + Params.dbName + ".interested_member IM ON M.member_id = IM.member_id " 
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
	 * Get the member associated with detail according to extra condition.
	 * @param dbCon
	 * 			the database
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the result to member associated with details
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if any member type does NOT exist
	 */
	private static List<Member> getDetail(DBCon dbCon, Staff staff, ExtraCond extraCond, String orderClause) throws SQLException, BusinessException{
		List<Member> result = getByCond(dbCon, staff, extraCond.toString(), orderClause);
		
		for(Member eachMember : result){
			
			String sql;
			
			//Get the member type detail to each member.
			eachMember.getMemberType().copyFrom(MemberTypeDao.getMemberTypeById(dbCon, staff, eachMember.getMemberType().getId()));
			
			//Get the discounts to each member
			sql = " SELECT MD.discount_id, MD.type, D.name, D.type AS d_type FROM " + Params.dbName + ".member_type_discount MD " +
					" JOIN " + Params.dbName + ".discount D " +
					" ON MD.discount_id = D.discount_id " +
					" WHERE member_type_id = " + eachMember.getMemberType().getId();
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				Discount discount = new Discount(dbCon.rs.getInt("MD.discount_id"));
				discount.setName(dbCon.rs.getString("D.name"));
				discount.setType(Type.valueOf(dbCon.rs.getInt("d_type")));
				eachMember.getMemberType().addDiscount(discount);
				if(MemberType.DiscountType.valueOf(dbCon.rs.getInt("type")) == MemberType.DiscountType.DEFAULT){
					eachMember.getMemberType().setDefaultDiscount(discount);
				}
			}
			dbCon.rs.close();
			
			//Get the public comments to each member
			eachMember.setPublicComments(MemberCommentDao.getPublicCommentByMember(dbCon, staff, eachMember));
			
			//Get the private comment to each member
			eachMember.setPrivateComment(MemberCommentDao.getPrivateCommentByMember(dbCon, staff, eachMember));
			
			//Get the favor foods to each member
			sql = " SELECT " +
				  " F.food_id, F.food_alias, F.name, F.restaurant_id " +
				  " FROM " + Params.dbName + ".member_favor_food MFF " +
				  " JOIN " + Params.dbName + ".food F ON MFF.food_id = F.food_id " +
				  " WHERE MFF.member_id = " + eachMember.getId() +
				  " ORDER BY point DESC ";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				Food favorFood = new Food(dbCon.rs.getInt("food_id"));
				favorFood.setAliasId(dbCon.rs.getInt("food_alias"));
				favorFood.setName(dbCon.rs.getString("name"));
				favorFood.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
				eachMember.addFavorFood(favorFood);
			}
			dbCon.rs.close();
			
			//Get the recommend foods to each member
			sql = " SELECT " +
				  " F.food_id, F.food_alias, F.name, F.restaurant_id " +
				  " FROM " + Params.dbName + ".member_recommend_food MRF " +
				  " JOIN " + Params.dbName + ".food F ON MRF.food_id = F.food_id " +
				  " WHERE MRF.member_id = " + eachMember.getId() +
				  " ORDER BY point DESC ";
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			while(dbCon.rs.next()){
				Food recommendFood = new Food(dbCon.rs.getInt("food_id"));
				recommendFood.setAliasId(dbCon.rs.getInt("food_alias"));
				recommendFood.setName(dbCon.rs.getString("name"));
				recommendFood.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
				eachMember.addRecommendFood(recommendFood);
			}
			dbCon.rs.close();
			
		}
		
		return result;
	}
	
	/**
	 * Get the interested member according to extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @return the result to interested members
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if any member type does NOT exist
	 */
	public static List<Member> getInterestedMember(Staff staff, String extraCond) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getInterestedMember(dbCon, staff, extraCond, null);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the interested member according to extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause
	 * @return the result to interested members
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if any member type does NOT exist
	 */
	private static List<Member> getInterestedMember(DBCon dbCon, Staff staff, String extraCond, String orderClause) throws SQLException, BusinessException{
		extraCond = " AND M.member_id IN( SELECT member_id FROM interested_member WHERE staff_id = " + staff.getId() + ")" + (extraCond != null ? extraCond : "");
		return getByCond(dbCon, staff, extraCond, null);
	}
	
	/**
	 * Get member by extra condition.
	 * @param dbCon
	 * 			the database connection
	 * @param extraCond
	 * 			the extra condition
	 * @param orderClause
	 * 			the order clause 
	 * @return the list holding the result
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if any member type does NOT exist
	 */
	private static List<Member> getByCond(DBCon dbCon, Staff staff, String extraCond, String orderClause) throws SQLException, BusinessException{
		List<Member> result = new ArrayList<Member>();
		String sql;
		sql = " SELECT "	+
			  " M.member_id, M.restaurant_id, M.point, M.used_point, " +
			  " M.base_balance, M.extra_balance, M.consumption_amount, M.last_consumption, M.used_balance," +
			  " M.total_consumption, M.total_point, M.total_charge, " +
			  " M.member_card, M.name AS member_name, M.sex, M.create_date, " +
			  " M.tele, M.mobile, M.birthday, M.id_card, M.company, M.contact_addr, M.comment, " +
			  " MT.member_type_id, MT.name AS member_type_name, MT.attribute, MT.exchange_rate, MT.charge_rate, MT.type, MT.initial_point " +
			  " FROM " + Params.dbName + ".member M " +
			  " JOIN " + Params.dbName + ".member_type MT ON M.member_type_id = MT.member_type_id " +
			  " WHERE 1 = 1 " +
			  " AND M.restaurant_id = " + staff.getRestaurantId() +
			  (extraCond != null ? extraCond : " ") +
			  (orderClause != null ? orderClause : "");
			
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			Member member = new Member(dbCon.rs.getInt("member_id"));
			member.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			member.setBaseBalance(dbCon.rs.getFloat("base_balance"));
			member.setExtraBalance(dbCon.rs.getFloat("extra_balance"));
			member.setUsedBalance(dbCon.rs.getFloat("used_balance"));
			member.setConsumptionAmount(dbCon.rs.getInt("consumption_amount"));
			Timestamp ts = dbCon.rs.getTimestamp("last_consumption");
			if(ts != null){
				member.setLastConsumption(ts.getTime());
			}
			member.setUsedPoint(dbCon.rs.getInt("used_point"));
			member.setPoint(dbCon.rs.getInt("point"));
			member.setTotalConsumption(dbCon.rs.getFloat("total_consumption"));
			member.setTotalPoint(dbCon.rs.getInt("total_point"));
			member.setTotalCharge(dbCon.rs.getFloat("total_charge"));
			member.setMemberCard(dbCon.rs.getString("member_card"));
			member.setName(dbCon.rs.getString("member_name"));
			member.setSex(dbCon.rs.getInt("sex"));
			member.setCreateDate(dbCon.rs.getTimestamp("create_date").getTime());
			member.setTele(dbCon.rs.getString("tele"));
			member.setMobile(dbCon.rs.getString("mobile"));
			ts = dbCon.rs.getTimestamp("birthday");
			if(ts != null){
				member.setBirthday(ts.getTime());
			}
			member.setIdCard(dbCon.rs.getString("id_card"));
			member.setCompany(dbCon.rs.getString("company"));
			member.setContactAddress(dbCon.rs.getString("contact_addr"));
			
			MemberType memberType = new MemberType(dbCon.rs.getInt("member_type_id"));
			memberType.setName(dbCon.rs.getString("member_type_name"));
			memberType.setAttribute(dbCon.rs.getInt("attribute"));
			memberType.setExchangeRate(dbCon.rs.getFloat("exchange_rate"));
			memberType.setChargeRate(dbCon.rs.getFloat("charge_rate"));
			memberType.setType(dbCon.rs.getInt("type"));
			memberType.setInitialPoint(dbCon.rs.getInt("initial_point"));
			member.setMemberType(memberType);
			
			result.add(member);
		}
		dbCon.rs.close();
		
		return result;
	}
	
	/**
	 * Get member by extra condition
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @param orderClause
	 * 			the order clause 
	 * @return the list holding the result
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<Member> getByCond(Staff staff, ExtraCond extraCond, String orderClause) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			if(extraCond != null){
				return MemberDao.getByCond(dbCon, staff, extraCond.toString(), orderClause);
			}else{
				return MemberDao.getByCond(dbCon, staff, null, orderClause);
			}
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the member according to member id.
	 * @param staff
	 * 			the staff to perform this action
	 * @param memberId
	 * 			the member id to search
	 * @return the member associated with this id
	 * @throws BusinessException
	 * 			throws if the member to this id is NOT found
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static Member getById(Staff staff, int memberId) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberDao.getById(dbCon, staff, memberId);
		}finally{
			dbCon.disconnect();
		}
	}
	/**
	 * Get the member according to member id.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param memberId
	 * 			the member id to search
	 * @return the member associated with this id
	 * @throws BusinessException
	 * 			throws if the member to this id is NOT found
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static Member getById(DBCon dbCon, Staff staff, int memberId) throws BusinessException, SQLException{
		List<Member> ml = MemberDao.getDetail(dbCon, staff, new ExtraCond().setId(memberId), null);
		if(ml.isEmpty()){
			throw new BusinessException(MemberError.MEMBER_NOT_EXIST);
		}else{
			return ml.get(0);
		}
	}
	
	/**
	 * Get the member according to card.
	 * @param dbCon
	 * @param staff
	 * @param cardAlias
	 * @return the member owns the card
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws the member to this card does NOT exist
	 */
	public static Member getByCard(DBCon dbCon, Staff staff, String cardAlias) throws SQLException, BusinessException{
		List<Member> ml = MemberDao.getDetail(dbCon, staff, new ExtraCond().setCard(cardAlias), null);
		if(ml.isEmpty()){
			throw new BusinessException(MemberError.MEMBER_NOT_EXIST);
		}else{
			return ml.get(0);
		}
	}
	
	/**
	 * Get the member according to card.
	 * @param staff
	 * @param cardAlias
	 * @return the member owns the card
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws the member to this card does NOT exist
	 */
	public static Member getByCard(Staff staff, String cardAlias) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByCard(dbCon, staff, cardAlias);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the member according to mobile
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the term
	 * @param mobile
	 * 			the mobile
	 * @return the member matched this mobile
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the member to this mobile does NOT exist
	 */
	public static Member getByMobile(DBCon dbCon, Staff staff, String mobile) throws SQLException, BusinessException{
		List<Member> result = MemberDao.getDetail(dbCon, staff, new ExtraCond().setMobile(mobile), null);
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
	 * @param staff
	 * 			the staff to perform this action
	 * @param mobile
	 * 			the mobile
	 * @return the member matched this mobile
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the member to this mobile does NOT exist
	 */
	public static Member getByMobile(Staff staff, String mobile) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByMobile(dbCon, staff, mobile);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Insert a new member.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
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
	public static int insert(DBCon dbCon, Staff staff, Member.InsertBuilder builder) throws SQLException, BusinessException{
		//判断是否有会员模块
		if(!RestaurantDao.getById(staff.getRestaurantId()).hasModule(Module.Code.MEMBER)){
			//限制添加条数
			String sql = "SELECT COUNT(*) FROM " + Params.dbName + ".member WHERE restaurant_id = " + staff.getRestaurantId();
			dbCon.rs = dbCon.stmt.executeQuery(sql);
			int memberCount = 0;
			if(dbCon.rs.next()){
				memberCount = dbCon.rs.getInt(1);
			}
			dbCon.rs.close(); 
			if(memberCount > 50){
				throw new BusinessException(ModuleError.MEMBER_LIMIT);
			}
		}
		
		Member member = builder.build();

		//检查是否信息有重复
		//Check to see whether the mobile or member card is duplicated.
		if(!getByCond(dbCon, staff, new ExtraCond().setMobile(member.getMobile()).toString(), null).isEmpty()){
			throw new BusinessException(MemberError.MOBLIE_DUPLICATED);
		}
		if(member.hasMemberCard() && !getByCond(dbCon, staff, new ExtraCond().setCard(member.getMemberCard()).toString(), null).isEmpty()){
			throw new BusinessException(MemberError.MEMBER_CARD_DUPLICATED);
		}
		
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".member " +
			  " (member_type_id, member_card, member_card_crc, restaurant_id, name, sex, tele, mobile, mobile_crc, birthday, " +
			  " id_card, company, contact_addr, create_date, point)" +
			  " VALUES( " +
			  member.getMemberType().getId() + "," + 
			  "'" + member.getMemberCard() + "'," +
			  " CRC32('" + member.getMemberCard() + "')," +
			  staff.getRestaurantId() + "," + 
			  "'" + member.getName() + "'," + 
			  member.getSex().getVal() + "," + 
			  "'" + member.getTele() + "'," + 
			  "'" + member.getMobile() + "'," +
			  " CRC32('" + member.getMobile() + "')," +
			 (member.getBirthday() != 0 ? ("'" + DateUtil.format(member.getBirthday()) + "'") : "NULL")	+ "," +
			 "'" + member.getIdCard() + "'," + 
			 "'" + member.getCompany()+ "'," +
			 "'" + member.getContactAddress() + "'," +
			 "'" + DateUtil.format(member.getCreateDate()) + "'," +
			 " (SELECT initial_point FROM member_type WHERE member_type_id = " + member.getMemberType().getId() + ")" + 
			 ")";
		
		dbCon.stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		dbCon.rs = dbCon.stmt.getGeneratedKeys();
		int memberId = 0;
		if(dbCon.rs.next()){
			memberId = dbCon.rs.getInt(1);
		}else{
			throw new SQLException("The id of member is not generated successfully.");
		}	
		
		//Commit the private comment
		if(member.hasPrivateComment()){
			MemberCommentDao.commit(dbCon, staff, CommitBuilder.newPrivateBuilder(staff.getId(), memberId, member.getPrivateComment().getComment()));
		}
		
		//Commit the public comment
		if(member.hasPublicComments()){
			MemberCommentDao.commit(dbCon, staff, CommitBuilder.newPublicBuilder(staff.getId(), memberId, member.getPublicComments().get(0).getComment()));
		}
		
		return memberId;
	}
	
	/**
	 * Insert a new member.
	 * @param staff
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
	public static int insert(Staff staff, Member.InsertBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int memberId = MemberDao.insert(dbCon, staff, builder);
			dbCon.conn.commit();
			return memberId;
			
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
			
		}catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Update a member.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to update member
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			<li>throws if the mobile to new member has been exist before
	 * 			<li>throws if the card to new member has been exist before
	 * 			<li>throws if the member to update does NOT exist
	 */
	public static void update(DBCon dbCon, Staff staff, Member.UpdateBuilder builder) throws SQLException, BusinessException{
		Member member = builder.build();
		// 旧会员类型是充值属性, 修改为优惠属性时, 检查是否还有余额, 有则不允许修改
		if(builder.isMemberTypeChanged()){
			MemberType newType = MemberTypeDao.getMemberTypeById(dbCon, staff, member.getMemberType().getId());
			Member oriMember = MemberDao.getById(staff, member.getId());
			if(newType.isPoint() && oriMember.getMemberType().isCharge() && oriMember.getTotalBalance() > 0){
				throw new BusinessException("该会员还有余额, 不允许设为优惠属性的类型会员", MemberError.UPDATE_FAIL);
			}
		}
		
		//Check to see whether the mobile or member card is duplicated.
		if(builder.isMobileChanged()){
			if(!getByCond(dbCon, staff, new ExtraCond().setMobile(member.getMobile()) + " AND M.member_id <> " + member.getId(), null).isEmpty()){
				throw new BusinessException(MemberError.MOBLIE_DUPLICATED);
			}
		}
		if(builder.isMemberCardChanged()){
			if(member.hasMemberCard() && !getByCond(dbCon, staff, new ExtraCond().setCard(member.getMemberCard()) + " AND M.member_id <> " + member.getId(), null).isEmpty()){
				throw new BusinessException(MemberError.MEMBER_CARD_DUPLICATED);
			}
		}
		
		String sql;
		sql = " UPDATE " + Params.dbName + ".member SET " +
			  " member_id = " + member.getId() +
			  (builder.isNameChanged() ? " ,name = '" + member.getName() + "'" : "") +
			  (builder.isMobileChanged() ? " ,mobile = " + "'" + member.getMobile() + "'" : "") +
			  (builder.isMobileChanged() ? " ,mobile_crc = CRC32('" + member.getMobile() + "')" : "") +
			  (builder.isMemberTypeChanged() ? " ,member_type_id = " + member.getMemberType().getId() : "")	+
			  (builder.isMemberCardChanged() ? " ,member_card = '" + member.getMemberCard() + "'" : "")	+
			  (builder.isMemberCardChanged() ? ", member_card_crc = CRC32('" + member.getMemberCard() + "')" : "") +
			  (builder.isTeleChanged() ? " ,tele = '" + member.getTele() + "'" : "") +
			  (builder.isSexChanged() ? " ,sex = " + member.getSex().getVal() : "")	+
			  (builder.isIdChardChanged() ? " ,id_card = '" + member.getIdCard() + "'" : "") +
			  (builder.isBirthdayChanged() ? " ,birthday = '" + DateUtil.format(member.getBirthday()) + "'" : "") +
			  (builder.isCompanyChanged() ? " ,company = '" + member.getCompany() + "'" : "") +
			  (builder.isContactAddrChanged() ? " ,contact_addr = '" + member.getContactAddress() + "'" : "") +
			  " WHERE member_id = " + member.getId(); 
		
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(MemberError.MEMBER_NOT_EXIST);
		}
		
		//Commit the private comment
		if(builder.isPrivateCommentChanged()){
			MemberCommentDao.commit(dbCon, staff, CommitBuilder.newPrivateBuilder(staff.getId(), member.getId(), member.getPrivateComment().getComment()));
		}
		
		//Commit the public comment
		if(builder.isPublicCommentChanged()){
			MemberCommentDao.commit(dbCon, staff, CommitBuilder.newPublicBuilder(staff.getId(), member.getId(), member.getPublicComments().get(0).getComment()));
		}
	}
	
	/**
	 * Update a member.
	 * @param staff
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
	public static void update(Staff staff, Member.UpdateBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			update(dbCon, staff, builder);
			dbCon.conn.commit();
			
		}catch(SQLException e){
			dbCon.conn.rollback();
			throw e;
			
		}catch(BusinessException e){
			dbCon.conn.rollback();
			throw e;
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the member and associated member operation (today & history) according to id
	 * @param staff
	 * 			the terminal
	 * @param memberId
	 * 			the member id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void deleteById(Staff staff, int memberId) throws SQLException {
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			MemberDao.deleteById(dbCon, staff, memberId);
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
	 * @param staff
	 * 			the staff to perform this action
	 * @param memberId
	 * 			the member id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void deleteById(DBCon dbCon, Staff staff, int memberId) throws SQLException{
		String sql;
		//Delete the coupon associated with this member
		CouponDao.delete(dbCon, staff, new CouponDao.ExtraCond().setMember(memberId));
		
		//Delete the interested member.
		sql = " DELETE FROM " + Params.dbName + ".interested_member WHERE member_id = " + memberId;
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the member comment.
		sql = " DELETE FROM " + Params.dbName + ".member_comment WHERE member_id = " + memberId;
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the member operation.
		//sql = " DELETE FROM " + Params.dbName + ".member_operation WHERE member_id = " + memberId;
		//dbCon.stmt.executeUpdate(sql);
		
		//Delete the member operation history.
		//sql = " DELETE FROM " + Params.dbName + ".member_operation_history WHERE member_id = " + memberId;
		//dbCon.stmt.executeUpdate(sql);
		
		//Change the weixin status to be interested if exist
		sql = " UPDATE " + Params.dbName + ".weixin_member SET " +
			  " status = " + WeixinMemberDao.Status.INTERESTED.getVal() +
			  " WHERE member_id = " + memberId;
		dbCon.stmt.executeUpdate(sql);
		
		//Delete the member
		sql = " DELETE FROM " + Params.dbName + ".member WHERE member_id = " + memberId;
		dbCon.stmt.executeUpdate(sql);
		
	}
	
	/**
	 * Perform to be interested in specific member.
	 * @param staff
	 * 			the staff to perform this action
	 * @param memberId
	 * 			the member to be interested in
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void interestedIn(Staff staff, int memberId) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			interestedIn(dbCon, staff, memberId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Perform to be interested in specific member.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param memberId
	 * 			the member to be interested in
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void interestedIn(DBCon dbCon, Staff staff, int memberId) throws SQLException{
		String sql;
		sql = " SELECT * FROM " + Params.dbName + ".interested_member WHERE member_id = " + memberId + " AND " + " staff_id = " + staff.getId();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(!dbCon.rs.next()){
			sql = " INSERT INTO " + Params.dbName + ".interested_member " +
				  " (`staff_id`, `member_id`, `start_date`) " + 
				  " VALUES (" +
				  staff.getId() + "," +
				  memberId + "," +
				  "NOW()" + 
				  " ) ";
			dbCon.stmt.executeUpdate(sql);
		}
		
		dbCon.rs.close();
	}
	
	/**
	 * Cancel interested in the specific member
	 * @param staff
	 * 			the staff to perform this action
	 * @param memberId
	 * 			the member to cancel interested
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void cancelInterestedIn(Staff staff, int memberId) throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			cancelInterestedIn(dbCon, staff, memberId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Cancel interested in the specific member
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param memberId
	 * 			the member to cancel interested
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static void cancelInterestedIn(DBCon dbCon, Staff staff, int memberId) throws SQLException{
		String sql;
		sql = " DELETE FROM " + Params.dbName + ".interested_member WHERE member_id = " + memberId + " AND " + " staff_id = " + staff.getId();
		dbCon.stmt.executeUpdate(sql);
	}
	
	/**
	 * Perform the consume operation to a member.
	 * @param dbCon	
	 * 			the database connection
	 * @param staff	
	 * 			the staff to perform this action
	 * @param memberId 
	 * 			the id to member account
	 * @param consumePrice	
	 * 			the price to consume
	 * @param coupon
	 * 			the coupon to use, null means no coupon
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
	public static MemberOperation consume(DBCon dbCon, Staff staff, int memberId, float consumePrice, Coupon coupon, Order.PayType payType, int orderId) throws SQLException, BusinessException{
		
		Member member = getById(dbCon, staff, memberId);
		
		//Perform the consume operation and get the related member operation.
		MemberOperation mo = member.consume(consumePrice, coupon, payType);
		
		//Perform to use coupon.
		if(coupon != null){
			CouponDao.use(dbCon, staff, coupon.getId(), orderId);
		}
		
		//Set the associate order id
		mo.setOrderId(orderId);
		
		//Insert the member operation to this consumption operation.
		MemberOperationDao.insert(dbCon, staff, mo);
		
		//Update the base & extra balance and point.
		String sql = " UPDATE " + Params.dbName + ".member SET" + 
					 " consumption_amount = " + member.getConsumptionAmount() +
					 " ,last_consumption = '" + DateUtil.format(System.currentTimeMillis(), DateUtil.Pattern.DATE_TIME) + "'" +
					 " ,used_balance = " + member.getUsedBalance() +  
					 " ,base_balance = " + member.getBaseBalance() +  
					 " ,extra_balance = " + member.getExtraBalance() + 
					 " ,total_consumption = " + member.getTotalConsumption() + 
					 " ,total_point = " + member.getTotalPoint() +  
					 " ,point = " + member.getPoint() +
					 " WHERE member_id = " + memberId;
		dbCon.stmt.executeUpdate(sql);
		
		return mo;
	}
	
	/**
	 * Perform the consume operation to a member.
	 * @param staff	
	 * 			the staff to perform this action
	 * @param memberId 
	 * 			the id to member account
	 * @param consumePrice	
	 * 			the price to consume
	 * @param coupon
	 * 			the coupon to use, null means no coupon 
	 * @param payType
	 * 			the payment type referred to {@link Order.PayType}
	 * @param orderId
	 * 			the associated order id to this consumption
	 * @return the member operation to the consumption operation
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements.
	 * @throws BusinessException
	 *	 		throws if one of cases below occurred<br>
	 *			<li>the consume price exceeds total balance to this member account<br>
	 *			<li>the member account to consume is NOT found.
	 */
	public static MemberOperation consume(Staff staff, int memberId, float consumePrice, Coupon coupon, Order.PayType payType, int orderId) throws SQLException, BusinessException{
		
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			MemberOperation mo = MemberDao.consume(dbCon, staff, memberId, consumePrice, coupon, payType, orderId);
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
	 * @param staff
	 * 			the staff to perform this action 
	 * @param memberId
	 * 			the id of member account to be charged.
	 * @param chargeMoney
	 * 			the amount of charge money
	 * @param accountMoney
	 * 			the amount of account money
	 * @param chargeType
	 * 			the charge type referred to {@link Member.ChargeType}
	 * @return the member operation to this charge.
	 * @throws BusinessException
	 * 			throw if the member id to search is NOT found 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 */
	public static MemberOperation charge(DBCon dbCon, Staff staff, int memberId, float chargeMoney, float accountMoney, ChargeType chargeType) throws BusinessException, SQLException{
		
		if(chargeMoney < 0){
			throw new IllegalArgumentException("The amount of charge money(amount = " + chargeMoney + ") must be more than zero");
		}
		
		Member member = getById(dbCon, staff, memberId);
		
		//Perform the charge operation and get the related member operation.
		MemberOperation mo = member.charge(chargeMoney, accountMoney, chargeType);
		
		//Insert the member operation to this charge operation.
		MemberOperationDao.insert(dbCon, staff, mo);
		
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
	 * @param staff
	 * 			the staff to perform this action 
	 * @param memberId
	 * 			the id of member account to be charged.
	 * @param chargeMoney
	 * 			the amount of charge money
	 * @param accountMoney
	 * 			the amount of account money
	 * @param chargeType
	 * 			the charge type referred to {@link Member.ChargeType}
	 * @return the member operation to this charge.
	 * @throws BusinessException
	 * 			throw if the member id to search is NOT found 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 */
	public static MemberOperation charge(Staff staff, int memberId, float chargeMoney, float accountMoney, ChargeType chargeType) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			MemberOperation mo = MemberDao.charge(dbCon, staff, memberId, chargeMoney, accountMoney, chargeType);
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
	 * Perform the refund operation to a specific member account.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param memberId
	 * 			the id to member account
	 * @param refundMoney
	 * 			the refund money
	 * @param accountMoney
	 * 			the account money
	 * @return the member operation to refund
	 * @throws BusinessException
	 * 			throws if insufficient to refund
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static MemberOperation refund(DBCon dbCon, Staff staff, int memberId, float refundMoney, float accountMoney) throws BusinessException, SQLException{
		if(refundMoney < 0){
			throw new IllegalArgumentException("The amount of take money(amount = " + refundMoney + ") must be more than zero");
		}
		Member member = getById(dbCon, staff, memberId);
		
		MemberOperation mo = member.refund(refundMoney, accountMoney);
		
		if(mo.getRemainingBaseMoney() < 0){
			throw new BusinessException("无足够余额取款");
		}
		
		MemberOperationDao.insert(dbCon, staff, mo);
		
		String sql = " UPDATE " + Params.dbName + ".member SET" +
				 " base_balance = " + member.getBaseBalance() + ", " +
				 " extra_balance = " + member.getExtraBalance() + "," + 
				 " total_charge = " + member.getTotalCharge() + 
				 " WHERE member_id = " + memberId;
		dbCon.stmt.executeUpdate(sql);
		
		return mo;
		
	}
	
	/**
	 * Perform the refund operation to a specific member account.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param memberId
	 * 			the id to member account
	 * @param refundMoney
	 * 			the refund money
	 * @param accountMoney
	 * 			the account money
	 * @return the member operation to refund
	 * @throws BusinessException
	 * 			throws if insufficient to refund
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static MemberOperation refund(Staff staff, int memberId, float refundMoney, float accountMoney) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			MemberOperation mo = MemberDao.refund(dbCon, staff, memberId, refundMoney, accountMoney);
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
	 * @param staff
	 * 			the staff to perform this action
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
	public static MemberOperation pointConsume(DBCon dbCon, Staff staff, int memberId, int pointConsume) throws BusinessException, SQLException{
		
		if(pointConsume < 0){
			throw new IllegalArgumentException("The amount of point to consume(amount = " + pointConsume + ") must be more than zero");
		}
		
		Member member = getById(dbCon, staff, memberId);
		
		//Perform the point consumption and get the related member operation.
		MemberOperation mo = member.pointConsume(pointConsume);
				
		//Insert the member operation to this point consumption.
		MemberOperationDao.insert(dbCon, staff, mo);
		
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
	 * @param staff
	 * 			the staff to perform this action
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
	public static MemberOperation pointConsume(Staff staff, int memberId, int pointConsume) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			MemberOperation mo = MemberDao.pointConsume(dbCon, staff, memberId, pointConsume);
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
	 * @param staff
	 * 			the staff to perform this action
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
	public static MemberOperation adjustPoint(Staff staff, int memberId, int deltaPoint, Member.AdjustType adjust) throws SQLException, BusinessException{
		
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			MemberOperation mo = MemberDao.adjustPoint(dbCon, staff, memberId, deltaPoint, adjust);
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
	 * @param staff
	 * 			the staff to perform this action
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
	public static MemberOperation adjustPoint(DBCon dbCon, Staff staff, int memberId, int deltaPoint, Member.AdjustType adjust) throws SQLException, BusinessException{
		
		Member member = getById(dbCon, staff, memberId);
		
		if(adjust == Member.AdjustType.INCREASE){
			deltaPoint = Math.abs(deltaPoint);
		}else if(adjust == Member.AdjustType.REDUCE){
			deltaPoint = -Math.abs(deltaPoint);
		}
		
		//Perform the point adjust and get the related member operation.
		MemberOperation mo = member.adjustPoint(deltaPoint, adjust);
				
		//Insert the member operation to this point consumption.
		MemberOperationDao.insert(dbCon, staff, mo);
		
		//Update the point.
		String sql = " UPDATE " + Params.dbName + ".member SET" +
					 " point = " + member.getPoint() + 
					 " WHERE member_id = " + memberId;
		
		dbCon.stmt.executeUpdate(sql);
		
		return mo;
		
	}
	
	/**
	 * Perform to adjust balance to a specified member. 
	 * @param staff
	 * 			the staff to perform this action
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
	public static MemberOperation adjustBalance(Staff staff, int memberId, float deltaBalance) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			MemberOperation mo = MemberDao.adjustBalance(dbCon, staff, memberId, deltaBalance);
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
	 * @param staff
	 * 			the staff to perform this action
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
	public static MemberOperation adjustBalance(DBCon dbCon, Staff staff, int memberId, float deltaBalance) throws SQLException, BusinessException{
		Member member = getById(dbCon, staff, memberId);
		
		//Perform the point adjust and get the related member operation.
		MemberOperation mo = member.adjustBalance(deltaBalance);
				
		//Insert the member operation to this point consumption.
		MemberOperationDao.insert(dbCon, staff, mo);
		
		//Update the point.
		String sql = " UPDATE " + Params.dbName + ".member SET" +
					 " base_balance = " + member.getBaseBalance() + "," +
					 " extra_balance =  " + member.getExtraBalance() + 
					 " WHERE member_id = " + memberId;
		
		dbCon.stmt.executeUpdate(sql);
		
		return mo;
	}
	
	
	/**
	 * Calculate most favor foods to each member.
	 * @return the result to calculate favor foods
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static CalcFavorResult calcFavorFoods() throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcFavorFoods(dbCon);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate most favor foods to each member.
	 * @param dbCon
	 * 			the database connection
	 * @return the result to calculate favor foods
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static CalcFavorResult calcFavorFoods(DBCon dbCon) throws SQLException{
		
		long beginTime = System.currentTimeMillis();
		
		String sql;
		//Delete all the original member favor foods.
		sql = " DELETE FROM " + Params.dbName + ".member_favor_food";
		dbCon.stmt.executeUpdate(sql);
		
		//Calculate the favor foods to each member.
		sql = " SELECT MOH.member_id " +
			  " FROM " + Params.dbName + ".member_operation_history MOH " +
			  " JOIN " + Params.dbName + ".member M ON M.member_id = MOH.member_id " +
			  " GROUP BY member_id ";
		List<Integer> memberIds = new ArrayList<Integer>();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			memberIds.add(dbCon.rs.getInt("member_id"));
		}
		dbCon.rs.close();
		for(Integer memberId : memberIds){
			calcFavorFoods(dbCon, memberId);
		}
		
		return new CalcFavorResult((int)(System.currentTimeMillis() - beginTime) / 1000);

	}
	
	/**
	 * Calculate the favor foods to a specific food.
	 * @param dbCon
	 * 			the database connection
	 * @param memberId
	 * 			the member to calculate favor foods
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	private static void calcFavorFoods(DBCon dbCon, int memberId) throws SQLException{
		
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".member_favor_food " +
			  " (`member_id`, `food_id`, `point`) " +
			  " SELECT MOH.member_id, OFH.food_id, F.weight * COUNT(*) AS point " +
			  " FROM " + Params.dbName + ".order_food_history OFH " +
			  " JOIN " + Params.dbName + ".member_operation_history MOH " + 
			  " ON OFH.order_id = MOH.order_id " + " AND " + " MOH.member_id = " + memberId +
			  " JOIN " + Params.dbName + ".food F ON OFH.food_id = F.food_id " +
			  " GROUP BY OFH.food_id " +
			  " HAVING point <> 0 " +
			  " ORDER BY point DESC " + 
			  " LIMIT 10 ";
		dbCon.stmt.executeUpdate(sql);
		
		//Get the max point from member favor foods
		sql = " SELECT @max_favor_food_point := MAX(point) FROM " + Params.dbName + ".member_favor_food WHERE member_id = " + memberId;
		dbCon.stmt.execute(sql);
		
		//将喜欢度归一化
		sql = " UPDATE " + Params.dbName + ".member_favor_food SET " +
			  " point = point / @max_favor_food_point " +
			  " WHERE member_id = " + memberId;
		dbCon.stmt.executeUpdate(sql);
		
	}

	/**
	 * Calculate the recommended foods to each member.
	 * @return the result to calculate recommend foods
	 * @throws SQLException	
	 * 			throws if failed to execute any SQL statement
	 */
	public static CalcRecommendResult calcRecommendFoods() throws SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcRecommendFoods(dbCon);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the recommended foods to each member.
	 * @param dbCon
	 * 			the database connection 
	 * @return the result to calculate recommend foods
	 * @throws SQLException	
	 * 			throws if failed to execute any SQL statement
	 */
	public static CalcRecommendResult calcRecommendFoods(DBCon dbCon) throws SQLException{
		
		long beginTime = System.currentTimeMillis();
		
		String sql;
		//Delete all the original member recommended foods.
		sql = " DELETE FROM " + Params.dbName + ".member_recommend_food";
		dbCon.stmt.executeUpdate(sql);
		
		//Calculate the recommended foods to each member.
		sql = " SELECT MOH.member_id " +
			  " FROM " + Params.dbName + ".member_operation_history MOH " +
			  " JOIN " + Params.dbName + ".member M ON MOH.member_id = M.member_id " +
			  " GROUP BY member_id ";
		List<Integer> memberIds = new ArrayList<Integer>();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			memberIds.add(dbCon.rs.getInt("member_id"));
		}
		dbCon.rs.close();
		
		for(int memberId : memberIds){
			calcRecommnedFoods(dbCon, memberId);
		}
		
		return new CalcRecommendResult((int)(System.currentTimeMillis() - beginTime) / 1000);
	}
	
	/**
	 * Calculate the recommended foods to a specific member.
	 * @param dbCon
	 * 			the database connection
	 * @param memberId
	 * 			the member to calculate its recommended foods
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	private static void calcRecommnedFoods(DBCon dbCon, int memberId) throws SQLException{
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".member_recommend_food " +
			  " (`member_id`, `food_id`, `point`) " +
			  " SELECT MAX(MFF.member_id) AS member_id, FA.associated_food_id, " +
			  " SUM(FA.similarity * MFF.point) AS associated_point " +
			  " FROM " + Params.dbName + ".food_association FA " +
			  " JOIN " + Params.dbName + ".member_favor_food MFF ON MFF.food_id = FA.food_id " +
			  " WHERE 1 = 1 " +
			  " AND MFF.member_id = " + memberId +
			  " AND FA.associated_food_id NOT IN (SELECT food_id FROM wireless_order_db.member_favor_food WHERE member_id = " + memberId + ") " +
			  " GROUP BY FA.associated_food_id " +
			  " ORDER BY associated_point DESC " +
			  " LIMIT 10 ";
		dbCon.stmt.executeUpdate(sql);
	} 
	
	/**
	 * Calculate the member rank.
	 * @param staff
	 * 			the staff to perform this action
	 * @param memberId
	 * 			the member id to calculate rank
	 * @return the rank {@link MemberRank}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the member to calculate rank does NOT exist
	 */
	public static MemberRank calcMemberRank(Staff staff, int memberId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return calcMemberRank(dbCon, staff, memberId);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Calculate the member rank.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param memberId
	 * 			the member id to calculate rank
	 * @return the rank {@link MemberRank}
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the member to calculate rank does NOT exist
	 */
	public static MemberRank calcMemberRank(DBCon dbCon, Staff staff, int memberId) throws SQLException, BusinessException{
		
		Member member = getById(dbCon, staff, memberId);
		
		String sql;
		sql = " SELECT COUNT(*) FROM " + Params.dbName + ".member WHERE restaurant_id = " + staff.getRestaurantId();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		int total = 0;
		if(dbCon.rs.next()){
			total = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		
		int rank = 0;
		sql = " SELECT COUNT(*) FROM " + Params.dbName + ".member WHERE restaurant_id = " + staff.getRestaurantId() + " AND total_point >= " + member.getTotalPoint();
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		if(dbCon.rs.next()){
			rank = dbCon.rs.getInt(1);
		}
		dbCon.rs.close();
		
		return new MemberRank(total, rank);
	}
}
