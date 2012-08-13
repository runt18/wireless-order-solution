package com.wireless.print.content;

import com.wireless.print.PStyle;

public class Content {
	
	int _len;
	int _style;
	
	protected Content(int style){
		_style = style;
		if(style == PStyle.PRINT_STYLE_58MM){
			_len = PStyle.LEN_58MM;
		}else if(style == PStyle.PRINT_STYLE_80MM){
			_len = PStyle.LEN_80MM;
		}else{
			_len = PStyle.LEN_58MM;
		}
	}
}
