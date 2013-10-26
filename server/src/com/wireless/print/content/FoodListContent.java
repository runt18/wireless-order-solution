package com.wireless.print.content;

import java.util.List;

import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;

public class FoodListContent extends ConcreteContent {

	private String _format;
	private List<OrderFood> _foods;
	
	public FoodListContent(String format, List<OrderFood> foods, PStyle style) {
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
		StringBuilder var = new StringBuilder();
		int cnt = 0;
		for(OrderFood of : _foods){
			if(of.asFood().isCombo()){
				var.append(new ComboDetail4ListContent(_format, of, mStyle));
			}else{
				var.append(new FoodDetailContent(_format, of, mStyle).toString() + (cnt++ < _foods.size() - 1 ? "\r\n" : ""));
			}
		}
		return var.toString();
	}
	
}
