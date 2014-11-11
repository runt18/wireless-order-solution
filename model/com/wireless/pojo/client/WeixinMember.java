package com.wireless.pojo.client;


public class WeixinMember {
	
	public static class BindBuilder{
		private final String serial;
		private final String mobile;
		
		public BindBuilder(String serial, String mobile){
			this.serial = serial;
			this.mobile = mobile;
		}
		
		public String getSerial(){
			return this.serial;
		}
		
		public String getMobile(){
			return this.mobile;
		}
	}
	
	public static class InsertBuilder{
		private final String serial;
		public InsertBuilder(String serial){
			this.serial = serial;
		}
		
		public WeixinMember build(){
			return new WeixinMember(this);
		}
	}
	
	public static enum Status{
		INTERESTED(1, "已关注"),
		BOUND(2, "已绑定"),
		CANCELED(3, "取消关注");
		
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
			throw new IllegalArgumentException("The val(" + val + ") is invalid.");
		}
		
		public int getVal(){
			return this.val;
		}
		
		public String getDesc(){
			return this.desc;
		}
		
		@Override
		public String toString(){
			return "Status(val=" + val + ",desc=" + desc + ")";
		}
	}
	
	private int card;
	private int restaurantId;
	private int memberId;
	private long interestedDate;
	private long bindDate;
	private String weixinMemberSerial;
	private Status status;
	
	private WeixinMember(InsertBuilder builder){
		setWeixinMemberSerial(builder.serial);
		setStatus(Status.BOUND);
	}
	
	public WeixinMember(int card){
		this.card = card;
	}
	
	public int getCard() {
		return card;
	}
	
	public void setCard(int card) {
		this.card = card;
	}
	
	public int getRestaurantId() {
		return restaurantId;
	}
	
	public void setRestaurantId(int restaurantId) {
		this.restaurantId = restaurantId;
	}
	
	public int getMemberId() {
		return memberId;
	}
	
	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}
	
	public long getInterestedDate() {
		return interestedDate;
	}
	
	public void setInterestedDate(long interestedDate) {
		this.interestedDate = interestedDate;
	}
	
	public long getBindDate() {
		return bindDate;
	}
	
	public void setBindDate(long bindDate) {
		this.bindDate = bindDate;
	}
	
	public String getSerial() {
		return weixinMemberSerial;
	}
	
	public void setWeixinMemberSerial(String weixinMemberSerial) {
		this.weixinMemberSerial = weixinMemberSerial;
	}
	
	public void setStatus(Status status){
		this.status = status;
	}
	
	public Status getStatus(){
		return this.status;
	}
	
	@Override
	public int hashCode(){
		return card * 31 + 17;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof WeixinMember)){
			return false;
		}else{
			return card == ((WeixinMember)obj).card;
		}
	}
	
	@Override
	public String toString(){
		return Integer.toString(this.card);
	}
}
