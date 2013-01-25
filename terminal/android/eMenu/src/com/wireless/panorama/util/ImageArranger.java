package com.wireless.panorama.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import com.wireless.ordermenu.R;
import com.wireless.protocol.Food;
import com.wireless.protocol.Pager;

public class ImageArranger {
	private Activity mActivity;
	private ArrayList<Context> mContexts;
	private ArrayList<FramePager> mFoodGroups;
	
	public ImageArranger(Activity act, String packageName){
		mActivity = act;
		mContexts = (ArrayList<Context>) getPackageContexts(packageName);
		mFoodGroups = new ArrayList<FramePager>();
		
		FoodGroupProvider provider = FoodGroupProvider.getInstance();
		List<? extends Pager> groups = provider.getGroups();
		
		notifyFoodGroupsChanged(groups);
	}
	
	public void notifyFoodGroupsChanged(List<? extends Pager> groups){
		for(Pager p : groups){
			FramePager pager = new FramePager(p);
			pager.setFrameId(getFrameId());
			
			int layoutId  = getLayoutId(pager);
			if(layoutId != -1){
				pager.setLayoutId(layoutId);
				mFoodGroups.add(pager);
			}
		}
	}
	
	/**
	 * get all context matched by packageName
	 * @param packageName
	 * @return List<Context>, it might be empty
	 */
	public List<Context> getPackageContexts(String packageName){
		
		ArrayList<PackageInfo> layoutList = new ArrayList<PackageInfo>();
		List<PackageInfo> packs = mActivity.getPackageManager().getInstalledPackages(0);
		for(PackageInfo p: packs){
			
			if(isLayoutPackage(p.packageName, packageName)){
				layoutList.add(p);
			}
		}
		
		ArrayList<Context> contexts = new ArrayList<Context>();
		for(PackageInfo layoutPack : layoutList){
			try{
				Context context = mActivity.createPackageContext(layoutPack.packageName, Context.CONTEXT_IGNORE_SECURITY);
				contexts.add(context);
			} catch(NameNotFoundException e){
				
			}
		}
		
		return contexts;
	}
	
	/**
	 * get the context which name is matched by packageName
	 * @param packageName
	 * @return context if found
	 */
	public Context getContext(String packageName){
		if(mContexts != null){
			for(Context c : mContexts){
				if(isLayoutPackage(c.getPackageName(), packageName)){
					return c;
				} 
			}
		} 
		return null;
	}
	/**
	 * 判断是否是皮肤主题
	 * @param regex 要匹配的包名
	 */
	private boolean isLayoutPackage(String packageName, String regex)
	{
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(packageName);
		return matcher.find();
	}
	
	/**
	 * 根据菜品的数量组合出需要的layout名称
	 * 再根据名称找到layout ID
	 * 若找不到则返回 -1
	 * @param group
	 * @return
	 */
	private int getLayoutId(FramePager group){
		StringBuilder firstNameBuilder = new StringBuilder();
		//组合layout名称
		firstNameBuilder.append("l");
		if(group.hasLargeFoods())
			firstNameBuilder.append(group.getLargeFoods().length);
		else firstNameBuilder.append("0");
		
		firstNameBuilder.append("m");
		if(group.hasMediumFoods()) 
			firstNameBuilder.append(group.getMediumFoods().length);
		else firstNameBuilder.append("0");
		
		firstNameBuilder.append("s");
		if(group.hasSmallFoods())
			firstNameBuilder.append(group.getSmallFoods().length);
		else firstNameBuilder.append("0");
		
		firstNameBuilder.append("t");
		if(group.hasTextFoods())
			firstNameBuilder.append(group.getTextFoods().length);
		else firstNameBuilder.append("0");
		
		Context context = getContext(mActivity.getString(R.string.layout_packageName));

		if(context == null)
			return -1;
		
		StringBuilder lastNameBuilder;
		
		ArrayList<Integer> ids = new ArrayList<Integer>();
		//根据名称查找layout
		for(int i=0;i<10;i++){
			lastNameBuilder = new StringBuilder(firstNameBuilder);
			lastNameBuilder.append(i);
			int id = context.getResources().getIdentifier(lastNameBuilder.toString(), "layout", context.getPackageName());
			
			if(id != 0){
				//TODO 多个时增加筛选算法  
				ids.add(id);
			} 
		}
		if(ids.isEmpty())
			return -1;
		else {
			Calendar cale = Calendar.getInstance();
			int day = cale.get(Calendar.DAY_OF_YEAR);
			int index = day % ids.size();
			return ids.get(index);
		}
	}
	
	
	private int getFrameId(){
		//FIXME 修改成可拓展的
		Context context = getContext(mActivity.getString(R.string.layout_packageName));
		if(context == null)
			return -1;
		
		int id = context.getResources().getIdentifier(mActivity.getResources().getString(R.string.picture_frame),
				"drawable", context.getPackageName());
		
		if(id != 0){
			return id;
		}
		else return -1;
	}

