package com.wireless.pojo.promotion;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class PromotionTrigger implements Jsonable {

	public static class InsertBuilder{
		private IssueRule issueRule;
		private UseRule useRule;
		private Type type;
		private int extra;
		private int promotionId;
		
		private InsertBuilder(Type type, IssueRule trigger, int extra){
			this.issueRule = trigger;
			this.type = type;
			this.extra = extra;
		}

		private InsertBuilder(Type type, UseRule trigger, int extra){
			this.useRule = trigger;
			this.type = type;
			this.extra = extra;
		}
		
		//微信关注发券
		public static InsertBuilder newIssue4Wx(){
			return new InsertBuilder(Type.ISSUE, IssueRule.WX_SUBSCRIBE, 0);
		}
		
		//单次消费满发券
		public static InsertBuilder newIssue4SingleExceed(int exceed){
			return new InsertBuilder(Type.ISSUE, IssueRule.SINGLE_EXCEED, exceed);
		}
		
		//免费发券
		public static InsertBuilder newIssue4Free(){
			return new InsertBuilder(Type.ISSUE, IssueRule.FREE, 0);
		}

		//免费用券
		public static InsertBuilder newUse4Free(){
			return new InsertBuilder(Type.USE, UseRule.FREE, 0);
		}
		
		//单次消费满用券
		public static InsertBuilder newUse4SingleExceed(int exceed){
			return new InsertBuilder(Type.USE, UseRule.SINGLE_EXCEED, exceed);
		}
		
		public InsertBuilder setPromotion(Promotion promotion){
			this.promotionId = promotion.getId();
			return this;
		}

		public InsertBuilder setPromotion(int promotionId){
			this.promotionId = promotionId;
			return this;
		}
		
		public PromotionTrigger build(){
			return new PromotionTrigger(this);
		}
	}
	
	public static class UpdateBuilder{
		private int id;
		private IssueRule issueRule;
		private UseRule useRule;
		private Type type;
		private int extra;
		
		public UpdateBuilder setId(int id){
			this.id = id;
			return this;
		}
		
		public UpdateBuilder setIssueRule(IssueRule rule){
			this.issueRule = rule;
			return this;
		}
		
		public boolean isIssueRuleChanged(){
			return this.issueRule != null;
		}
		
		public UpdateBuilder setUseRule(UseRule rule){
			this.useRule = rule;
			return this;
		}
		
		public boolean isUseRuleChanged(){
			return this.useRule != null;
		}
		
		public UpdateBuilder setType(Type type){
			this.type = type;
			return this;
		}
		
		public boolean isTypeChanged(){
			return this.type != null;
		}
		
		public UpdateBuilder setExtra(int extra){
			this.extra = extra;
			return this;
		}
		
		public boolean isExtraChanged(){
			return this.extra != 0;
		}
		
		public PromotionTrigger build(){
			return new PromotionTrigger(this);
		}
		
	}
	
	public static enum IssueRule{
		FREE(1, "免费发券"),
		SINGLE_EXCEED(2, "单次消费满"),
		WX_SUBSCRIBE(3, "微信关注")
		;
		private final int val;
		private final String desc;
		
		IssueRule(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public int getVal(){
			return this.val;
		}
		
		public static IssueRule valueOf(int val){
			for(IssueRule trigger : values()){
				if(trigger.val == val){
					return trigger;
				}
			}
			throw new IllegalArgumentException("The issue trigger (val = " + val + ") is invalid.");
		}
		
		public boolean isSingleExceed(){
			return this == SINGLE_EXCEED;
		}
		
		public boolean isWxSubscribe(){
			return this == WX_SUBSCRIBE;
		}
		
		@Override
		public String toString(){
			return this.desc;
		}
	}
	
	public static enum UseRule{
		FREE(1, "免费使用"),
		SINGLE_EXCEED(2, "单次消费满"),
		;
		private final int val;
		private final String desc;
		
		UseRule(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		public int getVal(){
			return this.val;
		}
		
		public static UseRule valueOf(int val){
			for(UseRule trigger : values()){
				if(trigger.val == val){
					return trigger;
				}
			}
			throw new IllegalArgumentException("The use trigger (val = " + val + ") is invalid.");
		}
		
		public boolean isSingleExceed(){
			return this == SINGLE_EXCEED;
		}
		
		@Override
		public String toString(){
			return this.desc;
		}
	}
	
	public static enum Type{
		ISSUE(1, "发券规则"),
		USE(2, "用券规则")
		;
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
			throw new IllegalArgumentException("The type (val = " + val + ") is invalid.");
		}
		
		public int getVal(){
			return this.val;
		}
		
		public boolean isUse(){
			return this == USE;
		}
		
		public boolean isIssue(){
			return this == ISSUE;
		}
		
		@Override
		public String toString(){
			return this.desc;
		}
	}
	
	private int id;
	private int restaurantId;
	private int promotionId;
	private IssueRule issueRule;
	private UseRule useRule;
	private int extra;
	private Type type;
	
	private PromotionTrigger(InsertBuilder builder){
		this.type = builder.type;
		this.issueRule = builder.issueRule;
		this.useRule = builder.useRule;
		this.extra = builder.extra;
		this.promotionId = builder.promotionId;
	}

	private PromotionTrigger(UpdateBuilder builder){
		this.id = builder.id;
		this.type = builder.type;
		this.issueRule = builder.issueRule;
		this.useRule = builder.useRule;
		this.extra = builder.extra;
	}
	
	public PromotionTrigger(int id){
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public void setRestaurantId(int restaurantId){
		this.restaurantId = restaurantId;
	}
	
	public int getRestaurantId(){
		return this.restaurantId;
	}
	
	public int getPromotionId() {
		return promotionId;
	}
	
	public void setPromotionId(int promotionId) {
		this.promotionId = promotionId;
	}
	
	public IssueRule getIssueRule() {
		return issueRule;
	}
	
	public void setIssueRule(IssueRule issueRule) {
		this.issueRule = issueRule;
	}
	
	public UseRule getUseRule() {
		return useRule;
	}
	
	public void setUseRule(UseRule useRule) {
		this.useRule = useRule;
	}
	
	public int getExtra() {
		return extra;
	}
	
	public void setExtra(int extra) {
		this.extra = extra;
	}
	
	public Type getType() {
		return type;
	}
	
	public void setType(Type type) {
		this.type = type;
	}
	
	@Override
	public int hashCode(){
		return this.id * 31 + 17;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof PromotionTrigger)){
			return false;
		}else{
			return this.id == ((PromotionTrigger)obj).id;
		}
	}
	
	@Override
	public String toString(){
		return "type : " + this.type.desc +
			   ",trigger : " + (this.type == Type.ISSUE ? this.issueRule.desc : this.useRule.desc);
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.id);
		jm.putInt("promotionId", this.promotionId);
		jm.putInt("type", this.type.val);
		jm.putString("typeText", this.type.desc);
		if(this.useRule != null){
			jm.putInt("useRule", this.useRule.val);
			jm.putString("useRuleText", this.useRule.desc);
		}
		if(this.issueRule != null){
			jm.putInt("issueRule", this.issueRule.val);
			jm.putString("issueRuleText", this.issueRule.desc);
		}
		jm.putInt("extra", this.extra);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jm, int flag) {
		
	}
}
