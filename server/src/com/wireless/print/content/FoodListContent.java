package com.wireless.print.content;

import com.wireless.print.PStyle;
import com.wireless.print.PType;
import com.wireless.protocol.OrderFood;

public class FoodListContent extends ConcreteContent {

	private String _format;
	private OrderFood[] _foods;
	
	public FoodListContent(String format, OrderFood[] foods, PStyle style) {
		super(PType.PRINT_UNKNOWN, style);
		_format = format;
		_foods = foods;
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
		StringBuffer var = new StringBuffer();
		for(int i = 0; i < _foods.length; i++){
			if(_foods[i].asFood().isCombo()){
				var.append(new ComboDetail4ListContent(_format, _foods[i], mStyle));
			}else{
				var.append(new FoodDetailContent(_format, _foods[i], mStyle).toString() + (i < _foods.length - 1 ? "\r\n" : ""));
			}
		}
		return var.toString();
	}
	
}
