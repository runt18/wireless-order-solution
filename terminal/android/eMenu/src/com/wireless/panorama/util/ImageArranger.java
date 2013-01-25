package com.wireless.panorama.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.Context;

import com.wireless.ordermenu.R;
import com.wireless.protocol.Food;
import com.wireless.protocol.Pager;

public class ImageArranger {
	private Activity mActivity;
	private ArrayList<Context> mContexts;
	private ArrayList<FramePager> mFoodGroups;
	
	public ImageArranger(Activity act, String packageName){
		mActivity = act;
		mContexts = (ArrayList<Context>) ContextLoader.getPackageContexts(act, packageName);
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
	 * get the context which name is matched by packageName
	 * @param packageName
	 * @return context if found
	 */
	public Context getContext(String packageName){
		if(mContexts != null){
			for(Context c : mContexts){
				if(ContextLoader.isMatchedPackage(c, packageName)){
					return c;
				} 
			}
		} 
		return null;
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
		
//		Context context = getContext(mActivity.getString(R.string.layout_packageName));

		if(mContexts == null && mContexts.isEmpty())
			return -1;
		
		Context context = mContexts.get(0);
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
			int index;
			Random rand = new Random(System.currentTimeMillis());
			index = rand.nextInt(ids.size());
			return ids.get(index);
		}
	}
	
	
	private int getFrameId(){
		//FIXME 修改成可拓展的
		if(mContexts == null && mContexts.isEmpty())
			return -1;
		Context context = null;
		
		context = mContexts.get(0);
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
	
}
