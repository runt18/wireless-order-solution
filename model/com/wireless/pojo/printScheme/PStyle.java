package com.wireless.pojo.printScheme;

public enum PStyle{
//	public final static int PRINT_STYLE_UNKNOWN = 0;
//	public final static int PRINT_STYLE_58MM = 1;
//	public final static int PRINT_STYLE_80MM = 2;
//	public final static int LEN_58MM = 32;
//	public final static int LEN_80MM = 48;
	
	PRINT_STYLE_UNKNOWN(0, "未知类型"),
	PRINT_STYLE_58MM(1, "58mm"),
	PRINT_STYLE_80MM(2, "80mm");
	
	private final int mVal;
	
	private final String mDesc;
	
	PStyle(int val, String desc){
		this.mVal = val;
		this.mDesc = desc;
	}
	
	public int getVal(){
		return mVal;
	}
	
	public String getDesc(){
		return mDesc;
	}
	
	public static PStyle get(int val){
		for(PStyle style : values()){
			if(style.mVal == val){
				return style;
			}
		}
		return PRINT_STYLE_UNKNOWN;
	}
	
	@Override
	public String toString(){
		return this.mDesc;
	}
	
}