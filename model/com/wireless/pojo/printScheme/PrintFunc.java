package com.wireless.pojo.printScheme;

import java.util.Collections;
import java.util.List;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.regionMgr.Region;
import com.wireless.pojo.util.SortedList;

public class PrintFunc implements Comparable<PrintFunc>, Jsonable{
	
	private int mId;
	
	private final PType mType;
	
	private final List<Region> mRegions = SortedList.newInstance();
	
	private List<Department> mDept = SortedList.newInstance();
	
	private final List<Kitchen> mKitchens = SortedList.newInstance();
	
	private final int mRepeat;
	
	/**
	 * The helper class to create the print function of summary.
	 */
	public static class SummaryBuilder{
		private int mRepeat = 1;
		private PType mType;
		private List<Region> mRegions = SortedList.newInstance();
		private List<Department> mDept = SortedList.newInstance();
		
		private SummaryBuilder(){
		}
		
		public static SummaryBuilder newPrintOrder(){
			SummaryBuilder builder = new SummaryBuilder();
			builder.setType( PType.PRINT_ORDER);
			return builder;
		}
		
		public static SummaryBuilder newAllCancelledFood(){
			SummaryBuilder builder = new SummaryBuilder();
			builder.setType(PType.PRINT_ALL_CANCELLED_FOOD);
			return builder;
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
		
		public SummaryBuilder addDepartment(Department dept){
			mDept.add(dept);
			return this;
		}		
		
		public PType getType() {
			return mType;
		}

		private SummaryBuilder setType(PType type) {
			this.mType = type;
			return this;
		}

		public PrintFunc build(){
			return new PrintFunc(this);
		}
	}
	
	
	
	
	public static class DetailBuilder{
		private int mRepeat = 1;
		private List<Kitchen> mKitchens = SortedList.newInstance();
		private PType mType;
		
		private DetailBuilder(){
		}
		
		public static DetailBuilder newPrintFoodDetail(){
			DetailBuilder builder = new DetailBuilder();
			builder.setType(PType.PRINT_ORDER_DETAIL);
			return builder;
			
		}
		
		public static DetailBuilder newCancelledFood(){
			DetailBuilder builder = new DetailBuilder();
			builder.setType(PType.PRINT_CANCELLED_FOOD_DETAIL);
			return builder;
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
		
		public PType getType() {
			return mType;
		}

		private DetailBuilder setType(PType type) {
			this.mType = type;
			return this;
		}

		public PrintFunc build(){
			return new PrintFunc(this);
		}
	}
	
	public static class Builder{
		private int mRepeat = 1;
		private PType mType ;
		private List<Region> mRegions = SortedList.newInstance();
		
		private Builder(){
			
		}
		
		public static Builder newReceipt(){
			Builder builder = new Builder();
			builder.setmType(PType.PRINT_RECEIPT);
			return builder;
		}
		
		public static Builder newTempReceipt(){
			Builder builder = new Builder();
			builder.setmType(PType.PRINT_TEMP_RECEIPT);
			return builder;
		}

		public static Builder newTransferTable(){
			Builder builder = new Builder();
			builder.setmType(PType.PRINT_TRANSFER_TABLE);
			return builder;
		}		
		
		public static Builder newAllHurriedFood(){
			Builder builder = new Builder();
			builder.setmType(PType.PRINT_ALL_HURRIED_FOOD);
			return builder;
		}
		
		public Builder setRepeat(int repeat){
			mRepeat = repeat;
			return this;
		}
		
		public Builder addRegion(Region regionToAdd){
			if(!mRegions.contains(regionToAdd)){
				mRegions.add(regionToAdd);
			}
			return this;
		}
		
		
		public PType getmType() {
			return mType;
		}

		private Builder setmType(PType mType) {
			this.mType = mType;
			return this;
		}
		
		public PrintFunc build(){
			return new PrintFunc(this);
		}
		
	}
	
	
	
	private PrintFunc(SummaryBuilder builder){
		this(builder.getType(), builder.mRepeat);
		mRegions.addAll(builder.mRegions);
		mDept.addAll(builder.mDept);
	}
	
	private PrintFunc(DetailBuilder builder){
		this(builder.getType(), builder.mRepeat);
		mKitchens.addAll(builder.mKitchens);
	}
	
