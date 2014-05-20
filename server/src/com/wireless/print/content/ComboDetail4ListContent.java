package com.wireless.print.content;

import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.util.NumericUtil;
import com.wireless.print.content.FoodDetailContent.DisplayConfig;

public class ComboDetail4ListContent extends ConcreteContent {

	private final DisplayConfig mDisplayConfig;
	private final OrderFood mOrderFood;
	
	public ComboDetail4ListContent(DisplayConfig format, OrderFood food, PType printType, PStyle style) {
		super(printType, style);
		mDisplayConfig = format;
		mOrderFood = food;
	}
	
	@Override
	public String toString(){
		if(mOrderFood.asFood().isCombo()){
			StringBuilder var = new StringBuilder();
			var.append(new FoodDetailContent(mDisplayConfig, mOrderFood, mPrintType, mStyle).toString());
			for(Food subFood : mOrderFood.asFood().getChildFoods()){
				var.append(SEP).append(" |-").append(subFood.getName() + "(" + NumericUtil.float2String2(subFood.getAmount() * mOrderFood.getCount()) + ")");
			}
			return var.toString();
		}else{
			return new FoodDetailContent(mDisplayConfig, mOrderFood, mPrintType, mStyle).toString();
		}
	}

}
