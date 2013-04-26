package com.wireless.pojo.client;

import com.wireless.exception.BusinessException;
import com.wireless.exception.ProtocolError;
import com.wireless.pojo.client.MemberOperation.ChargeType;
import com.wireless.pojo.client.MemberOperation.OperationType;
import com.wireless.pojo.client.MemberType.Attribute;
import com.wireless.pojo.dishesOrder.Order.PayType;
import com.wireless.pojo.system.Staff;
import com.wireless.pojo.util.DateUtil;
import com.wireless.protocol.PMember;

public class Member {
	
	public static enum Status{
		
		NORMAL(PMember.STATUS_NORMAL),
		DISABLED(PMember.STATUS_DISABLED);
		
		private final int val;
		private Status(int val){
			this.val = val;
		}
		
		@Override
		public String toString(){
			if(this == NORMAL){
				return "member status : normal(val = " + val + ")";
			}else if(this == DISABLED){
				return "member status : disabled(val = " + val + ")";
			}else{
				return "member status : invalid(val = " + val + ")";
			}
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
	}
	
	public static final String OPERATION_INSERT = "添加会员资料.";
	public static final String OPERATION_UPDATE = "修改会员资料.";
	public static final String OPERATION_DELETE = "删除会员资料.";
	public static final String OPERATION_CHARGE = "会员充值.";
	private int id;
	private int restaurantID;
	private float baseBalance;
	private float extraBalance;
	private int point;
	private long birthDate;
	private long lastModDate;
	private String comment;
	private Status status = Status.NORMAL;
	
	private MemberType memberType = null;
	private MemberCard memberCard = null;
	private Client client = null;
	private Staff staff = null;
	
	public Member(){
		this.memberType = new MemberType();
		this.memberCard = new MemberCard();
		this.client = new Client();
		this.staff = new Staff();
		this.staff.setTerminal(null);
	}
	
	public final PMember toProtocolObj(){
		PMember protocolObj = new PMember();
		
		protocolObj.setId(getId());
		protocolObj.setName(getClient().getName());
		protocolObj.setRestaurantId(getRestaurantID());
		protocolObj.setBaseBalance(getBaseBalance());
		protocolObj.setExtraBalance(getExtraBalance());
		protocolObj.setPoint(getPoint());
		protocolObj.setBirthDate(getBirthDate());
		protocolObj.setStatus(getStatus().getVal());
		protocolObj.getMemberType().setName(getMemberType().getName());
		protocolObj.getMemberType().setExchangeRate(getMemberType().getExchangeRate());
		
		return protocolObj;
	}

	private void copyFrom(PMember protocolObj){
		this.setId(protocolObj.getId());
		this.getClient().setName(protocolObj.getName());
		this.setRestaurantID(protocolObj.getRestaurantId());
		this.setBaseBalance(protocolObj.getBaseBalance());
		this.setExtraBalance(protocolObj.getExtraBalance());
		this.setPoint(protocolObj.getPoint());
		this.setBirthDate(protocolObj.getBirthDate());
		this.setStatus(Status.valueOf(protocolObj.getStatus()));
	}

	public static Member buildFromProtocol(PMember protocolObj){
		Member member = new Member();
		member.copyFrom(protocolObj);
		return member;
	}
	
