package com.wireless.print.content;

import java.util.List;

import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.print.content.FoodDetailContent.DisplayConfig;

public class FoodListContent extends ConcreteContent {

	private final DisplayConfig mDisplayConfig;
	private final List<OrderFood> mOrderFoods;
	
	public FoodListContent(DisplayConfig format, List<OrderFood> orderFoods, PType printType, PStyle style) {
		super(printType, style);
		mDisplayConfig = format;
		mOrderFoods = orderFoods;
	}

	/**
	 * Generate the order food list to print.
	 * The style to this food list is like below.<br>
	 * --------------------------------<br>
	 * Food1-Taste(1)		     $32.00<br>
	 * Food2-Taste(1)	         $23.50<br>
	 * Food3-Taste(1)	 	     $45.45<br>
	 *  |-ChildFood1(1)  			   <br>
	 *  |-ChildFood2(2)				   <br>
	 * --------------------------------<br>
	 */
	@Override
	public String toString(){
		StringBuilder var = new StringBuilder();
		int cnt = 0;
		for(OrderFood of : mOrderFoods){
			if(of.asFood().isCombo()){
				var.append(new ComboDetail4ListContent(mDisplayConfig, of, mPrintType, mStyle));
			}else{
				var.append(new FoodDetailContent(mDisplayConfig, of, mPrintType, mStyle).toString() + (cnt++ < mOrderFoods.size() - 1 ? SEP : ""));
			}
		}
		return var.toString();
	}
	
}
