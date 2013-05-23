package com.wireless.panorama.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import android.content.Context;

import com.wireless.pojo.foodGroup.Pager;
import com.wireless.pojo.menuMgr.Food;
import com.wireless.util.ContextLoader;
/**
 * 根据传入的pager列表，为每个列表分配layout
 * <p>
 * 如果没有找到对应layout，则不返回layout，
 * 找到的layout若有多个，则根据算法筛选一个</p>
 * 
 * <p>该类包含图片边框id的获取和背景id的获取功能，<b>默认都使用第一个</b></p>
 * 
 * <p>该类使用{@link ContextLoader} 来获取匹配的包，若有多个包，则<b>默认使用第一个匹配的包</b>
 * <br/>
 * 使用{@link Selector} 来筛选layout。可使用{@link #setSelector() } 方法设置不同的算法
 * </p>
 * @see #setSelector(Selector)
 */
public class LayoutArranger {
	private ArrayList<Context> mContexts;
	private ArrayList<FramePager> mFoodGroups;
	
	//默认使用的包索引
	private int mIndexOfContext = 0;
	//默认使用的背景索引
	private int mIndexOfBackground = 0;
	//默认使用的边框索引
	private int mIndexOfFrame = 0;

	/*
	 * 默认的selector实现
	 * 根据日期随机筛选
	 */
	private Selector mSelector = new Selector(){
		Random mRand = new Random(Calendar.getInstance().get(Calendar.DAY_OF_YEAR)); 
		@Override
		public int select(List<Integer> ids) {
			return ids.get(Math.abs(mRand.nextInt()) % ids.size());
		}
	};
	
	public LayoutArranger(Context context, String packageName){
		mContexts = (ArrayList<Context>) ContextLoader.getPackageContexts(context, packageName);
		mFoodGroups = new ArrayList<FramePager>();
	}
	
	public LayoutArranger(Context context, String packageName, List<? extends Pager> groups){
		mContexts = (ArrayList<Context>) ContextLoader.getPackageContexts(context, packageName);
		mFoodGroups = new ArrayList<FramePager>();
		
		notifyFoodGroupsChanged(groups);
	}
	/**
	 * 设置数据源
	 * <p>查找layout的id并将id分配给{@link Pager},并将所有包含layout的pager持有.
	 * <br/>
	 * 同时会查找和设置边框id和背景id</p>
	 */
	public void notifyFoodGroupsChanged(List<? extends Pager> groups){
		for(Pager p : groups){
			FramePager pager = new FramePager(p);
			pager.setFrameId(getFrameId());
			pager.setBackgroundId(getBackgroundId());
			
			int layoutId = getLayoutId(pager);
			if(layoutId != -1){
				pager.setLayoutId(layoutId);
				mFoodGroups.add(pager);
			}
		}
	}
	
	/**
	 * Get the context which name is matched by package name.
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
	 * @return 被{@link Selector }筛选出的id
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
		
		Context context = mContexts.get(mIndexOfContext);
		if(context == null)
			return -1;
		
		StringBuilder lastNameBuilder;
		
		ArrayList<Integer> ids = new ArrayList<Integer>();
		//根据名称查找layout
		for(int i = 0; i < 10; i++){
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
		
		context = mContexts.get(mIndexOfContext);
		if(context == null)
			return -1;
		
		ArrayList<Integer> ids = new ArrayList<Integer>();
		String firstName = "f";
		for(int i = 0; i < 10; i++){
			String lastName = firstName + i;
			int id = context.getResources().getIdentifier(lastName,	"drawable", context.getPackageName());
			if(id != 0){
				ids.add(id);
			}
		}
		if(ids.isEmpty())
			return -1;
		else {
			//FIXME 拓展,考虑根据什么条件来筛选背景样式
			return ids.get(mIndexOfFrame);
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
		
		context = mContexts.get(mIndexOfContext);
		if(context == null)
			return -1;
		
		ArrayList<Integer> bgs = new ArrayList<Integer>();
		String firstName = "b";
		for(int i = 0; i < 10; i++){
			String lastName = firstName + i;
			
			int id = context.getResources().getIdentifier(lastName, "drawable", context.getPackageName());
			if(id != 0){
				bgs.add(id);
			}
		}
		if(bgs.isEmpty()){
			return -1;
		}else{
			return bgs.get(mIndexOfBackground);
		}
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
	
	/**
	 * 筛选layout算法的接口
	 * @author ggdsn1
	 *
	 */
	public interface Selector{
		public int select(List<Integer> ids);
	}
}
