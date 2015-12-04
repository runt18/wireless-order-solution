package org.marker.weixin.js;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.marker.weixin.api.BaseAPI;
import org.marker.weixin.auth.AuthorizerToken;

import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class JsApiTicket implements Jsonable {
	private int errCode;
	private String errMsg;
	private String ticket;
	private int expiresTime;

	public static JsApiTicket newInstance(AuthorizerToken authorizerToken) throws ClientProtocolException, IOException{
		final String result = BaseAPI.doPost("https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token="+ authorizerToken.getAccessToken() +"&type=jsapi", "");
		return JObject.parse(JSON_CREATOR, 0, result);
	}
	
	public int getErrCode(){
		return this.errCode;
	}
	
	public String getErrMsg(){
		return this.errMsg;
	}
	
	public String getTicket() {
		return ticket;
	}

	public int getExpiresTime(){
		return this.expiresTime;
	}

	public static Jsonable.Creator<JsApiTicket> JSON_CREATOR = new Jsonable.Creator<JsApiTicket>() {
		@Override
		public JsApiTicket newInstance() {
			return new JsApiTicket();
		}
	};		
	
	@Override
	public JsonMap toJsonMap(int flag) {
		return null;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		this.errCode = jsonMap.getInt("errcode");
		this.errMsg = jsonMap.getString("errmsg");
		this.ticket = jsonMap.getString("ticket");
		this.expiresTime = jsonMap.getInt("expires_in");
	}
}
