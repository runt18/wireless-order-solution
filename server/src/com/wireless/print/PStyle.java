package com.wireless.print;

public enum PStyle{
//	public final static int PRINT_STYLE_UNKNOWN = 0;
//	public final static int PRINT_STYLE_58MM = 1;
//	public final static int PRINT_STYLE_80MM = 2;
//	public final static int LEN_58MM = 32;
//	public final static int LEN_80MM = 48;
	
	PRINT_STYLE_UNKNOWN(0),
	PRINT_STYLE_58MM(1),
	PRINT_STYLE_80MM(2);
	
	private final int mVal;
	
	PStyle(int val){
		this.mVal = val;
	}
	
	public int getVal(){
		return mVal;
	}
	
	public static PStyle get(int val){
		for(PStyle style : values()){
			if(style.mVal == val){
				return style;
			}
		}
		return PRINT_STYLE_UNKNOWN;
	}
	
}