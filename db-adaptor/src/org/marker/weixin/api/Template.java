package org.marker.weixin.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ClientProtocolException;

import com.alibaba.fastjson.JSONObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class Template {

	public static class Keyword implements Jsonable{
		private final String key;
		private final String val;
		private final String color;
		
		public Keyword(String key, String val){
			this.key = key;
			this.val = val;
			this.color = "#173177";
		}

		@Override
		public JsonMap toJsonMap(int flag) {
			JsonMap jm = new JsonMap();
			jm.putJsonable(this.key, new Jsonable(){

				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putString("value", Keyword.this.val);
					jm.putString("color", Keyword.this.color);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jm, int flag) {
					
				}
				
			}, 0);
			return jm;
		}

		@Override
		public void fromJsonMap(JsonMap jm, int flag) {
		}
	}
	
	public static class Builder implements Jsonable{
		private String toUser;
		private String templateId;
		private String url;
		private final List<Keyword> keywords = new ArrayList<Keyword>();
		
		public Builder setToUser(String toUser){
			this.toUser = toUser;
			return this;
		}
		
		public Builder setTemplateId(String templateId){
			this.templateId = templateId;
			return this;
		}
		
		public Builder setUrl(String url){
			this.url = url;
			return this;
		}
		
		public Builder addKeyword(Keyword keyword){
			this.keywords.add(keyword);
			return this;
		}

		@Override
		public JsonMap toJsonMap(int flag) {
			JsonMap jm = new JsonMap();
			jm.putString("touser", this.toUser);
			jm.putString("template_id", this.templateId);
			jm.putString("url", this.url);
			jm.putJsonable("data", new Jsonable(){
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					for(Keyword keyword : Builder.this.keywords){
						jm.putJsonable(keyword, 0);
					}
					return jm;
				}
				@Override
				public void fromJsonMap(JsonMap jm, int flag) {
				}
				
			}, 0);
			return jm;
		}

		@Override
		public void fromJsonMap(JsonMap jm, int flag) {
			
		}
		
		@Override
		public String toString(){
			return JSONObject.toJSONString(this.toJsonMap(0));
		}
	}

	public static Status send(Token token, Template.Builder builder) throws ClientProtocolException, IOException{
		return BaseAPI.doPost(BaseAPI.BASE_URI + "/cgi-bin/message/template/send?access_token=" + token.getAccessToken(), builder);
	}
	
	public static void main(String[] args) throws ClientProtocolException, IOException{
		String appId = "wx6fde9cd2c7fc791e";
		String appSecret = "0a360a43b80e3a334e5e52da706a3134";
		
		Token token = Token.newInstance(appId, appSecret);
		
		Template.Builder builder;
		System.out.println(builder = new Template.Builder().setToUser("odgTwt4tqd_mu-q9EY02rqHrp_M0")
											     .setTemplateId("UdyaL-jQJjC5aUh8A3VdeOrzkm2DQDZpUvny8kZ1kZ0")
											     .addKeyword(new Keyword("first", "你好，还有事情要做")).addKeyword(new Keyword("work", "梨园完蛋了")));
		
		System.out.println(Template.send(token, builder));
	}
}
