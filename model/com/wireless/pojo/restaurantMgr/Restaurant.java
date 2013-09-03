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
	private float liveness;
	private long birthDate;
	
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
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public float getLiveness(){
		return this.liveness;
	}
	
	public void setBirthDate(long birthDate){
		this.birthDate = birthDate;
	}
	
	public long getBirthDate(){
		return this.birthDate;
	}
	
	public void setLiveness(float liveness){
		if(liveness < 0 || liveness > 1){
			throw new IllegalArgumentException("The liveness must be ranged from 0 to 1.");
		}
		this.liveness = liveness;
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
