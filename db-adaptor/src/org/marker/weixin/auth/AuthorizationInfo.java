package org.marker.weixin.auth;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.marker.weixin.api.BaseAPI;

import com.alibaba.fastjson.JSONObject;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class AuthorizationInfo implements Jsonable{
	
	private String authorizerAppId;
	private String authorizerAccessToken;
	private int expires;
	private String authorizerRefreshToken;
	
	public static AuthorizationInfo newInstance(ComponentAccessToken componentAccessToken, final String authCode) throws ClientProtocolException, IOException{
		String result = BaseAPI.doPost(BaseAPI.BASE_URI + "/cgi-bin/component/api_query_auth?component_access_token=" + componentAccessToken.getToken(), JSONObject.toJSONString(new Jsonable(){

			@Override
			public JsonMap toJsonMap(int flag) {
				JsonMap jm = new JsonMap();
				jm.putString("component_appid", AuthParam.APP_ID);
				jm.putString("authorization_code", authCode);
				return jm;
			}

			@Override
			public void fromJsonMap(JsonMap jsonMap, int flag) {
				
			}
			
		}.toJsonMap(0)));
		
		return JObject.parse(JSON_CREATOR, 0, JSONObject.parseObject(result).getString("authorization_info"));
	}
	
	public String getAuthorizerAppId() {
		return authorizerAppId;
	}
	
	public String getAuthorizerAccessToken() {
		return authorizerAccessToken;
	}
	
	public int getExpires() {
		return expires;
	}
	
	public String getAuthorizerRefreshToken() {
		return authorizerRefreshToken;
	}
	
	@Override
	public JsonMap toJsonMap(int flag) {
		return null;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		authorizerAppId = jsonMap.getString("authorizer_appid");
		authorizerAccessToken = jsonMap.getString("authorizer_access_token");
		expires = jsonMap.getInt("expires_in");
		authorizerRefreshToken = jsonMap.getString("authorizer_refresh_token");
	}
	
	public static Jsonable.Creator<AuthorizationInfo> JSON_CREATOR = new Jsonable.Creator<AuthorizationInfo>() {
		@Override
		public AuthorizationInfo newInstance() {
			return new AuthorizationInfo();
		}
	};
	
	@Override
	public String toString(){
		return JSONObject.toJSONString(this);
	}
}
