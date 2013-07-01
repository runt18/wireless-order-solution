package com.wireless.pojo.inventoryMgr;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;

public class MaterialCate implements Jsonable {
	
	public enum Type{
		/**
		 * value : 1
		 * text : 商品
		 */
		GOOD(1, "商品"),
		/**
		 * value : 2
		 * text : 原料
		 */
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
			for(Type temp : values()){
				if(temp.getValue() == value){
					return temp;
				}
			}
			throw new IllegalArgumentException("The type value(val = " + value + ") passed is invalid.");
		}
	}
	
	private int id;
	private int restaurantId;
	private String name;
	private Type type;
	
	public MaterialCate(){}
	
	public MaterialCate(int restaurantId, String name){
		this(0, restaurantId, name);
	}
	
	public MaterialCate(int id, int restaurantId, String name){
		this(id, restaurantId, name, Type.MATERIAL);
	}
	
	public MaterialCate(int id, int restaurantId, String name, Type type){
		this.id = id;
		this.restaurantId = restaurantId;
		this.name = name;
		this.type = type;
	}
	
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
	public void setType(int type) {
		this.type = Type.valueOf(type);
	}
	
	@Override
	public String toString() {
		return "cateId=" + this.getId() + ", cateName=" + this.getName();
	}
	
	@Override
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new LinkedHashMap<String, Object>();
		jm.put("id", this.id);
		jm.put("rid", this.restaurantId);
		jm.put("name", this.name);
		if(this.type != null){
			jm.put("typeValue", this.type.getValue());
			jm.put("typeText", this.type.getText());			
		}
		
		return Collections.unmodifiableMap(jm);
	}
	
	@Override
	public List<Object> toJsonList(int flag) {
		return null;
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

	@Override
	public void fromJsonMap(Map<String, Object> map) {
		
	}
}
