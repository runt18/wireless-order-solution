package com.wireless.pojo.restaurantMgr;

import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;


public class Restaurant implements Parcelable{
	
	public final static byte RESTAURANT_PARCELABLE_COMPLEX = 0;
	public final static byte RESTAURANT_PARCELABLE_SIMPLE = 1;
	
	//The reserved restaurant id
	public static final int ADMIN = 1;
	public static final int IDLE = 2;
	public static final int DISCARD = 3;
	public static final int RESERVED_1 = 4;
	public static final int RESERVED_2 = 5;
	public static final int RESERVED_3 = 6;
	public static final int RESERVED_4 = 7;
	public static final int RESERVED_5 = 8;
	public static final int RESERVED_6 = 9;
	public static final int RESERVED_7 = 10;
	
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
	
	public Restaurant(){
		
	}
	
	public Restaurant(int id){
		this.id = id;
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
	
	public String getName() {
		if(restaurantName == null){
			restaurantName = "";
		}
		return restaurantName;
	}
	
	public void setName(String restaurantName) {
		this.restaurantName = restaurantName;
	}
	
	public String getInfo() {
		if(restaurantInfo == null){
			restaurantInfo = "";
		}
		return restaurantInfo;
	}
	
	public void setInfo(String restaurantInfo) {
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
	
	public boolean hasPwd(){
		return pwd != null ? pwd.length() != 0 : false;
	}
	
	public String getPwd2() {
		return pwd2;
	}
	
	public boolean hasPwd2(){
		return pwd2 != null ? pwd2.length() != 0 : false;
	}
	
	public void setPwd2(String pwd2) {
		this.pwd2 = pwd2;
	}
	
	public String getPwd3() {
		return pwd3;
	}
	
	public boolean hasPwd3(){
		return pwd3 != null ? pwd3.length() != 0 : false;
	}
	
	public void setPwd3(String pwd3) {
		this.pwd3 = pwd3;
	}
	
	public String getPwd4() {
		return pwd4;
	}
	
	public boolean hasPwd4(){
		return pwd4 != null ? pwd4.length() != 0 : false;
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
	
	public boolean hasPwd5(){
		return pwd5 != null ? pwd5.length() != 0 : false;
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
		return "restaurant(id = " + id + ", name = " + getName() + ")";
	}
	
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == RESTAURANT_PARCELABLE_SIMPLE){
			dest.writeInt(this.id);
			
		}else if(flag == RESTAURANT_PARCELABLE_COMPLEX){
			dest.writeInt(this.id);
			dest.writeString(this.restaurantName);
			dest.writeString(this.restaurantInfo);
			dest.writeString(this.pwd);
			dest.writeString(this.pwd2);
			dest.writeString(this.pwd3);
			dest.writeString(this.pwd4);
			dest.writeString(this.pwd5);
		}
	}

	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		if(flag == RESTAURANT_PARCELABLE_SIMPLE){
			this.id = source.readInt();
			
		}else if(flag == RESTAURANT_PARCELABLE_COMPLEX){
			this.id = source.readInt();
			this.restaurantName = source.readString();
			this.restaurantInfo = source.readString();
			this.pwd = source.readString();
			this.pwd2 = source.readString();
			this.pwd3 = source.readString();
			this.pwd4 = source.readString();
			this.pwd5 = source.readString();
		}
	}
	
	public final static Parcelable.Creator<Restaurant> CREATOR = new Parcelable.Creator<Restaurant>() {
		
		public Restaurant[] newInstance(int size) {
			return new Restaurant[size];
		}
		
		public Restaurant newInstance() {
			return new Restaurant();
		}
	};
}
