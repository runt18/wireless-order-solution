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
	
	private boolean success = true;
	private int totalProperty = 0;
	private List root = new ArrayList();
	private byte errCode = 0;
	private String errMsg = "";
	
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
	public byte getErrCode() {
		return errCode;
	}
	public void setErrCode(byte errCode) {
		this.errCode = errCode;
	}
	public String getErrMsg() {
		return errMsg;
	}
	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}
		
}
