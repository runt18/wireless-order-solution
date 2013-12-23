package com.wireless.json;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
	private Map<Object, Object> other;				// 其他附加信息
	
	public JObject(){
		this.root = new ArrayList<Jsonable>();
		this.other = new LinkedHashMap<Object, Object>();
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
		this.initTip(false, WebParams.TIP_TITLE_DEFAULT, e.getCode(), e.getMessage());
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
		Map<String, Object> jm = new LinkedHashMap<String, Object>();
		jm.put("success", this.isSuccess());
		jm.put("totalProperty", this.getTotalProperty());
		jm.put("root", this.getRoot());
		jm.put("code", this.getCode());
		jm.put("msg", this.getMsg());
		jm.put("title", this.getTitle());
		jm.put("other", this.getOther());
		
		return Collections.unmodifiableMap(jm);
	}

	@Override
	public List<Object> toJsonList(int flag) {
		
		return null;
	}
	
	@Override
	public String toString() {
		JsonPackage jp = new JsonPackage(this, 0, Jsonable.Type.PAIR);
		return jp.toString();
	}
	
	public String toString(int flag) {
		JsonPackage jp = new JsonPackage(this, flag, Jsonable.Type.PAIR);
		return jp.toString();
	}
	
	public String toString(Jsonable.Type type) {
		JsonPackage jp = new JsonPackage(this, 0, type);
		return jp.toString();
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
	public Map<Object, Object> getOther() {
		return other;
	}
	public void setOther(Map<Object, Object> other) {
		this.other = other;
	}
	
}
