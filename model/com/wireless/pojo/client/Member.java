package com.wireless.pojo.client;

import java.util.Date;

import com.wireless.exception.BusinessException;
import com.wireless.exception.ProtocolError;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;
import com.wireless.pojo.client.MemberOperation.ChargeType;
import com.wireless.pojo.client.MemberOperation.OperationType;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.util.DateUtil;

public class Member implements Parcelable{
	
	public static final int MEMBER_PARCELABLE_SIMPLE = 0;
	public static final int MEMBER_PARCELABLE_COMPLEX = 1;
	
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
	
	public static enum Status{
		
		NORMAL(0, "normal"),
		DISABLED(1, "disable");
		
		private final int val;
		private final String desc;
		private Status(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		@Override
		public String toString(){
			return "member status : invalid(val = " + val + ",desc = " + desc + ")";
		}
		
		public static Status valueOf(int val){
			for(Status status : values()){
				if(status.val == val){
					return status;
				}
			}
			
			throw new IllegalArgumentException("The member status(val = " + val + ") passed is invalid.");
		}
		
		public int getVal(){
			return this.val;
		}
		
		public String getDesc(){
			return this.desc;
		}
	}
	
	public static class InsertBuilder{
		private final int restaurantId;			// 餐厅编号
		private final String name;				// 客户名称
		private Sex sex = Sex.MALE;				// 性别
		private String tele;					// 电话
		private final String mobile;			// 手机
		private long birthday;					// 生日
		private String idCard;					// 身份证
		private String company;					// 公司
		private String tastePref;				// 口味
		private String taboo;					// 忌讳
		private String contactAddress;			// 联系地址
		private String comment;					// 备注
		private final int memberTypeId;			// 会员类型
		private String memberCard;				// 会员卡号
		
		public InsertBuilder(int restaurantId, String name, String mobile, int memberTypeId){
			this.restaurantId = restaurantId;
			this.name = name;
			this.mobile = mobile;
			this.memberTypeId = memberTypeId;
		}
		
		public InsertBuilder setSex(Sex sex){
			this.sex = sex;
			return this;
		}
		
		public InsertBuilder setTele(String tele){
			this.tele = tele;
			return this;
		}
		
		public InsertBuilder setBirthday(long birthday){
			this.birthday = birthday;
			return this;
		}
		
		public InsertBuilder setIdCard(String idCard){
			this.idCard = idCard;
			return this;
		}
		
		public InsertBuilder setCompany(String company){
			this.company = company;
			return this;
		}
		
		public InsertBuilder setTastePref(String tastePref){
			this.tastePref = tastePref;
			return this;
		}
		
		public InsertBuilder setTaboo(String taboo){
			this.taboo = taboo;
			return this;
		}
		
		public InsertBuilder setContactAddr(String addr){
			this.contactAddress = addr;
			return this;
		}
		
		public InsertBuilder setMemberCard(String card){
			this.memberCard = card;
			return this;
		}
		
		public Member build(){
			return new Member(this);
		}
	}
	
	public static class UpdateBuilder{
		private final int memberId;				// 会员编号
		private final String name;				// 客户名称
		private Sex sex = Sex.MALE;				// 性别
		private String tele;					// 电话
		private final String mobile;			// 手机
		private long birthday;					// 生日
		private String idCard;					// 身份证
		private String company;					// 公司
		private String tastePref;				// 口味
		private String taboo;					// 忌讳
		private String contactAddress;			// 联系地址
		private String comment;					// 备注
		private final int memberTypeId;			// 会员类型
		private String memberCard;				// 会员卡号
		
		public UpdateBuilder(int memberId, String name, String mobile, int memberTypeId){
			this.memberId = memberId;
			this.name = name;
			this.mobile = mobile;
			this.memberTypeId = memberTypeId;
		}
		
		public UpdateBuilder setSex(Sex sex){
			this.sex = sex;
			return this;
		}
		
		public UpdateBuilder setTele(String tele){
			this.tele = tele;
			return this;
		}
		
		public UpdateBuilder setBirthday(long birthday){
			this.birthday = birthday;
			return this;
		}
		
		public UpdateBuilder setIdCard(String idCard){
			this.idCard = idCard;
			return this;
		}
		
		public UpdateBuilder setCompany(String company){
			this.company = company;
			return this;
		}
		
		public UpdateBuilder setTastePref(String tastePref){
			this.tastePref = tastePref;
			return this;
		}
		
		public UpdateBuilder setTaboo(String taboo){
			this.taboo = taboo;
			return this;
		}
		
		public UpdateBuilder setContactAddr(String addr){
			this.contactAddress = addr;
			return this;
		}
		
		public UpdateBuilder setMemberCard(String card){
			this.memberCard = card;
			return this;
		}
		
		public Member build(){
			return new Member(this);
		}
	}
	
	private int id;
	private int restaurantId;
	private float baseBalance;
	private float extraBalance;
	private int point;
	private Status status = Status.NORMAL;
	
