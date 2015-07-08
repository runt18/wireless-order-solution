package org.marker.weixin.auth;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.marker.weixin.api.BaseAPI;

import com.alibaba.fastjson.JSONObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class AuthorizerToken implements Jsonable{

	private String appId;
	private String accessToken;
	private String refreshToken;
	private int expired;
	
	public static AuthorizerToken newInstance(ComponentAccessToken componentAccessToken, final String authorizerAppId, final String refreshToken) throws ClientProtocolException, IOException{
		String result = BaseAPI.doPost(BaseAPI.BASE_URI + "/cgi-bin/component/api_authorizer_token?component_access_token=" + componentAccessToken.getToken(), JSONObject.toJSONString(new Jsonable(){

			@Override
			public JsonMap toJsonMap(int flag) {
				JsonMap jm = new JsonMap();
				jm.putString("component_appid", AuthParam.APP_ID);
				jm.putString("authorizer_appid", authorizerAppId);
				jm.putString("authorizer_refresh_token", refreshToken);
				return jm;
			}

			@Override
			public void fromJsonMap(JsonMap jsonMap, int flag) {
				
			}
			
		}.toJsonMap(0)));
		
		AuthorizerToken instance = new AuthorizerToken();
		instance.accessToken = JSONObject.parseObject(result).getString("authorizer_access_token");
		instance.refreshToken = JSONObject.parseObject(result).getString("authorizer_refresh_token");
		instance.expired = JSONObject.parseObject(result).getIntValue("expires_in");
		return instance;
	}
	
	public int getExpired(){
		return this.expired;
	}
	
	public String getAppId() {
		return appId;
	}
	
	public String getAccessToken() {
		return accessToken;
	}
	
	public String getRefreshToken() {
		return refreshToken;
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putString("authorizer_access_token", accessToken);
		jm.putString("authorizer_refresh_token", refreshToken);
		jm.putInt("expires_in", expired);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		this.accessToken = jsonMap.getString("authorizer_access_token");
		this.refreshToken = jsonMap.getString("authorizer_refresh_token");
		this.expired = jsonMap.getInt("expires_in");
	}
	
	@Override
	public String toString(){
		return JSONObject.toJSONString(toJsonMap(0));
	}
	
}
