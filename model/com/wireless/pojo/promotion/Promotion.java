package com.wireless.pojo.promotion;

import java.text.ParseException;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.billStatistics.DateRange;
import com.wireless.pojo.oss.OssImage;
import com.wireless.pojo.util.DateUtil;

public class Promotion implements Jsonable{

	public static class CreateBuilder{
		//private DateRange range;
		private final String title;
		private final String body;
		private final String entire;
		private Type type = Type.NORMAL;
		private final CouponType.InsertBuilder typeBuilder;
		private PromotionTrigger.InsertBuilder issueTriggerBuilder;
		private PromotionTrigger.InsertBuilder useTriggerBuilder;
		
		private CreateBuilder(String title, String body, CouponType.InsertBuilder typeBuilder, String entire){
			this.title = title;
			this.body = body;
			this.entire = entire;
			this.typeBuilder = typeBuilder;
		}
		
		public static CreateBuilder newInstance(String title, String body, CouponType.InsertBuilder typeBuilder, String entire){
			CreateBuilder instance = new CreateBuilder(title, body, typeBuilder, entire);
			return instance;
		}
		
		public CreateBuilder setIssueTrigger(PromotionTrigger.InsertBuilder builder){
			if(!builder.build().getType().isIssue()){
				throw new IllegalArgumentException("设置的必须是发券规则");
			}
			this.issueTriggerBuilder = builder;
			return this;
		}
		
		public CreateBuilder setUseTrigger(PromotionTrigger.InsertBuilder builder){
			if(!builder.build().getType().isUse()){
				throw new IllegalArgumentException("设置的必须是用券规则");
			}
			this.useTriggerBuilder = builder;
			return this;
		}
		
		public CouponType.InsertBuilder getTypeBuilder(){
			return this.typeBuilder;
		}
		
		public boolean hasIssueTrigger(){
			return this.issueTriggerBuilder != null;
		}
		
		public boolean hasUseTrigger(){
			return this.useTriggerBuilder != null;
		}
		
		public PromotionTrigger.InsertBuilder getIssueTriggerBuilder(){
			return this.issueTriggerBuilder;
		}
		
		public PromotionTrigger.InsertBuilder getUseTriggerBuilder(){
			return this.useTriggerBuilder;
		}
		
		public Promotion build(){
			return new Promotion(this);
		}
	}
	
	public static class UpdateBuilder{
		private final static DateRange UPDATE_FLAG = new DateRange(0, 0);
		private final static PromotionTrigger.InsertBuilder TRIGGER_UPDATE_FLAG = PromotionTrigger.InsertBuilder.newIssue4Free();
		private final int id;
		private DateRange range = UPDATE_FLAG;
		private String title;
		private String body;
		private String entire;
		private CouponType.UpdateBuilder typeBuilder;
		private PromotionTrigger.InsertBuilder issueTriggerBuilder = TRIGGER_UPDATE_FLAG;
		private PromotionTrigger.InsertBuilder useTriggerBuilder = TRIGGER_UPDATE_FLAG;
		
		public UpdateBuilder(int id){
			this.id = id;
		}
		
		public UpdateBuilder setIssueTrigger(PromotionTrigger.InsertBuilder builder){
			if(!builder.build().getType().isIssue()){
				throw new IllegalArgumentException("设置的必须是发券规则");
			}
			this.issueTriggerBuilder = builder;
			return this;
		}

		public boolean isIssueTriggerChanged(){
			return this.issueTriggerBuilder != TRIGGER_UPDATE_FLAG;
		}
		
		public PromotionTrigger.InsertBuilder getIssueTriggerBuilder(){
			return this.issueTriggerBuilder;
		}
		
		public UpdateBuilder setUseTrigger(PromotionTrigger.InsertBuilder builder){
			if(!builder.build().getType().isUse()){
				throw new IllegalArgumentException("设置的必须是用券规则");
			}
			this.useTriggerBuilder = builder;
			return this;
		}

		public boolean isUseTriggerChanged(){
			return this.useTriggerBuilder != TRIGGER_UPDATE_FLAG;
		}
		
