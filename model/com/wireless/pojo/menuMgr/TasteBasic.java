package com.wireless.pojo.menuMgr;

public class TasteBasic {
	
	public static final int CATE_TASTE = 0x0;		/* 口味 */
	public static final int CATE_STYLE = 0x1;		/* 做法 */
	public static final int CATE_SPEC = 0x2;		/* 规格 */
	public static final int CALC_PRICE = 0x0;		/* 按价格计算  */
	public static final int CALC_RATE = 0x1;		/* 按比例计算  */
	public static final String CATE_TASTE_TEXT = "口味"; 
	public static final String CATE_STYLE_TEXT = "做法"; 
	public static final String CATE_SPEC_TEXT = "规格"; 
	public static final String CALC_PRICE_TEXT = "按价格"; 
	public static final String CALC_RATE_TEXT = "按比例";
	
	private int restaurantID;			// 餐厅编号
	private int rank = 0;				// 口味排名
	private int tasteID;				// 口味编号
	private int tasteAliasID;			// 口味自定义编号
	private String tasteName = "无口味";	// 口味名称
	private float tastePrice;			// 口味价格
	private float tasteRate;			// 口味比例
	private int tasteCategory;			// 口味类型    0:口味  1:做法     2:规格
	private int tasteCalc;				// 口味计算方式          0:按价格     1:按比例
	private int type;					// 操作类型	0:默认    1:系统保留(不可删除)
	
	public TasteBasic(){}
	
	public TasteBasic(com.wireless.protocol.Taste pt){
		if(pt == null)
			return;
		this.restaurantID = pt.getRestaurantId();
		this.tasteID = pt.getTasteId();
		this.tasteAliasID = pt.getAliasId();
		this.tasteName = pt.getPreference();
		this.tastePrice = pt.getPrice();
		this.tasteRate = pt.getRate();
		this.tasteCategory = pt.getCategory();
		this.tasteCalc = pt.getCalc();
		this.type = pt.getType();
	}
	
	public int getRestaurantID() {
		return restaurantID;
	}
	public void setRestaurantID(int restaurantID) {
		this.restaurantID = restaurantID;
	}
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
	public int getTasteCategory() {
		return tasteCategory;
	}
	public String getTasteCategoryFormat() {
		if(tasteCategory == TasteBasic.CATE_TASTE){
			return TasteBasic.CATE_TASTE_TEXT;
		}else if(tasteCategory == TasteBasic.CATE_STYLE){
			return TasteBasic.CATE_STYLE_TEXT;
		}else if(tasteCategory == TasteBasic.CATE_SPEC){
			return TasteBasic.CATE_SPEC_TEXT;
		}else{
			return null;
		}
	}
	public void setTasteCategory(int tasteCategory) {
		this.tasteCategory = tasteCategory;
	}
	public int getTasteCalc() {
		return tasteCalc;
	}
	public String getTasteCalcFormat() {
		if(tasteCalc == TasteBasic.CALC_PRICE){
			return TasteBasic.CALC_PRICE_TEXT;
		}else if(tasteCalc == TasteBasic.CALC_RATE){
			return TasteBasic.CALC_RATE_TEXT;
		}else{
			return null;
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
	
	/**
	 * 
	 * @param pojo
	 * @param clazz
	 * @return
	 */
	public static Object changeToOther(TasteBasic pojo, Class<?> clazz){
		Object obj = null;
		if(clazz.equals(com.wireless.protocol.Taste.class)){
			com.wireless.protocol.Taste pt = new com.wireless.protocol.Taste();
			
			pt.setTasteId(pojo.getTasteID());
			pt.setAliasId(pojo.getTasteAliasID());
			pt.setPreference(pojo.getTasteName());
			pt.setPrice(pojo.getTastePrice());
			pt.setRestaurantId(pojo.getRestaurantID());
			pt.setRate(pojo.getTasteRate());
			pt.setCalc((short) pojo.getTasteCalc());
			pt.setCategory((short) pojo.getTasteCategory());
			pt.setType((short) pojo.getType());
			
			obj = pt;
		}
		return obj;
	}
	
}
