package com.wireless.pojo.client;

public class MemberOperation {
	public static short OTYPE_CHARGE = 1;	// 操作类型 1:充值
	public static short OTYPE_CONSUME = 2;	// 操作类型 2:消费
	public static short OTYPE_FREEZE = 3;	// 操作类型 3:冻结
	public static short OTYPE_UNFREEZE = 4;	// 操作类型 4:解冻
	public static short OTYPE_EXCHANGE = 5;	// 操作类型 5:换卡
	
	public static short PAY_TYPE_CHARGE = 1;	// 充值收款类型 1:现金
	public static short PAY_TYPE_CONSUME = 2;	// 充值收款类型 2:刷卡
	
	private int id;
	private int restaurantID;
	private int staffID;
	private String staffName;
	private int memberID;
	private int memberCardID;
	private String memberCardAlias;
	private String sep;
	private long data;
	private short type;
	private short payType;
	private float payMoney;
	private short chargeType;
	private float chargeMoney;
	private float deltaBaseMoney;
	private float deltaGiftMoney;
	private int deltaPoint;
	private float remainingBaseMoney;
	private float remainingGiftMoney;
	private int remainingPoint;
	private String comment;
	
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
	public int getStaffID() {
		return staffID;
	}
	public void setStaffID(int staffID) {
		this.staffID = staffID;
	}
	public String getStaffName() {
		return staffName;
	}
	public void setStaffName(String staffName) {
		this.staffName = staffName;
	}
	public int getMemberID() {
		return memberID;
	}
	public void setMemberID(int memberID) {
		this.memberID = memberID;
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
	public String getSep() {
		return sep;
	}
	public void setSep(String sep) {
		this.sep = sep;
	}
	public long getData() {
		return data;
	}
	public void setData(long data) {
		this.data = data;
	}
	public short getType() {
		return type;
	}
	public void setType(short type) {
		this.type = type;
	}
	public short getPayType() {
		return payType;
	}
	public void setPayType(short payType) {
		this.payType = payType;
	}
	public float getPayMoney() {
		return payMoney;
	}
	public void setPayMoney(float payMoney) {
		this.payMoney = payMoney;
	}
	public short getChargeType() {
		return chargeType;
	}
	public void setChargeType(short chargeType) {
		this.chargeType = chargeType;
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
	public float getDeltaGiftMoney() {
		return deltaGiftMoney;
	}
	public void setDeltaGiftMoney(float deltaGiftMoney) {
		this.deltaGiftMoney = deltaGiftMoney;
	}
	public int getDeltaPoint() {
		return deltaPoint;
	}
	public void setDeltaPoint(int deltaPoint) {
		this.deltaPoint = deltaPoint;
	}
	public float getRemainingBaseMoney() {
		return remainingBaseMoney;
	}
	public void setRemainingBaseMoney(float remainingBaseMoney) {
		this.remainingBaseMoney = remainingBaseMoney;
	}
	public float getRemainingGiftMoney() {
		return remainingGiftMoney;
	}
	public void setRemainingGiftMoney(float remainingGiftMoney) {
		this.remainingGiftMoney = remainingGiftMoney;
	}
	public int getRemainingPoint() {
		return remainingPoint;
	}
	public void setRemainingPoint(int remainingPoint) {
		this.remainingPoint = remainingPoint;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
}
