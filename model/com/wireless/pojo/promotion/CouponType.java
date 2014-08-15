package com.wireless.pojo.promotion;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.util.DateUtil;

public class CouponType implements Jsonable{

	public static class InsertBuilder{
		private final String name;
		private final float price;
		private long expired;
		private String comment;
		private String image;
		
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
		
		public InsertBuilder setComment(String comment){
			this.comment = comment;
			return this;
		}
		
		public InsertBuilder setImage(String image){
			this.image = image;
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
		private String comment;
		private String image;
		
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
		
		public boolean isCommentChanged(){
			return this.comment != null;
		}
		
		public UpdateBuilder setComment(String comment){
			this.comment = comment;
			return this;
		}
		
		public UpdateBuilder setImage(String image){
			this.image = image;
			return this;
		}
		
		public boolean isImageChanged(){
			return this.image != null;
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
	private String comment;
	private String image;
	
	private CouponType(UpdateBuilder builder){
		setId(builder.id);
		setName(builder.name);
		setExpired(builder.expired);
		setComment(builder.comment);
		setImage(builder.image);
	}
	
	private CouponType(InsertBuilder builder){
		setName(builder.name);
		setPrice(builder.price);
		setExpired(builder.expired);
		setComment(builder.comment);
		setImage(builder.image);
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
			setComment(src.getComment());
			setImage(src.getImage());
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
	
	public String getExpiredFormat() {
		return DateUtil.formatToDate(expired);
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
	
	public void setComment(String comment){
		this.comment = comment;
	}
	
	public String getComment(){
		if(this.comment == null){
			return "";
		}
		return this.comment;
	}
	
	public String getImage(){
		if(image != null){
			return image;
		}else{
			return "";
		}
	}
	
	public void setImage(String image){
		this.image = image;
	}
	
	public boolean hasImage(){
		return image != null ? image.length() != 0 : false;
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

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.id);
		jm.putInt("restaurantId", this.restaurantId);
		jm.putString("name", this.name);
		jm.putFloat("price", this.price);
		jm.putFloat("expired", this.expired);
		jm.putString("expiredFormat", DateUtil.formatToDate(this.expired));
		jm.putString("image", this.image);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
}
