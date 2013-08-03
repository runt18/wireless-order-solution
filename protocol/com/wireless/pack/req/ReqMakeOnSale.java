package com.wireless.pack.req;

import java.util.List;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.staffMgr.Staff;

public class ReqMakeOnSale extends RequestPackage {

	public ReqMakeOnSale(Staff staff, List<Food> toOnSale) {
		super(staff);
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.MAKE_FOOD_ON_SALE;
		fillBody(toOnSale, Food.FOOD_PARCELABLE_SIMPLE);
	}

}
