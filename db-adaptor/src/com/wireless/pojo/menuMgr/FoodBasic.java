package com.wireless.pojo.menuMgr;

import com.wireless.util.WebParams;

public class FoodBasic {
	
	private int restaurantID;        // 餐厅编号
	private int foodID;				 // 菜品数据库编号
	private int foodAlias;			 // 菜品自定义编号
	private int foodName;			 // 菜品编号
	private String pinyin;			 // 菜品拼音
	private float unitPriec;	     // 菜品单价
	private int kitchenId;		 	 // 菜品所属厨房数据库编号
	private int kitchenAlias;		 // 菜品所属厨房已定义编号
	private int kitchenName;		 // 菜品所属厨房名称
	private short status;			 // 菜品状态    0x01:特价 0x02推荐  0x04:售完  0x08:赠送  0x10:时价
	private int tasteRefType = 1;    // 菜品口味关联方式,默认智能关联       1:智能关联  2:人工关联
	
	public int getRestaurantID() {
		return restaurantID;
	}
	public void setRestaurantID(int restaurantID) {
		this.restaurantID = restaurantID;
	}
	public int getFoodID() {
		return foodID;
	}
	public void setFoodID(int foodID) {
		this.foodID = foodID;
	}
	public int getFoodAlias() {
		return foodAlias;
	}
	public void setFoodAlias(int foodAlias) {
		this.foodAlias = foodAlias;
	}
	public int getFoodName() {
		return foodName;
	}
	public void setFoodName(int foodName) {
		this.foodName = foodName;
	}
	public String getPinyin() {
		return pinyin;
	}
	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
	}
	public float getUnitPriec() {
		return unitPriec;
	}
	public void setUnitPriec(float unitPriec) {
		this.unitPriec = unitPriec;
	}
	public int getKitchenId() {
		return kitchenId;
	}
	public void setKitchenId(int kitchenId) {
		this.kitchenId = kitchenId;
	}
	public int getKitchenAlias() {
		return kitchenAlias;
	}
	public void setKitchenAlias(int kitchenAlias) {
		this.kitchenAlias = kitchenAlias;
	}	
	public int getKitchenName() {
		return kitchenName;
	}
	public void setKitchenName(int kitchenName) {
		this.kitchenName = kitchenName;
	}
	public short getStatus() {
		return status;
	}
	public void setStatus(short status) {
		this.status = status;
	}
	public int getTasteRefType() {
		return tasteRefType;
	}
	public void setTasteRefType(int tasteRefType) {
		this.tasteRefType = tasteRefType;
	}
	
	/**
	 * 是否特价
	 * @return
	 */
	public boolean isSpecial(){
		return ((this.status & WebParams.SPECIAL) != 0);
	}
	
	/**
	 * 是否推荐
	 * @return
	 */
	public boolean isRecommend(){
		return ((this.status & WebParams.RECOMMEND) != 0);
	}
	
	/**
	 * 是否售完
	 * @return
	 */
	public boolean isSellOut(){
		return ((this.status & WebParams.SELL_OUT) != 0);
	}
	
	/**
	 * 是否赠送
	 * @return
	 */
	public boolean isGift(){
		return ((this.status & WebParams.GIFT) != 0);	
	}
	
	/**
	 * 是否时价
	 * @return
	 */
	public boolean isCurPrice(){
		return ((this.status & WebParams.CUR_PRICE) != 0);
	}
	
}
