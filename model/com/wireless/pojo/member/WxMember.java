package com.wireless.pojo.member;


public class WxMember {
	
	public static class BindBuilder{
		private final String serial;
		private final String mobile;
		private String name;
		private String birthday;
		private Member.Age age;
		private Member.Sex sex;
		
		public BindBuilder(String serial, String mobile){
			this.serial = serial;
			this.mobile = mobile;
		}
		
		public BindBuilder setName(String name){
			this.name = name;
			return this;
		}
		
		public boolean isNameChanged(){
			return this.name != null;
		}
		
		public BindBuilder setBirthday(String birthday){
			this.birthday = birthday;
			return this;
		}
		
		public boolean isBirthdayChanged(){
			return this.birthday != null;
		}
		
		public String getBirthday(){
			return this.birthday;
		}
		
		public String getSerial(){
			return this.serial;
		}
		
		public String getMobile(){
			return this.mobile;
		}
		
		public String getName(){
			return this.name;
		}
		
		public Member.Age getAge(){
			return this.age;
		}
		
		public BindBuilder setAge(Member.Age age){
			this.age = age;
			return this;
		}
		
		public boolean isAgeChanged(){
			return this.age != null;
		}
		
		public boolean isSexChanged(){
			return this.sex != null;
		}
		
		public BindBuilder setSex(Member.Sex sex){
			this.sex = sex;
			return this;
		}
		
		public Member.Sex getSex(){
			return this.sex;
		}
	}
	
	public static class InsertBuilder{
		private final String serial;
		public InsertBuilder(String serial){
			this.serial = serial;
		}
		
		public WxMember build(){
			return new WxMember(this);
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
	private int fansAmount;
	
	private WxMember(InsertBuilder builder){
		setWeixinMemberSerial(builder.serial);
		setStatus(Status.BOUND);
	}
	
	public void setFansAmount(int fansAmount){
		this.fansAmount = fansAmount;
	}
	
	public int getFansAmount(){
		return this.fansAmount;
	}
	
	public WxMember(int card){
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
		if(obj == null || !(obj instanceof WxMember)){
			return false;
		}else{
			return card == ((WxMember)obj).card;
		}
	}
	
	@Override
	public String toString(){
		return Integer.toString(this.card);
	}
}
