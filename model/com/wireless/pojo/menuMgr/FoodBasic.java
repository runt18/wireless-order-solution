package com.wireless.pojo.menuMgr;

public class FoodBasic {
	public static final int TASTE_SMART_REF = 1;
	public static final int TASTE_MANUAL_REF = 2;
	public static final String TASTE_SMART_REF_TEXT = "智能关联";
	public static final String TASTE_MANUAL_REF_TEXT = "人工关联";
	/**
	 * The status of the food.
	 * It can be the combination of values below.
	 */
	public static final short FS_SPECIAL = 0x01;		/* 特价 */
	public static final short FS_RECOMMEND = 0x02;		/* 推荐 */ 
	public static final short FS_STOP = 0x04;			/* 停售 */
	public static final short FS_GIFT = 0x08;			/* 赠送 */
	public static final short FS_CUR_PRICE = 0x10;		/* 时价 */
	public static final short FS_COMBO = 0x20;			/* 套菜 */
	public static final short FS_HOT = 0x40;			/* 热销 */
	public static final short FS_WEIGHT = 0x80;			/* 称重 */
	/**
	 * 
	 */
	public static final short FOOD_NORMAL = 0x0;		/* 正常 */
	public static final short FOOD_HANG_UP = 0x1;		/* 叫起 */
	public static final short FOOD_IMMEDIATE = 0x2;		/* 即起 */
	
	private int restaurantID;	// 餐厅编号
	private long foodID;		// 菜品数据库编号
	private long aliasID;		// 菜品自定义编号
	private String foodName;	// 菜品编号
	private String pinyin;		// 菜品拼音
	private float unitPrice;	// 菜品单价
	private long kitchenID;		// 菜品所属厨房编号
	private Kitchen kitchen;	// 菜品所属厨房信息
	private int status;			// 菜品状态    0x01:特价 0x02推荐  0x04:售完  0x08:赠送  0x10:时价 0x20:套菜
	private String desc;		// 菜品简介
	private String img;			// 图片简要路径
	private int tasteRefType;	// 菜品口味关联方式,默认智能关联       1:智能关联  2:人工关联
		
	public FoodBasic(){
		this.kitchen = new Kitchen();
		this.tasteRefType = FoodBasic.TASTE_SMART_REF;
	}
	public FoodBasic(com.wireless.protocol.Food pt){
		if(pt != null){
			this.tasteRefType = pt.getTasteRefType();
			this.restaurantID = pt.getRestaurantId();
			this.foodID = pt.getFoodId();
			this.aliasID = pt.getAliasId();
			this.foodName = pt.getName();
			this.pinyin = pt.getPinyin();
			this.unitPrice = pt.getPrice();
			this.kitchenID = pt.getKitchen().getId();
			this.kitchen = pt.getKitchen();
			this.status = pt.getStatus();
			this.desc = pt.desc;
			this.img = pt.image;
		}
	}
	
