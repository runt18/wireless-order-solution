package com.wireless.db;

import com.wireless.pojo.util.DateType;

public class DBTbl {
	
	public final DateType dateType;
	public final String orderTbl;
	public final String orderFoodTbl;
	public final String tgTbl;
	public final String ntgTbl;
	public final String shiftTbl;
	public final String paymentTbl;
	public final String moTbl;
	public final String mixedTbl;
	public final String dailyTbl;
	
	public DBTbl(DateType dateType){
		this.dateType = dateType;
		if(dateType.isToday()){
			orderTbl = "order";
			orderFoodTbl = "order_food";
			tgTbl = "taste_group";
			ntgTbl = "normal_taste_group";
			shiftTbl = "shift";
			paymentTbl = "payment";
			moTbl = "member_operation";
			mixedTbl = "mixed_payment";
			dailyTbl = null;
		}else if(dateType.isHistory()){
			orderTbl = "order_history";
			orderFoodTbl = "order_food_history";
			tgTbl = "taste_group_history";
			ntgTbl = "normal_taste_group_history";
			shiftTbl = "shift_history";
			paymentTbl = "payment_history";
			moTbl = "member_operation_history";
			mixedTbl = "mixed_payment_history";
			dailyTbl = "daily_settle_history";
		}else if(dateType.isArchive()){
			orderTbl = "order_archive";
			orderFoodTbl = "order_food_archive";
			tgTbl = "taste_group_archive";
			ntgTbl = "normal_taste_group_archive";
			shiftTbl = "shift_archive";
			paymentTbl = "payment_archive";
			moTbl = "member_operation_archive";
			mixedTbl = "mixed_payment_archive";
			dailyTbl = "daily_settle_archive";
		}else{
			throw new IllegalArgumentException("The date type(val = " + dateType.getValue() + ") is invalid.");
		}
	}
}
