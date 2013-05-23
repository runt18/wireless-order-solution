package com.wireless.pojo.foodGroup;

import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;
import com.wireless.pojo.menuMgr.Food;


public class Pager implements Parcelable{
	
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
		setLargeFoods(largeFoods);
		setMediumFoods(mediumFoods);
		setSmallFoods(smallFoods);
		setTextFoods(textFoods);
		setCaptainFood(captainFood);
	}

	public int getLayoutId(){
		return this.mLayoutId;
	}
	
	public void setLayoutId(int layoutId){
		this.mLayoutId = layoutId;
	}
	
	public boolean hasLayoutId(){
		if(mLayoutId <= 0)
			return false;
		else return true;
	}
	
	public Food[] getLargeFoods() {
		return mLargeFoods;
	}

	public void setLargeFoods(Food[] largeFoods) {
		if(largeFoods != null){
			this.mLargeFoods = largeFoods;
		}else{
			this.mLargeFoods = new Food[0];
		}
	}
	
	public boolean hasLargeFoods(){
		if(mLargeFoods != null && mLargeFoods.length != 0)
			return true;
		else return false;
	}

	public Food[] getMediumFoods() {
		return mMediumFoods;
	}

	public void setMediumFoods(Food[] mediumFoods) {
		if(mediumFoods != null){
			this.mMediumFoods = mediumFoods;
		}else{
			this.mMediumFoods = new Food[0];
		}
	}

	public boolean hasMediumFoods(){
		if(mMediumFoods != null && mMediumFoods.length != 0)
			return true;
		else return false;
	}
	
	public Food[] getSmallFoods() {
		return mSmallFoods;
	}

	public void setSmallFoods(Food[] smallFoods) {
		if(smallFoods != null){
			this.mSmallFoods = smallFoods;
		}else{
			this.mSmallFoods = new Food[0];
		}
	}
	
	public boolean hasSmallFoods(){
		if(mSmallFoods != null && mSmallFoods.length != 0)
			return true;
		else return false;
	}
	
	public Food[] getTextFoods() {
		return mTextFoods;
	}

	public void setTextFoods(Food[] textFoods) {
		if(textFoods != null){
			this.mTextFoods = textFoods;
		}else{
			this.mTextFoods = new Food[0];
		}
	}
	
	public boolean hasTextFoods(){
		if(mTextFoods != null && mTextFoods.length != 0)
			return true;
		else return false;
	}

	public Food getCaptainFood() {
		return mCaptainFood;
	}

	public void setCaptainFood(Food captainFood) {
		if(captainFood != null){
			this.mCaptainFood = captainFood;
		}else{
			this.mCaptainFood = new Food();
		}
	}
	
	public boolean hasCaptainFood(){
		if(mCaptainFood != null && mCaptainFood.getAliasId() != 0) return true;
		else return false;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Pager)){
			return false;
		}else{
			try{
				for(int i = 0; i < mLargeFoods.length; i++){
					Food f = ((Pager)(obj)).mLargeFoods[i];
					if(f == null || !mLargeFoods[i].equals(f)){
						return false;
					}
				}
				
				for(int i = 0; i < mMediumFoods.length; i++){
					Food f = ((Pager)(obj)).mMediumFoods[i];
					if(f == null || !mMediumFoods[i].equals(f)){
						return false;
					}
				}
				
				for(int i = 0; i < mSmallFoods.length; i++){
					Food f = ((Pager)(obj)).mSmallFoods[i];
					if(f == null || !mSmallFoods[i].equals(f)){
						return false;
					}
				}
				
				for(int i = 0; i < mTextFoods.length; i++){
					Food f = ((Pager)(obj)).mTextFoods[i];
					if(f == null || !mTextFoods[i].equals(f)){
						return false;
					}
				}
				
				if(!mCaptainFood.equals(((Pager)(obj)).mCaptainFood)){
					return false;
				}
				
				return true;
				
			}catch(NullPointerException e){
				return false;
				
			}catch(ArrayIndexOutOfBoundsException e){
				return false;
			}
		}
	}
	
	@Override
	public String toString(){
		StringBuffer largeFoodAlias = new StringBuffer();
		for(Food f : mLargeFoods){
			if(largeFoodAlias.length() == 0){
				largeFoodAlias.append(f.getAliasId());
			}else{
				largeFoodAlias.append(",").append(f.getAliasId());
			}
		}
		
		StringBuffer mediumFoodAlias = new StringBuffer();
		for(Food f : mMediumFoods){
			if(mediumFoodAlias.length() == 0){
				mediumFoodAlias.append(f.getAliasId());
			}else{
				mediumFoodAlias.append(",").append(f.getAliasId());
			}
		}
		
		StringBuffer smallFoodAlias = new StringBuffer();
		for(Food f : mSmallFoods){
			if(smallFoodAlias.length() == 0){
				smallFoodAlias.append(f.getAliasId());
			}else{
				smallFoodAlias.append(",").append(f.getAliasId());
			}
		}
		
		StringBuffer textFoodAlias = new StringBuffer();
		for(Food f : mTextFoods){
			if(textFoodAlias.length() == 0){
				textFoodAlias.append(f.getAliasId());
			}else{
				textFoodAlias.append(",").append(f.getAliasId());
			}
		}
		return "caption(" + mCaptainFood.getAliasId() + ")" +
			   (largeFoodAlias.length() != 0 ? ", large(" + largeFoodAlias + ")" : "") +
			   (mediumFoodAlias.length() != 0 ? ", medium(" + mediumFoodAlias + ")" : "") +
			   (smallFoodAlias.length() != 0 ? ", small(" + smallFoodAlias + ")" : "") +
			   (textFoodAlias.length() != 0 ? ", text(" + textFoodAlias + ")" : "");
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeInt(this.mLayoutId);
		dest.writeParcelArray(this.mLargeFoods, Food.FOOD_PARCELABLE_SIMPLE);
		dest.writeParcelArray(this.mMediumFoods, Food.FOOD_PARCELABLE_SIMPLE);
		dest.writeParcelArray(this.mSmallFoods, Food.FOOD_PARCELABLE_SIMPLE);
		dest.writeParcelArray(this.mTextFoods, Food.FOOD_PARCELABLE_SIMPLE);
		dest.writeParcel(this.mCaptainFood, Food.FOOD_PARCELABLE_SIMPLE);
	}

	@Override
	public void createFromParcel(Parcel source) {
		this.mLayoutId = source.readInt();
		
		this.mLargeFoods = source.readParcelArray(Food.CREATOR);
		
		this.mMediumFoods = source.readParcelArray(Food.CREATOR);
		
		this.mSmallFoods = source.readParcelArray(Food.CREATOR);
		
		this.mTextFoods = source.readParcelArray(Food.CREATOR);
		
		this.mCaptainFood = source.readParcel(Food.CREATOR);
	}
	
	public final static Parcelable.Creator<Pager> PAGER_CREATOR = new Parcelable.Creator<Pager>(){
		public Pager newInstance() {
			return new Pager();
		}
		
		public Pager[] newInstance(int size){
			return new Pager[size];
		}
	};
}