	public int getRestaurantID() {
		return restaurantID;
	}
	public void setRestaurantID(int restaurantID) {
		this.restaurantID = restaurantID;
	}
	public long getFoodID() {
		return foodID;
	}
	public void setFoodID(long foodID) {
		this.foodID = foodID;
	}
	public long getAliasID() {
		return aliasID;
	}
	public void setAliasID(long aliasID) {
		this.aliasID = aliasID;
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
	public long getKitchenID() {
		return kitchenID;
	}
	public void setKitchenID(long kitchenID) {
		this.kitchenID = kitchenID;
	}
	public Kitchen getKitchen() {
		return kitchen;
	}
	public void setKitchen(Kitchen kitchen) {
		this.kitchen = kitchen;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
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
		return ((this.status & FoodBasic.FS_SPECIAL) != 0);
	}
	public void setSpecial(String isSpecial){
		this.setSpecial(isSpecial == null ? false : Boolean.valueOf(isSpecial));
	}
	public void setSpecial(boolean isSpecial){
		if(isSpecial){
			this.status |= FoodBasic.FS_SPECIAL;
		}else{
			this.status &= ~FoodBasic.FS_SPECIAL;
		}
	}
	
	
	/**
	 * 是否推荐
	 * @return
	 */
	public boolean isRecommend(){
		return ((this.status & FoodBasic.FS_RECOMMEND) != 0);
	}
	public void setRecommend(String isRecommend){
		this.setRecommend(isRecommend == null ? false : Boolean.valueOf(isRecommend));
	}
	public void setRecommend(boolean isRecommend){
		if(isRecommend){
			this.status |= FoodBasic.FS_RECOMMEND;
		}else{
			this.status &= ~FoodBasic.FS_RECOMMEND;
		}
	}
	
	/**
	 * 是否停售
	 * @return
	 */
	public boolean isStop(){
		return ((this.status & FoodBasic.FS_STOP) != 0);
	}
	public void setStop(String isStop){
		this.setStop(isStop == null ? false : Boolean.valueOf(isStop));
	}
	public void setStop(boolean isStop){
		if(isStop){
			this.status |= FoodBasic.FS_STOP;
		}else{
			this.status &= ~FoodBasic.FS_STOP;
		}
	}
	
	/**
	 * 是否赠送
	 * @return
	 */
	public boolean isGift(){
		return ((this.status & FoodBasic.FS_GIFT) != 0);	
	}
	public void setGift(String isGift){
		this.setGift(isGift == null ? false : Boolean.valueOf(isGift));
	}
	public void setGift(boolean isGift){
		if(isGift){
			this.status |= FoodBasic.FS_GIFT;
		}else{
			this.status &= ~FoodBasic.FS_GIFT;
		}
	}
	
	/**
	 * 是否时价
	 * @return
	 */
	public boolean isCurrPrice(){
		return ((this.status & FoodBasic.FS_CUR_PRICE) != 0);
	}
	public void setCurrPrice(String isCurrPrice){
		this.setCurrPrice(isCurrPrice == null ? false : Boolean.valueOf(isCurrPrice));
	}
	public void setCurrPrice(boolean isCurrPrice){
		if(isCurrPrice){
			this.status |= FoodBasic.FS_CUR_PRICE;
		}else{
			this.status &= ~FoodBasic.FS_CUR_PRICE;
		}
	}
	
	/**
	 * 是否套菜
	 * @return
	 */
	public boolean isCombination(){
		return ((this.status & FoodBasic.FS_COMBO) != 0);
	}
	public void setCombination(String isCombination){
		this.setCombination(isCombination == null ? false : Boolean.valueOf(isCombination));
	}
	public void setCombination(boolean isCombination){
		if(isCombination){
			this.status |= FoodBasic.FS_COMBO;
		}else{
			this.status &= ~FoodBasic.FS_COMBO;
		}
	}
	
	/**
	 * 是否热销
	 * @return
	 */
	public boolean isHot(){
		return ((this.status & FoodBasic.FS_HOT) != 0);
	}
	public void setHot(String isHot){
		this.setHot(isHot == null ? false : Boolean.valueOf(isHot));
	}
	public void setHot(boolean isHot){
		if(isHot){
			this.status |= FoodBasic.FS_HOT;
		}else{
			this.status &= ~FoodBasic.FS_HOT;
		}
	}
	
	/**
	 * 是否称重
	 * @return
	 */
	public boolean isWeight(){
		return ((this.status & FoodBasic.FS_WEIGHT) != 0);
	}
	public void setWeight(String isWeight){
		this.setWeight(isWeight == null ? false : Boolean.valueOf(isWeight));
	}
	public void setWeight(boolean isWeight){
		if(isWeight){
			this.status |= FoodBasic.FS_WEIGHT;
		}else{
			this.status &= ~FoodBasic.FS_WEIGHT;
		}
	}
}
