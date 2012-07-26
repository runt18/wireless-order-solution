package com.wireless.pojo.menuMgr;

import com.wireless.util.WebParams;

public class FoodTaste {
	private long foodID;				// 菜品编号
	private String foodName;				// 菜品名称
	private long restaurantID;			// 餐厅编号
	private int rank = 0;				// 口味排名
	private long tasteID;				// 口味编号
	private long tasteAlias;
	private String tasteName;			// 口味名称
	private float tastePrice;			// 口味价格
	private float tasteRate;			// 口味比例
	private long tasteCategory;			// 口味类型    0:口味  1:做法     2:规格
	private long tasteCalc;				// 口味计算方式          0:按价格     1:按比例
	
	
	public long getFoodID() {
		return foodID;
	}
	public void setFoodID(long foodID) {
		this.foodID = foodID;
	}
	public String getFoodName() {
		return foodName;
	}
	public void setFoodName(String foodName) {
		this.foodName = foodName;
	}
	public long getRestaurantID() {
		return restaurantID;
	}
	public void setRestaurantID(long restaurantID) {
		this.restaurantID = restaurantID;
	}
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
	public long getTasteID() {
		return tasteID;
	}
	public void setTasteID(long tasteID) {
		this.tasteID = tasteID;
	}
	public long getTasteAlias() {
		return tasteAlias;
	}
	public void setTasteAlias(long tasteAlias) {
		this.tasteAlias = tasteAlias;
	}
	public String getTasteName() {
		return tasteName;
	}
	public void setTasteName(String tasteName) {
		this.tasteName = tasteName;
	}
	public float getTastePrice() {
		return tastePrice;
	}
	public void setTastePrice(float tastePrice) {
		this.tastePrice = tastePrice;
	}
	public float getTasteRate() {
		return tasteRate;
	}
	public void setTasteRate(float tasteRate) {
		this.tasteRate = tasteRate;
	}
	public long getTasteCategory() {
		return tasteCategory;
	}
	public String getTasteCategoryFormat() {
		if(tasteCategory == WebParams.CATE_TASTE){
			return WebParams.CATE_TASTE_TEXT;
		}else if(tasteCategory == WebParams.CATE_STYLE){
			return WebParams.CATE_STYLE_TEXT;
		}else if(tasteCategory == WebParams.CATE_SPEC){
			return WebParams.CATE_SPEC_TEXT;
		}else{
			return "";
		}
	}
	public void setTasteCategory(long tasteCategory) {
		this.tasteCategory = tasteCategory;
	}
	public long getTasteCalc() {
		return tasteCalc;
	}
	public String getTasteCalcFormat() {
		if(tasteCalc == WebParams.CALC_PRICE){
			return WebParams.CALC_PRICE_TEXT;
		}else if(tasteCalc == WebParams.CALC_RATE){
			return WebParams.CALC_RATE_TEXT;
		}else{
			return "";
		}
	}
	public void setTasteCalc(long tasteCalc) {
		this.tasteCalc = tasteCalc;
	}
	
}
