package com.wireless.print.content.concrete;

import com.wireless.pojo.printScheme.PStyle;
import com.wireless.pojo.printScheme.PType;

public class QRCodeContent extends ConcreteContent{

	public static enum CorrectionLevel{
		L((char)0x30, "L"),
		M((char)0x31, "M"),
		Q((char)0x32, "Q"),
		H((char)0x33, "H");
		
		private final char val;
		private final String desc;
		
		private CorrectionLevel(char val, String desc) {
			this.val = val;
			this.desc = desc;
		}
		
		@Override
		public String toString(){
			return this.desc;
		}
	}
	
	private final char size;
	private final char[] content;
	private final CorrectionLevel level;
	
	public QRCodeContent(PType printType, PStyle style, String content) {
		this(printType, style, 0x06, CorrectionLevel.Q, content);
	}

	public QRCodeContent(PType printType, PStyle style, int size, CorrectionLevel level, String content) {
		super(printType, style);
		this.level = level;
		this.size = (char)size;
		this.content = content.toCharArray();
	}
	
	@Override
	public String toString(){
		StringBuilder bytesToQRCode = new StringBuilder();
		//Set the Module Function 167  n between 1 and 16 (decimal) in hex
		bytesToQRCode.append(new char[]{ 0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43, size });
		//Set correction error level Function 169 n 0x30 -> 7% L 0x31 -> 15% M 0x32 -> 25% Q 0x33 -> 30% H
		bytesToQRCode.append(new char[]{ 0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x45, level.val });
		//Store Data Function 180 -> n storage chars (see below) + 3
		final char pL, pH;
		final int length = content.length + 3;
		if(length < 255){
			pL = (char)length;
			pH = 0x00;
		}else if(length < 7092){
			pL = 0xFF;
			pH = (char)((length - pL) / 256);
		}else{
			throw new IllegalArgumentException("The length to content exceeds 7092 bytes");
		}
		bytesToQRCode.append(new char[]{ 0x1D, 0x28, 0x6B, pL, pH, 0x31, 0x50, 0x30 });
		//The content
		bytesToQRCode.append(content);
		//PRINT Function 181
		bytesToQRCode.append(new char[]{ 0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30 });
		
		return bytesToQRCode.toString();
	}
}
