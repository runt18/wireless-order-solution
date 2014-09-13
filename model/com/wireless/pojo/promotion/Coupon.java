package com.wireless.pojo.promotion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.util.SortedList;


public class Coupon implements Jsonable{

	public final static byte ST_PARCELABLE_COMPLEX = 0;
	public final static byte ST_PARCELABLE_SIMPLE = 1;	
	
	public static class CreateBuilder{
		private final int couponTypeId;
		private final int promotionId;
		private final List<Integer> members = SortedList.newInstance();
		
		public CreateBuilder(int couponTypeId, int promotionId){
			this.couponTypeId = couponTypeId;
			this.promotionId = promotionId;
		}
		
		public CreateBuilder addMemberId(int memberId){
			if(!members.contains(memberId)){
				members.add(memberId);
			}
			return this;
		}
		
		public CreateBuilder setMembers(List<Integer> members){
			this.members.addAll(members);
			return this;
		}
		
		public List<InsertBuilder> build(){
			List<InsertBuilder> builders = new ArrayList<InsertBuilder>();
			for(int memberId : members){
				builders.add(new InsertBuilder(couponTypeId, memberId, promotionId));
			}
			return Collections.unmodifiableList(builders);
		}
	}
	
	public static class InsertBuilder{
		private final int couponTypeId;
		private final int promotionId;
		private final int memberId;
		
		public InsertBuilder(int couponTypeId, int memberId, int promotionId){
			this.couponTypeId = couponTypeId;
			this.memberId = memberId;
			this.promotionId = promotionId;
		}
		
		public Coupon build(){
			return new Coupon(this);
		}
	}
	
	public static enum Status{
		UNKNOWN(0, "未知"),
		CREATED(1, "已创建"),
		PUBLISHED(2, "已发布"),
		DRAWN(3, "已领取"),
		USED(4, "已使用"),
		EXPIRED(5, "已过期"),
		FINISH(6, "已结束");
		
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
			throw new IllegalArgumentException("The status(val=" + val + ") is invalid.");
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
	
	public class DrawProgress implements Jsonable{
		private final int point;
		private DrawProgress(int point){
			this.point = point;
		}
		
		public int getPoint(){
			return this.point;
		}
		
		public boolean isOk(){
			if(promotion.getType() == Promotion.Type.FREE){
				return true;
			}else if(promotion.getType() == Promotion.Type.ONCE || promotion.getType() == Promotion.Type.TOTAL){
				return point >= promotion.getPoint();
			}else{
				return false;
			}
		}
		
		@Override
		public String toString(){
			return promotion.getType().toString() + promotion.getPoint() + ", 当前积分" + point;
		}

		@Override
		public JsonMap toJsonMap(int flag) {
			JsonMap jm = new JsonMap();
			jm.putInt("point", this.point);
			jm.putBoolean("isOk", this.isOk());
			return jm;
		}

		@Override
		public void fromJsonMap(JsonMap jsonMap, int flag) {
			
		}
	}
	
	private int id;
	private int restaurantId;
	private CouponType couponType;
	private Promotion promotion;
	private long birthDate;
	private Member member;
	private int orderId;
	private long orderDate;
	private Status status = Status.UNKNOWN;
	private DrawProgress drawProgress;
	
	private Coupon(InsertBuilder builder){
		setCouponType(new CouponType(builder.couponTypeId));
		setMember(new Member(builder.memberId));
		setPromotion(new Promotion(builder.promotionId));
		setStatus(Status.CREATED);
		setBirthDate(System.currentTimeMillis());
	}
	
	public Coupon(int id){
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

	public CouponType getCouponType() {
		return couponType;
	}

	public void setCouponType(CouponType couponType) {
		if(couponType != null){
			if(this.couponType == null){
				this.couponType = new CouponType(0);
			}
			this.couponType.copyFrom(couponType);
		}
	}

	public long getBirthDate() {
		return birthDate;
	}
	
	public void setBirthDate(long birthDate) {
		this.birthDate = birthDate;
	}

	public Member getMember() {
		return member;
	}

	public void setMember(Member member) {
		this.member = member;
	}

	public Promotion getPromotion(){
		return this.promotion;
	}
	
	public void setPromotion(Promotion promotion){
		this.promotion = promotion;
	}
	
	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public long getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(long orderDate) {
		this.orderDate = orderDate;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public boolean isCreated(){
		return this.status == Status.CREATED;
	}
	
	public boolean isUsed(){
		return this.status == Status.USED;
	}
	
	public boolean isExpired(){
		return this.status == Status.EXPIRED;
	}
	
	public float getPrice(){
		if(this.couponType != null){
			return this.couponType.getPrice();
		}else{
			return 0;
		}
	}
	
	public String getName(){
		if(this.couponType != null){
			return this.couponType.getName();
		}else{
			return "";
		}
	}
	
	public long getExpired(){
		if(this.couponType != null){
			return this.couponType.getExpired();
		}else{
			return 0;
		}
	}
	
	public void setDrawProgress(int point){
		drawProgress = new DrawProgress(point);
	}
	
	public DrawProgress getDrawProgress(){
		return this.drawProgress;
	}
	
	@Override
	public int hashCode(){
		return id * 31 + 17;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Coupon)){
			return false;
		}else{
			return id == ((Coupon)obj).id;
		}
	}
	
	@Override
	public String toString(){
		return "coupon(id = " + getId() +
				",status = " + getStatus().getDesc() + 
				(couponType != null ? ",type = " + couponType + "" : "") + ")";
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putJsonable("member", this.member, 0);
		jm.putInt("couponId", this.id);
		jm.putInt("restaurantId", this.restaurantId);
		jm.putJsonable("couponType", this.couponType, 0);
		jm.putString("orderId", this.orderId == 0 ? "----" : Integer.toString(this.orderId));
		jm.putString("orderDate", DateUtil.formatToDate(this.getOrderDate()));
		jm.putString("statusText", this.status.desc);
		jm.putInt("statusValue", this.status.val);
		jm.putString("birthDate", DateUtil.formatToDate(this.getBirthDate()));
		
		if(flag == ST_PARCELABLE_COMPLEX){
			jm.putJsonable("promotion", this.promotion, 0);
			jm.putJsonable("drawProgress", getDrawProgress(), 0);			
		}

		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
}
