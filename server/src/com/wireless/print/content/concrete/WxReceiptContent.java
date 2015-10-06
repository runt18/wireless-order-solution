package com.wireless.print.content.concrete;

import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;

public class WxReceiptContent extends ConcreteContent{

	private final String codeUrl;
	
	public WxReceiptContent(String codeUrl, PStyle style) {
		super(PType.PRINT_WX_RECEIT, style);
		this.codeUrl = codeUrl;
	}

	@Override
	public String toString(){
		return new String(new char[]{0x1B, 0x61, 0x01}) +
			   new QRCodeContent(getPrintType(), getStyle(), codeUrl) + 
			   new String(new char[]{0x1B, 0x61, 0x00}) + 
			   SEP + SEP + SEP + SEP + SEP + CUT;
	}
}
