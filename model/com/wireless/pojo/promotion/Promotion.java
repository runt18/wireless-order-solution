package com.wireless.pojo.promotion;

import java.util.Collections;
import java.util.List;

import com.wireless.pojo.billStatistics.DateRange;
import com.wireless.pojo.util.SortedList;

public class Promotion {

	public static class CreateBuilder{
		private final DateRange range;
		private final String title;
		private final String body;
		private Type type = Type.DISPLAY_ONLY;
		private int point;
		private final CouponType.InsertBuilder typeBuilder;
		private final List<Integer> members = SortedList.newInstance();
		
		public CreateBuilder(String title, DateRange range, String body, CouponType.InsertBuilder typeBuilder){
			this.title = title;
			this.range = range;
			this.body = body;
			this.typeBuilder = typeBuilder;
		}
		
		public CreateBuilder setType(Type type){
			this.type = type;
			return this;
		}
		
		public CreateBuilder setPoint(int point){
			this.point = point;
			return this;
		}
		
		public CreateBuilder addMember(int memberId){
			if(type == Type.DISPLAY_ONLY){
				throw new IllegalStateException("【" + Type.DISPLAY_ONLY.desc + "】类型的优惠活动不能发放优惠券");
			}
			if(!members.contains(memberId)){
				members.add(memberId);
			}
			return this;
		}
		
		public List<Integer> getMembers(){
			return Collections.unmodifiableList(members);
		}
		
		public CouponType.InsertBuilder getTypeBuilder(){
			return this.typeBuilder;
		}
		
		public Promotion build(){
			return new Promotion(this);
		}
	}
	
	public static enum Type{
		DISPLAY_ONLY(1, "只展示"),
		FREE(2, "免费领取"),
		ONCE(3, "单次积分符合条件领取"),
		TOTAL(4, "累计积分符合条件领取");
		
		private final int val;
		private final String desc;
		Type(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public static Type valueOf(int val){
			for(Type type : values()){
				if(type.val == val){
					return type;
				}
			}
			throw new IllegalArgumentException("The type(val = " + val + ") is invalid.");
		}
		
		public int getVal(){
			return this.val;
		}
		
		@Override
		public String toString(){
			return desc;
		}
	}
	
	public static enum Status{
		CREATED(1, "已创建"),
		PUBLISH(2, "已发布"),
		PROGRESS(3, "进行中"),
		FINISH(4, "已结束");
		
		private final int val;
		private final String desc;
		Status(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public static Status valueOf(int val){
			for(Status status : values()){
				if(status.val == val){
					return status;
				}
			}
			throw new IllegalArgumentException("The status(val = " + val + ") is invalid.");
		}
		
		public int getVal(){
			return this.val;
		}
		
		@Override
		public String toString(){
			return desc;
		}
	}
	
	private int id;
	private int restaurantId;
	private long createDate;
	private DateRange dateRange;;
	private String title;
	private String body;
	private CouponType couponType;
	private Status status = Status.CREATED;
	private Type type = Type.FREE;
	private int point;
	
	private Promotion(CreateBuilder builder){
		this.createDate = System.currentTimeMillis();
		this.dateRange = builder.range;
		this.title = builder.title;
		this.body = builder.body;
		this.type = builder.type;
		this.point = builder.point;
		this.couponType = builder.typeBuilder.build();
		this.status = Status.CREATED;
	}
	
	public Promotion(int id){
		this.id = id;
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
	
	public DateRange getDateRange(){
		return this.dateRange;
	}
	
	public void setDateRange(DateRange range){
		this.dateRange = range;
	}
	
	public long getCreateDate(){
		return this.createDate;
	}
	
	public void setCreateDate(long createDate){
		this.createDate = createDate;
	}
	
	public String getTitle() {
		if(title == null){
			return "";
		}
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getBody() {
		if(body == null){
			return "";
		}
		return body;
	}
	
	public void setBody(String body) {
		this.body = body;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}
	
	public Type getType() {
		return type;
	}
	
	public void setType(Type type) {
		this.type = type;
	}
	
	public CouponType getCouponType(){
		return this.couponType;
	}
	
	public void setCouponType(CouponType couponType){
		this.couponType = couponType;
	}
	
	public int getPoint(){
		return this.point;
	}
	
	public void setPoint(int point){
		this.point = point;
	}
	
	@Override
	public int hashCode(){
		return id * 31 + 17;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Promotion)){
			return false;
		}else{
			return id == ((Promotion)obj).id;
		}
	}
	
	@Override
	public String toString(){
		return title;
	}
	
}