	private PrintFunc(Builder builder){
		this(builder.getmType(), builder.mRepeat);
		mRegions.addAll(builder.mRegions);
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
	
	public List<Department> getDepartment(){
		return Collections.unmodifiableList(mDept);
	}
	
	public void addDepartment(Department dept){
		if(!mDept.contains(dept)){
			mDept.add(dept);
		}
	}

	public boolean removeDepartment(Department dept){
		return mDept.remove(dept);
	}
	
	public void setDepartmentAll(){
		mDept = null;
	}
	
	public boolean isDeptAll(){
		return mDept.isEmpty();
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
		return mKitchens.isEmpty();
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
		return mRegions.isEmpty();
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
	
	public boolean isTypeMatched(PType typeToCompare){
		if(mType == PType.PRINT_ORDER){ 
			return typeToCompare == PType.PRINT_ORDER || 
				   typeToCompare == PType.PRINT_ALL_EXTRA_FOOD ||
				   typeToCompare == PType.PRINT_ORDER_PATCH;
			
		}else if(mType == PType.PRINT_ORDER_DETAIL){
			return typeToCompare == PType.PRINT_ORDER_DETAIL || 
				   typeToCompare == PType.PRINT_EXTRA_FOOD_DETAIL ||
				   typeToCompare == PType.PRINT_ORDER_DETAIL_PATCH;
			
		}else if(mType == PType.PRINT_RECEIPT){
			return typeToCompare == PType.PRINT_RECEIPT ||
				   typeToCompare == PType.PRINT_MEMBER_RECEIPT ||
				   typeToCompare.isShift();
			
		}else if(mType == PType.PRINT_TEMP_RECEIPT){
			return typeToCompare == PType.PRINT_TEMP_RECEIPT;
			
		}else if(mType == PType.PRINT_CANCELLED_FOOD_DETAIL){
			return typeToCompare == PType.PRINT_CANCELLED_FOOD_DETAIL;
			
		}else if(mType == PType.PRINT_TRANSFER_TABLE){
			return typeToCompare == PType.PRINT_TRANSFER_TABLE;
					
		}else if(mType == PType.PRINT_ALL_CANCELLED_FOOD){
			return typeToCompare == PType.PRINT_ALL_CANCELLED_FOOD;
			
		}else if(mType == PType.PRINT_ALL_HURRIED_FOOD){
			return typeToCompare == PType.PRINT_ALL_HURRIED_FOOD;
			
		}else{
			return false;
		}

	}
	
	public boolean isRegionMatched(Region regionToCompare){
		if(isRegionAll()){
			return true;
		}else{
			return mRegions.contains(regionToCompare);
		}
	}

	@Override
	public JsonMap toJsonMap(int flag) {
		String regions = "";
		String regionValues = "";
		
		String kitchens = "";
		String kitchenValues = "";
		
		String depts = "";
		String deptValues = "";
		
		JsonMap jm = new JsonMap();
		jm.putInt("printFuncId", this.mId);
		jm.putInt("pTypeValue", this.mType.getVal());
		jm.putString("pTypeText", this.mType.getDesc());
		jm.putInt("repeat", this.mRepeat);
		
		if(this.mRegions.size() > 0){
			regions = "";
			for (Region region : this.mRegions) {
				if(regions == ""){
					regionValues += region.getId();
					regions += region.getName();
				}else{
					regionValues += ("," + region.getId());
					regions += ("," + region.getName());
				}
			}
		}
		
		if(this.mKitchens.size() > 0){
			kitchens = "";
			
			for (Kitchen kitchen : this.mKitchens) {
				if(kitchens == ""){
					kitchenValues += kitchen.getId();
					kitchens += kitchen.getName();
				}else{
					kitchenValues += ("," + kitchen.getId());
					kitchens += ("," + kitchen.getName());
				}
			}
		}
		
		if(this.mDept.size() > 0){
			for (Department department : this.mDept) {
				if(depts == ""){
					deptValues += department.getId();
					depts += department.getName();
				}else{
					deptValues += ("," + department.getId());
					depts += ("," + department.getName());
				}
			}
		}

		
		if(this.mType == PType.PRINT_ORDER || this.mType == PType.PRINT_ALL_CANCELLED_FOOD){
			kitchens = "----";
			if(isRegionAll()){
				regions = "所有区域";
				regionValues = "";
			}
			if(isDeptAll()){
				depts = "所有部门";
				deptValues = "";
			}
		}else if(this.mType == PType.PRINT_ORDER_DETAIL || this.mType == PType.PRINT_CANCELLED_FOOD_DETAIL){
			regions = "----";
			depts = "----";
			if(isKitchenAll()){
				kitchens = "所有厨房";
				kitchenValues = "";
			}
		
		}else{
			depts = "----";
			kitchens = "----";
			if(isRegionAll()){
				regions = "所有区域";
				regionValues = "";
			}
		}
		
		

		jm.putString("regionValues", regionValues);
		jm.putString("regions", regions);

		jm.putString("kitchens", kitchens);
		jm.putString("kitchenValues", kitchenValues);
		
		jm.putString("dept", depts);
		jm.putString("deptValue", deptValues);
		
		
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
		
	}
}
