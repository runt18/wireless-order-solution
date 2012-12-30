package com.wireless.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 
 * @author Wu.
 * @version
 * @createDate 2012.06.05
 * @lastUpdate
 *
 */
@SuppressWarnings({"rawtypes"})
public class JObject {
	
	private boolean success = true;         		// 操作状态
	private int totalProperty = 0;		    		// 数据数量
	private List root;								// 数据主体
	private int code = WebParams.ERROR_CODE;		// 错误码
	private String msg = WebParams.ERROR_MSG;		// 错误提示信息
	private String title = WebParams.ERROR_TITLE; 	// 错误信息标题
	private int lv = WebParams.ERROR_LV;			// 错误等级
	private HashMap other = new HashMap();			// 其他附加信息
	
	/*-----------------------             ------------------------*/
	public JObject(){
		this.root = new ArrayList(); 
	}
	
	public JObject(int totalProperty, List root){
		this.root = new ArrayList(); 
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
	
	/*-------------------------     InitTip   --------------------------------*/
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
	
	/*--------------------------     Get Set    ----------------------------*/
	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public int getTotalProperty() {
		return totalProperty == 0 ? root.size() : totalProperty;
	}

	public void setTotalProperty(int totalProperty) {
		this.totalProperty = totalProperty;
	}

	public List getRoot() {
		return root;
	}

	public void setRoot(List root) {
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

	public HashMap getOther() {
		return other;
	}

	public void setOther(HashMap other) {
		this.other = other;
	}	
	
}
