package com.wireless.pojo.client;

public class MemberCard {
	public static final int STATUS_NORMAL = 0;
	public static final int STATUS_LOST = 1;
	public static final int STATUS_DISABLE = 2;
	public static final String OPERATION_INSERT = "添加会员卡资料.";
	public static final String OPERATION_UPDATE = "修改会员卡资料.";
	public static final String OPERATION_LOST = "挂失会员卡.";
	public static final String OPERATION_DISABLE = "禁用会员卡.";
	private int id;
	private int restaurantID;
	private String aliasID;
	private long lastModDate;
	private int lastStaffID;
	private String comment;
	private int status = MemberCard.STATUS_NORMAL; 
	
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
		this.aliasID = aliasID;
	}
	public long getLastModDate() {
		return lastModDate;
	}
	public void setLastModDate(long lastModDate) {
		this.lastModDate = lastModDate;
	}
	public int getLastStaffID() {
		return lastStaffID;
	}
	public void setLastStaffID(int lastStaffID) {
		this.lastStaffID = lastStaffID;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
}
