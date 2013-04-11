package com.wireless.pojo.restaurantMgr;

import com.wireless.protocol.PRestaurant;

public class Restaurant {
	
	public static enum RecordAlive{
		NEVER_EXPIRED(0),
		THREE_MONTHS(3600 * 24 * 90),
		HALF_A_YEAR(3600 * 24 * 180),
		ONE_YEAR(3600 * 24 * 360);
		
		private final int aliveSeconds;
		
		RecordAlive(int aliveSeconds){
			this.aliveSeconds = aliveSeconds;
		}
		
		public int getSeconds(){
			return aliveSeconds;
		}
	}
	
	private int id;
	private String account;
	private String restaurantName;
	private String restaurantInfo;
	private int recordAlive;
	private String tele1;
	private String tele2;
	private String address;
	private String pwd;
	private String pwd2;
	private String pwd3;
	private String pwd4;
	private String pwd5;
	
	public final PRestaurant toProtocol(){
		PRestaurant protocolObj = new PRestaurant();
		
		protocolObj.setId(getId());
		protocolObj.setAddr(getAddress());
		protocolObj.setInfo(getRestaurantInfo());
		protocolObj.setName(getRestaurantName());
		protocolObj.setPwd(getPwd());
		protocolObj.setPwd2(getPwd2());
		protocolObj.setPwd3(getPwd3());
		protocolObj.setPwd4(getPwd4());
		protocolObj.setPwd5(getPwd5());
		protocolObj.setTele1(getTele1());
		protocolObj.setTele2(getTele2());
		
		return protocolObj;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getAccount() {
		return account;
	}
	
	public void setAccount(String account) {
		this.account = account;
	}
	
	public String getRestaurantName() {
		return restaurantName;
	}
	
	public void setRestaurantName(String restaurantName) {
		this.restaurantName = restaurantName;
	}
	
	public String getRestaurantInfo() {
		return restaurantInfo;
	}
	
	public void setRestaurantInfo(String restaurantInfo) {
		this.restaurantInfo = restaurantInfo;
	}
	
	public int getRecordAlive() {
		return recordAlive;
	}
	
	public void setRecordAlive(int recordAlive) {
		this.recordAlive = recordAlive;
	}
	
	public void setRecordAlive(RecordAlive alive){
		this.recordAlive = alive.getSeconds();
	}
	
	public String getTele1() {
		return tele1;
	}
	
	public void setTele1(String tele1) {
		this.tele1 = tele1;
	}
	
	public String getTele2() {
		return tele2;
	}
	
	public void setTele2(String tele2) {
		this.tele2 = tele2;
	}
	
	public String getPwd() {
		return pwd;
	}
	
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	
	public String getPwd2() {
		return pwd2;
	}
	
	public void setPwd2(String pwd2) {
		this.pwd2 = pwd2;
	}
	
	public String getPwd3() {
		return pwd3;
	}
	
	public void setPwd3(String pwd3) {
		this.pwd3 = pwd3;
	}
	
	public String getPwd4() {
		return pwd4;
	}
	
	public void setPwd4(String pwd4) {
		this.pwd4 = pwd4;
	}
	
	public String getPwd5() {
		return pwd5;
	}
	
	public void setPwd5(String pwd5) {
		this.pwd5 = pwd5;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	@Override
	public int hashCode(){
		return id * 31 + 17;
	}
	
	@Override 
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Restaurant)){
			return false;
		}else{
			return id == ((Restaurant)obj).id;
		}
	}
	
	@Override 
	public String toString(){
		return "restaurant(id = " + id + ", name = " + restaurantName + ")";
	}
}
