package org.marker.weixin.auth;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.marker.weixin.api.BaseAPI;

import com.alibaba.fastjson.JSONObject;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class PreAuthCode implements Jsonable{

	private String code;
	private int expired;

	public static PreAuthCode newInstance(final ComponentAccessToken componentAccessToken) throws ClientProtocolException, IOException{
		String result = BaseAPI.doPost(BaseAPI.BASE_URI + "/cgi-bin/component/api_create_preauthcode?component_access_token=" + componentAccessToken.getToken(), JSONObject.toJSONString(new Jsonable(){
			@Override
			public JsonMap toJsonMap(int flag) {
				JsonMap jm = new JsonMap();
				jm.putString("component_appid", AuthParam.APP_ID);
				return jm;
			}
			@Override
			public void fromJsonMap(JsonMap jsonMap, int flag) {
				
			}
		}.toJsonMap(0)));
		return JObject.parse(JSON_CREATOR, 0, result);
	}
	
	public String getCode(){
		return this.code;
	}
	
	public int getExpired(){
		return this.expired;
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putString("pre_auth_code", code);
		jm.putInt("expires_in", expired);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		this.code = jsonMap.getString("pre_auth_code");
		this.expired = jsonMap.getInt("expires_in");
	}
	
	public static Jsonable.Creator<PreAuthCode> JSON_CREATOR = new Jsonable.Creator<PreAuthCode>() {
		@Override
		public PreAuthCode newInstance() {
			return new PreAuthCode();
		}
	};
	
	@Override
	public String toString(){
		return JSONObject.toJSONString(this.toJsonMap(0));
	}
}
