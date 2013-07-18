package com.wireless.pojo.printScheme;

import java.util.Collections;
import java.util.List;

import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.util.SortedList;

public class PrintFunc implements Comparable<PrintFunc>{
	
	private int mId;
	
	private final PType mType;
	
	private final List<Region> mRegions = SortedList.newInstance();
	
	private Department mDept;
	
	private final List<Kitchen> mKitchens = SortedList.newInstance();
	
	private final int mRepeat;
	
	/**
	 * The helper class to create the print function of summary.
	 */
	public static class SummaryBuilder{
		private int mRepeat = 1;
		private List<Region> mRegions = SortedList.newInstance();
		private Department mDept;
		
		public SummaryBuilder(){
		}
		
		public SummaryBuilder setRepeat(int repeat){
			mRepeat = repeat;
			return this;
		}
		
		public SummaryBuilder addRegion(Region regionToAdd){
			if(!mRegions.contains(regionToAdd)){
				mRegions.add(regionToAdd);
			}
			return this;
		}
		
		public SummaryBuilder setDepartment(Department dept){
			mDept = dept;
			return this;
		}
		
		public PrintFunc build(){
			return new PrintFunc(this);
		}
	}
	
	public static class DetailBuilder{
		private int mRepeat = 1;
		private List<Kitchen> mKitchens = SortedList.newInstance();
		
		public DetailBuilder(){
		}
		
		public DetailBuilder setRepeat(int repeat){
			mRepeat = repeat;
			return this;
		}
		
		public DetailBuilder addKitchen(Kitchen kitcheToAdd){
			if(!mKitchens.contains(kitcheToAdd)){
				mKitchens.add(kitcheToAdd);
			}
			return this;
		}
		
		public PrintFunc build(){
			return new PrintFunc(this);
		}
	}
	
	private PrintFunc(SummaryBuilder builder){
		this(PType.PRINT_ORDER, builder.mRepeat);
		mRegions.addAll(builder.mRegions);
		setDepartment(builder.mDept);
	}
	
	private PrintFunc(DetailBuilder builder){
		this(PType.PRINT_ORDER_DETAIL, builder.mRepeat);
		mKitchens.addAll(builder.mKitchens);
	}
	
	public PrintFunc(PType type, int repeat){
		this.mType = type;
		this.mRepeat = repeat;
	}
	
	public int getId(){
		return mId;
	}
	
	public void setId(int id){
		mId = id;
	}
	
	public PType getType(){
		return mType;
	}
	
	public int getRepeat(){
		return mRepeat;
	}
	
	public Department getDepartment(){
		return mDept;
	}
	
	public void setDepartment(Department dept){
		mDept = dept;
	}

	public void setDepartmentAll(){
		mDept = null;
	}
	
	public boolean isDeptAll(){
		return mDept == null;
	}
	
	public List<Region> getRegions(){
		return Collections.unmodifiableList(mRegions);
	}
	
	public List<Kitchen> getKitchens(){
		return Collections.unmodifiableList(mKitchens);
	}
	
	public void addKitchen(Kitchen kitchenToAdd){
		if(!mKitchens.contains(kitchenToAdd)){
			mKitchens.add(kitchenToAdd);
		}
	}
	
	public boolean removeKitchen(Kitchen kitchenToRemove){
		return mKitchens.remove(kitchenToRemove);
	}
	
	public void setKitchenAll(){
		mKitchens.clear();
	}
	
	public boolean isKitchenAll(){
		return mKitchens.size() == 0;
	}
	
	public void addRegion(Region regionToAdd){
		if(!mRegions.contains(regionToAdd)){
			mRegions.add(regionToAdd);
		}
	}
	
	public boolean removeRegion(Region regionToRemove){
		return mRegions.remove(regionToRemove);
	}
	
	public void setRegionAll(){
		mRegions.clear();
	}
	
	public boolean isRegionAll(){
		return mRegions.size() == 0;
	}
	
	@Override
	public String toString(){
		return "type : " + mType.getDesc() + ", repeat : " + getRepeat();
	}
	
	@Override
	public int hashCode(){
		return 17 * 31 + mType.getVal();
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof PrintFunc)){
			return false;
		}else{
			PrintFunc right = (PrintFunc)obj;
			return getType().getVal() == right.mType.getVal();
		}
	}

	@Override
	public int compareTo(PrintFunc o) {
		if(mType.getVal() > o.mType.getVal()){
			return 1;
		}else if(mType.getVal() < o.mType.getVal()){
			return -1;
		}else{
			return 0;
		}
	}
}
