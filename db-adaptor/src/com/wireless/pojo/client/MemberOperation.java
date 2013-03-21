package com.wireless.pojo.client;

public class MemberOperation {
	/**
	 * 操作类型
	 * 1-充值  2-消费  3-冻结  4-解冻  5-换卡  6-反结帐退款  7-反结帐消费
	 * @author WuZY
	 */
	public enum OPERATION_TYPE{
		CHARGE((short) 1, "充值", "CZ"), 
		CONSUME((short) 2, "消费", "XF"),
		FREEZE((short) 3, "冻结", "DJ"),
		UNFREEZE((short) 4, "解冻", "JD"),
		EXCHANGE((short) 5, "换卡", "HK"),
		UNPAY_CANCEL((short) 6, "反结帐退款", "FJZTK"),
		UNPAY_CONSUME((short) 7, "反结帐消费", "FJZXF");
		
		private short value;
		private String name;
		private String sep; // 流水号前缀
		OPERATION_TYPE(short value, String name, String seq){
			this.value = value;
			this.name = name;
			this.sep = seq;
		}
		public short getValue() {
			return value;
		}
		public String getName() {
			return name;
		}
		public String getSep() {
			return sep;
		}
	}
	
	/**
	 * 消费类型操作, 消费方式
	 * 1-现金  2-刷卡  3-签单  4-挂账
	 * @author WuZY
	 */
	public enum PAY_TYPE{
		CASH((short) 1, "现金", "XJ"),
		CREDIT_CARD((short) 2, "刷卡", "SK"),
		SIGN((short) 3, "签单", "QD"),
		HANG((short) 4, "挂账", "GZ");
		
		private short value;
		private String name;
		private String sep;
		PAY_TYPE(short value, String name, String seq){
			this.value = value;
			this.name = name;
			this.sep = seq;
		}
		public short getValue() {
			return value;
		}
		public String getName() {
			return name;
		}
		public String getSep() {
			return sep;
		}
	}
	
	/**
	 * 充值类型操作, 收款方式
	 * 1-现金  2-刷卡
	 * @author WuZY
	 */
	public enum RECHARGE_TYPE{
		CASH((short) 1, "现金", "XJ"),
		CREDIT_CARD((short) 2, "刷卡", "SK");
		
		private short value;
		private String name;
		private String sep;
		RECHARGE_TYPE(short value, String name, String seq){
			this.value = value;
			this.name = name;
			this.sep = seq;
		}
		public short getValue() {
			return value;
		}
		public String getName() {
			return name;
		}
		public String getSep() {
			return sep;
		}
	}
	
	private int id;
	private int restaurantID;
	private long staffID;
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
