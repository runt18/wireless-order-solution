package com.wireless.print.content;

import com.wireless.print.PStyle;
import com.wireless.print.PType;
import com.wireless.protocol.OrderFood;

public class FoodListWithSepContent extends ConcreteContent {
	
	private String mFormat;
	private OrderFood[] mFoods;
	private PType mPrintType;
	
	public FoodListWithSepContent(String format, PType printType, OrderFood[] foods, PStyle style) {
		super(PType.PRINT_UNKNOWN, style);
		mPrintType = printType;
		mFormat = format;
		mFoods = foods;
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
		//generate the separator
		StringBuffer sep = new StringBuffer();
		if(mStyle == PStyle.PRINT_STYLE_58MM){
			for(int i = 0; i < LEN_58MM; i++){
				sep.append('-');
			}
			
		}else if(mStyle == PStyle.PRINT_STYLE_80MM){
			for(int i = 0; i < LEN_80MM; i++){
				sep.append('-');
			}
		}
		sep.insert(0, "\r\n").insert(sep.length(), "\r\n");
		
		StringBuffer var = new StringBuffer();
		for(int i = 0; i < mFoods.length; i++){
			if(mFoods[i].asFood().isCombo()){
				var.append(new ComboDetail4ListContent(mFormat, mFoods[i], mStyle).toString());
			}else{
				var.append(new FoodDetailContent(mFormat, mFoods[i], mStyle).toString());
				if(mPrintType == PType.PRINT_ALL_CANCELLED_FOOD && mFoods[i].hasCancelReason()){
					var.append("\r\n").append("Ô­Òò:" + mFoods[i].getCancelReason().getReason());
				}
			}
			var.append((i < mFoods.length - 1 ? sep : ""));
		}
		return var.toString();
	}
}
