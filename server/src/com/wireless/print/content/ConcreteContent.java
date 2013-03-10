package com.wireless.print.content;

import com.wireless.print.PStyle;
import com.wireless.print.PType;
import com.wireless.protocol.Order;


public abstract class ConcreteContent extends Content {
	
	final protected PType _printType;
	final Order _order;
	final String _waiter;
	
	protected ConcreteContent(PType printType, PStyle style){
		super(style);
		_printType = printType;
		_waiter = null;
		_order = null;
	}
	
	protected ConcreteContent(Order order, String waiter, PType printType, PStyle style){
		super(style);
		_order = new Order();
		_order.copyFrom(order);
		_waiter = waiter;
		_printType = printType;
	}
}
