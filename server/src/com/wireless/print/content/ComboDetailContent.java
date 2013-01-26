package com.wireless.print.content;

import com.wireless.protocol.Food;
import com.wireless.protocol.NumericUtil;
import com.wireless.protocol.OrderFood;
import com.wireless.protocol.Reserved;

public class ComboDetailContent extends ConcreteContent {

	private String _format;
	private OrderFood _parent;
	private Food _child;
	
	public ComboDetailContent(String format, OrderFood parent, Food child, int style){
		super(Reserved.PRINT_UNKNOWN, style);
		_format = format;
		_parent = parent;
		_child = child;
	}

	/**
	 * Generate combo string for detail to print.
	 * The style to this food list is like below.<br>
	 * --------------------------------------------<br>
	 * (Ьз)(На)Food-Taste(1)(8.5ел)(Ьи,Мі)    $32.00<br>
	 *  |-SubFood(1)                               <br>
	 * --------------------------------------------<br>
	 */
	@Override
	public String toString(){
		StringBuffer var = new StringBuffer();
		var.append(new FoodDetailContent(_format, _parent, _style).toString());
		var.append("\r\n").append(" |-").append(_child.getName() + "(" + NumericUtil.float2String2(_child.amount * _parent.getCount()) + ")");
		return var.toString();
	}
	
}
