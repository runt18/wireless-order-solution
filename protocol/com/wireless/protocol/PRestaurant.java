package com.wireless.protocol;

import com.wireless.protocol.parcel.Parcel;
import com.wireless.protocol.parcel.Parcelable;


public class PRestaurant implements Parcelable{
	
	public final static byte RESTAURANT_PARCELABLE_COMPLEX = 0;
	public final static byte RESTAURANT_PARCELABLE_SIMPLE = 1;
	
	int id;
	String name;
	String tele1;
	String tele2;
	String addr;
	String info;
	String owner;
	String pwd;				//管理员权限密码
	String pwd2;			//财务权限密码
	String pwd3;			//店长权限密码
	String pwd4;			//收银权限密码
	String pwd5;			//退菜权限密码
	//public Setting setting = new Setting();
	
	public PRestaurant(){
		
	}
	
	public PRestaurant(String name){
		this.name = name;
	}
	
	public PRestaurant(String name, String info, String owner){
		this.name = name;
		this.info = info;
		this.owner = owner;
	}
	/**
	 * The reserved restaurant id
	 */
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		if(name == null){
			name = "";
		}
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTele1() {
		if(tele1 == null){
			tele1 = "";
		}
		return tele1;
	}

	public void setTele1(String tele1) {
		this.tele1 = tele1;
	}

	public String getTele2() {
		if(tele2 == null){
			tele2 = "";
		}
		return tele2;
	}

	public void setTele2(String tele2) {
		this.tele2 = tele2;
	}

	public String getInfo() {
		if(info == null){
			info = "";
		}
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getAddr() {
		return addr;
	}

	public void setAddr(String addr) {
		this.addr = addr;
	}

	public String getOwner() {
		if(owner == null){
			owner = "";
		}
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
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

	public void setPwd2(String pwd2) {
		this.pwd2 = pwd2;
	}

	public boolean hasPwd2(){
		return pwd2 != null ? pwd2.length() != 0 : false;
	}

	public String getPwd3() {
		return pwd3;
	}

	public void setPwd3(String pwd3) {
		this.pwd3 = pwd3;
	}

	public boolean hasPwd3(){
		return pwd3 != null ? pwd3.length() != 0 : false;
	}
	
	public String getPwd4() {
		return pwd4;
	}

	public void setPwd4(String pwd4) {
		this.pwd4 = pwd4;
	}

	public boolean hasPwd4(){
		return pwd4 != null ? pwd4.length() != 0 : false;
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
	
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == RESTAURANT_PARCELABLE_SIMPLE){
			dest.writeInt(this.id);
			
		}else if(flag == RESTAURANT_PARCELABLE_COMPLEX){
			dest.writeInt(this.id);
			dest.writeString(this.name);
			dest.writeString(this.info);
			dest.writeString(this.owner);
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
			this.name = source.readString();
			this.info = source.readString();
			this.owner = source.readString();
			this.pwd = source.readString();
			this.pwd2 = source.readString();
			this.pwd3 = source.readString();
			this.pwd4 = source.readString();
			this.pwd5 = source.readString();
		}
	}
	
	public final static Parcelable.Creator RESTAURANT_CREATOR = new Parcelable.Creator() {
		
		public Parcelable[] newInstance(int size) {
			return new PRestaurant[size];
		}
		
		public Parcelable newInstance() {
			return new PRestaurant();
		}
	};
	
}


