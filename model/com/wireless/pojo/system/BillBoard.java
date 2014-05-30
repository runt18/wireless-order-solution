package com.wireless.pojo.system;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.restaurantMgr.Restaurant;
import com.wireless.pojo.util.DateUtil;

public class BillBoard implements Jsonable {
	
	public enum Type{
		SYSTEM(1, "系统公告"),
		RESTAURANT(2, "餐厅通知"),
		WX_INFO(3, "促销信息");
		
		Type(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		private int val;
		private String desc;
		public int getVal(){
			return val;
		}
		public String getDesc(){
			return desc;
		}
		public static Type valueOf(int val){
			for(Type temp : values()){
				if(temp.val == val){
					return temp;
				}
			}
			throw new IllegalArgumentException("The val(" + val + ") is invalid.");
		}
	}
	
	public static class Builder{
		public Builder(){
			data = new BillBoard();
		}
		protected BillBoard data;
		public Builder setTitle(String title){
			data.setTitle(title);
			return this;
		}
		public Builder setDesc(String desc){
			data.setDesc(desc);
			return this;
		}
		public Builder setType(Type type){
			data.setType(type);
			return this;
		}
		public Builder setType(int type) {
			data.setType(type);
			return this;
		}
		public Builder setRestaurantId(int id) {
			data.setRestaurant(new Restaurant(id));
			return this;
		}
		public Builder setExpired(long expired) {
			data.setExpired(expired);
			return this;
		}
		public BillBoard build(){
			return data.clone();
		}
	}
	
	public static class InsertBuilder extends Builder{
		
	}
	public static class UpdateBuilder extends Builder{
		public UpdateBuilder setId(int id){
			data.setId(id);
			return this;
		}
	}
	
	
	public BillBoard(){}
	
	private int id;
	private String title;
	private String desc;
	private long created;
	private long expired;
	private Type type;
	private Restaurant restaurant;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public long getCreated() {
		return created;
	}
	public void setCreated(long created) {
		this.created = created;
	}
	public long getExpired() {
		return expired;
	}
	public void setExpired(long expired) {
		this.expired = expired;
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
	public Restaurant getRestaurant() {
		return restaurant;
	}
	public void setRestaurant(Restaurant restaurant) {
		this.restaurant = restaurant;
	}
	
	@Override
	protected BillBoard clone() {
		BillBoard clone = new BillBoard();
		clone.id = this.id;
		clone.title = this.title;
		clone.desc = this.desc;
		clone.created = this.created;
		clone.expired = this.expired;
		clone.type = this.type;
		clone.restaurant = this.restaurant;
		return clone;
	}
	@Override
	public int hashCode() {
		return id * 31 + 17;
	}
	@Override
	public String toString() {
		return "id : " + id + ", title: " + title;
	}
	
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", id);
		jm.putString("title", title);
		jm.putString("desc", desc);
		jm.putLong("created", created);
		jm.putString("createdFormat", DateUtil.format(created));
		jm.putLong("expired", expired);
		jm.putString("expiredFormat", DateUtil.format(expired));
		jm.putInt("typeVal", type.getVal());
		jm.putString("typeDesc", type.getDesc());
		jm.putJsonable("restaurant", restaurant, 0);
		
		return jm;
	}
	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		// TODO Auto-generated method stub
		
	}
}
