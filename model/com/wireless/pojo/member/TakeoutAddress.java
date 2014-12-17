package com.wireless.pojo.member;

public class TakeoutAddress {

	public static class InsertBuilder{
		private final int memberId;
		private final String address;
		
		public InsertBuilder(Member member, String address){
			this.memberId = member.getId();
			this.address = address;
		}
		
		public InsertBuilder(int memberId, String address){
			this.memberId = memberId;
			this.address = address;
		}
		
		public TakeoutAddress build(){
			return new TakeoutAddress(this);
		}
		
	}
	
	private int id;
	private int memberId;
	private String address;
	private long lastUsed;
	
	private TakeoutAddress(InsertBuilder builder){
		setMemberId(builder.memberId);
		setAddress(builder.address);
	}
	
	public TakeoutAddress(int id){
		this.id = id;
	}
	
	public void setId(int id){
		this.id = id;
	}
	
	public int getId(){
		return this.id;
	}
	
	public int getMemberId() {
		return memberId;
	}
	
	public void setMemberId(int memberId) {
		this.memberId = memberId;
	}
	
	public String getAddress() {
		if(address == null){
			return "";
		}
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public long getLastUsed() {
		return lastUsed;
	}
	
	public void setLastUsed(long lastUsed) {
		this.lastUsed = lastUsed;
	}
	
}
