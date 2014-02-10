package com.wireless.pojo.dishesOrder;

public enum PrintOption{
	DO_PRINT(0, "打印"),
	DO_NOT_PRINT(1, "不打印");
	
	private final int val;
	private final String desc;
	
	PrintOption(int val, String desc){
		this.val = val;
		this.desc = desc;
	}
	
	public static PrintOption valueOf(int val){
		for(PrintOption option : values()){
			if(option.val == val){
				return option;
			}
		}
		throw new IllegalArgumentException("The val(" + val + ") is invalid.");
	}
	
	public byte getVal(){
		return (byte)val;
	}
	
	public String getDesc(){
		return desc;
	}
	
	@Override
	public String toString(){
		return "PrintOption(val = " + val + ",desc = " + desc + ")";
	}
}