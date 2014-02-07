package com.wireless.pojo.coupon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wireless.pojo.util.SortedList;


public class Coupon {

	public static class InsertAllBuilder{
		private final int couponTypeId;
		private final List<Integer> members = SortedList.newInstance();
		
		public InsertAllBuilder(int couponTypeId){
			this.couponTypeId = couponTypeId;
		}
		
		public InsertAllBuilder addMemberId(int memberId){
			if(!members.contains(memberId)){
				members.add(memberId);
			}
			return this;
		}
		
		public List<InsertBuilder> build(){
			List<InsertBuilder> builders = new ArrayList<InsertBuilder>();
			for(int memberId : members){
				builders.add(new InsertBuilder(couponTypeId, memberId));
			}
			return Collections.unmodifiableList(builders);
		}
	}
	
	public static class InsertBuilder{
		private final int couponTypeId;
		private final int memberId;
		
		public InsertBuilder(int couponTypeId, int memberId){
			this.couponTypeId = couponTypeId;
			this.memberId = memberId;
		}
		
		public Coupon build(){
			return new Coupon(this);
		}
	}
	
	public static enum Status{
		UNKNOWN(0, "未知"),
		CREATED(1, "已发放"),
		USED(2, "已使用"),
		EXPIRED(3, "已过期");
		
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
	
	private int id;
	private int restaurantId;
	private CouponType couponType = new CouponType(0);
	private long birthDate;
	private int memberId;
	private int orderId;
	private long orderDate;
	private Status status = Status.UNKNOWN;
	
	private Coupon(InsertBuilder builder){
		setCouponType(new CouponType(builder.couponTypeId));
		setMemberId(builder.memberId);
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
		this.couponType.copyFrom(couponType);
	}

	public long getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(long birthDate) {
		this.birthDate = birthDate;
	}

	public int getMemberId() {
		return memberId;
	}

	public void setMemberId(int memberId) {
		this.memberId = memberId;
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
		return this.couponType.getPrice();
	}
	
	public String getName(){
		return this.couponType.getName();
	}
	
	public long getExpired(){
		return this.couponType.getExpired();
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
}
