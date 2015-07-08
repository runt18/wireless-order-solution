package org.marker.weixin.auth;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.marker.weixin.api.BaseAPI;

import com.alibaba.fastjson.JSONObject;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class ComponentAccessToken implements Jsonable{

	private String token;
	private int expired;
	
	public static ComponentAccessToken newInstance(final ComponentVerifyTicket ticket) throws ClientProtocolException, IOException{
		String result = BaseAPI.doPost(BaseAPI.BASE_URI + "/cgi-bin/component/api_component_token", JSONObject.toJSONString(new Jsonable(){

			@Override
			public JsonMap toJsonMap(int flag) {
				JsonMap jm = new JsonMap();
				jm.putString("component_appid", AuthParam.APP_ID);
				jm.putString("component_appsecret", AuthParam.APP_SECRET);
				jm.putString("component_verify_ticket", ticket.getTicket());
				return jm;
			}

			@Override
			public void fromJsonMap(JsonMap jsonMap, int flag) {
				
			}
			
		}.toJsonMap(0)));
		return JObject.parse(JSON_CREATOR, 0, result);
	}
	
	public String getToken(){
		return this.token;
	}
	
	public int getExpired(){
		return this.expired;
	}
	
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putString("component_access_token", token);
		jm.putInt("expires_in", expired);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		this.token = jsonMap.getString("component_access_token");
		this.expired = jsonMap.getInt("expires_in");
	}
	
	public static Jsonable.Creator<ComponentAccessToken> JSON_CREATOR = new Jsonable.Creator<ComponentAccessToken>() {
		@Override
		public ComponentAccessToken newInstance() {
			return new ComponentAccessToken();
		}
	};
	
	@Override
	public String toString(){
		return JSONObject.toJSONString(this.toJsonMap(0));
	}
}
