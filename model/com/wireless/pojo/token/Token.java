package com.wireless.pojo.token;

import com.alibaba.fastjson.JSON;
import com.wireless.exception.BusinessException;
import com.wireless.json.JObject;
import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.restaurantMgr.Restaurant;

public class Token implements Jsonable{
	
	public static class InsertBuilder{
		private final String account;
		private final byte[] encryptedCreateToken;
		private final long lastModified;
		
		public InsertBuilder(String account, byte[] createToken, long lastModified){
			this.account = account;
			this.encryptedCreateToken = createToken;
			this.lastModified = lastModified;
		}

		public String getAccount(){
			return this.account;
		}
		
		public byte[] getEncryptedCreateToken(){
			return this.encryptedCreateToken;
		}
		
		public long getLastModified(){
			return this.lastModified;
		}
		
	}
	
	public static class FailedInsertBuilder{
		private final String account;
		private final byte[] failedEncryptedToken;
		private final byte[] createdEncryptedToken;
		private final long lastModified;
		
		public FailedInsertBuilder(byte[] failedEncryptedToken, String account, byte[] createdEncryptedToken, long lastModified){
			this.account = account;
			this.failedEncryptedToken = failedEncryptedToken;
			this.createdEncryptedToken = createdEncryptedToken;
			this.lastModified = lastModified;
		}
		
		public String getAccount(){
			return this.account;
		}
		
		public byte[] getFailedEncryptedToken(){
			return this.failedEncryptedToken;
		}
		
		public InsertBuilder getTokenBuilder(){
			return new InsertBuilder(account, createdEncryptedToken, lastModified);
		}
	}
	
	public static class VerifyBuilder{
		private final String account;
		private final byte[] encryptedToken;
		
		public VerifyBuilder(String account, byte[] encryptedToken){
			this.account = account;
			this.encryptedToken = encryptedToken;
		}
		
		public String getAccount(){
			return this.account;
		}
		
		public byte[] getEncryptedToken(){
			return this.encryptedToken;
		}
	}
	
	public static class UpdateBuilder{
		private final int id;
		private int restaurantId;
		
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
		
		public Token build(){
			return new Token(this);
		}
	}
	
	private int id;
	private Restaurant restaurant;
	private long birthDate;
	private long lastModified;
	
	private Token(UpdateBuilder builder){
		setId(builder.id);
		setRestaurant(new Restaurant(builder.restaurantId));
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
	
	public byte[] encrypt() throws BusinessException{
		RSACoder coder = new RSACoder(restaurant.getPublicKey(), restaurant.getPrivateKey());
		return coder.encryptByPublicKey(JSON.toJSONString(this.toJsonMap(0)).getBytes());
	}
	
	public void decrypt(byte[] encryptedToken) throws BusinessException{
		RSACoder coder = new RSACoder(restaurant.getPublicKey(), restaurant.getPrivateKey());
		Token decryptedToken = JObject.parse(Token.JSON_CREATOR, 0, new String(coder.decryptByPrivateKey(encryptedToken)));
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
