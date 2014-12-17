package com.wireless.pojo.promotion;

import java.util.Collections;
import java.util.List;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DateRange;
import com.wireless.pojo.member.Member;
import com.wireless.pojo.oss.OssImage;
import com.wireless.pojo.util.SortedList;

public class Promotion implements Jsonable{

	public static class CreateBuilder{
		private final DateRange range;
		private final String title;
		private final String body;
		private final String entire;
		private final Rule rule;
		private int point;
		private Type type = Type.NORMAL;
		private final CouponType.InsertBuilder typeBuilder;
		private final SortedList<Member> members = SortedList.newInstance();
		
		private CreateBuilder(String title, DateRange range, String body, Rule type, CouponType.InsertBuilder typeBuilder, String entire){
			this.title = title;
			this.range = range;
			this.body = body;
			this.entire = entire;
			this.rule = type;
			this.typeBuilder = typeBuilder;
		}
		
		public static CreateBuilder newInstance(String title, DateRange range, String body, Rule rule, CouponType.InsertBuilder typeBuilder, String entire){
			if(rule == Rule.DISPLAY_ONLY){
				throw new IllegalArgumentException("【" + Rule.DISPLAY_ONLY.desc + "】类型不能创建优惠券");
			}
			CreateBuilder instance = new CreateBuilder(title, range, body, rule, typeBuilder, entire);
			return instance;
		}
		
		public static CreateBuilder newInstance4Display(String title, DateRange range, String body, String entire){
			CreateBuilder instance = new CreateBuilder(title, range, body, Rule.DISPLAY_ONLY, new CouponType.InsertBuilder(title, 0, range.getEndingTime()), entire);
			return instance;
		}

		public static CreateBuilder newInstance4Welcome(String title, DateRange range, String body, Rule rule, CouponType.InsertBuilder typeBuilder, String entire){
			CreateBuilder instance = new CreateBuilder(title, range, body, rule, typeBuilder, entire);
			instance.type = Type.WELCOME;
			return instance;
		}
		
		public static CreateBuilder newInstance4Welcome(String title, DateRange range, String body, String entire){
			CreateBuilder instance = newInstance4Display(title, range, body, entire);
			instance.type = Type.WELCOME;
			return instance;
		}
		
		public CreateBuilder setPoint(int point){
			if(point <= 0){
				throw new IllegalArgumentException("积分条件不能小于0");
			}
			this.point = point;
			return this;
		}
		
		public CreateBuilder addMember(int memberId){
			Member member = new Member(memberId);
			if(!members.contains(member)){
				members.add(member);
			}
			return this;
		}
		
		public List<Member> getMembers(){
			return Collections.unmodifiableList(members);
		}
		
		public CouponType.InsertBuilder getTypeBuilder(){
			return this.typeBuilder;
		}
		
		public Promotion build(){
			return new Promotion(this);
		}
	}
	
	public static class UpdateBuilder{
		private final int id;
		private DateRange range;
		private String title;
		private String body;
		private String entire;
		private int point = -1;
		private CouponType.UpdateBuilder typeBuilder;
		private List<Member> members;
		
		public UpdateBuilder(int id){
			this.id = id;
		}
		
		public UpdateBuilder setTitle(String title){
			this.title = title;
			return this;
		}
		
		public boolean isTitleChanged(){
			return this.title != null;
		}
		
		public UpdateBuilder setBody(String body, String entire){
			this.body = body;
			this.entire = entire;
			return this;
		}
		
		public boolean isBodyChanged(){
			return this.body != null;
		}
		
		public UpdateBuilder setRange(DateRange range){
			this.range = range;
			return this;
		}
		
		public boolean isRangeChanged(){
			return this.range != null;
		}
		
		public UpdateBuilder setPoint(int point){
			this.point = point;
			return this;
		}
		
		public boolean isPointChanged(){
			return this.point != -1;
		}
		
		public UpdateBuilder addMember(int memberId){
			if(members == null){
				 members = SortedList.newInstance();
			}
			Member member = new Member(memberId);
			if(!members.contains(member)){
				members.add(member);
			}
			return this;
		}
		
		public UpdateBuilder setAllMember(){
			if(members == null){
				members = SortedList.newInstance();
			}else{
				members.clear();
			}
			return this;
		}
		
		public boolean isMemberChanged(){
			return members != null;
		}
		
		public List<Member> getMembers(){
			if(members != null){
				return Collections.unmodifiableList(members);
			}else{
				return Collections.emptyList();
			}
		}
		
		public UpdateBuilder setCouponTypeBuilder(CouponType.UpdateBuilder builder){
			this.typeBuilder = builder;
			return this;
		}
		
		public CouponType.UpdateBuilder getCouponTypeBuilder(){
			return this.typeBuilder;
		}
		
