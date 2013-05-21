package com.wireless.print.content;

import java.io.UnsupportedEncodingException;

import com.wireless.pojo.dishesOrder.Order;
import com.wireless.print.PStyle;
import com.wireless.print.PType;


public abstract class ConcreteContent implements Content {
	
	final static int LEN_58MM = 32;
	final static int LEN_80MM = 48;
	
	final static String LINE_58MM = "--------------------------------";
	final static String LINE_80MM = "-----------------------------------------------";
	final static String SEP = "\r\n";
	final static String CUT = new String(new char[]{ 0x1b, 0x6d });
	
	final String mSeperatorLine;
	
	final int mLen;
	final PStyle mStyle;
	final PType mPrintType;
	
	final Order _order;
	final String _waiter;
	
	protected ConcreteContent(PStyle style){
		mStyle = style;
		if(style == PStyle.PRINT_STYLE_58MM){
			mLen = LEN_58MM;
			mSeperatorLine = LINE_58MM;
		}else if(style == PStyle.PRINT_STYLE_80MM){
			mLen = LEN_80MM;
			mSeperatorLine = LINE_80MM;
		}else{
			mLen = LEN_58MM;
			mSeperatorLine = LINE_58MM;
		}
		
		mPrintType = PType.PRINT_UNKNOWN;
		_waiter = null;
		_order = null;
	}
	
	protected ConcreteContent(PType printType, PStyle style){
		
		mStyle = style;
		if(style == PStyle.PRINT_STYLE_58MM){
			mLen = LEN_58MM;
			mSeperatorLine = LINE_58MM;
		}else if(style == PStyle.PRINT_STYLE_80MM){
			mLen = LEN_80MM;
			mSeperatorLine = LINE_80MM;
		}else{
			mLen = LEN_58MM;
			mSeperatorLine = LINE_58MM;
		}
		
		mPrintType = printType;
		_waiter = null;
		_order = null;
	}
	
	protected ConcreteContent(Order order, String waiter, PType printType, PStyle style){
		
		mStyle = style;
		if(style == PStyle.PRINT_STYLE_58MM){
			mLen = LEN_58MM;
			mSeperatorLine = LINE_58MM;
		}else if(style == PStyle.PRINT_STYLE_80MM){
			mLen = LEN_80MM;
			mSeperatorLine = LINE_80MM;
		}else{
			mLen = LEN_58MM;
			mSeperatorLine = LINE_58MM;
		}
		
		_order = new Order();
		_order.copyFrom(order);
		_waiter = waiter;
		mPrintType = printType;
	}
	
	public byte[] toBytes(){
		try{
			return toString().getBytes("GBK");
		}catch(UnsupportedEncodingException e){
			return new byte[0];
		}
	}
	
	public PType getPrintType(){
		return this.mPrintType;
	}
	
	public PStyle getStyle(){
		return mStyle;
	}
}