	public ArrayList<FramePager> getGroups() {
		return mFoodGroups;
	}
	/**
	 * 返回特定的pager
	 * @param position
	 * @return 若未找到，则返回null
	 */
	public FramePager getGroup(int position){
		if(mFoodGroups.size() > position){
			return mFoodGroups.get(position);
		} else return null;
	}
	
	public Food getCaptainFood(int position){
		FramePager pager = getGroup(position);
		if(pager != null){
			return pager.getCaptainFood();
		} else return null;
	}
	
//	private class GroupProvider{
//		private ArrayList<FramePager> mGroups;
//
//		public GroupProvider() {
//			
//			ArrayList<Food> list1 = new ArrayList<Food>();
//			ArrayList<Food> list2 = new ArrayList<Food>();
//			ArrayList<Food> list3 = new ArrayList<Food>();
//			
//			
//			for(Food f : WirelessOrder.foodMenu.foods){
//				if(f.getKitchen().getAliasId() == 15)
//				{
//					list1.add(f); 
//				}
//				else if(f.getKitchen().getAliasId() == 11)
//					list2.add(f);
//				else if(f.getKitchen().getAliasId() == 6)
//					list3.add(f); 
//			}
//			
//			mGroups = new ArrayList<FramePager>();
//			
//			FramePager group1 = new FramePager(list1.subList(1, 2).toArray(new Food[1]), null, list1.subList(3, 6).toArray(new Food[3]), null, list1.get(0));
//			FramePager group2 = new FramePager(list2.subList(0, 2).toArray(new Food[2]), null, list2.subList(2, 4).toArray(new Food[2]), null, list2.get(0));
//			FramePager group3 = new FramePager(list3.subList(3, 6).toArray(new Food[3]), null, null, null, list3.get(0));
//			mGroups.add(group1);
//			mGroups.add(group2);
//			mGroups.add(group3);
//			
//			//找回captainFood所在部门
//			for(FramePager p : mGroups){
//				Food captainFood = p.getCaptainFood();
//				Kitchen kc = captainFood.getKitchen();
//				for(Kitchen k : WirelessOrder.foodMenu.kitchens){
//					if(kc.getAliasId() == k.getAliasId()){
//						captainFood.setKitchen(k);
//						break;
//					}
//				}
//			}
//			//按部门排序
//			Collections.sort(mGroups, new SortByDept());
//		}
//
//
//		public ArrayList<FramePager> getGroups() {
//			return mGroups;
//		}
//		
//		class SortByDept implements Comparator<FramePager>{
//
//			@Override
//			public int compare(FramePager lhs, FramePager rhs) {
//				Department left = lhs.getCaptainFood().getKitchen().getDept();
//				Department right = rhs.getCaptainFood().getKitchen().getDept();
//				
//				if(left.getId() > right.getId()){
//					return 1;
//				} else if (left.getId() < right.getId()){
//					return -1;
//				}
//				else return 0;
//			}
//
//		}
//	}
}
