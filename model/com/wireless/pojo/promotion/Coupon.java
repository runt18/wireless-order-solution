package com.wireless.pojo.promotion;

import java.util.Collections;
import java.util.List;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.util.SortedList;


public class Coupon implements Jsonable{

	public static class CreateBuilder{
		private DrawType drawType = DrawType.AUTO;
		private final int couponTypeId;
		private final int promotionId;
		private final SortedList<Member> members = SortedList.newInstance();
		
		public CreateBuilder(int couponTypeId, int promotionId){
			this.couponTypeId = couponTypeId;
			this.promotionId = promotionId;
		}

		public int getCouponTypeId(){
			return this.couponTypeId;
		}
		
		public int getPromotionId(){
			return this.promotionId;
		}
		
		public CreateBuilder setDrawType(DrawType drawType){
			this.drawType = drawType;
			return this;
		}
		
		public DrawType getDrawType(){
			return this.drawType;
		}
		
		public CreateBuilder addMember(int memberId){
			Member member = new Member(memberId);
			if(!members.containsElement(member)){
				members.add(member);
			}
			return this;
		}
		
		public CreateBuilder setMembers(List<Member> members){
			this.members.clear();
			this.members.addAll(members);
			return this;
		}
		
		public List<Member> getMembers(){
			return Collections.unmodifiableList(members);
		}
		
	}
	
	public static class InsertBuilder{
		private final CouponType couponType;
		private final Promotion promotion;
		private final Member member;
		
		public InsertBuilder(CouponType couponType, Member member, Promotion promotion){
			this.couponType = couponType;
			this.member = member;
			this.promotion = promotion;
		}
		
		public Coupon build(){
			return new Coupon(this);
		}
	}
	
	public static enum DrawType{
		AUTO("自动"),
		MANUAL("手动");
		
		private final String desc;
		DrawType(String desc){
			this.desc = desc;
		}
		
		@Override
		public String toString(){
			return this.desc;
		}
	}
	
	public static enum Status{
		UNKNOWN(0, "未知"),
		CREATED(1, "已创建"),
		//PUBLISHED(2, "已发布"),
		DRAWN(3, "已领取"),
		USED(4, "已使用"),
		//EXPIRED(5, "已过期"),
		//FINISH(6, "已结束");
		;
		
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
	
	public static class DrawProgress implements Jsonable{
		
		private int point;
		private final Coupon coupon;
		private DrawProgress(int point, Coupon coupon){
			this.point = point;
			this.coupon = coupon;
		}
		
		public int getPoint(){
			return this.point;
		}
		
		public boolean isOk(){
//			if(coupon.getStatus() == Coupon.Status.PUBLISHED && coupon.getPromotion().getStatus() == Promotion.Status.PROGRESS){
//				if(coupon.getPromotion().getRule() == Promotion.Rule.FREE){
//					return true;
//				}else if(coupon.getPromotion().getRule() == Promotion.Rule.ONCE || coupon.getPromotion().getRule() == Promotion.Rule.TOTAL){
//					return point >= coupon.getPromotion().getPoint();
//				}else{
//					return false;
//				}
//			}else{
//				return false;
//			}
			return true;
		}
		
		@Override
		public String toString(){
			return coupon.getPromotion().getTitle() + ", " + coupon.getPromotion().getRule().toString() + coupon.getPromotion().getPoint() + ", 当前积分" + point;
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
	private long drawDate;
	private int orderId;
	private long orderDate;
	private Status status = Status.UNKNOWN;
	private final DrawProgress drawProgress = new DrawProgress(0, this);
	
	private Coupon(InsertBuilder builder){
		setCouponType(builder.couponType);
		setMember(builder.member);
		setPromotion(builder.promotion);
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

	public long getDrawDate(){
		return this.drawDate;
	}
	
	public void setDrawDate(long drawDate){
		this.drawDate = drawDate;
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

	public boolean isDrawn(){
		return this.status == Status.DRAWN;
	}
	
	public boolean isCreated(){
		return this.status == Status.CREATED;
	}
	
	public boolean isUsed(){
		return this.status == Status.USED;
	}
	
	public boolean isExpired(){
		return this.couponType.isExpired();
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
		drawProgress.point = point;
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

	public final static byte COUPON_JSONABLE_COMPLEX = 0;
	public final static byte COUPON_JSONABLE_SIMPLE = 1;	
	public final static byte COUPON_JSONABLE_WITH_PROMOTION = 2;		
	
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putJsonable("member", this.member, 0);
		jm.putInt("couponId", this.id);
		jm.putInt("restaurantId", this.restaurantId);
		jm.putString("orderId", this.orderId == 0 ? "----" : Integer.toString(this.orderId));
		jm.putString("orderDate", DateUtil.formatToDate(this.getOrderDate()));
		jm.putString("statusText", this.status.desc);
		jm.putInt("statusValue", this.status.val);
		jm.putString("birthDate", DateUtil.formatToDate(this.getBirthDate()));
		
		if(flag == COUPON_JSONABLE_COMPLEX){
			jm.putJsonable("promotion", this.promotion, 0);
			jm.putJsonable("drawProgress", getDrawProgress(), 0);			
			jm.putJsonable("couponType", this.couponType, CouponType.COUPON_TYPE_JSONABLE_COMPLEX);
		}else if(flag == COUPON_JSONABLE_SIMPLE){
			jm.putJsonable("couponType", this.couponType, CouponType.COUPON_TYPE_JSONABLE_SIMPLE);
		}else if(flag == COUPON_JSONABLE_WITH_PROMOTION){
			jm.putJsonable("promotion", this.promotion, 0);
		}

		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
}
