package com.wireless.pojo.member.represent;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.util.DateUtil;

public class Represent implements Jsonable {

	public static class InsertBuilder{
		private long finishDate;
		private String title;
		private String slogon;
		private String body;
		private int recommendPoint;
		private float recommendMoney;
		private int subscribePoint;
		private float subscribeMoney;
		
		public InsertBuilder setFinishDate(long finishDate){
			this.finishDate = finishDate;
			return this;
		}
		
		public InsertBuilder setTitle(String title){
			this.title = title;
			return this;
		}
		
		public InsertBuilder setSlogon(String slogon){
			this.slogon = slogon;
			return this;
		}
		
		public InsertBuilder setBody(String body){
			this.body = body;
			return this;
		}
		
		public InsertBuilder setRecommendPoint(int point){
			this.recommendPoint = point;
			return this;
		}
		
		public InsertBuilder setRecommendMoney(float money){
			this.recommendMoney = money;
			return this;
		}
		
		public InsertBuilder setSubscribePoint(int point){
			this.subscribePoint = point;
			return this;
		}
		
		public InsertBuilder setSubscribeMoney(float money){
			this.subscribeMoney = money;
			return this;
		}
		
		public Represent build(){
			return new Represent(this);
		}
	}
	
	public static class UpdateBuilder{
		private final int id;
		private long finishDate;
		private String title;
		private String slogon;
		private String body;
		private Integer recommendPoint;
		private Float recommendMoney;
		private Integer subscribePoint;
		private Float subscribeMoney;
		
		public UpdateBuilder(int id){
			this.id = id;
		}
		
		public UpdateBuilder setFinishDate(String finishDate){
			this.finishDate = DateUtil.parseDate(finishDate);
			return this;
		}
		
		public UpdateBuilder setFinishDate(long finishDate){
			this.finishDate = finishDate;
			return this;
		}
		
		public boolean isFinishDateChanged(){
			return this.finishDate != 0;
		}
		
		public UpdateBuilder setTitle(String title){
			this.title = title;
			return this;
		}
		
		public boolean isTitleChanged(){
			return this.title != null;
		}
		
		public UpdateBuilder setSlogon(String slogon){
			this.slogon = slogon;
			return this;
		}
		
		public boolean isSlogonChanged(){
			return this.slogon != null;
		}
		
		public UpdateBuilder setBody(String body){
			this.body = body;
			return this;
		}
		
		public boolean isBodyChanged(){
			return this.body != null;
		}
		
		public UpdateBuilder setRecommendPoint(int point){
			this.recommendPoint = point;
			return this;
		}
		
		public boolean isRecommendPointChanged(){
			return this.recommendPoint != null;
		}
		
		public UpdateBuilder setRecommendMoney(float money){
			this.recommendMoney = money;
			return this;
		}
		
		public boolean isRecommendMoneyChanged(){
			return this.recommendMoney != null;
		}
		
		public UpdateBuilder setSubscribePoint(int point){
			this.subscribePoint = point;
			return this;
		}
		
		public boolean isSubcribePointChanged(){
			return this.subscribePoint != null;
		}
		
		public UpdateBuilder setSubscribeMoney(float money){
			this.subscribeMoney = money;
			return this;
		}
		
		public boolean isSubscribeMoneyChanged(){
			return this.subscribeMoney != null;
		}
		
		public Represent build(){
			return new Represent(this);
		}
	}
	
	private int id;
	private long finishDate;
	private String title;
	private String slogon;
	private String body;
	private int recommendPoint;
	private float recommentMoney;
	private int subscribePoint;
	private float subscribeMoney;

	private Represent(UpdateBuilder builder){
		this.id = builder.id;
		this.finishDate = builder.finishDate;
		this.title = builder.title;
		this.slogon = builder.slogon;
		this.body = builder.body;
		this.recommendPoint = builder.recommendPoint;
		this.recommentMoney = builder.recommendMoney;
		this.subscribePoint = builder.subscribePoint;
		this.subscribeMoney = builder.subscribeMoney;
	}
	
	private Represent(InsertBuilder builder){
		this.finishDate = builder.finishDate;
		this.title = builder.title;
		this.slogon = builder.slogon;
		this.body = builder.body;
		this.recommendPoint = builder.recommendPoint;
		this.recommentMoney = builder.recommendMoney;
		this.subscribePoint = builder.subscribePoint;
		this.subscribeMoney = builder.subscribeMoney;
	}
	
	public Represent(int id){
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public long getFinishDate() {
		return finishDate;
	}
	
	public void setFinishDate(long finishDate) {
		this.finishDate = finishDate;
	}
	
	public boolean isProgress(){
		return this.finishDate > System.currentTimeMillis();
	}
	
	public String getTitle() {
		if(this.title == null){
			return "";
		}
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getSlogon() {
		if(this.slogon == null){
			return "";
		}
		return slogon;
	}
	
	public void setSlogon(String slogon) {
		this.slogon = slogon;
	}
	
	public String getBody() {
		if(this.body == null){
			return "";
		}
		return body;
	}
	
	public void setBody(String body) {
		this.body = body;
	}
	
	public int getRecommendPoint() {
		return recommendPoint;
	}
	
	public void setRecommendPoint(int recommendPoint) {
		this.recommendPoint = recommendPoint;
	}
	
	public float getRecommentMoney() {
		return recommentMoney;
	}
	
	public void setRecommentMoney(float recommentMoney) {
		this.recommentMoney = recommentMoney;
	}
	
	public int getSubscribePoint() {
		return subscribePoint;
	}
	
	public void setSubscribePoint(int subscribePoint) {
		this.subscribePoint = subscribePoint;
	}
	
	public float getSubscribeMoney() {
		return subscribeMoney;
	}
	
	public void setSubscribeMoney(float subscribeMoney) {
		this.subscribeMoney = subscribeMoney;
	}
	
	@Override
	public int hashCode(){
		return this.id * 31 + 17;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Represent)){
			return false;
		}else{
			return this.id == ((Represent)obj).id;
		}
	}
	
	@Override
	public String toString(){
		return this.id + "";
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jm, int flag) {
		
	}
	
}
