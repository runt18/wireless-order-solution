package com.wireless.pojo.member;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DutyRange;
import com.wireless.pojo.util.DateUtil;

public class MemberCond implements Jsonable{

	public static class InsertBuilder{
		private final String name;
		private MemberType memberType;
		private RangeType rangeType;
		private DutyRange range;
		private float minConsumeMoney;
		private float maxConsumeMoney;
		private int minConsumeAmount;
		private int maxConsumeAmount;
		private float minBalance;
		private float maxBalance;
		
		public InsertBuilder(String name){
			this.name = name;
		}
		
		public InsertBuilder setRangeType(RangeType rangeType){
//			if(rangeType == RangeType.USER_DEFINE){
//				throw new IllegalArgumentException("不能设置自定义查询时间段");
//			}
			this.rangeType = rangeType;
			return this;
		}
		
		public InsertBuilder setRange(String onDuty, String offDuty){
			return setRange(DateUtil.parseDate(onDuty), DateUtil.parseDate(offDuty));
		}
		
		public InsertBuilder setRange(long onDuty, long offDuty){
			if(offDuty < onDuty){
				throw new IllegalArgumentException("会员查询的结束时间不能小于开始时间");
			}
			this.range = new DutyRange(onDuty, offDuty);
			this.rangeType = RangeType.USER_DEFINE;
			return this;
		}
		
		public InsertBuilder setMemberType(MemberType memberType){
			this.memberType = memberType;
			return this;
		}
		
		public InsertBuilder setConsumeMoney(float min, float max){
			if(max > 0 && max < min){
				throw new IllegalArgumentException("消费最大金额不能小于最小金额");
			}
			this.minConsumeMoney = min < 0 ? 0 : min;
			this.maxConsumeMoney = max < 0 ? 0 : max;
			return this;
		}
		
		public InsertBuilder setConsumeAmount(int min, int max){
			if(max > 0 && max < min){
				throw new IllegalArgumentException("消费最大次数不能小于最小次数");
			}
			this.minConsumeAmount = min < 0 ? 0 : min;
			this.maxConsumeAmount = max < 0 ? 0 : max;
			return this;
		}
		
		public InsertBuilder setBalance(float min, float max){
			if(max > 0 && max < min){
				throw new IllegalArgumentException("消费最大余额不能小于最小余额");
			}
			this.minBalance = min < 0 ? 0 : min;
			this.maxBalance = max < 0 ? 0 : max;
			return this;
		}
		
		public MemberCond build(){
			return new MemberCond(this);
		}
	}
	
	public static class UpdateBuilder{
		private final int id;
		private String name;
		private final static MemberType DUMMY = new MemberType(0);
		private MemberType memberType = DUMMY;
		private RangeType rangeType;
		private DutyRange range;
		private float minConsumeMoney;
		private float maxConsumeMoney;
		private int minConsumeAmount;
		private int maxConsumeAmount;
		private float minBalance;
		private float maxBalance;
		
		public UpdateBuilder(int id){
			this.id = id;
		}
		
		public UpdateBuilder setName(String name){
			this.name = name;
			return this;
		}
		
		public boolean isNameChanged(){
			return this.name != null;
		}
		
		public UpdateBuilder setMemberType(MemberType type){
			this.memberType = type;
			return this;
		}
		
		public boolean isMemberTypeChanged(){
			return this.memberType != DUMMY;
		}
		
		public boolean isRangeTypeChanged(){
			return this.rangeType != null;
		}
		
		public UpdateBuilder setRangeType(RangeType rangeType){
//			if(rangeType == RangeType.USER_DEFINE){
//				throw new IllegalArgumentException("不能设置自定义查询时间段");
//			}
			this.rangeType = rangeType;
			return this;
		}
		
		public UpdateBuilder setRange(String onDuty, String offDuty){
			return setRange(DateUtil.parseDate(onDuty), DateUtil.parseDate(offDuty));
		}
		
		public UpdateBuilder setRange(long onDuty, long offDuty){
			if(offDuty < onDuty){
				throw new IllegalArgumentException("会员查询的结束时间不能小于开始时间");
			}
			this.range = new DutyRange(onDuty, offDuty);
			this.rangeType = RangeType.USER_DEFINE;
			return this;
		}
		
		public boolean isRangeChanged(){
			return this.range != null;
		}
		
		public UpdateBuilder setConsumeMoney(float min, float max){
			if(max > 0 && max < min){
				throw new IllegalArgumentException("消费最大金额不能小于最小金额");
			}
			this.minConsumeMoney = min < 0 ? 0 : min;
			this.maxConsumeMoney = max < 0 ? 0 : max;
			return this;
		}
		
		public boolean isConsumeMoneyChanged(){
			return minConsumeMoney != 0 || maxConsumeMoney != 0;
		}
		
		public UpdateBuilder setConsumeAmount(int min, int max){
			if(max > 0 && max < min){
				throw new IllegalArgumentException("消费最大次数不能小于最小次数");
			}
			this.minConsumeAmount = min < 0 ? 0 : min;
			this.maxConsumeAmount = max < 0 ? 0 : max;
			return this;
		}
		
		public boolean isConsumeAmountChanged(){
			return minConsumeAmount != 0 || maxConsumeAmount != 0;
		}
		
