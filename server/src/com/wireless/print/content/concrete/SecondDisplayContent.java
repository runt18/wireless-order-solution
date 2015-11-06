package com.wireless.print.content.concrete;

import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;
import com.wireless.pojo.util.NumericUtil;

public class SecondDisplayContent extends ConcreteContent {
	
	//�����ۡ��ַ�������������ȫ��
	//private final static String UNIT_ON = new String(new char[]{ 0x1B, 0x73, 0x31 });
	//���ܼơ��ַ�������������ȫ��
	private final static String TOTAL_ON = new String(new char[]{ 0x1B, 0x73, 0x32 }); 
	//���տ�ַ�������������ȫ��
	//private final static String RECEIVE_ON = new String(new char[]{ 0x1B, 0x73, 0x33 });
	//�����㡱�ַ�������������ȫ��
	//private final static String CASH_ON = new String(new char[]{ 0x1B, 0x73, 0x34 }); 
	
	private final float display;
	
	public SecondDisplayContent(float display) {
		super(PType.PRINT_2ND_DISPLAY, PStyle.PRINT_STYLE_UNKNOWN);
		this.display = display;
	}
	
	@Override
	public String toString(){
		return new String(new char[]{ 0x1B, 0x51, 0x41 }) + NumericUtil.float2String2(display) + new String(new char[]{ 0x0D }) +
			   TOTAL_ON;
	}
}
