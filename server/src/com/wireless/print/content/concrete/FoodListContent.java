package com.wireless.print.content.concrete;

import java.util.List;

import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.print.content.concrete.FoodDetailContent.DisplayConfig;

public class FoodListContent extends ConcreteContent {

	private final DisplayConfig mDisplayConfig;
	private final List<OrderFood> mOrderFoods;
	private final FoodDetailContent.DetailType mDetailType;
	
	public FoodListContent(DisplayConfig config, List<OrderFood> orderFoods, PType printType, PStyle style, FoodDetailContent.DetailType detailType) {
		super(printType, style);
		mDisplayConfig = config;
		mOrderFoods = orderFoods;
		mDetailType = detailType;
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
				var.append(new ComboDetail4ListContent(mDisplayConfig, of, mPrintType, mStyle, mDetailType));
			}else{
				var.append(new FoodDetailContent(mDisplayConfig, of, mPrintType, mStyle, mDetailType).toString());
			}
			var.append(cnt++ < mOrderFoods.size() - 1 ? SEP : "");
		}
		return var.toString();
	}
	
}
