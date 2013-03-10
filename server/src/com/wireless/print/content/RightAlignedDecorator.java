package com.wireless.print.content;

import java.io.UnsupportedEncodingException;

import com.wireless.print.PStyle;

public class RightAlignedDecorator extends ContentDecorator {
	
	public RightAlignedDecorator(String value, PStyle style){
		super(value, style);
	}
	
	public RightAlignedDecorator(Content content) {
		super(content);
	}

	@Override
	public String toString(){
		String var = "$(space_left)$(value)";
		try{
			/**
			 * Calculate the amount of spaces to left,
			 * and replace the $(space_left) with it.
			 */
			int leftSpaceAmt = _len - _content.toString().getBytes("GBK").length;
			StringBuffer space = new StringBuffer();
			for(int i = 0; i < leftSpaceAmt; i++){
				space.append(' ');
			}
			var = var.replace("$(space_left)", space);
			
			//replace the $(title)
			var = var.replace("$(value)", _content.toString());
			
			//var = new String(var.getBytes("GBK"), "GBK");
						
		}catch(UnsupportedEncodingException e){}
		
		return var;
	}
}
