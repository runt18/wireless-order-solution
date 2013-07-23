package com.wireless.pojo.printScheme;

import com.wireless.pack.Reserved;

public enum PType {
	
	PRINT_UNKNOWN(Reserved.PRINT_UNKNOWN, "未知类型"),
	PRINT_ORDER(Reserved.PRINT_ORDER, "下单"),
	PRINT_ORDER_DETAIL(Reserved.PRINT_ORDER_DETAIL, "下单详细"),
	PRINT_RECEIPT(Reserved.PRINT_RECEIPT, "结帐"),
	PRINT_EXTRA_FOOD(Reserved.PRINT_EXTRA_FOOD, "加菜详细"),
	PRINT_CANCELLED_FOOD(Reserved.PRINT_CANCELLED_FOOD, "退菜详细"),
	PRINT_TRANSFER_TABLE(Reserved.PRINT_TRANSFER_TABLE, "转台"),
	PRINT_ALL_EXTRA_FOOD(Reserved.PRINT_ALL_EXTRA_FOOD, "加菜"),
	PRINT_ALL_CANCELLED_FOOD(Reserved.PRINT_ALL_CANCELLED_FOOD, "退菜"),
	PRINT_ALL_HURRIED_FOOD(Reserved.PRINT_ALL_HURRIED_FOOD, "催菜"),
	PRINT_HURRIED_FOOD(Reserved.PRINT_HURRIED_FOOD, "催菜"),
	PRINT_HISTORY_DAILY_SETTLE_RECEIPT(Reserved.PRINT_HISTORY_DAILY_SETTLE_RECEIPT, "历史日结"),
	PRINT_HISTORY_SHIFT_RECEIPT(Reserved.PRINT_HISTORY_SHIFT_RECEIPT, "历史交班"),
	PRINT_DAILY_SETTLE_RECEIPT(Reserved.PRINT_DAILY_SETTLE_RECEIPT, "日结表"),
	PRINT_TEMP_SHIFT_RECEIPT(Reserved.PRINT_TEMP_SHIFT_RECEIPT, "交班表"),
	PRINT_SHIFT_RECEIPT(Reserved.PRINT_SHIFT_RECEIPT, "交班表"),
	PRINT_TEMP_RECEIPT(Reserved.PRINT_TEMP_RECEIPT, "暂结"),
	PRINT_MEMBER_RECEIPT(Reserved.PRINT_MEMBER_RECEIPT, "会员对账单");
	
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
		return this == PType.PRINT_ORDER_DETAIL || this == PType.PRINT_EXTRA_FOOD || 
				this == PType.PRINT_CANCELLED_FOOD || this == PType.PRINT_HURRIED_FOOD;
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
