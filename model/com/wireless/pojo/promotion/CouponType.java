package com.wireless.pojo.promotion;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.oss.OssImage;
import com.wireless.pojo.util.DateUtil;

public class CouponType implements Jsonable{

	public static class InsertBuilder{
		private final String name;
		private final float price;
		private final long expired;
		private String comment;
		
		private OssImage ossImage;
		
		public InsertBuilder(String name, float price, String expiredDate){
			this.name = name;
			this.price = price;
			this.expired = DateUtil.parseDate(expiredDate);
		}
		
		public InsertBuilder(String name, float price, long expiredDate){
			this.name = name;
			this.price = price;
			this.expired = expiredDate;
		}
		
		public InsertBuilder setComment(String comment){
			this.comment = comment;
			return this;
		}
		
		public InsertBuilder setImage(int ossImageId){
			this.ossImage = new OssImage(ossImageId);
			return this;
		}
		
		public InsertBuilder setImage(OssImage ossImage){
			this.ossImage = ossImage;
			return this;
		}
		
		public boolean hasImage(){
			return this.ossImage != null;
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
		private float price = -1;
		private OssImage ossImage;
		
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
		
		public UpdateBuilder setImage(int ossImageId){
			this.ossImage = new OssImage(ossImageId);
			return this;
		}
		
		public UpdateBuilder setImage(OssImage ossImage){
			this.ossImage = ossImage;
			return this;
		}
		
		public boolean isImageChanged(){
			return this.ossImage != null;
		}
		
		public UpdateBuilder setPrice(float price){
			this.price = price;
			return this;
		}
		
		public boolean isPriceChanged(){
			return this.price != -1;
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
	private OssImage image;
	
	private CouponType(UpdateBuilder builder){
		setId(builder.id);
		setName(builder.name);
		setExpired(builder.expired);
		setComment(builder.comment);
		setPrice(builder.price);
		setImage(builder.ossImage);
	}
	
	private CouponType(InsertBuilder builder){
		setName(builder.name);
		setPrice(builder.price);
		setExpired(builder.expired);
		setComment(builder.comment);
		setImage(builder.ossImage);
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
	
	public OssImage getImage(){
		return image;
	}
	
	public void setImage(OssImage image){
		this.image = image;
	}
	
	public boolean hasImage(){
		return image != null;
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

	public final static byte COUPON_TYPE_JSONABLE_COMPLEX = 0;
	public final static byte COUPON_TYPE_JSONABLE_SIMPLE = 1;
	
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.id);
		jm.putInt("restaurantId", this.restaurantId);
		jm.putString("name", this.name);
		jm.putFloat("price", this.price);
		jm.putBoolean("isExpired", this.isExpired());
		jm.putString("expiredFormat", DateUtil.formatToDate(this.expired));
		if(flag == COUPON_TYPE_JSONABLE_COMPLEX){
			if(this.image != null){
				jm.putJsonable("ossImage", this.image, 0);
			}
		}
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
}
