package com.wireless.pojo.member;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.wireless.exception.BusinessException;
import com.wireless.exception.MemberError;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.dishesOrder.PayType;
import com.wireless.pojo.member.MemberOperation.ChargeType;
import com.wireless.pojo.member.MemberOperation.OperationType;
import com.wireless.pojo.member.represent.Represent;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.promotion.Promotion;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.util.DateUtil.Pattern;

public class Member implements Parcelable, Jsonable, Comparable<Member>{
	
	public static final int MEMBER_PARCELABLE_SIMPLE = 0;
	public static final int MEMBER_PARCELABLE_COMPLEX = 1;
	
	public static enum Age{
		UNKNOWN(0, "未填写", "1900"),
		AGE_60(1, "60后", "1960"),
		AGE_70(2, "70后", "1970"),
		AGE_80(3, "80后", "1980"),
		AGE_90(4, "90后", "1990"),
		AGE_00(5, "00后", "2000"),
		AGE_50(6, "50后", "1950");
		
		private final int val;
		private final String desc;
		public final String suffix;
		
		Age(int val, String desc, String suffix){
			this.val = val;
			this.desc = desc;
			this.suffix = suffix;
		}
		
		public static Age valueOf(int val){
			for(Age age : values()){
				if(age.val == val){
					return age;
				}
			}
			throw new IllegalArgumentException("The age(val = " + val + ") is invalid.");
		}
		
		public int getVal(){
			return this.val;
		}
		
		@Override
		public String toString(){
			return this.desc;
		}
	}
	
	/**
	 * 积分调整类型
	 * @author WuZY
	 *	INCREASE: 在原积分基础上增加积分
	 *	REDUCE: 在原积分基础上减少积分
	 *	SET: 设置积分
	 */
	public static enum AdjustType{
		INCREASE(1, "增加"),
		REDUCE(2, 	"减少"),
		SET(3, 		"设置");
		
		AdjustType(int value, String text){
			this.value = value;
			this.text = text;
		}
		
		private int value;
		private String text;
		
		public int getValue(){
			return this.value;
		}
		public String getText(){
			return this.text;
		}
		@Override
		public String toString(){
			return "adjustment(value = " + value + ",text = " + text + ")";
		}
		public static AdjustType valueOf(int value){
			for(AdjustType temp : values()){
				if(value == temp.getValue()){
					return temp;
				}
			}
			throw new IllegalArgumentException("The adjustment(value = " + value + ") is invalid.");
		}
	}
	
	public static enum Sex{
		MALE(0, "男"),
		FEMALE(1, "女");
		
		private final int val;
		private final String desc;
		
		Sex(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		@Override
		public String toString(){
			return "sex(val = " + val + ",desc = " + desc + ")";
		}
		
		public static Sex valueOf(int val){
			for(Sex sex : values()){
				if(val == sex.val){
					return sex;
				}
			}
			throw new IllegalArgumentException("The sex(val = " + val + ") is invalid.");
		}
		
		public int getVal(){
			return val;
		}
		
		public String getDesc(){
			return desc;
		}
		
	}
	
	public static class InsertBuilder{
		private final Member data = new Member();
		
		public InsertBuilder(String name, int memberTypeId){
			data.setName(name);
			data.setMemberType(new MemberType(memberTypeId));
		}
		
		public InsertBuilder(String name, MemberType memberType){
			data.setName(name);
			data.setMemberType(memberType);
		}
		
		public InsertBuilder setReferrer(int staffId){
			data.setReferrerId(staffId);
			return this;
		}
		public InsertBuilder setSex(Sex sex){
			this.data.setSex(sex);
			return this;
		}
		public InsertBuilder setTele(String tele){
			this.data.setTele(tele);
			return this;
		}
		public InsertBuilder setBirthday(long birthday){
			this.data.setBirthday(birthday);
			return this;
		}
		public InsertBuilder setAge(Age age){
			this.data.setAge(age);
			return this;
		}
		public InsertBuilder setIdCard(String idCard){
			this.data.setIdCard(idCard);
			return this;
		}
		public InsertBuilder setCompany(String company){
			this.data.setCompany(company);
			return this;
		}
		public InsertBuilder setContactAddr(String addr){
			this.data.setContactAddress(addr);
			return this;
		}
		public InsertBuilder setMemberCard(String card){
			this.data.setMemberCard(card);
			return this;
		}
		public InsertBuilder setMobile(String mobile){
			this.data.setMobile(mobile);
			return this;
		}
		public Member build(){
			return this.data;
		}
	}
	
	public static class UpdateBuilder{
		
		private final int memberId;
		
		private String name;				// 客户名称
		private Sex sex;					// 性别
		private String tele;				// 电话
		private String mobile;				// 手机
		private Age age;					// 年龄段
		private Long birthday;				// 生日
		private String idCard;				// 身份证
		private String company;				// 公司
		private String contactAddress;		// 联系地址
		private int memberTypeId;			// 会员类型编号
		private String memberCard;			// 会员卡号
		private int referrerId;				// 推荐人
		private int wxOrderAmount = -1;		// 微信订单数量
		
		public UpdateBuilder(int memberId){
			this.memberId = memberId;
		}
		
		public UpdateBuilder setWxOrderAmount(int wxOrderAmount){
			this.wxOrderAmount = wxOrderAmount;
			return this;
		}
		
		public boolean isWxOrderAmountChanged(){
			return this.wxOrderAmount > 0;
		}
		
		public UpdateBuilder setReferrer(int staffId){
			this.referrerId = staffId;
			return this;
		}
		
		public boolean isReferrerChanged(){
			return this.referrerId != 0;
		}
		
		public UpdateBuilder setName(String name){
			this.name = name;
			return this;
		}
		
