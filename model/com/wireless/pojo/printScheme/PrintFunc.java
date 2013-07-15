package com.wireless.pojo.printScheme;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.wireless.pojo.menuMgr.Department;
import com.wireless.pojo.menuMgr.Kitchen;
import com.wireless.pojo.regionMgr.Region;

public class PrintFunc {
	
	private final PType mType;
	
	private final List<Region> mRegions = new ArrayList<Region>();
	
	private Department mDept;
	
	private final List<Kitchen> mKitchens = new ArrayList<Kitchen>();
	
	private final int mRepeat;
	
	private PrintFunc(PType type, int repeat){
		this.mType = type;
		this.mRepeat = repeat;
	}
	
	public static PrintFunc newSummaryFunc(int repeat){
		return new PrintFunc(PType.PRINT_ORDER, repeat);
	}
	
	public static PrintFunc newSummaryFunc(Department dept, List<Region> regions, int repeat){
		PrintFunc printFunc = new PrintFunc(PType.PRINT_ORDER, repeat);
		printFunc.mDept = dept;
		printFunc.mRegions.addAll(regions);
		return printFunc;
	}
	
	public static PrintFunc newSummaryFunc(Department dept, int repeat){
		PrintFunc printFunc = new PrintFunc(PType.PRINT_ORDER, repeat);
		printFunc.mDept = dept;
		return printFunc;
	}
	
	public static PrintFunc newSummaryFunc(List<Region> regions, int repeat){
		PrintFunc printFunc = new PrintFunc(PType.PRINT_ORDER, repeat);
		printFunc.mRegions.addAll(regions);
		return printFunc;
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
	
	List<Region> getRegions(){
		return Collections.unmodifiableList(mRegions);
	}
	
	List<Kitchen> getKitchens(){
		return Collections.unmodifiableList(mKitchens);
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
}
