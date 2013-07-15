package com.wireless.pojo.printScheme;

import com.wireless.pack.Reserved;

public enum PType {
	
	PRINT_UNKNOWN(Reserved.PRINT_UNKNOWN, "unknown"),
	PRINT_ORDER(Reserved.PRINT_ORDER, "summary to order"),
	PRINT_ORDER_DETAIL(Reserved.PRINT_ORDER_DETAIL, "detail to order"),
	PRINT_RECEIPT(Reserved.PRINT_RECEIPT, "receipt"),
	PRINT_EXTRA_FOOD(Reserved.PRINT_EXTRA_FOOD, "detail to extra food"),
	PRINT_CANCELLED_FOOD(Reserved.PRINT_CANCELLED_FOOD, "detail to cancelled food"),
	PRINT_TRANSFER_TABLE(Reserved.PRINT_TRANSFER_TABLE, "detail to transfer table"),
	PRINT_ALL_EXTRA_FOOD(Reserved.PRINT_ALL_EXTRA_FOOD, "summary to extra food"),
	PRINT_ALL_CANCELLED_FOOD(Reserved.PRINT_ALL_CANCELLED_FOOD, "summary to cancelled food"),
	PRINT_ALL_HURRIED_FOOD(Reserved.PRINT_ALL_HURRIED_FOOD, "summary to hurried food"),
	PRINT_HURRIED_FOOD(Reserved.PRINT_HURRIED_FOOD, "detail to hurried food"),
	PRINT_HISTORY_DAILY_SETTLE_RECEIPT(Reserved.PRINT_HISTORY_DAILY_SETTLE_RECEIPT, "history daily settle receipt"),
	PRINT_HISTORY_SHIFT_RECEIPT(Reserved.PRINT_HISTORY_SHIFT_RECEIPT, "history shift receipt"),
	PRINT_DAILY_SETTLE_RECEIPT(Reserved.PRINT_DAILY_SETTLE_RECEIPT, "temp daily settle receipt"),
	PRINT_TEMP_SHIFT_RECEIPT(Reserved.PRINT_TEMP_SHIFT_RECEIPT, "temp shift receipt"),
	PRINT_SHIFT_RECEIPT(Reserved.PRINT_SHIFT_RECEIPT, "shift receipt"),
	PRINT_TEMP_RECEIPT(Reserved.PRINT_TEMP_RECEIPT, "temp receipt"),
	PRINT_MEMBER_RECEIPT(Reserved.PRINT_MEMBER_RECEIPT, "member receipt");
	
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
