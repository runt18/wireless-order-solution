package com.wireless.print.content.concrete;

import java.io.UnsupportedEncodingException;

import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.print.content.Content;


public abstract class ConcreteContent implements Content {
	
	private final static int LEN_58MM = 32;
	private final static int LEN_76MM = 40;
	private final static int LEN_80MM = 48;
	
	private final static String LINE_58MM = "--------------------------------";
	private final static String LINE_76MM = "---------------------------------------";
	private final static String LINE_80MM = "-----------------------------------------------";
	final static String SEP = "\r\n";
	final static String CUT = new String(new char[]{ 0x1b, 0x6d });
	
	final String mSeperatorLine;
	
	protected final int mLen;
	protected final PStyle mStyle;
	protected final PType mPrintType;
	
	protected ConcreteContent(PType printType, PStyle style){
		
		mStyle = style;
		if(style == PStyle.PRINT_STYLE_58MM){
			mLen = LEN_58MM;
			mSeperatorLine = LINE_58MM;
		}else if(style == PStyle.PRINT_STYLE_80MM){
			mLen = LEN_80MM;
			mSeperatorLine = LINE_80MM;
		}else if(style == PStyle.PRINT_STYLE_76MM){
			mLen = LEN_76MM;
			mSeperatorLine = LINE_76MM;
		}else{
			mLen = LEN_58MM;
			mSeperatorLine = LINE_58MM;
		}
		
		mPrintType = printType;
	}
	
	@Override
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
