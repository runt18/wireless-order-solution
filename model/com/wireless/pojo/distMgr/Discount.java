package com.wireless.pojo.distMgr;

import java.util.ArrayList;
import java.util.List;

import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;

public class Discount implements Parcelable{
	
	public final static byte DISCOUNT_PARCELABLE_COMPLEX = 0;
	public final static byte DISCOUNT_PARCELABLE_SIMPLE = 1;
	
	public static enum Status{
		
		NORMAL(0, "normal"),								// 一般类型
		DEFAULT(1, "default"),							// 一般类型
		RESERVED(2, "reserved"),						// 系统保留
		DEFAULT_RESERVED(3, "default & reserved"),	// 既是默认类型, 也是系统保留(默认类型属于用户自定义操作,等级高于系统保留)
		MEMBER_TYPE(4, "member");					// 会员类型全单使用的
		
		private final int val;
		private final String desc;
		
		private Status(int val, String desc){
			this.val = val;
			this.desc = desc;
		}
		
		@Override
		public String toString(){
			return "status(val = " + val + ", desc = " + desc + ")";
		}
		
		public static Status valueOf(int val){
			for(Status status : values()){
				if(status.getVal() == val){
					return status;
				}
			}
			throw new IllegalArgumentException("The discount status(val = " + val + ") passed is invalid.");
		}
		
		public int getVal(){
			return this.val;
		}
		
	}
	
	private String name;
	private int id;
	private int restaurantID;
	private int level;
	private Status status = Status.NORMAL;
	private List<DiscountPlan> plans = new ArrayList<DiscountPlan>();
	
	public Discount(){
		
	}
	
	public Discount(int id){
		this.id = id;
	}
	
	public String getName(){
		if(name == null){
			name = "";
		}
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public int getId(){
		return id;
	}
	
	public void setId(int id){
		this.id = id; 
	}
	
	public int getRestaurantId(){
		return restaurantID;
	}
	
	public void setRestaurantId(int restId){
		this.restaurantID = restId;
	}
	
	public int getLevel(){
		return level;
	}
	
	public void setLevel(int level){
		this.level = level;
	}
	
	public Status getStatus() {
		return status;
	}
	
	public void setStatus(int statusVal) {
		this.status = Status.valueOf(statusVal);
	}
	
	public void setStatus(Status status){
		this.status = status;
	}
	
	public List<DiscountPlan> getPlans(){
		return plans;
	}
	
	public void addPlans(List<DiscountPlan> plans){
		for(DiscountPlan plan : plans){
			addPlan(plan);
		}
	}
	
	public void addPlan(DiscountPlan plan){
		if(plan != null){
			plan.setDiscount(this);
			plans.add(plan);
		}
	}
	
	public boolean isNormal(){
		return this.status == Status.NORMAL;
	}
	
	public boolean isDefault(){
		return this.status == Status.DEFAULT;
	}
	
	public boolean isReserved(){
		return this.status == Status.RESERVED;
	}
	
	public boolean isDefaultReserved(){
		return this.status == Status.DEFAULT_RESERVED;
	}
	
	public boolean isMemberType(){
		return this.status == Status.MEMBER_TYPE;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Discount)){
			return false;
		}else{
			return this.id == ((Discount)obj).id;
		}
	}
	
	@Override
	public int hashCode(){
		return this.id * 31 + 17;
	}
	
	@Override
	public String toString(){
		return "discount(id = " + id + ", restaurant_id = " + restaurantID + ", name = " + getName() + ")";
	}
	
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == DISCOUNT_PARCELABLE_SIMPLE){
			dest.writeInt(this.id);
			
		}else if(flag == DISCOUNT_PARCELABLE_COMPLEX){
			dest.writeInt(this.id);
			dest.writeShort(this.level);
			dest.writeByte(this.status.getVal());
			dest.writeString(this.name);
			dest.writeParcelList(this.plans, 0);
		}
	}

	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		if(flag == DISCOUNT_PARCELABLE_SIMPLE){
			this.id = source.readInt();
			
		}else if(flag == DISCOUNT_PARCELABLE_COMPLEX){
			this.id = source.readInt();
			this.level = source.readShort();
			this.status = Status.valueOf(source.readByte());
			this.name = source.readString();
			this.plans = source.readParcelList(DiscountPlan.DP_CREATOR);
		}
	}
	
	public final static Parcelable.Creator<Discount> DISCOUNT_CREATOR = new Parcelable.Creator<Discount>() {
		
		public Discount[] newInstance(int size) {
			return new Discount[size];
		}
		
		public Discount newInstance() {
			return new Discount();
		}
	};
}
