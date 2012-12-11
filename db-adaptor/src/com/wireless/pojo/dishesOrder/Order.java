package com.wireless.pojo.dishesOrder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.wireless.pojo.system.Table;
import com.wireless.util.WebParams;

public class Order {
	private int id;										// 账单编号
	private long seq_id;								// 数据编号
	private int restaurantID;							// 餐厅编号
	private long orderDate;								// 账单最后修改时间
	private float totalPrice;							// 总金额
	private float acturalPrice;							// 实收
	private int customNum;								// 客户人数
	private String waiter;								// 服务员名
	private int payManner = WebParams.MANNER_CASH;		// 结账方式  1:现金  2:刷卡  3:会员卡  4:签单  5:挂账
	private int discountID;								// 折扣方案编号
	private String memberID;							// 会员编号
	private String member;								// 会员名称
	private int terminalModel;							//
	private int terminalPin;							// 
	private Table destTbl;								// 
	private Table destTbl2;								// 
	private Table srcTbl;								// 
	private float totalPrice2;							//
	private int category = WebParams.CATE_NORMAL;		// 订单类型
	private String comment;								// 备注
	private float serviceRate;							// 服务费率
	private float minCost;								// 最低消费金额
	private float giftPrice;							// 赠送金额
	private float discountPrice;						// 折扣金额
	private float returnPrice;							// 退菜金额
	private float erasrPuotaPrice;						// 抹数金额
	private boolean isPaid;								// 是否反结账
	private boolean isDiscount;							// 是否打折
	private boolean isGift;								// 是否赠送
	private boolean isReturn;							// 是否退菜
	private boolean isErasePuota;						// 是否抹数
	private List<OrderFood> orderFoods;
	
	public Order(){
		this.srcTbl = new Table();
		this.destTbl = new Table();
		this.destTbl2 = new Table();
		this.orderFoods = new ArrayList<OrderFood>();
	}
	
	public String getOrderDateFormat(){
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(this.orderDate);
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public long getSeq_id() {
		return seq_id;
	}
	public void setSeq_id(long seq_id) {
		this.seq_id = seq_id;
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
	public int getPayManner() {
		return payManner;
	}
	public void setPayManner(int payManner) {
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
	public Table getDestTbl() {
		return destTbl;
	}
	public void setDestTbl(Table destTbl) {
		this.destTbl = destTbl;
	}
	public Table getDestTbl2() {
		return destTbl2;
	}
	public void setDestTbl2(Table destTbl2) {
		this.destTbl2 = destTbl2;
	}
	public Table getSrcTbl() {
		return srcTbl;
	}
	public void setSrcTbl(Table srcTbl) {
		this.srcTbl = srcTbl;
	}
	public float getTotalPrice2() {
		return totalPrice2;
	}
	public void setTotalPrice2(float totalPrice2) {
		this.totalPrice2 = totalPrice2;
	}
	public int getCategory() {
		return category;
	}
	public void setCategory(int category) {
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
	public boolean isPaid() {
		return isPaid;
	}
	public void setPaid(boolean isPaid) {
		this.isPaid = isPaid;
	}
	public void setPaid(int arg) {
		if(arg == 0){
			this.isPaid = false;
		}else if(arg == 1){
			this.isPaid = true;
		}else{
			this.isPaid = false;
		}
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
	public float getReturnPrice() {
		return returnPrice;
	}
	public void setReturnPrice(float returnPrice) {
		this.returnPrice = returnPrice;
	}
	public float getErasrPuotaPrice() {
		return erasrPuotaPrice;
	}
	public void setErasrPuotaPrice(float erasrPuotaPrice) {
		this.erasrPuotaPrice = erasrPuotaPrice;
	}
	public boolean isDiscount() {
		return isDiscount;
	}
	public void setDiscount(boolean isDiscount) {
		this.isDiscount = isDiscount;
	}
	public boolean isGift() {
		return isGift;
	}
	public void setGift(boolean isGift) {
		this.isGift = isGift;
	}
	public boolean isReturn() {
		return isReturn;
	}
	public void setReturn(boolean isReturn) {
		this.isReturn = isReturn;
	}
	public boolean isErasePuota() {
		return isErasePuota;
	}
	public void setErasePuota(boolean isErasePuota) {
		this.isErasePuota = isErasePuota;
	}
	
}
