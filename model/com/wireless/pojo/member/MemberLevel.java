package com.wireless.pojo.member;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;

public class MemberLevel implements Jsonable{

	private int id;
	private int restaurantId;
	private int levelId;
	private int pointThreshold;
	private MemberType memberType;
	
	public MemberLevel(int id){
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
		this.memberType.setId(typeId);
	}
	
	public void setMemberTypeName(String name){
		this.memberType.setName(name);
	}


	public static class Builder{
		protected MemberLevel data;
		public Builder(){
			data = new MemberLevel(0);
		}
	
		public MemberLevel build(){
			return data;
		}
		
	}
	
	public static class InsertBuilder extends Builder{
		public InsertBuilder(int pointThreshold, int memberTypeId){
			data.setPointThreshold(pointThreshold);
			data.getMemberType().setId(memberTypeId);	
		}
		public InsertBuilder setRestaurantId(int restaurantId){
			data.setRestaurantId(restaurantId);
			return this;
		}
	}
	
	public static class UpdateBuilder extends Builder{
		
		public UpdateBuilder(int id){
			data.setPointThreshold(-1);
			data.setId(id);
		}
		
		public UpdateBuilder setPointThreshold(int pointThreshold){
			data.setPointThreshold(pointThreshold);
			return this;
		}
		
		public UpdateBuilder setMemberTypeId(int memberTypeId){
			data.getMemberType().setId(memberTypeId);	
			return this;
		}
		
		public boolean isMemberTypeIdChange(){
			return data.memberType != null;
		}
		
		public boolean isPointThresholdChange(){
			return data.pointThreshold >= 0;
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
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putInt("id", this.id);
		jm.putInt("restaurantId", this.restaurantId);
		jm.putInt("levelId", this.levelId);
		jm.putInt("pointThreshold", this.pointThreshold);
		jm.putInt("memberTypeId", this.memberType.getId());
		jm.putString("memberTypeName", this.memberType.getName());
		jm.putString("desc", this.getMemberType().getDesc());
		return jm;
	}
	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
	

}
