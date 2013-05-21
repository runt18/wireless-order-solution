package com.wireless.parcel;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.wireless.pojo.dishesOrder.Food;
import com.wireless.pojo.menuMgr.DepartmentTree;
import com.wireless.pojo.menuMgr.FoodList;

public class DepartmentTreeParcel implements Parcelable{

	public static final String KEY_VALUE = "com.wireless.lib.parcel.DepartmentTreeParcel";
	
	private FoodList mFoodList;
	
	public DepartmentTreeParcel(DepartmentTree deptTree){
		if(deptTree != null){
			List<Food> foodList = new ArrayList<Food>();
			for(Food f : deptTree.asFoodList()){
				foodList.add(f);
			}
			mFoodList = new FoodList(foodList);
		}
	}
	
	public DepartmentTreeParcel(Parcel in){
		//un-marshal the food list to this department tree
		List<FoodParcel> foodParcels = in.createTypedArrayList(FoodParcel.CREATOR);
		List<Food> foodList = new ArrayList<Food>();
		for(FoodParcel fp : foodParcels){
			foodList.add(fp.asFood());
		}
		mFoodList = new FoodList(foodList);
	}
	
	public DepartmentTree asDeptTree(){
		if(mFoodList != null){
			return mFoodList.asDeptTree();
		}else{
			return null;
		}
	}
	
	public static final Parcelable.Creator<DepartmentTreeParcel> CREATOR = new Parcelable.Creator<DepartmentTreeParcel>() {
		public DepartmentTreeParcel createFromParcel(Parcel in) {
			boolean isNull = in.readInt() == 1 ? true : false;
			if(isNull){
				return null;
			}else{
				return new DepartmentTreeParcel(in);
			}
		}

		public DepartmentTreeParcel[] newArray(int size) {
			return new DepartmentTreeParcel[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		if(mFoodList == null){
			dest.writeInt(1);
		}else{
			dest.writeInt(0);
			
			//marshal the food list to this department tree
			List<FoodParcel> foodParcels = new ArrayList<FoodParcel>(); 
			for(Food f : mFoodList){
				foodParcels.add(new FoodParcel(f));
			}
			dest.writeTypedList(foodParcels);
		}
	}

}
