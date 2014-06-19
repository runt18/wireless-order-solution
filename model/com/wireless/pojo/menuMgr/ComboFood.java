package com.wireless.pojo.menuMgr;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;

public class ComboFood implements Parcelable, Jsonable{
	
	public static final byte COMBO_FOOD_PARCELABLE_COMPLEX = 0;
	public static final byte COMBO_FOOD_PARCELABLE_SIMPLE = 1;
	
	private int amount;
	private Food food;
	
	private ComboFood(){
	}
	
	public ComboFood(Food food, int amount){
		this.food = food;
		this.amount = amount;
	}
	
	@Override
	public int hashCode(){
		return food.hashCode();
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof ComboFood)){
			return false;
		}else{
			return food.equals(((ComboFood)obj).food);
		}
	}
	
	public int getAmount(){
		return this.amount;
	}
	
	public int getFoodId(){
		if(food != null){
			return this.food.getFoodId();
		}else{
			return 0;
		}
	}
	
	public String getName(){
		if(food != null){
			return food.getName();
		}else{
			return "";
		}
	}
	
	public Food asFood(){
		return this.food;
	}
	
	@Override
	public String toString(){
		return food.toString() + ", " + amount;
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeInt(food.getFoodId());
		dest.writeInt(food.getRestaurantId());
		dest.writeInt(amount);
	}

	@Override
	public void createFromParcel(Parcel source) {
		food = new Food(source.readInt());
		food.setRestaurantId(source.readInt());
		amount = source.readInt();
	}
	
	public final static Parcelable.Creator<ComboFood> CREATOR = new Parcelable.Creator<ComboFood>(){

		@Override
		public ComboFood newInstance() {
			return new ComboFood();
		}
		
		@Override
		public ComboFood[] newInstance(int size){
			return new ComboFood[size];
		}
		
	};

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putJsonable(food, Food.FOOD_JSONABLE_COMPLEX);
		jm.putInt(Food.Key4Json.COMBO_AMOUNT.key, amount);
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		
	}
}