		public boolean isNameChanged(){
			return this.name != null;
		}
		
		public UpdateBuilder setMobile(String mobile){
			this.mobile = mobile;
			return this;
		}
		
		public boolean isMobileChanged(){
			return this.mobile != null;
		}
		
		public UpdateBuilder setMemberType(int memberTypeId){
			this.memberTypeId = memberTypeId;
			return this;
		}
		
		public UpdateBuilder setMemberType(MemberType memberType){
			this.memberTypeId = memberType.getId();
			return this;
		}
		
		public boolean isMemberTypeChanged(){
			return this.memberTypeId != 0;
		}
		
		public UpdateBuilder setSex(Sex sex){
			this.sex = sex;
			return this;
		}
		
		public boolean isSexChanged(){
			return this.sex != null;
		}
		
		public UpdateBuilder setTele(String tele){
			this.tele = tele;
			return this;
		}
		
		public boolean isTeleChanged(){
			return this.tele != null;
		}
		
		public UpdateBuilder setAge(Age age){
			this.age = age;
			return this;
		}
		
		public boolean isAgeChanged(){
			return this.age != null;
		}
		
		public UpdateBuilder setBirthday(String birthday){
			if(birthday != null){
				this.birthday = DateUtil.parseDate(birthday);
			}
			return this;
		}
		
		public UpdateBuilder setBirthday(long birthday){
			this.birthday = birthday;
			return this;
		}
		
		public boolean isBirthdayChanged(){
			return this.birthday != null;
		}
		
		public UpdateBuilder setIdCard(String idCard){
			this.idCard = idCard;
			return this;
		}
		
		public boolean isIdChardChanged(){
			return this.idCard != null;
		}
		
		public UpdateBuilder setCompany(String company){
			this.company = company;
			return this;
		}
		
		public boolean isCompanyChanged(){
			return this.company != null;
		}
		
		public UpdateBuilder setContactAddr(String addr){
			this.contactAddress = addr;
			return this;
		}
		
		public boolean isContactAddrChanged(){
			return this.contactAddress != null;
		}
		
		public UpdateBuilder setMemberCard(String card){
			this.memberCard = card;
			return this;
		}
		
		public boolean isMemberCardChanged(){
			return this.memberCard != null;
		}
		
		public Member build(){
			return new Member(this);
		}
	}
	
	public static class PointExchangeBuilder{
		private final int memberId;
		private final List<Map.Entry<Integer, Integer>> coupons = new ArrayList<Map.Entry<Integer, Integer>>();
		private String comment;
		private boolean isIssueAndUse;
		
		public PointExchangeBuilder(int memberId){
			this.memberId = memberId;
		} 
		
		public PointExchangeBuilder addPromotion(final Promotion promotion, final int amount){
			return addPromotion(promotion.getId(), amount);
		}
		
		public PointExchangeBuilder addPromotion(final int promotionId, final int amount){
			coupons.add(new Map.Entry<Integer, Integer>() {
				
				@Override
				public Integer getKey() {
					return promotionId;
				}

				@Override
				public Integer getValue() {
					return amount;
				}

				@Override
				public Integer setValue(Integer value) {
					return null;
				}
			});
			return this;
		}
		
		public List<Map.Entry<Integer, Integer>> getPromotions(){
			return Collections.unmodifiableList(coupons);
		}
		
		public PointExchangeBuilder setComment(String comment){
			this.comment = comment;
			return this;
		}
		
		public PointExchangeBuilder setIssueAndUse(boolean onOff){
			this.isIssueAndUse = onOff;
			return this;
		}
		
		public int getMemberId(){
			return this.memberId;
		}
		
		public String getComment(){
			if(this.comment == null){
				return "";
			}
			return this.comment;
		}
		
		public boolean isIssueAndUse(){
			return this.isIssueAndUse;
		}
		
	}
	
	public static class BindBuilder{
		private final int memberId;
		private final String mobile;
		private final String card;
		private String name;
		private Sex sex;
		private long birthday;
		private Age age;
		private final MemberType memberType = new MemberType(0);
		
		public BindBuilder(int memberId, String mobile, String card){
			this.memberId = memberId;
			this.mobile = mobile;
			this.card = card;
		}
		public BindBuilder setName(String name) {
			this.name = name;
			return this;
		}

		public BindBuilder setSex(Sex sex) {
			this.sex = sex;
			return this;
		}

		public BindBuilder setAge(Age age){
			this.age = age;
			return this;
		}
		
		public BindBuilder setBirthday(long birthday) {
			this.birthday = birthday;
			return this;
		}

		public BindBuilder setMemberType(int memberTypeId){
			this.memberType.setId(memberTypeId);
			return this;
		}

		public BindBuilder setMemberType(MemberType memberType){
			return setMemberType(memberType.getId());
		}
		
		public Member[] build(){
			Member source = new Member(memberId);
			Member dest = new Member(0);
			dest.setMobile(mobile);
			dest.setMemberCard(card);
			dest.setName(name);
			dest.setSex(sex);
			dest.setBirthday(birthday);
			dest.setAge(age);
			dest.setMemberType(memberType);
			return new Member[]{ source, dest };
		}
	}
	
	public static class ChainBuilder{
		private final int subscriber;
		private final int referrer;
		
		public ChainBuilder(int subscriber, int referrer){
			this.subscriber = subscriber;
			this.referrer = referrer;
		}
		
		public ChainBuilder(Member subscriber, Member referrer){
			this(subscriber.getId(), referrer.getId());
		}
		
		public int getSubscriber(){
			return this.subscriber;
		}
		
		public int getReferrer(){
			return this.referrer;
		}
	}
	
	public static class UpgradeBuilder{
		private final int memberId;
		private String wxServer;
		
		public UpgradeBuilder(int memberId){
			this.memberId = memberId;
		}
		
