package org.marker.weixin.api;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import com.alibaba.fastjson.JSON;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class QRCode implements Jsonable{

	static enum ActionName{
		TEMP("QR_SCENE", "临时"),
		LIMIT("QR_LIMIT_SCENE", "永久");
		
		private final String val;
		@SuppressWarnings("unused")
		private final String desc;
		
		private ActionName(String val, String desc) {
			this.val = val;
			this.desc = desc;
		}
		
		@Override
		public String toString(){
			return val;
		}
	}
	
	private int expired = 900;
	private int sceneId;
	private String ticket;
	private ActionName actionName = ActionName.TEMP;
	
	public QRCode setExpired(int expired){
		this.expired = expired;
		return this;
	}

	public QRCode setSceneId(String sceneId){
		this.sceneId = Integer.parseInt(sceneId);
		return this;
	}
	
	public QRCode setSceneId(int sceneId){
		this.sceneId = sceneId;
		return this;
	}
	
	public QRCode setActionName(ActionName actionName){
		this.actionName = actionName;
		return this;
	}
	
	public static enum Key4Json{
		EXPIRED_SECONDS("expire_seconds"),
		ACTION_NAME("action_name"),
		ACTION_INFO("action_info"),
		SCENE_ID("scene_id"),
		SCENE_STR("scene_str"),
		TICKET("ticket");
		private final String key;
		Key4Json(String key){
			this.key = key;
		}
	}
	
	public String createTicket(Token token) throws ClientProtocolException, IOException{
		String response = BaseAPI.doPost(BaseAPI.BASE_URI + "/cgi-bin/qrcode/create?access_token=" + token.getAccessToken(), JSON.toJSONString(toJsonMap(QR_CODE_JSONABLE_4_TICKET)));
		Status status = JObject.parse(Status.JSON_CREATOR, 0, response);
		if(status.getCode() != 0){
			throw new IOException(status.getDesc());
		}else{
			return JObject.parse(JSON_CREATOR, QR_CODE_JSONABLE_4_TICKET, response).ticket;
		}
	}

	public String createUrl(Token token) throws ClientProtocolException, IOException{
		return BaseAPI.QRCODE_DOWNLOAD_URI + "/cgi-bin/showqrcode?ticket=" + createTicket(token);
	}
	
	public final static int QR_CODE_JSONABLE_4_TICKET = 0;
	public final static int QR_CODE_JSONABLE_4_IMG = 1;
	
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		if(flag == QR_CODE_JSONABLE_4_TICKET){
			jm.putInt(Key4Json.EXPIRED_SECONDS.key, expired);
			jm.putString(Key4Json.ACTION_NAME.key, actionName.val);
			jm.putJsonable(Key4Json.ACTION_INFO.key, new Jsonable(){
				@Override
				public JsonMap toJsonMap(int flag) {
					JsonMap jm = new JsonMap();
					jm.putJsonable("scene", new Jsonable(){
						@Override
						public JsonMap toJsonMap(int flag) {
							JsonMap jm = new JsonMap();
							jm.putInt(Key4Json.SCENE_ID.key, sceneId);
							return jm;
						}

						@Override
						public void fromJsonMap(JsonMap jsonMap, int flag) {}
						
					}, 0);
					return jm;
				}

				@Override
				public void fromJsonMap(JsonMap jsonMap, int flag) {
					
				}
				
			}, 0);
		}
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		if(flag == QR_CODE_JSONABLE_4_TICKET){
			expired = jsonMap.getInt(Key4Json.EXPIRED_SECONDS.key);
			ticket = jsonMap.getString(Key4Json.TICKET.key);
		}
	}
	
	public static Jsonable.Creator<QRCode> JSON_CREATOR = new Jsonable.Creator<QRCode>() {
		@Override
		public QRCode newInstance() {
			return new QRCode();
		}
	};
	
	public static void main(String[] args) throws IOException{
		String appId = "wx6fde9cd2c7fc791e";
		String appSecret = "0a360a43b80e3a334e5e52da706a3134";
		System.out.println(new QRCode().setSceneId(98).createUrl(Token.newInstance(appId, appSecret)));
	}
}
