package org.marker.weixin.api;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.marker.weixin.auth.AuthorizerToken;

import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class Token implements Jsonable{
	
	private String accessToken;
	private int expiresIn;

	private Token(){}
	
	public static Token newInstance(AuthorizerToken authorizerToken){
		Token token = new Token();
		token.setAccessToken(authorizerToken.getAccessToken());
		token.setExpiresIn(authorizerToken.getExpired());
		return token;
	}
	
	public static Token newInstance(String appId, String appSecret) throws ClientProtocolException, IOException{
		return JObject.parse(JSON_CREATOR, 0, BaseAPI.doGet(BaseAPI.BASE_URI + "/cgi-bin/token?grant_type=client_credential&appid=" + appId + "&secret=" + appSecret));
	}
	
	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public int getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(int expiresIn) {
		this.expiresIn = expiresIn;
	}

	@Override
	public String toString(){
		return "accessToken : " + accessToken + " ,expiresIn : " + expiresIn;
	}
	
	@Override
	public JsonMap toJsonMap(int flag) {
		return null;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		this.accessToken = jsonMap.getString("access_token");
		this.expiresIn = jsonMap.getInt("expires_in");
	}
	
	public static Jsonable.Creator<Token> JSON_CREATOR = new Jsonable.Creator<Token>() {
		@Override
		public Token newInstance() {
			return new Token();
		}
	};
	
	public static void main(String[] args) throws IOException{
		String appId = "wxa7b4687daedda86f";
		String appSecret = "4322fbf1c4bba4cccd90424e2e16306b";
		System.out.println(newInstance(appId, appSecret));
	}
}