		public UpgradeBuilder(Member member){
			this.memberId = member.getId();
		}
		
		public UpgradeBuilder setWxServer(String wxServer){
			this.wxServer = wxServer;
			return this;
		}
		
		public String getWxServer(){
			return this.wxServer;
		}
		
		public int getMemberId(){
			return this.memberId;
		}
	}
	
	public static class ConsumeBuilder{
		private final int memberId;
		private float consumePrice;
		private float extraPrice;
		private PayType payType;
		private final int orderId;
		
		public ConsumeBuilder(Member member, Order order){
			this.memberId = member.getId();
			this.orderId = order.getId();
		}
		
		public ConsumeBuilder(int memberId, int orderId){
			this.memberId = memberId;
			this.orderId = orderId;
		}
		
		public ConsumeBuilder setPrice(PayType payType, float consumePrice, float extraPrice){
			this.payType = payType;
			this.consumePrice = consumePrice;
			this.extraPrice = extraPrice;
			return this;
		}

		public ConsumeBuilder setPrice(PayType payType, float consumePrice){
			return setPrice(payType, consumePrice, 0);
		}
		
		public int getMemberId(){
			return this.memberId;
		}
		
		public float getConsumePrice(){
			return this.consumePrice;
		}
		
		public float getExtraPrice(){
			return this.extraPrice;
		}
		
		public int getOrderId(){
			return this.orderId;
		}
		
		public PayType getPayType(){
			return this.payType;
		}
	}
	
	private int id;
	private int restaurantId;
	private String restaurantName;      //操作门店
	private int branchId;
	private float totalCommission;		// 佣金总额
	private float totalConsumption;		// 消费总额
	private int totalPoint;				// 累计积分
	private float totalCharge;			// 累计充值额
	private float totalRefund;          // 累计取款
	private float usedBalance;			// 累计会员余额消费
	private int consumptionAmount;		// 累计消费次数
	private long lastConsumption;		// 最近一次消费时间
	private float baseBalance;			// 基础余额
	private float extraBalance;			// 额外余额
	private int usedPoint;				// 累计使用积分
	private int point;					// 当前积分
	
	private String name;				// 客户名称
	private Sex sex = Sex.MALE;			// 性别
	private String tele;				// 电话
	private String mobile;				// 手机
	private Age age;					// 年龄段
	private long birthday;				// 生日
	private String idCard;				// 身份证
	private String company;				// 公司
	private String contactAddress;		// 联系地址
	private long createDate;			// 创建时间
	private MemberType memberType;		// 会员类型
	private String memberCard;			// 会员卡号
	private String referrer;			// 推荐人
	private int referrerId;				// 推荐人Id
	private int wxOrderAmount;			// 微信订单数量
	
	private WxMember weixin;			// 微信信息
	//Ta喜欢的菜品
	private final List<Food> favorFoods = new ArrayList<Food>();
	//向Ta推荐的菜品
	private final List<Food> recommendFoods = new ArrayList<Food>();
	//【我要代言】的粉丝数
	private int fansAmount;
	
	
	private Member(){
		
	}
	
	public Member(int id){
		this.id = id;
	}
	
	private Member(UpdateBuilder builder){
		
		setId(builder.memberId);
		
		if(builder.isNameChanged()){
			setName(builder.name);
		}
		if(builder.isSexChanged()){
			setSex(builder.sex);
		}
		if(builder.isMobileChanged()){
			setMobile(builder.mobile);
		}
		if(builder.isBirthdayChanged()){
			setBirthday(builder.birthday);
		}
		if(builder.isAgeChanged()){
			setAge(builder.age);
		}
		if(builder.isIdChardChanged()){
			setIdCard(builder.idCard);
		}
		if(builder.isCompanyChanged()){
			setCompany(builder.company);
		}
		if(builder.isContactAddrChanged()){
			setContactAddress(builder.contactAddress);
		}
		if(builder.isMemberTypeChanged()){
			setMemberType(new MemberType(builder.memberTypeId));
		}
		if(builder.isMemberCardChanged()){
			setMemberCard(builder.memberCard);
		}
		if(builder.isReferrerChanged()){
			setReferrerId(builder.referrerId);
		}
		if(builder.isWxOrderAmountChanged()){
			setWxOrderAmount(builder.wxOrderAmount);
		}
	}
	
	/**
	 * Check to see whether the balance of member account is enough for consumption in case of paid by member.
	 * @param consumePrice
	 * 			the amount to consume price
	 * @param payType
	 * 			the payment type referred to {@link PayType}
	 * @throws BusinessException
	 *          throws if any cases below
	 *          <li>the consume price exceeds total balance to this member account
	 *          <li>the point member is NOT allowed to pay by balance   
	 */
	public void checkConsume(float consumePrice, PayType payType) throws BusinessException{
		if(memberType.isPoint() && payType.equals(PayType.MEMBER)){
			throw new BusinessException("【积分会员】不能使用【会员卡】结账");
			
		}else if(memberType.isCharge() && payType.equals(PayType.MEMBER)){
			checkBalance(consumePrice);
		}
	}
	
	private void checkBalance(float consumePrice) throws BusinessException{
		if(getTotalBalance() < consumePrice){
			//Check to see whether the balance of member account is enough or NOT in case of unpaid.
			throw new BusinessException(MemberError.EXCEED_BALANCE);
		}
	}
	
