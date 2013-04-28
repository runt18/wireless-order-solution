package com.wireless.json;

import java.util.List;
import java.util.Map;

public interface Jsonable {

	public Map<String, Object> toMap(int flag);
	
	public List<?> toList(int flag);
	
}
