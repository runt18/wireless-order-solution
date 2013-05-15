package com.wireless.print.content;

import com.wireless.pojo.util.NumericUtil;
import com.wireless.print.PStyle;
import com.wireless.print.PType;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;

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
		if(_food.asFood().isCombo()){
			StringBuffer var = new StringBuffer();
			var.append(new FoodDetailContent(_format, _food, mStyle).toString());
			for(Food subFood : _food.asFood().getChildFoods()){
				var.append("\r\n").append(" |-").append(subFood.getName() + "(" + NumericUtil.float2String2(subFood.getAmount() * _food.getCount()) + ")");
			}
			return var.toString();
		}else{
			return new FoodDetailContent(_format, _food, mStyle).toString();
		}
	}

}
