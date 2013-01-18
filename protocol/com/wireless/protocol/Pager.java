package com.wireless.protocol;

public class Pager {
	
	int mLayoutId;			//the id to layout this pager uses
	
	Food[] mLargeFoods;		//the foods belongs to large style
	Food[] mMediumFoods;	//the foods belongs to medium style
	Food[] mSmallFoods;		//the foods belongs to small style
	Food[] mTextFoods;		//the foods belongs to text style
			
	Food mCaptainFood;		//the captain food to this pager
	
	public Pager(){
		this.mLargeFoods = new Food[0];
		this.mMediumFoods = new Food[0];
		this.mSmallFoods = new Food[0];
		this.mTextFoods = new Food[0];
		this.mCaptainFood = new Food();
	}
	
	public Pager(Food[] largeFoods, Food[] mediumFoods, Food[] smallFoods, Food[] textFoods, Food captainFood){
		this.mLargeFoods = largeFoods;
		this.mMediumFoods = mediumFoods;
		this.mSmallFoods = smallFoods;
		this.mTextFoods = textFoods;
		this.mCaptainFood = captainFood;
	}

	public int getLayoutId(){
		return this.mLayoutId;
	}
	
	public void setLayoutId(int layoutId){
		this.mLayoutId = layoutId;
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
	
	@Override
	public String toString(){
		StringBuffer largeFoodAlias = new StringBuffer();
		for(Food f : mLargeFoods){
			if(largeFoodAlias.length() == 0){
				largeFoodAlias.append(f.getAliasId());
			}else{
				largeFoodAlias.append(f.getAliasId()).append(",");
			}
		}
		
		StringBuffer mediumFoodAlias = new StringBuffer();
		for(Food f : mMediumFoods){
			if(largeFoodAlias.length() == 0){
				mediumFoodAlias.append(f.getAliasId());
			}else{
				mediumFoodAlias.append(f.getAliasId()).append(",");
			}
		}
		
		StringBuffer smallFoodAlias = new StringBuffer();
		for(Food f : mSmallFoods){
			if(largeFoodAlias.length() == 0){
				smallFoodAlias.append(f.getAliasId());
			}else{
				smallFoodAlias.append(f.getAliasId()).append(",");
			}
		}
		
		StringBuffer textFoodAlias = new StringBuffer();
		for(Food f : mTextFoods){
			if(largeFoodAlias.length() == 0){
				textFoodAlias.append(f.getAliasId());
			}else{
				textFoodAlias.append(f.getAliasId()).append(",");
			}
		}
		return "caption(" + mCaptainFood.getAliasId() + ") " +
			   (largeFoodAlias.length() != 0 ? ",large(" + largeFoodAlias + ")," : "") +
			   (mediumFoodAlias.length() != 0 ? ",medium(" + mediumFoodAlias + ")," : "") +
			   (smallFoodAlias.length() != 0 ? ",small(" + smallFoodAlias + ")," : "") +
			   (textFoodAlias.length() != 0 ? ",text(" + textFoodAlias + ")," : "");
	}
}
