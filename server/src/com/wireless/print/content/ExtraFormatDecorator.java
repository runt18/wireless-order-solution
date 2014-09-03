package com.wireless.print.content;

import com.wireless.pojo.printScheme.PStyle;

public class ExtraFormatDecorator extends ConcreteContentDecorator {

	private static class FormatDescriptor{
		private final char[] header;
		private final char[] tail;
		
		FormatDescriptor(char[] header, char[] tail){
			this.header = header;
			this.tail = tail;
		}
	}
	
	final static char[] FORMAT_NORMAL_FONT = { 0x1D, 0x21, 0x00 };
	final static char[] FORMAT_LARGE_FONT_V_1X = { 0x1D, 0x21, 0x01 };
	final static char[] FORMAT_LARGE_FONT_V_2X = { 0x1D, 0x21, 0x02 };
	final static char[] FORMAT_LARGE_FONT_V_3X = { 0x1D, 0x21, 0x03 };
	final static char[] FORMAT_LARGE_FONT_H_1X = { 0x1D, 0x21, 0x10 };
	final static char[] FORMAT_LARGE_FONT_H_2X = { 0x1D, 0x21, 0x20 };
	final static char[] FORMAT_LARGE_FONT_H_3X = { 0x1D, 0x21, 0x30 };
	final static char[] FORMAT_LARGE_FONT_VH_1X = { 0x1D, 0x21, 0x11 };
	final static char[] FORMAT_LARGE_FONT_VH_2X = { 0x1D, 0x21, 0x21 };
	final static char[] FORMAT_LARGE_FONT_VH_3X = { 0x1D, 0x21, 0x31 };

	public final static FormatDescriptor NORMAL_FONT = new FormatDescriptor(FORMAT_NORMAL_FONT, FORMAT_NORMAL_FONT);
	public final static FormatDescriptor LARGE_FONT_V_1X = new FormatDescriptor(FORMAT_LARGE_FONT_V_1X, FORMAT_NORMAL_FONT); 
	public final static FormatDescriptor LARGE_FONT_V_2X = new FormatDescriptor(FORMAT_LARGE_FONT_V_2X, FORMAT_NORMAL_FONT);
	public final static FormatDescriptor LARGE_FONT_V_3X = new FormatDescriptor(FORMAT_LARGE_FONT_V_3X, FORMAT_NORMAL_FONT);
	public final static FormatDescriptor LARGE_FONT_H_1X = new FormatDescriptor(FORMAT_LARGE_FONT_H_1X, FORMAT_NORMAL_FONT); 
	public final static FormatDescriptor LARGE_FONT_H_2X = new FormatDescriptor(FORMAT_LARGE_FONT_H_2X, FORMAT_NORMAL_FONT);
	public final static FormatDescriptor LARGE_FONT_H_3X = new FormatDescriptor(FORMAT_LARGE_FONT_H_3X, FORMAT_NORMAL_FONT);
	public final static FormatDescriptor LARGE_FONT_VH_1X = new FormatDescriptor(FORMAT_LARGE_FONT_VH_1X, FORMAT_NORMAL_FONT); 
	public final static FormatDescriptor LARGE_FONT_VH_2X = new FormatDescriptor(FORMAT_LARGE_FONT_VH_2X, FORMAT_NORMAL_FONT);
	public final static FormatDescriptor LARGE_FONT_VH_3X = new FormatDescriptor(FORMAT_LARGE_FONT_VH_3X, FORMAT_NORMAL_FONT);
	
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
