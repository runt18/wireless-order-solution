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
	private int lv = 0;								// 错误等级
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
	public void initTip(BusinessException e){
		this.initTip(false, JObject.TIP_TITLE_DEFAULT, e.getErrCode().getCode(), e.getMessage());
	}
	public void initTip(SQLException e){
		this.initTip(false, JObject.TIP_TITLE_EXCEPTION, e.getErrorCode(), e.getMessage());
	}
	public void initTip4Exception(Exception e){
		this.initTip(false, JObject.TIP_TITLE_EXCEPTION, 9999, e.getMessage());
	}
	

	
	/*-------------------------     config     --------------------------------*/
	@Override
	public JsonMap toJsonMap(int flag) {
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

	public void setRoot(Jsonable root){
		final List<Jsonable> roots = new ArrayList<Jsonable>(1);
		roots.add(root);
		this.root = roots;
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
