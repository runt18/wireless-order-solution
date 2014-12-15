package com.wireless.print.content.concrete;

import com.wireless.pojo.dishesOrder.ComboOrderFood;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.ComboFood;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.print.content.concrete.FoodDetailContent.DisplayConfig;

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
			for(ComboFood subFood : mOrderFood.asFood().getChildFoods()){
				String foodName = subFood.getName();
				for(ComboOrderFood cof : mOrderFood.getCombo()){
					if(cof.asComboFood().equals(subFood)){
						foodName = cof.toString();
						break;
					}
				}
				var.append(SEP).append(" |-").append(foodName);
			}
			return var.toString();
		}else{
			return new FoodDetailContent(mDisplayConfig, mOrderFood, mPrintType, mStyle).toString();
		}
	}

}
