package com.wireless.pojo.client;

import com.wireless.pojo.util.DateUtil;
import com.wireless.protocol.Order;

public class MemberOperation {
	
	/**
	 * 操作类型
	 * 1-充值,  2-消费,  3-冻结,  4-解冻,  5-换卡,  6-反结帐退款,  7-反结帐消费
	 * @author WuZY
	 */
	public static enum OperationType{
		CHARGE(			1, 	"充值", 			"CZ"), 
		CONSUME(		2, 	"消费", 			"XF"),
		FREEZE(			3, 	"冻结", 			"DJ"),
		UNFREEZE(		4, 	"解冻", 			"JD"),
		EXCHANGE(		5, 	"换卡", 			"HK"),
		UNPAY_CANCEL(	6, 	"反结帐退款", 	"FJZTK"),
		UNPAY_CONSUME(	7, 	"反结帐消费", 	"FJZXF");
		
		private final int value;			//
		private final String name;			//
		private final String prefix; 	//流水号前缀
		
		OperationType(int value, String name, String prefix){
			this.value = value;
			this.name = name;
			this.prefix = prefix;
		}
		
		@Override
		public String toString(){
			return this.getName();
		}
		
		public static OperationType valueOf(int val){
			for(OperationType ot : values()){
				if(ot.getValue() == val){
					return ot;
				}
			}
			
			throw new IllegalArgumentException("The operation value(val = " + val + ") passed is invalid.");
		}
		
		public int getValue() {
			return value;
		}
		
		public String getName() {
			return name;
		}
		
		public String getPrefix() {
			return prefix;
		}
	}
	
	/**
	 * 充值类型操作, 收款方式
	 * 1-现金  2-刷卡
	 * @author WuZY
	 */
	public static enum ChargeType{
		
		CASH( 			1, 	"现金", 	"XJ"),
		CREDIT_CARD( 	2, 	"刷卡", 	"SK");
		
		private final int value;
		private final String name;
		private final String prefix;
		
		ChargeType(int value, String name, String prefix){
			this.value = value;
			this.name = name;
			this.prefix = prefix;
		}
		
		@Override
		public String toString(){
			return this.name;
		}
		
		public static ChargeType valueOf(int val){
			for(ChargeType ct : values()){
				if(ct.getValue() == val){
					return ct;
				}
			}
			
			throw new IllegalArgumentException("The charge type(val = " + val + ") passed is invalid.");
		}
		
		public int getValue() {
			return value;
		}
		
		public String getName() {
			return name;
		}
		
		public String getPrefix() {
			return prefix;
		}
	}
	
	private int id;
	private int restaurantID;
	private long staffID;
	private String staffName;
	private Member member;
	private int memberCardID;
	private String memberCardAlias;
	private String seq;
	private long operateDate;
	private OperationType operateType;
	private float payMoney;
	private Order.PayType payType;
	private int orderId;
	private ChargeType chargeType;
	private float chargeMoney;
	private float deltaBaseMoney;
	private float deltaExtraMoney;
	private int deltaPoint;
	private float remainingBaseMoney;
	private float remainingExtraMoney;
	private int remainingPoint;
	private String comment;
	