	/**
	 * Restore the member account according to last consumption.
	 * @param lastConsumption
	 * 			the member last consumption
	 * @return the member operation to account restore
	 * @throws BusinessException
	 *             throws if the consume price exceeds total balance to this member account
	 */
	public MemberOperation restore(MemberOperation lastConsumption) throws BusinessException{
		//还原结账前的账户状态
		baseBalance += lastConsumption.getDeltaBaseMoney();
		extraBalance += lastConsumption.getDeltaExtraMoney();
		usedBalance -= lastConsumption.getDeltaTotalMoney();
		consumptionAmount--;
		totalConsumption -= lastConsumption.getPayMoney();
		point -= lastConsumption.getDeltaPoint();
		totalPoint -= lastConsumption.getDeltaPoint();
		
		//重新执行会员结账, 生成member operation
		MemberOperation mo = consume(0, PayType.MEMBER);
		mo.setOperationType(OperationType.RE_CONSUME_RESTORE);
		
		return mo;
	}
	
	/**
	 * Perform the re-consumption operation to this member account in case of paid by member.
	 * 
	 * @param consumePrice
	 *            the amount to consume price
	 * @param payType
	 * 			  the pay type referred to {@link PayType}
	 * @param lastConsumption
	 * 			  the last consumption used to restore the account
	 * @return the member operation to this consumption
	 * @throws BusinessException
	 *             throws if the consume price exceeds total balance to this member account
	 */
	public MemberOperation reConsume(float consumePrice, PayType payType) throws BusinessException{
		//重新执行会员结账
		MemberOperation mo = consume(consumePrice, payType);
		mo.setOperationType(OperationType.RE_CONSUME);
		return mo;
	}
	
	/**
	 * Perform the consumption operation to this member account in case of paid by member.
	 * 
	 * @param consumePrice
	 *            the amount to consume price
	 * @param coupon
	 * 			the coupon to use, null means not use coupon
	 * @param payType
	 * 			  	the pay type referred to {@link PayType}
	 * @return the member operation to this consumption
	 * @throws BusinessException
	 *             throws if the consume price exceeds total balance to this member account
	 */
	public MemberOperation consume(final float consumePrice, final PayType payType) throws BusinessException{

		return consume(consumePrice, 0, payType);
//		float deltaBase = 0, deltaExtra = 0;
//		if(payType.equals(PayType.MEMBER)){
//			//检查余额是否充足
//			checkBalance(consumePrice);
//
//			/*
//			 * 计算账户余额。
//			 * 先扣除基础账户中的余额，再扣除赠送账户中的余额
//			 */
//			if(baseBalance < consumePrice){
//				deltaBase = baseBalance;
//				deltaExtra = consumePrice - baseBalance;
//				
//				baseBalance = 0;
//				extraBalance = extraBalance - deltaExtra;
//				
//			}else{
//				deltaBase = consumePrice;
//				deltaExtra = 0;
//				baseBalance = baseBalance - consumePrice;
//			}
//			
//			//累计消费金额
//			usedBalance += consumePrice;
//			
//		}
//		
//		//累计消费次数
//		consumptionAmount++;
//		
//		//累计消费额
//		totalConsumption += consumePrice;
//		
//		//累计会员当前可使用的积分
//		int deltaPoint = Math.round(consumePrice * this.getMemberType().getExchangeRate());
//		point += deltaPoint;
//		
//		//累计从消费兑换而增加的积分
//		totalPoint += deltaPoint;
//		
//		MemberOperation mo = MemberOperation.newMO(getId(), getName(), getMobile(), getMemberCard());
//		mo.setOperationType(OperationType.CONSUME);
//		mo.setPayType(payType);
//		mo.setPayMoney(consumePrice);
//		mo.setDeltaBaseMoney(deltaBase);
//		mo.setRemainingBaseMoney(baseBalance);
//		mo.setDeltaExtraMoney(deltaExtra);
//		mo.setRemainingExtraMoney(extraBalance);
//		mo.setDeltaPoint(deltaPoint);
//		mo.setRemainingPoint(point);
//		
//		return mo;
	}
	
	/**
	 * Perform the consumption operation to this member.
	 * @param consumePrice
	 * 			the consumption price
	 * @param extraPrice
	 * 			the extra price to use
	 * @param payType
	 * 			the pay type
	 * @return the member operation to this consumption
	 * @throws BusinessException
	 * 			throws if any cases below
	 * 			<p>会员余额（从赠送账户扣减固定金额）
	 * 			<li>付款方式不是【会员余额】
	 * 			<li>基础账户余额不足
	 * 			<li>赠送账户余额不足
	 * 			<p>会员余额
	 * 			<li>账户余额不足
	 */
	public MemberOperation consume(final float consumePrice, final float extraPrice, final PayType payType) throws BusinessException{

		float deltaBase = 0, deltaExtra = 0;
		if(extraPrice > 0){
			//扣减赠送账户的情况必须要检查以下内容
			
			//付款方式必须是【会员余额】
			if(!payType.equals(PayType.MEMBER)){
				throw new BusinessException("付款方式必须是【会员余额】");
			}
			
			//总消费额必须小于扣减的赠送金额
			if(consumePrice < extraPrice){
				throw new BusinessException("总消费额不能小于扣减的赠送金额");
			}
			
			//赠送账户余额必须大于扣减的赠送金额
			if(this.extraBalance < extraPrice){
				throw new BusinessException("对不起, 你的赠送账户余额不足");
			}
			
			//基础账户余额必须大于扣减的基础金额
			if(this.baseBalance < consumePrice - extraPrice){
				throw new BusinessException("对不起, 你的基础账户余额不足");
			}
			
			//计算赠送账户的余额
			deltaExtra = extraPrice;
			this.extraBalance = this.extraBalance - deltaExtra;
			
			//计算接触账户的余额
			deltaBase = consumePrice - extraPrice;
			this.baseBalance = this.baseBalance - deltaBase;
			
		}else if(payType.equals(PayType.MEMBER)){
			//检查账户余额是否充足
			if(getTotalBalance() < consumePrice){
				throw new BusinessException("对不起, 你的账户余额不足");
			}
			
			/*
			 * 计算账户余额。
			 * 先扣除基础账户中的余额，再扣除赠送账户中的余额
			 */
			if(baseBalance < consumePrice){
				deltaBase = baseBalance;
				deltaExtra = consumePrice - baseBalance;
				
				baseBalance = 0;
				extraBalance = extraBalance - deltaExtra;
				
			}else{
				deltaBase = consumePrice;
				deltaExtra = 0;
				baseBalance = baseBalance - consumePrice;
			}
		}
		
		//累计消费金额
		usedBalance += consumePrice;
		
		//累计消费次数
		consumptionAmount++;
		
		//累计消费额
		totalConsumption += consumePrice;
		
		//累计会员当前可使用的积分
		int deltaPoint = Math.round(consumePrice * this.getMemberType().getExchangeRate());
		point += deltaPoint;
		
		//累计从消费兑换而增加的积分
		totalPoint += deltaPoint;
		
		MemberOperation mo = MemberOperation.newMO(getId(), getName(), getMobile(), getMemberCard());
		mo.setOperationType(OperationType.CONSUME);
		mo.setPayType(payType);
		mo.setPayMoney(consumePrice);
		mo.setDeltaBaseMoney(deltaBase);
		mo.setRemainingBaseMoney(baseBalance);
		mo.setDeltaExtraMoney(deltaExtra);
		mo.setRemainingExtraMoney(extraBalance);
		mo.setDeltaPoint(deltaPoint);
		mo.setRemainingPoint(point);
		
		return mo;
	}
	
