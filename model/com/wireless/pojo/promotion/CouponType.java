package com.wireless.pojo.promotion;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.oss.OssImage;
import com.wireless.pojo.util.DateUtil;

public class CouponType implements Jsonable{
	public static enum ExpiredType{
		EXPIRED_DATE(1, "开始结束时间"),
		DURANTION(2, "有效期");
		
		private final int val;
		private final String desc;
		
		ExpiredType(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public static ExpiredType valueOf(int val){
			for(ExpiredType expiredType : values()){
				if(expiredType.val == val){
					return expiredType;
				}
			}
			throw new IllegalArgumentException("The expiredType(val = " + val + ") passed is invalid.");
		}
		
		public int getVal(){
			return this.val;
		}
		
		public String getDesc(){
			return this.desc;
		}
		
		@Override
		public String toString(){
			return this.desc;
		}
	}
	
	public static class InsertBuilder{
		private final String name;
		private final float price;
		private long endExpired;
		private long beginExpired;
		private String comment;
		private int limitAmount;
		private int expiredDuration;
		private ExpiredType expiredType;
		
		
		private OssImage ossImage;
		
		public InsertBuilder(String name, float price){
			this.name = name;
			this.price = price;
		}
		
		public InsertBuilder setLimitAmount(int limit){
			this.limitAmount = limit;
			return this;
		}
		
		public InsertBuilder setExpiredDuration(int duration){
			this.expiredDuration = duration;
			return this;
		}
		
		public InsertBuilder setExpiredType(ExpiredType expiredType){
			this.expiredType = expiredType;
			return this;
		}
		
		public InsertBuilder setExpired(long beginExpired, long endExpired){
			if(beginExpired != 0 && endExpired != 0){
				if(beginExpired > endExpired){
					throw new IllegalArgumentException("开始时间不能大于结束时间");
				}
			}
			
			this.beginExpired = beginExpired > 0 ? beginExpired : 0;
			this.endExpired = endExpired > 0 ? endExpired : 0;
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
		private int limitAmount = -1;
		private int expiredDuration;
		private ExpiredType expiredType;
		
		public UpdateBuilder(int id, String name){
			this.id = id;
			this.name = name;
		}
		
		public boolean isLimitAmountChanged(){
			return this.limitAmount >= 0;
		}
		
		public UpdateBuilder setLimitAmount(int limit){
			if(limit < 0){
				throw new IllegalArgumentException("张数限制不能小于0");
			}
			this.limitAmount = limit;
			return this;
		}
		
		public boolean isNameChanged(){
			return name != null;
		}
		
		public UpdateBuilder setExpired(long beginExpired, long endExpired){
			if(beginExpired != 0 && endExpired != 0){
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
		
		public boolean isExpiredDurationChanged(){
			return this.expiredDuration >=0;
		}
		
		public boolean isExpiredTypeChanged(){
			return this.expiredType != null;
		}
		
		public UpdateBuilder setExpiredDuration(int duration){
			this.expiredDuration = duration;
			return this;
		}
		
		public UpdateBuilder setExpiredType(ExpiredType expiredType){
			this.expiredType = expiredType;
			return this;
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
	private int limitAmout;
	private int expiredDuration;
	private ExpiredType expiredType;
	
	private CouponType(UpdateBuilder builder){
		setId(builder.id);
		setName(builder.name);
		setBeginExpired(builder.beginExpired);
		setEndExpired(builder.endExpired);
		setComment(builder.comment);
		setPrice(builder.price);
		setImage(builder.ossImage);
		setLimitAmount(builder.limitAmount);
		setExpiredDuration(builder.expiredDuration);
		setExpiredType(builder.expiredType);
	}
	
	private CouponType(InsertBuilder builder){
		setName(builder.name);
		setPrice(builder.price);
		setBeginExpired(builder.beginExpired);
		setEndExpired(builder.endExpired);
		setComment(builder.comment);
		setImage(builder.ossImage);
		setLimitAmount(builder.limitAmount);
		setExpiredDuration(builder.expiredDuration);
		setExpiredType(builder.expiredType);
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
			setLimitAmount(src.limitAmout);
			setExpiredDuration(src.getExpiredDuration());
			setExpiredType(src.getExpiredType());
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
	
	public boolean hasLimit(){
		return this.limitAmout > 0;
	}
	
	
	public int getExpiredDuration() {
		return expiredDuration;
	}

	public void setExpiredDuration(int expiredDuration) {
		this.expiredDuration = expiredDuration;
	}

	public ExpiredType getExpiredType() {
		return expiredType;
	}

	public void setExpiredType(ExpiredType expiredType) {
		this.expiredType = expiredType;
	}

	public int getLimitAmount(){
		return limitAmout;
	}
	
	public void setLimitAmount(int limit){
		this.limitAmout = limit;
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
	
	public boolean hasEndExpire(){
		return this.endExpired != 0;
	}
	
	public boolean hasBeginExpire(){
		return this.beginExpired != 0;
	}
	
	public boolean hasExpiredDuration(){
		return this.expiredDuration != 0;
	}
	
	public boolean isBeforeBegin(){
		if(beginExpired == 0){
			return false;
		}else{
			return System.currentTimeMillis() < beginExpired;
		}
	}
	
	public boolean isAfterEnd(){
		if(endExpired == 0){
			return false;
		}else{
			return System.currentTimeMillis() > endExpired;
		}
	}

	public boolean isBetween(){
		return !isBeforeBegin() && !isAfterEnd();
	}
	
	boolean isExpired(){
		return isBeforeBegin() || isAfterEnd();
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
		jm.putInt("limitAmount", this.limitAmout);
		jm.putInt("expiredDuration", this.expiredDuration);
		jm.putInt("expiredType", this.expiredType != null ? this.expiredType.val : 1);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
}
