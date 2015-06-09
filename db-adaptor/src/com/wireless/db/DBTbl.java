package com.wireless.db;

import com.wireless.pojo.util.DateType;

public class DBTbl {
	
	public final String orderTbl;
	public final String orderFoodTbl;
	public final String tgTbl;
	public final String ntgTbl;
	public final String shiftTbl;
	public final String paymentTbl;
	public final String moTbl;
	public final String mixedTbl;
	
	public DBTbl(DateType dateType){
		if(dateType.isToday()){
			orderTbl = "order";
			orderFoodTbl = "order_food";
			tgTbl = "taste_group";
			ntgTbl = "normal_taste_group";
			shiftTbl = "shift";
			paymentTbl = "payment";
			moTbl = "member_operation";
			mixedTbl = "mixed_payment";
		}else if(dateType.isHistory()){
			orderTbl = "order_history";
			orderFoodTbl = "order_food_history";
			tgTbl = "taste_group_history";
			ntgTbl = "normal_taste_group_history";
			shiftTbl = "shift_history";
			paymentTbl = "payment_history";
			moTbl = "member_operation_history";
			mixedTbl = "mixed_payment_history";
		}else{
			orderTbl = "order";
			orderFoodTbl = "order_food";
			tgTbl = "taste_group";
			ntgTbl = "normal_taste_group";
			shiftTbl = "shift";
			paymentTbl = "payment";
			moTbl = "member_operation";
			mixedTbl = "mixed_payment";
		}
	}
}
