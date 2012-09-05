package com.wireless.pojo.dishesOrder;

import java.text.SimpleDateFormat;

import com.wireless.protocol.Kitchen;
import com.wireless.protocol.Taste;
import com.wireless.util.WebParams;

public class OrderFood {

	private String foodName;				// 菜品名称
	private long foodID;					// 菜品编号
	private int aliasID;					// 菜品账单编号
	private short status;					// 菜品状态    0x01:特价 0x02推荐  0x04:售完  0x08:赠送  0x10:时价
	private Kitchen kitchen;				// 厨房
	private short kitchenId;				// 厨房编号
	private Taste[] taste = new Taste[3];	// 口味
	private String tastePref;				// 口味名称
	private int tasteID;					// 口味编号
	private int tasteIDTwo;					// 口味编号2
	private int tasteIDThree;				// 口味编号3
	private float tastePrice;				// 口味价格
	private float count;					// 数量	
	private float unitPrice;				// 菜品单价
	private float discount;					// 折扣率
	private float totalDiscount;			// 折扣额
	private boolean isSpecial;				// 是否特价
	private boolean isRecommed;				// 是否推荐
	private boolean isSoldout;				// 是否停售
	private boolean isGift;					// 是否赠送
	private boolean isCurrPrice;			// 是否时价
	private boolean isTemporary = false;	// 是否临时菜
	private Taste tmpTaste;					// 临时口味
	private boolean isTmpTaste;				// 是否临时口味
	private String tmpTastePref;			// 临时口味名称
	private float tmpTastePrice;			// 临时口味价格
	private int tmpTasteAlias;				// 临时口味编号
	private int seqID;						// 流水号	
	private long orderDate;					// 日期时间
	private String waiter;					// 服务员
	private float acturalPrice;				// 实价 = 菜品单价 * 折扣率 + 口味价钱
	private float totalPrice;				// 总价 = （菜品单价 * 折扣率 + 口味价格）* 数量
	
	private short hangStatus = WebParams.FOOD_NORMAL;			// 菜品状态  0:正常,1:叫起,2:即起
	
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
	public short getKitchenId() {
		kitchenId = this.kitchen.aliasID;
		return kitchenId;
	}
	public Taste[] getTaste() {
		return taste;
	}
	public void setTaste(Taste[] taste) {
		this.taste = taste;
	}
	public String getTastePref() {
		return tastePref;
	}
	public void setTastePref(String tastePref) {
		this.tastePref = tastePref;
	}
	
	public int getTasteID() {
		tasteID = this.getTaste()[0] != null ? this.getTaste()[0].aliasID : null;
		return tasteID;
	}
	
	public int getTasteIDTwo() {
		tasteIDTwo = this.taste[1] != null ? this.taste[1].aliasID : tasteIDTwo;
		return tasteIDTwo;
	}
	
	public int getTasteIDThree() {
		tasteIDThree = this.taste[2] != null ? this.taste[2].aliasID : tasteIDThree;
		return tasteIDThree;
	}
	
	public float getCount() {
		return count;
	}
	public void setCount(float count) {
		this.count = count;
	}
	public float getTastePrice() {
		return tastePrice;
	}
	public void setTastePrice(float tastePrice) {
		this.tastePrice = tastePrice;
	}
	public float getUnitPrice() {
		return unitPrice;
	}
	public void setUnitPrice(float unitPrice) {
		this.unitPrice = unitPrice;
	}
	public float getDiscount() {
		if(isSpecial == true || isGift == true){
			discount = 1.00f;  // 特价和赠送菜品不打折
		}
		return discount;
	}
	public void setDiscount(float discount) {
		this.discount = discount;
	}
	
	public float getTotalDiscount() {
		totalDiscount = unitPrice * count * (1 - discount);
		return totalDiscount;
	}
	
	public boolean isSpecial() {
		isSpecial = ((status & WebParams.FS_SPECIAL) != 0);
		return isSpecial;
	}
	
	public boolean isRecommed() {
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
	public Taste getTmpTaste() {
		return tmpTaste;
	}
	public void setTmpTaste(Taste tmpTaste) {
		this.tmpTaste = tmpTaste;
	}
	
	public boolean isTmpTaste() {
		isTmpTaste = this.tmpTaste != null ? true : false;
		return isTmpTaste;
	}
	
	public String getTmpTastePref() {
		tmpTastePref = this.tmpTaste != null ? this.tmpTaste.getPreference() : "";
		return tmpTastePref;
	}
	
	public float getTmpTastePrice() {
		tmpTastePrice = this.tmpTaste != null ? this.tmpTaste.getPrice() : 0.00f;
		return tmpTastePrice;
	}
	
	public int getTmpTasteAlias() {
		tmpTasteAlias = this.tmpTaste != null ? this.tmpTaste.aliasID : 0;
		return tmpTasteAlias;
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
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(this.orderDate);
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
	
	public float getActuralPrice() {
		acturalPrice = this.getTotalPrice();
		return acturalPrice;
	}
	
	public float getTotalPrice() {
		if(isSpecial == true || isGift == true){
			// 特价和赠送菜品不打折
			totalPrice = (unitPrice + tastePrice) * count; 
		}else{
			totalPrice = (unitPrice + tastePrice) * discount * count;
		}
		return totalPrice;
	}
		
	public short getHangStatus() {
		return hangStatus;
	}
	
	public void setHangStatus(short hangStatus) {
		this.hangStatus = hangStatus;
	}	
	
}
