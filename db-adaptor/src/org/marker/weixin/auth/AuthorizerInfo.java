package org.marker.weixin.auth;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.marker.weixin.api.BaseAPI;

import com.alibaba.fastjson.JSONObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class AuthorizerInfo {

	private String nickName;
	private String headImg;
	private String userName;
	private String qrCodeUrl;
	private String appId;

	public static AuthorizerInfo newInstance(ComponentAccessToken componentAccessToken, final String authAppId) throws ClientProtocolException, IOException{
		String result = BaseAPI.doPost(BaseAPI.BASE_URI + "/cgi-bin/component/api_get_authorizer_info?component_access_token=" + componentAccessToken.getToken(), JSONObject.toJSONString(new Jsonable(){

			@Override
			public JsonMap toJsonMap(int flag) {
				JsonMap jm = new JsonMap();
				jm.putString("component_appid", AuthParam.APP_ID);
				jm.putString("authorizer_appid", authAppId);
				return jm;
			}

			@Override
			public void fromJsonMap(JsonMap jsonMap, int flag) {
				
			}
			
		}.toJsonMap(0)));
		
		AuthorizerInfo instance = new AuthorizerInfo();
		instance.nickName = JSONObject.parseObject(result).getJSONObject("authorizer_info").getString("nick_name");
		instance.headImg = JSONObject.parseObject(result).getJSONObject("authorizer_info").getString("head_img");
		instance.userName = JSONObject.parseObject(result).getJSONObject("authorizer_info").getString("user_name");
		instance.qrCodeUrl = JSONObject.parseObject(result).getJSONObject("authorizer_info").getString("qrcode_url");
		instance.appId = JSONObject.parseObject(result).getJSONObject("authorization_info").getString("authorizer_appid");
		return instance;
	}
	
	public String getNickName() {
		return nickName;
	}
	
	public String getHeadImg() {
		return headImg;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public String getQrCodeUrl() {
		return qrCodeUrl;
	}
	
	public String getAppId() {
		return appId;
	}
	
	@Override
	public String toString(){
		return JSONObject.toJSONString(this);
	}
}
