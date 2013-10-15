package com.wireless.pojo.client;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.wireless.exception.BusinessException;
import com.wireless.exception.MemberError;
import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;
import com.wireless.pojo.client.MemberOperation.ChargeType;
import com.wireless.pojo.client.MemberOperation.OperationType;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.util.SortedList;

public class Member implements Parcelable, Jsonable, Comparable<Member>{
	
	public static final int MEMBER_PARCELABLE_SIMPLE = 0;
	public static final int MEMBER_PARCELABLE_COMPLEX = 1;
	
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
		private Member data;
		public InsertBuilder(int restaurantId, String name, String mobile, int memberTypeId, Sex sex){
			this.data = new Member();
			this.data.setRestaurantId(restaurantId);
			this.data.setName(name);
			this.data.setMobile(mobile);
			this.data.setMemberType(new MemberType(memberTypeId));
			this.data.setSex(sex);
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
		public InsertBuilder setBirthday(String birthday){
			this.data.setBirthday(birthday);
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
		public InsertBuilder setPrivateComment(String comment){
			this.data.setPrivateComment(MemberComment.newPrivateComment(null, null, comment));
			return this;
		}
		public InsertBuilder setPublicComment(String comment){
			this.data.addPublicComment(MemberComment.newPublicComment(null, null, comment));
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
		public Member build(){
			return this.data;
		}
	}
	
	public static class UpdateBuilder{
		
		private final int memberId;
		private final int restaurantId;
		
		private String name;				// 客户名称
		private Sex sex;					// 性别
		private String tele;				// 电话
		private String mobile;				// 手机
		private long birthday;				// 生日
		private String idCard;				// 身份证
		private String company;				// 公司
		private String contactAddress;		// 联系地址
		private int memberTypeId = -1;		// 会员类型编号
		private String memberCard;			// 会员卡号
		private String privateComment;		// 私人评论
		private String publicComment;		// 公开评论
		
		public UpdateBuilder(int memberId, int restaurantId){
			this.memberId = memberId;
			this.restaurantId = restaurantId;
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
		
		public UpdateBuilder setMemberTypeId(int memberTypeId){
			this.memberTypeId = memberTypeId;
			return this;
		}
		
		public boolean isMemberTypeChanged(){
			return this.memberTypeId >= 0;
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
			return this.birthday != 0;
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
		
		public UpdateBuilder setPrivateComment(String comment){
			this.privateComment = comment;
			return this;
		}
		
		public boolean isPrivateCommentChanged(){
			return this.privateComment != null;
		}
		
		public UpdateBuilder setPublicComment(String comment){
			this.publicComment = comment;
			return this;
		}
		
		public boolean isPublicCommentChanged(){
			return this.publicComment != null;
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
	
	private int id;
	private int restaurantId;
	private float totalConsumption;		// 消费总额
	private int totalPoint;				// 累计积分
	private float totalCharge;			// 累计充值额
	private float usedBalance;			// 累计会员余额消费
	private int consumptionAmount;		// 累计消费次数
	private float baseBalance;			// 基础余额
	private float extraBalance;			// 额外余额
	private int usedPoint;				// 累计使用积分
	private int point;					// 当前积分
	
	private String name;				// 客户名称
	private Sex sex = Sex.MALE;			// 性别
	private String tele;				// 电话
	private String mobile;				// 手机
	private long birthday;				// 生日
	private String idCard;				// 身份证
	private String company;				// 公司
	private String contactAddress;		// 联系地址
	private long createDate;			// 创建时间
	private MemberType memberType;		// 会员类型
	private String memberCard;			// 会员卡号
	
	//会员的公开评论
	private SortedList<MemberComment> publicComments = SortedList.newInstance(new Comparator<MemberComment>(){
		@Override
		public int compare(MemberComment arg0, MemberComment arg1) {
			//按评论时间倒序
			if(arg0.getLastModified() > arg1.getLastModified()){
				return -1;
			}else if(arg0.getLastModified() < arg1.getLastModified()){
				return 1;
			}else{
				return 0;
			}
		}	
	
	});
	private MemberComment privateComment;	//某个员工的私有评论
	
	public Member(){
		
	}
	
	public Member(int id){
		this.id = id;
	}
	
	private Member(UpdateBuilder builder){
		
		setId(builder.memberId);
		setRestaurantId(builder.restaurantId);
		
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
		if(builder.isPublicCommentChanged()){
			addPublicComment(MemberComment.newPublicComment(null, new Member(builder.memberId), builder.publicComment));
		}
		if(builder.isPrivateCommentChanged()){
			setPrivateComment(MemberComment.newPrivateComment(null, new Member(builder.memberId), builder.privateComment));
		}
	}
	
//	/**
//	 * 
//	 * @param id
//	 * @param baseBalance
//	 * @param extraBalance
//	 * @param comment
//	 * @return
//	 */
//	public static Member buildToBalance(int id, float baseBalance, float extraBalance, String comment){
//		Member updateBalance = new Member();
//		updateBalance.setId(id);
//		updateBalance.setBaseBalance(baseBalance);
//		updateBalance.setExtraBalance(extraBalance);
//		updateBalance.setComment(comment);
//		return updateBalance;
//	}
//	
//	/**
//	 * 
//	 * @param id
//	 * @param point
//	 * @param staffID
//	 * @param comment
//	 * @return
//	 */
//	public static Member buildToPoint(int id, int point, String comment){
//		Member updateBalance = new Member();
//		updateBalance.setId(id);
//		updateBalance.setPoint(point);
//		updateBalance.setComment(comment);
//		return updateBalance;
//	}
	
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
			throw new BusinessException(MemberError.EXCEED_BALANCE);
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

		MemberOperation mo = MemberOperation.newMO(getId(), getName(), getMobile(), getMemberCard());
		
		mo.setOperationType(OperationType.CONSUME);
		mo.setPayType(payType);
		mo.setPayMoney(consumePrice);
		
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
			
			//累计消费金额
			usedBalance += consumePrice;
			
			mo.setDeltaBaseMoney(deltaBase);
			mo.setDeltaExtraMoney(deltaExtra);
		}
		
		//累计消费次数
		consumptionAmount++;
		
		//累计消费额
		totalConsumption += consumePrice;
		
		//累计会员当前可使用的积分
		int deltaPoint = Math.round(consumePrice * this.getMemberType().getExchangeRate());
		mo.setDeltaPoint(deltaPoint);
		point += deltaPoint;
		
		//累计从消费兑换而增加的积分
		totalPoint += deltaPoint;
		
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
		
		MemberOperation mo = MemberOperation.newMO(getId(), getName(), getMobile(), getMemberCard());
		
		mo.setOperationType(OperationType.CHARGE);
		mo.setChargeMoney(chargeMoney);
		mo.setChargeType(chargeType);
		
		float deltaBase = chargeMoney;
		float deltaExtra = chargeMoney * Math.abs(getMemberType().getChargeRate() - 1);

		totalCharge += chargeMoney;
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
		
		if(pointToConsume > point){
			throw new BusinessException(MemberError.EXCEED_POINT);
		}
		
		MemberOperation mo = MemberOperation.newMO(getId(), getName(), getMobile(), getMemberCard());
		mo.setOperationType(OperationType.POINT_CONSUME);

		point = point - pointToConsume;
		usedPoint += pointToConsume;
		
		mo.setDeltaPoint(pointToConsume * -1);
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
			
		}else if(adjustType == AdjustType.REDUCE){
			deltaPoint = -Math.abs(deltaPoint);
			point = point + deltaPoint;
			
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
			
		}else if(flag == MEMBER_PARCELABLE_COMPLEX){
			
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
			
		}else if(flag == MEMBER_PARCELABLE_COMPLEX){
			
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
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new LinkedHashMap<String, Object>();
		jm.put("id", this.id);
		jm.put("rid", this.restaurantId);
		jm.put("totalCharge", this.totalCharge);
		jm.put("totalPoint", this.totalPoint);
		jm.put("totalConsumption", this.totalConsumption);
		jm.put("consumptionAmount", this.consumptionAmount);
		jm.put("baseBalance", this.baseBalance);
		jm.put("extraBalance", this.extraBalance);
		jm.put("totalBalance", this.getTotalBalance());
		jm.put("usedBalance", this.usedBalance);
		jm.put("point", this.point);
		jm.put("usedPoint", this.usedPoint);
		if(this.sex != null){
			jm.put("sexText", this.sex.getDesc());
			jm.put("sexValue", this.sex.getVal());
		}
		if(this.memberType != null){
			jm.put("memberType", this.memberType);
		}
		jm.put("name", this.name);
		jm.put("tele", this.tele);
		jm.put("mobile", this.mobile);
		jm.put("birthday", this.birthday);
		jm.put("birthdayFormat", DateUtil.format(this.birthday));
		jm.put("idCard", this.idCard);
		jm.put("company", this.company);
		//jm.put("tastePref", this.tastePref);
		//jm.put("taboo", this.taboo);
		jm.put("contactAddress", this.contactAddress);
		//jm.put("comment", this.comment);
		jm.put("createDate", this.createDate);
		jm.put("createDateFormat", DateUtil.format(this.createDate));
		jm.put("memberCard", this.memberCard);
		
		return Collections.unmodifiableMap(jm);
	}

	@Override
	public List<Object> toJsonList(int flag) {
		return null;
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
	
	public MemberType getMemberType() {
		if(memberType == null){
			memberType = new MemberType(0);
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
			return "";
		}
		return memberCard;
	}
	
	public void setMemberCard(String memberCard) {
		this.memberCard = memberCard;
	}
	
	public String getName() {
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
	
	public void addPublicComment(MemberComment comment){
		if(comment != null){
			for(MemberComment publicComment : publicComments){
				if(publicComment.equals(comment)){
					publicComment.setComment(comment.getComment());
					return;
				}
			}
			this.publicComments.add(comment);
		}
	}
	
	public void setPublicComments(List<MemberComment> comments){
		if(comments != null){
			this.publicComments.clear();
			this.publicComments.addAll(comments);
		}
	}
	
	public List<MemberComment> getPublicComments(){
		return Collections.unmodifiableList(this.publicComments);
	}
	
	public boolean hasPublicComments(){
		return this.publicComments.size() != 0;
	}
	
	public void setPrivateComment(MemberComment comment){
		if(comment != null){
			this.privateComment = comment;
		}
	}
	
	public MemberComment getPrivateComment(){
		if(this.privateComment != null){
			return this.privateComment;
		}else{
			return MemberComment.newPrivateComment(null, this, "");
		}
	}
	
	public boolean hasPrivateComment(){
		return this.privateComment != null;
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
