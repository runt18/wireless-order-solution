package com.wireless.print.content;

import com.wireless.print.PStyle;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Reserved;

public class FoodListWithSepContent extends ConcreteContent {
	
	private String _format;
	private OrderFood[] _foods;
	
	public FoodListWithSepContent(String format, OrderFood[] foods, int style) {
		super(Reserved.PRINT_UNKNOWN, style);
		_format = format;
		_foods = foods;
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
		if(_style == PStyle.PRINT_STYLE_58MM){
			for(int i = 0; i < PStyle.LEN_58MM; i++){
				sep.append('-');
			}
			
		}else if(_style == PStyle.PRINT_STYLE_80MM){
			for(int i = 0; i < PStyle.LEN_80MM; i++){
				sep.append('-');
			}
		}
		sep.insert(0, "\r\n").insert(sep.length(), "\r\n");
		
		StringBuffer var = new StringBuffer();
		for(int i = 0; i < _foods.length; i++){
			if(_foods[i].isCombo()){
				var.append(new ComboDetail4ListContent(_format, _foods[i], _style).toString());
			}else{
				var.append(new FoodDetailContent(_format, _foods[i], _style).toString());
			}
			var.append((i < _foods.length - 1 ? sep : ""));
		}
		return var.toString();
	}
}
