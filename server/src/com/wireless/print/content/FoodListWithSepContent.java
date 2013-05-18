package com.wireless.print.content;

import java.util.List;

import com.wireless.print.PStyle;
import com.wireless.print.PType;
import com.wireless.protocol.OrderFood;

public class FoodListWithSepContent extends ConcreteContent {
	
	private String mFormat;
	private List<OrderFood> mFoods;
	private PType mPrintType;
	
	public FoodListWithSepContent(String format, PType printType, List<OrderFood> foods, PStyle style) {
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
		int cnt = 0;
		for(OrderFood of : mFoods){
			if(of.asFood().isCombo()){
				var.append(new ComboDetail4ListContent(mFormat, of, mStyle).toString());
			}else{
				var.append(new FoodDetailContent(mFormat, of, mStyle).toString());
				if(mPrintType == PType.PRINT_ALL_CANCELLED_FOOD && of.hasCancelReason()){
					var.append("\r\n").append("Ô­Òò:" + of.getCancelReason().getReason());
				}
			}
			var.append((cnt++ < mFoods.size() - 1 ? sep : ""));
		}
		return var.toString();
	}
}
