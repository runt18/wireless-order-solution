package com.wireless.db.member;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mysql.jdbc.Statement;
import com.wireless.db.DBCon;
import com.wireless.db.Params;
import com.wireless.db.member.represent.RepresentChainDao;
import com.wireless.db.member.represent.RepresentDao;
import com.wireless.db.promotion.CouponDao;
import com.wireless.db.promotion.PromotionDao;
import com.wireless.db.restaurantMgr.RestaurantDao;
import com.wireless.db.staffMgr.StaffDao;
import com.wireless.db.weixin.member.WxMemberDao;
import com.wireless.exception.BusinessException;
import com.wireless.exception.MemberError;
import com.wireless.exception.ModuleError;
import com.wireless.exception.StaffError;
import com.wireless.exception.WxMemberError;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pack.req.ReqQueryMember;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;
import com.wireless.pojo.billStatistics.DateRange;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.PayType;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.member.MemberCond;
import com.wireless.pojo.member.MemberLevel;
import com.wireless.pojo.member.MemberOperation;
import com.wireless.pojo.member.MemberOperation.ChargeType;
import com.wireless.pojo.member.MemberType;
import com.wireless.pojo.member.WxMember;
import com.wireless.pojo.member.represent.Represent;
import com.wireless.pojo.member.represent.RepresentChain;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.promotion.Coupon;
import com.wireless.pojo.promotion.Promotion;
import com.wireless.pojo.restaurantMgr.Module;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.staffMgr.Privilege;
import com.wireless.pojo.staffMgr.Staff;
import com.wireless.pojo.util.DateType;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.util.SortedList;
import com.wireless.sms.msg.Msg4Upgrade;
import com.wireless.util.SQLUtil;

public class MemberDao {
	
	public static class IdleExtraCond extends ExtraCond implements Jsonable{
		private final static IdleExtraCond mInstance = new IdleExtraCond();
		
		public static IdleExtraCond instance(){
			return mInstance;
		}
		
		private IdleExtraCond(){
			//活跃会员的条件：3个月内消费不足3次
			Calendar c = Calendar.getInstance();
			c.add(Calendar.MONTH, -3);
			super.range = new DutyRange(c.getTime().getTime(), System.currentTimeMillis());
			
			super.maxConsumeAmount = 3;
		}

		@Override
		public JsonMap toJsonMap(int flag) {
			JsonMap jm = new JsonMap();
			jm.putInt("maxConsumeAmount", super.maxConsumeAmount);
			jm.putString("beginDate", super.range.getOnDutyFormat());
			jm.putString("endDate", super.range.getOffDutyFormat());
			jm.putInt("range", 4);
			return jm;
		}

		@Override
		public void fromJsonMap(JsonMap jsonMap, int flag) {
			
		}
	}
	
	public static class ActiveExtraCond extends ExtraCond implements Jsonable{
		private final static ActiveExtraCond mInstance = new ActiveExtraCond();
		
		public static ActiveExtraCond instance(){
			return mInstance;
		}
		
		private ActiveExtraCond(){
			//活跃会员的条件：3个月内消费5次
			Calendar c = Calendar.getInstance();
			c.add(Calendar.MONTH, -3);
			super.range = new DutyRange(c.getTime().getTime(), System.currentTimeMillis());
			
			super.minConsumeAmount = 5;

		}

		@Override
		public JsonMap toJsonMap(int flag) {
			JsonMap jm = new JsonMap();
			jm.putInt("minConsumeAmount", super.minConsumeAmount);
			jm.putString("beginDate", super.range.getOnDutyFormat());
			jm.putString("endDate", super.range.getOffDutyFormat());
			jm.putInt("range", 4);
			return jm;
		}

		@Override
		public void fromJsonMap(JsonMap jsonMap, int flag) {
			
		}
	}
	
	public static class ExtraCond implements Parcelable{
		private int id;
		private int branchId;
		private String card;
		private String mobile;
		private String name;
		private String fuzzy;
		private int memberTypeId;
		private int minConsumeAmount;
		private int maxConsumeAmount;
		private float minTotalConsume;
		private float maxTotalConsume;
		private float minBalance;
		private float maxBalance;
		private float minTotalCharge;
		private float maxTotalCharge;
		private DutyRange range;
		private DutyRange createRange;
		private String weixinCard;
		private String weixinSerial;
		private int referrerId;
		private int restaurantId;
		private DateRange birthdayRange;
		private int minLastConsumption;
		private int maxLastConsumption;
		private Member.Sex sex;
		private final List<Member.Age> ages = new ArrayList<Member.Age>();
		private Boolean isRaw;
		private int minFansAmount;
		private int maxFansAmount;
		private float minCommissionAmount;
		private float maxCommissionAmount;
		
		public ExtraCond(MemberCond memberCond){
			setRange(memberCond.getRange());
			if(memberCond.getMemberType() != null){
				setMemberType(memberCond.getMemberType());
			}
			this.minBalance = memberCond.getMinBalance();
			this.maxBalance = memberCond.getMaxBalance();
			this.minConsumeAmount = memberCond.getMinConsumeAmount();
			this.maxConsumeAmount = memberCond.getMaxConsumeAmount();
			this.minTotalConsume = memberCond.getMinConsumeMoney();
			this.maxTotalConsume = memberCond.getMaxConsumeMoney();
			this.minLastConsumption = memberCond.getMinLastConsumption();
			if(memberCond.hasSex()){
				this.sex = memberCond.getSex();
			}
			this.minTotalCharge = memberCond.getMinCharge();
			this.maxTotalCharge = memberCond.getMaxCharge();
			this.isRaw = memberCond.isRaw();
			this.ages.addAll(memberCond.getAges());
			this.minFansAmount = memberCond.getMinFansAmount();
			this.maxFansAmount = memberCond.getMaxFansAmount();
			this.minCommissionAmount = memberCond.getMinCommissionAmount();
			this.maxCommissionAmount = memberCond.getMaxCommissionAmount();
		}
		
		public ExtraCond(ReqQueryMember.ExtraCond extraCond){
			this.setId(extraCond.getId()).setFuzzyName(extraCond.getFuzzyName());
		}
		
		public ExtraCond(){
			
		}
		
		private ExtraCond setRestaurantId(int restaurantId){
			this.restaurantId = restaurantId;
			return this;
		}
		
		public ExtraCond setBranch(int branchId){
			this.branchId = branchId;
			return this;
		}
		
		public ExtraCond setBranch(Restaurant branch){
			this.branchId = branch.getId();
			return this;
		}
		
		public ExtraCond setSex(Member.Sex sex){
			this.sex = sex;
			return this;
		}
		
		public ExtraCond addAge(Member.Age age){
			this.ages.add(age);
			return this;
		}
		
		public ExtraCond setRaw(boolean onOff){
			this.isRaw = onOff;
			return this;
		}
		
		public ExtraCond setBirthday(String begin, String end) throws ParseException{
			this.birthdayRange = new DateRange(begin, end);
			return this;
		}
		
		public ExtraCond setReferrer(int referrerId){
			this.referrerId = referrerId;
			return this;
		}
		
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
		
		public ExtraCond setWeixinSerial(String weixinSerial){
			this.weixinSerial = weixinSerial;
			return this;
		}
		
		public ExtraCond setWeixinCard(String weixinCard){
			this.weixinCard = weixinCard;
			return this;
		}
		
