package com.wireless.pojo.promotion;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.oss.OssImage;
import com.wireless.pojo.util.DateUtil;

public class CouponType implements Jsonable{

	public static class InsertBuilder{
		private final String name;
		private final float price;
		private long endExpired;
		private long beginExpired;
		private String comment;
		
		private OssImage ossImage;
		
		public InsertBuilder(String name, float price){
			this.name = name;
			this.price = price;
		}
		
		public InsertBuilder setExpired(long beginExpired, long endExpired){
			if(beginExpired > endExpired){
				throw new IllegalArgumentException("开始时间不能大于结束时间");
			}
			this.beginExpired = beginExpired;
			this.endExpired = endExpired;
			
			return this;
		}
		
		public InsertBuilder setExpired(String beginExpired, String endExpired){
			return setExpired(DateUtil.parseDate(beginExpired), DateUtil.parseDate(endExpired));
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
		private long beginExpired;
		private long endExpired;
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
		
		public UpdateBuilder setExpired(long beginExpired, long endExpired){
			if(beginExpired > 0 && endExpired > 0){
				if(beginExpired > endExpired){
					throw new IllegalArgumentException("开始时间不能大于结束时间");
				}
			}
			
			this.beginExpired = beginExpired > 0 ? beginExpired : 0;
			this.endExpired = endExpired > 0 ? endExpired : 0;
			return this;
		}
		
		public UpdateBuilder setExpired(String beginExpired, String endExpired){
			return setExpired(DateUtil.parseDate(beginExpired), DateUtil.parseDate(endExpired));
		}
		
		public boolean isBeginExpiredChanged(){
			return this.beginExpired >= 0;
		}
		
		public boolean isEndExpiredChanged(){
			return this.endExpired >= 0;
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
	private long beginExpired;
	private long endExpired;
	private String comment;
	private OssImage image;
	
	private CouponType(UpdateBuilder builder){
		setId(builder.id);
		setName(builder.name);
		setBeginExpired(builder.beginExpired);
		setEndExpired(builder.endExpired);
		setComment(builder.comment);
		setPrice(builder.price);
		setImage(builder.ossImage);
	}
	
	private CouponType(InsertBuilder builder){
		setName(builder.name);
		setPrice(builder.price);
		setBeginExpired(builder.beginExpired);
		setEndExpired(builder.endExpired);
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
			setBeginExpired(src.getBeginExpired());
			setEndExpired(src.getEndExpired());
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
	
	public long getBeginExpired(){
		return beginExpired;
	}
	
	public long getEndExpired() {
		return endExpired;
	}
	
	public void setBeginExpired(long beginExpired){
		this.beginExpired  = beginExpired > 0 ? beginExpired : 0;
	}
	
	public void setEndExpired(long endExpired){
		this.endExpired = endExpired > 0 ? endExpired : 0;
	}
	
	public boolean isExpired(){
		if(beginExpired != 0 && endExpired != 0){
			return System.currentTimeMillis() > endExpired || beginExpired > System.currentTimeMillis();
		}else if(beginExpired == 0 && endExpired != 0){
			return System.currentTimeMillis() > endExpired;
		}else if(beginExpired != 0 && endExpired == 0){
			return System.currentTimeMillis() < beginExpired;
		}else{
			return false;
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
		jm.putString("beginExpired", this.beginExpired > 0 ? DateUtil.formatToDate(this.beginExpired) : "");
		jm.putString("endExpired", this.endExpired > 0 ? DateUtil.formatToDate(this.endExpired) : "");
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