	private String name;				// 客户名称
	private Sex sex = Sex.MALE;			// 性别
	private String tele;				// 电话
	private String mobile;				// 手机
	private long birthday;				// 生日
	private String idCard;				// 身份证
	private String company;				// 公司
	private String tastePref;			// 口味
	private String taboo;				// 忌讳
	private String contactAddress;		// 联系地址
	private String comment;				// 备注
	private long createDate;			// 创建时间
	private MemberType memberType;		// 会员类型
	private String memberCard;			// 会员卡号
	
	private Member(InsertBuilder builder){
		setRestaurantId(builder.restaurantId);
		setName(builder.name);
		setMobile(builder.mobile);
		setSex(builder.sex);
		setTele(builder.tele);
		setBirthday(builder.birthday);
		setIdCard(builder.idCard);
		setCompany(builder.company);
		setTastePref(builder.tastePref);
		setTaboo(builder.taboo);
		setContactAddress(builder.contactAddress);
		setComment(builder.comment);
		setCreateDate(new Date().getTime());
		setMemberCard(builder.memberCard);
		getMemberType().setTypeID(builder.memberTypeId);
	}
	
	private Member(UpdateBuilder builder){
		setId(builder.memberId);
		setName(builder.name);
		setMobile(builder.mobile);
		setSex(builder.sex);
		setTele(builder.tele);
		setBirthday(builder.birthday);
		setIdCard(builder.idCard);
		setCompany(builder.company);
		setTastePref(builder.tastePref);
		setTaboo(builder.taboo);
		setContactAddress(builder.contactAddress);
		setComment(builder.comment);
		setMemberCard(builder.memberCard);
		getMemberType().setTypeID(builder.memberTypeId);
	}
	
	public Member(){
		
	}
	
	public Member(int id){
		this.id = id;
	}
	
	/**
	 * 
	 * @param id
	 * @param baseBalance
	 * @param extraBalance
	 * @param comment
	 * @return
	 */
	public static Member buildToBalance(int id, float baseBalance, float extraBalance, String comment){
		Member updateBalance = new Member();
		updateBalance.setId(id);
		updateBalance.setBaseBalance(baseBalance);
		updateBalance.setExtraBalance(extraBalance);
		updateBalance.setComment(comment);
		return updateBalance;
	}
	
	/**
	 * 
	 * @param id
	 * @param point
	 * @param staffID
	 * @param comment
	 * @return
	 */
	public static Member buildToPoint(int id, int point, String comment){
		Member updateBalance = new Member();
		updateBalance.setId(id);
		updateBalance.setPoint(point);
		updateBalance.setComment(comment);
		return updateBalance;
	}
	
	/**
	 * Check to see whether the balance of member account is enough for consumption in case of paid by member.
	 * @param consumePrice
	 * 			the amount to consume price
	 * @param payType
	 * 			the payment type referred to {@link Order.PayType}
	 * @throws BusinessException
	 *             throws if the consume price exceeds total balance to this member account
	 */
	public void checkConsume(float consumePrice, Order.PayType payType) throws BusinessException{
		
		if(payType != Order.PayType.MEMBER){
			return;
		}
		
		if(getTotalBalance() < consumePrice){
			//Check to see whether the balance of member account is enough or NOT in case of unpaid.
			throw new BusinessException("The consume price to order exceeds the balance of member account", ProtocolError.EXCEED_MEMBER_BALANCE);
		}
	}
	
	/**
	 * Perform the consumption operation to this member account in case of paid by member.
	 * 
	 * @param consumePrice
	 *            the amount to consume price
	 * @param payType
	 * 			  	the pay type referred to {@link Order.PayType}
	 * @return the member operation to this consumption
	 * @throws BusinessException
	 *             throws if the consume price exceeds total balance to this member account
	 */
	public MemberOperation consume(float consumePrice, Order.PayType payType) throws BusinessException{

		MemberOperation mo = new MemberOperation();
		
		mo.setMemberId(getId());
		mo.setMemberName(getName());
		mo.setMemberMobile(getMobile());
		mo.setMemberCard(getMemberCard());
		mo.setOperationType(OperationType.CONSUME);
		mo.setPayType(payType);
		
		
		if(payType == Order.PayType.MEMBER){
			//使用会员付款时扣除账户余额
			checkConsume(consumePrice, payType);

			mo.setPayMoney(consumePrice);
			
			float deltaBase, deltaExtra;
			
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
			mo.setDeltaBaseMoney(deltaBase);
			mo.setDeltaExtraMoney(deltaExtra);
		}
		
		//累计会员积分
		int deltaPoint = Math.round(consumePrice * getMemberType().getExchangeRate());
		mo.setDeltaPoint(deltaPoint);
		point += deltaPoint;
		
		mo.setRemainingBaseMoney(baseBalance);
		mo.setRemainingExtraMoney(extraBalance);
		mo.setRemainingPoint(point);
		
		return mo;
	}
	
