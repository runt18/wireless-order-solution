package com.wireless.pojo.printScheme;

import java.util.ArrayList;
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
	
	private int printerId;
	
	private PType mType;
	
	private final List<Region> mRegions = SortedList.newInstance();
	
	private final List<Department> mDepts = SortedList.newInstance();
	
	private final List<Kitchen> mKitchens = SortedList.newInstance();
	
	private int mRepeat;
	
	private String mComment;
	
	private boolean isIncludeCancel = true;
	
	public static class UpdateBuilder{
		private final int mId;
		private int mRepeat;
		private PType mPType;
		private String mComment;
		private List<Region> mRegions;
		private List<Department> mDept;
		private List<Kitchen> mKitchens;
		private int isIncludeCancel = -1;
		
		public UpdateBuilder(int id){
			this.mId = id;
		}
		
		public boolean isTypeChanged(){
			return this.mPType != null;
		}
		
		public UpdateBuilder setType(PType type){
			this.mPType = type;
			return this;
		}
		
		public UpdateBuilder setType(PType type, boolean onOff){
			if(type != PType.PRINT_ORDER_DETAIL && type != PType.PRINT_ORDER){
				throw new IllegalArgumentException("只有【" + PType.PRINT_ORDER_DETAIL.getDesc() + "】或【" + PType.PRINT_ORDER.getDesc() + "】的打印功能才可设置打印退菜");
			}else{
				this.mPType = type;
				isIncludeCancel = onOff ? 1 : 0;
			}
			return this;
		}
		
		public boolean isCommentChanged(){
			return this.mComment != null;
		}
		
		public UpdateBuilder setComment(String comment){
			this.mComment = comment;
			return this;
		}
		
		public UpdateBuilder setIncludeCancel(boolean onOff){
			this.isIncludeCancel = onOff ? 1 : 0;
			return this;
		}
		
		public boolean isIncludeCancelChanged(){
			return this.isIncludeCancel != -1;
		}
		
		public boolean isIncludeCancel(){
			return this.isIncludeCancel == 1;
		}
		
		public boolean isRepeatChanged(){
			return this.mRepeat != 0;
		}
		
		public UpdateBuilder setRepeat(int repeat){
			mRepeat = repeat;
			return this;
		}
		
		public boolean isRegionChanged(){
			return this.mRegions != null;
		}
		
		public UpdateBuilder setRegionAll(){
			mRegions = new ArrayList<Region>(0);
			return this;
		}
		
		public UpdateBuilder addRegion(Region regionToAdd){
			if(mRegions == null){
				mRegions = SortedList.newInstance();
			}
			if(!mRegions.contains(regionToAdd)){
				mRegions.add(regionToAdd);
			}
			return this;
		}
		
		public boolean isDeptChanged(){
			return this.mDept != null;
		}
		
		public UpdateBuilder setDepartmentAll(){
			mDept = new ArrayList<Department>(0);
			return this;
		}
		
		public UpdateBuilder addDepartment(Department dept){
			if(mDept == null){
				mDept = SortedList.newInstance();
			}
			if(!mDept.contains(dept)){
				mDept.add(dept);
			}
			return this;
		}
		
		public boolean isKitchenChanged(){
			return this.mKitchens != null;
		}
		
		public UpdateBuilder setKitchenAll(){
			this.mKitchens = new ArrayList<Kitchen>(0);
			return this;
		}
		
		public UpdateBuilder addKitchen(Kitchen kitchen){
			if(mKitchens == null){
				mKitchens = SortedList.newInstance();
			}
			if(!mKitchens.contains(kitchen)){
				mKitchens.add(kitchen);
			}
			return this;
		}
		
		public PrintFunc build(){
			return new PrintFunc(this);
		}
	}
	
	/**
	 * The helper class to create the print function of summary.
	 */
	public static class SummaryBuilder{
		private final int printerId;
		private int mRepeat = 1;
		private final PType mType;
		private final List<Region> mRegions = SortedList.newInstance();
		private final List<Department> mDepts = SortedList.newInstance();
		private final boolean isIncludeCancel;
		private String comment;
		
		private SummaryBuilder(int printerId, PType type, boolean isIncludeCancel){
			this.printerId = printerId;
			this.isIncludeCancel = isIncludeCancel;
			this.mType = type;
		}
		
		public static SummaryBuilder newExtra(int printerId, boolean isIncludeCancel){
			SummaryBuilder builder = new SummaryBuilder(printerId, PType.PRINT_ORDER, isIncludeCancel);
			return builder;
		}
		
		public static SummaryBuilder newCancel(PrintFunc func){
			SummaryBuilder builder = new SummaryBuilder(func.getPrinterId(), PType.PRINT_ALL_CANCELLED_FOOD, false);
			builder.setRepeat(func.getRepeat());
			builder.setDepartments(func.getDepartment());
			builder.setRegions(func.getRegions());
			builder.setComment(func.getComment());
			return builder;
		}
		
		public boolean isIncludeCancel(){
			return this.isIncludeCancel;
		}
		
		public SummaryBuilder setComment(String comment){
			this.comment = comment;
			return this;
		}
		
		public SummaryBuilder setRepeat(int repeat){
			mRepeat = repeat;
			return this;
		}
		
		public SummaryBuilder setRegions(List<Region> regions){
			if(regions != null){
				mRegions.clear();
				mRegions.addAll(regions);
			}
			return this;
		}
		
		public SummaryBuilder addRegion(Region regionToAdd){
			if(!mRegions.contains(regionToAdd)){
				mRegions.add(regionToAdd);
			}
			return this;
		}
		
		public SummaryBuilder setDepartments(List<Department> depts){
			if(depts != null){
				mDepts.clear();
				mDepts.addAll(depts);
			}
			return this;
		}
		
		public SummaryBuilder addDepartment(Department dept){
			mDepts.add(dept);
			return this;
		}		
		
		public PType getType() {
			return mType;
		}

		public PrintFunc build(){
			return new PrintFunc(this);
		}
	}
	
	public static class DetailBuilder{
		private final int mPrinterId;
		private int mRepeat = 1;
		private List<Kitchen> mKitchens = SortedList.newInstance();
		private final PType mType;
		private final boolean isIncludeCancel;
		
		private DetailBuilder(int printerId, PType type, boolean isIncludeCancel){
			this.mPrinterId = printerId;
			this.isIncludeCancel = isIncludeCancel;
			this.mType = type;
		}
		
		public static DetailBuilder newExtra(int printerId, boolean isIncludeCancel){
			DetailBuilder builder = new DetailBuilder(printerId, PType.PRINT_ORDER_DETAIL, isIncludeCancel);
			return builder;
		}
		
		public static DetailBuilder newCancel(PrintFunc func){
			DetailBuilder builder = new DetailBuilder(func.getPrinterId(), PType.PRINT_CANCELLED_FOOD_DETAIL, false);
			builder.setRepeat(func.getRepeat());
			builder.setKitchens(func.getKitchens());
			return builder;
		}
		
		public boolean isIncludeCancel(){
			return this.isIncludeCancel;
		}
		
		public DetailBuilder setRepeat(int repeat){
			mRepeat = repeat;
			return this;
		}
		
		public DetailBuilder setKitchens(List<Kitchen> kitchens){
			if(kitchens != null){
				mKitchens.clear();
				mKitchens.addAll(kitchens);
			}
			return this;
		}
		
		public DetailBuilder addKitchen(Kitchen kitchenToAdd){
			if(!mKitchens.contains(kitchenToAdd)){
				mKitchens.add(kitchenToAdd);
			}
			return this;
		}
		
		public PType getType() {
			return mType;
		}

		public PrintFunc build(){
			return new PrintFunc(this);
		}
	}
	
	public static class Builder{
		private final int mPrinterId;
		private int mRepeat = 1;
		private PType mType ;
		private List<Region> mRegions = SortedList.newInstance();
		
		private Builder(int printerId){
			this.mPrinterId = printerId;
		}
		
		public static Builder newReceipt(int printerId){
			Builder builder = new Builder(printerId);
			builder.setType(PType.PRINT_RECEIPT);
			return builder;
		}
		
		public static Builder newTempReceipt(int printerId){
			Builder builder = new Builder(printerId);
			builder.setType(PType.PRINT_TEMP_RECEIPT);
			return builder;
		}

		public static Builder newTransferTable(int printerId){
			Builder builder = new Builder(printerId);
			builder.setType(PType.PRINT_TRANSFER_TABLE);
			return builder;
		}		
		
		public static Builder newAllHurriedFood(int printerId){
			Builder builder = new Builder(printerId);
			builder.setType(PType.PRINT_ALL_HURRIED_FOOD);
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
		
		
		public PType getType() {
			return mType;
		}

		private Builder setType(PType mType) {
			this.mType = mType;
			return this;
		}
		
		public PrintFunc build(){
			return new PrintFunc(this);
		}
		
	}
	
	private PrintFunc(UpdateBuilder builder){
		setId(builder.mId);
		if(builder.isTypeChanged()){
			setType(builder.mPType);
		}
		if(builder.isRepeatChanged()){
			setRepeat(builder.mRepeat);
		}
		if(builder.isRegionChanged()){
			mRegions.addAll(builder.mRegions);
		}
		if(builder.isDeptChanged()){
			mDepts.addAll(builder.mDept);
		}
		if(builder.isKitchenChanged()){
			mKitchens.addAll(builder.mKitchens);
		}
		if(builder.isCommentChanged()){
			mComment = builder.mComment;
		}
	}
	
	private PrintFunc(SummaryBuilder builder){
		this(builder.getType(), builder.mRepeat);
		this.printerId = builder.printerId;
		mRegions.addAll(builder.mRegions);
		mDepts.addAll(builder.mDepts);
		mComment = builder.comment;
	}
	
	private PrintFunc(DetailBuilder builder){
		this(builder.getType(), builder.mRepeat);
		this.printerId = builder.mPrinterId;
		mKitchens.addAll(builder.mKitchens);
	}
	
	private PrintFunc(Builder builder){
		this(builder.getType(), builder.mRepeat);
		this.printerId = builder.mPrinterId;
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
	
	public void setType(PType type){
		this.mType = type;
	}
	
	public PType getType(){
		return mType;
	}
	
	public void setPrinterId(int printerId){
		this.printerId = printerId;
	}
	
	public int getPrinterId(){
		return this.printerId;
	}
	
	public void setRepeat(int repeat){
		this.mRepeat = repeat;
	}
	
	public int getRepeat(){
		return mRepeat;
	}
	
	public List<Department> getDepartment(){
		return Collections.unmodifiableList(mDepts);
	}
	
	public void setDepartments(List<Department> depts){
		if(depts != null){
			mDepts.clear();
			mDepts.addAll(depts);
		}
	}
	
	public void addDepartment(Department dept){
		if(!mDepts.contains(dept)){
			mDepts.add(dept);
		}
	}

	public boolean removeDepartment(Department dept){
		return mDepts.remove(dept);
	}
	
	public void setDepartmentAll(){
		mDepts.clear();
	}
	
	public boolean isDeptAll(){
		return mDepts.isEmpty();
	}
	
	public List<Region> getRegions(){
		return Collections.unmodifiableList(mRegions);
	}
	
	public List<Kitchen> getKitchens(){
		return Collections.unmodifiableList(mKitchens);
	}
	
	public void setKitchens(List<Kitchen> kitchens){
		if(kitchens != null){
			mKitchens.clear();
			mKitchens.addAll(kitchens);
		}
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
	
	public void setRegions(List<Region> regions){
		if(regions != null){
			mRegions.clear();
			mRegions.addAll(regions);
		}
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
	
	public boolean hasComment(){
		return getComment().trim().length() != 0;
	}
	
	public String getComment(){
		if(this.mComment == null){
			return "";
		}
		return this.mComment;
	}
	
	public void setComment(String comment){
		this.mComment = comment;
	}
	
	public boolean isIncludeCancel() {
		return isIncludeCancel;
	}

	public void setIncludeCancel(boolean isIncludeCancel) {
		this.isIncludeCancel = isIncludeCancel;
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
			return getType().getVal() == ((PrintFunc)obj).mType.getVal();
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
		
		jm.putBoolean("isIncludeCancel", this.isIncludeCancel);
		
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
		
		if(this.mDepts.size() > 0){
			for (Department department : this.mDepts) {
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
