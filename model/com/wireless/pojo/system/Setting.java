package com.wireless.pojo.system;

public class Setting {
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
		public static boolean isDecimalCut(Tail t){
			if(t == null)
				throw new NullPointerException("The tail is null.");
			return t == Tail.DECIMAL_CUT;
		}
		public static boolean isDecimalRound(Tail t){
			if(t == null)
				throw new NullPointerException("The tail is null.");
			return t == Tail.DECIMAL_ROUND;
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
	private int restaurantID;
	private Tail priceTail = Tail.NO_ACTION;	// 金额尾数处理方式  0:不处理  1:抹零 2:四舍五入
	private int receiptStyle;  
	private int eraseQuota;
	private long currentMonth;
	
	
	public long getCurrentMonth() {
		return currentMonth;
	}
	public void setCurrentMonth(long currentMonth) {
		this.currentMonth = currentMonth;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getRestaurantID() {
		return restaurantID;
	}
	public void setRestaurantID(int restaurantID) {
		this.restaurantID = restaurantID;
	}
	public String getPriceTailText(){
		return this.priceTail == null ? null : this.priceTail.getText();
	}
	public Integer getPriceTailValue(){
		return this.priceTail == null ? null : this.priceTail.getValue();
	}
	public Tail getPriceTail() {
		return priceTail;
	}
	public void setPriceTail(Tail priceTail) {
		this.priceTail = priceTail;
	}
	public void setPriceTail(int priceTail) {
		this.priceTail = Tail.valueOf(priceTail);
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
	
}
