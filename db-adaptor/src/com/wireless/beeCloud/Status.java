package com.wireless.beeCloud;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import org.apache.http.client.ClientProtocolException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class Status {

	private final BeeCloud app;
	
	Status(BeeCloud app){
		this.app = app;
	}
	
	public Response ask(Bill.Channel channel, String billNo) throws KeyManagementException, ClientProtocolException, NoSuchAlgorithmException, IOException{
		String response = app.doGet("https://" + BeeCloud.DYNC + "/2/rest/bills?para=" + URLEncoder.encode(new Request(app, billNo, channel).toString(), "UTF-8"));
		return JObject.parse(Response.JSON_CREATOR, 0, response);
	}
	
	private static class Request implements Jsonable{

		Request(BeeCloud app, String billNo, Bill.Channel channel){
			this.appId = app.appId;
			this.timestamp = System.currentTimeMillis();
			this.appSign = app.createAppSign(timestamp);
			this.billNo = billNo;
			this.channel = channel;
		}
		
		private final String appId;
		private final long timestamp;
		private final String appSign;
		private final String billNo;
		private final Bill.Channel channel;
		
		@Override
		public JsonMap toJsonMap(int flag) {
			JsonMap jm = new JsonMap();
			jm.putString("app_id", this.appId);
			jm.putLong("timestamp", this.timestamp);
			jm.putString("app_sign", this.appSign);
			jm.putString("bill_no", this.billNo);
			jm.putString("channel", this.channel.val);
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
		//private boolean payResult;
		
		private String billNo;
		private String title;
		private String channel;
		private String subChannel;
		private boolean payResult;
		
		public int getResultCode() {
			return resultCode;
		}

		public String getResultMsg() {
			return resultMsg;
		}

		public String getErrDetail() {
			return errDetail;
		}

		public boolean isPaySuccess(){
			return this.payResult;
		}
		
		@Override
		public JsonMap toJsonMap(int flag) {
			JsonMap jm = new JsonMap();
			jm.putInt("result_code", this.resultCode);
			jm.putString("result_msg", this.resultMsg);
			jm.putString("err_detail", this.errDetail);
			
			jm.putBoolean("spay_result", this.payResult);
			jm.putString("bill_no", this.billNo);
			jm.putString("title", this.title);
			jm.putString("channel", this.channel);
			jm.putString("sub_channel", this.subChannel);
			return jm;
		}

		@Override
		public void fromJsonMap(JsonMap jm, int flag) {
			this.resultCode = jm.getInt("result_code");
			this.resultMsg = jm.getString("result_msg");
			this.errDetail = jm.getString("err_detail");
			
			JSONObject billObject = (JSONObject)jm.getJSONObject().getJSONArray("bills").get(0);
			this.billNo = billObject.getString("bill_no");
			this.title = billObject.getString("title");
			this.channel = billObject.getString("channel");
			this.subChannel = billObject.getString("sub_channel");
			this.payResult = billObject.getBoolean("spay_result");
		}
		
		@Override
		public String toString(){
			return JSON.toJSONString(toJsonMap(0));
		}
		
		static Jsonable.Creator<Response> JSON_CREATOR = new Jsonable.Creator<Response>() {
			@Override
			public Response newInstance() {
				return new Response();
			}
		};
	}
}
