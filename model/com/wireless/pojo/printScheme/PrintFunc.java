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
	
	private boolean enabled = true;
	
	public static class SummaryUpdateBuilder{
		private final UpdateBuilder builder;
		  
		public SummaryUpdateBuilder(int printerId, PType type){
			if(type == PType.PRINT_ORDER || type == PType.PRINT_ALL_CANCELLED_FOOD){
				this.builder = new UpdateBuilder(printerId, type);  
			}else{
				throw new IllegalArgumentException("打印类型只能是【" + PType.PRINT_ORDER.getDesc() + "】或者【" + PType.PRINT_ALL_CANCELLED_FOOD.getDesc() + "】");
			}
		}
		
		public SummaryUpdateBuilder setRepeat(int repeat){
			builder.setRepeat(repeat);
			return this;
		}
		
		public SummaryUpdateBuilder setComment(String comment){
			this.builder.setComment(comment);
			return this;
		}
		
		public SummaryUpdateBuilder setRegionAll(){
			this.builder.setRegionAll();
			return this;
		}
		
		public SummaryUpdateBuilder addRegion(Region regionToAdd){
			this.builder.addRegion(regionToAdd);
			return this;
		}
		
		public SummaryUpdateBuilder setDepartmentAll(){
			this.builder.setDepartmentAll();
			return this;
		}
		
		public SummaryUpdateBuilder addDepartment(Department dept){
			this.builder.addDepartment(dept);
			return this;
		}
		
		public UpdateBuilder build(){
			return this.builder;
		}
	}
	
	public static class DetailUpdateBuilder{
		private final UpdateBuilder builder;
		private final int printerId;
		private final boolean extraEnabled;
		private final boolean cancelEnabled;
		
		public DetailUpdateBuilder(int printerId, boolean extraEnabled, boolean cancelEnabled){
			this.builder = new UpdateBuilder(printerId, PType.PRINT_UNKNOWN);
			this.printerId = printerId;
			this.extraEnabled = extraEnabled;
			this.cancelEnabled = cancelEnabled;
		}
		
		public DetailUpdateBuilder setRegionAll(){
			builder.setRegionAll();
			return this;
		}
		
		public DetailUpdateBuilder addRegion(Region region){
			builder.addRegion(region);
			return this;
		}
		
		public DetailUpdateBuilder setRepeat(int repeat){
			builder.setRepeat(repeat);
			return this;
		}
		
		public DetailUpdateBuilder setKitchenAll(){
			this.builder.setKitchenAll();
			return this;
		}
		
		public DetailUpdateBuilder addKitchen(Kitchen kitchen){
			this.builder.addKitchen(kitchen);
			return this;
		}
		
		public int getPrinterId(){
			return this.printerId;
		}
		
		public UpdateBuilder[] build(int extraId, int cancelId){
			final UpdateBuilder[] result = new UpdateBuilder[2];
			result[0] = new UpdateBuilder(builder.mPrinterId, PType.PRINT_ORDER_DETAIL, builder).setEnabled(extraEnabled);
			result[1] = new UpdateBuilder(builder.mPrinterId, PType.PRINT_CANCELLED_FOOD_DETAIL, builder).setEnabled(cancelEnabled);
			if(builder.isRegionChanged()){
				result[0].mRegions = new ArrayList<Region>(builder.mRegions);
				result[1].mRegions = new ArrayList<Region>(builder.mRegions);
			}
			return result;
		}
	}
	
	public static class UpdateBuilder{
		private final int mPrinterId;
		private int mRepeat;
		private final PType mPType;
		private String mComment;
		private List<Region> mRegions;
		private List<Department> mDept;
		private List<Kitchen> mKitchens;
		private int enabled = -1;
		
		private UpdateBuilder(int printerId, PType type, UpdateBuilder src){
			this(printerId, type);
			if(src.isRepeatChanged()){
				setRepeat(src.mRepeat);
			}
			if(src.isCommentChanged()){
				setComment(src.mComment);
			}
			if(src.isRegionChanged()){
				if(src.mRegions.isEmpty()){
					setRegionAll();
				}else{
					for(Region region : src.mRegions){
						addRegion(region);
					}
				}
			}
			if(src.isDeptChanged()){
				if(src.mDept.isEmpty()){
					setDepartmentAll();
				}else{
					for(Department dept : src.mDept){
						addDepartment(dept);
					}
				}
			}
			if(src.isKitchenChanged()){
				if(src.mKitchens.isEmpty()){
					setKitchenAll();
				}else{
					for(Kitchen kitchen : src.mKitchens){
						addKitchen(kitchen);
					}
				}
			}
			if(src.isEnabledChanged()){
				setEnabled(src.enabled == 1);
			}
		}
		
		public UpdateBuilder(Printer printer, PType type){
			this(printer.getId(), type);
		}
		
		public UpdateBuilder(int printerId, PType type){
			this.mPrinterId = printerId;
			this.mPType = type;
		}
		
		public boolean isCommentChanged(){
			return this.mComment != null;
		}
		
		public UpdateBuilder setComment(String comment){
			this.mComment = comment;
			return this;
		}
		
		public boolean isEnabledChanged(){
			return this.enabled >= 0;
		}
		
		public UpdateBuilder setEnabled(boolean onOff){
			this.enabled = onOff ? 1 : 0;
			return this;
		}
		
		public boolean isEnabled(){
			return this.enabled == 1;
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
		private final PType type;
		private int mRepeat = 1;
		private final List<Region> mRegions = SortedList.newInstance();
		private final List<Department> mDepts = SortedList.newInstance();
		private String comment;
		
		public SummaryBuilder(int printerId, PType type){
			if(type == PType.PRINT_ORDER || type == PType.PRINT_ALL_CANCELLED_FOOD){
				this.printerId = printerId;
				this.type = type;
			}else{
				throw new IllegalArgumentException("打印类型只能是【" + PType.PRINT_ORDER.getDesc() + "】或者【" + PType.PRINT_ALL_CANCELLED_FOOD.getDesc() + "】");
			}
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
		
		public PrintFunc build(){
			return new PrintFunc(this);
		}
		
	}
	
	public static class DetailBuilder{
		private final int mPrinterId;
		private int mRepeat = 1;
		private final List<Kitchen> mKitchens = SortedList.newInstance();
		private final boolean extraEnabled;
		private final boolean cancelEnabled;
		
		public DetailBuilder(Printer printer, boolean extraEnabled, boolean cancelEnabled){
			this(printer.getId(), extraEnabled, cancelEnabled);
		}
		
		public DetailBuilder(int printerId, boolean extraEnabled, boolean cancelEnabled){
			this.mPrinterId = printerId;
			this.extraEnabled = extraEnabled;
			this.cancelEnabled = cancelEnabled;
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
		
		public PrintFunc[] build(){
			final PrintFunc[] result = new PrintFunc[2];
			result[0] = new PrintFunc(this, PType.PRINT_ORDER_DETAIL, extraEnabled);
			result[1] = new PrintFunc(this, PType.PRINT_CANCELLED_FOOD_DETAIL, cancelEnabled);
			return result;
		}
	}
	
	public static class Builder{
		private final int mPrinterId;
		private int mRepeat = 1;
		private PType mType ;
		private final List<Region> mRegions = SortedList.newInstance();
		private String mComment;
		
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
		
		public static Builder newTransferFood(int printerId){
			Builder builder = new Builder(printerId);
			builder.setType(PType.PRINT_TRANSFER_FOOD);
			return builder;
		}	
		
		public static Builder new2ndDisplay(int printerId){
			Builder builder = new Builder(printerId);
			builder.setType(PType.PRINT_2ND_DISPLAY);
			builder.setComment("客显");
			return builder;
		}
		
		public static Builder newWxOrder(int printerId){
			Builder builder = new Builder(printerId);
			builder.setType(PType.PRINT_WX_ORDER);
			return builder;
		}

		public static Builder newWxBook(int printerId){
			Builder builder = new Builder(printerId);
			builder.setType(PType.PRINT_BOOK);
			return builder;
		}
		
		public static Builder newWxWaiter(int printerId){
			Builder builder = new Builder(printerId);
			builder.setType(PType.PRINT_WX_WAITER);
			return builder;
		}
		
		public static Builder newWxCallPay(int printerId){
			Builder builder = new Builder(printerId);
			builder.setType(PType.PRINT_WX_CALL_PAY);
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
		
		public String getComment(){
			if(this.mComment == null){
				return "";
			}
			return this.mComment;
		}
		
		public void setComment(String comment){
			this.mComment = comment;
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
		setPrinterId(builder.mPrinterId);
		setType(builder.mPType);
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
		if(builder.isEnabledChanged()){
			enabled = builder.enabled == 1;
		}
	}
	
	private PrintFunc(SummaryBuilder builder){
		this(builder.type, builder.mRepeat);
		this.printerId = builder.printerId;
		this.mRegions.addAll(builder.mRegions);
		this.mDepts.addAll(builder.mDepts);
		this.mComment = builder.comment;
	}
	
	private PrintFunc(DetailBuilder builder, PType type, boolean enabled){
		this(type, builder.mRepeat);
		this.printerId = builder.mPrinterId;
		this.mKitchens.addAll(builder.mKitchens);
		this.enabled = enabled;
	}
	
	private PrintFunc(Builder builder){
		this(builder.getType(), builder.mRepeat);
		this.printerId = builder.mPrinterId;
		mRegions.addAll(builder.mRegions);
		mComment = builder.mComment;
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
	
	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean onOff) {
		this.enabled = onOff;
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
				   typeToCompare == PType.PRINT_WX_RECEIT ||
				   typeToCompare.isShift();
			
		}else{
			return typeToCompare == mType;
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
		jm.putInt("printerId", this.getPrinterId());
		jm.putInt("pTypeValue", this.mType.getVal());
		jm.putString("pTypeText", this.mType.getDesc());
		jm.putInt("repeat", this.mRepeat);
		jm.putString("comment", this.mComment);
		
		jm.putBoolean("enabled", this.enabled);
		
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
