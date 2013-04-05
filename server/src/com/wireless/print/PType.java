package com.wireless.print;

import com.wireless.pack.Reserved;

public enum PType {
	
	PRINT_UNKNOWN(Reserved.PRINT_UNKNOWN),
	PRINT_ORDER(Reserved.PRINT_ORDER),
	PRINT_ORDER_DETAIL(Reserved.PRINT_ORDER_DETAIL),
	PRINT_RECEIPT(Reserved.PRINT_RECEIPT),
	PRINT_EXTRA_FOOD(Reserved.PRINT_EXTRA_FOOD),
	PRINT_CANCELLED_FOOD(Reserved.PRINT_CANCELLED_FOOD),
	PRINT_TRANSFER_TABLE(Reserved.PRINT_TRANSFER_TABLE),
	PRINT_ALL_EXTRA_FOOD(Reserved.PRINT_ALL_EXTRA_FOOD),
	PRINT_ALL_CANCELLED_FOOD(Reserved.PRINT_ALL_CANCELLED_FOOD),
	PRINT_ALL_HURRIED_FOOD(Reserved.PRINT_ALL_HURRIED_FOOD),
	PRINT_HURRIED_FOOD(Reserved.PRINT_HURRIED_FOOD),
	PRINT_HISTORY_DAILY_SETTLE_RECEIPT(Reserved.PRINT_HISTORY_DAILY_SETTLE_RECEIPT),
	PRINT_HISTORY_SHIFT_RECEIPT(Reserved.PRINT_HISTORY_SHIFT_RECEIPT),
	PRINT_DAILY_SETTLE_RECEIPT(Reserved.PRINT_DAILY_SETTLE_RECEIPT),
	PRINT_TEMP_SHIFT_RECEIPT(Reserved.PRINT_TEMP_SHIFT_RECEIPT),
	PRINT_SHIFT_RECEIPT(Reserved.PRINT_SHIFT_RECEIPT),
	PRINT_TEMP_RECEIPT(Reserved.PRINT_TEMP_RECEIPT),
	PRINT_MEMBER_RECEIPT(Reserved.PRINT_MEMBER_RECEIPT);
	
	private final int mVal;
	
	PType(int val){
		mVal = val;
	}
	
	public int getVal(){
		return mVal;
	}
	
	public static PType valueOf(int val){
		for(PType type : values()){
			if(type.mVal == val){
				return type;
			}
		}
		return PRINT_UNKNOWN;
	}
	
	public String toString(){
		if(this == PType.PRINT_ORDER){
			return "(type : summary to order)";
			
		}else if(this == PType.PRINT_ALL_CANCELLED_FOOD){
			return "(type : summary to cancelled foods)";
			
		}else if(this == PType.PRINT_ALL_EXTRA_FOOD){
			return "(type : summary to extra foods)";

		}else if(this == PType.PRINT_ALL_HURRIED_FOOD){
			return "(type : summary to hurried foods)";

		}else if(this == PType.PRINT_ORDER_DETAIL){
			return "(type : detail to order)";

		}else if(this == PType.PRINT_EXTRA_FOOD){
			return "(type : detail to extra foods)";

		}else if(this == PType.PRINT_CANCELLED_FOOD){
			return "(type : detail to cancelled foods)";

		}else if(this == PType.PRINT_HURRIED_FOOD){
			return "(type : detail to hurried foods)";

		}else if(this == PType.PRINT_TRANSFER_TABLE){
			return "(type : transfer table)";

		}else if(this == PType.PRINT_RECEIPT){
			return "(type : receipt)";
			
		}else if(this == PType.PRINT_TEMP_RECEIPT){
			return "(type : temp receipt)";
			
		}else if(this == PRINT_SHIFT_RECEIPT){
			return "(type : shift receipt)";
			
		}else if(this == PRINT_TEMP_SHIFT_RECEIPT){
			return "(type : temp shift receipt)";
			
		}else if(this == PRINT_DAILY_SETTLE_RECEIPT){
			return "(type : daily settle receipt)";
			
		}else if(this == PRINT_HISTORY_DAILY_SETTLE_RECEIPT){
			return "(type : history daily settle receipt)";
			
		}else if(this == PRINT_HISTORY_SHIFT_RECEIPT){
			return "(type : history shift receipt)";
			
		}else if(this == PRINT_MEMBER_RECEIPT){
			return "(type : member receipt)";
			
		}else{
			return "(type : unknown)";
		}
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
