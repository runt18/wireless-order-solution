package com.wireless.print.content;

import com.wireless.pojo.dishesOrder.ComboOrderFood;
import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.ComboFood;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.print.content.FoodDetailContent.DisplayConfig;

public class ComboDetailContent extends ConcreteContent {

	private final DisplayConfig mDisplayConfig;
	private final OrderFood mParent;
	private final ComboFood mChild;
	
	public ComboDetailContent(DisplayConfig config, OrderFood parent, ComboFood child, PType printType, PStyle style){
		super(printType, style);
		mDisplayConfig = config;
		mParent = parent;
		mChild = child;
	}

	/**
	 * Generate combo string for detail to print.
	 * The style to this food list is like below.<br>
	 * --------------------------------------------<br>
	 * (Ьз)(На)Food-Taste(1)(8.5ел)(Ьи,Мі)    $32.00<br>
	 *  |-SubFood(1)                               <br>
	 * --------------------------------------------<br>
	 */
	@Override
	public String toString(){
		StringBuilder var = new StringBuilder();
		var.append(new FoodDetailContent(mDisplayConfig, mParent, mPrintType, mStyle).toString());
		String foodName = mChild.getName();
		for(ComboOrderFood cof : mParent.getCombo()){
			if(cof.asComboFood().equals(mChild)){
				foodName = cof.toString();
				break;
			}
		}
		var.append(SEP).append(" |-").append(foodName);
		return var.toString();
	}
	
}
