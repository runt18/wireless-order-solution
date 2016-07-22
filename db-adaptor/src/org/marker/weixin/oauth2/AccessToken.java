package org.marker.weixin.oauth2;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.marker.weixin.api.BaseAPI;

import com.alibaba.fastjson.JSONObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class AccessToken implements Jsonable{

	private String accessToken;
	private String refreshToken;
	private String openId;
	private int expires;
	
	private AccessToken(){}
	
	public static AccessToken newInstance(final String appId, final String appSecret, final String code) throws ClientProtocolException, IOException{
		String result = BaseAPI.doPost(BaseAPI.BASE_URI + "/sns/oauth2/access_token?appid=" + appId +"&secret=" + appSecret + "&code=" + code +"&grant_type=authorization_code" , JSONObject.toJSONString(new Jsonable() {
			
			@Override
			public JsonMap toJsonMap(int flag) {
				JsonMap jm = new JsonMap();
				jm.putString("appId", appId);
				jm.putString("secret", appSecret);
				jm.putString("code", code);
				return jm;
			}
			
			@Override
			public void fromJsonMap(JsonMap jm, int flag) {
				
			}
		}.toJsonMap(0)));
		AccessToken accessToken = new AccessToken(); 
		accessToken.refreshToken = JSONObject.parseObject(result).getString("refresh_token");
		accessToken.accessToken = JSONObject.parseObject(result).getString("access_token");
		accessToken.openId = JSONObject.parseObject(result).getString("openid");
		accessToken.expires = JSONObject.parseObject(result).getIntValue("expires_in");
		
		return accessToken;
	}
	
	
	public String getAccessToken() {
		return accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public String getOpenId() {
		return openId;
	}

	public int getExpires() {
		return expires;
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putString("access_token", accessToken);
		jm.putString("refresh_token", refreshToken);
		jm.putInt("expires_in", expires);
		jm.putString("openId", openId);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jm, int flag) {
		this.accessToken = jm.getString("access_token");
		this.refreshToken = jm.getString("refresh_token");
		this.expires = jm.getInt("expires_in");
		this.openId = jm.getString("openid");
	}
	
	public String toString(){
		return JSONObject.toJSONString(toJsonMap(0));
	}

}
