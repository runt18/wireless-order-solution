package org.marker.weixin.api;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import com.alibaba.fastjson.JSONObject;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.util.DateUtil;

public class User implements Jsonable{
	private boolean subscribe;
	private String openId;
	private String nickName;
	private String language;
	private String city;
	private String province;
	private String country;
	private String headImgUrl;
	private long subscribeTime;
	private String unionId;
	private String remark;
	private int sex;
	
	public static User newInstance(Token token, String openId) throws ClientProtocolException, IOException{
		return JObject.parse(JSON_CREATOR, 0, BaseAPI.doGet("https://api.weixin.qq.com/cgi-bin/user/info?access_token=" + token.getAccessToken() + "&openid=" + openId + "&lang=zh_CN"));
	}
	
	public Status remark(Token token, String content) throws ClientProtocolException, IOException{
		setRemark(content);
		return BaseAPI.doPost("https://api.weixin.qq.com/cgi-bin/user/info/updateremark?access_token=" + token.getAccessToken(), new Jsonable(){

			@Override
			public JsonMap toJsonMap(int flag) {
				JsonMap jm = new JsonMap();
				jm.putString(Key4Json.OPEN_ID.key, openId);
				jm.putString(Key4Json.REMARK.key, remark);
				return jm;
			}

			@Override
			public void fromJsonMap(JsonMap jm, int flag) {
				
			}
			
		}, 0);
	}
	
	public User(String openId){
		this.openId = openId;
		
	}
	
	private User(){
		
	}
	
	public void setSubscribe(boolean subscribe){
		this.subscribe = subscribe;
	}
	
	public boolean isSubscribed(){
		return subscribe;
	}
	
	public String getOpenId() {
		return openId;
	}
	
	public void setOpenId(String openId) {
		this.openId = openId;
	}
	
	public String getNickName() {
		if(nickName == null){
			return "";
		}
		return nickName;
	}
	
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	
	public String getLanguage() {
		if(language == null){
			return "";
		}
		return language;
	}
	
	public void setLanguage(String language) {
		this.language = language;
	}
	
	public String getCity() {
		if(city == null){
			return "";
		}
		return city;
	}
	
	public void setCity(String city) {
		this.city = city;
	}
	
	public String getProvince() {
		if(province == null){
			return "";
		}
		return province;
	}
	
	public void setProvince(String province) {
		this.province = province;
	}
	
	public String getCountry() {
		if(country == null){
			return "";
		}
		return country;
	}
	
	public void setCountry(String country) {
		this.country = country;
	}
	
	public String getHeadImgUrl() {
		if(headImgUrl == null){
			return "";
		}
		return headImgUrl;
	}
	
	public void setHeadImgUrl(String headImgUrl) {
		this.headImgUrl = headImgUrl;
	}
	
	public long getSubscribeTime() {
		return subscribeTime;
	}
	
	public void setSubscribeTime(long subscribeTime) {
		this.subscribeTime = subscribeTime;
	}
	
	public String getUnionId() {
		if(unionId == null){
			return "";
		}
		return unionId;
	}
	
	public void setUnionId(String unionId) {
		this.unionId = unionId;
	}

	public void setRemark(String remark){
		this.remark = remark;
	}
	
	public String getRemark(){
		if(remark == null){
			return "";
		}
		return this.remark;
	}
	
	public String getSex(){
		if(this.sex == 1){
			return "男";
		}else if(this.sex == 2){
			return "女";
		}else{
			return "未知";
		}
	}
	
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putString(Key4Json.OPEN_ID.key, openId);
		jm.putString(Key4Json.REMARK.key, remark);
		jm.putBoolean(Key4Json.SUBSCRIBE.key, this.subscribe);
		jm.putString(Key4Json.SUBSCRIBE_TIME.key, DateUtil.format(this.subscribeTime, DateUtil.Pattern.TIME));
		jm.putString(Key4Json.NICK_NAME.key, this.nickName);
		jm.putString(Key4Json.LANGUAGE.key, this.language);
		jm.putString(Key4Json.CITY.key, this.city);
		jm.putString(Key4Json.PROVINCE.key, this.province);
		jm.putString(Key4Json.HEAD_IMG_URL.key, this.headImgUrl);
		jm.putString(Key4Json.SEX.key, this.getSex());
		return jm;
	}

	public static enum Key4Json{
		SUBSCRIBE("subscribe"),
		OPEN_ID("openid"),
		NICK_NAME("nickname"),
		LANGUAGE("language"),
		SEX("sex"),
		CITY("city"),
		PROVINCE("province"),
		HEAD_IMG_URL("headimgurl"),
		SUBSCRIBE_TIME("subscribe_time"),
		UNION_ID("unionid"),
		REMARK("remark");
		
		private final String key;
		Key4Json(String key){
			this.key = key;
		}
		
		@Override
		public String toString(){
			return this.key;
		}
	}
	
	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		setSubscribe(jsonMap.getBoolean(Key4Json.SUBSCRIBE.key));
		setOpenId(jsonMap.getString(Key4Json.OPEN_ID.key));
		setNickName(jsonMap.getString(Key4Json.NICK_NAME.key));
		setLanguage(jsonMap.getString(Key4Json.LANGUAGE.key));
		setCity(jsonMap.getString(Key4Json.CITY.key));
		setProvince(jsonMap.getString(Key4Json.PROVINCE.key));
		setHeadImgUrl(jsonMap.getString(Key4Json.HEAD_IMG_URL.key));
		setSubscribeTime(jsonMap.getLong(Key4Json.SUBSCRIBE_TIME.key));
		setUnionId(jsonMap.getString(Key4Json.UNION_ID.key));
		this.sex = jsonMap.getInt(Key4Json.SEX.key);
	}
	
	public static Jsonable.Creator<User> JSON_CREATOR = new Jsonable.Creator<User>() {
		@Override
		public User newInstance() {
			return new User();
		}
	};
	
	@Override
	public String toString(){
		return "nick_name:" + getNickName() + ", open_id:" + getOpenId();
	}
	
	public static void main(String[] args) throws IOException{
		String appId = "wx49b3278a8728ff76";
		String appSecret = "0ba130d87e14a1a37e20c78a2b0ee3ba";
		String openId = "oM02TjtmLtadFjiGtlUuxTFjJhno";
		Token token = Token.newInstance(appId, appSecret);
		System.out.println(JSONObject.toJSON(User.newInstance(token, openId).toJsonMap(0)));
		System.out.println(new User(openId).remark(token, "小肥蛇"));
	}
}
