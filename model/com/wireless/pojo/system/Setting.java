package com.wireless.pojo.system;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class Setting implements Jsonable{
	/* 结帐单显示的选项设置  */
	public final static int RECEIPT_DISCOUNT = 0x01;		//结帐单是否显示折扣
	public final static int RECEIPT_AMOUNT = 0x02;			//结帐单是否显示数量
	public final static int RECEIPT_STATUS = 0x04;			//结帐单是否显示状态
	public final static int RECEIPT_TOTAL_DISCOUNT = 0x08;	//结帐单是否显示折扣额
	public final static int RECEIPT_DEF = RECEIPT_DISCOUNT | RECEIPT_AMOUNT | RECEIPT_STATUS | RECEIPT_TOTAL_DISCOUNT;
	
	/**
	 * 结账时尾数处理方式
	 * 	0:小数部分不处理(默认)
	 * 	1:小数抹零
	 * 	2:小数四舍五入
	 * @author WuZY
	 *
	 */
	public static enum Tail{
		NO_ACTION(0, "小数部分不处理"),
		DECIMAL_CUT(1, "小数抹零"),
		DECIMAL_ROUND(2, "小数四舍五入");
		
		private int value = 0;
		private String text;
		Tail(int value, String text){
			this.value = value;
			this.text = text;
		}
		public int getValue() {
			return value;
		}
		public String getText() {
			return text;
		}
		public boolean isDecimalCut(){
			return this == Tail.DECIMAL_CUT;
		}
		public boolean isDecimalRound(){
			return this == Tail.DECIMAL_ROUND;
		}
		
		public static Tail valueOf(int value){
			for(Tail temp : values()){
				if(temp.getValue() == value)
					return temp;
			}
			throw new IllegalArgumentException("The tail value(val = " + value + ") passed is invalid.");
		}
	}
	
	/**
	 * 盘点任务状态
	 * 	1:已完成
	 * 	2:进行中(默认)
	 * @author WuZY
	 *
	 */
	public static enum StockTake{
		FINISH(1, "已完成"),
		CHECKING(2, "进行中");
		
		private int value = 0;
		private String text;
		StockTake(int value, String text){
			this.value = value;
			this.text = text;
		}
		public int getValue() {
			return value;
		}
		public String getText() {
			return text;
		}
		public boolean isChecking(){
			return this == StockTake.CHECKING;
		}
		public boolean isFinish(){
			return this == StockTake.CHECKING;
		}
		public static StockTake valueOf(int value){
			for(StockTake temp : values()){
				if(temp.getValue() == value)
					return temp;
			}
			throw new IllegalArgumentException("The stockTake value(val = " + value + ") passed is invalid.");
		}
		public static boolean isChecking(StockTake s){
			if(s == null)
				throw new NullPointerException("The stockTake is null.");
			return s == StockTake.CHECKING;
		}
		public static boolean isFinish(StockTake s){
			if(s == null)
				throw new NullPointerException("The stockTake is null.");
			return s == StockTake.FINISH;
		}
	}
	
	private int id;
	private int restaurantId;
	private Tail priceTail = Tail.NO_ACTION;	// 金额尾数处理方式  0:不处理  1:抹零 2:四舍五入
	private int receiptStyle;  
	private int eraseQuota;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getRestaurantId() {
		return restaurantId;
	}
	
	public void setRestaurantId(int restaurantId) {
		this.restaurantId = restaurantId;
	}
	
	public Tail getPriceTail() {
		return priceTail;
	}
	
	public void setPriceTail(Tail priceTail) {
		this.priceTail = priceTail;
	}
	
	public int getReceiptStyle() {
		return receiptStyle;
	}
	
	public void setReceiptStyle(int receiptStyle) {
		this.receiptStyle = receiptStyle;
	}
	
	public int getEraseQuota() {
		return eraseQuota;
	}
	
	public void setEraseQuota(int eraseQuota) {
		this.eraseQuota = eraseQuota;
	}
	
	public boolean hasEraseQuota(){
		return this.eraseQuota != 0;
	}
	
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.getId());
		jm.putInt("restaurantID", this.getRestaurantId());
		jm.putString("priceTailText", this.priceTail != null ? this.priceTail.text : null);
		jm.putInt("priceTailValue", this.priceTail != null ? this.priceTail.value : 0);
		jm.putInt("receiptStyle", this.getReceiptStyle());
		jm.putInt("eraseQuota", this.getEraseQuota());
		return jm;
	}
	
	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
		
	}
	
}
