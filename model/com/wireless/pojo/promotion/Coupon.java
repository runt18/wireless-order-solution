package com.wireless.pojo.promotion;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.dishesOrder.Order;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.util.DateUtil;
import com.wireless.pojo.util.SortedList;


public class Coupon implements Jsonable{

	public static class UseBuilder{
		private final int memberId;
		private final List<Integer> couponsToUse = SortedList.newInstance();
		private String comment;
		private final CouponOperation.Operate operation;
		private final int associateId;

		public static UseBuilder newInstance4Fast(int memberId){
			return new UseBuilder(memberId, CouponOperation.Operate.FAST_USE, 0);
		}
		
		public static UseBuilder newInstance4Fast(Member member){
			return new UseBuilder(member.getId(), CouponOperation.Operate.FAST_USE, 0);
		}
		
		public static UseBuilder newInstance4Order(Member member, int orderId){
			return new UseBuilder(member.getId(), CouponOperation.Operate.ORDER_USE, orderId);
		}
		
		public static UseBuilder newInstance4Order(int memberId, int orderId){
			return new UseBuilder(memberId, CouponOperation.Operate.ORDER_USE, orderId);
		}
		
		public static UseBuilder newInstance4Point(int memberId){
			return new UseBuilder(memberId, CouponOperation.Operate.POINT_EXCHANGE_USE, 0);
		}
		
		public static UseBuilder newInstance4Point(Member member){
			return new UseBuilder(member.getId(), CouponOperation.Operate.POINT_EXCHANGE_USE, 0);
		}
		
		private UseBuilder(int memberId, CouponOperation.Operate operation, int associateId){
			this.memberId = memberId;
			this.operation = operation;
			this.associateId = associateId;
		}
		
		public int getMemberId(){
			return this.memberId;
		}
		
		public CouponOperation.Operate getOperation(){
			return this.operation;
		}
		
		public int getAssociateId(){
			return this.associateId;
		}
		
		public UseBuilder addCoupon(Coupon coupon){
			return addCoupon(coupon.getId());
		}
		
		public UseBuilder addCoupon(int couponId){
			if(!couponsToUse.contains(couponId)){
				this.couponsToUse.add(couponId);
			}
			return this;
		}
		
		public UseBuilder setCoupons(List<Integer> coupons){
			if(coupons != null){
				this.couponsToUse.clear();
				this.couponsToUse.addAll(coupons);
			}
			return this;
		}
		
		public List<Integer> getCoupons(){
			return Collections.unmodifiableList(this.couponsToUse);
		}
		
		public UseBuilder setComment(String comment){
			this.comment = comment;
			return this;
		}
		
		public String getComment(){
			if(this.comment == null){
				return "";
			}
			return this.comment;
		}
	}
	
	public static class IssueBuilder{
		private final Map<Integer, Integer> promotions = new HashMap<Integer, Integer>();
		private final SortedList<Integer> members = SortedList.newInstance();
		private final CouponOperation.Operate operate;
		private final int issueAssociateId;
		private String comment;
		private String wxServer;
		
		public static IssueBuilder newInstance4Fast(){
			return new IssueBuilder(CouponOperation.Operate.FAST_ISSUE, 0);
		}
		
		public static IssueBuilder newInstance4Batch(){
			return new IssueBuilder(CouponOperation.Operate.BATCH_ISSUE, 0);
		}
		
		public static IssueBuilder newInstance4Order(Order order){
			return new IssueBuilder(CouponOperation.Operate.ORDER_ISSUE, order.getId());
		}
		
		public static IssueBuilder newInstance4Order(int orderId){
			return new IssueBuilder(CouponOperation.Operate.ORDER_ISSUE, orderId);
		}
		
		public static IssueBuilder newInstance4WxSubscribe(){
			return new IssueBuilder(CouponOperation.Operate.WX_SUBSCRIBE_ISSUE, 0);
		}

		public static IssueBuilder newInstance4WxScan(){
			return new IssueBuilder(CouponOperation.Operate.WX_SCAN_ISSUE, 0);
		}
		
		public static IssueBuilder newInstance4PointExchange(){
			return new IssueBuilder(CouponOperation.Operate.POINT_EXCHANGE_ISSUE, 0);
		}
		
		private IssueBuilder(CouponOperation.Operate operate, int associateId){
			this.operate = operate;
			this.issueAssociateId = associateId;
		}
		
		public Set<Entry<Integer, Integer>> getPromotions(){
			return this.promotions.entrySet();
		}
		
		public IssueBuilder addPromotion(int promotionId){
			return addPromotion(promotionId, 1);
		}
		