		public UpdateBuilder setBalance(float min, float max){
			if(max > 0 && max < min){
				throw new IllegalArgumentException("消费最大余额不能小于最小余额");
			}
			this.minBalance = min < 0 ? 0 : min;
			this.maxBalance = max < 0 ? 0 : max;
			return this;
		}
		
		public boolean isBalanceChanged(){
			return minBalance != 0 || maxBalance != 0;
		}
		
		public MemberCond build(){
			return new MemberCond(this);
		}
	}
	
	public static enum RangeType{
		LAST_1_MONTH(1, "近1月"),
		LAST_2_MONTHS(2, "近2月"),
		LAST_3_MONTHS(3, "近3月"),
		USER_DEFINE(4, "自定义");
		
		private final int val;
		private String desc;
		
		RangeType(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public int getVal(){
			return this.val;
		}
		
		public static RangeType valueOf(int val){
			for(RangeType type : values()){
				if(type.val == val){
					return type;
				}
			}
			throw new IllegalArgumentException("The range type(val = " + val + ") is invalid.");
		}
		
		@Override
		public String toString(){
			return this.desc;
		}
	}
	
	private int id;
	private int restaurantId;
	private String name;
	private MemberType memberType;
	private RangeType rangeType;
	private DutyRange range;
	private float minConsumeMoney;
	private float maxConsumeMoney;
	private int minConsumeAmount;
	private int maxConsumeAmount;
	private float minBalance;
	private float maxBalance;
	
	
	private MemberCond(UpdateBuilder builder){
		setId(builder.id);
		setName(builder.name);
		setMemberType(builder.memberType);
		setRangeType(builder.rangeType);
		setRange(builder.range);
		setMinConsumeMoney(builder.minConsumeMoney);
		setMaxConsumeMoney(builder.maxConsumeMoney);
		setMinConsumeAmount(builder.minConsumeAmount);
		setMaxConsumeAmount(builder.maxConsumeAmount);
		setMinBalance(builder.minBalance);
		setMaxBalance(builder.maxBalance);
	}
	
	private MemberCond(InsertBuilder builder){
		setName(builder.name);
		setMemberType(builder.memberType);
		setRangeType(builder.rangeType);
		setRange(builder.range);
		setMinConsumeMoney(builder.minConsumeMoney);
		setMaxConsumeMoney(builder.maxConsumeMoney);
		setMinConsumeAmount(builder.minConsumeAmount);
		setMaxConsumeAmount(builder.maxConsumeAmount);
		setMinBalance(builder.minBalance);
		setMaxBalance(builder.maxBalance);
	}
	
	public MemberCond(int id){
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getRestaurantId() {
		return restaurantId;
	}
	
	public void setRestaurantId(int restaurantId) {
		this.restaurantId = restaurantId;
	}
	
	public String getName() {
		if(this.name == null){
			return "";
		}
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public MemberType getMemberType() {
		return memberType;
	}
	
	public boolean hasMemberType(){
		return this.memberType != null;
	}
	
	public void setMemberType(MemberType memberType) {
		this.memberType = memberType;
	}
	
	public boolean hasRange(){
		return this.range != null;
	}
	
	public DutyRange getRange() {
		return range;
	}
	
	public void setRange(DutyRange range) {
		this.range = range;
	}
	
	public RangeType getRangeType(){
		return this.rangeType;
	}
	
	public void setRangeType(RangeType type){
		this.rangeType = type;
	}
	
	public float getMinConsumeMoney() {
		return minConsumeMoney;
	}
	
	public void setMinConsumeMoney(float minConsumeMoney) {
		this.minConsumeMoney = minConsumeMoney;
	}
	
	public float getMaxConsumeMoney() {
		return maxConsumeMoney;
	}
	
	public void setMaxConsumeMoney(float maxConsumeMoney) {
		this.maxConsumeMoney = maxConsumeMoney;
	}
	
	public int getMinConsumeAmount() {
		return minConsumeAmount;
	}
	
	public void setMinConsumeAmount(int minConsumeAmount) {
		this.minConsumeAmount = minConsumeAmount;
	}
	
	public int getMaxConsumeAmount() {
		return maxConsumeAmount;
	}
	
	public void setMaxConsumeAmount(int maxConsumeAmount) {
		this.maxConsumeAmount = maxConsumeAmount;
	}
	
	public float getMinBalance() {
		return minBalance;
	}
	
	public void setMinBalance(float minBalance) {
		this.minBalance = minBalance;
	}
	
	public float getMaxBalance() {
		return maxBalance;
	}
	
	public void setMaxBalance(float maxBalance) {
		this.maxBalance = maxBalance;
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.id);
		jm.putString("name", this.name);
		if( this.memberType != null){
			jm.putInt("memberType", this.memberType.getId() );
		}
		jm.putInt("rangeType", this.getRangeType().getVal());
		jm.putString("beginDate", this.getRange().getOnDutyFormat(DateUtil.Pattern.DATE));
		jm.putString("endDate", this.getRange().getOffDutyFormat(DateUtil.Pattern.DATE));
		jm.putFloat("minConsumeMoney", this.minConsumeMoney);
		jm.putFloat("maxConsumeMoney", this.maxConsumeMoney);
		jm.putInt("minConsumeAmount", this.minConsumeAmount);
		jm.putInt("maxConsumeAmount", this.maxConsumeAmount);
		jm.putFloat("minBalance", this.minBalance);
		jm.putFloat("maxBalance", this.maxBalance);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jm, int flag) {
		
	}
}
