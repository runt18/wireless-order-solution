package com.wireless.protocol;

public class Pager {
	
	Food[] mLargeFoods;		//the foods belongs to large style
	Food[] mMediumFoods;	//the foods belongs to medium style
	Food[] mSmallFoods;		//the foods belongs to small style
	Food[] mTextFoods;		//the foods belongs to text style
			
	Food mCaptainFood;		//the captain food to this pager
	
	public Pager(){
		
	}
	
	public Pager(Food[] largeFoods, Food[] mediumFoods, Food[] smallFoods, Food[] textFoods, Food captainFood){
		this.mLargeFoods = largeFoods;
		this.mMediumFoods = mediumFoods;
		this.mSmallFoods = smallFoods;
		this.mTextFoods = textFoods;
		this.mCaptainFood = captainFood;
	}

	public Food[] getLargeFoods() {
		return mLargeFoods;
	}

	public void setLargeFoods(Food[] largeFoods) {
		this.mLargeFoods = largeFoods;
	}

	public Food[] getMediumFoods() {
		return mMediumFoods;
	}

	public void setMediumFoods(Food[] mediumFoods) {
		this.mMediumFoods = mediumFoods;
	}

	public Food[] getSmallFoods() {
		return mSmallFoods;
	}

	public void setSmallFoods(Food[] smallFoods) {
		this.mSmallFoods = smallFoods;
	}

	public Food[] getTextFoods() {
		return mTextFoods;
	}

	public void setTextFoods(Food[] textFoods) {
		this.mTextFoods = textFoods;
	}

	public Food getCaptainFood() {
		return mCaptainFood;
	}

	public void setCaptainFood(Food captainFood) {
		this.mCaptainFood = captainFood;
	}
	
	
}
