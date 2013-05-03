package com.wireless.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public final class JsonPackage {
	
	private final List<Jsonable> mJsonables = new ArrayList<Jsonable>();
	
	private final Jsonable.Type mType;
	
	private final int mFlag;
	
	public JsonPackage(Jsonable jsonable, int flag, Jsonable.Type type){
		mType = type;
		mFlag = flag;
		mJsonables.add(jsonable);
	}
	
	public JsonPackage(List<? extends Jsonable> jsonables, int flag, Jsonable.Type type){
		mType = type;
		mFlag = flag;
		mJsonables.addAll(jsonables);
	}
	
	public JsonPackage(Jsonable[] jsonables, int flag, Jsonable.Type type){
		mType = type;
		mFlag = flag;
		mJsonables.addAll(Arrays.asList(jsonables));
	}
	
	@Override
	public String toString(){
		if(mJsonables.size() == 1 && mType == Jsonable.Type.LIST){
			return JSONArray.fromObject(mJsonables.get(0).toJsonList(mFlag)).toString();
			
		}else if(mJsonables.size() == 1 && mType == Jsonable.Type.PAIR){
			return JSONObject.fromObject(mJsonables.get(0).toJsonMap(mFlag)).toString();
			
		}else if(mJsonables.size() > 1){
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			int count = 0;
			for(Jsonable jsonable : mJsonables){
				if(count > 0){
					sb.append(",");
				}
				sb.append(new JsonPackage(jsonable, mFlag, mType));
				count++;
			}
			sb.append("]");
			return sb.toString();
			
		}else {
			throw new IllegalStateException();
		}
	}
}
