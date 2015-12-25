package com.wireless.json;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wireless.exception.BusinessException;

public class JObject implements Jsonable {
	
	private boolean success = true;         		// 操作状态
	private int totalProperty = 0;		    		// 数据数量
	private List<? extends Jsonable> root;			// 数据主体
	private int code = 0;							// 错误码
	private String msg = "";						// 错误提示信息
	private String title = TIP_TITLE_DEFAULT; 		// 错误信息标题
	private Jsonable extra;							// 其他附加信息
	
	public static final String TIP_TITLE_DEFAULT = "提示";
	public static final String TIP_TITLE_WARNING = "警告";
	public static final String TIP_TITLE_EXCEPTION = "异常";
	public static final String TIP_TITLE_ERROE = "错误";
	
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
		this.initTip(success, this.title, 0, msg);
	}
	
	public JObject(boolean success, String title, String msg){
		this.initTip(success, title, 0, msg);
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
		this.initTip(success, this.title, 0, msg);
	}
	public void initTip(boolean success, String title, String msg){
		this.initTip(success, title, 0, msg);
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
	
	public void initTip(Exception e){
		if(e instanceof BusinessException){
			this.initTip(false, JObject.TIP_TITLE_DEFAULT, ((BusinessException)e).getErrCode().getCode(), e.getMessage());
		}else if(e instanceof SQLException){
			this.initTip(false, JObject.TIP_TITLE_EXCEPTION, ((SQLException)e).getErrorCode(), e.getMessage());
		}else{
			this.initTip(false, JObject.TIP_TITLE_EXCEPTION, 9999, e.getMessage());
		}
	}
	
	public void initTip4Exception(Exception e){
		this.initTip(false, JObject.TIP_TITLE_EXCEPTION, 9999, e.getMessage());
	}
	

	
	/*-------------------------     config     --------------------------------*/
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putBoolean("success", this.success);
		jm.putInt("totalProperty", this.totalProperty);
		jm.putJsonableList("root", this.root, flag);
		jm.putInt("code", this.code);
		jm.putString("msg", this.msg);
		jm.putString("title", this.title);
		jm.putJsonable("other", this.extra, 0);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
	public String toString(int flag){
		return JSON.toJSONString(toJsonMap(flag));
	}
	
	@Override
	public String toString() {
		return JSON.toJSONString(toJsonMap(0));
	}
	
	public boolean isSuccess() {
		return success;
	}
	
	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	public void setTotalProperty(int totalProperty) {
		this.totalProperty = totalProperty;
	}
	
	public void setRoot(List<? extends Jsonable> root) {
		this.root = root;
	}

	public void setRoot(Jsonable root){
		final List<Jsonable> roots = new ArrayList<Jsonable>(1);
		roots.add(root);
		this.root = roots;
	}
	
	public void setCode(int code) {
		this.code = code;
	}
	
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	public String getMsg(){
		return this.msg;
	}
	
	public void setTitle(String title) {
		this.title = title;
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
