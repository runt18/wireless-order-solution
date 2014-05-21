package com.wireless.json;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wireless.exception.BusinessException;
import com.wireless.util.WebParams;

public class JObject implements Jsonable {
	
	private boolean success = true;         		// 操作状态
	private int totalProperty = 0;		    		// 数据数量
	private List<? extends Jsonable> root;			// 数据主体
	private int code = WebParams.ERROR_CODE;		// 错误码
	private String msg = WebParams.ERROR_MSG;		// 错误提示信息
	private String title = WebParams.ERROR_TITLE; 	// 错误信息标题
	private int lv = WebParams.ERROR_LV;			// 错误等级
	private Jsonable extra;							// 其他附加信息
	
	public JObject(){
		
	}
	
	public JObject(int totalProperty, List<? extends Jsonable> root){
		this();
		this.totalProperty = totalProperty;
		this.root = root;
	}
	
	public JObject(String msg){
		this.msg = msg;
	}
	
	public JObject(boolean success, String msg){
		this.initTip(success, this.title, WebParams.ERROR_CODE, msg);
	}
	
	public JObject(boolean success, String title, String msg){
		this.initTip(success, title, WebParams.ERROR_CODE, msg);
	}
	
	public JObject(boolean success, String title, int code, String msg){
		this.success = success;
		this.title = title;
		this.code = code;
		this.msg = msg;
	}
	
	
	
	/*-------------------------     initTip   --------------------------------*/
	public void initTip(String msg){
		this.msg = msg;
	}
	public void initTip(boolean success, String msg){
		this.initTip(success, this.title, WebParams.ERROR_CODE, msg);
	}
	public void initTip(boolean success, String title, String msg){
		this.initTip(success, title, WebParams.ERROR_CODE, msg);
	}
	public void initTip(boolean success, int code, String msg){
		this.initTip(success, this.title, code, msg);
	}
	public void initTip(boolean success, String title, int code, String msg){
		this.success = success;
		this.title = title;
		this.code = code;
		this.msg = msg;
	}
	public void initTip(BusinessException e){
		this.initTip(false, WebParams.TIP_TITLE_DEFAULT, e.getErrCode().getCode(), e.getMessage());
	}
	public void initTip(SQLException e){
		this.initTip(false, WebParams.TIP_TITLE_EXCEPTION, e.getErrorCode(), e.getMessage());
	}
	public void initTip(IllegalArgumentException e){
		this.initTip(false, WebParams.TIP_TITLE_DEFAULT, 8888, e.getMessage());
	}
	public void initTip(Exception e){
		this.initTip(false, WebParams.TIP_TITLE_EXCEPTION, 9999, WebParams.TIP_CONTENT_SQLEXCEPTION);
	}
	

	
	/*-------------------------     config     --------------------------------*/
	@Override
	public Map<String, Object> toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putBoolean("success", this.isSuccess());
		jm.putInt("totalProperty", this.getTotalProperty());
		jm.putJsonableList("root", this.getRoot(), flag);
		jm.putInt("code", this.getCode());
		jm.putString("msg", this.getMsg());
		jm.putString("title", this.getTitle());
		jm.putJsonable("other", this.getExtra(), 0);
		
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Object> toJsonMap(Map<String, Object> src){
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		for(Entry<String, Object> entry : src.entrySet()){
			if(entry.getValue() instanceof Jsonable){
				map.put(entry.getKey(), toJsonMap(((Jsonable)entry.getValue()).toJsonMap(0)));
				
			}else if(entry.getValue() instanceof Map){
				map.put(entry.getKey(), toJsonMap((Map<String, Object>)entry.getValue()));
				
			}else if(entry.getValue() instanceof List){
				List<Map<String, Object>> lm = new ArrayList<Map<String, Object>>();
				for(Object item : (List<?>)entry.getValue()){
					if(item instanceof Jsonable){
						lm.add(toJsonMap(((Jsonable)item).toJsonMap(0)));
						
					}else if(item instanceof Map){
						lm.add(toJsonMap((Map<String, Object>)item));
						
					}else{
						throw new IllegalArgumentException("The item put to json map can ONLY be map or jsonable.");
					}
				}
				map.put(entry.getKey(), lm);	
				
			}else{
				map.put(entry.getKey(), entry.getValue());
			}
		}
		return map;
	}
	
	@Override
	public String toString() {
		return JSON.toJSONString(toJsonMap(toJsonMap(0)));
	}
	
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public int getTotalProperty() {
		return totalProperty;
	}
	public void setTotalProperty(int totalProperty) {
		this.totalProperty = totalProperty;
	}
	public List<? extends Jsonable> getRoot() {
		return root;
	}
	public void setRoot(List<? extends Jsonable> root) {
		this.root = root;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getLv() {
		return lv;
	}
	public void setLv(int lv) {
		this.lv = lv;
	}
	public Jsonable getExtra() {
		return extra;
	}
	public void setExtra(Jsonable other) {
		this.extra = other;
	}
	
	public static <T extends Jsonable> T parse(Jsonable.Creator<T> creator, int flag, String jsonText){
		T instance = creator.newInstance();
		instance.fromJsonMap(new JsonMap(JSONObject.parseObject(jsonText)), flag);
		return instance;
	}
	
	public static <T extends Jsonable> List<T> parseList(Jsonable.Creator<T> creator, int flag, String jsonText){
		JSONArray jsonArray = JSONObject.parseArray(jsonText);
		List<T> result = new ArrayList<T>(jsonArray.size());
		for(int i = 0; i < jsonArray.size(); i++){
			T instance = creator.newInstance();
			instance.fromJsonMap(new JsonMap(jsonArray.getJSONObject(i)), flag);
			result.add(instance);
		}
		return result;
	}
}
