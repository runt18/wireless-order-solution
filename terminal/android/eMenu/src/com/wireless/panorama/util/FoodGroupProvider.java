package com.wireless.panorama.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.wireless.common.WirelessOrder;
import com.wireless.protocol.Food;
import com.wireless.protocol.PDepartment;
import com.wireless.protocol.PKitchen;
import com.wireless.protocol.Pager;
/**
 * 储存获取到的pager，并将pager按部门排序
 * @author ggdsn1
 *
 */
public class FoodGroupProvider {
	private static FoodGroupProvider mProvider;
	public static FoodGroupProvider getInstance(){
		if(mProvider != null)
			return mProvider;
		else {
			mProvider = new FoodGroupProvider();
			return mProvider;
		}
	}

	private List<? extends Pager> mPagers;
	
	private FoodGroupProvider(){}
	
	public void setGroups(Pager[] pagers){
		if(pagers != null && pagers.length != 0)
			mPagers = sortGroups(Arrays.asList(pagers));
		else mPagers = null;
	}
	
	public void setGroups(List<? extends Pager> pagers){
		if(pagers != null && !pagers.isEmpty())
			mPagers = sortGroups(pagers);
		else mPagers = null;
	}

	public List<? extends Pager> getGroups() {
		return mPagers;
	}
	
	public boolean hasGroup(){
		if(mPagers != null && !mPagers.isEmpty())
			return true;
		else return false;
	}
	
	private List<? extends Pager> sortGroups(List<? extends Pager> pagers){
		//找回captainFood所在部门
		for(Pager p : pagers){
			Food captainFood = p.getCaptainFood();
			PKitchen kc = captainFood.getKitchen();
			for(PKitchen k : WirelessOrder.foodMenu.kitchens){
				if(kc.getAliasId() == k.getAliasId()){
					captainFood.setKitchen(k);
					break;
				}
			}
		}
		//按部门排序
		Collections.sort(pagers, new SortByDept());
		return pagers;
	}
	
	class SortByDept implements Comparator<Pager>{

		@Override
		public int compare(Pager lhs, Pager rhs) {
			PDepartment left = lhs.getCaptainFood().getKitchen().getDept();
			PDepartment right = rhs.getCaptainFood().getKitchen().getDept();
			
			if(left.getId() > right.getId()){
				return 1;
			} else if (left.getId() < right.getId()){
				return -1;
			}else{
				return 0;
			}
		}

	}
}
