package com.wireless.print.content;

import com.wireless.pojo.dishesOrder.OrderFood;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.util.NumericUtil;

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
			StringBuilder var = new StringBuilder();
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
