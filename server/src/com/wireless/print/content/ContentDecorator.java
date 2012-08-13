package com.wireless.print.content;

public abstract class ContentDecorator extends Content{
	
	protected Content _content;
	
	protected ContentDecorator(final String value, int style){
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
