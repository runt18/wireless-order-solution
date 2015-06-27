package com.wireless.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class JsonMap implements Map<String, Object>{

	private final JSONObject jmap = new JSONObject(true);
	
	public JsonMap(){
		
	}
	
	public JsonMap(Map<String, Object> map){
		jmap.putAll(map);
	}
	
	public JSONObject getJSONObject(){
		return this.jmap;
	}
	
	@Override
	public void clear() {
		jmap.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return jmap.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return jmap.containsValue(value);
	}
	
	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		return jmap.entrySet();
	}

	/**
	 * @deprecated
	 */
	@Override
	public Object get(Object key) {
		throw new UnsupportedOperationException();
	}

	public byte getByte(String key){
		return jmap.getByteValue(key);
	}
	
	public short getShort(String key){
		return jmap.getShortValue(key);
	}
	
	public int getInt(String key){
        return jmap.getIntValue(key);
	}
	
	public long getLong(String key){
		return jmap.getLongValue(key);
	}
	
	public float getFloat(String key){
		return jmap.getFloatValue(key);
	}
	
	public String getString(String key){
		return jmap.getString(key);
	}
	
	public boolean getBoolean(String key){
		return jmap.getBooleanValue(key);
	}
	
	public <T extends Jsonable> T getJsonable(String key, Jsonable.Creator<T> creator, int flag){
		T instance = creator.newInstance();
		instance.fromJsonMap(new JsonMap(jmap.getJSONObject(key)), flag);
		return instance;
	}
	
	public <T extends Jsonable> List<T> getJsonableList(String key, Jsonable.Creator<T> creator, int flag){
		JSONArray jsonArray = jmap.getJSONArray(key);
		List<T> result = new ArrayList<T>(jsonArray.size());
		for(int i = 0; i < jsonArray.size(); i++){
			T instance = creator.newInstance();
			instance.fromJsonMap(new JsonMap(jsonArray.getJSONObject(i)), flag);
			result.add(instance);
		}
		return result;
	}
	
	@Override
	public boolean isEmpty() {
		return jmap.isEmpty();
	}

	@Override
	public Set<String> keySet() {
		return jmap.keySet();
	}

	/**
	 * @deprecated
	 * You should use other methods like {@link #putInt(String, int)} or {@link #putString(String, String)}
	 * to put specific value to json map.
	 */
	@Override
	public Object put(String key, Object value) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @deprecated
	 * You should use other methods like {@link #putInt(String, int)} or {@link #putString(String, String)}
	 * to put specific value to json map.
	 */
	@Override
	public void putAll(Map<? extends String, ? extends Object> arg0) {
		throw new UnsupportedOperationException("");
	}
	
	public void putByte(String key, byte byteValue){
		jmap.put(key, byteValue);
	}
	
	public void putShort(String key, short shortValue){
		jmap.put(key, shortValue);
	}
	
	public void putInt(String key, int intValue){
		jmap.put(key, intValue);
	}

	public void putLong(String key, long longValue){
		jmap.put(key, longValue);
	}
	
	public void putFloat(String key, float floatValue){
		jmap.put(key, floatValue);
	}
	
	public void putString(String key, String stringValue){
		jmap.put(key, stringValue);
	}
	
	public void putBoolean(String key, boolean boolValue){
		jmap.put(key, boolValue);
	}
	
	public void putJsonMap(String key, JsonMap jm){
		if(jm != null){
			jmap.put(key, jm);
		}
	}
	
	public void putJsonMap(JsonMap jm){
		if(jm != null){
			jmap.putAll(jm);
		}
	}
	
	public void putJsonable(String key, Jsonable jsonable, int flag){
		if(jsonable != null){
			jmap.put(key, jsonable.toJsonMap(flag));
		}
	}
	
	public void putJsonable(Jsonable jsonable, int flag){
		if(jsonable != null){
			jmap.putAll(jsonable.toJsonMap(flag));
		}
	}
	
	public void putJsonableList(String key, List<? extends Jsonable> jsonables, int flag){
		if(jsonables != null){
			List<Map<String, Object>> lm = new ArrayList<Map<String, Object>>(jsonables.size());
			for(Jsonable jsonable : jsonables){
				if(jsonable != null){
					lm.add(jsonable.toJsonMap(flag));
				}
			}
			jmap.put(key, lm);
		}
	}
	
	@Override
	public Object remove(Object key) {
		return jmap.remove(key);
	}

	@Override
	public int size() {
		return jmap.size();
	}

	@Override
	public Collection<Object> values() {
		return jmap.values();
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof JsonMap)){
			return false;
		}else{
			return ((JsonMap)obj).jmap.equals(jmap);
		}
	}
	
	@Override
	public int hashCode(){
		return jmap.hashCode();
	}

	@Override
	public String toString(){
		return jmap.toString();
	}
}
