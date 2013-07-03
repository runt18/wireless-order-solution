package com.wireless.pack.req;

import java.util.List;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pojo.menuMgr.Food;

public class ReqMakeOnSale extends RequestPackage {

	public ReqMakeOnSale(PinGen gen, List<Food> toOnSale) {
		super(gen);
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.MAKE_FOOD_ON_SALE;
		fillBody(toOnSale, Food.FOOD_PARCELABLE_SIMPLE);
	}

}
