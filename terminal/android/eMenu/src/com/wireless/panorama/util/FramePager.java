package com.wireless.panorama.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.wireless.protocol.Food;
import com.wireless.protocol.Pager;
/**
 * 拓展的pager
 * 增加了背景属性
 * @author ggdsn1
 *
 */
public class FramePager extends Pager {

	protected int mFrameId;
	public FramePager() {
		super();
		mFrameId = -1;
	}

	public FramePager(Food[] largeFoods, Food[] mediumFoods, Food[] smallFoods,
			Food[] textFoods, Food captainFood) {
		super(largeFoods, mediumFoods, smallFoods, textFoods, captainFood);
		mFrameId = -1;
	}
	
	public FramePager(Food[] largeFoods, Food[] mediumFoods, Food[] smallFoods,
			Food[] textFoods, Food captainFood, int frameId) {
		super(largeFoods, mediumFoods, smallFoods, textFoods, captainFood);
		this.mFrameId = frameId;
	}
	
	public FramePager(Pager p){
		super(p.getLargeFoods(), p.getMediumFoods(), p.getSmallFoods(), p.getTextFoods(), p.getCaptainFood());
		mFrameId = -1;
	}

	public int getFrameId() {
		return mFrameId;
	}

	public void setFrameId(int frameId) {
		this.mFrameId = frameId;
	}
	
	public boolean hasFrameId(){
		return mFrameId >= 0 ? true : false; 
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
}
