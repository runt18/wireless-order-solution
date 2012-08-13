package com.wireless.print.content;

import com.wireless.protocol.Order;
import com.wireless.protocol.Terminal;


public abstract class ConcreteContent extends Content {
	
	protected int _printType;
	protected Order _order;
	protected Terminal _term;
	
	protected ConcreteContent(int printType, int style){
		super(style);
		_printType = printType;
	}
	
	protected ConcreteContent(Order order, Terminal term, int printType, int style){
		super(style);
		_order = order;
		_term = term;
		_printType = printType;
	}
}
