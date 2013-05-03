package com.wireless.json;

import java.util.List;
import java.util.Map;

public interface Jsonable {

	public static enum Type{
		PAIR,
		LIST;
	}
	
	/**
	 * Flatten the object to a key-value map.
	 * @param flag additional flags about how the object should be flatten
	 * @return the key-value map to the object
	 */
	public Map<String, Object> toJsonMap(int flag);
	
	/**
	 * Flatten the object to a list.
	 * @param flag additional flags about how the object should be flatten
	 * @return the list to the object
	 */
	public List<Object> toJsonList(int flag);
	
}
