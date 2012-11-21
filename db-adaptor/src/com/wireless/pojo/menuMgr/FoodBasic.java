package com.wireless.pojo.menuMgr;

import com.wireless.util.WebParams;

public class FoodBasic {
	
	private int restaurantID;        // 餐厅编号
	private int foodID;				 // 菜品数据库编号
	private int foodAliasID;		 // 菜品自定义编号
	private String foodName;	     // 菜品编号
	private String pinyin;			 // 菜品拼音
	private double unitPrice;	     // 菜品单价
	private Kitchen kitchen = new Kitchen();	// 菜品所属厨房信息
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
	
	public double getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(double unitPrice) {
		this.unitPrice = unitPrice;
	}

	public Kitchen getKitchen() {
		return kitchen;
	}

	public void setKitchen(Kitchen kitchen) {
		this.kitchen = kitchen;
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
	public void setSpecial(String isSpecial){
		this.setSpecial(isSpecial == null ? false : Boolean.valueOf(isSpecial));
	}
	public void setSpecial(boolean isSpecial){
		if(isSpecial){
			this.status |= WebParams.FS_SPECIAL;
		}else{
			this.status &= ~WebParams.FS_SPECIAL;
		}
	}
	
	
	/**
	 * 是否推荐
	 * @return
	 */
	public boolean isRecommend(){
		return ((this.status & WebParams.FS_RECOMMEND) != 0);
	}
	public void setRecommend(String isRecommend){
		this.setRecommend(isRecommend == null ? false : Boolean.valueOf(isRecommend));
	}
	public void setRecommend(boolean isRecommend){
		if(isRecommend){
			this.status |= WebParams.FS_RECOMMEND;
		}else{
			this.status &= ~WebParams.FS_RECOMMEND;
		}
	}
	
	/**
	 * 是否停售
	 * @return
	 */
	public boolean isStop(){
		return ((this.status & WebParams.FS_STOP) != 0);
	}
	public void setStop(String isStop){
		this.setStop(isStop == null ? false : Boolean.valueOf(isStop));
	}
	public void setStop(boolean isStop){
		if(isStop){
			this.status |= WebParams.FS_STOP;
		}else{
			this.status &= ~WebParams.FS_STOP;
		}
	}
	
	/**
	 * 是否赠送
	 * @return
	 */
	public boolean isGift(){
		return ((this.status & WebParams.FS_GIFT) != 0);	
	}
	public void setGift(String isGift){
		this.setGift(isGift == null ? false : Boolean.valueOf(isGift));
	}
	public void setGift(boolean isGift){
		if(isGift){
			this.status |= WebParams.FS_GIFT;
		}else{
			this.status &= ~WebParams.FS_GIFT;
		}
	}
	
	/**
	 * 是否时价
	 * @return
	 */
	public boolean isCurrPrice(){
		return ((this.status & WebParams.FS_CUR_PRICE) != 0);
	}
	public void setCurrPrice(String isCurrPrice){
		this.setCurrPrice(isCurrPrice == null ? false : Boolean.valueOf(isCurrPrice));
	}
	public void setCurrPrice(boolean isCurrPrice){
		if(isCurrPrice){
			this.status |= WebParams.FS_CUR_PRICE;
		}else{
			this.status &= ~WebParams.FS_CUR_PRICE;
		}
	}
	
	/**
	 * 是否套菜
	 * @return
	 */
	public boolean isCombination(){
		return ((this.status & WebParams.FS_COMBO) != 0);
	}
	public void setCombination(String isCombination){
		this.setCombination(isCombination == null ? false : Boolean.valueOf(isCombination));
	}
	public void setCombination(boolean isCombination){
		if(isCombination){
			this.status |= WebParams.FS_COMBO;
		}else{
			this.status &= ~WebParams.FS_COMBO;
		}
	}
	
	/**
	 * 是否热销
	 * @return
	 */
	public boolean isHot(){
		return ((this.status & WebParams.FS_HOT) != 0);
	}
	public void setHot(String isHot){
		this.setHot(isHot == null ? false : Boolean.valueOf(isHot));
	}
	public void setHot(boolean isHot){
		if(isHot){
			this.status |= WebParams.FS_HOT;
		}else{
			this.status &= ~WebParams.FS_HOT;
		}
	}
	
}