		public IssueBuilder addPromotion(int promotionId, int amount){
			promotions.put(promotionId, amount);
			return this;
		}
		
		public IssueBuilder addPromotion(Promotion promotion){
			return addPromotion(promotion.getId(), 1);
		}
		
		public IssueBuilder addPromotion(Promotion promotion, int amount){
			promotions.put(promotion.getId(), amount);
			return this;
		}
		
		public IssueBuilder addMember(int memberId){
			if(!members.containsElement(memberId)){
				members.add(memberId);
			}
			return this;
		}
		
		public IssueBuilder addMember(Member member){
			if(!members.containsElement(member.getId())){
				members.add(member.getId());
			}
			return this;
		}
		
		public IssueBuilder setMembers(List<Member> members){
			this.members.clear();
			for(Member each : members){
				this.members.add(each.getId());
			}
			return this;
		}
		
		public CouponOperation.Operate getOperation(){
			return this.operate;
		}
		
		public IssueBuilder setComment(String comment){
			this.comment = comment;
			return this;
		}
		
		public String getComment(){
			if(this.comment == null){
				return "";
			}
			return this.comment;
		}
		
		public int getAssociateId(){
			return this.issueAssociateId;
		}
		
		public List<Integer> getMembers(){
			return Collections.unmodifiableList(members);
		}
		
		public IssueBuilder setWxServer(String wxServer){
			this.wxServer = wxServer;
			return this;
		}
		
		public String getWxServer(){
			if(this.wxServer == null){
				return "";
			}
			return this.wxServer;
		}
		
		public boolean hasWxServer(){
			return getWxServer().length() > 0;
		}
		
	}
	
//	public static class CreateBuilder{
//		private DrawType drawType = DrawType.AUTO;
//		private final int couponTypeId;
//		private final int promotionId;
//		private final SortedList<Member> members = SortedList.newInstance();
//		
//		public CreateBuilder(int couponTypeId, int promotionId){
//			this.couponTypeId = couponTypeId;
//			this.promotionId = promotionId;
//		}
//
//		public int getCouponTypeId(){
//			return this.couponTypeId;
//		}
//		
//		public int getPromotionId(){
//			return this.promotionId;
//		}
//		
//		public CreateBuilder setDrawType(DrawType drawType){
//			this.drawType = drawType;
//			return this;
//		}
//		
//		public DrawType getDrawType(){
//			return this.drawType;
//		}
//		
//		public CreateBuilder addMember(int memberId){
//			Member member = new Member(memberId);
//			if(!members.containsElement(member)){
//				members.add(member);
//			}
//			return this;
//		}
//		
//		public CreateBuilder setMembers(List<Member> members){
//			this.members.clear();
//			this.members.addAll(members);
//			return this;
//		}
//		
//		public List<Member> getMembers(){
//			return Collections.unmodifiableList(members);
//		}
//		
//	}
	
//	public static class InsertBuilder{
//		private final CouponType couponType;
//		private final Promotion promotion;
//		private final Member member;
//		
//		public InsertBuilder(CouponType couponType, Member member, Promotion promotion){
//			this.couponType = couponType;
//			this.member = member;
//			this.promotion = promotion;
//		}
//		
//		public Coupon build(){
//			return new Coupon(this);
//		}
//	}
	
	public static enum Status{
		UNKNOWN(0, "未知"),
		CREATED(1, "已创建"),
		//PUBLISHED(2, "已发布"),
		ISSUED(3, "已发放"),
		USED(4, "已使用"),
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
	
	private int id;
	private int restaurantId;
	private CouponType couponType;
	private Promotion promotion;
	private long birthDate;
	private Member member;
	private Status status = Status.UNKNOWN;
	
//	private Coupon(InsertBuilder builder){
//		setCouponType(builder.couponType);
//		setMember(builder.member);
//		setPromotion(builder.promotion);
//		setStatus(Status.CREATED);
//		setBirthDate(System.currentTimeMillis());
//	}
	
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

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public boolean isDrawn(){
		return this.status == Status.ISSUED;
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
		jm.putString("statusText", this.status.desc);
		jm.putInt("statusValue", this.status.val);
		jm.putString("birthDate", DateUtil.formatToDate(this.getBirthDate()));
		if(flag == COUPON_JSONABLE_COMPLEX){
			jm.putJsonable("promotion", this.promotion, 0);
			jm.putJsonable("couponType", this.couponType, CouponType.COUPON_TYPE_JSONABLE_COMPLEX);
		}else if(flag == COUPON_JSONABLE_SIMPLE){
			jm.putJsonable("promotion", this.promotion, 0);
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
