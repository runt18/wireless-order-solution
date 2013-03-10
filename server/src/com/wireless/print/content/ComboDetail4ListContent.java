package com.wireless.print.content;

import com.wireless.print.PStyle;
import com.wireless.print.PType;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
import com.wireless.util.NumericUtil;

public class ComboDetail4ListContent extends ConcreteContent {

	private String _format;
	private OrderFood _food;
	
	public ComboDetail4ListContent(String format, OrderFood food, PStyle style) {
		super(PType.PRINT_UNKNOWN, style);
		_format = format;
		_food = food;
	}
	
	@Override
	public String toString(){
		if(_food.isCombo()){
			StringBuffer var = new StringBuffer();
			var.append(new FoodDetailContent(_format, _food, _style).toString());
			for(Food subFood : _food.getChildFoods()){
				var.append("\r\n").append(" |-").append(subFood.getName() + "(" + NumericUtil.float2String2(subFood.getAmount() * _food.getCount()) + ")");
			}
			return var.toString();
		}else{
			return new FoodDetailContent(_format, _food, _style).toString();
		}
	}

}
