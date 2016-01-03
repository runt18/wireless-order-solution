package com.wireless.pojo.printScheme;


public enum PType {
	
	PRINT_UNKNOWN(0, "未知类型"),
	PRINT_ORDER(1, "点菜总单"),
	PRINT_ORDER_DETAIL(2, "点菜分单"),
	PRINT_RECEIPT(3, "结帐"),
	PRINT_EXTRA_FOOD_DETAIL(4, "加菜分单"),
	PRINT_CANCELLED_FOOD_DETAIL(5, "退菜分单"),
	PRINT_TRANSFER_TABLE(6, "转台"),
	PRINT_ALL_EXTRA_FOOD(7, "加菜总单"),
	PRINT_ALL_CANCELLED_FOOD(8, "退菜总单"),
	PRINT_ALL_HURRIED_FOOD(9, "催菜"),
	PRINT_HURRIED_FOOD(10, "催菜"),
	PRINT_MEMBER_RECEIPT(11, "会员对账单"),
	PRINT_PAYMENT_RECEIPT(12, "交款表"),
	PRINT_HISTORY_PAYMENT_RECEIPT(13, "历史交款表"),
	PRINT_ORDER_PATCH(14, "补打总单"),
	PRINT_ORDER_DETAIL_PATCH(15, "补打明细"),
	PRINT_TRANSFER_FOOD(16, "转菜"),
	PRINT_WX_RECEIT(17, "微信支付"),
	PRINT_2ND_DISPLAY(18, "客显"),
	PRINT_WX_ORDER(19, "微信账单"),
	PRINT_BOOK(20, "预订"),
	PRINT_HISTORY_DAILY_SETTLE_RECEIPT(122, "历史日结"),
	PRINT_HISTORY_SHIFT_RECEIPT(123, "历史交班"),
	PRINT_DAILY_SETTLE_RECEIPT(124, "日结表"),
	PRINT_TEMP_SHIFT_RECEIPT(125, "交班表"),
	PRINT_SHIFT_RECEIPT(126, "交班表"),
	PRINT_TEMP_RECEIPT(127, "暂结");
	
	private final int mVal;
	private final String mDesc;
	
	PType(int val, String desc){
		mVal = val;
		mDesc = desc;
	}
	
	public int getVal(){
		return mVal;
	}
	
	public String getDesc(){
		return mDesc;
	}
	
	public static PType valueOf(int val){
		for(PType type : values()){
			if(type.mVal == val){
				return type;
			}
		}
		return PRINT_UNKNOWN;
	}
	
	@Override
	public String toString(){
		return this.mDesc;
	}
	
	public boolean isSummary(){
		return this == PType.PRINT_ORDER || this == PType.PRINT_ALL_CANCELLED_FOOD || 
			   this == PType.PRINT_ALL_EXTRA_FOOD || this == PType.PRINT_ALL_HURRIED_FOOD ||
			   this == PType.PRINT_ORDER_PATCH;
	}
	
	public boolean isDetail(){
		return this == PType.PRINT_ORDER_DETAIL || this == PType.PRINT_EXTRA_FOOD_DETAIL || 
				this == PType.PRINT_CANCELLED_FOOD_DETAIL || this == PType.PRINT_HURRIED_FOOD ||
				this == PType.PRINT_ORDER_DETAIL_PATCH;
	}
	
	public boolean isTransTbl(){
		return this == PType.PRINT_TRANSFER_TABLE;
	}
	
	public boolean isTransFood(){
		return this == PType.PRINT_TRANSFER_FOOD;
	}
	
	public boolean isReceipt(){
		return this == PType.PRINT_RECEIPT || this == PType.PRINT_TEMP_RECEIPT;
	}
	
	public boolean isWxReceipt(){
		return this == PType.PRINT_WX_RECEIT;
	}
	
	public boolean isBook(){
		return this == PType.PRINT_BOOK;
	}
	
	public boolean isShift(){
		return this == PType.PRINT_SHIFT_RECEIPT || this == PType.PRINT_TEMP_SHIFT_RECEIPT || 
			   this == PType.PRINT_DAILY_SETTLE_RECEIPT || this == PType.PRINT_HISTORY_DAILY_SETTLE_RECEIPT ||
			   this == PType.PRINT_HISTORY_SHIFT_RECEIPT || this == PType.PRINT_PAYMENT_RECEIPT ||
			   this == PType.PRINT_HISTORY_PAYMENT_RECEIPT;
	}
	
	public boolean isMember(){
		return this == PType.PRINT_MEMBER_RECEIPT;
	}
	
	public boolean is2ndDisplay(){
		return this == PType.PRINT_2ND_DISPLAY;
	}
	
	public boolean isWxOrder(){
		return this == PType.PRINT_WX_ORDER;
	}
}
