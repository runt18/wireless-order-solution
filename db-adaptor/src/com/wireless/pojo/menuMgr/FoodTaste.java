package com.wireless.pojo.menuMgr;

import com.wireless.util.WebParams;

public class FoodTaste extends FoodBasic{
	
	private int rank = 0;				// 口味排名
	private int tasteID;				// 口味编号
	private int tasteAliasID;			// 口味自定义编号
	private String tasteName;			// 口味名称
	private double tastePrice;			// 口味价格
	private double tasteRate;			// 口味比例
	private int tasteCategory;			// 口味类型    0:口味  1:做法     2:规格
	private int tasteCalc;				// 口味计算方式          0:按价格     1:按比例
	private int type;					// 操作类型	0:默认    1:系统保留(不可删除)
	
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
	public int getTasteID() {
		return tasteID;
	}
	public void setTasteID(int tasteID) {
		this.tasteID = tasteID;
	}
	public int getTasteAliasID() {
		return tasteAliasID;
	}
	public void setTasteAliasID(int tasteAliasID) {
		this.tasteAliasID = tasteAliasID;
	}
	public String getTasteName() {
		return tasteName;
	}
	public void setTasteName(String tasteName) {
		this.tasteName = tasteName;
	}
	public double getTastePrice() {
		return tastePrice;
	}
	public void setTastePrice(double tastePrice) {
		this.tastePrice = tastePrice;
	}
	public double getTasteRate() {
		return tasteRate;
	}
	public void setTasteRate(double tasteRate) {
		this.tasteRate = tasteRate;
	}
	public int getTasteCategory() {
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
	public void setTasteCategory(int tasteCategory) {
		this.tasteCategory = tasteCategory;
	}
	public int getTasteCalc() {
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
	public void setTasteCalc(int tasteCalc) {
		this.tasteCalc = tasteCalc;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	
}
