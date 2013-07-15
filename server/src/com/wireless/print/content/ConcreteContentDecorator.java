package com.wireless.print.content;

import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;

public abstract class ConcreteContentDecorator extends ConcreteContent{
	
	protected Content _content;
	
	protected ConcreteContentDecorator(final String value, PStyle style){
		super(PType.PRINT_UNKNOWN, style);
		_content = new ConcreteContent(PType.PRINT_UNKNOWN, style){
			@Override
			public String toString(){
				return value;
			}
		};
	}
	
	protected ConcreteContentDecorator(ConcreteContent content){
		super(content.mPrintType, content.mStyle);
		_content = content;		
	}	
	
}
