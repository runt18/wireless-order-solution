package com.wireless.panorama.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.wireless.common.WirelessOrder;
import com.wireless.ordermenu.R;
import com.wireless.protocol.Food;
import com.wireless.protocol.Kitchen;

public class ImageArranger {
	private Activity mActivity;
	private ArrayList<Context> mContexts;
	private ArrayList<PanoramaGroup> mFoodGroups;
	
	public ImageArranger(Activity act, String packageName){
		mActivity = act;
		mContexts = (ArrayList<Context>) getPackageContexts(packageName);
		mFoodGroups = new ArrayList<PanoramaGroup>();
		
		GroupProvider provider = new GroupProvider();
		ArrayList<PanoramaGroup> mGroups = provider.getGroups();
		
		for(PanoramaGroup g : mGroups){
			int layoutId  = getLayoutId(g);
			if(layoutId != -1){
				g.setLayoutId(layoutId);
				mFoodGroups.add(g);
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
	
	private int getLayoutId(PanoramaGroup group){
		StringBuilder firstNameBuilder = new StringBuilder();
		firstNameBuilder.append("l");
		firstNameBuilder.append(group.getLargeCount());
		firstNameBuilder.append("s");
		firstNameBuilder.append(group.getSmallCount());
		
		Context context = getContext(mActivity.getString(R.string.layout_packageName));

		StringBuilder lastNameBuilder;
		for(int i=0;i<10;i++){
			lastNameBuilder = new StringBuilder(firstNameBuilder);
			lastNameBuilder.append(i);
			Log.i("name",lastNameBuilder.toString());
			int id = context.getResources().getIdentifier(lastNameBuilder.toString(), "layout", context.getPackageName());
			
			if(id != 0){
				//TODO 多个时增加筛选算法 
				return id;
			} 
		}
		return -1;
	}
	

	public ArrayList<PanoramaGroup> getGroups() {
		return mFoodGroups;
	}


	private class GroupProvider{
		private ArrayList<PanoramaGroup> mGroups;

		public GroupProvider() {
			
			ArrayList<Food> list1 = new ArrayList<Food>();
			ArrayList<Food> list2 = new ArrayList<Food>();
			ArrayList<Food> list3 = new ArrayList<Food>();
			
			
			for(Food f : WirelessOrder.foodMenu.foods){
				if(f.kitchen.getAliasId() == 15)
				{
					list1.add(f);
				}
				else if(f.kitchen.getAliasId() == 11)
					list2.add(f);
				else if(f.kitchen.getAliasId() == 6)
					list3.add(f);
			}
			
			mGroups = new ArrayList<PanoramaGroup>();
			
			PanoramaGroup group1 = new PanoramaGroup(list1.get(0).kitchen, list1.subList(1, 2), list1.subList(3, 6));
			PanoramaGroup group2 = new PanoramaGroup(list2.get(0).kitchen, list2.subList(0, 2), list2.subList(2, 4));
			PanoramaGroup group3 = new PanoramaGroup(list3.get(0).kitchen, list3.subList(3, 6), null);
			mGroups.add(group1);
			mGroups.add(group2);
			mGroups.add(group3);
			
		}

		public PanoramaGroup getGroup(int index){
			return mGroups.get(index);
		}

		public ArrayList<PanoramaGroup> getGroups() {
			return mGroups;
		}
	}
}
