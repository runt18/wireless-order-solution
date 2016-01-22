package com.wireless.beeCloud;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pack.ProtocolPackage;

public class Bill {
	
	public static enum Channel{
		WX_NATIVE("WX_NATIVE", "微信二维码支付"),
		WX_SCAN("WX_SCAN", "微信条形码支付"),
		WX_JSAPI("WX_JSAPI", "微信公众号支付");
		final String val;
		final String desc;
		
		Channel(String val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		@Override
		public String toString(){
			return this.desc;
		}
	}

	private final BeeCloud app;
	
	Bill(BeeCloud app){
		this.app = app;
	}
	
//	public Response ask(Channel channel, int totalFee, String billNo, String title) throws Exception{
//		String response = app.doPost("https://" + BeeCloud.DYNC + "/1/rest/offline/bill", new Request(app, channel, totalFee, billNo, title).toString());
//		return JObject.parse(Response.JSON_CREATOR, 0, response);
//	}
//	
//	public Response ask(Channel channel, int totalFee, String billNo, String title, String authCode) throws Exception{
//		String response = app.doPost("https://" + BeeCloud.DYNC + "/1/rest/offline/bill", new Request(app, channel, totalFee, billNo, title).setAuthCode(authCode).toString());
//		return JObject.parse(Response.JSON_CREATOR, 0, response);
//	}

	public Response ask(final Request request, final Callable<ProtocolPackage> postAction) throws Exception{
		return ask(request, postAction, 10);
	}
	
	public Response ask(final Request request, final Callable<ProtocolPackage> postAction, final int timeout) throws Exception{
		String responseStr = app.doPost("https://" + BeeCloud.DYNC + "/2/rest/bill", request.setBeeCloud(app).toString());
		Response response = JObject.parse(Response.JSON_CREATOR, 0, responseStr);
		if(response.isOk() && postAction != null){
			final long now = System.currentTimeMillis();
			final ScheduledThreadPoolExecutor schedule = new ScheduledThreadPoolExecutor(1);
			new Callable<ProtocolPackage>() {
				@Override
				public ProtocolPackage call() throws Exception {
					if(System.currentTimeMillis() - now > timeout * 60 * 1000){
						schedule.shutdown();
						app.revert.ask(request.billNo, request.channel);
						return null;
					}else if(new Status(app).ask(request.channel, request.billNo).isPaySuccess()){
						schedule.shutdown();
						return postAction.call();
					}else{
						schedule.schedule(this, 5, TimeUnit.SECONDS);
						return null;
					}
				}
			}.call();
			
		}
		return response;
	}

	public static class Request implements Jsonable{
		
//		public Request(BeeCloud app, Channel channel, int totalFee, String billNo, String title){
//			this.appId = app.appId;
//			this.timestamp = System.currentTimeMillis();
//			this.appSign = app.createAppSign(timestamp);
//			this.totalFee = totalFee;
//			this.channel = channel;
//			this.billNo = billNo;
//			this.title = title;
//		}
		
		public Request(){
			
		}
		
		Request setBeeCloud(BeeCloud app){
			this.appId = app.appId;
			this.timestamp = System.currentTimeMillis();
			this.appSign = app.createAppSign(timestamp);
			return this;
		}
		
		public Request setChannel(Channel channel){
			this.channel = channel;
			return this;
		}
		
		public Request setTotalFee(int totalFee){
			this.totalFee = totalFee;
			return this;
		}
		
		public Request setTitle(String title){
			this.title = title;
			return this;
		}
		
		public Request setBillNo(String billNo){
			this.billNo = billNo;
			return this;
		}
		
		public Request setAuthCode(String authCode){
			this.authCode = authCode;
			return this;
		}
		
		public Request setOptional(JsonMap optional){
			this.optional = optional;
			return this;
		}
		
