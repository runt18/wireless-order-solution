package com.wireless.panorama.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.Context;

import com.wireless.protocol.Food;
import com.wireless.protocol.Pager;
/**
 * 根据传入的pager列表，为每个列表分配layout
 * 如果没有找到对应layout，则不返回layout
 * 
 * 找到的layout若有多个，则根据算法筛选
 * @author ggdsn1
 *
 */
public class LayoutArranger {
	private ArrayList<Context> mContexts;
	private ArrayList<FramePager> mFoodGroups;
	/*
	 * 默认的selector实现
	 * 根据日期随机筛选
	 */
	private Selector mSelector = new Selector(){
		Random mRand = new Random(Calendar.getInstance().get(Calendar.DAY_OF_YEAR)); 
		@Override
		public int select(List<Integer> ids) {
			int index;
			index = Math.abs(mRand.nextInt()) % ids.size();
			return ids.get(index);
		}
	};
	
	public LayoutArranger(Activity act, String packageName){
		mContexts = (ArrayList<Context>) ContextLoader.getPackageContexts(act, packageName);
		mFoodGroups = new ArrayList<FramePager>();
	}
	
	public LayoutArranger(Activity act, String packageName, List<? extends Pager> groups){
		mContexts = (ArrayList<Context>) ContextLoader.getPackageContexts(act, packageName);
		mFoodGroups = new ArrayList<FramePager>();
		
		notifyFoodGroupsChanged(groups);
	}
	/**
	 * 设置数据源
	 */
	public void notifyFoodGroupsChanged(List<? extends Pager> groups){
		for(Pager p : groups){
			BackgroundPager pager = new BackgroundPager(p);
			pager.setFrameId(getFrameId());
			pager.setBackgroundId(getBackgroundId());
			
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
		
		if(mContexts == null || mContexts.isEmpty())
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
				ids.add(id);
			} 
		}
		if(ids.isEmpty())
			return -1;
		else {
			return mSelector.select(ids);
		}
	}
	
	/**
	 * 查找并返回边框id，若有多个则返回第一个
	 * @return
	 */
	private int getFrameId(){
		if(mContexts == null || mContexts.isEmpty())
			return -1;
		Context context = null;
		
		context = mContexts.get(0);
		if(context == null)
			return -1;
		
		ArrayList<Integer> ids = new ArrayList<Integer>();
		String firstName = "f";
		for(int i=0;i<10;i++){
			String lastName = firstName + i;
			int id = context.getResources().getIdentifier(lastName,
					"drawable", context.getPackageName());
			if(id != 0)
				ids.add(id);
		}
		if(ids.isEmpty())
			return -1;
		else {
			//FIXME 拓展,考虑根据什么条件来筛选背景样式
			return ids.get(0);
		}
	}
	
	/**
	 * 查找并返回backgroundId 若有多个则返回第一个
	 * @return
	 */
	private int getBackgroundId(){
		if(mContexts == null || mContexts.isEmpty())
			return -1;
		Context context = null;
		
		context = mContexts.get(0);
		if(context == null)
			return -1;
		
		ArrayList<Integer> bgs = new ArrayList<Integer>();
		String firstName = "b";
		for(int i=0;i<10;i++){
			String lastName = firstName + i;
			
			int id = context.getResources().getIdentifier(lastName, "drawable", context.getPackageName());
			if(id != 0)
				bgs.add(id);
		}
		if(bgs.isEmpty())
			return -1;
		else return bgs.get(0);
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
	
	public void setSelector(Selector arr){
		mSelector = arr;
	}
	
	public interface Selector{
		public int select(List<Integer> ids);
	}
}
