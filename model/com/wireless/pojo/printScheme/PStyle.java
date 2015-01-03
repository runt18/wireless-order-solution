package com.wireless.pojo.printScheme;

public enum PStyle{
	
	PRINT_STYLE_UNKNOWN(0, "未知类型"),
	PRINT_STYLE_58MM(1, "58mm"),
	PRINT_STYLE_80MM(2, "80mm"),
	PRINT_STYLE_76MM(3, "76mm"),
	PRINT_STYLE_50MM_40MM(4, "50mm * 40mm");
	
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
	
	public static PStyle valueOf(int val){
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