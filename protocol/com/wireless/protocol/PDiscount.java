package com.wireless.protocol;

import com.wireless.protocol.parcel.Parcel;
import com.wireless.protocol.parcel.Parcelable;


class PDiscount implements Parcelable{
	
	public final static byte DISCOUNT_PARCELABLE_COMPLEX = 0;
	public final static byte DISCOUNT_PARCELABLE_SIMPLE = 1;
	
	public static final int NORMAL = 0;				// 一般类型
	public static final int DEFAULT = 1;			// 默认类型
	public static final int RESERVED = 2;			// 系统保留
	public static final int DEFAULT_RESERVED = 3;	// 既是默认类型, 也是系统保留(默认类型属于用户自定义操作,等级高于系统保留)
	public static final int MEMBER_TYPE = 4;		// 会员类型全单使用的
	
	int mDiscountId;
	String mName;
	int restaurantId;
	int mLevel;
	PDiscountPlan[] mPlans;
	int mStatus = NORMAL;
	
	public PDiscount(){
		
	}
	
	public PDiscount(int discountId){
		this();
		this.mDiscountId = discountId;
	}
	
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof PDiscount)){
			return false;
		}else{
			return mDiscountId == ((PDiscount)obj).mDiscountId;
		}
	}
	
	public int hashCode(){
		return mDiscountId * 31 + 17;
	}
	
	public String toString(){
		return "discount(id = " + mDiscountId + ", restaurant_id = " + restaurantId + ", name = " + mName + ")";
	}
	
	public int getStatus(){
		return mStatus;
	}
	
	public void setStatus(int status){
		this.mStatus = status;
	}
	
	public boolean isNormal(){
		return mStatus == NORMAL;
	}
	
	public boolean isDefault(){
		return mStatus == DEFAULT;
	}	

	public boolean isReserved(){
		return mStatus == RESERVED;
	}
	
	public boolean isMember(){
		return mStatus == MEMBER_TYPE;
	}

	public int getId() {
		return mDiscountId;
	}

	public void setId(int discountId) {
		this.mDiscountId = discountId;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public int getRestaurantId() {
		return restaurantId;
	}

	public void setRestaurantId(int restaurantId) {
		this.restaurantId = restaurantId;
	}

	public int getLevel() {
		return mLevel;
	}

	public void setLevel(int level) {
		this.mLevel = level;
	}

	public PDiscountPlan[] getPlans() {
		if(mPlans == null){
			mPlans = new PDiscountPlan[0];
		}
		return mPlans;
	}

	public void setPlans(PDiscountPlan[] plans) {
		this.mPlans = plans;
	}

	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == DISCOUNT_PARCELABLE_SIMPLE){
			dest.writeInt(this.mDiscountId);
			
		}else if(flag == DISCOUNT_PARCELABLE_COMPLEX){
			dest.writeInt(this.mDiscountId);
			dest.writeShort(this.mLevel);
			dest.writeByte(this.mStatus);
			dest.writeString(this.mName);
			dest.writeParcelArray(this.mPlans, 0);
		}
	}

	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		if(flag == DISCOUNT_PARCELABLE_SIMPLE){
			this.mDiscountId = source.readInt();
			
		}else if(flag == DISCOUNT_PARCELABLE_COMPLEX){
			this.mDiscountId = source.readInt();
			this.mLevel = source.readShort();
			this.mStatus = source.readByte();
			this.mName = source.readString();
			Parcelable[] plans = source.readParcelArray(PDiscountPlan.DP_CREATOR);
			if(plans != null){
				this.mPlans = new PDiscountPlan[plans.length];
				for(int i = 0; i < mPlans.length; i++){
					mPlans[i] = (PDiscountPlan)plans[i];
				}
			}
		}
	}
	
	public final static Parcelable.Creator DISCOUNT_CREATOR = new Parcelable.Creator() {
		
		public Parcelable[] newInstance(int size) {
			return new PDiscount[size];
		}
		
		public Parcelable newInstance() {
			return new PDiscount();
		}
	};
}
