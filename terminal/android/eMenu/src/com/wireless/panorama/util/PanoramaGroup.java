package com.wireless.panorama.util;

import java.util.ArrayList;
import java.util.List;

import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;

public class PanoramaGroup {
	private Kitchen mKitchen;
	
	private List<Food> mLargeList = new ArrayList<Food>();
	private List<Food> mSmallList = new ArrayList<Food>();
	
	private int mLayoutId = 0;

	public PanoramaGroup(Kitchen mKitchen, List<Food> list,
			List<Food> list2) {
		super();
		this.mKitchen = mKitchen;
		
		if(list != null)
			this.mLargeList = list;
		if(list2 != null)
			this.mSmallList = list2;
	}
	
	public int getLayoutId() {
		return mLayoutId;
	}
	public void setLayoutId(int layoutId) {
		this.mLayoutId = layoutId;
	}
	
	public Kitchen getKitchen() {
		return mKitchen;
	}
	public void setKitchen(Kitchen kitchen) {
		this.mKitchen = kitchen;
	}
	public int getSmallCount() {
		return mSmallList.size();
	}
	
	
	public void setLargeList(ArrayList<Food> LargeList) {
		if(LargeList != null)
			this.mLargeList = LargeList;
	}
	public List<Food> getSmallList() {
		return mSmallList;
	}
	public void setSmallList(ArrayList<Food> smallList) {
		if(smallList != null)
			this.mSmallList = smallList;
	}
	public int getLargeCount() {
		return mLargeList.size();
	}
	public List<Food> getLargeList() {
		return mLargeList;
	}
	
	
}