	/**
	 * Recommend the member in case of represent.
	 * @param represent
	 * 			the represent associated with this member
	 * @return the member operation including both subscribe money & point
	 */
	public MemberOperation[] recommend(Represent represent){
		List<MemberOperation> result = new ArrayList<MemberOperation>();
		
		if(represent.getRecommentMoney() > 0){
			MemberOperation mo = MemberOperation.newMO(getId(), getName(), getMobile(), getMemberCard());
			mo.setOperationType(OperationType.CHARGE);
			mo.setChargeMoney(represent.getRecommentMoney());
			mo.setChargeType(ChargeType.RECOMMEND);
			
			this.totalCharge += represent.getRecommentMoney();
			this.extraBalance += represent.getRecommentMoney();
			
			mo.setDeltaBaseMoney(0);
			mo.setDeltaExtraMoney(represent.getRecommentMoney());
			
			mo.setRemainingBaseMoney(this.baseBalance);
			mo.setRemainingExtraMoney(this.extraBalance);
			mo.setRemainingPoint(this.point);
			
			result.add(mo);
		}
		
		if(represent.getRecommendPoint() > 0){
			
			this.point += represent.getRecommendPoint();
			this.totalPoint += represent.getRecommendPoint();
			MemberOperation mo = MemberOperation.newMO(getId(), getName(), getMobile(), getMemberCard());
			mo.setOperationType(OperationType.POINT_RECOMMEND);
			
			mo.setDeltaPoint(represent.getRecommendPoint());
			mo.setRemainingPoint(point);
			
			result.add(mo);
		}

		
		return result.toArray(new MemberOperation[result.size()]);
	}
	
	/**
	 * Subscribe the weixin in case of represent.
	 * @param represent
	 * 			the represent associated with this member
	 * @return the member operation including both subscribe money & point
	 */
	public MemberOperation[] subscribe(Represent represent){

		List<MemberOperation> result = new ArrayList<MemberOperation>();
		
		if(represent.getSubscribeMoney() > 0){
			MemberOperation mo = MemberOperation.newMO(getId(), getName(), getMobile(), getMemberCard());
			mo.setOperationType(OperationType.CHARGE);
			mo.setChargeMoney(represent.getSubscribeMoney());
			mo.setChargeType(ChargeType.SUBSCRIBE);
			
			this.totalCharge += represent.getSubscribeMoney();
			this.extraBalance += represent.getSubscribeMoney();
			
			mo.setDeltaBaseMoney(0);
			mo.setDeltaExtraMoney(represent.getSubscribeMoney());
			
			mo.setRemainingBaseMoney(this.baseBalance);
			mo.setRemainingExtraMoney(this.extraBalance);
			mo.setRemainingPoint(this.point);
			
			result.add(mo);
		}
		
		if(represent.getSubscribePoint() > 0){
			
			this.point += represent.getSubscribePoint();
			this.totalPoint += represent.getSubscribePoint();
			
			MemberOperation mo = MemberOperation.newMO(getId(), getName(), getMobile(), getMemberCard());
			mo.setOperationType(OperationType.POINT_SUBSCRIBE);
			
			mo.setDeltaPoint(represent.getSubscribePoint());
			mo.setRemainingPoint(point);
			
			result.add(mo);
		}

		
		return result.toArray(new MemberOperation[result.size()]);
	}
	
	/**
	 * Perform the charge operation according the amount of charge money and charge type.
	 * @param chargeMoney
	 * 			the amount of charge money
	 * @param chargeType
	 * 			the charge type referred to {@link ChargeType}
	 * @return the member operation to this charge
	 */
	public MemberOperation charge(float chargeMoney, float accountMoney, ChargeType chargeType){
		
		MemberOperation mo = MemberOperation.newMO(getId(), getName(), getMobile(), getMemberCard());
		
		mo.setOperationType(OperationType.CHARGE);
		mo.setChargeMoney(chargeMoney);
		mo.setChargeType(chargeType);
		
		//float deltaBase = chargeMoney;
		//float deltaExtra = chargeMoney * Math.abs(getMemberType().getChargeRate() - 1);

		this.totalCharge += chargeMoney;
		this.baseBalance += chargeMoney;
		this.extraBalance += (accountMoney - chargeMoney);
		
		if(chargeType == ChargeType.COMMISSION){
			this.totalCommission += (accountMoney - chargeMoney);
		}
		
		mo.setDeltaBaseMoney(chargeMoney);
		mo.setDeltaExtraMoney(accountMoney - chargeMoney);
		
		mo.setRemainingBaseMoney(this.baseBalance);
		mo.setRemainingExtraMoney(this.extraBalance);
		mo.setRemainingPoint(this.point);
		
		return mo;
	}
	
