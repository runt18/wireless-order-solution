package com.wireless.print.content.decorator;

import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.print.content.concrete.ConcreteContent;

public abstract class ConcreteContentDecorator extends ConcreteContent{
	
	protected ConcreteContent _content;
	
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
		super(content.getPrintType(), content.getStyle());
		_content = content;		
	}	
	
}
