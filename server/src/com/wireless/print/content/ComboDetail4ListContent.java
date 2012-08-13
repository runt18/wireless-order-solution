package com.wireless.print.content;

import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Reserved;
import com.wireless.protocol.Util;

public class ComboDetail4ListContent extends ConcreteContent {

	private String _format;
	private OrderFood _food;
	
	public ComboDetail4ListContent(String format, OrderFood food, int style) {
		super(Reserved.PRINT_UNKNOWN, style);
		_format = format;
		_food = food;
	}
	
	@Override
	public String toString(){
		if(_food.isCombo()){
			StringBuffer var = new StringBuffer();
			var.append(new FoodDetailContent(_format, _food, _style).toString());
			for(Food subFood : _food.childFoods){
				var.append("\r\n").append(" |-").append(subFood.name + "(" + Util.float2String2(subFood.amount * _food.getCount()) + ")");
			}
			return var.toString();
		}else{
			return new FoodDetailContent(_format, _food, _style).toString();
		}
	}

}