		public PromotionTrigger.InsertBuilder getUseTriggerBuilder(){
			return this.useTriggerBuilder;
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
		
		public UpdateBuilder setRange(String begin, String end) throws ParseException{
			setRange(DateUtil.parseDate(begin, DateUtil.Pattern.DATE), DateUtil.parseDate(end, DateUtil.Pattern.DATE));
			return this;
		}
		
		public UpdateBuilder setRange(DateRange range){
			setRange(range.getOpeningTime(), range.getEndingTime());
			return this;
		}
		
		public UpdateBuilder setInfinitRange(){
			this.range = null;
			return this;
		}
		
		public UpdateBuilder setRange(long begin, long end){
			if(begin < 0 || end < 0){
				throw new IllegalArgumentException("活动时间不能小于0");
			}
			if(end < begin){
				throw new IllegalArgumentException("活动结束时间不能小于开始时间");
			}
			this.range = new DateRange(begin, end);
			return this;
		}
		
		public boolean isRangeChanged(){
			return this.range != UPDATE_FLAG;
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
	
	public static class PublishBuilder{
		
		private final int promotionId;
		private int condId;
		private long finishDate;
		
		public PublishBuilder(int promotionId){
			this.promotionId = promotionId;
		}
		
		public PublishBuilder setFinishDate(long finishDate){
			this.finishDate = finishDate;
			return this;
		}
		
		public PublishBuilder setFinishDate(String finishDate){
			this.finishDate = DateUtil.parseDate(finishDate);
			return this;
		}
		
		public int getCondId(){
			return this.condId;
		}
		
		public Promotion build(){
			return new Promotion(this);
		}
		
	}
	
	public static enum Rule{
		DISPLAY_ONLY(1, "只展示"),
		FREE(2, "免费领取");
		
		private final int val;
		private final String desc;
		Rule(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public static Rule valueOf(int val){
			for(Rule rule : values()){
				if(rule.val == val){
					return rule;
				}
			}
			throw new IllegalArgumentException("The rule(val = " + val + ") is invalid.");
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
		//WELCOME(2, "welcome");  // 欢迎活动
		;
		
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
	
//	public static enum Trigger implements Jsonable{
//		WX_SUBSCRIBE(1, "微信关注");
//		
//		Trigger(int val, String desc){
//			this.val = val;
//			this.desc = desc;
//		}
//		
//		private final int val;
//		private final String desc;
//		
//		public static Trigger valueOf(int val){
//			for(Trigger type : values()){
//				if(type.val == val){
//					return type;
//				}
//			}
//			throw new IllegalArgumentException("The trigger type(val = " + val + ") passed is invalid.");
//		}
//		
//		public int getVal(){
//			return this.val;
//		}
//		
//		@Override
//		public String toString(){
//			return this.desc;
//		}
//
//		@Override
//		public JsonMap toJsonMap(int flag) {
//			JsonMap jm = new JsonMap();
//			jm.putInt("triggerType", this.val);
//			jm.putString("triggerText", this.desc);
//			return jm;
//		}
//
//		@Override
//		public void fromJsonMap(JsonMap jm, int flag) {
//			
//		}
//	}
	
	private int id;
	private int restaurantId;
	private long createDate;
	private DateRange dateRange;;
	private String title;
	private String body;
	private String entire;
	private CouponType couponType;
	private Rule rule = Rule.FREE;
	private Type type = Type.NORMAL;
	private PromotionTrigger issueTrigger;
	private PromotionTrigger useTrigger;
	
	private OssImage image;
	
	
	private Promotion(CreateBuilder builder){
		this.createDate = System.currentTimeMillis();
		//this.dateRange = builder.range;
		this.title = builder.title;
		this.body = builder.body;
		this.entire = builder.entire;
		this.couponType = builder.typeBuilder.build();
		this.type = builder.type;
		if(builder.issueTriggerBuilder != null){
			this.issueTrigger = builder.issueTriggerBuilder.build();
		}
		if(builder.useTriggerBuilder != null){
			this.useTrigger = builder.useTriggerBuilder.build();
		}
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
		if(builder.isCouponTypeChanged()){
			this.couponType = builder.typeBuilder.build();
		}
		if(builder.isIssueTriggerChanged() && builder.issueTriggerBuilder != null){
			this.issueTrigger = builder.issueTriggerBuilder.build();
		}
	}
	
	private Promotion(PublishBuilder builder){
		this.id = builder.promotionId;
		this.dateRange = new DateRange(0, builder.finishDate);
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
	
	public boolean hasDateRange(){
		return this.dateRange != null;
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
		if(this.dateRange != null){
			long now = System.currentTimeMillis();
			if(this.dateRange.getEndingTime() == 0){
				return Status.CREATED;
			}else if(this.dateRange.getEndingTime() > now){
				return Status.PROGRESS;
			}else{
				return Status.FINISH;
			}
		}else{
			return Status.CREATED;
		}
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
	
	public CouponType getCouponType(){
		return this.couponType;
	}
	
	public void setCouponType(CouponType couponType){
		this.couponType = couponType;
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

	public void setUseTrigger(PromotionTrigger trigger){
		this.useTrigger = trigger;
	}
	
	public PromotionTrigger getUseTrigger(){
		return this.useTrigger;
	}
	
	public boolean hasUseTrigger(){
		return this.useTrigger != null;
	}
	
	public void setIssueTrigger(PromotionTrigger trigger){
		this.issueTrigger = trigger;
	}
	
	public PromotionTrigger getIssueTrigger(){
		return this.issueTrigger;
	}
	
	public boolean hasIssueTrigger(){
		return this.issueTrigger != null;
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
		jm.putString("image", this.getImage() != null ? this.getImage().getObjectUrl() : "http://digie-image-real.oss.aliyuncs.com/nophoto.jpg");
		jm.putInt("status", this.getStatus().getVal());
		jm.putInt("pType", this.rule.getVal());
		jm.putInt("rule", this.rule.getVal());
		jm.putJsonable("coupon", this.couponType, 0);
		jm.putJsonable("issueTrigger", this.issueTrigger, 0);
		jm.putJsonable("useTrigger", this.useTrigger, 0);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	
}
