package com.wireless.pojo.menuMgr;

import com.wireless.util.WebParams;

public class FoodBasic {
	
	private int restaurantID;        // 餐厅编号
	private int foodID;				 // 菜品数据库编号
	private int foodAliasID;		 // 菜品自定义编号
	private String foodName;	     // 菜品编号
	private String pinyin;			 // 菜品拼音
	private float unitPrice;	     // 菜品单价
	private int kitchenID;		 	 // 菜品所属厨房数据库编号
	private int kitchenAliasID;		 // 菜品所属厨房已定义编号
	private String kitchenName;		 // 菜品所属厨房名称
	private byte status;			 // 菜品状态    0x01:特价 0x02推荐  0x04:售完  0x08:赠送  0x10:时价 0x20:套菜
	private String desc;			 // 菜品简介
	private String img;				 // 图片名称
	private int tasteRefType = WebParams.TASTE_SMART_REF;  // 菜品口味关联方式,默认智能关联       1:智能关联  2:人工关联
		
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

	public int getFoodAliasID() {
		return foodAliasID;
	}

	public void setFoodAliasID(int foodAliasID) {
		this.foodAliasID = foodAliasID;
	}

	public String getFoodName() {
		return foodName;
	}

	public void setFoodName(String foodName) {
		this.foodName = foodName;
	}

	public String getPinyin() {
		return pinyin;
	}

	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
	}
	
	public float getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(float unitPrice) {
		this.unitPrice = unitPrice;
	}

	public int getKitchenID() {
		return kitchenID;
	}

	public void setKitchenID(int kitchenID) {
		this.kitchenID = kitchenID;
	}

	public int getKitchenAliasID() {
		return kitchenAliasID;
	}

	public void setKitchenAliasID(int kitchenAliasID) {
		this.kitchenAliasID = kitchenAliasID;
	}

	public String getKitchenName() {
		return kitchenName;
	}

	public void setKitchenName(String kitchenName) {
		this.kitchenName = kitchenName;
	}

	public byte getStatus() {
		return status;
	}

	public void setStatus(byte status) {
		this.status = status;
	}
	
	public String getDesc() {
		desc = desc == null ? desc : desc.trim().length() == 0 ? null : desc;
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
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
		return ((this.status & WebParams.FS_SPECIAL) != 0);
	}
	
	/**
	 * 是否推荐
	 * @return
	 */
	public boolean isRecommend(){
		return ((this.status & WebParams.FS_RECOMMEND) != 0);
	}
	
	/**
	 * 是否停售
	 * @return
	 */
	public boolean isStop(){
		return ((this.status & WebParams.FS_STOP) != 0);
	}
	
	/**
	 * 是否赠送
	 * @return
	 */
	public boolean isGift(){
		return ((this.status & WebParams.FS_GIFT) != 0);	
	}
	
	/**
	 * 是否时价
	 * @return
	 */
	public boolean isCurrPrice(){
		return ((this.status & WebParams.FS_CUR_PRICE) != 0);
	}
	
	/**
	 * 是否套菜
	 * @return
	 */
	public boolean isCombination(){
		return ((this.status & WebParams.FS_COMBO) != 0);
	}
	
}
