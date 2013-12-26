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
	private MemberType memberType;
	
	
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
	public MemberType getMemberType() {
		if(memberType == null){
			memberType = new MemberType(-1);
		}
		return memberType;
	}
	public void setMemberType(MemberType memberType) {
		this.memberType = memberType;
	}
	
	public void setMemberTypeId(int typeId){
		this.memberType.setTypeId(typeId);
	}
	
	public void setMemberTypeName(String name){
		this.memberType.setName(name);
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
			data.getMemberType().setTypeId(memberTypeId);	
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
			data.getMemberType().setTypeId(memberTypeId);	
			return this;
		}
		
		public boolean isMemberTypeIdChange(){
			return data.memberType != null;
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
		jm.put("memberTypeId", this.memberType.getTypeId());
		jm.put("memberTypeName", this.memberType.getName());
		return Collections.unmodifiableMap(jm);
	}
	@Override
	public List<Object> toJsonList(int flag) {
		return null;
	}
	

}