	public float getDeltaTotalMoney(){
		return this.deltaBaseMoney + this.deltaExtraMoney;
	}
	public float getRemainingTotalMoney(){
		return this.remainingBaseMoney + this.remainingExtraMoney;
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
	public long getStaffID() {
		return staffID;
	}
	public void setStaffID(long staffID) {
		this.staffID = staffID;
	}
	public String getStaffName() {
		return staffName;
	}
	public void setStaffName(String staffName) {
		this.staffName = staffName;
	}
	
	public void setMember(Member member){
		this.member = member;
	}
	
	public Member getMember(){
		return member;
	}
	
	public Integer getMemberID() {
		return this.member == null ? null : this.member.getId();
	}
	
	public void setMemberID(int memberID) {
		this.member = this.member == null ? new Member() : this.member;
		this.member.setId(memberID);
	}
	
	public int getMemberCardID() {
		return memberCardID;
	}
	
	public void setMemberCardID(int memberCardID) {
		this.memberCardID = memberCardID;
	}
	public String getMemberCardAlias() {
		return memberCardAlias;
	}
	public void setMemberCardAlias(String memberCardAlias) {
		this.memberCardAlias = memberCardAlias;
	}
	
	public String getOperateSeq() {
		return seq;
	}
	
	public void setOperateSeq(String seq) {
		this.seq = seq;
	}
	
	public String getOperateDateFormat() {
		return this.operateDate > 0 ? DateUtil.format(this.operateDate) : null;
	}
	public long getOperateDate() {
		return operateDate;
	}
	
	public void setOperateDate(long date) {
		this.operateDate = date;
	}
	public Integer getOperationTypeValue() {
		return this.operateType != null ? this.operateType.getValue() : null;
	}
	public String getOperationTypeText() {
		return this.operateType != null ? this.operateType.getName() : null;
	}
	public OperationType getOperationType() {
		return operateType;
	}
	
	public void setOperationType(int type) {
		this.operateType = OperationType.valueOf(type);
	}
	
	public void setOperationType(OperationType ot){
		this.operateType = ot;
	}
	
	public Integer getPayTypeValue(){
		return this.payType != null ? this.payType.getVal() : null;
	}
	
	public String getPayTypeText(){
		return this.payType != null ? this.payType.getDesc() : null;
	}
	
	public void setOrderId(int orderId){
		this.orderId = orderId;
	}
	
	public int getOrderId(){
		return this.orderId;
	}
	
	public Order.PayType getPayType(){
		return this.payType;
	}
	
	public void setPayType(Order.PayType type){
		this.payType = type;
	}
	
	public float getPayMoney() {
		return payMoney;
	}
	
	public void setPayMoney(float payMoney) {
		this.payMoney = payMoney;
	}
	public Integer getChargeTypeValue() {
		return this.chargeType != null ? this.chargeType.getValue() : null;
	}
	public String getChargeTypeText() {
		return this.chargeType != null ? this.chargeType.getName() : null;
	}
	public ChargeType getChargeType() {
		return chargeType;
	}
	
	public void setChargeType(ChargeType type){
		this.chargeType = type;
	}
	
	public void setChargeType(short chargeType) {
		this.chargeType = ChargeType.valueOf(chargeType);
	}
	
	public float getChargeMoney() {
		return chargeMoney;
	}
	public void setChargeMoney(float chargeMoney) {
		this.chargeMoney = chargeMoney;
	}
	public float getDeltaBaseMoney() {
		return deltaBaseMoney;
	}
	public void setDeltaBaseMoney(float deltaBaseMoney) {
		this.deltaBaseMoney = deltaBaseMoney;
	}
	
	public float getDeltaExtraMoney() {
		return deltaExtraMoney;
	}
	public void setDeltaExtraMoney(float deltaExtraMoney) {
		this.deltaExtraMoney = deltaExtraMoney;
	}
	public float getRemainingBaseMoney() {
		return remainingBaseMoney;
	}
	public void setRemainingBaseMoney(float remainingBaseMoney) {
		this.remainingBaseMoney = remainingBaseMoney;
	}
	public float getRemainingExtraMoney() {
		return remainingExtraMoney;
	}
	public void setRemainingExtraMoney(float remainingExtraMoney) {
		this.remainingExtraMoney = remainingExtraMoney;
	}
	public int getDeltaPoint() {
		return deltaPoint;
	}
	public void setDeltaPoint(int deltaPoint) {
		this.deltaPoint = deltaPoint;
	}
	public int getRemainingPoint() {
		return remainingPoint;
	}
	
	public void setRemainingPoint(int remainingPoint) {
		this.remainingPoint = remainingPoint;
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
}
