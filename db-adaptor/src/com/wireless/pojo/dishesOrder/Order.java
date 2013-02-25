package com.wireless.pojo.dishesOrder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Order {
	public static final short MANNER_CASH = 1;			//现金
	public static final short MANNER_CREDIT_CARD = 2;	//刷卡
	public static final short MANNER_MEMBER = 3;		//会员卡
	public static final short MANNER_SIGN = 4;			//签单
	public static final short MANNER_HANG = 5;			//挂账
	public static final String MANNER_CASH_TEXT = "现金";	
	public static final String MANNER_CREDIT_CARD_TEXT = "刷卡";
	public static final String MANNER_MEMBER_TEXT = "会员卡";
	public static final String MANNER_SIGN_TEXT = "签单";	
	public static final String MANNER_HANG_TEXT = "挂账";	
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
	private short payManner;	// 结账方式  1:现金  2:刷卡  3:会员卡  4:签单  5:挂账
	private int discountID;		// 折扣方案编号
	private String memberID;	// 会员编号
	private String member;		// 会员名称
	private int terminalModel;	//
	private int terminalPin;	//
	private int regionID;		// 区域编号
	private String regionName;	// 区域名称
	private short category;		// 订单类型
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
	private String tableName;	// 餐台名称
	private short status;		// 账单状态 0:未结帐 1:已结账 2: 反结账
	private List<OrderFood> orderFoods;		// 账单包涵菜品
	private List<Order> childOrder;    // 账单组子账单
	
	public Order(com.wireless.protocol.Order protocol){
		this();
		this.setId(protocol.getId());
		this.setCustomNum(protocol.getCustomNum());
		this.setOrderDate(protocol.getOrderDate());
		this.setServiceRate(protocol.getServiceRate());
		this.setCategory(protocol.getCategory());
		this.setStatus(Short.valueOf(protocol.getStatus()+""));
		this.setMinCost(protocol.getDestTbl().getMinimumCost());
		this.setRestaurantID(protocol.getRestaurantId());
		this.setDiscountID(protocol.getDiscount().getId());
		this.setPayManner(Short.valueOf(protocol.payManner+""));
		this.setOrderFoods(null);
		this.setGiftPrice(protocol.getGiftPrice());
		this.setDiscountPrice(protocol.getDiscountPrice());
		this.setCancelPrice(protocol.getCancelPrice());
		this.setErasePuotaPrice(protocol.getErasePrice());
		this.setRepaidPrice(protocol.getRepaidPrice());
		this.setActuralPrice(protocol.getActualPrice());
		this.setTotalPrice(protocol.calcPriceBeforeDiscount());
		this.setTableID(protocol.getDestTbl().getTableId());
		this.setTableAlias(protocol.getDestTbl().getAliasId());
		this.setTableName(protocol.getDestTbl().getName());
	}
	
	public Order(){
		this.payManner = Order.MANNER_CASH;
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
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(this.orderDate);
	}
	
	// 
	public String getPayMannerFormat() {
		switch(this.payManner){
			case Order.MANNER_CASH:
				return Order.MANNER_CASH_TEXT;
			case Order.MANNER_CREDIT_CARD:
				return Order.MANNER_CREDIT_CARD_TEXT;
			case Order.MANNER_MEMBER:
				return Order.MANNER_MEMBER_TEXT;
			case Order.MANNER_SIGN:
				return Order.MANNER_SIGN_TEXT;
			case Order.MANNER_HANG:
				return Order.MANNER_HANG_TEXT;
			default:
				return "";
		}
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
	public short getPayManner() {
		return payManner;
	}
	public void setPayManner(short payManner) {
		this.payManner = payManner;
	}
	public int getDiscountID() {
		return discountID;
	}
	public void setDiscountID(int discountID) {
		this.discountID = discountID;
	}
	public String getMemberID() {
		return memberID;
	}
	public void setMemberID(String memberID) {
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
	
}
