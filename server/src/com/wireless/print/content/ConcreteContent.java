package com.wireless.print.content;

import java.io.UnsupportedEncodingException;

import com.wireless.print.PStyle;
import com.wireless.print.PType;
import com.wireless.protocol.Order;


public abstract class ConcreteContent implements Content {
	
	final static int LEN_58MM = 32;
	final static int LEN_80MM = 48;
	
	final int _len;
	final PStyle _style;
	
	final protected PType _printType;
	final Order _order;
	final String _waiter;
	
	protected ConcreteContent(PType printType, PStyle style){
		
		_style = style;
		if(style == PStyle.PRINT_STYLE_58MM){
			_len = LEN_58MM;
		}else if(style == PStyle.PRINT_STYLE_80MM){
			_len = LEN_80MM;
		}else{
			_len = LEN_58MM;
		}
		
		_printType = printType;
		_waiter = null;
		_order = null;
	}
	
	protected ConcreteContent(Order order, String waiter, PType printType, PStyle style){
		
		_style = style;
		if(style == PStyle.PRINT_STYLE_58MM){
			_len = LEN_58MM;
		}else if(style == PStyle.PRINT_STYLE_80MM){
			_len = LEN_80MM;
		}else{
			_len = LEN_58MM;
		}
		
		_order = new Order();
		_order.copyFrom(order);
		_waiter = waiter;
		_printType = printType;
	}
	
	public byte[] toBytes(){
		try{
			return toString().getBytes("GBK");
		}catch(UnsupportedEncodingException e){
			return new byte[0];
		}
	}
	
	public PStyle getStyle(){
		return _style;
	}
}
