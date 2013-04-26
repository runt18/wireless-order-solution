package com.wireless.pojo.dishesOrder;

import java.util.ArrayList;
import java.util.List;

import com.wireless.util.DateUtil;

public class Order {
	
	/**
	 * 收款方式
	 * 1-现金,  2-刷卡,  3-会员消费,  4-签单,  5-挂账
	 */
	public static enum PayType{
		
		CASH(com.wireless.protocol.Order.PAYMENT_CASH, "现金"),					//现金
		CREDIT_CARD(com.wireless.protocol.Order.PAYMENT_CREDIT_CARD, "刷卡"),	//刷卡
		MEMBER(com.wireless.protocol.Order.PAYMENT_MEMBER, "会员余额"),			//会员
		SIGN(com.wireless.protocol.Order.PAYMENT_SIGN, "签单"),					//签单
		HANG(com.wireless.protocol.Order.PAYMENT_HANG, "挂账");					//挂账
		
		private final int value;
		
		private final String desc;
		
		public static PayType valueOf(int value){
			for(PayType type : values()){
				if(type.getVal() == value){
					return type;
				}
			}
			throw new IllegalArgumentException("The pay type(val = " + value + ") passed is invalid.");
		}
		
		PayType(int value, String desc){
			this.value = value;
			this.desc = desc;
		}
		
		public int getVal(){
			return this.value;
		}
		
		public String getDesc(){
			return this.desc;
		}
		
		@Override
		public String toString(){
			return "(type :" + value + ",desc : " + this.desc + ")";
		}

	};
	/**
	 * 结账类型
	 * 1-会员,  2-普通
	 */
	public static enum SettleType{
		NORMAL(1, "普通"), 
		MEMBER(2, "会员");
		private int value;
		private String text;
		SettleType(int value, String text){
			this.value = value;
			this.text = text;
		}
		public int getValue() {
			return value;
		}
		public String getText() {
			return text;
		}
		public static SettleType valueOf(int value){
			for(SettleType type : values()){
				if(type.getValue() == value){
					return type;
				}
			}
			throw new IllegalArgumentException("The pay type(val = " + value + ") passed is invalid.");
		}
		@Override
		public String toString(){
			return "(type :" + value + ",text : " + this.text + ")";
		}
	}
	
	public static final short CATE_NORMAL = 1;			//一般
	public static final short CATE_TAKE_OUT = 2;		//外卖
	public static final short CATE_JOIN_TABLE = 3;		//拆台
	public static final short CATE_MERGER_TABLE = 4;	//并台
	public static final short CATE_MERGER_CHILD = 5;	//账单组子账单
	public static final String CATE_NORMAL_TEXT = "一般";	
	public static final String CATE_TAKE_OUT_TEXT = "外卖";
	public static final String CATE_JOIN_TABLE_TEXT = "拆台";	
	public static final String CATE_MERGER_TABLE_TEXT = "并台";
	public static final String CATE_MERGER_CHILD_TEXT = "一般(子账单)";
	public static final short STATUS_UNPAID = 0;	//未结帐
	public static final short STATUS_PAID = 1;		//已结帐
	public static final short STATUS_REPAID = 2;	//反结帐
	public static final String STATUS_UNPAID_TEXT = "未结帐";
	public static final String STATUS_PAID_TEXT = "已结帐";
	public static final String STATUS_REPAID_TEXT = "反结帐";
	
	private long id;			// 账单编号
	private long seqID;			// 数据编号
	private int restaurantID;	// 餐厅编号
	private long orderDate;		// 账单最后修改时间
	private float totalPrice;	// 总金额
	private float acturalPrice;	// 实收
	private int customNum;		// 客户人数
	private String waiter;		// 服务员名
	private int discountID;		// 折扣方案编号
	private int memberID;	// 会员编号
	private String member;		// 会员名称
	private int terminalModel;	//
	private int terminalPin;	//
	private int regionID;		// 区域编号
	private String regionName;	// 区域名称
	private short category;		// 账单类型
	private SettleType settleType;		// 结账类型  1:会员 2:普通
	private PayType payType;	// 收款方式  1:现金  2:刷卡  3:会员卡  4:签单  5:挂账
	private String comment;		// 备注
	private float serviceRate;	// 服务费率
	private float minCost;		// 最低消费金额
	private float giftPrice;	// 赠送金额
	private float discountPrice;// 折扣金额
	private float cancelPrice;	// 退菜金额
	private float erasePuotaPrice;// 抹数金额
	private float repaidPrice;	// 反结账金额
	private int tableID;		// 餐台编号
	private int tableAlias;		// 餐台自定义编号
	private short tableStatus;	// 残餐台状态
	private String tableName;	// 餐台名称
	private short status;		// 账单状态 0:未结帐 1:已结账 2: 反结账
	private int pricePlanID;
	private String pricePlanName;
	private List<OrderFood> orderFoods;		// 账单包涵菜品
	private List<Order> childOrder;    // 账单组子账单
	
