package com.wireless.beeCloud;

import com.alibaba.fastjson.JSON;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class WebHook implements Jsonable{
	
	private String sign;
	private long timestamp;
	private String channelType;
	private String subChannelType;
	private String transactionType;
	private String transactionId;
	private int transactionFee;
	private boolean tradeSuccess;
	private String messageDetail;
	private String optional;
	
	public String getSign() {
		return sign;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public String getChannelType() {
		return channelType;
	}
	
	public String getSubChannelType() {
		return subChannelType;
	}
	
	public String getTransactionType() {
		return transactionType;
	}
	
	public String getTransactionId() {
		return transactionId;
	}
	
	public int getTransactionFee() {
		return transactionFee;
	}
	
	public boolean isTradeSuccess() {
		return tradeSuccess;
	}
	
	public String getMessageDetail() {
		return messageDetail;
	}
	
	public String getOptional() {
		return optional;
	}
	
	@Override
	public JsonMap toJsonMap(int flag) {
		return null;
	}
	@Override
	public void fromJsonMap(JsonMap jm, int flag) {
		this.sign = jm.getString("sign");
		this.timestamp = jm.getLong("timestamp");
		this.channelType = jm.getString("channel_type");
		this.subChannelType = jm.getString("sub_channel_type");
		this.transactionType = jm.getString("transaction_type");
		this.transactionId = jm.getString("transaction_id");
		this.transactionFee = jm.getInt("transaction_fee");
		this.tradeSuccess = jm.getBoolean("trade_success");
		this.messageDetail = jm.getString("message_detail");
		this.optional = jm.getString("optional");
	}
	
	public static Jsonable.Creator<WebHook> JSON_CREATOR = new Jsonable.Creator<WebHook>() {
		@Override
		public WebHook newInstance() {
			return new WebHook();
		}
	};
	
	@Override
	public String toString(){
		return JSON.toJSONString(this);
	}
}
