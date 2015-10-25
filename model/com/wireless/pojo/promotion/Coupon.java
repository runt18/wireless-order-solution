package com.wireless.pojo.promotion;

import java.util.Collections;
import java.util.List;

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
		private final UseMode mode;
		private final int associateId;

		public static UseBuilder newInstance4Fast(int memberId){
			return new UseBuilder(memberId, UseMode.FAST, 0);
		}
		
		public static UseBuilder newInstance4Fast(Member member){
			return new UseBuilder(member.getId(), UseMode.FAST, 0);
		}
		
		public static UseBuilder newInstance4Order(Member member, int orderId){
			return new UseBuilder(member.getId(), UseMode.ORDER, orderId);
		}
		
		public static UseBuilder newInstance4Order(int memberId, int orderId){
			return new UseBuilder(memberId, UseMode.ORDER, orderId);
		}
		
		private UseBuilder(int memberId, UseMode mode, int associateId){
			this.memberId = memberId;
			this.mode = mode;
			this.associateId = associateId;
		}
		
		public int getMemberId(){
			return this.memberId;
		}
		
		public UseMode getMode(){
			return this.mode;
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
		private final SortedList<Integer> promotions = SortedList.newInstance();
		private final SortedList<Integer> members = SortedList.newInstance();
		private final IssueMode issueMode;
		private final int issueAssociateId;
		private String comment;
		
		public static IssueBuilder newInstance4Fast(){
			return new IssueBuilder(IssueMode.FAST, 0);
		}
		
		public static IssueBuilder newInstance4Order(Order order){
			return new IssueBuilder(IssueMode.ORDER, order.getId());
		}
		
		public static IssueBuilder newInstance4Order(int orderId){
			return new IssueBuilder(IssueMode.ORDER, orderId);
		}
		
		public static IssueBuilder newInstance4WxSubscribe(){
			return new IssueBuilder(IssueMode.WX_SUBSCRIBE, 0);
		}
		
		private IssueBuilder(IssueMode mode, int associateId){
			this.issueMode = mode;
			this.issueAssociateId = associateId;
		}
		
		public List<Integer> getPromotions(){
			return Collections.unmodifiableList(this.promotions);
		}
		
		public IssueBuilder addPromotion(int promotionId){
			if(!promotions.containsElement(promotionId)){
				promotions.add(promotionId);
			}
			return this;
		}
		
		public IssueBuilder addPromotion(Promotion promotion){
			if(!promotions.containsElement(promotion.getId())){
				promotions.add(promotion.getId());
			}
			return this;
		}
		
		public IssueBuilder setPromotions(List<Promotion> promotions){
			this.promotions.clear();
			for(Promotion each : promotions){
				this.promotions.add(each.getId());
			}
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
		
		public IssueMode getMode(){
			return this.issueMode;
		}
		
		public int getAssociateId(){
			return this.issueAssociateId;
		}
		
		public List<Integer> getMembers(){
			return Collections.unmodifiableList(members);
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
	
	public static enum IssueMode{
		FAST(1, "快速"),
		ORDER(2, "账单"),
		WX_SUBSCRIBE(3, "微信关注");
		
		private final int val;
		private final String desc;
		
		IssueMode(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public int getVal(){
			return this.val;
		}
		
		public static IssueMode valueOf(int val){
			for(IssueMode mode : values()){
				if(mode.val == val){
					return mode;
				}
			}
			throw new IllegalArgumentException("The (val = " + val + ") passed is invalid.");
		}
		
		@Override
		public String toString(){
			return this.desc;
		}
	}
	
	public static enum UseMode{
		FAST(1, "快速"),
		ORDER(2, "账单");
		
		private final int val;
		private final String desc;
		
		UseMode(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public int getVal(){
			return this.val;
		}
		
		public static UseMode valueOf(int val){
			for(UseMode mode : values()){
				if(mode.val == val){
					return mode;
				}
			}
			throw new IllegalArgumentException("The (val = " + val + ") passed is invalid.");
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
	private long issueDate;
	private String issueStaff;
	private IssueMode issueMode;
	private int issueAssociateId;
	private String issueComment;
	private long useDate;
	private String useStaff;
	private UseMode useMode;
	private int useAssociateId;
	private String useComment;
	private Status status = Status.UNKNOWN;
	private final DrawProgress drawProgress = new DrawProgress(0, this);
	
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
	
	public long getExpired(){
		if(this.couponType != null){
			return this.couponType.getExpired();
		}else{
			return 0;
		}
	}
	
	public long getIssueDate() {
		return issueDate;
	}

	public void setIssueDate(long issueDate) {
		this.issueDate = issueDate;
	}

	public String getIssueStaff() {
		if(this.issueStaff == null){
			return "";
		}
		return issueStaff;
	}

	public void setIssueStaff(String issueStaff) {
		this.issueStaff = issueStaff;
	}

	public IssueMode getIssueMode() {
		return issueMode;
	}

	public void setIssueMode(IssueMode issueMode) {
		this.issueMode = issueMode;
	}

	public int getIssueAssociateId() {
		return issueAssociateId;
	}

	public void setIssueAssociateId(int issueAssociateId) {
		this.issueAssociateId = issueAssociateId;
	}

	public String getIssueComment() {
		if(this.issueComment == null){
			return "";
		}
		return issueComment;
	}

	public void setIssueComment(String issueComment) {
		this.issueComment = issueComment;
	}

	public long getUseDate() {
		return useDate;
	}

	public void setUseDate(long useDate) {
		this.useDate = useDate;
	}

	public String getUseStaff() {
		if(this.useStaff == null){
			return "";
		}
		return useStaff;
	}

	public void setUseStaff(String useStaff) {
		this.useStaff = useStaff;
	}

	public UseMode getUseMode() {
		return useMode;
	}

	public void setUseMode(UseMode useMode) {
		this.useMode = useMode;
	}

	public int getUseAssociateId() {
		return useAssociateId;
	}

	public void setUseAssociateId(int useAssociateId) {
		this.useAssociateId = useAssociateId;
	}

	public void setUseComment(String comment){
		this.useComment = comment;
	}
	
	public String getUseComment(){
		if(this.useComment == null){
			return "";
		}
		return this.useComment;
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
