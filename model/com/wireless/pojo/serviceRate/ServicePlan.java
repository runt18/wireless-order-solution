package com.wireless.pojo.serviceRate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.regionMgr.Region;


public class ServicePlan implements Jsonable{

	public static class UpdateBuilder{
		private final int planId;
		private String name;
		private Status status;
		private final List<ServiceRate> rates = new ArrayList<ServiceRate>();
		
		public UpdateBuilder(int planId){
			this.planId = planId;
		}
		
		public UpdateBuilder setName(String name){
			this.name = name;
			return this;
		}
		
		public boolean isNameChanged(){
			return this.name != null;
		}
		
		public UpdateBuilder setStatus(Status status){
			this.status = status;
			return this;
		}
		
		public boolean isStatusChanged(){
			return this.status != null;
		}
		
		public UpdateBuilder addRate(int regionId, float rate){
			if(rate < 0 || rate > 1){
				throw new IllegalArgumentException("服务费率的范围在[0 - 1]之间");
			}
			ServiceRate sr = new ServiceRate();
			sr.setRegion(new Region((short)regionId));
			sr.setRate(rate);
			rates.add(sr);
			return this;
		}
		
		public UpdateBuilder addRate(Region region, float rate){
			if(rate < 0 || rate > 1){
				throw new IllegalArgumentException("服务费率的范围在[0 - 1]之间");
			}
			ServiceRate sr = new ServiceRate();
			sr.setRegion(region);
			sr.setRate(rate);
			rates.add(sr);
			return this;
		}
		
		public boolean isRateChanged(){
			return !this.rates.isEmpty();
		}
		
		public ServicePlan build(){
			return new ServicePlan(this);
		}
	}
	
	public static class InsertBuilder{
		private final String name;
		private Type type = Type.NORMAL;
		private Status status = Status.NORMAL;
		private float initRate;
		
		public InsertBuilder(String name){
			this.name = name;
		}
		
		public InsertBuilder setType(Type type){
			this.type = type;
			return this;
		}
		
		public InsertBuilder setStatus(Status status){
			this.status = status;
			return this;
		}
		
		public InsertBuilder setRate(float rate){
			if(rate < 0 || rate > 1){
				throw new IllegalArgumentException("服务费率的范围在[0 - 1]之间");
			}
			this.initRate = rate;
			return this;
		}
		
		public float getRate(){
			return this.initRate;
		}
		
		public ServicePlan build(){
			return new ServicePlan(this);
		}
	}
	
	public static enum Status{
		
		NORMAL(1, "普通"),	// 普通
		DEFAULT(2, "默认");	// 默认
		
		private final int val;
		private final String desc;
		
		private Status(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		@Override
		public String toString(){
			return "status(val = " + val + ", desc = " + desc + ")";
		}
		
		public static Status valueOf(int val){
			for(Status status : values()){
				if(status.val == val){
					return status;
				}
			}
			throw new IllegalArgumentException("The discount status(val = " + val + ") passed is invalid.");
		}
		
		public int getVal(){
			return this.val;
		}
		
	}
	
	public static enum Type{
		
		NORMAL(1, "normal"),	// 普通
		RESERVED(2, "reserved");// 保留
		
		private final int val;
		private final String desc;
		
		private Type(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		@Override
		public String toString(){
			return "type(val = " + val + ", desc = " + desc + ")";
		}
		
		public static Type valueOf(int val){
			for(Type type : values()){
				if(type.getVal() == val){
					return type;
				}
			}
			throw new IllegalArgumentException("The discount type(val = " + val + ") passed is invalid.");
		}
		
		public int getVal(){
			return this.val;
		}
		
	}
	
	private int planId;
	private int restaurantId;
	private String name;
	private Type type;
	private Status status;
	private final List<ServiceRate> rates = new ArrayList<ServiceRate>();
	
	public ServicePlan(int planId){
		this.planId = planId;
	}
	
	private ServicePlan(UpdateBuilder builder){
		this.planId = builder.planId;
		this.status = builder.status;
		this.name = builder.name;
		this.rates.addAll(builder.rates);
	}
	
	private ServicePlan(InsertBuilder builder){
		this.name = builder.name;
		this.status = builder.status;
		this.type = builder.type;
	}
	
	public int getPlanId() {
		return planId;
	}
	
	public void setPlanId(int planId) {
		this.planId = planId;
	}
	
	public int getRestaurantId() {
		return restaurantId;
	}
	
	public void setRestaurantId(int restaurantId) {
		this.restaurantId = restaurantId;
	}
	
	public String getName() {
		if(name == null){
			return "";
		}
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Type getType() {
		return type;
	}
	
	public boolean isReserved(){
		return this.type == Type.RESERVED;
	}
	
	public void setType(Type type) {
		this.type = type;
	}
	
	public Status getStatus(){
		return this.status;
	}
	
	public boolean isDefault(){
		return this.status == Status.DEFAULT;
	}
	
	public void setStatus(Status status){
		this.status = status;
	}
	
	public void addRate(ServiceRate rate){
		if(rate != null){
			rates.add(rate);
		}
	}
	
	public void setRates(List<ServiceRate> rates){
		if(rates != null){
			this.rates.clear();
			this.rates.addAll(rates);
		}
	}
	
	public boolean hasRates(){
		return !this.rates.isEmpty();
	}
	
	public List<ServiceRate> getRates(){
		return Collections.unmodifiableList(this.rates);
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof ServicePlan)){
			return false;
		}else{
			return this.planId == ((ServicePlan)obj).planId;
		}
	}
	
	@Override
	public int hashCode(){
		return this.planId * 17 + 31;
	}
	
	@Override
	public String toString(){
		return this.name != null ? this.name : "";
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.planId);
		jm.putString("name", this.name);
		jm.putInt("type", this.type.val);
		jm.putString("typeText", this.type.desc);
		jm.putInt("status", this.status.val);
		jm.putString("statusText", this.status.desc);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jm, int flag) {
		
	}
}
