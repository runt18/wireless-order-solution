package com.wireless.print.content;

import com.wireless.print.PStyle;
import com.wireless.print.PType;
import com.wireless.protocol.Food;
import com.wireless.protocol.OrderFood;
import com.wireless.util.NumericUtil;

public class ComboDetailContent extends ConcreteContent {

	private String _format;
	private OrderFood _parent;
	private Food _child;
	
	public ComboDetailContent(String format, OrderFood parent, Food child, PStyle style){
		super(PType.PRINT_UNKNOWN, style);
		_format = format;
		_parent = parent;
		_child = child;
	}

	/**
	 * Generate combo string for detail to print.
	 * The style to this food list is like below.<br>
	 * --------------------------------------------<br>
	 * (��)(��)Food-Taste(1)(8.5��)(��,��)    $32.00<br>
	 *  |-SubFood(1)                               <br>
	 * --------------------------------------------<br>
	 */
	@Override
	public String toString(){
		StringBuffer var = new StringBuffer();
		var.append(new FoodDetailContent(_format, _parent, mStyle).toString());
		var.append("\r\n").append(" |-").append(_child.getName() + "(" + NumericUtil.float2String2(_child.getAmount() * _parent.getCount()) + ")");
		return var.toString();
	}
	
}
