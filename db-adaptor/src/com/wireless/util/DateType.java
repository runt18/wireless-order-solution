package com.wireless.util;

import java.util.Map;

public enum DateType {
	
	TODAY(0, "当日"), 
	HISTORY(1, "历史");
	
	private final int type;
	private final String desc;
	
	private DateType(int type, String name){
		this.type = type;
		this.desc = name;
	}
	
	public int getValue(){
		return type;
	}
	
	public String getDesc(){
		return desc;
	}
	
	/**
	 * @deprecated
	 * @param params
	 * @return
	 */
	public static DateType getValue(Map<Object, Object> params){
		if(params.get(DateType.TODAY) != null)
			return DateType.TODAY;
		if(params.get(DateType.HISTORY) != null)
			return DateType.HISTORY;
		else
			return null;
	}
	
	/**
	 * @deprecated
	 * @param type
	 * @return
	 */
	public static int getValue(String type){
		DateType dt = getType(type);
		if(dt == null){
			return -1;
		}else{
			return dt.getValue();
		}
	}
	
	/**
	 * @deprecated
	 * @param params
	 * @return
	 */
	public static boolean hasType(Map<Object, Object> params){
		if(params.get(DateType.TODAY) != null)
			return true;
		if(params.get(DateType.HISTORY) != null)
			return true;
		else
			return false;
	}
	
	/**
	 * @deprecated
	 * @param params
	 * @return
	 */
	public static DateType getType(Map<Object, Object> params){
		if(params.get(DateType.TODAY) != null)
			return DateType.TODAY;
		if(params.get(DateType.HISTORY) != null)
			return DateType.HISTORY;
		else
			return null;
	}
	
	/**
	 * @deprecated
	 * @param type
	 * @return
	 */
	public static DateType getType(String type){
		if(type != null && !type.trim().isEmpty()){
			if(type.trim().toUpperCase().equals(DateType.TODAY.toString().toUpperCase()))
				return DateType.TODAY;
			else if(type.trim().toUpperCase().equals(DateType.HISTORY.toString().toUpperCase()))
				return DateType.HISTORY;
			else
				return null;
		}else{
			return null;
		}
	}
	
	public boolean isToday(){
		return this == DateType.TODAY;
	}
	
	public boolean isHistory(){
		return this == DateType.HISTORY;
	}
	
	public static DateType valueOf(int type){
		for(DateType dateType : values()){
			if(dateType.type == type){
				return dateType;
			}
		}
		throw new IllegalArgumentException("The type(val = " + type + ") is invalid.");
	}
	
	@Override
	public String toString(){
		return "DateType(val = " + getValue() + ", desc = " + getDesc() + ")";
	}
	
}
