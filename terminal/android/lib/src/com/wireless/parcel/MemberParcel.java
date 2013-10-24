package com.wireless.parcel;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.pojo.client.Member;
import com.wireless.pojo.menuMgr.Food;

public class MemberParcel implements Parcelable {
	public static final String KEY_VALUE = "com.wireless.lib.parcel.MemberParcel";
	
	private final Member mSrcMember;
	
	public MemberParcel(Member member){
		this.mSrcMember = member;
	}
	
	public Member asMember(){
		return mSrcMember;
	}
	
	private MemberParcel(Parcel in){
		if(in.readInt() != 1){
			mSrcMember = new Member(0);
			mSrcMember.setId(in.readInt());
			mSrcMember.setName(in.readString());
			mSrcMember.setMobile(in.readString());
			mSrcMember.setMemberCard(in.readString());
			mSrcMember.setConsumptionAmount(in.readInt());
			mSrcMember.setLastConsumption(in.readLong());
			
			//un-marshal the favor foods
			List<FoodParcel> favorFoodParcels = in.createTypedArrayList(FoodParcel.CREATOR);
			for(FoodParcel fp : favorFoodParcels){
				mSrcMember.addFavorFood(fp.asFood());
			}
			
			//un-marshal the recommend foods
			List<FoodParcel> recommendFoodParcels = in.createTypedArrayList(FoodParcel.CREATOR);
			for(FoodParcel fp : recommendFoodParcels){
				mSrcMember.addRecommendFood(fp.asFood());
			}
			
		}else{
			mSrcMember = null;
		}
	}
	
	public static final Parcelable.Creator<MemberParcel> CREATOR = new Parcelable.Creator<MemberParcel>() {
		public MemberParcel createFromParcel(Parcel in) {
			return new MemberParcel(in);
		}

		public MemberParcel[] newArray(int size) {
			return new MemberParcel[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		if(mSrcMember == null){
			dest.writeInt(1);
		}else{
			dest.writeInt(0);
			dest.writeInt(mSrcMember.getId());
			dest.writeString(mSrcMember.getName());
			dest.writeString(mSrcMember.getMobile());
			dest.writeString(mSrcMember.getMemberCard());
			dest.writeInt(mSrcMember.getConsumptionAmount());
			dest.writeLong(mSrcMember.getLastConsumption());
			
			//marshal the favor foods
			List<FoodParcel> favorFoodParcels = new ArrayList<FoodParcel>(mSrcMember.getFavorFoods().size());
			for(Food favorFood : mSrcMember.getFavorFoods()){
				favorFoodParcels.add(new FoodParcel(favorFood));
			}
			dest.writeTypedList(favorFoodParcels);
			
			//marshal the recommend foods
			List<FoodParcel> recommendFoodParcels = new ArrayList<FoodParcel>(mSrcMember.getRecommendFoods().size());
			for(Food recommendFood : mSrcMember.getRecommendFoods()){
				recommendFoodParcels.add(new FoodParcel(recommendFood));
			}
			dest.writeTypedList(recommendFoodParcels);
		}
	}

}
