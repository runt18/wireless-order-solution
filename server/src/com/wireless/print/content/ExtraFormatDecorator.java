package com.wireless.print.content;

public class ExtraFormatDecorator extends ContentDecorator {

	private char[] _format;
	
	public ExtraFormatDecorator(String value, int style, char[] format){
		super(value, style);
		_format = format;
	}
	
	public ExtraFormatDecorator(Content content, char[] format) {
		super(content);
		_format = format;
	}

	@Override
	public String toString(){		
		if(_format != null){
			return new String(_format) + _content.toString();
		}else{
			return _content.toString();
		}
	}
	
}
