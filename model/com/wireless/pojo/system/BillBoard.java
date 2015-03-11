package com.wireless.pojo.system;

import java.text.ParseException;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.util.DateUtil;

public class BillBoard implements Jsonable {
	
	public enum Type{
		SYSTEM(1, "系统公告"),
		RESTAURANT(2, "餐厅通知");
		
		Type(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		private int val;
		private String desc;
		
		public int getVal(){
			return val;
		}
		
		public String getDesc(){
			return desc;
		}
		
		public static Type valueOf(int val){
			for(Type type : values()){
				if(type.val == val){
					return type;
				}
			}
			throw new IllegalArgumentException("The val(" + val + ") is invalid.");
		}
		
		@Override
		public String toString(){
			return this.desc;
		}
	}
	
	public static enum Status{
		CREATED(1, "已创建"),
		READ(2, "已读");
		
		Status(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		private final int val;
		private final String desc;
		
		public static Status valueOf(int val){
			for(Status status : values()){
				if(status.val == val){
					return status;
				}
			}
			throw new IllegalArgumentException("The status(" + val + ") is invalid.");
		}
		
		public int getVal(){
			return this.val;
		}
		
		@Override
		public String toString(){
			return this.desc;
		}
	}
	
	public static class InsertBuilder{
		private final String title;
		private final long expired;
		private final int restaurantId;
		private String body;
		private final Type type;
		
		public static InsertBuilder build4Restaurant(String title, int restaurantId, String expired) throws ParseException{
			return new InsertBuilder(title, expired, restaurantId);
		}
		
		public static InsertBuilder build4System(String title, String expired) throws ParseException{
			return new InsertBuilder(title, expired);
		}
		
		private InsertBuilder(String title, String expired, int restaurantId) throws ParseException{
			this.title = title;
			this.expired = DateUtil.parseDate(expired, DateUtil.Pattern.DATE_TIME);
			this.restaurantId = restaurantId;
			this.type = Type.RESTAURANT;
		}
		
		private InsertBuilder(String title, String expired) throws ParseException{
			this.title = title;
			this.expired = DateUtil.parseDate(expired, DateUtil.Pattern.DATE_TIME);
			this.restaurantId = 0;
			this.type = Type.SYSTEM;
		}

		public InsertBuilder setBody(String body){
			this.body = body;
			return this;
		}
		
		public BillBoard build(){
			return new BillBoard(this);
		}
	}
	
	public static class UpdateBuilder{
		private final int id;
		private String title;
		private String body;
		private long expired;
		private Status status;
		
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
		
		public UpdateBuilder setBody(String body){
			this.body = body;
			return this;
		}
		
		public boolean isBodyChanged(){
			return this.body != null;
		}
		
		public UpdateBuilder setExpired(String expired) throws ParseException{
			this.expired = DateUtil.parseDate(expired, DateUtil.Pattern.DATE_TIME);
			return this;
		}
		
		public boolean isExpiredChanged(){
			return this.expired != 0;
		}
		
		public UpdateBuilder setStatus(Status status){
			this.status = status;
			return this;
		}
		
		public boolean isStatusChanged(){
			return this.status != null;
		}
		
		public BillBoard build(){
			return new BillBoard(this);
		}
	}
	
	
	private int id;
	private String title;
	private String body;
	private long created;
	private long expired;
	private Type type;
	private Status status;
	private int restaurantId;
	
	private BillBoard(InsertBuilder builder){
		this.title = builder.title;
		this.created = System.currentTimeMillis();
		this.expired = builder.expired;
		this.body = builder.body;
		this.status = Status.CREATED;
		this.type = builder.type;
		this.restaurantId = builder.restaurantId;
	}
	
	private BillBoard(UpdateBuilder builder){
		this.id = builder.id;
		if(builder.isTitleChanged()){
			this.title = builder.title;
		}
		if(builder.isBodyChanged()){
			this.body = builder.body;
		}
		if(builder.isExpiredChanged()){
			this.expired = builder.expired;
		}
		if(builder.isStatusChanged()){
			this.status = builder.status;
		}
	}
	
	public BillBoard(int id){
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getTitle() {
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
	
	public long getCreated() {
		return created;
	}
	
	public void setCreated(long created) {
		this.created = created;
	}
	
	public long getExpired() {
		return expired;
	}
	
	public void setExpired(long expired) {
		this.expired = expired;
	}
	
	public boolean isExpired(){
		return System.currentTimeMillis() > this.expired;
	}
	
	public Type getType() {
		return type;
	}
	
	public void setType(Type type) {
		this.type = type;
	}

	public Status getStatus(){
		return this.status;
	}
	
	public void setStatus(Status status){
		this.status = status;
	}
	
	public int getRestaurantId() {
		return restaurantId;
	}
	
	public void setRestaurantId(int restaurantId) {
		this.restaurantId = restaurantId;
	}
	
	@Override
	public int hashCode() {
		return id * 31 + 17;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof BillBoard)){
			return false;
		}else{
			return id == ((BillBoard)obj).id;
		}
	}
	
	@Override
	public String toString() {
		return "id: " + id + ", title: " + title;
	}
	
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", id);
		jm.putString("title", title);
		jm.putString("desc", body);
		jm.putLong("created", created);
		jm.putString("createdFormat", DateUtil.format(created));
		jm.putLong("expired", expired);
		jm.putString("expiredFormat", DateUtil.format(expired));
		jm.putInt("typeVal", type.getVal());
		jm.putString("typeDesc", type.getDesc());
		jm.putInt("status", status.getVal());
		jm.putString("stautsDesc", status.toString());		
		jm.putInt("restaurant", restaurantId);
		
		return jm;
	}
	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		// TODO Auto-generated method stub
		
	}
}
