package com.wireless.pojo.member;

public class TakeoutAddress {

	public static class InsertBuilder{
		private final int memberId;
		private final String address;
		private final String tele;
		private final String name;
		
		public InsertBuilder(Member member, String address, String tele, String name){
			this.memberId = member.getId();
			this.address = address;
			this.tele = tele;
			this.name = name;
		}
		
		public InsertBuilder(int memberId, String address, String tele, String name){
			this.memberId = memberId;
			this.address = address;
			this.tele = tele;
			this.name = name;
		}
		
		public TakeoutAddress build(){
			return new TakeoutAddress(this);
		}
		
	}
	
	private int id;
	private int memberId;
	private String address;
	private String tele;
	private String name;
	private long lastUsed;
	
	private TakeoutAddress(InsertBuilder builder){
		setMemberId(builder.memberId);
		setAddress(builder.address);
		setTele(builder.tele);
		setName(builder.name);
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
	
	public String getTele(){
		if(this.tele == null){
			return "";
		}
		return tele;
	}
	
	public void setTele(String tele){
		this.tele = tele;
	}
	
	public String getName(){
		if(this.name == null){
			return "";
		}
		return this.name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public long getLastUsed() {
		return lastUsed;
	}
	
	public void setLastUsed(long lastUsed) {
		this.lastUsed = lastUsed;
	}
	
}
