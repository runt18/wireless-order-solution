package com.wireless.pojo.token;

import com.alibaba.fastjson.JSON;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.restaurantMgr.Restaurant;

public class Token implements Jsonable{
	
	public static class InsertBuilder{
		private final int restaurantId;
		
		public InsertBuilder(int restaurantId){
			this.restaurantId = restaurantId;
		}
		
		public InsertBuilder(Restaurant restaurant){
			this.restaurantId = restaurant.getId();
		}
		
		public int getRestaurantId(){
			return this.restaurantId;
		}
		
	}
	
	public static class GenerateBuilder{
		private final String account;
		private final int code;
		
		public GenerateBuilder(String account, int code){
			this.account = account;
			this.code = code;
		}
		
		public String getAccount(){
			return this.account;
		}
		
		public int getCode(){
			return this.code;
		}
		
	}
	
	public static class FailedGenerateBuilder{
		private final String account;
		private final String failedEncryptedToken;
		private final int code;
		
		public FailedGenerateBuilder(String failedEncryptedToken, String account, int code){
			this.account = account;
			this.failedEncryptedToken = failedEncryptedToken;
			this.code = code;
		}
		
		public String getAccount(){
			return this.account;
		}
		
		public String getFailedEncryptedToken(){
			return this.failedEncryptedToken;
		}
		
		public GenerateBuilder getTokenBuilder(){
			return new GenerateBuilder(account, code);
		}
	}
	
	public static class VerifyBuilder{
		private final String account;
		private final String encryptedToken;
		
		public VerifyBuilder(String account, String encryptedToken){
			this.account = account;
			this.encryptedToken = encryptedToken;
		}
		
		public String getAccount(){
			return this.account;
		}
		
		public String getEncryptedToken(){
			return this.encryptedToken;
		}
	}
	
	public static class UpdateBuilder{
		private final int id;
		private int restaurantId;
		private Status status;
		private int code = -1;
		
		public UpdateBuilder(int id){
			this.id = id;
		}
		
		public UpdateBuilder setRestaurant(int restaurantId){
			this.restaurantId = restaurantId;
			return this;
		}
		
		public UpdateBuilder setRestaurant(Restaurant restaurant){
			this.restaurantId = restaurant.getId();
			return this;
		}
		
		public boolean isRestaurantChanged(){
			return restaurantId != 0;
		}
		
		public UpdateBuilder setStatus(Status status){
			this.status = status;
			return this;
		}
		
		public boolean isStatusChanged(){
			return this.status != null;
		}
		
		public UpdateBuilder setCode(int code){
			this.code = code;
			return this;
		}
		
		public boolean isCodeChanged(){
			return this.code >= 0;
		}
		
		public Token build(){
			return new Token(this);
		}
	}
	
	public static enum Status{
		DYN_CODE(1, "已生成动态验证码"),
		TOKEN(2, "已生成Token");
		
		private final int val;
		private final String desc;
		
		Status(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public int getVal(){
			return this.val;
		}
		
		public static Status valueOf(int val){
			for(Status status : values()){
				if(status.val == val){
					return status;
				}
			}
			throw new IllegalArgumentException("The status(val = " + val + ") is invalid.");
		}
		
		@Override
		public String toString(){
			return this.desc;
		}
	}
	
	private int id;
	private Restaurant restaurant;
	private long birthDate;
	private long lastModified;
	private int code;
	private Status status;
	
	private Token(UpdateBuilder builder){
		setId(builder.id);
		setRestaurant(new Restaurant(builder.restaurantId));
		setStatus(builder.status);
		setCode(builder.code);
	}
	
	public Token(int id){
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public Restaurant getRestaurant() {
		return restaurant;
	}
	
	public void setRestaurant(Restaurant restaurant) {
		this.restaurant = restaurant;
	}
	
	public long getBirthDate() {
		return birthDate;
	}
	
	public void setBirthDate(long birthDate) {
		this.birthDate = birthDate;
	}
	
	public long getLastModified() {
		return lastModified;
	}
	
	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}
	
	public int getCode(){
		return this.code;
	}
	
	public void setCode(int code){
		this.code = code;
	}
	
	public Status getStatus(){
		return this.status;
	}
	
	public void setStatus(Status status){
		this.status = status;
	}
	
	public boolean isCodeExpired(){
		return System.currentTimeMillis() - this.lastModified >= 10 * 60 * 1000;
	}
	
	public String encrypt() throws BusinessException{
		RSACoder coder = new RSACoder(restaurant.getPublicKey(), restaurant.getPrivateKey());
		return coder.encryptBASE64(coder.encryptByPublicKey(JSON.toJSONString(this.toJsonMap(0)).getBytes()));
		
	}
	
	public void decrypt(String encryptedToken) throws BusinessException{
		RSACoder coder = new RSACoder(restaurant.getPublicKey(), restaurant.getPrivateKey());
		Token decryptedToken = JObject.parse(Token.JSON_CREATOR, 0, new String(coder.decryptByPrivateKey(coder.decryptBASE64(encryptedToken))));
		copyFrom(decryptedToken);
	}

	private void copyFrom(Token src){
		if(src != null){
			this.id = src.id;
			this.restaurant = src.restaurant;
			this.birthDate = src.birthDate;
			this.lastModified = src.lastModified;
		}
	}
	
	@Override
	public int hashCode(){
		return id * 31 + 17;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Token)){
			return false;
		}else{
			return id == ((Token)obj).id;
		}
	}
	
	@Override
	public String toString(){
		return JSON.toJSONString(this);
	}

	public static enum Key4Json{
		TOKEN_ID("tid", "token_id"),
		ACCOUNT("rid", "restaurant_id"),
		LAST_MODIFIED("lm", "last_modified");
		public final String val;
		@SuppressWarnings("unused")
		private final String desc;
		
		Key4Json(String val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		@Override
		public String toString(){
			return this.val;
		}
	}
	
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt(Key4Json.TOKEN_ID.val, this.id);
		jm.putInt(Key4Json.ACCOUNT.val, this.restaurant.getId());
		jm.putLong(Key4Json.LAST_MODIFIED.val, this.lastModified);
		jm.putInt("code", this.getCode());
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		setId(jsonMap.getInt(Key4Json.TOKEN_ID.val));
		setRestaurant(new Restaurant(jsonMap.getInt(Key4Json.ACCOUNT.val)));
		setLastModified(jsonMap.getLong(Key4Json.LAST_MODIFIED.val));
	}
	
	public static Jsonable.Creator<Token> JSON_CREATOR = new Jsonable.Creator<Token>() {
		@Override
		public Token newInstance() {
			return new Token(0);
		}
	};
}