	public Order(com.wireless.protocol.Order pt){
		if(pt != null){
			this.id = pt.getId();
			this.customNum = pt.getCustomNum();
			this.orderDate = pt.getOrderDate();
			this.serviceRate = pt.getServiceRate();
			this.category = pt.getCategory();
			this.payType = PayType.valueOf(pt.getPaymentType());
			this.settleType = SettleType.valueOf(pt.getSettleType());
			this.status = Short.valueOf(pt.getStatus()+"");
			this.minCost = pt.getDestTbl().getMinimumCost();
			this.restaurantID = pt.getRestaurantId();
			this.discountID = pt.getDiscount().getId();
			this.orderFoods = null;
			this.giftPrice = pt.getGiftPrice();
			this.discountPrice = pt.getDiscountPrice();
			this.cancelPrice = pt.getCancelPrice();
			this.erasePuotaPrice = pt.getErasePrice();
			this.repaidPrice = pt.getRepaidPrice();
			this.acturalPrice = pt.getActualPrice();
			this.totalPrice = pt.calcPriceBeforeDiscount();
			this.tableID = pt.getDestTbl().getTableId();
			this.tableAlias = pt.getDestTbl().getAliasId();
			this.tableName = pt.getDestTbl().getName();
			this.tableStatus = pt.getDestTbl().getStatus();
			if(pt.getPricePlan() != null){
				this.pricePlanID = pt.getPricePlan().getId();
				this.pricePlanName = pt.getPricePlan().getName();				
			}
			if(pt.getMember() != null){
				this.memberID = pt.getMember().getId();
				this.member = pt.getMember().getName();
			}
			
			if(pt.getOrderFoods() != null && pt.getOrderFoods().length > 0){
				this.orderFoods = new ArrayList<OrderFood>();
				for(com.wireless.protocol.OrderFood temp : pt.getOrderFoods()){
					this.orderFoods.add(new OrderFood(temp));
				}
			}
			
			if(pt.getChildOrder() != null && pt.getChildOrder().length > 0){
				this.childOrder = new ArrayList<Order>();
				for(com.wireless.protocol.Order temp : pt.getChildOrder()){
					this.childOrder.add(new Order(temp));
				}
			}
		}
		
	}
	
	public Order(){
		this.payType = PayType.CASH;
		this.category = Order.CATE_NORMAL;
		this.orderFoods = new ArrayList<OrderFood>();
		this.childOrder = new ArrayList<Order>();
	}
	
	// 是否打折
	public boolean isDiscount() {
		return this.discountPrice > 0;
	}
	// 是否赠送
	public boolean isGift() {
		return this.giftPrice > 0;
	}
	// 是否退菜
	public boolean isReturn() {
		return this.cancelPrice > 0;
	}
	// 是否抹数
	public boolean isErasePuota() {
		return this.erasePuotaPrice > 0;
	}	
	// 是否账单组父元素
	public boolean isMerger(){
		return this.category == Order.CATE_MERGER_TABLE;
	}
	
	public String getOrderDateFormat(){
		return DateUtil.format(this.orderDate);
	}
	
	// 
	public String getPayMannerFormat() {
		return this.payType.getDesc();
	}
	