	/**
	 * Refund from the member balance.
	 * @param refundMoney
	 * 			the money refund to member
	 * @param accountMoney
	 * 			the money refund from account
	 * @return the associated member operation
	 * @throws BusinessException
	 * 			throws if the refund money exceeds the base balance
	 */
	public MemberOperation refund(float refundMoney, float accountMoney) throws BusinessException{
		if(refundMoney > this.baseBalance){
			throw new BusinessException("取款金额不能多于基础账户的余额", MemberError.REFUND_FAIL);
		}
		
		MemberOperation mo = MemberOperation.newMO(getId(), getName(), getMobile(), getMemberCard());
		
		mo.setOperationType(OperationType.REFUND);
		mo.setChargeType(ChargeType.CASH);
		mo.setChargeMoney(-refundMoney);
		
		//float deltaBase = takeMoney;
		//float deltaExtra = takeMoney * Math.abs(getMemberType().getChargeRate() - 1);
		this.totalRefund += refundMoney;
		this.baseBalance -= refundMoney;
		this.extraBalance -= (accountMoney - refundMoney);
		
		
		mo.setDeltaBaseMoney(-refundMoney);
		mo.setDeltaExtraMoney(-(accountMoney-refundMoney));
		
		mo.setRemainingBaseMoney(this.baseBalance);
		mo.setRemainingExtraMoney(this.extraBalance);
		mo.setRemainingPoint(this.point);
		
		return mo;
	}
	
	/**
	 * Perform to point consumption.
	 * @param pointToConsume how many points to consume
	 * @return the member operation to this point consumption
	 * @throws BusinessException
	 * 			throws if point to consume exceeds the remaining
	 */
	public MemberOperation pointConsume(int pointToConsume) throws BusinessException{
		
		if(pointToConsume > point){
			throw new BusinessException(MemberError.EXCEED_POINT);
		}
		
		MemberOperation mo = MemberOperation.newMO(getId(), getName(), getMobile(), getMemberCard());
		mo.setOperationType(OperationType.POINT_CONSUME);

		point = point - pointToConsume;
		usedPoint += pointToConsume;
		
		mo.setDeltaPoint(-pointToConsume);
		mo.setRemainingPoint(point);
		
		return mo;
	}
	
	/**
	 * Adjust the remaining point.
	 * @param deltaPoint the delta point
	 * @return the member operation to this point adjustment
	 * @throws BusinessException
	 * 			throws if exceed remaining point
	 */
	public MemberOperation adjustPoint(int deltaPoint, AdjustType adjustType) throws BusinessException{

		if(adjustType == AdjustType.INCREASE){
			deltaPoint = Math.abs(deltaPoint);
			point = point + deltaPoint;
			//累计从消费兑换而增加的积分
			totalPoint += deltaPoint;
			
		}else if(adjustType == AdjustType.REDUCE){
			deltaPoint = -Math.abs(deltaPoint);
			point = point + deltaPoint;
			//累计从消费兑换而增加的积分
			totalPoint -= Math.abs(deltaPoint);
			
		}else if(adjustType == AdjustType.SET){
			int prePoint = point;
			point = Math.abs(deltaPoint);
			deltaPoint = prePoint - point;
		}
		
		if(point < 0){
			throw new BusinessException(MemberError.EXCEED_POINT);
		}
		
		
		MemberOperation mo = MemberOperation.newMO(getId(), getName(), getMobile(), getMemberCard());
		mo.setOperationType(OperationType.POINT_ADJUST);
		
		mo.setDeltaPoint(deltaPoint);
		mo.setRemainingPoint(point);
		
		return mo;
	}
	
	/**
	 * Adjust the remaining balance.
	 * @param deltaBalance the delta charge money
	 * @return the member operation to this balance adjustment
	 */
	public MemberOperation adjustBalance(float deltaBalance){
		MemberOperation mo = MemberOperation.newMO(getId(), getName(), getMobile(), getMemberCard());
		mo.setOperationType(OperationType.BALANCE_ADJUST);
		
		float deltaBase;
		float deltaExtra;
		
		float newBaseBalance = baseBalance - deltaBalance;
		if(newBaseBalance > 0){
			baseBalance = newBaseBalance;
			deltaBase = deltaBalance;
			deltaExtra = 0;
			
		}else{
			baseBalance = 0;
			deltaBase = baseBalance;
			
			deltaExtra = Math.abs(newBaseBalance);
			float newExtraBalance = extraBalance - deltaExtra;
			if(newExtraBalance < 0){
				deltaExtra = extraBalance;
				extraBalance = 0;
			}else{
				extraBalance = newExtraBalance;
			}
		}
		
		mo.setDeltaBaseMoney(deltaBase);
		mo.setDeltaExtraMoney(deltaExtra);
		mo.setRemainingBaseMoney(baseBalance);
		mo.setRemainingExtraMoney(extraBalance);
		
		return mo;
	}
	
	
	
