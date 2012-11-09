package com.wireless.pojo.dishesOrder;

import java.text.SimpleDateFormat;

import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.menuMgr.TasteBasic;
import com.wireless.pojo.menuMgr.TasteGroup;
import com.wireless.util.WebParams;

public class OrderFood {

	private String foodName;				// 菜品名称
	private long foodID;					// 菜品编号
	private int aliasID;					// 菜品账单编号
	private short status;					// 菜品状态    0x01:特价 0x02推荐  0x04:售完  0x08:赠送  0x10:时价
	private Kitchen kitchen;				// 厨房
	private int kitchenId;					// 厨房编号
	private TasteGroup tasteGroup;			// 口味			
	private double count;					// 数量	
	private double unitPrice;				// 菜品单价
	private double discount;				// 折扣率
	private double totalDiscount;			// 折扣额
	private boolean isSpecial;				// 是否特价
	private boolean isRecommed;				// 是否推荐
	private boolean isSoldout;				// 是否停售
	private boolean isGift;					// 是否赠送
	private boolean isCurrPrice;			// 是否时价
	private boolean isTemporary = false;	// 是否临时菜
	private int seqID;						// 流水号	
	private long orderDate = 0;				// 日期时间
	private String waiter;					// 服务员
	private double acturalPrice;			// 实价 = 总价
	private double totalPrice;				// 总价 = (菜品单价  + 口味价钱) * 折扣率 * 数量
	
	private short hangStatus = WebParams.FOOD_NORMAL;	// 菜品状态  0:正常,1:叫起,2:即起
	
	public OrderFood() {
		this.kitchen = new Kitchen();
		this.tasteGroup = new TasteGroup();
	}
	
	public String getFoodName() {
		return foodName;
	}
	public void setFoodName(String foodName) {
		this.foodName = foodName;
	}	
	public long getFoodID() {
		return foodID;
	}
	public void setFoodID(long foodID) {
		this.foodID = foodID;
	}
	public int getAliasID() {
		return aliasID;
	}
	public void setAliasID(int aliasID) {
		this.aliasID = aliasID;
	}
	public short getStatus() {
		return status;
	}
	public void setStatus(short status) {
		this.status = status;
	}
	public Kitchen getKitchen() {
		return kitchen;
	}
	public void setKitchen(Kitchen kitchen) {
		this.kitchen = kitchen;
	}
	public int getKitchenId() {
		kitchenId = this.kitchen.getKitchenID();
		return kitchenId;
	}
	public TasteGroup getTasteGroup() {
		return tasteGroup;
	}
	public void setTasteGroup(TasteGroup tasteGroup) {
		this.tasteGroup = tasteGroup == null ? new TasteGroup() : tasteGroup;
	}
	public double getCount() {
		return count;
	}
	public void setCount(double count) {
		this.count = count;
	}
	public double getUnitPrice() {
		return unitPrice;
	}
	public void setUnitPrice(double unitPrice) {
		this.unitPrice = unitPrice;
	}
	public double getDiscount() {
		if(isSpecial == true || isGift == true){
			discount = 1.00f;  // 特价和赠送菜品不打折
		}
		return discount;
	}
	public void setDiscount(double discount) {
		this.discount = discount;
	}
	public double getTotalDiscount() {
		totalDiscount = (unitPrice + this.getTastePrice()) * count * (1 - discount);
		return totalDiscount;
	}
	public boolean isSpecial() {
		isSpecial = ((status & WebParams.FS_SPECIAL) != 0);
		return isSpecial;
	}
	public boolean isRecommend() {
		isRecommed = ((status & WebParams.FS_RECOMMEND) != 0);
		return isRecommed;
	}
	public boolean isSoldout() {
		isSoldout = ((status & WebParams.FS_STOP) != 0);
		return isSoldout;
	}
	public boolean isGift() {
		isGift = ((status & WebParams.FS_GIFT) != 0);
		return isGift;
	}
	public boolean isCurrPrice() {
		isCurrPrice = ((status & WebParams.FS_CUR_PRICE) != 0);
		return isCurrPrice;
	}
	public boolean isTemporary() {
		return isTemporary;
	}
	public void setTemporary(boolean isTemporary) {
		this.isTemporary = isTemporary;
	}
	public int getSeqID() {
		return seqID;
	}
	public void setSeqID(int seqID) {
		this.seqID = seqID;
	}
	public long getOrderDate() {
		return orderDate;
	}
	public String getOrderDateFormat(){
		return this.orderDate > 0 ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(this.orderDate) : null;
	}
	public void setOrderDate(long orderDate) {
		this.orderDate = orderDate;
	}
	public String getWaiter() {
		return waiter;
	}
	public void setWaiter(String waiter) {
		this.waiter = waiter;
	}
	public double getActuralPrice() {
		acturalPrice = this.getTotalPrice();
		return acturalPrice;
	}
	public double getTotalPrice() {
		if(isSpecial == true || isGift == true){
			// 特价和赠送菜品不打折
			totalPrice = (unitPrice + this.getTastePrice()) * count; 
		}else{
			totalPrice = (unitPrice + this.getTastePrice()) * discount * count;
		}
		return totalPrice;
	}
	public short getHangStatus() {
		return hangStatus;
	}
	public void setHangStatus(short hangStatus) {
		this.hangStatus = hangStatus;
	}
	// 添加口味 (快捷操作) 等同于 TasteGroup.addTaste(FoodTaste ft);
	public void addTaste(TasteBasic ft){
		this.tasteGroup.addTaste(ft);
	}
	// 口味价钱 (快捷显示) 等同于 TasteGroup.getNormalTaste().getTastePrice()
	public double getTastePrice(){
		return this.tasteGroup != null && this.tasteGroup.getNormalTaste() != null ? this.tasteGroup.getNormalTaste().getTastePrice() : 0;
	}
	// 口味 (快捷显示) 等同于 TasteGroup.getNormalTaste().getTasteName()
	public String getTastePref(){
		return this.tasteGroup != null && this.tasteGroup.getNormalTaste() != null && this.tasteGroup.getNormalTaste().getTasteName().trim().length() > 0 ? this.tasteGroup.getNormalTaste().getTasteName() : "无口味";
	}
}