		public boolean isCouponTypeChanged(){
			return this.typeBuilder != null;
		}
		
		public Promotion build(){
			return new Promotion(this);
		}
	}
	
	public static enum Rule{
		DISPLAY_ONLY(1, "只展示"),
		FREE(2, "免费领取"),
		ONCE(3, "单次积分符合条件领取"),
		TOTAL(4, "累计积分符合条件领取");
		
		private final int val;
		private final String desc;
		Rule(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public static Rule valueOf(int val){
			for(Rule type : values()){
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
	
	public static enum Type{
		
		NORMAL(1, "normal"),	// 普通活动
		WELCOME(2, "welcome");  // 欢迎活动
		
		private final int val;
		private final String desc;
		
		private Type(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		@Override
		public String toString(){
			return desc;
		}
		
		public static Type valueOf(int val){
			for(Type type : values()){
				if(type.getVal() == val){
					return type;
				}
			}
			throw new IllegalArgumentException("The type type(val = " + val + ") passed is invalid.");
		}
		
		public int getVal(){
			return this.val;
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
	
	public static enum Oriented{
		ALL(1, "全部会员"),
		SPECIFIC(2, "特定会员");
		
		private final int val;
		private final String desc;
		
		Oriented(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public static Oriented valueOf(int val){
			for(Oriented oriented : values()){
				if(oriented.val == val){
					return oriented;
				}
			}
			throw new IllegalArgumentException("The oriented (val = " + val + ") passed is invalid.");
		}
		
		public int getVal(){
			return val;
		}
		
		public String getDesc(){
			return desc;
		}
		
		@Override
		public String toString(){
			return this.desc;
		}
	}
	
	private int id;
	private int restaurantId;
	private long createDate;
	private DateRange dateRange;;
	private String title;
	private String body;
	private String entire;
	private CouponType couponType;
	private Status status = Status.CREATED;
	private Rule rule = Rule.FREE;
	private Type type = Type.NORMAL;
	private Oriented oriented;
	private int point;
	private OssImage image;
	
	private Promotion(CreateBuilder builder){
		this.createDate = System.currentTimeMillis();
		this.dateRange = builder.range;
		this.title = builder.title;
		this.body = builder.body;
		this.entire = builder.entire;
		this.rule = builder.rule;
		this.point = builder.point;
		if(builder.rule != Rule.DISPLAY_ONLY){
			this.couponType = builder.typeBuilder.build();
		}
		if(builder.members.isEmpty() || builder.type == Type.WELCOME){
			this.oriented = Oriented.ALL;
		}else{
			this.oriented = Oriented.SPECIFIC;
		}
		this.type = builder.type;
		this.status = Status.CREATED;
	}
	
	private Promotion(UpdateBuilder builder){
		this.id = builder.id;
		if(builder.isRangeChanged()){
			this.dateRange = builder.range;
		}
		if(builder.isTitleChanged()){
			this.title = builder.title;
		}
		if(builder.isBodyChanged()){
			this.body = builder.body;
			this.entire = builder.entire;
		}
		if(builder.isPointChanged()){
			this.point = builder.point;
		}
		if(builder.isCouponTypeChanged()){
			this.couponType = builder.typeBuilder.build();
		}
		if(builder.isMemberChanged()){
			if(builder.members.isEmpty()){
				this.oriented = Oriented.ALL;
			}else{
				this.oriented = Oriented.SPECIFIC;
			}
		}
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
	
	public void setEntire(String entire){
		this.entire = entire;
	}
	
	public String getEntire(){
		if(entire == null){
			return "";
		}
		return this.entire;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}
	
	public Rule getRule() {
		return rule;
	}
	
	public void setRule(Rule rule) {
		this.rule = rule;
	}
	
	public void setType(Type type){
		this.type = type;
	}
	
	public Type getType(){
		return this.type;
	}
	
	public Oriented getOriented(){
		return this.oriented;
	}
	
	public void setOriented(Oriented oriented){
		this.oriented = oriented;
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
	
	public void setImage(OssImage image){
		this.image = image;
	}
	
	public OssImage getImage(){
		return this.image;
	}
	
	public boolean hasImage(){
		return this.image != null;
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

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.id);
		jm.putInt("rid", this.restaurantId);
		jm.putString("promotionBeginDate", this.dateRange != null ? this.dateRange.getOpeningFormat() : "");
		jm.putString("promotionEndDate", this.dateRange != null ? this.dateRange.getEndingFormat() : "");
		jm.putString("title", this.title);
		jm.putString("body", this.body);
		jm.putString("entire", this.entire);
		jm.putInt("point", this.point);
		jm.putInt("status", this.status.getVal());
		jm.putInt("pType", this.rule.getVal());
		jm.putInt("oriented", this.oriented.getVal());
		jm.putJsonable("coupon", this.couponType, 0);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
}
