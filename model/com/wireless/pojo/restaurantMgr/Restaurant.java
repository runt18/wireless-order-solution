package com.wireless.pojo.restaurantMgr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;
import com.wireless.pojo.util.DateUtil;


public class Restaurant implements Parcelable{
	
	public static class InsertBuilder{
		private final String account;
		private final String restaurantName;
		private final long expireDate;
		private final String pwd;
		private String restaurantInfo;
		private RecordAlive recordAlive = RecordAlive.HALF_A_YEAR;
		private String tele1;
		private String tele2;
		private String address;
		
		public InsertBuilder(String account, String restaurantName, long expireDate, String pwd){
			this.account = account;
			this.restaurantName = restaurantName;
			this.expireDate = expireDate;
			this.pwd = pwd;
		}
		
		public InsertBuilder setRestaurantInfo(String info){
			this.restaurantInfo = info;
			return this;
		}
		
		public InsertBuilder setRecordAlive(RecordAlive recordAlive){
			this.recordAlive = recordAlive;
			return this;
		}
		
		public InsertBuilder setTele1(String tele1){
			this.tele1 = tele1;
			return this;
		}
		
		public InsertBuilder setTele2(String tele2){
			this.tele2 = tele2;
			return this;
		}
		
		public InsertBuilder setAddress(String address){
			this.address = address;
			return this;
		}
		
		public String getPwd(){
			return this.pwd;
		}
		
		public Restaurant build(){
			return new Restaurant(this);
		}
	}
	
	//The helper class to update a restaurant
	public static class UpdateBuilder{
		private final int id;
		private String account;
		private String restaurantName;
		private long expireDate;
		private String pwd;
		private String restaurantInfo;
		private RecordAlive recordAlive;
		private String tele1;
		private String tele2;
		private String address;
		
		public UpdateBuilder(int id){
			this.id = id;
		}
		
		public UpdateBuilder setAccount(String account){
			this.account = account;
			return this;
		}
		
		public String getAccount(){
			return this.account;
		}
		
		public UpdateBuilder setRestaurantName(String name){
			this.restaurantName = name;
			return this;
		}
		
		public String getRestaurantName(){
			return this.restaurantName;
		}
		
		public UpdateBuilder setExpireDate(long expiredate){
			this.expireDate = expiredate;
			return this;
		}
		
		public long getExpireDate(){
			return this.expireDate;
		}
		
		public UpdateBuilder setPwd(String pwd){
			this.pwd = pwd;
			return this;
		}
		
		public UpdateBuilder setRestaurantInfo(String info){
			this.restaurantInfo = info;
			return this;
		}

		public String getRestaurantInfo(){
			return this.restaurantInfo;
		}
		
		public UpdateBuilder setRecordAlive(RecordAlive recordAlive){
			this.recordAlive = recordAlive;
			return this;
		}
		
		public RecordAlive getRecordAlive(){
			return this.recordAlive;
		}
		
		public UpdateBuilder setTele1(String tele1){
			this.tele1 = tele1;
			return this;
		}
		
		public String getTele1(){
			return this.tele1;
		}
		
		public UpdateBuilder setTele2(String tele2){
			this.tele2 = tele2;
			return this;
		}
		
		public String getTele2(){
			return this.tele2;
		}
		
		public UpdateBuilder setAddress(String address){
			this.address = address;
			return this;
		}
		
		public String getAddress(){
			return this.address;
		}
		
		public String getPwd(){
			return this.pwd;
		}
		
		public int getId(){
			return id;
		}
		
	}
	
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
		NEVER_EXPIRED(1, 0, "无限期"),
		THREE_MONTHS(2, 3600 * 24 * 90, "90天"),
		HALF_A_YEAR(3, 3600 * 24 * 180, "180天"),
		ONE_YEAR(4, 3600 * 24 * 360, "1年");
		
		private final int val;
		private final int aliveSeconds;
		private final String desc;
		
		RecordAlive(int val, int aliveSeconds, String desc){
			this.val = val;
			this.aliveSeconds = aliveSeconds;
			this.desc = desc;
		}
		
		public static RecordAlive valueOf(int val){
			for(RecordAlive recordAlive : values()){
				if(recordAlive.val == val){
					return recordAlive;
				}
			}
			throw new IllegalArgumentException("The val(" + val + ") is invalid.");
		}
		
		public int getVal(){
			return val;
		}
		
		public int getSeconds(){
			return aliveSeconds;
		}
		
		public String getDesc(){
			return this.desc;
		}
		
		@Override
		public String toString(){
			return "RecordAlive(val = " + val + ",desc = " + desc + ")";
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
	private long expireDate;
	
	public Restaurant(){
		
	}
	
	public Restaurant(int id){
		this.id = id;
	}
	
	private Restaurant(InsertBuilder builder){
		setAccount(builder.account);
		setName(builder.restaurantName);
		setInfo(builder.restaurantInfo);
		setRecordAlive(builder.recordAlive.getSeconds());
		setTele1(builder.tele1);
		setTele2(builder.tele2);
		setAddress(builder.address);
		setExpireDate(builder.expireDate);
		String now = new SimpleDateFormat(DateUtil.Pattern.DATE.getPattern(), Locale.getDefault()).format(new Date());
		try {
			setBirthDate(new SimpleDateFormat(DateUtil.Pattern.DATE.getPattern(), Locale.getDefault()).parse(now).getTime());
		} catch (ParseException ignored) {}
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
		if(account != null){
			this.account = account;
		}
	}
	
	public String getName() {
		if(restaurantName == null){
			return "";
		}
		return restaurantName;
	}
	
	public void setName(String restaurantName) {
		if(restaurantName != null){
			this.restaurantName = restaurantName;
		}
	}
	
	public String getInfo() {
		if(restaurantInfo == null){
			return "";
		}
		return restaurantInfo;
	}
	
	public void setInfo(String restaurantInfo) {
		if(restaurantInfo != null){
			this.restaurantInfo = restaurantInfo;
		}
	}
	
	public int getRecordAlive() {
		return recordAlive;
	}
	
	public void setRecordAlive(int recordAlive) {
		this.recordAlive = recordAlive;
	}
	
	public void setRecordAlive(RecordAlive alive){
		if(alive != null){
			this.recordAlive = alive.getSeconds();
		}
	}
	
	public String getTele1() {
		if(tele1 == null){
			return "";
		}
		return tele1;
	}
	
	public void setTele1(String tele1) {
		if(tele1 != null){
			this.tele1 = tele1;
		}
	}
	
	public String getTele2() {
		if(tele2 == null){
			return "";
		}
		return tele2;
	}
	
	public void setTele2(String tele2) {
		if(tele2 != null){
			this.tele2 = tele2;
		}
	}
	
	public String getAddress() {
		if(address == null){
			return "";
		}
		return address;
	}
	
	public void setAddress(String address) {
		if(address != null){
			this.address = address;
		}
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
	
	public long getExpireDate() {
		return expireDate;
	}

	public void setExpireDate(long expireDate) {
		if(expireDate != 0){
			this.expireDate = expireDate;
		}
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
