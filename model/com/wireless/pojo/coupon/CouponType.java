package com.wireless.pojo.coupon;

import com.wireless.pojo.util.DateUtil;

public class CouponType {

	public static class InsertBuilder{
		private final String name;
		private final float price;
		private long expired;
		
		public InsertBuilder(String name, float price){
			this.name = name;
			this.price = price;
		}
		
		public InsertBuilder setExpired(String expiredDate){
			this.expired = DateUtil.parseDate(expiredDate);
			return this;
		}
		
		public InsertBuilder setExpired(long expired){
			if(expired >= 0){
				this.expired = expired;
			}
			return this;
		}
		
		public CouponType build(){
			return new CouponType(this);
		}
	}
	
	public static class UpdateBuilder{
		private final int id;
		private final String name;
		private long expired = Integer.MIN_VALUE;
		
		public UpdateBuilder(int id, String name){
			this.id = id;
			this.name = name;
		}
		
		public boolean isNameChanged(){
			return name != null;
		}
		
		public UpdateBuilder setExpired(String expiredDate){
			this.expired = DateUtil.parseDate(expiredDate);
			return this;
		}
		
		public UpdateBuilder setExpired(long expired){
			if(expired > 0){
				this.expired = expired;
			}
			return this;
		}
		
		public boolean isExpiredChanged(){
			return this.expired >= 0;
		}
		
		public CouponType build(){
			return new CouponType(this);
		}
	}
	
	private int id;
	private int restaurantId;
	private String name;
	private float price;
	private long expired;
	
	private CouponType(UpdateBuilder builder){
		setId(builder.id);
		setName(builder.name);
		setExpired(builder.expired);
	}
	
	private CouponType(InsertBuilder builder){
		setName(builder.name);
		setPrice(builder.price);
		setExpired(builder.expired);
	}
	
	public CouponType(int id){
		this.id = id;
	}
	
	public void copyFrom(CouponType src){
		if(src != null && src != this){
			setId(src.getId());
			setRestaurantId(src.getRestaurantId());
			setName(src.getName());
			setPrice(src.getPrice());
			setExpired(src.getExpired());
		}
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
		if(name == null){
			return "";
		}
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public float getPrice() {
		return price;
	}
	
	public void setPrice(float price) {
		this.price = price;
	}
	
	public long getExpired() {
		return expired;
	}
	
	public void setExpired(long expired) {
		if(expired >= 0){
			this.expired = expired;
		}
	}

	public boolean isExpired(){
		if(expired == 0){
			return false;
		}else{
			return System.currentTimeMillis() > expired;
		}
	}
	
	@Override
	public int hashCode(){
		return id * 17 + 31;
	}
	
	@Override 
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof CouponType)){
			return false;
		}else{
			return id == ((CouponType)obj).id;
		}
	}
	
	@Override
	public String toString(){
		return "coupon type(id = " + getId() + ",name = " + getName() + ")";
	}
	
}
