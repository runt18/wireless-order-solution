package com.wireless.pojo.inventoryMgr;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class MaterialCate implements Jsonable {
	
	public static enum Type{
		GOOD(1, "商品"),
		MATERIAL(2, "原料");
		
		private int value;
		private String text;
		
		Type(int value, String text){
			this.value = value;
			this.text = text;
		}
		public int getValue() {
			return value;
		}
		public String getText() {
			return text;
		}
		
		public static Type valueOf(int value){
			for(Type type : values()){
				if(type.getValue() == value){
					return type;
				}
			}
			throw new IllegalArgumentException("The type value(val = " + value + ") passed is invalid.");
		}
	}
	
	
	public static class InsertBuilder {
		private String name;
		private Type type;
		
		public InsertBuilder(){}
		
		public InsertBuilder setName(String name) {
			this.name = name;
			return this;
		}
		
		public InsertBuilder setType(Type type) {
			this.type = type;
			return this;
		}
		
		public MaterialCate build() {
			return new MaterialCate(this);
		}
	}
	
	
	
	public static class UpdateBuilder {
		private final int id;
		private String name;
		private Type type;
		
		public UpdateBuilder(int id){
			this.id = id;
		}
		
		
		public UpdateBuilder setName(String name) {
			this.name = name;
			return this;
		}
		
		public UpdateBuilder setType(Type type) {
			this.type = type;
			return this;
		}
		
		public boolean isNameChanged() {
			return this.name != null;
		}
		
		public boolean isTypeChanged(){
			return this.type != null;
		}
		
		public MaterialCate build() {
			return new MaterialCate(this);
		}
	}
	
	
	
	private int id;
	private int restaurantId;
	private String name;
	private Type type;
	
	public MaterialCate(int id){
		this.id = id;
	}
	
	public MaterialCate(InsertBuilder builder) {
		this.name = builder.name;
		this.type = builder.type;
	}
	
	public MaterialCate(UpdateBuilder builder) {
		this.id = builder.id;
		if(builder.isNameChanged()){
			this.name = builder.name;
		}
		if(builder.isTypeChanged()){
			this.type = builder.type;
		}
	}
	
//	public MaterialCate(int id){
//		this.id = id;
//	}
	
	
	
//	public MaterialCate(int restaurantId, String name){
//		this(0, restaurantId, name);
//	}
//	
//	public MaterialCate(int id, int restaurantId, String name){
//		this(id, restaurantId, name, Type.MATERIAL);
//	}
//	
//	public MaterialCate(int id, int restaurantId, String name, Type type){
//		this.id = id;
//		this.restaurantId = restaurantId;
//		this.name = name;
//		this.type = type;
//	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	public int getRestaurantId() {
		return restaurantId;
	}
	public void setRestaurantId(int restaurantId) {
		this.restaurantId = restaurantId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return "cateId=" + this.getId() + ", cateName=" + this.getName();
	}
	
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.id);
		jm.putInt("rid", this.restaurantId);
		jm.putString("name", this.name);
		if(this.type != null){
			jm.putInt("typeValue", this.type.getValue());
			jm.putString("typeText", this.type.getText());			
		}
		
		return jm;
	}
	
	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}

	@Override
	public int hashCode(){
		return id * 31 + 17;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof MaterialCate)){
			return false;
		}else{
			return id == ((MaterialCate)obj).id && restaurantId == ((MaterialCate)obj).restaurantId;
		}
	}

}
