package com.wireless.parcel;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.pojo.menuMgr.Department;

public class DepartmentParcel implements Parcelable{

	public static final String KEY_VALUE = "com.wireless.lib.parcel.DepartmentParcel";
	
	private Department mSrcDept;
	
	public DepartmentParcel(Department dept){
		this.mSrcDept = dept;
	}
	
	public DepartmentParcel(Parcel in){
		mSrcDept = new Department();
		mSrcDept.setId((short)in.readInt());
		mSrcDept.setRestaurantId(in.readInt());
		mSrcDept.setType(in.readInt());
		mSrcDept.setName(in.readString());
	}
	
	public Department asDept(){
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
			dest.writeInt(mSrcDept.getType().getVal());
			dest.writeString(mSrcDept.getName());
		}
	}

}
