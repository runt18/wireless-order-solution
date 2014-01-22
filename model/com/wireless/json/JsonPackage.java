package com.wireless.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSONArray;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import net.sf.json.util.CycleDetectionStrategy;

public final class JsonPackage {
	
	private final List<Jsonable> mJsonables = new ArrayList<Jsonable>();
	
	private final Jsonable.Type mType;
	
	private final int mFlag;
	
	private JsonConfig jc = new JsonConfig();
	
	void initJsonConfig(){
		this.jc.setIgnoreDefaultExcludes(false);       
		this.jc.setCycleDetectionStrategy(CycleDetectionStrategy.LENIENT);     
	}
	
	public JsonPackage(Jsonable jsonable, int flag, Jsonable.Type type){
		initJsonConfig();
		mType = type;
		mFlag = flag;
		mJsonables.add(jsonable);
	}
	
	public JsonPackage(List<? extends Jsonable> jsonables, int flag, Jsonable.Type type){
		initJsonConfig();
		mType = type;
		mFlag = flag;
		mJsonables.addAll(jsonables);
	}
	
	public JsonPackage(Jsonable[] jsonables, int flag, Jsonable.Type type){
		initJsonConfig();
		mType = type;
		mFlag = flag;
		mJsonables.addAll(Arrays.asList(jsonables));
	}
	
	private Map<String, Object> toJsonMap(){
		if(mType == Jsonable.Type.PAIR){
			Map<String, Object> map = new HashMap<String, Object>();
			for(Jsonable jsonable : mJsonables){
				for(Entry<String, Object> entry : jsonable.toJsonMap(mFlag).entrySet()){
					if(entry.getValue() instanceof Jsonable){
						map.put(entry.getKey(), new JsonPackage((Jsonable)entry.getValue(), mFlag, Jsonable.Type.PAIR).toJsonMap());
						
						
					}else if(entry.getValue() instanceof Map){
						map.put(entry.getKey(), entry.getValue());
					}
				}
			}
			return map;
		}else{
			throw new UnsupportedOperationException("The json type is NOT map.");
		}
	}
	
	private List<List<Object>> toJsonList(){
		if(mType == Jsonable.Type.LIST){
			List<List<Object>> list = new ArrayList<List<Object>>();
			if(mJsonables.size() == 1){
				list.add(mJsonables.get(0).toJsonList(mFlag));
			}else{
				for(Jsonable jsonable : mJsonables){
					list.add(jsonable.toJsonList(mFlag));
				}
			}
			return list;
		}else{
			throw new UnsupportedOperationException("The json type is NOT list.");
		}
	}
	
	@SuppressWarnings("unchecked")
	private void changeToMap(Map<String, Object> mobj){
		if(mobj == null)
			return;
		Iterator<Entry<String, Object>> it = mobj.entrySet().iterator();
		Map<String, Object> item = null;
		while(it.hasNext()){	
			Entry<String, Object> entry = it.next();
			if(entry != null){
				if(entry.getValue() instanceof List){
					List<?> list = (List<?>)entry.getValue();
					List<Map<String, Object>> lm = new ArrayList<Map<String, Object>>();
					for(Object temp : list){
						if(temp instanceof Jsonable){
							item = new LinkedHashMap<String, Object>(((Jsonable)temp).toJsonMap(mFlag));
							changeToMap(item);
							lm.add(item);
						}else if(temp instanceof Map){
							Map<String, Object> tempMap = (Map<String, Object>) temp;
							changeToMap(tempMap);
							lm.add(tempMap);
						}
					}
					entry.setValue(lm);
				}else if(entry.getValue() instanceof Jsonable){
					item = new LinkedHashMap<String, Object>(((Jsonable)entry.getValue()).toJsonMap(mFlag));
					changeToMap(item);
					entry.setValue(item);
				}else if(entry.getValue() instanceof Map){
					item = new LinkedHashMap<String, Object>((Map<String, Object>)entry.getValue());
					changeToMap(item);
					entry.setValue(item);
				}
			}
		}
	}
	
	
	
	@Override
	public String toString(){
		if(mJsonables.size() == 1 && mType == Jsonable.Type.LIST){
			return JSONArray.fromObject(mJsonables.get(0).toJsonList(mFlag)).toString();
		}else if(mJsonables.size() == 1 && mType == Jsonable.Type.PAIR){
			Map<String, Object> copy = new LinkedHashMap<String, Object>(mJsonables.get(0).toJsonMap(mFlag));
			changeToMap(copy);
			return JSONSerializer.toJSON(copy, this.jc).toString();
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
