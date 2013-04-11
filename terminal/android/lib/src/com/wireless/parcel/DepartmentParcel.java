package com.wireless.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.protocol.PDepartment;

public class DepartmentParcel implements Parcelable{

	public static final String KEY_VALUE = "com.wireless.lib.parcel.DepartmentParcel";
	
	private PDepartment mSrcDept;
	
	public DepartmentParcel(PDepartment dept){
		this.mSrcDept = dept;
	}
	
	public DepartmentParcel(Parcel in){
		mSrcDept = new PDepartment();
		mSrcDept.setId((short)in.readInt());
		mSrcDept.setRestaurantId(in.readInt());
		mSrcDept.setType((short)in.readInt());
		mSrcDept.setName(in.readString());
	}
	
	public PDepartment asDept(){
		return this.mSrcDept;
	}
	
	public static final Parcelable.Creator<DepartmentParcel> CREATOR = new Parcelable.Creator<DepartmentParcel>() {
		public DepartmentParcel createFromParcel(Parcel in) {
			boolean isNull = in.readInt() == 1 ? true : false;
			if(isNull){
				return null;
			}else{
				return new DepartmentParcel(in);
			}
		}

		public DepartmentParcel[] newArray(int size) {
			return new DepartmentParcel[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		if(mSrcDept == null){
			dest.writeInt(1);
		}else{
			dest.writeInt(0);
			
			dest.writeInt(mSrcDept.getId());
			dest.writeInt(mSrcDept.getRestaurantId());
			dest.writeInt(mSrcDept.getType());
			dest.writeString(mSrcDept.getName());
		}
	}

}
