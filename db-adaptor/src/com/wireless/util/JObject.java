package com.wireless.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Wu.
 * @version
 * @createDate 2012.06. 05
 * @lastUpdate
 *
 */
@SuppressWarnings({"rawtypes"})
public class JObject {
	
	private boolean success = true;         		// 操作状态
	private int totalProperty = 0;		    		// 数据数量
	private List root = new ArrayList();			// 数据主体
	private byte code = WebParams.ERROR_CODE;		// 错误码
	private String msg = WebParams.ERROR_MSG;		// 错误提示信息
	private String title = WebParams.ERROR_TITLE; 	// 错误信息标题
	private long lv = WebParams.ERROR_LV;			// 错误等级
	
	public JObject(){}
	
	public JObject(int totalProperty, List root){
		this.totalProperty = totalProperty;
		this.root = root;
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

	public List getRoot() {
		return root;
	}

	public void setRoot(List root) {
		this.root = root;
	}

	public byte getCode() {
		return code;
	}

	public void setCode(byte code) {
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

	public long getLv() {
		return lv;
	}

	public void setLv(long lv) {
		this.lv = lv;
	}
	
		
}
