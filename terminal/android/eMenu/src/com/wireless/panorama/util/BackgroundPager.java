package com.wireless.panorama.util;

import com.wireless.protocol.Food;
import com.wireless.protocol.Pager;

/**
 * 带有背景id的pager，拓展自{@link FramePager}
 * @author ggdsn1
 *
 */
public class BackgroundPager extends FramePager {
	protected int mBackgroundId = -1;

	public BackgroundPager() {
		super();
	}

	public BackgroundPager(Food[] largeFoods, Food[] mediumFoods, Food[] smallFoods, Food[] textFoods, Food captainFood, int frameId) {
		super(largeFoods, mediumFoods, smallFoods, textFoods, captainFood, frameId);
	}

	public BackgroundPager(Food[] largeFoods, Food[] mediumFoods,
			Food[] smallFoods, Food[] textFoods, Food captainFood) {
		super(largeFoods, mediumFoods, smallFoods, textFoods, captainFood);
	}

	public BackgroundPager(Pager p) {
		super(p);
	}
	
	public BackgroundPager(Food[] largeFoods, Food[] mediumFoods, Food[] smallFoods, Food[] textFoods, Food captainFood, int frameId, int bgId) {
		super(largeFoods, mediumFoods, smallFoods, textFoods, captainFood, frameId);
		mBackgroundId = bgId;
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
