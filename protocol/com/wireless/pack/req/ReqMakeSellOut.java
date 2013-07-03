package com.wireless.pack.req;

import java.util.List;

import com.wireless.pack.Mode;
import com.wireless.pack.Type;
import com.wireless.pojo.menuMgr.Food;

public class ReqMakeSellOut extends RequestPackage {

	public ReqMakeSellOut(PinGen gen, List<Food> toSellOut) {
		super(gen);
		header.mode = Mode.ORDER_BUSSINESS;
		header.type = Type.MAKE_FOOD_SELL_OUT;
		fillBody(toSellOut, Food.FOOD_PARCELABLE_SIMPLE);
	}

}
