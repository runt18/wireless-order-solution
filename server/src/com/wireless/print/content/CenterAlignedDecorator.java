package com.wireless.print.content;

import java.io.UnsupportedEncodingException;

import com.wireless.pojo.printScheme.PStyle;

public class CenterAlignedDecorator extends ConcreteContentDecorator {

	public CenterAlignedDecorator(String value, PStyle style){
		super(value, style);
	}
	
	public CenterAlignedDecorator(ConcreteContent content) {
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
			int leftSpaceAmt = (mLen - _content.toString().getBytes("GBK").length) / 2;
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