	//
	public String getCategoryFormat() {
		switch(this.category){
			case Order.CATE_NORMAL:
				return Order.CATE_NORMAL_TEXT;
			case Order.CATE_TAKE_OUT:
				return Order.CATE_TAKE_OUT_TEXT;
			case Order.CATE_JOIN_TABLE:
				return Order.CATE_JOIN_TABLE_TEXT;
			case Order.CATE_MERGER_TABLE:
				return CATE_MERGER_TABLE_TEXT;
			case Order.CATE_MERGER_CHILD:
				return CATE_MERGER_CHILD_TEXT;
			default:
				return "";
		}
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getSeqID() {
		return seqID;
	}
	public void setSeqID(long seqID) {
		this.seqID = seqID;
	}
	public int getRestaurantID() {
		return restaurantID;
	}
	public void setRestaurantID(int restaurantID) {
		this.restaurantID = restaurantID;
	}
	public long getOrderDate() {
		return orderDate;
	}
	public void setOrderDate(long orderDate) {
		this.orderDate = orderDate;
	}
	public float getGiftPrice() {
		return giftPrice;
	}
	public void setGiftPrice(float giftPrice) {
		this.giftPrice = giftPrice;
	}
	public float getTotalPrice() {
		return totalPrice;
	}
	public void setTotalPrice(float totalPrice) {
		this.totalPrice = totalPrice;
	}
	public int getCustomNum() {
		return customNum;
	}
	public void setCustomNum(int customNum) {
		this.customNum = customNum;
	}
	public String getWaiter() {
		return waiter;
	}
	public void setWaiter(String waiter) {
		this.waiter = waiter;
	}
	public int getDiscountID() {
		return discountID;
	}
	public void setDiscountID(int discountID) {
		this.discountID = discountID;
	}
	public int getMemberID() {
		return memberID;
	}
	public void setMemberID(int memberID) {
		this.memberID = memberID;
	}
	public String getMember() {
		return member;
	}
	public void setMember(String member) {
		this.member = member;
	}
	public int getTerminalModel() {
		return terminalModel;
	}
	public void setTerminalModel(int terminalModel) {
		this.terminalModel = terminalModel;
	}
	public int getTerminalPin() {
		return terminalPin;
	}
	public void setTerminalPin(int terminalPin) {
		this.terminalPin = terminalPin;
	}
	public short getCategory() {
		return category;
	}
	public void setCategory(short category) {
		this.category = category;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public float getServiceRate() {
		return serviceRate;
	}
	public void setServiceRate(float serviceRate) {
		this.serviceRate = serviceRate;
	}
	public List<OrderFood> getOrderFoods() {
		return orderFoods;
	}
	public void setOrderFoods(List<OrderFood> orderFoods) {
		this.orderFoods = orderFoods;
	}
	/**
	 * 
	 * @param pt
	 * @param o
	 */
	public void setOrderFoods(List<com.wireless.protocol.OrderFood> pt, Object o) {
		this.orderFoods = new ArrayList<OrderFood>();
		for(com.wireless.protocol.OrderFood temp : pt){
			this.orderFoods.add(new OrderFood(temp));
		}
	}
	/**
	 * 
	 * @param pt
	 * @param o
	 */
	public void setOrderFoods(com.wireless.protocol.OrderFood[] pt, Object o) {
		this.orderFoods = new ArrayList<OrderFood>();
		for(com.wireless.protocol.OrderFood temp : pt){
			this.orderFoods.add(new OrderFood(temp));
		}
	}
	public float getMinCost() {
		return minCost;
	}
	public void setMinCost(float minCost) {
		this.minCost = minCost;
	}
	public float getActuralPrice() {
		return acturalPrice;
	}
	public void setActuralPrice(float acturalPrice) {
		this.acturalPrice = acturalPrice;
	}
	public float getDiscountPrice() {
		return discountPrice;
	}
	public void setDiscountPrice(float discountPrice) {
		this.discountPrice = discountPrice;
	}
	public float getCancelPrice() {
		return cancelPrice;
	}
	public void setCancelPrice(float cancelPrice) {
		this.cancelPrice = cancelPrice;
	}
	public float getErasePuotaPrice() {
		return erasePuotaPrice;
	}
	public void setErasePuotaPrice(float erasePuotaPrice) {
		this.erasePuotaPrice = erasePuotaPrice;
	}
	public float getRepaidPrice() {
		return repaidPrice;
	}
	public void setRepaidPrice(float repaidPrice) {
		this.repaidPrice = repaidPrice;
	}
	public int getRegionID() {
		return regionID;
	}
	public void setRegionID(int regionID) {
		this.regionID = regionID;
	}
	public String getRegionName() {
		return regionName;
	}
	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}
	public int getTableID() {
		return tableID;
	}
	public void setTableID(int tableID) {
		this.tableID = tableID;
	}
	public int getTableAlias() {
		return tableAlias;
	}
	public void setTableAlias(int tableAlias) {
		this.tableAlias = tableAlias;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public short getTableStatus() {
		return tableStatus;
	}
	public void setTableStatus(short tableStatus) {
		this.tableStatus = tableStatus;
	}
	public short getStatus() {
		return status;
	}
	public void setStatus(short status) {
		this.status = status;
	}
	public List<Order> getChildOrder() {
		return childOrder;
	}
	public void setChildOrder(List<Order> childOrder) {
		this.childOrder = childOrder;
	}
	public int getPricePlanID() {
		return pricePlanID;
	}
	public void setPricePlanID(int pricePlanID) {
		this.pricePlanID = pricePlanID;
	}
	public String getPricePlanName() {
		return pricePlanName;
	}
	public void setPricePlanName(String pricePlanName) {
		this.pricePlanName = pricePlanName;
	}
	public String getSettleTypeFormat() {
		return this.settleType != null ? this.settleType.getText() : null;
	}
	public Integer getSettleTypeValue() {
		return this.settleType != null ? this.settleType.getValue() : null;
	}
	public SettleType getSettleType() {
		return settleType;
	}
	public void setSettleType(SettleType settleType) {
		this.settleType = settleType;
	}
	public void setSettleType(int settleType) {
		this.settleType = SettleType.valueOf(settleType);
	}
	public Integer getPayTypeValue() {
		return this.payType != null ? this.payType.getVal() : null;
	}
	public PayType getPayType() {
		return payType;
	}
	public void setPayType(PayType payType) {
		this.payType = payType;
	}
	public void setPayType(int payType) {
		this.payType = PayType.valueOf(payType);
	}
	
}
