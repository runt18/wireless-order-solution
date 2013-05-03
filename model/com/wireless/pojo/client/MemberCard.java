package com.wireless.pojo.client;

public class MemberCard {
	
	public enum Status{
		NORMAL(0, "普通"),
		LOST(1, "挂失"),
		DISABLE(2, "禁用"),
		ACTIVE(3, "活动");
		
		private int value;
		private String name;
		Status(int value, String name){
			this.value = value;
			this.name = name;
		}
		public int getValue() {
			return value;
		}
		public String getName() {
			return name;
		}
		public static Status valueOf(int value){
			for(Status temp : values()){
				if(temp.getValue() == value){
					return temp;
				}
			}
			throw new IllegalArgumentException("The status type(val = " + value + ") passed is invalid.");
		}
		@Override
		public String toString() {
			return this.name();
		}
	}
	
	public static final String OPERATION_INSERT = "添加会员卡资料.";
	public static final String OPERATION_UPDATE = "修改会员卡资料.";
	public static final String OPERATION_LOST = "挂失会员卡.";
	public static final String OPERATION_DISABLE = "禁用会员卡.";
	public static final String OPERATION_RESET = "重置会员卡信息.";
	public static final String OPERATION_ENABLE = "启用会员卡.";
	public static final String OPERATION_ACTIVE = "设置状态为正在使用.";
	public static final String OPERATION_CHANGE = "更换会员卡.";
	private int id;
	private int restaurantID;
	private String aliasID;
	private long lastModDate;
	private long lastStaffID;
	private String comment;
	private Status status = MemberCard.Status.NORMAL; 
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getRestaurantID() {
		return restaurantID;
	}
	public void setRestaurantID(int restaurantID) {
		this.restaurantID = restaurantID;
	}
	public String getAliasID() {
		return aliasID;
	}
	public void setAliasID(String aliasID) {
		this.aliasID = aliasID != null ? aliasID.trim() : null;
	}
	public long getLastModDate() {
		return lastModDate;
	}
	public void setLastModDate(long lastModDate) {
		this.lastModDate = lastModDate;
	}
	public long getLastStaffID() {
		return lastStaffID;
	}
	public void setLastStaffID(long lastStaffID) {
		this.lastStaffID = lastStaffID;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public int getStatusValue() {
		return status.getValue();
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	public void setStatus(int status) {
		this.status = MemberCard.Status.valueOf(status);
	}
}