	/**
	 * Perform the charge operation according the amount of charge money and charge type.
	 * @param chargeMoney
	 * 			the amount of charge money
	 * @param chargeType
	 * 			the charge type referred to {@link ChargeType}
	 * @return the member operation to this charge
	 */
	public MemberOperation charge(float chargeMoney, ChargeType chargeType){
		MemberOperation mo = new MemberOperation();
		
		mo.setMemberId(getId());
		mo.setMemberName(getName());
		mo.setMemberMobile(getMobile());
		mo.setMemberCard(getMemberCard());
		mo.setOperationType(OperationType.CHARGE);
		mo.setChargeMoney(chargeMoney);
		mo.setChargeType(chargeType);
		
		float deltaBase = chargeMoney;
		float deltaExtra = chargeMoney * Math.abs(getMemberType().getChargeRate() - 1);

		baseBalance += deltaBase;
		extraBalance += deltaExtra;
		
		mo.setDeltaBaseMoney(deltaBase);
		mo.setDeltaExtraMoney(deltaExtra);
		
		mo.setRemainingBaseMoney(baseBalance);
		mo.setRemainingExtraMoney(extraBalance);
		mo.setRemainingPoint(point);
		
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
		MemberOperation mo = new MemberOperation();

		//TODO
		
		return mo;
	}
	
	/**
	 * Adjust the remaining point.
	 * @param deltaPoint the delta point
	 * @return the member operation to this point adjustment
	 * @throws BusinessException
	 * 			throws if delta point exceeds the remaining
	 */
	public MemberOperation adjustPoint(int deltaPoint) throws BusinessException{
		MemberOperation mo = new MemberOperation();
		
		//TODO
		
		return mo;
	}
	
	/**
	 * Adjust the remaining balance.
	 * @param deltaCharge the delta charge money
	 * @return the member operation to this balance adjustment
	 * @throws BusinessException
	 * 			throws if the delta charge money exceeds the remaining
	 */
	public MemberOperation adjustBalance(float deltaCharge) throws BusinessException{
		MemberOperation mo = new MemberOperation();
		
		//TODO
		
		return mo;
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
	
	public int getRestaurantId() {
		return restaurantId;
	}
	
	public void setRestaurantId(int restaurantId) {
		this.restaurantId = restaurantId;
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
	
	public Status getStatus() {
		return status;
	}
	
	public void setStatus(int statusVal) {
		this.status = Status.valueOf(statusVal);
	}
	
	public void setStatus(Status status){
		this.status = status;
	}
	
	public MemberType getMemberType() {
		if(memberType == null){
			memberType = new MemberType();
		}
		return memberType;
	}
	
	public void setMemberType(MemberType memberType) {
		if(memberType != null){
			this.memberType = memberType;
		}
	}
	
	public boolean hasMemberCard(){
		return getMemberCard().length() != 0;
	}
	
	public String getMemberCard() {
		if(memberCard == null){
			memberCard = "";
		}
		return memberCard;
	}
	
	public void setMemberCard(String memberCard) {
		this.memberCard = memberCard;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
		return tele;
	}
	public void setTele(String tele) {
		this.tele = tele;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public long getBirthday() {
		return birthday;
	}
	
	public void setBirthday(long birthday) {
		this.birthday = birthday;
	}
	
	public void setBirthday(String birthday) {
		if(birthday != null && birthday.trim().length() > 0){
			this.birthday = DateUtil.parseDate(birthday);
		}else{
			this.birthday = 0;
		}
	}
	
	public String getIdCard() {
		if(idCard == null){
			idCard = "";
		}
		return idCard;
	}
	
	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}
	
	public String getCompany() {
		if(company == null){
			company = "";
		}
		return company;
	}
	
	public void setCompany(String company) {
		this.company = company;
	}
	
	public String getTastePref() {
		if(tastePref == null){
			tastePref = "";
		}
		return tastePref;
	}
	
	public void setTastePref(String tastePref) {
		this.tastePref = tastePref;
	}
	
	public String getTaboo() {
		if(taboo == null){
			taboo = "";
		}
		return taboo;
	}
	
	public void setTaboo(String taboo) {
		this.taboo = taboo;
	}
	
	public String getContactAddress() {
		if(contactAddress == null){
			contactAddress = "";
		}
		return contactAddress;
	}
	
	public void setContactAddress(String contactAddress) {
		this.contactAddress = contactAddress;
	}
	
	public String getComment() {
		if(comment == null){
			comment = "";
		}
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public long getCreateDate() {
		return createDate;
	}
	
	public void setCreateDate(long createDate) {
		this.createDate = createDate;
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
			dest.writeInt(this.id);
			
		}else if(flag == MEMBER_PARCELABLE_COMPLEX){
			
		}
		
	}

	@Override
	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		if(flag == MEMBER_PARCELABLE_SIMPLE){
			this.id = source.readInt();
			
		}else if(flag == MEMBER_PARCELABLE_COMPLEX){
			
		}
	}
	
	public final static Parcelable.Creator<Member> MEMBER_CREATOR = new Parcelable.Creator<Member>() {
		
		public Member[] newInstance(int size) {
			return new Member[size];
		}
		
		public Member newInstance() {
			return new Member();
		}
	};
	
}
