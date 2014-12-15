package com.wireless.print.content.decorator;

import java.io.UnsupportedEncodingException;

import com.wireless.pojo.printScheme.PStyle;
import com.wireless.print.content.concrete.ConcreteContent;
import com.wireless.print.content.decorator.ExtraFormatDecorator.Format;

public class CenterAlignedDecorator extends ConcreteContentDecorator {

	private final static Format FORMAT_CENTER_ALIGNED_WIRE = new Format(new char[]{0x1B, 0x61, 0x01}, new char[]{0x0D, 0x1B, 0x61, 0x00});
	
	public CenterAlignedDecorator(String value, PStyle style){
		super(value, style);
	}
	
	public CenterAlignedDecorator(ConcreteContent content) {
		super(content);
	}

	@Override
	public String toString(){
		if(mStyle == PStyle.PRINT_STYLE_76MM){
			return new ExtraFormatDecorator(_content, FORMAT_CENTER_ALIGNED_WIRE).toString();
			
		}else{
			String var = "$(space_left)$(value)";
			try{
				/**
				 * Calculate the amount of spaces to left,
				 * and replace the $(space_left) with it.
				 */
				int leftSpaceAmt = (mLen - _content.toString().getBytes("GBK").length) / 2;
				StringBuilder space = new StringBuilder();
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
	
}
