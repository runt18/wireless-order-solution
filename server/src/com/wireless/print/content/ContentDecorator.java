package com.wireless.print.content;

import com.wireless.print.PStyle;

public abstract class ContentDecorator extends Content{
	
	protected Content _content;
	
	protected ContentDecorator(final String value, PStyle style){
		super(style);
		_content = new Content(style){
			@Override
			public String toString(){
				return value;
			}
		};
	}
	
	protected ContentDecorator(Content content){
		super(content._style);
		_content = content;		
	}	
	
}
