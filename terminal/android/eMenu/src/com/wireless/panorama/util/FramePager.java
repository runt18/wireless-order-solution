package com.wireless.panorama.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.wireless.pojo.foodGroup.Pager;
import com.wireless.pojo.menuMgr.Food;
/**
 * 拓展的pager
 * 增加了背景属性, 边框（图片背景）,和获取全部菜品的功能
 * @author ggdsn1
 *
 */
public class FramePager extends Pager {
	protected int mBackgroundId = -1;

	protected int mFrameId = -1;
	public FramePager() {
		super();
	}

	public FramePager(Food[] largeFoods, Food[] mediumFoods, Food[] smallFoods,
			Food[] textFoods, Food captainFood) {
		super(largeFoods, mediumFoods, smallFoods, textFoods, captainFood);
	}
	
	public FramePager(Food[] largeFoods, Food[] mediumFoods, Food[] smallFoods,
			Food[] textFoods, Food captainFood, int frameId, int bgId) {
		super(largeFoods, mediumFoods, smallFoods, textFoods, captainFood);
		this.mFrameId = frameId;
		mBackgroundId = bgId;
	}
	
	public FramePager(Pager p){
		super(p.getLargeFoods(), p.getMediumFoods(), p.getSmallFoods(), p.getTextFoods(), p.getCaptainFood());
	}

	public int getFrameId() {
		return mFrameId;
	}

	public void setFrameId(int frameId) {
		this.mFrameId = frameId;
	}
	
	public boolean hasFrameId(){
		return mFrameId > 0 ? true : false; 
	}
	
	public List<Food> getAllFoodsByList(){
		List<Food> foods = new ArrayList<Food>();
		foods.addAll(Arrays.asList(getLargeFoods()));
		foods.addAll(Arrays.asList(getMediumFoods()));
		foods.addAll(Arrays.asList(getSmallFoods()));
		foods.addAll(Arrays.asList(getTextFoods()));
		
		return foods;
	}
	
	public Food[] getAllFoodsByArray(){
		List<Food> foods = getAllFoodsByList();
		return foods.toArray(new Food[foods.size()]);
	}
	
	public int getBackgroundId() {
		return mBackgroundId;
	}

	public void setBackgroundId(int backgroundId) {
		if(backgroundId > 0)
			this.mBackgroundId = backgroundId;
	}
	
	public boolean hasBackgroundId(){
		return mBackgroundId > 0 ? true : false;
	}
}
