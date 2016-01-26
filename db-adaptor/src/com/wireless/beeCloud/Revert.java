package com.wireless.beeCloud;

import com.alibaba.fastjson.JSON;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class Revert {

	private final BeeCloud app;
	
	Revert(BeeCloud app){
		this.app = app;
	}
	
	public Response ask(String billNo, Bill.Channel channel) throws Exception{
		final String url = "https://" + BeeCloud.DYNC + "/2/rest" + (channel.online ? "" : "/offline") + "/bill/" + billNo;
		return JObject.parse(Response.JSON_CREATOR, 0, this.app.doPost(url, new Request(this.app, channel).toString()));
	}
	
	static class Request implements Jsonable{
		final private String appId;
		final private long timestamp;
		final private String appSign;
		final private Bill.Channel channel;
		
		Request(BeeCloud app, Bill.Channel channel){
			this.appId = app.appId;
			this.timestamp = System.currentTimeMillis();
			this.appSign = app.createAppSign(timestamp);
			this.channel = channel;
		}
		
		@Override
		public JsonMap toJsonMap(int flag) {
			JsonMap jm = new JsonMap();
			jm.putString("app_id", this.appId);
			jm.putLong("timestamp", this.timestamp);
			jm.putString("app_sign", this.appSign);
			jm.putString("channel", this.channel.val);
			jm.putString("method", "REVERT");
			return jm;
		}
		
		@Override
		public void fromJsonMap(JsonMap jm, int flag) {
		}
	
		@Override
		public String toString(){
			return JSON.toJSONString(toJsonMap(0));
		}
	}
	
	public static class Response implements Jsonable{

		private int resultCode;
		private String resultMsg;
		private String errDetail;
		private boolean revertStatus;
		
		public boolean isOk() {
			return resultCode == 0;
		}

		public String getResultMsg() {
			return resultMsg;
		}

		public String getErrDetail() {
			return errDetail;
		}

		public boolean isRevert() {
			return revertStatus;
		}

		@Override
		public JsonMap toJsonMap(int flag) {
			return null;
		}

		@Override
		public void fromJsonMap(JsonMap jm, int flag) {
			this.resultCode = jm.getInt("result_code");
			this.resultMsg = jm.getString("result_msg");
			this.errDetail = jm.getString("err_detail");
			this.revertStatus = jm.getBoolean("revert_status");
		}
		
		@Override
		public String toString(){
			return JSON.toJSONString(this);
		}
		
		static Jsonable.Creator<Response> JSON_CREATOR = new Jsonable.Creator<Response>() {
			@Override
			public Response newInstance() {
				return new Response();
			}
		};
	}
}
