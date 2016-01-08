package com.wireless.pojo.weixin.action;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class WxKeyword implements Jsonable{

	public static class InsertBuilder{
		private final Type type;
		private final String keyword;
		
		public InsertBuilder(String keyword, Type type){
			this.type = type;
			this.keyword = keyword;
		}
		
		public WxKeyword build(){
			return new WxKeyword(this);
		}
	}
	
	public static class UpdateBuilder{
		private final int id;
		private String keyword;
		private int actionId;
		
		public UpdateBuilder(int id){
			this.id = id;
		}
		
		public UpdateBuilder setKeyword(String keyword){
			this.keyword = keyword;
			return this;
		}
		
		public boolean isKeywordChanged(){
			return this.keyword != null;
		}
		
		public UpdateBuilder setAction(int actionId){
			this.actionId = actionId;
			return this;
		}
		
		public UpdateBuilder setAction(WxMenuAction action){
			this.actionId = action.getId();
			return this;
		}
		
		public boolean isActionChanged(){
			return this.actionId != 0;
		}
		
		public WxKeyword build(){
			return new WxKeyword(this);
		}
	}
	
	public static enum Type{
		NORMAL(1, "普通回复"),
		EXCEPTION(2, "例外回复");
		
		Type(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		private final int val;
		private final String desc;
		
		public static Type valueOf(int val){
			for(Type type : values()){
				if(type.val == val){
					return type;
				}
			}
			throw new IllegalArgumentException("The(val = " + val + ") is invalid.");
		}
		
		public int getVal(){
			return this.val;
		}
		
		@Override
		public String toString(){
			return this.desc;
		}
	}
	
	private int id;
	private int restaurantId;
	private String keyword;
	private int actionId;
	private Type type;
	
	private WxKeyword(UpdateBuilder builder){
		setId(builder.id);
		setActionId(builder.actionId);
		setKeyword(builder.keyword);
	}
	
	private WxKeyword(InsertBuilder builder){
		setType(builder.type);
		setKeyword(builder.keyword);
	}
	
	public WxKeyword(int id){
		this.id = id;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setRestaurantId(int restaurantId){
		this.restaurantId = restaurantId;
	}
	
	public int getRestaurantId(){
		return this.restaurantId;
	}
	public String getKeyword() {
		if(keyword == null){
			return "";
		}
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public int getActionId() {
		return actionId;
	}

	public void setActionId(int actionId) {
		this.actionId = actionId;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	@Override
	public int hashCode(){
		return this.id * 31 + 17;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof WxKeyword)){
			return false;
		}else{
			return this.id == ((WxKeyword)obj).id;
		}
	}
	
	@Override
	public String toString(){
		return "keyword : " + keyword;
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.id);
		jm.putString("keyword", this.keyword);
		jm.putInt("actionId", this.actionId);
		jm.putString("typeText", this.type.desc);
		jm.putInt("typeVal", this.type.val);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jm, int flag) {
		
	}
	
}
