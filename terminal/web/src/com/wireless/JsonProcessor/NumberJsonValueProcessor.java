package com.wireless.JsonProcessor;

import net.sf.json.JSONNull;
import net.sf.json.processors.DefaultValueProcessor;

public class NumberJsonValueProcessor implements DefaultValueProcessor {

	public Object getDefaultValue(Class type) {
		return JSONNull.getInstance();
	}

}
