package com.wireless.sms.msg;

import com.wireless.pojo.sms.SMSDetail;

public class Msg {
	private final String sign;
	private final String content;
	private final SMSDetail.Operation operation;
	
	public Msg(String content, String sign, SMSDetail.Operation operation){
		this.content = content;
		this.sign = sign;
		this.operation = operation;
	}
	
	public String getSign(){
		return sign;
	}
	
	public String getContent(){
		return content;
	}
	
	public SMSDetail.Operation getOperation(){
		return operation;
	}
	
	@Override
	public String toString(){
		return content + "【" + (sign.trim().isEmpty() ? "微信餐厅" : sign) + "】";
	}
}
