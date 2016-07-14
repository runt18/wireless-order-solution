package com.wireless.print.content.concrete;

import java.util.List;

import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.print.content.concrete.FoodDetailContent.DisplayConfig;

public class FoodListWithSepContent extends ConcreteContent {
	
	private final DisplayConfig mDisplayConfig;
	private final List<OrderFood> mOrderFoods;
	private final FoodDetailContent.DetailType mDetailType;
	
	public FoodListWithSepContent(DisplayConfig config, List<OrderFood> orderFoods, PType printType, PStyle style, FoodDetailContent.DetailType detailType) {
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
	 * --------------------------------<br>
	 * Food2-Taste(1)	         $23.50<br>
	 * --------------------------------<br>
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
				var.append(new ComboDetail4ListContent(mDisplayConfig, of, mPrintType, mStyle, mDetailType).toString());
			}else{
				//菜品设置了数量不累加时, 菜品分开打印
				if(of.asFood().isSplit() && !of.asFood().isCombo()){
					final int amount;
					final float count;
					//delta不为0时表示是加菜或者退菜，为0时表示是补打
					if(of.getDelta() != 0){
						count = Math.abs(of.getDelta());
						amount = Float.valueOf(count).intValue();
					}else{
						count = Math.abs(of.getCount());
						amount = Float.valueOf(count).intValue();
					}
					//只有是小数时才分开打印
					if(count % amount == 0){
						OrderFood single = (OrderFood)of.clone();
						for(int i = 0; i < amount; i++){
							single.setCount(0);
							single.addCount(1);
							if(i > 0){
								var.append(SEP);
							}
							var.append(make(single));
						}
					}else{
						var.append(make(of));
					}
				}else{
					var.append(make(of));
				}
			}
			
			if(mStyle == PStyle.PRINT_STYLE_76MM){
				var.append((cnt++ < mOrderFoods.size() - 1 ? SEP + "---------------------------------" + SEP : ""));
			}else{
				var.append((cnt++ < mOrderFoods.size() - 1 ? SEP + mSeperatorLine + SEP : ""));
			}
		}
		return var.toString();
	}
	
	private String make(OrderFood of){
		final StringBuilder var = new StringBuilder();
		var.append(new FoodDetailContent(mDisplayConfig, of, mPrintType, mStyle, mDetailType).toString());
		if(mPrintType == PType.PRINT_ALL_CANCELLED_FOOD && of.hasCancelReason()){
			var.append(SEP).append("原因:" + of.getCancelReason().getReason());
		}
		return var.toString();
	}
}
