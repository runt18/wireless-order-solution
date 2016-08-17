package com.wireless.pojo.member;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
		private int minLastConsumption;
		private int maxLastConsumption;
		private float minTotalCharge;
		private float maxTotalCharge;
		private final List<Member.Age> ages = new ArrayList<Member.Age>();
		private Member.Sex sex;
		private boolean isRaw;
		private int minFansAmount;
		private int maxFansAmount;
		private float minCommissionAmount;
		private float maxCommissionAmount;
		private Integer recentlyBirthday;
		
		
		public InsertBuilder(String name){
			this.name = name;
		}
		
		public InsertBuilder setMinCommissionAmount(float minCommissionAmount){
			this.minCommissionAmount = minCommissionAmount;
			return this;
		}
		
		public InsertBuilder setMaxCommissionAmount(float maxComissionAmount){
			this.maxCommissionAmount = maxComissionAmount;
			return this;
		}
		
		public InsertBuilder setLastConsumption(int min, int max){
			this.minLastConsumption = min;
			this.maxLastConsumption = max;
			return this;
		}
		
		public InsertBuilder setRangeType(RangeType rangeType){
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
		
		public InsertBuilder setRaw(boolean onOff){
			this.isRaw = onOff;
			return this;
		}
		
		public InsertBuilder setRecentlyBirthday(int birthday){
			this.recentlyBirthday = birthday;
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
		
		public InsertBuilder setSex(Member.Sex sex){
			this.sex = sex;
			return this;
		}
		
		public InsertBuilder addAge(Member.Age age){
			this.ages.add(age);
			return this;
		}
		
		public InsertBuilder setCharge(float minCharge, float maxCharge){
			this.minTotalCharge = minCharge;
			this.maxTotalCharge = maxCharge;
			return this;
		}
		
		public InsertBuilder setMinFansAmount(int minFansAmount){
			this.minFansAmount = minFansAmount;
			return this;
		}
		
		public InsertBuilder setMaxFansAmount(int maxFansAmount){
			this.maxFansAmount = maxFansAmount;
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
		private boolean isRangeChanged = false;
		private DutyRange range;
		private float minConsumeMoney = -1;
		private float maxConsumeMoney = -1;
		private int minConsumeAmount = -1;
		private int maxConsumeAmount = -1;
		private float minBalance = -1;
		private float maxBalance = -1;
		private int minLastConsumption = -1;
		private int maxLastConsumption = -1;
		private Float minCharge;
		private Float maxCharge;
		private boolean isSexChanged = false;
		private Member.Sex sex;
		private List<Member.Age> ages;
		private boolean isRawChanged = false;
		private Boolean isRaw;
		private Integer minFansAmount;
		private Integer maxFansAmount;
		private Float minCommissionAmount;
		private Float maxCommissionAmount;
		private Integer recentlyBirthday;
		
		public UpdateBuilder(int id){
			this.id = id;
		}
		
		public UpdateBuilder setRecentlyBirthday(int birthday){
			this.recentlyBirthday = birthday;
			return this;
		}
		
		public boolean isBirthdayChange(){
			return this.recentlyBirthday != null;
		}
		
		public UpdateBuilder setCommissionRange(float min, float max){
			if(max > 0 && max < min){
				throw new IllegalArgumentException("最大佣金总额不能小雨最少佣金总额");
			}
			this.minCommissionAmount = min;
			this.maxCommissionAmount = max;
			return this;
		}
		
		public boolean isCommissionChanged(){
			return this.minCommissionAmount != null || this.maxCommissionAmount != null;
		}
		
		public UpdateBuilder setFansRange(int min, int max){
			if(max > 0 && max < min){
				throw new IllegalArgumentException("最大粉丝数不能小于最小粉丝数");
			}
			this.minFansAmount = min;
			this.maxFansAmount = max;
			return this;
		}
		
		public boolean isFansChanged(){
			return this.minFansAmount != null || this.maxFansAmount != null;
		}
		
		public UpdateBuilder setRaw(Boolean onOff){
			this.isRawChanged = true;
			this.isRaw = onOff;
			return this;
		}
		
		public boolean isRawChanged(){
			return this.isRawChanged;
		}
		
		public UpdateBuilder setSex(Member.Sex sex){
			this.isSexChanged = true;
			this.sex = sex;
			return this;
		}
		
		public boolean isSexChanged(){
			return this.isSexChanged;
		}
		
		public UpdateBuilder setCharge(float min, float max){
			this.minCharge = min;
			this.maxCharge = max;
			return this;
		}
		
		public boolean isChargeChanged(){
			return this.minCharge != null || this.maxCharge != null;
		}
		
		public UpdateBuilder addAge(Member.Age age){
			if(this.ages == null){
				this.ages = new ArrayList<Member.Age>();
			}
			this.ages.add(age);
			return this;
		}
		
		public UpdateBuilder clearAge(){
			if(this.ages == null){
				this.ages = new ArrayList<Member.Age>();
			}
			this.ages.clear();
			return this;
		}
		
		public boolean isAgeChanged(){
			return this.ages != null;
		}
		
		public UpdateBuilder setLastConsumption(int min, int max){
			this.minLastConsumption = min;
			this.maxLastConsumption = max;
			return this;
		}
		
		public boolean isLastConsumptionChanged(){
			return this.minLastConsumption >= 0 || this.maxLastConsumption >= 0;
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
			return this.isRangeChanged;
		}
		
		public UpdateBuilder setRangeType(RangeType rangeType){
//			if(rangeType == RangeType.USER_DEFINE){
//				throw new IllegalArgumentException("不能设置自定义查询时间段");
//			}
			this.isRangeChanged = true;
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
			return minConsumeMoney >= 0 || maxConsumeMoney >= 0;
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
			return minConsumeAmount >= 0 || maxConsumeAmount >= 0;
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
			return minBalance >= 0 || maxBalance >= 0;
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
	private int minLastConsumption;
	private int maxLastConsumption;
	private Member.Sex sex;
	private final List<Member.Age> ages = new ArrayList<Member.Age>();
	private float minCharge;
	private float maxCharge;
	private int minFansAmount;
	private int maxFansAmount;
	private float minCommissionAmount;
	private float maxCommissionAmount;
	private Boolean isRaw;
	private Integer recentlyBirthday;
	
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
		setMinLastConsumption(builder.minLastConsumption);
		setMaxLastConsumption(builder.maxLastConsumption);
		setMinCharge(builder.minCharge != null ? builder.minCharge : 0);
		setMaxCharge(builder.maxCharge != null ? builder.maxCharge : 0);
		setMinFansAmount(builder.minFansAmount != null ? builder.minFansAmount : 0);
		setMaxFansAmount(builder.maxFansAmount != null ? builder.maxFansAmount : 0);
		setMinCommissionAmount(builder.minCommissionAmount != null ? builder.minCommissionAmount : 0);
		setMaxCommissionAmount(builder.maxCommissionAmount != null ? builder.maxCommissionAmount : 0);
		setSex(builder.sex);
		setAges(builder.ages);
		if(builder.isRaw != null){
			setRaw(builder.isRaw);
		}
		setRecentlyBirthday(builder.recentlyBirthday != null ? builder.recentlyBirthday : -1);
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
		setMinLastConsumption(builder.minLastConsumption);
		setMaxLastConsumption(builder.maxLastConsumption);
		setSex(builder.sex);
		setAges(builder.ages);
		setMinCharge(builder.minTotalCharge);
		setMaxCharge(builder.maxTotalCharge);
		setRaw(builder.isRaw);
		setMinFansAmount(builder.minFansAmount);
		setMaxFansAmount(builder.maxFansAmount);
		setMinCommissionAmount(builder.minCommissionAmount);
		setMaxCommissionAmount(builder.maxCommissionAmount);
		setRecentlyBirthday(builder.recentlyBirthday != null ? builder.recentlyBirthday : -1);
	}
	
	public MemberCond(int id){
		this.id = id;
	}
	
	
	public int getRecentlyBirthday() {
		return recentlyBirthday;
	}

	public void setRecentlyBirthday(int birthday) {
		this.recentlyBirthday = birthday;
	}

	public void setMaxCommissionAmount(float max){
		this.maxCommissionAmount = max;
	}
	
	public float getMaxCommissionAmount(){
		return this.maxCommissionAmount;
	}
	
	public void setMinCommissionAmount(float min){
		this.minCommissionAmount = min;
	}
	
	public float getMinCommissionAmount(){
		return this.minCommissionAmount;
	}
	
	public void setMaxFansAmount(int max){
		this.maxFansAmount = max;
	}
	
	public int getMaxFansAmount(){
		return this.maxFansAmount;
	}
	
	public void setMinFansAmount(int min){
		this.minFansAmount = min;
	}
	
	public int getMinFansAmount(){
		return this.minFansAmount;
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

	public boolean hasRaw(){
		return this.isRaw != null;
	}
	
	public boolean isRaw(){
		return this.isRaw != null ? this.isRaw : false;
	}
	
	public void setRaw(boolean onOff){
		this.isRaw = onOff;
	}
	
	public void setSex(Member.Sex sex){
		this.sex = sex;
	}
	
	public boolean hasSex(){
		return this.sex != null;
	}
	
	public Member.Sex getSex(){
		return this.sex;
	}
	
	public void setAges(List<Member.Age> ages){
		if(ages != null){
			this.ages.clear();
			this.ages.addAll(ages);
		}
	}
	
	public void setAges(String ageString){
		if(ageString != null && ageString.length() > 0){
			for(String age : ageString.split(",")){
				addAge(Member.Age.valueOf(Integer.parseInt(age)));
			}
		}
	}
	
	public void addAge(Member.Age age){
		this.ages.add(age);
	}
	
	public String getAgesString(){
		StringBuilder ages = new StringBuilder();
		for(Member.Age age : this.ages){
			if(ages.length() > 0){
				ages.append(",");
			}
			ages.append(age.getVal());
		}
		return ages.toString();
	}
	
	public List<Member.Age> getAges(){
		return Collections.unmodifiableList(this.ages);
	}
	
	public void setMinCharge(float minCharge){
		this.minCharge = minCharge;
	}
	
	public float getMinCharge(){
		return this.minCharge;
	}
	
	public void setMaxCharge(float maxCharge){
		this.maxCharge = maxCharge;
	}
	
	public float getMaxCharge(){
		return this.maxCharge;
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

	public void setMinLastConsumption(int minLastConsumption){
		this.minLastConsumption = minLastConsumption;
	}
	
	public int getMinLastConsumption(){
		return this.minLastConsumption;
	}
	
	public void setMaxLastConsumption(int maxLastConsumption){
		this.maxLastConsumption = maxLastConsumption;
	}
	
	public int getMaxLastConsumption(){
		return this.maxLastConsumption;
	}
	
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.id);
		jm.putString("name", this.name);
		if( this.memberType != null){
			jm.putInt("memberType", this.memberType.getId() );
		}
		if(this.rangeType != null){
			jm.putInt("rangeType", this.getRangeType().getVal());
			jm.putString("beginDate", this.getRange() != null ? this.getRange().getOnDutyFormat(DateUtil.Pattern.DATE) : null);
			jm.putString("endDate", this.getRange()!= null ? this.getRange().getOffDutyFormat(DateUtil.Pattern.DATE) : null);
		}
		jm.putFloat("minConsumeMoney", this.minConsumeMoney);
		jm.putFloat("maxConsumeMoney", this.maxConsumeMoney);
		jm.putInt("minConsumeAmount", this.minConsumeAmount);
		jm.putInt("maxConsumeAmount", this.maxConsumeAmount);
		jm.putFloat("minBalance", this.minBalance);
		jm.putFloat("maxBalance", this.maxBalance);
		jm.putInt("minLastConsumption", this.minLastConsumption);
		jm.putInt("maxLastConsumption", this.maxLastConsumption);
		if(hasSex()){
			jm.putInt("sex", this.sex.getVal());
			jm.putString("sexText", this.sex.getDesc());
		}
		if(hasRaw()){
			jm.putBoolean("isRaw", this.isRaw());
		}
		jm.putFloat("minCharge", this.minCharge);
		jm.putFloat("maxCharge", this.maxCharge);
		jm.putInt("minFansAmount", this.minFansAmount);
		jm.putInt("maxFansAmount", this.maxFansAmount);
		if(!this.ages.isEmpty()){
			final StringBuilder ageText = new StringBuilder();
			for(Member.Age age : ages){
				if(ageText.length() > 0){
					ageText.append(",");
				}
				ageText.append(age.toString());
			}
			jm.putString("ageText", ageText.toString());
			jm.putString("age", this.getAgesString());
		}
		
		jm.putFloat("minCommissionAmount", this.minCommissionAmount);
		jm.putFloat("maxCommissionAmount", this.maxCommissionAmount);
		jm.putFloat("birthday", this.recentlyBirthday);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jm, int flag) {
		
	}
}
