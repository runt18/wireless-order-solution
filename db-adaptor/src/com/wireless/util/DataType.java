package com.wireless.util;

import java.util.Map;

public enum DataType {
	
	TODAY(0x0, "当日"), HISTORY(0x1, "历史");
	
	private int type;
	private String name;
	
	private DataType(int type, String name){
		this.type = type;
		this.name = name;
	}
	
	public int getValue(){
		return type;
	}
	
	public String getName(){
		return name;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 */
	public static int getValue(Map<Object, Object> params){
		if(params.get(DataType.TODAY) != null)
			return DataType.TODAY.getValue();
		if(params.get(DataType.HISTORY) != null)
			return DataType.HISTORY.getValue();
		else
			return -1;
	}
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	public static int getValue(String type){
		DataType dt = getType(type);
		if(dt == null){
			return -1;
		}else{
			return dt.getValue();
		}
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 */
	public static boolean hasType(Map<Object, Object> params){
		if(params.get(DataType.TODAY) != null)
			return true;
		if(params.get(DataType.HISTORY) != null)
			return true;
		else
			return false;
	}
	
	/**
	 * 
	 * @param params
	 * @return
	 */
	public static DataType getType(Map<Object, Object> params){
		if(params.get(DataType.TODAY) != null)
			return DataType.TODAY;
		if(params.get(DataType.HISTORY) != null)
			return DataType.HISTORY;
		else
			return null;
	}
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	public static DataType getType(String type){
		if(type != null && !type.trim().isEmpty()){
			if(type.trim().toUpperCase().equals(DataType.TODAY.toString().toUpperCase()))
				return DataType.TODAY;
			else if(type.trim().toUpperCase().equals(DataType.HISTORY.toString().toUpperCase()))
				return DataType.HISTORY;
			else
				return null;
		}else{
			return null;
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isToday(){
		return this == DataType.TODAY;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isHistory(){
		return this == DataType.HISTORY;
	}
	
	@Override
	public String toString() {
		return this.name();
	}
	
}