	@Override
	public int hashCode(){
		return 17 * 31 + this.id;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Member)){
			return false;
		}else{
			return this.id == ((Member)obj).id;
		}
	}
	
	@Override
	public String toString(){
		return "member(id = " + getId() + ", name = " + getName() + ")";
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == MEMBER_PARCELABLE_SIMPLE){
			dest.writeInt(this.getId());
			dest.writeString(this.getName());
			dest.writeString(this.getMobile());
			dest.writeString(this.getMemberCard());
			dest.writeInt(this.getConsumptionAmount());
			dest.writeParcel(this.memberType, MemberType.MEMBER_TYPE_PARCELABLE_SIMPLE);
			
		}else if(flag == MEMBER_PARCELABLE_COMPLEX){
			dest.writeParcel(this.memberType, MemberType.MEMBER_TYPE_PARCELABLE_COMPLEX);
			dest.writeInt(this.getId());
			dest.writeString(this.getName());
			dest.writeString(this.getMobile());
			dest.writeString(this.getMemberCard());
			dest.writeInt(this.getConsumptionAmount());
			dest.writeLong(this.getLastConsumption());
			dest.writeParcelList(this.favorFoods, Food.FOOD_PARCELABLE_SIMPLE);
			dest.writeParcelList(this.recommendFoods, Food.FOOD_PARCELABLE_SIMPLE);
		}
		
	}

	@Override
	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		if(flag == MEMBER_PARCELABLE_SIMPLE){
			setId(source.readInt());
			setName(source.readString());
			setMobile(source.readString());
			setMemberCard(source.readString());
			setConsumptionAmount(source.readInt());
			setMemberType(source.readParcel(MemberType.CREATOR));
			
		}else if(flag == MEMBER_PARCELABLE_COMPLEX){
			setMemberType(source.readParcel(MemberType.CREATOR));
			setId(source.readInt());
			setName(source.readString());
			setMobile(source.readString());
			setMemberCard(source.readString());
			setConsumptionAmount(source.readInt());
			setLastConsumption(source.readLong());
			setFavorFoods(source.readParcelList(Food.CREATOR));
			setRecommendFoods(source.readParcelList(Food.CREATOR));
		}
	}
	
	public final static Parcelable.Creator<Member> CREATOR = new Parcelable.Creator<Member>() {
		
		public Member[] newInstance(int size) {
			return new Member[size];
		}
		
		public Member newInstance() {
			return new Member();
		}
	};

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.id);
		jm.putInt("rid", this.restaurantId);
		jm.putString("restaurantName", this.restaurantName);
		jm.putInt("branchId", this.branchId);
		jm.putFloat("totalCharge", this.totalCharge);
		jm.putFloat("totalRefund", this.totalRefund);
		jm.putInt("totalPoint", this.totalPoint);
		jm.putFloat("totalConsumption", this.totalConsumption);
		jm.putFloat("totalCommission", this.totalCommission);
		jm.putInt("consumptionAmount", this.consumptionAmount);
		jm.putString("lastConsumption", DateUtil.format(this.getLastConsumption()));
		jm.putFloat("baseBalance", this.baseBalance);
		jm.putFloat("extraBalance", this.extraBalance);
		jm.putFloat("totalBalance", this.getTotalBalance());
		jm.putFloat("usedBalance", this.usedBalance);
		jm.putInt("fansAmount", this.fansAmount);
		jm.putInt("point", this.point);
		jm.putInt("usedPoint", this.usedPoint);
		if(this.sex != null){
			jm.putString("sexText", this.sex.getDesc());
			jm.putInt("sexValue", this.sex.getVal());
		}
		jm.putJsonable("memberType", this.memberType, 0);
		jm.putString("name", this.name);
		jm.putString("tele", this.tele);
		jm.putString("mobile", this.getMobile());
		if(this.age != null){
			jm.putInt("ageVal", this.age.val);
			jm.putString("ageText", this.age.desc);
		}
		jm.putString("birthday", DateUtil.format(this.birthday, DateUtil.Pattern.DATE_TIME));
		jm.putString("birthdayFormat", this.birthday != 0 ? DateUtil.format(this.birthday, DateUtil.Pattern.DAY) : "");
		jm.putString("idCard", this.idCard);
		jm.putString("weixinCard", this.weixin != null?this.weixin.getCard() + "":"");
		jm.putString("company", this.company);
		jm.putString("contactAddress", this.contactAddress);
		jm.putLong("createDate", this.createDate);
		jm.putString("createDateFormat", DateUtil.format(this.createDate, Pattern.DATE.getPattern()));
		jm.putString("memberCard", this.memberCard);
		jm.putBoolean("isRaw", this.isRaw());
		jm.putString("referrer", this.getReferrer());
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
	public float getTotalCommission(){
		return this.totalCommission;
	}
	
	public void setTotalCommission(float totalCommission){
		this.totalCommission = totalCommission;
	}
	
	public float getTotalBalance(){
		return this.baseBalance + this.extraBalance;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getRestaurantName() {
		return restaurantName;
	}

	public void setRestaurantName(String restaurantName) {
		this.restaurantName = restaurantName;
	}

	public int getRestaurantId() {
		return restaurantId;
	}
	
	public void setRestaurantId(int restaurantId) {
		this.restaurantId = restaurantId;
	}
	
	public void setBranchId(int branchId){
		this.branchId = branchId;
	}
	
	public int getBranchId(){
		return this.branchId;
	}
	
	public float getBaseBalance() {
		return baseBalance;
	}
	
	public void setBaseBalance(float baseBalance) {
		this.baseBalance = baseBalance;
	}
	
	public float getExtraBalance() {
		return extraBalance;
	}
	
	public void setExtraBalance(float extraBalance) {
		this.extraBalance = extraBalance;
	}
	
	public int getPoint() {
		return point;
	}
	
	public void setPoint(int point) {
		this.point = point;
	}
	
	public MemberType getMemberType() {
		return memberType;
	}
	
	public void setMemberType(MemberType memberType) {
		if(memberType != null){
			if(this.memberType == null){
				this.memberType = new MemberType(0);
			}
			this.memberType.copyFrom(memberType);
		}
	}
	
	public boolean hasMemberCard(){
		return getMemberCard().length() != 0;
	}
	
	public String getMemberCard() {
		if(memberCard == null){
			return "";
		}
		return memberCard;
	}
	
	public void setMemberCard(String memberCard) {
		this.memberCard = memberCard.replaceFirst("^(0+)", "");
	}
	
	public void setWeixin(WxMember weixin){
		this.weixin = weixin;
	}
	
	public WxMember getWeixin(){
		return this.weixin;
	}
	
	public boolean hasWeixin(){
		return this.weixin != null;
	}
	
	public String getName() {
		if(name == null){
			name = "";
		}
		return name.trim();
	}
	
	public void setName(String name) {
		this.name = name.trim();
	}
	
	public Sex getSex() {
		return sex;
	}
	
	public void setSex(Sex sex){
		this.sex = sex;
	}
	
	public void setSex(int sexVal) {
		this.sex = Sex.valueOf(sexVal);
	}
	
	public String getTele() {
		if(tele == null){
			return "";
		}
		return tele;
	}
	
	public void setTele(String tele) {
		this.tele = tele;
	}
	
	public boolean isRaw(){
		return getMobile().trim().length() == 0 && getMemberCard().trim().length() == 0;
	}
	
	public String getMobile() {
		if(mobile == null){
			return "";
		}
		return mobile;
	}
	
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	
	public boolean hasMobile(){
		return this.getMobile().trim().length() != 0;
	}
	
	public Age getAge(){
		return this.age;
	}
	
	public void setAge(Age age){
		this.age = age;
	}
	
	public long getBirthday() {
		return birthday;
	}
	
	public void setBirthday(long birthday) {
		this.birthday = birthday;
	}
	
	public String getIdCard() {
		if(idCard == null){
			return "";
		}
		return idCard;
	}
	
	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}
	
	public String getCompany() {
		if(company == null){
			return "";
		}
		return company;
	}
	
	public void setCompany(String company) {
		this.company = company;
	}
	
	public void setFansAmount(int fansAmount){
		this.fansAmount = fansAmount;
	}
	
	public int getFansAmount(){
		return this.fansAmount;
	}
	
	public String getContactAddress() {
		if(contactAddress == null){
			return "";
		}
		return contactAddress;
	}
	
	public void setContactAddress(String contactAddress) {
		this.contactAddress = contactAddress;
	}
	
	public long getCreateDate() {
		return createDate;
	}
	
	public void setCreateDate(long createDate) {
		this.createDate = createDate;
	}
	
	public float getUsedBalance(){
		return this.usedBalance;
	}
	
	public void setUsedBalance(float usedBalance){
		this.usedBalance = usedBalance;
	}
	
	public int getUsedPoint(){
		return this.usedPoint;
	}
	
	public void setUsedPoint(int usedPoint){
		this.usedPoint = usedPoint;
	}
	
	public int getConsumptionAmount() {
		return consumptionAmount;
	}
	
	public void setConsumptionAmount(int consumptionAmount) {
		this.consumptionAmount = consumptionAmount;
	}

	public long getLastConsumption(){
		return this.lastConsumption;
	}
	
	public void setLastConsumption(long lastConsumption){
		this.lastConsumption = lastConsumption;
	}
	
	public float getTotalConsumption() {
		return totalConsumption;
	}

	public void setTotalConsumption(float totalConsumption) {
		this.totalConsumption = totalConsumption;
	}

	public int getTotalPoint() {
		return totalPoint;
	}

	public void setTotalPoint(int totalPoint) {
		this.totalPoint = totalPoint;
	}

	public float getTotalCharge() {
		return totalCharge;
	}

	public void setTotalCharge(float totalCharge) {
		this.totalCharge = totalCharge;
	}
	
	public float getTotalRefund(){
		return totalRefund;
	}
	
	public void setTotalRefund(float totalRefund){
		this.totalRefund = totalRefund;
	}
	
	public void setReferrerId(int staffId){
		this.referrerId = staffId;
	}
	
	public int getReferrerId(){
		return this.referrerId;
	}
	
	public void setReferrer(String staff){
		this.referrer = staff;
	}
	
	public String getReferrer(){
		if(this.referrer == null){
			return "";
		}
		return this.referrer;
	}
	
	public boolean hasReferrer(){
		return this.referrerId != 0;
	}
	
	public void setWxOrderAmount(int wxOrderAmount){
		this.wxOrderAmount = wxOrderAmount;
	}

	public int getWxOrderAmount(){
		return this.wxOrderAmount;
	}
	
	public List<Food> getFavorFoods(){
		return Collections.unmodifiableList(this.favorFoods);
	}
	
	public void setFavorFoods(List<Food> favorFoods){
		if(favorFoods != null){
			this.favorFoods.clear();
			this.favorFoods.addAll(favorFoods);
		}
	}
	
	public void addFavorFood(Food favorFood){
		if(favorFood != null){
			favorFoods.add(favorFood);
		}
	}
	
	public List<Food> getRecommendFoods(){
		return Collections.unmodifiableList(this.recommendFoods);
	}
	
	public void addRecommendFood(Food recommendFood){
		if(recommendFood != null){
			this.recommendFoods.add(recommendFood);
		}
	}
	
	public void setRecommendFoods(List<Food> recommendFoods){
		if(recommendFoods != null){
			this.recommendFoods.clear();
			this.recommendFoods.addAll(recommendFoods);
		}
	}

	@Override
	public int compareTo(Member arg0) {
		if(getId() > arg0.getId()){
			return 1;
		}else if(getId() < arg0.getId()){
			return -1;
		}else{
			return 0;
		}
	}
	
}