		public Request setOptional(Map<String, String> optional){
			this.optional = new JsonMap();
			for(Map.Entry<String, String> entry : optional.entrySet()){
				this.optional.putString(entry.getKey(), entry.getValue());
			}
			return this;
		}
		
		public Request setOpenId(String openId){
			this.openId = openId;
			return this;
		}
		
		private String appId;
		private long timestamp;
		private String appSign;
		private Channel channel;
		private int totalFee;
		private String billNo;
		private String title;
		private JsonMap optional;
		private String authCode;
		private String openId;
		
		@Override
		public JsonMap toJsonMap(int flag) {
			JsonMap jm = new JsonMap();
			jm.putString("app_id", this.appId);
			jm.putLong("timestamp", timestamp);
			jm.putString("app_sign", appSign);
			jm.putString("channel", channel.val);
			jm.putInt("total_fee", totalFee);
			jm.putString("bill_no", billNo);
			jm.putString("title", title);
			jm.putString("openid", this.openId);
			jm.putInt("bill_timeout", 360);
			jm.putJsonable("optional", optional != null ? 
				new Jsonable(){

					@Override
					public JsonMap toJsonMap(int flag) {
						return optional;
					}

					@Override
					public void fromJsonMap(JsonMap jm, int flag) {
					}
				
			} : null, 0);
			jm.putString("auth_code", this.authCode);
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

		private int resultCode = -1;
		private String resultMsg;
		private String errDetail;
		private boolean payResult;
		private String codeUrl;

		//-------------WX_JSAPI时返回---------------
		private String appId;		//微信应用APPID
		private String pack;		//微信支付打包参数
		private String nonceStr;	//随机字符串
		private String timestamp;	//当前时间戳，单位是毫秒，13位
		private String paySign;		//签名
		private String signType;	//签名类型，固定为MD5
		//------------------------------
		
		public boolean isOk(){
			return this.resultCode == 0;
		}
		
		public int getResultCode(){
			return this.resultCode;
		}

		public String getResultMsg(){
			return this.resultMsg;
		}
		
		public String getErrDetail(){
			return this.errDetail;
		}
		
		public boolean isPaySuccess(){
			return this.payResult;
		}
		
		public String getCodeUrl(){
			return this.codeUrl;
		}
		
		public String getAppId() {
			return appId;
		}

		public String getPackage() {
			return pack;
		}

		public String getNonceStr() {
			return nonceStr;
		}

		public String getTimestamp() {
			return timestamp;
		}

		public String getPaySign() {
			return paySign;
		}

		public String getSignType() {
			return signType;
		}

		public static Jsonable.Creator<Response> getJSON_CREATOR() {
			return JSON_CREATOR;
		}

		@Override
		public JsonMap toJsonMap(int flag) {
			JsonMap jm = new JsonMap();
			jm.putInt("result_code", this.resultCode);
			jm.putString("result_msg", this.resultMsg);
			jm.putString("err_detail", this.errDetail);
			jm.putBoolean("pay_result", this.payResult);
			jm.putString("code_url", this.codeUrl);
			
			//WX_JSAPI
			jm.putString("appId", this.appId);
			jm.putString("package", this.pack);
			jm.putString("nonceStr", this.nonceStr);
			jm.putString("timeStamp", this.timestamp);
			jm.putString("paySign", this.paySign);
			jm.putString("signType", this.signType);
			return jm;
		}

		@Override
		public void fromJsonMap(JsonMap jm, int flag) {
			resultCode = jm.getInt("result_code");
			resultMsg = jm.getString("result_msg");
			errDetail = jm.getString("err_detail");
			payResult = jm.getBoolean("pay_result");
			codeUrl = jm.getString("code_url");
			
			//WX_JSAPI
			appId = jm.getString("app_id");
			pack = jm.getString("package");
			nonceStr = jm.getString("nonce_str");
			timestamp = jm.getString("timestamp");
			paySign = jm.getString("pay_sign");
			signType = jm.getString("sign_type");
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
