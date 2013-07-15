package com.wireless.print.content;

import com.wireless.pojo.printScheme.PStyle;

public class ExtraFormatDecorator extends ConcreteContentDecorator {

	private static class FormatDescriptor{
		char[] header;
		char[] tail;
		
		FormatDescriptor(char[] header, char[] tail){
			this.header = header;
			this.tail = tail;
		}
	}
	
	final static char[] FORMAT_NORMAL_FONT = { 0x1D, 0x21, 0x00 };
	final static char[] FORMAT_LARGE_FONT_1X = { 0x1D, 0x21, 0x01 };
	final static char[] FORMAT_LARGE_FONT_2X = { 0x1D, 0x21, 0x02 };
	final static char[] FORMAT_LARGE_FONT_3X = { 0x1D, 0x21, 0x03 };

	public final static FormatDescriptor NORMAL_FONT = new FormatDescriptor(FORMAT_NORMAL_FONT, FORMAT_NORMAL_FONT);
	public final static FormatDescriptor LARGE_FONT_1X = new FormatDescriptor(FORMAT_LARGE_FONT_1X, FORMAT_NORMAL_FONT); 
	public final static FormatDescriptor LARGE_FONT_2X = new FormatDescriptor(FORMAT_LARGE_FONT_2X, FORMAT_NORMAL_FONT);
	public final static FormatDescriptor LARGE_FONT_3X = new FormatDescriptor(FORMAT_LARGE_FONT_3X, FORMAT_NORMAL_FONT);
	
	private String _header;
	private String _tail;
	
	public ExtraFormatDecorator(String value, PStyle style, FormatDescriptor desc){
		this(value, style, desc.header, desc.tail);
	}
	
	public ExtraFormatDecorator(String value, PStyle style, char[] header, char[] tail){
		super(value, style);
		if(header != null){
			_header = new String(header);
		}else{
			_header = "";
		}
		if(tail != null){
			_tail = new String(tail);
		}else{
			_tail = "";
		}
	}
	
	public ExtraFormatDecorator(ConcreteContent content, FormatDescriptor desc){
		this(content, desc.header, desc.tail);
	}
	
	public ExtraFormatDecorator(ConcreteContent content, char[] header, char[] tail) {
		super(content);
		if(header != null){
			_header = new String(header);
		}else{
			_header = "";
		}
		if(tail != null){
			_tail = new String(tail);
		}else{
			_tail = "";
		}
	}

	@Override
	public String toString(){		
		return _header + _content.toString() + _tail;
	}
	
}