	/**
	 * 
	 * @param id
	 * @param baseBalance
	 * @param extraBalance
	 * @param staffID
	 * @param comment
	 * @return
	 */
	public static Member buildToBalance(int id, float baseBalance, float extraBalance, long staffID, String comment){
		Member updateBalance = new Member();
		updateBalance.setId(id);
		updateBalance.setBaseBalance(baseBalance);
		updateBalance.setExtraBalance(extraBalance);
		updateBalance.getStaff().setId(staffID);
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
	public static Member buildToPoint(int id, int point, long staffID, String comment){
		Member updateBalance = new Member();
		updateBalance.setId(id);
		updateBalance.setPoint(point);
		updateBalance.getStaff().setId(staffID);
		updateBalance.setComment(comment);
		return updateBalance;
	}
	
	/**
	 * Check to see whether the balance of member account is enough for consumption in case of paid by member.
	 * @param consumePrice
	 * 			the amount to consume price
	 * @param payType
	 * 			the payment type referred to {@link PayType}
	 * @throws BusinessException
	 *             Throws if the consume price exceeds total balance to this
	 *             member account.
	 */
	public void checkConsume(float consumePrice, PayType payType) throws BusinessException{
		
		if(payType != PayType.MEMBER){
			return;
		}
		
		if(getTotalBalance() < consumePrice){
			//Check to see whether the balance of member account is enough or NOT in case of unpaid.
			throw new BusinessException("The consume price to order exceeds the balance of member account", ProtocolError.EXCEED_MEMBER_BALANCE);
		}
	}
	
	/**
	 * Check to see whether the balance of member account is enough for repaid consumption in case of paid by member.
	 * @param consumePrice
	 * 			the amount of consume price
	 * @param repaidOrderMO
	 * 			the member operation of order to be repaid
	 * @param payType
	 * 			the payment type referred to {@link PayType}
	 * @throws BusinessException
	 *             Throws if the consume price exceeds total balance to this
	 *             member account.
	 */
	public void checkRepaidConsume(float consumePrice, MemberOperation repaidOrderMO, PayType payType) throws BusinessException{
		
		if(payType != PayType.MEMBER){
			return;
		}
		
		float baseToRollback = this.baseBalance;
		float extraToRollback = this.extraBalance;
		int pointToRollBack = this.point;
		
		//Restore the member account according repaid order member operation
		this.baseBalance += repaidOrderMO.getDeltaBaseMoney();
		this.extraBalance += repaidOrderMO.getDeltaExtraMoney();
		this.point = this.point - repaidOrderMO.getDeltaPoint();
		if(point < 0){
			point = 0;
		}
		
		try{
			checkConsume(consumePrice, payType);
		}catch(BusinessException e){
			throw e;
		}finally{
			//Roll back the balance & point at the end of checking.
			this.baseBalance = baseToRollback;
			this.extraBalance = extraToRollback;
			this.point = pointToRollBack;
		}
	}
	
	/**
	 * Perform the consumption operation to this member account in case of paid by member.
	 * 
	 * @param consumePrice
	 *            the amount to consume price
	 * @param payType
	 * 			  	the pay type referred to {@link PayType}
	 * @return the member operation to this consumption
	 * @throws BusinessException
	 *             Throws if the consume price exceeds total balance to this
	 *             member account.
	 */
	public MemberOperation consume(float consumePrice, PayType payType) throws BusinessException{

		MemberOperation mo = new MemberOperation();
		
		mo.setMemberID(getId());
		mo.setMemberCardID(getMemberCard().getId());
		mo.setMemberCardAlias(getMemberCard().getAliasID());
		mo.setOperationType(OperationType.CONSUME);
		mo.setPayType(payType);
		
		if(getMemberType().getAttribute() == Attribute.COUPON){
			mo.setPayMoney(consumePrice);
			
		}else{
			if(payType != PayType.MEMBER){
				consumePrice = 0;
			}
			
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
			
			//累计会员积分
			int deltaPoint = Math.round(consumePrice * getMemberType().getExchangeRate());
			mo.setDeltaPoint(deltaPoint);
			point += deltaPoint;
			
			mo.setRemainingBaseMoney(baseBalance);
			mo.setRemainingExtraMoney(extraBalance);
			mo.setRemainingPoint(point);
		}
		
		return mo;
	}
	
	/**
	 * Perform the repaid consumption.
	 * 
	 * @param consumePrice
	 * 				the amount of consume price
	 * @param repaidOrderMO
	 * 				the member operation of the order to be re-paid
	 * @param payType
	 * 				the payment type referred to {@link PayType}
	 * @return Two member operation.<br>
	 * 		   One is to record unpaid cancel.<br>
	 * 		   The other is to record repaid consume.
	 * @throws BusinessException
	 *             Throws if the consume price exceeds total balance to this
	 *             member account.
	 */
	public MemberOperation[] repaidConsume(float consumePrice, MemberOperation repaidOrderMO, PayType payType) throws BusinessException{
		
		if(payType != PayType.MEMBER){
			consumePrice = 0;
		}
		
		checkRepaidConsume(consumePrice, repaidOrderMO, payType);
		
		//Generate a member operation to unpaid cancel
		MemberOperation moToUnpaidCancel = new MemberOperation();
		
		moToUnpaidCancel.setMemberID(getId());
		moToUnpaidCancel.setMemberCardID(getMemberCard().getId());
		moToUnpaidCancel.setMemberCardAlias(getMemberCard().getAliasID());
		moToUnpaidCancel.setOperationType(OperationType.UNPAY_CANCEL);
		
		moToUnpaidCancel.setDeltaBaseMoney(repaidOrderMO.getDeltaBaseMoney());
		moToUnpaidCancel.setDeltaExtraMoney(repaidOrderMO.getDeltaExtraMoney());
		moToUnpaidCancel.setDeltaPoint(repaidOrderMO.getDeltaPoint());
		
		moToUnpaidCancel.setRemainingBaseMoney(baseBalance);
		moToUnpaidCancel.setRemainingExtraMoney(extraBalance);
		moToUnpaidCancel.setRemainingPoint(point);
		
		//Generate a member operation to unpaid consume
		MemberOperation moToUnpaidConsume = consume(consumePrice, payType);
		moToUnpaidConsume.setOperationType(OperationType.UNPAY_CONSUME);
			
		return new MemberOperation[]{ moToUnpaidCancel,	moToUnpaidConsume };
		
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
		
		mo.setMemberID(getId());
		mo.setMemberCardID(getMemberCard().getId());
		mo.setMemberCardAlias(getMemberCard().getAliasID());
		mo.setOperationType(OperationType.CHARGE);
		mo.setChargeMoney(chargeMoney);
		mo.setChargeType(chargeType);
		
		float deltaBase = chargeMoney;
		float deltaExtra = chargeMoney * Math.abs(getMemberType().getChargeRate() - 1);
		int deltaPoint = Math.round(chargeMoney * getMemberType().getExchangeRate());

		baseBalance += deltaBase;
		extraBalance += deltaExtra;
		point += deltaPoint;
		
		mo.setDeltaBaseMoney(deltaBase);
		mo.setDeltaExtraMoney(deltaExtra);
		mo.setDeltaPoint(deltaPoint);
		
		mo.setRemainingBaseMoney(baseBalance);
		mo.setRemainingExtraMoney(extraBalance);
		mo.setRemainingPoint(point);
		
		return mo;
	}
	
	public float getTotalBalance(){
		return this.baseBalance + this.extraBalance;
	}
	
	public int getMemberTypeID() {
		return this.memberType == null ? 0 : this.memberType.getTypeID();
	}
	public void setMemberTypeID(int memberTypeID) {
		this.memberType = this.memberType == null ? new MemberType() : this.memberType;
		this.memberType.setTypeID(memberTypeID);
	}
	public int getMemberCardID() {
		return this.memberCard == null ? 0 : this.memberCard.getId();
	}
	public void setMemberCardID(int memberCardID) {
		this.memberCard = this.memberCard == null ? new MemberCard() : this.memberCard;
		this.memberCard.setId(memberCardID);
	}
	public long getStaffID() {
		return this.staff == null ? 0 : this.staff.getId();
	}
	public void setStaffID(long staffID) {
		this.staff = this.staff == null ? new Staff() : this.staff;
		this.staff.setId(staffID);
	}
	public int getClientID() {
		return this.client == null ? 0 : this.client.getClientID();
	}
	public void setClientID(int clientID) {
		this.client = this.client == null ? new Client() : this.client;
		this.client.setClientID(clientID);
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getRestaurantID() {
		return restaurantID;
	}
	public void setRestaurantID(int restaurantID) {
		this.restaurantID = restaurantID;
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
	public long getBirthDate() {
		return birthDate;
	}
	public String getBirthDateFormat() {
		return DateUtil.format(birthDate);
	}
	public void setBirthDate(long birthDate) {
		this.birthDate = birthDate;
	}
	public long getLastModDate() {
		return lastModDate;
	}
	public String getLastModDateFormat() {
		return DateUtil.format(lastModDate);
	}
	public void setLastModDate(long lastModDate) {
		this.lastModDate = lastModDate;
	}
	
	public String getComment() {
		if(this.comment == null){
			comment = "";
		}
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public Integer getStatusValue() {
		return status != null ? status.getVal() : null;
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
		return memberType;
	}
	public void setMemberType(MemberType memberType) {
		this.memberType = memberType;
	}
	public MemberCard getMemberCard() {
		return memberCard;
	}
	public void setMemberCard(MemberCard memberCard) {
		this.memberCard = memberCard;
	}
	public Client getClient() {
		return client;
	}
	public void setClient(Client client) {
		this.client = client;
	}
	public Staff getStaff() {
		return staff;
	}
	public void setStaff(Staff staff) {
		this.staff = staff;
	}
	
}
