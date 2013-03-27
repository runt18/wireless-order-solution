package com.wireless.protocol;

import com.wireless.protocol.parcel.Parcel;
import com.wireless.protocol.parcel.Parcelable;

public class PMember implements Parcelable{

	public static final int MEMBER_PARCELABLE_SIMPLE = 0;
	public static final int MEMBER_PARCELABLE_COMPLEX = 1;
	
	public static final int STATUS_NORMAL = 0;		//正常状态
	public static final int STATUS_DISABLED = 1;	//禁用状态
	
	int id;
	String name;
	int restaurantId;
	float baseBalance;
	float extraBalance;
	int point;
	long birthDate;
	int status;
	private PMemberType type;
	
	public PMember(int id){
		this.id = id;
	}
	
	public PMember(){
		
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public int getRestaurantId() {
		return restaurantId;
	}
	
	public void setRestaurantId(int restaurantId) {
		this.restaurantId = restaurantId;
	}
	
	public float getBaseBalance() {
		return baseBalance;
	}
	
	public void setBaseBalance(float baseBalance) {
		this.baseBalance = baseBalance;
	}
	
	public float getExtraBalance() {
		return extraBalance;
	}
	
	public void setExtraBalance(float extraBalance) {
		this.extraBalance = extraBalance;
	}
	
	public float getTotalBalance(){
		return baseBalance + extraBalance;
	}
	
	public int getPoint() {
		return point;
	}
	
	public void setPoint(int point) {
		this.point = point;
	}
	
	public long getBirthDate() {
		return birthDate;
	}
	
	public void setBirthDate(long birthDate) {
		this.birthDate = birthDate;
	}

	public void setStatus(int status){
		this.status = status;
	}
	
	public int getStatus(){
		return this.status;
	}
	
	public boolean isNormal(){
		return this.status == STATUS_NORMAL;
	}
	
	public boolean isDisabled(){
		return this.status == STATUS_DISABLED;
	}
	
	public void setMemberType(PMemberType type){
		this.type = type;
	}
	
	public PMemberType getMemberType(){
		if(this.type == null){
			this.type = new PMemberType();
		}
		return this.type;
	}
	
	public int hashCode(){
		return 17 * 31 + this.id;
	}
	
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof PMember)){
			return false;
		}else{
			return this.id == ((PMember)obj).id;
		}
	}
	
	public String toString(){
		return "member(name = " + this.name + ", id = " + this.id + ")";
	}

	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == MEMBER_PARCELABLE_SIMPLE){
			dest.writeInt(this.id);
			
		}else if(flag == MEMBER_PARCELABLE_COMPLEX){
			
		}
		
	}

	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		if(flag == MEMBER_PARCELABLE_SIMPLE){
			this.id = source.readInt();
			
		}else if(flag == MEMBER_PARCELABLE_COMPLEX){
			
		}
	}
	
	public final static Parcelable.Creator MEMBER_CREATOR = new Parcelable.Creator() {
		
		public Parcelable[] newInstance(int size) {
			return new PMember[size];
		}
		
		public Parcelable newInstance() {
			return new PMember();
		}
	};
}
