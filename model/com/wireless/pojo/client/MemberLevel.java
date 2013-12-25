package com.wireless.pojo.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wireless.json.Jsonable;

public class MemberLevel implements Jsonable{

	private int id;
	private int restaurantId;
	private int levelId;
	private int pointThreshold;
	private int memberTypeId = -1;
	private String memberTypeName;
	
	
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
	public int getLevelId() {
		return levelId;
	}
	public void setLevelId(int levelId) {
		this.levelId = levelId;
	}
	public int getPointThreshold() {
		return pointThreshold;
	}
	public void setPointThreshold(int pointThreshold) {
		this.pointThreshold = pointThreshold;
	}
	public int getMemberTypeId() {
		return memberTypeId;
	}
	public void setMemberTypeId(int memberTypeId) {
		this.memberTypeId = memberTypeId;
	}
	public String getMemberTypeName() {
		return memberTypeName;
	}
	public void setMemberTypeName(String memberTypeName) {
		this.memberTypeName = memberTypeName;
	}
	
	public static class Builder{
		protected MemberLevel data;
		public Builder(){
			data = new MemberLevel();
		}
	
		public MemberLevel build(){
			return data;
		}
		
	}
	
	public static class InsertBuilder extends Builder{
		public InsertBuilder(int pointThreshold, int memberTypeId){
			data.setPointThreshold(pointThreshold);
			data.setMemberTypeId(memberTypeId);	
		}
		public InsertBuilder setRestaurantId(int restaurantId){
			data.setRestaurantId(restaurantId);
			return this;
		}
	}
	
	public static class UpdateBuilder extends Builder{
		public UpdateBuilder(int id){
			data.setId(id);
		}
		
		public UpdateBuilder setPointThreshold(int pointThreshold){
			data.setPointThreshold(pointThreshold);
			return this;
		}
		
		public UpdateBuilder setMemberTypeId(int memberTypeId){
			data.setMemberTypeId(memberTypeId);
			return this;
		}
		
		public boolean isMemberTypeIdChange(){
			return data.memberTypeId > 0;
		}
		
		public boolean isPointThresholdChange(){
			return data.pointThreshold > 0;
		}
	}
	
	@Override 
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof MemberLevel)){
			return false;
		}else{
			return this.id == ((MemberLevel)obj).id;
		}
	}
	@Override
	public int hashCode() {
		return id * 31 + 17;
	}
	@Override
	public String toString() {
		return "id : " + id + ", level: " + levelId;
	}
	@Override
	public Map<String, Object> toJsonMap(int flag) {
		Map<String, Object> jm = new HashMap<String, Object>();
		jm.put("id", this.id);
		jm.put("restaurantId", this.restaurantId);
		jm.put("levelId", this.levelId);
		jm.put("pointThreshold", this.pointThreshold);
		jm.put("memberTypeId", this.memberTypeId);
		jm.put("memberTypeName", this.memberTypeName);
		return Collections.unmodifiableMap(jm);
	}
	@Override
	public List<Object> toJsonList(int flag) {
		return null;
	}
	

}
