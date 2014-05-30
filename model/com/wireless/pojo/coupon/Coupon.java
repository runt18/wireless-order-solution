package com.wireless.pojo.coupon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.client.Member;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.util.SortedList;


public class Coupon implements Jsonable{

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
	private CouponType couponType;
	private long birthDate;
	private String createStaff;
	private Member member;
	private int orderId;
	private long orderDate;
	private Status status = Status.UNKNOWN;
	
	private Coupon(InsertBuilder builder){
		setCouponType(new CouponType(builder.couponTypeId));
		setMember(new Member(builder.memberId));
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

	public String getCreateStaff(){
		if(this.createStaff == null){
			return "";
		}
		return this.createStaff;
	}
	
	public void setCreateStaff(String createStaff){
		this.createStaff = createStaff;
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
		jm.putString("createStaff", this.getCreateStaff());
		jm.putString("birthDate", DateUtil.formatToDate(this.getBirthDate()));
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
}