		public ExtraCond setWeixinCard(int weixinCard){
			this.weixinCard = Integer.toString(weixinCard);
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
		
		public ExtraCond setMemberType(MemberType memberType){
			this.memberTypeId = memberType.getId();
			return this;
		}
		
		public ExtraCond greaterCharge(float min){
			if(min < 0){
				throw new IllegalArgumentException("最小充值额不能小于0");
			}
			this.minTotalCharge = min;
			this.maxTotalCharge = 0;
			return this;
		}
		
		public ExtraCond lessCharge(float max){
			if(max < 0){
				throw new IllegalArgumentException("最大充值额不能小于0");
			}
			this.minTotalCharge = 0;
			this.maxTotalCharge = max;
			return this;
		}
		
		public ExtraCond greaterConsume(int amount){
			if(amount < 0){
				throw new IllegalArgumentException("消费次数小于0");
			}
			this.minConsumeAmount = amount;
			this.maxConsumeAmount = 0;
			return this;
		}
		
		public ExtraCond lessConsume(int amount){
			if(amount < 0){
				throw new IllegalArgumentException("消费次数小于0");
			}
			this.maxConsumeAmount = amount;
			this.minConsumeAmount = 0;
			return this;
		}
		
		public ExtraCond betweenConsume(int min, int max){
			if(min < 0 || max < 0){
				throw new IllegalArgumentException("消费次数小于0");
			}
			if(min > max){
				throw new IllegalArgumentException("最大消费次数不能小于最小消费次数");
			}
			this.minConsumeAmount = min;
			this.maxConsumeAmount = max;
			return this;
		}
		
		public ExtraCond greaterTotalConsume(float min){
			if(min < 0){
				throw new IllegalArgumentException("消费总额小于0");
			}
			this.minTotalConsume = min;
			this.maxTotalConsume = 0;
			return this;
		}
		
		public ExtraCond lessTotalConsume(float max){
			if(max < 0){
				throw new IllegalArgumentException("消费总额小于0");
			}
			this.minTotalConsume = 0;
			this.maxTotalConsume = max;
			return this;
		}
		
		public ExtraCond betweenTotalConsume(float min, float max){
			if(min < 0 || max < 0){
				throw new IllegalArgumentException("消费总额小于0");
			}
			if(min > max){
				throw new IllegalArgumentException("最大消费总额不能小于最小消费总额");
			}
			this.minTotalConsume = min;
			this.maxTotalConsume = max;
			return this;
		}
		
		public ExtraCond greaterBalance(float min){
			if(min < 0){
				throw new IllegalArgumentException("余额不能小于0");
			}
			this.minBalance = min;
			this.maxBalance = 0;
			return this;
		}
		
		public ExtraCond lessBalance(float max){
			if(max < 0){
				throw new IllegalArgumentException("余额不能小于0");
			}
			this.minBalance = 0;
			this.maxBalance = max;
			return this;
		}
		
		public ExtraCond betweenBalance(float min, float max){
			if(min < 0 || max < 0){
				throw new IllegalArgumentException("余额不能小于0");
			}
			if(min > max){
				throw new IllegalArgumentException("最大余额不能小于最小余额");
			}
			this.minBalance = min;
			this.maxBalance = max;
			return this;
		}
		
		public ExtraCond setCreateRange(DutyRange range){
			this.createRange = range;
			return this;
		}
		
		public ExtraCond setRange(DutyRange range){
			this.range = range;
			return this;
		}
		
		public ExtraCond setFansRange(int min, int max){
			this.minFansAmount = min;
			this.maxFansAmount = max;
			return this;
		}
		
		public ExtraCond setCommissionRange(float min, float max){
			this.minCommissionAmount = min;
			this.maxCommissionAmount = max;
			return this;
		}
		
		private String cond4Card(String card){
			if(card != null){
				String cardExcludeZero = card.replaceFirst("^(0+)", "");
				return (" M.member_card_crc = CRC32('" + cardExcludeZero + "') AND M.member_card = '" + cardExcludeZero + "'");
			}else{
				return "";
			}
		}
		
		private String cond4WeixinCard(String weixinCard, boolean defVal){
			try{
				return weixinCard != null ? (" WM.weixin_card = " + Integer.parseInt(weixinCard)) : "";
			}catch(NumberFormatException ignored){
				return defVal ? " 1 = 1 " : " 0 = 1 ";
			}
		}
		
		private String cond4Mobile(String mobile){
			return mobile != null ? (" M.mobile_crc = CRC32('" + mobile + "') AND M.mobile = '" + mobile + "'") : "";
		}
		
		@Override
		public String toString(){
			StringBuilder extraCond = new StringBuilder();
			if(fuzzy != null){
				extraCond.append(" AND (M.name LIKE '%" + fuzzy + "%' OR (" + cond4Card(fuzzy) + ") OR (" + cond4WeixinCard(fuzzy, false) + ") OR (" + cond4Mobile(fuzzy) + "))");
			}
			if(id > 0){
				extraCond.append(" AND M.member_id = " + id);
			}
			if(card != null){
				extraCond.append(" AND " + cond4Card(card));
			}
			if(mobile != null){
				extraCond.append(" AND " + cond4Mobile(mobile));
			}
			if(weixinCard != null){
				extraCond.append(" AND " + cond4WeixinCard(weixinCard, true));
			}
			if(weixinSerial != null){
				extraCond.append(" AND WM.weixin_serial = '" + weixinSerial + "' AND weixin_serial_crc = CRC32('" + weixinSerial + "')");
			}
			
			if(name != null){
				extraCond.append(" AND M.name = '" + name + "'");
			}
			if(memberTypeId > 0){
				extraCond.append(" AND MT.member_type_id = " + memberTypeId);
			}
			
			//会员余额
			if(minBalance > 0 && maxBalance == 0){
				extraCond.append(" AND (M.base_balance + M.extra_balance) >= " + minBalance);
			}else if(maxBalance > 0 && minBalance == 0){
				extraCond.append(" AND (M.base_balance + M.extra_balance)  <= " + maxBalance);
			}else if(maxBalance > 0 && minBalance > 0){
				extraCond.append(" AND (M.base_balance + M.extra_balance) BETWEEN " + minBalance + " AND " + maxBalance);
			}

			//会员充值额
			if(minTotalCharge > 0 && maxTotalCharge == 0){
				extraCond.append(" AND M.total_charge >= " + minTotalCharge);
			}else if(maxTotalCharge > 0 && minTotalCharge == 0){
				extraCond.append(" AND M.total_charge <= " + maxTotalCharge);
			}else if(maxTotalCharge > 0 && minTotalCharge > 0){
				extraCond.append(" AND M.total_charge BETWEEN " + minTotalCharge + " AND " + maxTotalCharge);
			}
			
			if(birthdayRange != null){
				extraCond.append(" AND M.birthday BETWEEN '" + birthdayRange.getOpeningFormat() + "' AND '" + birthdayRange.getEndingFormat() + "'");
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
				if(havingCond.length() > 0){
					String sql;
					sql = " SELECT member_id FROM " + Params.dbName + ".member_operation_history " +
						  " WHERE 1 = 1 " +
						  " AND restaurant_id = " + restaurantId +
						  " AND operate_type = " + MemberOperation.OperationType.CONSUME.getValue() + 
						  " AND operate_date BETWEEN '" + range.getOnDutyFormat() + "' AND '" + range.getOffDutyFormat() + "'" +
						  " GROUP BY member_id " +
						  " HAVING 1 = 1 " +
						  havingCond.toString();
					
					extraCond.append(" AND M.member_id IN ( " + sql + ")");
				}
				
			}else{
				if(minConsumeAmount > 0 && maxConsumeAmount == 0){
					extraCond.append(" AND M.consumption_amount >= " + minConsumeAmount);
				}else if(maxConsumeAmount > 0 && minConsumeAmount == 0){
					extraCond.append(" AND M.consumption_amount <= " + maxConsumeAmount);
				}else if(maxConsumeAmount > 0 && minConsumeAmount > 0){
					extraCond.append(" AND M.consumption_amount BETWEEN " + minConsumeAmount + " AND " + maxConsumeAmount);
				}
				
				if(minTotalConsume > 0 && maxTotalConsume == 0){
					extraCond.append(" AND M.total_consumption >= " + minTotalConsume);
				}else if(maxTotalConsume > 0 && minTotalConsume == 0){
					extraCond.append(" AND M.total_consumption <= " + maxTotalConsume);
				}else if(maxTotalConsume > 0 && minTotalConsume > 0){
					extraCond.append(" AND M.total_consumption BETWEEN " + minTotalConsume + " AND " + maxTotalConsume);
				}
				
			}
			
			//会员创建时间
			if(this.createRange != null){
				extraCond.append(" AND M.create_date BETWEEN '" + createRange.getOnDutyFormat() + "' AND '" + createRange.getOffDutyFormat() + "'");
			}
			
			//推荐人
			if(this.referrerId != 0){
				extraCond.append(" AND M.referrer_id = " + referrerId);
			}
			
			//最近消费日期
			if(this.minLastConsumption > 0 && this.maxLastConsumption == 0){
				extraCond.append(" AND DATEDIFF(NOW(), M.last_consumption) >= " + this.minLastConsumption);
			}else if(this.maxLastConsumption > 0 && this.minLastConsumption == 0){
				extraCond.append(" AND DATEDIFF(NOW(), M.last_consumption) <= " + this.maxLastConsumption);
			}else if(this.minLastConsumption > 0 && this.maxLastConsumption > 0){
				extraCond.append(" AND DATEDIFF(NOW(), M.last_consumption) BETWEEN " + this.minLastConsumption + " AND " + this.maxLastConsumption);
			}
			
			//性别
			if(this.sex != null){
				extraCond.append(" AND M.sex = " + this.sex.getVal());
			}

			//年龄段
			final StringBuilder ageCond = new StringBuilder();
			for(Member.Age age : this.ages){
				if(ageCond.length() != 0){
					ageCond.append(",");
				}
				ageCond.append(age.getVal());
			}
			if(ageCond.length() > 0){
				extraCond.append(" AND M.age IN (" + ageCond + ")");
			}
			
			//是否Raw
			if(isRaw != null && isRaw){
				extraCond.append(" AND LENGTH(TRIM(M.mobile)) > 0 AND LENGTH(TRIM(M.member_card)) > 0");
			}
			
			//门店
			if(branchId != 0){
				extraCond.append(" AND M.branch_id = " + branchId);
			}
			
			//设置粉丝数
			if(maxFansAmount > 0 || minFansAmount > 0){
				String sql;
				if(maxFansAmount > 0){
					sql = " SELECT member_id FROM " + Params.dbName + ".member" + 
						  " WHERE restaurant_id = " + restaurantId + " AND member_id " +
						  " NOT IN ( " + 
						  		" SELECT recommend_member_id " + 
						  		" FROM " + Params.dbName + ".represent_chain GROUP BY recommend_member_id " + " HAVING COUNT(*) > " + maxFansAmount +
						  " ) ";
					extraCond.append(" AND M.member_id IN ( " + sql + " ) ");	
				}
				if(minFansAmount > 0){
					sql = " SELECT recommend_member_id FROM " + Params.dbName + ".represent_chain" +
						  " WHERE restaurant_id = " + restaurantId + " GROUP BY recommend_member_id " + 
						  " HAVING COUNT(*) >= " + minFansAmount;
					extraCond.append(" AND M.member_id IN (" + sql +")");
				}
				
			}
			
			//设置佣金总额
			if(minCommissionAmount > 0 || maxCommissionAmount > 0){
				if(minCommissionAmount > 0 && maxCommissionAmount > 0){
					extraCond.append(" AND M.total_commission BETWEEN " + minCommissionAmount + " AND " + maxCommissionAmount);
				}else if (minCommissionAmount > 0 && maxCommissionAmount == 0){
					extraCond.append(" AND M.total_commission >= " + minCommissionAmount);
				}else if(minCommissionAmount == 0 && maxCommissionAmount > 0){
					extraCond.append(" AND M.total_commission <= " + maxCommissionAmount);
				}
			}
			
			return extraCond.toString();
		}

		@Override
		public void writeToParcel(Parcel dest, int flag) {
			dest.writeInt(this.id);
			dest.writeString(this.card);
			dest.writeString(this.mobile);
			dest.writeString(this.name);
			dest.writeString(this.fuzzy);
			dest.writeInt(this.memberTypeId);
		}

		@Override
		public void createFromParcel(Parcel source) {
			this.id = source.readInt();
			this.card = source.readString();
			this.mobile = source.readString();
			this.name = source.readString();
			this.fuzzy = source.readString();
			this.memberTypeId = source.readInt();
		}
		
		public final static Parcelable.Creator<ExtraCond> CREATOR = new Parcelable.Creator<ExtraCond>() {
			
			public ExtraCond[] newInstance(int size) {
				return new ExtraCond[size];
			}
			
			public ExtraCond newInstance() {
				return new ExtraCond();
			}
		};
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
			eachMember.getMemberType().copyFrom(MemberTypeDao.getById(dbCon, staff, eachMember.getMemberType().getId()));
			
			//Get the discounts to each member
//			sql = " SELECT MD.discount_id, MD.type, D.name, D.type AS d_type FROM " + Params.dbName + ".member_type_discount MD " +
//					" JOIN " + Params.dbName + ".discount D " +
//					" ON MD.discount_id = D.discount_id " +
//					" WHERE member_type_id = " + eachMember.getMemberType().getId();
//			dbCon.rs = dbCon.stmt.executeQuery(sql);
//			while(dbCon.rs.next()){
//				Discount discount = new Discount(dbCon.rs.getInt("MD.discount_id"));
//				discount.setName(dbCon.rs.getString("D.name"));
//				discount.setType(Type.valueOf(dbCon.rs.getInt("d_type")));
//				eachMember.getMemberType().addDiscount(discount);
//				if(MemberType.DiscountType.valueOf(dbCon.rs.getInt("type")) == MemberType.DiscountType.DEFAULT){
//					eachMember.getMemberType().setDefaultDiscount(discount);
//				}
//			}
//			dbCon.rs.close();
			
			//Get the fans amount to each member.
			eachMember.setFansAmount(RepresentChainDao.getByCond(dbCon, staff, new RepresentChainDao.ExtraCond().setReferrerId(eachMember.getId()).setOnlyAmount(true)).size());
			
			//Get the favor foods to each member
			sql = " SELECT " +
				  " F.food_id, F.food_alias, F.name, F.restaurant_id " +
				  " FROM " + Params.dbName + ".member_favor_food MFF " +
				  " JOIN " + Params.dbName + ".food F ON MFF.food_id = F.food_id " +
				  " WHERE MFF.member_id = " + eachMember.getId() +
				  " AND F.restaurant_id = " + staff.getRestaurantId() +
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
				  " AND F.restaurant_id = " + staff.getRestaurantId() +
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
	 */
	private static List<Member> getByCond(DBCon dbCon, Staff staff, String extraCond, String orderClause) throws SQLException{
//		if(!staff.getRole().hasPrivilege(Privilege.Code.MEMBER_CHECK)){
//			throw new BusinessException(StaffError.MEMBER_CHECK_NOT_ALLOW);
//		}
		final List<Member> result = new ArrayList<Member>();
		String sql;
		sql = " SELECT "	+
			  " R.restaurant_name, M.member_id, M.restaurant_id, M.branch_id, M.point, M.used_point, " +
			  " M.base_balance, M.extra_balance, M.consumption_amount, M.last_consumption, M.used_balance," +
			  " M.total_consumption, M.total_point, M.total_charge, M.total_refund, " +
			  " M.member_card, M.name AS member_name, M.sex, M.create_date, " +
			  " M.tele, M.mobile, M.age, M.birthday, M.id_card, M.company, M.contact_addr, M.comment, " +
			  " M.referrer, M.referrer_id, " +
			  " M.wx_order_amount, " +
			  " M.total_commission, " +
			  " MT.member_type_id, MT.name AS member_type_name, MT.attribute, MT.exchange_rate, MT.charge_rate, MT.type, MT.initial_point, " +
			  " WM.weixin_card " +
			  " FROM " + Params.dbName + ".member M " +
			  " JOIN " + Params.dbName + ".member_type MT ON M.member_type_id = MT.member_type_id " +
			  " LEFT JOIN " + Params.dbName + ".weixin_member WM ON WM.member_id = M.member_id " +
			  " LEFT JOIN " + Params.dbName + ".restaurant R ON R.id = M.restaurant_id " +
			  " WHERE 1 = 1 " +
			  " AND M.restaurant_id = " + (staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId()) +
			  (extraCond != null ? extraCond : " ") +
			  (orderClause != null ? orderClause : "");
		
		dbCon.rs = dbCon.stmt.executeQuery(sql);
		while(dbCon.rs.next()){
			Member member = new Member(dbCon.rs.getInt("member_id"));
			member.setRestaurantName(dbCon.rs.getString("restaurant_name"));
			member.setRestaurantId(dbCon.rs.getInt("restaurant_id"));
			member.setBranchId(dbCon.rs.getInt("branch_id"));
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
			member.setTotalRefund(dbCon.rs.getFloat("total_refund"));
			member.setMemberCard(dbCon.rs.getString("member_card"));
			member.setName(dbCon.rs.getString("member_name"));
			member.setSex(dbCon.rs.getInt("sex"));
			member.setCreateDate(dbCon.rs.getTimestamp("create_date").getTime());
			member.setTele(dbCon.rs.getString("tele"));
			member.setMobile(dbCon.rs.getString("mobile"));
			member.setAge(Member.Age.valueOf(dbCon.rs.getInt("age")));
			member.setTotalCommission(dbCon.rs.getFloat("total_commission"));
			ts = dbCon.rs.getTimestamp("birthday");
			if(ts != null){
				member.setBirthday(ts.getTime());
			}
			member.setIdCard(dbCon.rs.getString("id_card"));
			member.setCompany(dbCon.rs.getString("company"));
			member.setContactAddress(dbCon.rs.getString("contact_addr"));
			member.setReferrer(dbCon.rs.getString("referrer"));
			member.setReferrerId(dbCon.rs.getInt("referrer_id"));
			member.setWxOrderAmount(dbCon.rs.getInt("wx_order_amount"));
			
			MemberType memberType = new MemberType(dbCon.rs.getInt("member_type_id"));
			memberType.setName(dbCon.rs.getString("member_type_name"));
			memberType.setAttribute(dbCon.rs.getInt("attribute"));
			memberType.setExchangeRate(dbCon.rs.getFloat("exchange_rate"));
			memberType.setChargeRate(dbCon.rs.getFloat("charge_rate"));
			memberType.setType(dbCon.rs.getInt("type"));
			memberType.setInitialPoint(dbCon.rs.getInt("initial_point"));
			member.setMemberType(memberType);
			
			if(dbCon.rs.getInt("weixin_card") != 0){
				member.setWeixin(new WxMember(dbCon.rs.getInt("weixin_card")));
			}
			
			result.add(member);
		}
		dbCon.rs.close();
		
		return result;
	}
	
	/**
	 * Get member by extra condition
	 * @param DBCon
	 * 			the database connection
	 * @param extraCond
	 * 			the extra condition {@link ExtraCond}
	 * @param orderClause
	 * 			the order clause 
	 * @return the list holding the result
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the staff does NOT contains the member check privilege
	 */
	public static List<Member> getByCond(DBCon dbCon, Staff staff, ExtraCond extraCond, String orderClause) throws SQLException, BusinessException{
		if(extraCond != null){
			return MemberDao.getByCond(dbCon, staff, extraCond.setRestaurantId(staff.getRestaurantId()).toString(), orderClause);
		}else{
			return MemberDao.getByCond(dbCon, staff, "", orderClause);
		}
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
	 * @throws BusinessException 
	 * 			throws if the staff does NOT contains the member check privilege
	 */
	public static List<Member> getByCond(Staff staff, ExtraCond extraCond, String orderClause) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return MemberDao.getByCond(dbCon, staff, extraCond, orderClause);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the member according to weixin card.
	 * @param staff
	 * 			the staff to perform this action
	 * @param weixinCard
	 * 			the member weixin card to search
	 * @return the member associated with this id
	 * @throws BusinessException
	 * 			throws if the member to this weixin card is NOT found
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static Member getByWxCard(Staff staff, String weixinCard) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByWxCard(dbCon, staff, weixinCard);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the member according to weixin card.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param weixinCard
	 * 			the member weixin card to search
	 * @return the member associated with this id
	 * @throws BusinessException
	 * 			throws if the member to this weixin card is NOT found
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static Member getByWxCard(DBCon dbCon, Staff staff, String weixinCard) throws SQLException, BusinessException{
		List<Member> result = getByCond(dbCon, staff, new ExtraCond().setWeixinCard(weixinCard), null);
		if(result.isEmpty()){
			throw new BusinessException(MemberError.MEMBER_NOT_EXIST);
		}else{
			return fill(dbCon, staff, result.get(0));
		}
	}
	
	/**
	 * Get the member according to weixin serial.
	 * @param staff
	 * 			the staff to perform this action
	 * @param weixinSerial
	 * 			the member serial to search
	 * @return the member associated with this id
	 * @throws BusinessException
	 * 			throws if the member to this weixin serial is NOT found
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static Member getByWxSerial(Staff staff, String weixinSerial) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return getByWxSerial(dbCon, staff, weixinSerial);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Get the member according to weixin serial.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param weixinSerial
	 * 			the member serial to search
	 * @return the member associated with this id
	 * @throws BusinessException
	 * 			throws if the member to this weixin serial is NOT found
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static Member getByWxSerial(DBCon dbCon, Staff staff, String weixinSerial) throws SQLException, BusinessException{
		List<Member> result = getDetail(dbCon, staff, new ExtraCond().setWeixinSerial(weixinSerial), null);
		if(result.isEmpty()){
			throw new BusinessException(MemberError.MEMBER_NOT_EXIST);
		}else{
			return fill(dbCon, staff, result.get(0));
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
		List<Member> result = MemberDao.getDetail(dbCon, staff, new ExtraCond().setId(memberId), null);
		if(result.isEmpty()){
			throw new BusinessException(MemberError.MEMBER_NOT_EXIST);
		}else{
			return fill(dbCon, staff, result.get(0));
		}
	}
	
	private static Member fill(DBCon dbCon, Staff staff, Member member) throws SQLException, BusinessException{
		if(member.hasWeixin()){
			member.setWeixin(WxMemberDao.getByCard(dbCon, staff, member.getWeixin().getCard()));
		}
		return member;
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
		List<Member> result = MemberDao.getDetail(dbCon, staff, new ExtraCond().setCard(cardAlias), null);
		if(result.isEmpty()){
			throw new BusinessException(MemberError.MEMBER_NOT_EXIST);
		}else{
			return fill(dbCon, staff, result.get(0));
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
			return fill(dbCon, staff, result.get(0));
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
	 * 			throws if cases below
	 * 			<li>the mobile to new member has been exist before
	 * 			<li>the card to new member has been exist before
	 * 			<li>the member type does NOT exist
	 * 			<li>the staff does NOT have the member add privilege 
	 */
	public static int insert(DBCon dbCon, Staff staff, Member.InsertBuilder builder) throws SQLException, BusinessException{
		if(!staff.getRole().hasPrivilege(Privilege.Code.MEMBER_ADD)){
			throw new BusinessException(StaffError.MEMBER_ADD_NOT_ALLOW);
		}
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
		//Check to see whether the mobile is duplicated.
		if(member.hasMobile() && !getByCond(dbCon, staff, new ExtraCond().setMobile(member.getMobile()), null).isEmpty()){
			throw new BusinessException(MemberError.MOBLIE_DUPLICATED);
		}
		//Check to see whether the member card is duplicated.
		if(member.hasMemberCard() && !getByCond(dbCon, staff, new ExtraCond().setCard(member.getMemberCard()), null).isEmpty()){
			throw new BusinessException(MemberError.MEMBER_CARD_DUPLICATED);
		}
		
		//设置推荐人
		if(member.hasReferrer()){
			member.setReferrer(StaffDao.getById(dbCon, member.getReferrerId()).getName());
		}
		
		String sql;
		sql = " INSERT INTO " + Params.dbName + ".member " +
			  " (member_type_id, member_card, member_card_crc, restaurant_id, branch_id, name, sex, tele, mobile, mobile_crc, age, birthday, " +
			  " id_card, company, contact_addr, create_date, referrer, referrer_id, point)" +
			  " VALUES( " +
			  member.getMemberType().getId() + "," + 
			  "'" + member.getMemberCard() + "'," +
			  " CRC32('" + member.getMemberCard() + "')," +
			  (staff.isBranch() ? staff.getGroupId() : staff.getRestaurantId()) + "," +
			  staff.getRestaurantId() + "," +
			  "'" + member.getName() + "'," + 
			  member.getSex().getVal() + "," + 
			  "'" + member.getTele() + "'," + 
			  "'" + member.getMobile() + "'," +
			  " CRC32('" + member.getMobile() + "')," +
			  (member.getAge() != null ? member.getAge().getVal() : " NULL ") + "," +
			  (member.getBirthday() != 0 ? ("'" + DateUtil.format(member.getBirthday()) + "'") : "NULL") + "," +
			  "'" + member.getIdCard() + "'," + 
			  "'" + member.getCompany()+ "'," +
			  "'" + member.getContactAddress() + "'," +
			  " NOW(), " +
			  (member.hasReferrer() ? "'" + member.getReferrer() + "'" : "NULL") + "," +
			  (member.hasReferrer() ? member.getReferrerId() : "NULL") + "," +
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
	 * 			throws if cases below
	 * 			<li>the mobile to new member has been exist before
	 * 			<li>the card to new member has been exist before
	 * 			<li>the member type does NOT exist
	 * 			<li>the staff does NOT have the member add privilege 
	 * 			<li>throws if the staff does NOT contains the member update privilege
	 **/
	public static int insert(Staff staff, Member.InsertBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int memberId = MemberDao.insert(dbCon, staff, builder);
			dbCon.conn.commit();
			return memberId;
			
		}catch(SQLException | BusinessException e){
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
	 * 			<li>throws if the staff does NOT contains the member update privilege
	 */
	public static void update(DBCon dbCon, Staff staff, Member.UpdateBuilder builder) throws SQLException, BusinessException{
		if(!staff.getRole().hasPrivilege(Privilege.Code.MEMBER_MODIFY)){
			throw new BusinessException(StaffError.MEMBER_UPDATE_NOT_ALLOW);
		}
		Member member = builder.build();
		// 旧会员类型是充值属性, 修改为优惠属性时, 检查是否还有余额, 有则不允许修改
		if(builder.isMemberTypeChanged()){
			MemberType newType = MemberTypeDao.getById(dbCon, staff, member.getMemberType().getId());
			Member oriMember = MemberDao.getById(staff, member.getId());
			if(newType.isPoint() && oriMember.getMemberType().isCharge() && oriMember.getTotalBalance() > 0){
				throw new BusinessException("该会员还有余额, 不允许设为优惠属性的类型会员", MemberError.UPDATE_FAIL);
			}
		}
		
		//Check to see whether the mobile or member card is duplicated.
		if(builder.isMobileChanged() && member.hasMobile()){
			if(!getByCond(dbCon, staff, new ExtraCond().setMobile(member.getMobile()) + " AND M.member_id <> " + member.getId(), null).isEmpty()){
				throw new BusinessException(MemberError.MOBLIE_DUPLICATED);
			}
		}
		if(builder.isMemberCardChanged() && member.hasMemberCard()){
			if(member.hasMemberCard() && !getByCond(dbCon, staff, new ExtraCond().setCard(member.getMemberCard()) + " AND M.member_id <> " + member.getId(), null).isEmpty()){
				throw new BusinessException(MemberError.MEMBER_CARD_DUPLICATED);
			}
		}
		
		if(builder.isReferrerChanged()){
			Staff referrer = StaffDao.getById(dbCon, member.getReferrerId());
			member.setReferrer(referrer.getName());
			member.setReferrerId(referrer.getId());
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
			  (builder.isAgeChanged() ? " ,age = " + member.getAge().getVal() : "") +
			  (builder.isBirthdayChanged() ? " ,birthday = '" + DateUtil.format(member.getBirthday()) + "'" : "") +
			  (builder.isCompanyChanged() ? " ,company = '" + member.getCompany() + "'" : "") +
			  (builder.isContactAddrChanged() ? " ,contact_addr = '" + member.getContactAddress() + "'" : "") +
			  (builder.isReferrerChanged() ? " ,referrer = '" + member.getReferrer() + "'" : "") +
			  (builder.isReferrerChanged() ? " ,referrer_id = " + member.getReferrerId() : "") +
			  (builder.isWxOrderAmountChanged() ? " ,wx_order_amount = " + member.getWxOrderAmount() : "") +
			  " WHERE member_id = " + member.getId(); 
		
		if(dbCon.stmt.executeUpdate(sql) == 0){
			throw new BusinessException(MemberError.MEMBER_NOT_EXIST);
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
			
		}catch(SQLException | BusinessException e){
			dbCon.conn.rollback();
			throw e;
			
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the member according to extra condition{@link ExtraCond}. 
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the amount to member deleted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below
	 * 			<li>the member type associated with member does NOT exist
	 * 			<li>the staff does NOT contain the member remove privilege
	 */
	public static int deleteByCond(Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int amount = deleteByCond(dbCon, staff, extraCond);
			dbCon.conn.commit();
			return amount;
		}catch(BusinessException | SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Delete the member according to extra condition{@link ExtraCond}. 
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param extraCond
	 * 			the extra condition
	 * @return the amount to member deleted
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below
	 * 			<li>the member type associated with member does NOT exist
	 * 			<li>the staff does NOT contain the member remove privilege
	 */
	public static int deleteByCond(DBCon dbCon, Staff staff, ExtraCond extraCond) throws SQLException, BusinessException{
		if(!staff.getRole().hasPrivilege(Privilege.Code.MEMBER_REMOVE)){
			throw new BusinessException(StaffError.MEMBER_REMOVE_NOT_ALLOW);
		}
		int amount = 0;
		for(Member member : getByCond(dbCon, staff, extraCond, null)){
			if(member.getTotalBalance() > 0){
				throw new BusinessException("会员余额不为0, 不能删除", StaffError.MEMBER_REMOVE_NOT_ALLOW);
			}
			String sql;
			//Delete the coupon associated with this member
			CouponDao.deleteByCond(dbCon, staff, new CouponDao.ExtraCond().setMember(member.getId()));
			
			//Delete the member operation.
			//sql = " DELETE FROM " + Params.dbName + ".member_operation WHERE member_id = " + memberId;
			//dbCon.stmt.executeUpdate(sql);
			
			//Delete the member operation history.
			//sql = " DELETE FROM " + Params.dbName + ".member_operation_history WHERE member_id = " + memberId;
			//dbCon.stmt.executeUpdate(sql);
			
			//Delete the weixin member associated with this member.
			//WeixinMemberDao.deleteByCond(dbCon, staff, new WeixinMemberDao.ExtraCond().setMember(member));
			
			//Unbound the weixin member id
			sql = " UPDATE " + Params.dbName + ".weixin_member SET member_id = 0 WHERE member_id = " + member.getId();
			dbCon.stmt.executeUpdate(sql);
			
			//Delete the member
			sql = " DELETE FROM " + Params.dbName + ".member WHERE member_id = " + member.getId();
			dbCon.stmt.executeUpdate(sql);
			
			amount++;
		}
		return amount;
	}
	
	/**
	 * Delete the member to specific id
	 * @param staff
	 * 			the staff to perform this action
	 * @param memberId
	 * 			the member id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the member to delete does NOT exist
	 */
	public static void deleteById(Staff staff, int memberId) throws SQLException, BusinessException {
		if(deleteByCond(staff, new ExtraCond().setId(memberId)) == 0){
			throw new BusinessException(MemberError.MEMBER_NOT_EXIST);
		}
	}

	/**
	 * Delete the member to specific id
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param memberId
	 * 			the member id to delete
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException 
	 * 			throws if the member to delete does NOT exist
	 */
	public static void deleteById(DBCon dbCon, Staff staff, int memberId) throws SQLException, BusinessException{
		if(deleteByCond(dbCon, staff, new ExtraCond().setId(memberId)) == 0){
			throw new BusinessException(MemberError.MEMBER_NOT_EXIST);
		}
	}
	
	/**
	 * Perform the re-consume operation to a member.
	 * @param dbCon	
	 * 			the database connection
	 * @param staff	
	 * 			the staff to perform this action
	 * @param memberId 
	 * 			the id to member account
	 * @param consumePrice	
	 * 			the price to consume
	 * @param payType
	 * 			the payment type referred to {@link Order.PayType}
	 * @param order
	 * 			the associated order to this consumption
	 * @return the member operation to the consumption operation
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 * @throws BusinessException
	 *	 		throws if one of cases below occurred<br>
	 *			1 - the consume price exceeds total balance to this member account<br>
	 *			2 - the member account to consume is NOT found.
	 */
	public static MemberOperation reConsume(Staff staff, int memberId, float consumePrice, PayType payType, int orderId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			MemberOperation mo = reConsume(dbCon, staff, memberId, consumePrice, payType, orderId);
			dbCon.conn.commit();
			return mo;
		}catch(BusinessException | SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Perform the re-consume operation to a member.
	 * @param dbCon	
	 * 			the database connection
	 * @param staff	
	 * 			the staff to perform this action
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
	public static MemberOperation reConsume(DBCon dbCon, Staff staff, int memberId, float consumePrice, PayType payType, int orderId) throws SQLException, BusinessException{
		
		Member member = getById(dbCon, staff, memberId);
		
		//Perform the consume operation and get the related member operation.
		MemberOperation mo = member.reConsume(consumePrice, payType);
		
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
	 * Restore the member account from specific order
	 * @param dbCon	
	 * 			the database connection
	 * @param staff	
	 * 			the staff to perform this action
	 * @param memberId 
	 * 			the id to member account
	 * @param order
	 * 			the associated order to this consumption
	 * @return the member operation to restore account
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 * @throws BusinessException
	 *	 		throws if one of cases the member operation to last consumption NOT be found
	 */
	public static MemberOperation restore(DBCon dbCon, Staff staff, int memberId, Order order) throws SQLException, BusinessException{
		MemberOperation lastConsumption = MemberOperationDao.getLastConsumptionByOrder(dbCon, staff, order);
		
		Member member = getById(dbCon, staff, memberId);
		
		//Perform the consume operation and get the related member operation.
		MemberOperation mo = member.restore(lastConsumption);
		
		//Set the associate order id
		mo.setOrderId(order.getId());
		
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
	 * @param dbCon	
	 * 			the database connection
	 * @param staff	
	 * 			the staff to perform this action
	 * @param memberId 
	 * 			the id to member account
	 * @param consumePrice	
	 * 			the price to consume
	 * @param extraPrice
	 * 			the extra price to use
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
	 *	 		throws if one of cases below occurred
	 *			<li>the consume price exceeds total balance to this member account
	 *			<li>the member account to consume is NOT found
	 */
	public static MemberOperation consume(DBCon dbCon, Staff staff, int memberId, float consumePrice, float extraPrice, PayType payType, int orderId) throws SQLException, BusinessException{
		
		Member member = getById(dbCon, staff, memberId);
		
		//Perform the consume operation and get the related member operation.
		MemberOperation mo = member.consume(consumePrice, extraPrice, payType);
		
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

		//执行会员升级
		try{
			upgrade(dbCon, staff, new Member.UpgradeBuilder(memberId));
		}catch(BusinessException | SQLException ignored){
			ignored.printStackTrace();
		}
		
		//获取门店的佣金比例
		try{
			Represent represent = RepresentDao.getByCond(dbCon, staff, null).get(0);
			if(represent.isProgress() && represent.getComissionRate() > 0){
				//获取关系链
				final List<RepresentChain> recommends = RepresentChainDao.getByCond(dbCon, staff, new RepresentChainDao.ExtraCond().setSubscriberId(memberId));
				if(!recommends.isEmpty()){
					
					//获取推荐人
					Member referrer = getById(dbCon, staff, recommends.get(0).getRecommendMemberId());
					
					//计算出佣金充额(四舍五入)
					int commission = Math.round(consumePrice * represent.getComissionRate());
					
					//为推荐人充值佣金 
					if(commission > 0){
						charge(dbCon, staff, referrer.getId(), 0, commission, ChargeType.COMMISSION, Integer.toString(orderId));
					}
				}
			}
		}catch(BusinessException | SQLException ignored){
			ignored.printStackTrace();
		}
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
	 * @param extraPrice
	 * 			the extra price to use
	 * @param payType
	 * 			the payment type referred to {@link PayType}
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
	public static MemberOperation consume(Staff staff, int memberId, float consumePrice, float extraPrice, PayType payType, int orderId) throws SQLException, BusinessException{
		
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			MemberOperation mo = MemberDao.consume(dbCon, staff, memberId, consumePrice, extraPrice, payType, orderId);
			dbCon.conn.commit();
			return mo;
			
		}catch(BusinessException | SQLException e){
			dbCon.conn.rollback();
			throw e;	
		}finally{
			dbCon.disconnect();
		}
	}

	/**
	 * Perform the represent chain to both subscriber and referrer in case of represent.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to represent chain {@link Member#ChainBuilder}
	 * @return the member operations to both subscribe point & money
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if any cases below
	 * 			<li>the member to subscribe does NOT exist
	 * 			<li>the member to recommend does NOT exist
	 * 			<li>the represent does NOT exist
	 */
	public static Map<Member, MemberOperation[]> chain(Staff staff, Member.ChainBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			Map<Member, MemberOperation[]> result = chain(dbCon, staff, builder);
			dbCon.conn.commit();
			return result;
		}catch(BusinessException | SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Perform the represent chain to both subscriber and referrer in case of represent.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the builder to represent chain {@link Member#ChainBuilder}
	 * @return the member operations to both subscriber & referrer
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if any cases below
	 * 			<li>the member to subscribe does NOT exist
	 * 			<li>the member to recommend does NOT exist
	 * 			<li>the represent does NOT exist
	 */
	public static Map<Member, MemberOperation[]> chain(DBCon dbCon, Staff staff, Member.ChainBuilder builder) throws SQLException, BusinessException{
		
		if(builder.getSubscriber() == builder.getReferrer()){
			throw new BusinessException("推荐与关注不能是相同的会员");
		}

		if(!RepresentChainDao.getByCond(dbCon, staff, new RepresentChainDao.ExtraCond().setSubscriberId(builder.getSubscriber())).isEmpty()){
			throw new BusinessException("对不起, 您之前已经被推荐过");
		}
		
		final Represent represent = RepresentDao.getByCond(dbCon, staff, null).get(0);

		if(!represent.isProgress()){
			throw new BusinessException("对不起, 代言活动已经结束");
		}
		
		final Map<Member, MemberOperation[]> result = new HashMap<>();
		
		final Member subscriber = getById(dbCon, staff, builder.getSubscriber());
		
		//Perform the chain to subscriber.
		final MemberOperation[] mo2Subscirber = subscriber.subscribe(represent);
		
		result.put(subscriber, mo2Subscirber);
		
		//Insert the member operation to subscriber.
		for(MemberOperation mo : mo2Subscirber){
			MemberOperationDao.insert(dbCon, staff, mo);
		}
		
		//Update the base & extra balance and point to subscriber.
		String sql = " UPDATE " + Params.dbName + ".member SET" +
					 " point = " + subscriber.getPoint() + "," + 
					 " base_balance = " + subscriber.getBaseBalance() + ", " +
					 " extra_balance = " + subscriber.getExtraBalance() + "," + 
					 " total_charge = " + subscriber.getTotalCharge() + 
					 " WHERE member_id = " + subscriber.getId();
		dbCon.stmt.executeUpdate(sql);
		
		final Member referrer = getById(dbCon, staff, builder.getReferrer());
		
		//Perform the chain to referrer.
		final MemberOperation[] mo2Referrer = referrer.recommend(represent);
		
		result.put(referrer, mo2Referrer);
		
		//Insert the member operation to referrer.
		for(MemberOperation mo : mo2Referrer){
			MemberOperationDao.insert(dbCon, staff, mo);
		}
		
		//Update the base & extra balance and point to referrer.
		sql = " UPDATE " + Params.dbName + ".member SET" +
			  " point = " + referrer.getPoint() + "," + 
			  " base_balance = " + referrer.getBaseBalance() + ", " +
			  " extra_balance = " + referrer.getExtraBalance() + "," + 
			  " total_charge = " + referrer.getTotalCharge() + 
			  " WHERE member_id = " + referrer.getId();
		dbCon.stmt.executeUpdate(sql);
		
//		//Insert the represent chain.
		sql = " INSERT INTO " + Params.dbName + ".represent_chain ( " +
			  " `restaurant_id`, `subscribe_date`, " + 
			  " `recommend_member_id`, `recommend_member`, `recommend_point`, `recommend_money`, `subscribe_member_id`, `subscribe_member`, `subscribe_point`, `subscribe_money` ) VALUES ( " +
			  staff.getRestaurantId() + "," +
			  " NOW(), " +
			  referrer.getId() + "," +
			  "'" + referrer.getName() + "'," +
			  represent.getRecommendPoint() + "," +
			  represent.getRecommentMoney() + "," +
			  subscriber.getId() + "," +
			  "'" + subscriber.getName() + "'," +
			  represent.getSubscribePoint() + "," +
			  represent.getSubscribeMoney() + 
			  " ) ";
		
		dbCon.stmt.executeUpdate(sql);
		
		return result;
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
	 * 			throws if cases below
	 * 			<li>the member id to perform charge does NOT exit
	 * 			<li>the staff does NOT contains the charge privilege 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 */
	public static MemberOperation charge(DBCon dbCon, Staff staff, int memberId, float chargeMoney, float accountMoney, ChargeType chargeType) throws BusinessException, SQLException{
		return charge(dbCon, staff, memberId, chargeMoney, accountMoney, chargeType, null);
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
	 * @param comment
	 * 			the charge comment
	 * @return the member operation to this charge.
	 * @throws BusinessException
	 * 			throws if cases below
	 * 			<li>the member id to perform charge does NOT exit
	 * 			<li>the staff does NOT contains the charge privilege 
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statements
	 */
	public static MemberOperation charge(DBCon dbCon, Staff staff, int memberId, float chargeMoney, float accountMoney, ChargeType chargeType, String comment) throws BusinessException, SQLException{
		if(!staff.getRole().hasPrivilege(Privilege.Code.MEMBER_CHARGE) && chargeType != ChargeType.COMMISSION){
			throw new BusinessException(StaffError.MEMBER_CHARGE_NOT_ALLOW);
		}
		
		if(chargeMoney < 0){
			throw new IllegalArgumentException("The amount of charge money(amount = " + chargeMoney + ") must be more than zero");
		}
		
		Member member = getById(dbCon, staff, memberId);
		
		//Perform the charge operation and get the related member operation.
		MemberOperation mo = member.charge(chargeMoney, accountMoney, chargeType);
		if(comment != null){
			if(chargeType != ChargeType.COMMISSION){
				List<MemberOperation> mo4Consume = MemberOperationDao.getByCond(dbCon, staff, new MemberOperationDao.ExtraCond(DateType.TODAY)
																		  .setOrder(Integer.parseInt(comment))
																		  .addOperationType(MemberOperation.OperationType.CONSUME), null);
				mo.setComment("账单号:" + comment + ",消费人:" + (mo4Consume.isEmpty() ? "---" : mo4Consume.get(0).getMemberName()));
			}else{
				mo.setComment(comment);
			}
		}
		
		//Insert the member operation to this charge operation.
		MemberOperationDao.insert(dbCon, staff, mo);
		
		//Update the base & extra balance and point.
		String sql = " UPDATE " + Params.dbName + ".member SET" +
					 " member_id = member_id " +
					 " ,base_balance = " + member.getBaseBalance() + 
					 " ,extra_balance = " + member.getExtraBalance()  + 
					 " ,total_charge = " + member.getTotalCharge() + 
					 (chargeType == ChargeType.COMMISSION ? " ,total_commission = " + member.getTotalCommission() : "") +
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
	 * 			throws if cases below
	 * 			<li>the member id to perform charge does NOT exit
	 * 			<li>the staff does NOT contains the charge privilege  
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
			
		}catch(BusinessException | SQLException e){
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
	 * 			throws if cases below
	 * 			<li>insufficient to refund
	 * 			<li>the staff does NOT contains member refund privilege
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static MemberOperation refund(DBCon dbCon, Staff staff, int memberId, float refundMoney, float accountMoney) throws BusinessException, SQLException{
		if(!staff.getRole().hasPrivilege(Privilege.Code.MEMBER_REFUND)){
			throw new BusinessException(StaffError.MEMBER_REFUND_NOT_ALLOW);
		}
		
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
				 " total_refund = " + member.getTotalRefund() +  
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
	 * 			throws if cases below
	 * 			<li>insufficient to refund
	 * 			<li>the staff does NOT contains member refund privilege
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
			
		}catch(BusinessException | SQLException e){
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
	 * @return the list to member operation to this point consumption 
	 * @throws BusinessException
	 * 			throws if point to consume exceeds the remaining
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 */
	public static List<MemberOperation> pointConsume(DBCon dbCon, Staff staff, Member.PointExchangeBuilder pointBuilder) throws BusinessException, SQLException{
		
		final List<Map.Entry<Promotion, Integer>> promotions = new ArrayList<>();
		float pointsExchange = 0;
		for(final Map.Entry<Integer, Integer> entry : pointBuilder.getPromotions()){
			final Promotion promotion = PromotionDao.getById(dbCon, staff, entry.getKey());
			if(!promotion.getIssueTrigger().getIssueRule().isPointExchange()){
				throw new BusinessException(("对不起, 【$(promotion)】的优惠券不能积分兑换").replace("$(promotion)", promotion.getCouponType().getName()));
			}
			pointsExchange += promotion.getIssueTrigger().getExtra() * entry.getValue();
			
			promotions.add(new Map.Entry<Promotion, Integer>() {

				@Override
				public Promotion getKey() {
					return promotion;
				}

				@Override
				public Integer getValue() {
					return entry.getValue();
				}

				@Override
				public Integer setValue(Integer value) {
					return null;
				}
			});	
		}
		
		final Member member = getById(dbCon, staff, pointBuilder.getMemberId());
		
		if(member.getPoint() < pointsExchange){
			throw new BusinessException("会员所剩积分不足,兑换失败");
		}
		
		final List<MemberOperation> result = new ArrayList<>();
		for(Entry<Promotion, Integer> entry : promotions){
			
			//Perform the point consumption and get the related member operation.
			MemberOperation mo = member.pointConsume(entry.getKey().getIssueTrigger().getExtra() * entry.getValue());
			
			StringBuilder allComment = new StringBuilder();
			allComment.append(("兑换【$(promotion)】优惠券【$(amount)】张,").replace("$(promotion)", entry.getKey().getTitle()).replace("$(amount)", String.valueOf(entry.getValue())));
			
			mo.setComment(allComment + pointBuilder.getComment());
			
			final Coupon.IssueBuilder issueBuilder = Coupon.IssueBuilder.newInstance4PointExchange();
			issueBuilder.addPromotion(entry.getKey().getId(), entry.getValue());
			issueBuilder.addMember(pointBuilder.getMemberId());
			
			//发送优惠券
			int[] issuedCoupons = CouponDao.issue(dbCon, staff, issueBuilder);
			
			//使用优惠券
			if(pointBuilder.isIssueAndUse()){
				for(int couponId : issuedCoupons){
					final Coupon coupon = CouponDao.getById(dbCon, staff, couponId);
					
					if(coupon.getPromotion().getUseTrigger().getUseRule().isFree()){
						final Coupon.UseBuilder useBuilder = Coupon.UseBuilder.newInstance4Point(member.getId());
						useBuilder.addCoupon(coupon);
						CouponDao.use(dbCon, staff, useBuilder);
					}else{
						throw new BusinessException(("对不起, 【$(promotion)】的优惠券不符合使用条件").replace("$(promotion)", coupon.getPromotion().getCouponType().getName()));
					}
				}
			}
			
			//Insert the member operation to this point consumption.
			mo.setId(MemberOperationDao.insert(dbCon, staff, mo));
		
			//Update the point.
			String sql = " UPDATE " + Params.dbName + ".member SET" +
					     " used_point = " + member.getUsedPoint() + "," +
						 " point = " + member.getPoint() + 
						 " WHERE member_id = " + pointBuilder.getMemberId();
			dbCon.stmt.executeUpdate(sql);
			
			result.add(mo);
		}
		
		return result;
		
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
	public static List<MemberOperation> pointConsume(Staff staff, Member.PointExchangeBuilder pointBuilder) throws BusinessException, SQLException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			List<MemberOperation> result = MemberDao.pointConsume(dbCon, staff, pointBuilder);
			dbCon.conn.commit();
			return result;
			
		}catch(BusinessException | SQLException e){
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
			
		}catch(BusinessException | SQLException e){
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
			
		}catch(BusinessException | SQLException e){
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
	 * Bind the raw member according to specific builder {@link Member#BindBuilder}.
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the bind builder
	 * @return the id to bound destination member
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below
	 * 			<li>the source member does NOT exist
	 * 			<li>the source member is NOT raw.
	 * 			<li>the destination member with mobile & card is NOT the same
	 */
	public static int bind(Staff staff, Member.BindBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			int memberId = bind(dbCon, staff, builder);
			dbCon.conn.commit();
			return memberId;
		}catch(BusinessException | SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Bind the raw member according to specific builder {@link Member#BindBuilder}.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder
	 * 			the bind builder
	 * @return the id to bound destination member
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if cases below
	 * 			<li>the source member does NOT exist
	 * 			<li>the source member is NOT raw.
	 * 			<li>the destination member with mobile & card is NOT the same
	 */
	public static int bind(DBCon dbCon, Staff staff, Member.BindBuilder builder) throws SQLException, BusinessException{
		
		Member source = getById(dbCon, staff, builder.build()[0].getId());
		if(!source.isRaw()){
			throw new BusinessException("【" + source.getName() + "】已绑定手机或会员卡", MemberError.BIND_FAIL);
		}
		
		Member dest = builder.build()[1];
		final List<Member> destsMatched = new ArrayList<Member>();
		//Get the member matched mobile.
		if(dest.hasMobile()){
			try{
				destsMatched.add(getByMobile(dbCon, staff, dest.getMobile()));
			}catch(BusinessException ignored){
				ignored.printStackTrace();
			}
		}
		//Get the member matched card.
		if(dest.hasMemberCard()){
			try{
				destsMatched.add(getByCard(dbCon, staff, dest.getMemberCard()));
			}catch(BusinessException ignored){
				ignored.printStackTrace();
			}
		}
		
		if(destsMatched.size() == 0){
			dest = source;
			
		}else if(destsMatched.size() == 1){
			dest = destsMatched.get(0);
			
		}else if(destsMatched.size() == 2){
			if(destsMatched.get(0).equals(destsMatched.get(1))){
				dest = destsMatched.get(0);
			}else{
				throw new BusinessException("绑定的手机和会员卡号分别属于两个不同的会员", MemberError.BIND_FAIL);
			}
		}
		
		if(!source.equals(dest)){
			if(source.hasWeixin()){
				String sql;
				//Associated the source weixin member with the destination.  
				sql = " UPDATE " + Params.dbName + ".weixin_member SET " + 
					  " member_id = " + dest.getId() +
					  " ,status = " + WxMember.Status.BOUND.getVal() + 
					  " ,bind_date = NOW() " +
					  " WHERE weixin_card = " + source.getWeixin().getCard();
				if(dbCon.stmt.executeUpdate(sql) == 0){
					throw new BusinessException(WxMemberError.WEIXIN_INFO_NOT_EXIST);
				}
			}
			//Delete the source member.
			deleteById(dbCon, staff, source.getId());
		}
		
		//Update the destination member like name, sex, and so on.
		int destMemberId = dest.getId();
		Member.UpdateBuilder updateBuilder = new Member.UpdateBuilder(destMemberId);
		dest = builder.build()[1];
		if(dest.getName().length() != 0){
			updateBuilder.setName(dest.getName());
		}
		if(dest.hasMobile()){
			updateBuilder.setMobile(dest.getMobile());
		}
		if(dest.hasMemberCard()){
			updateBuilder.setMemberCard(dest.getMemberCard());
		}
		if(dest.getAge() != null){
			updateBuilder.setAge(dest.getAge());
		}
		if(dest.getBirthday() != 0){
			updateBuilder.setBirthday(dest.getBirthday());
		}
		if(dest.getMemberType().getId() != 0){
			updateBuilder.setMemberType(dest.getMemberType().getId());
		}
		updateBuilder.setSex(dest.getSex());
		update(dbCon, staff, updateBuilder);
		
		return destMemberId;
	}
	
	/**
	 * patch the wx card to an exist member.
	 * @param staff
	 * 			the staff to perform this action
	 * @param destMemberId
	 * 			the member attached to
	 * @param wxMemberId
	 * 			the id to wx member
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if any cases below
	 * 			<li>the destination member does NOT exist
	 * 			<li>the weixin member does NOT exist
	 * 			<li>the destination member has weixin card before
	 */
	public static void patchWxCard(Staff staff, int destMemberId, int wxMemberId) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			dbCon.conn.setAutoCommit(false);
			patchWxCard(dbCon, staff, destMemberId, wxMemberId);
			dbCon.conn.commit();
		}catch(BusinessException | SQLException e){
			dbCon.conn.rollback();
			throw e;
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * patch the wx card to an exist member.
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param destMemberId
	 * 			the member attached to
	 * @param wxMemberId
	 * 			the id to wx member
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if any cases below
	 * 			<li>the destination member does NOT exist
	 * 			<li>the weixin member does NOT exist
	 * 			<li>the destination member has weixin card before
	 */
	public static void patchWxCard(DBCon dbCon, Staff staff, int destMemberId, int wxMemberId) throws SQLException, BusinessException{
		Member destMember = getById(staff, destMemberId);
		Member wxMember = getById(staff, wxMemberId);
		
		if(destMember.hasWeixin()){
			throw new BusinessException("此会员已有微信电子卡，不能再执行补发操作");
		}
		
		//Delete the wx member.
		MemberDao.deleteById(staff, wxMember.getId());
		
		String sql;
		//Associated the weixin member with the destination.  
		sql = " UPDATE " + Params.dbName + ".weixin_member SET " + 
			  " member_id = " + destMemberId +
			  " ,status = " + WxMember.Status.BOUND.getVal() + 
			  " ,bind_date = NOW() " +
			  " WHERE weixin_card = " + wxMember.getWeixin().getCard();
		
		dbCon.stmt.executeUpdate(sql);
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
	
	/**
	 * Upgrade the member
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder {@link Member#UpgradeBuilder}
	 * 			the member to upgrade
	 * @return the level this member upgrade to, <code>null</code> means no level upgrade
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the member to upgrade does NOT exist
	 */
	public static Msg4Upgrade upgrade(Staff staff, Member.UpgradeBuilder builder) throws SQLException, BusinessException{
		DBCon dbCon = new DBCon();
		try{
			dbCon.connect();
			return upgrade(dbCon, staff, builder);
		}finally{
			dbCon.disconnect();
		}
	}
	
	/**
	 * Upgrade the member
	 * @param dbCon
	 * 			the database connection
	 * @param staff
	 * 			the staff to perform this action
	 * @param builder {@link Member#UpgradeBuilder}
	 * 			the member to upgrade
	 * @return the level this member upgrade to, <code>null</code> means no level upgrade
	 * @throws SQLException
	 * 			throws if failed to execute any SQL statement
	 * @throws BusinessException
	 * 			throws if the member to upgrade does NOT exist
	 */
	public static Msg4Upgrade upgrade(DBCon dbCon, Staff staff, Member.UpgradeBuilder builder) throws SQLException, BusinessException{
		//Check to see whether the member level to this restaurant exist.
		List<MemberLevel> lvs = MemberLevelDao.get(dbCon, staff);
		if(lvs.isEmpty()){
			return null;
		}
		
		//Sorted the level using threshold by descend 
		List<MemberLevel> upLvs = SortedList.newInstance(lvs, new Comparator<MemberLevel>(){
			@Override
			public int compare(MemberLevel lv1, MemberLevel lv2) {
				if(lv1.getPointThreshold() > lv2.getPointThreshold()){
					return -1;
				}else if(lv1.getPointThreshold() < lv2.getPointThreshold()){
					return 1;
				}else{
					return 0;
				}
			}
		});
		
		Member member = getById(dbCon, staff, builder.getMemberId());
		
		for(MemberLevel lv : lvs){
			//If the member type belongs to level route and its total point is greater than the threshold, then perform member level upgrade.
			if(member.getMemberType().equals(lv.getMemberType()) && member.getTotalPoint() > lv.getPointThreshold()){
				for(MemberLevel lvToUpgrade : upLvs){
					//upgrade the member to level whose threshold is nearest the member's
					if(member.getTotalPoint() > lvToUpgrade.getPointThreshold()){
						if(!member.getMemberType().equals(lvToUpgrade.getMemberType())){
							MemberDao.update(dbCon, staff, new Member.UpdateBuilder(member.getId()).setMemberType(lvToUpgrade.getMemberType()));
							return new Msg4Upgrade(member, lvToUpgrade);
						}
					}
				}
			}
		}
		
		return null;
	}
}
