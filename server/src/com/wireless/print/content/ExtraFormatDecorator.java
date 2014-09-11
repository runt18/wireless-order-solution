package com.wireless.print.content;

import com.wireless.pojo.printScheme.PStyle;

public class ExtraFormatDecorator extends ConcreteContentDecorator {

	static class FormatDescriptor{
		
		private final Format wire;		//��ʽ��ӡ��
		private final Format thermal;	//������ӡ��
		
		FormatDescriptor(Format wire, Format thermal){
			this.wire = wire;
			this.thermal = thermal;
		}
	}
	
	static class Format{
		private final char[] header;
		private final char[] tail;
		
		Format(char[] header, char[] tail){
			this.header = header;
			this.tail = tail;
		}
	}
	
	private final static char[] CODE_NORMAL_FONT_WIRE = { 0x1C, 0x57, 0x00, 0x1B, 0x21, 0x01 };
	private final static char[] CODE_LARGE_FONT_V_WIRE = { 0x1C, 0x57, 0x01, 0x1B, 0x21, 0x18 };
	
	private final static char[] CODE_NORMAL_FONT_THERMAL = { 0x1D, 0x21, 0x00 };
	private final static char[] CODE_LARGE_FONT_V_1X_THERMAL = { 0x1D, 0x21, 0x01 };
	private final static char[] CODE_LARGE_FONT_V_2X_THERMAL = { 0x1D, 0x21, 0x02 };
	private final static char[] CODE_LARGE_FONT_V_3X_THERMAL = { 0x1D, 0x21, 0x03 };
	private final static char[] CODE_LARGE_FONT_H_1X_THERMAL = { 0x1D, 0x21, 0x10 };
	private final static char[] CODE_LARGE_FONT_H_2X_THERMAL = { 0x1D, 0x21, 0x20 };
	private final static char[] CODE_LARGE_FONT_H_3X_THERMAL = { 0x1D, 0x21, 0x30 };
	private final static char[] CODE_LARGE_FONT_VH_1X_THERMAL = { 0x1D, 0x21, 0x11 };
	private final static char[] CODE_LARGE_FONT_VH_2X_THERMAL = { 0x1D, 0x21, 0x21 };
	private final static char[] CODE_LARGE_FONT_VH_3X_THERMAL = { 0x1D, 0x21, 0x31 };

	final static Format FORMAT_LARGE_FONT_WIRE = new Format(CODE_LARGE_FONT_V_WIRE, CODE_NORMAL_FONT_WIRE);
	final static Format FORMAT_NORMAL_FONT_WIRE = new Format(CODE_NORMAL_FONT_WIRE, new char[]{});
	final static Format FORMAT_NORMAL_FONT_THERMAL = new Format(CODE_NORMAL_FONT_THERMAL, new char[]{});
	final static Format FORMAT_LARGE_FONT_V_1X_THERMAL = new Format(CODE_LARGE_FONT_V_1X_THERMAL, CODE_NORMAL_FONT_THERMAL);
	final static Format FORMAT_LARGE_FONT_V_2X_THERMAL = new Format(CODE_LARGE_FONT_V_2X_THERMAL, CODE_NORMAL_FONT_THERMAL);
	final static Format FORMAT_LARGE_FONT_V_3X_THERMAL = new Format(CODE_LARGE_FONT_V_3X_THERMAL, CODE_NORMAL_FONT_THERMAL);
	final static Format FORMAT_LARGE_FONT_H_1X_THERMAL = new Format(CODE_LARGE_FONT_H_1X_THERMAL, CODE_NORMAL_FONT_THERMAL);
	final static Format FORMAT_LARGE_FONT_H_2X_THERMAL = new Format(CODE_LARGE_FONT_H_2X_THERMAL, CODE_NORMAL_FONT_THERMAL);
	final static Format FORMAT_LARGE_FONT_H_3X_THERMAL = new Format(CODE_LARGE_FONT_H_3X_THERMAL, CODE_NORMAL_FONT_THERMAL);
	final static Format FORMAT_LARGE_FONT_VH_1X_THERMAL = new Format(CODE_LARGE_FONT_VH_1X_THERMAL, CODE_NORMAL_FONT_THERMAL);
	final static Format FORMAT_LARGE_FONT_VH_2X_THERMAL = new Format(CODE_LARGE_FONT_VH_2X_THERMAL, CODE_NORMAL_FONT_THERMAL);
	final static Format FORMAT_LARGE_FONT_VH_3X_THERMAL = new Format(CODE_LARGE_FONT_VH_3X_THERMAL, CODE_NORMAL_FONT_THERMAL);
	
	public final static FormatDescriptor LARGE_FONT_V_1X = new FormatDescriptor(FORMAT_LARGE_FONT_WIRE, FORMAT_LARGE_FONT_V_1X_THERMAL); 
	public final static FormatDescriptor LARGE_FONT_V_2X = new FormatDescriptor(FORMAT_LARGE_FONT_WIRE, FORMAT_LARGE_FONT_V_2X_THERMAL);
	public final static FormatDescriptor LARGE_FONT_V_3X = new FormatDescriptor(FORMAT_LARGE_FONT_WIRE, FORMAT_LARGE_FONT_V_3X_THERMAL);
	public final static FormatDescriptor LARGE_FONT_H_1X = new FormatDescriptor(FORMAT_LARGE_FONT_WIRE, FORMAT_LARGE_FONT_H_1X_THERMAL); 
	public final static FormatDescriptor LARGE_FONT_H_2X = new FormatDescriptor(FORMAT_LARGE_FONT_WIRE, FORMAT_LARGE_FONT_H_2X_THERMAL);
	public final static FormatDescriptor LARGE_FONT_H_3X = new FormatDescriptor(FORMAT_LARGE_FONT_WIRE, FORMAT_LARGE_FONT_H_3X_THERMAL);
	public final static FormatDescriptor LARGE_FONT_VH_1X = new FormatDescriptor(FORMAT_LARGE_FONT_WIRE, FORMAT_LARGE_FONT_VH_1X_THERMAL); 
	public final static FormatDescriptor LARGE_FONT_VH_2X = new FormatDescriptor(FORMAT_LARGE_FONT_WIRE, FORMAT_LARGE_FONT_VH_2X_THERMAL);
	public final static FormatDescriptor LARGE_FONT_VH_3X = new FormatDescriptor(FORMAT_LARGE_FONT_WIRE, FORMAT_LARGE_FONT_VH_3X_THERMAL);
	
	private final String _header;
	private final String _tail;
	
	public ExtraFormatDecorator(String value, PStyle style, FormatDescriptor desc){
		super(value, style);
		if(style == PStyle.PRINT_STYLE_76MM){
			_header = new String(desc.wire.header);
			_tail = new String(desc.wire.tail);
		}else{
			_header = new String(desc.thermal.header);
			_tail = new String(desc.thermal.tail);
		}
	}
	
	public ExtraFormatDecorator(ConcreteContent content, FormatDescriptor desc){
		this(content.toString(), content.mStyle, desc);
	}
	
	public ExtraFormatDecorator(ConcreteContent content, Format format) {
		super(content);
		_header = new String(format.header);
		_tail = new String(format.tail);
	}

	public ExtraFormatDecorator(String value, PStyle style, Format format) {
		super(value, style);
		_header = new String(format.header);
		_tail = new String(format.tail);
	}
	
	@Override
	public String toString(){		
		return _header + _content.toString() + _tail;
	}
	
}
