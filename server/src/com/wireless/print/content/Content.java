package com.wireless.print.content;

import java.io.UnsupportedEncodingException;

import com.wireless.print.PStyle;

public class Content {
	
	final static int LEN_58MM = 32;
	final static int LEN_80MM = 48;
	
	final int _len;
	final PStyle _style;
	
	protected Content(PStyle style){
		_style = style;
		if(style == PStyle.PRINT_STYLE_58MM){
			_len = LEN_58MM;
		}else if(style == PStyle.PRINT_STYLE_80MM){
			_len = LEN_80MM;
		}else{
			_len = LEN_58MM;
		}
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
