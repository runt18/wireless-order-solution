package com.wireless.pojo.printScheme;


public enum PType {
	
	PRINT_UNKNOWN(0, "未知类型"),
	PRINT_ORDER(1, "下单"),
	PRINT_ORDER_DETAIL(2, "下单详细"),
	PRINT_RECEIPT(3, "结帐"),
	PRINT_EXTRA_FOOD_DETAIL(4, "加菜详细"),
	PRINT_CANCELLED_FOOD_DETAIL(5, "退菜详细"),
	PRINT_TRANSFER_TABLE(6, "转台"),
	PRINT_ALL_EXTRA_FOOD(7, "加菜"),
	PRINT_ALL_CANCELLED_FOOD(8, "退菜"),
	PRINT_ALL_HURRIED_FOOD(9, "催菜"),
	PRINT_HURRIED_FOOD(10, "催菜"),
	PRINT_MEMBER_RECEIPT(11, "会员对账单"),
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
			   this == PType.PRINT_ALL_EXTRA_FOOD || this == PType.PRINT_ALL_HURRIED_FOOD;
	}
	
	public boolean isDetail(){
		return this == PType.PRINT_ORDER_DETAIL || this == PType.PRINT_EXTRA_FOOD_DETAIL || 
				this == PType.PRINT_CANCELLED_FOOD_DETAIL || this == PType.PRINT_HURRIED_FOOD;
	}
	
	public boolean isTransTbl(){
		return this == PType.PRINT_TRANSFER_TABLE;
	}
	
	public boolean isReceipt(){
		return this == PType.PRINT_RECEIPT || this == PType.PRINT_TEMP_RECEIPT;
	}
	
	public boolean isShift(){
		return this == PType.PRINT_SHIFT_RECEIPT || this == PType.PRINT_TEMP_SHIFT_RECEIPT || 
			   this == PType.PRINT_DAILY_SETTLE_RECEIPT || this == PType.PRINT_HISTORY_DAILY_SETTLE_RECEIPT ||
			   this == PType.PRINT_HISTORY_SHIFT_RECEIPT;
	}
	
	public boolean isMember(){
		return this == PType.PRINT_MEMBER_RECEIPT;
	}
}
