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
	
	public void copyFrom(ComboFood src){
		if(src != null && this != src){
			food.copyFrom(src.food);
			amount = src.amount;
		}
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
		dest.writeByte(flag);
		if(flag == COMBO_FOOD_PARCELABLE_SIMPLE){
			dest.writeParcel(food, Food.FOOD_PARCELABLE_SIMPLE);
			
		}else if(flag == COMBO_FOOD_PARCELABLE_COMPLEX){
			dest.writeParcel(food, Food.FOOD_PARCELABLE_COMPLEX);
		}
		
		dest.writeInt(amount);

	}

	@Override
	public void createFromParcel(Parcel source) {
		source.readByte();
		food = source.readParcel(Food.CREATOR);
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

	public final static int COMBO_FOOD_JSONABLE_COMPLEX = 0;
	public final static int COMBO_FOOD_JSONABLE_SIMPLE = 1;
	
	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		if(flag == COMBO_FOOD_JSONABLE_COMPLEX){
			jm.putJsonable(food, Food.FOOD_JSONABLE_COMPLEX);
		}else if(flag == COMBO_FOOD_JSONABLE_SIMPLE){
			jm.putJsonable(food, Food.FOOD_JSONABLE_SIMPLE);
		}
		jm.putInt(Food.Key4Json.COMBO_AMOUNT.key, amount);
		return jm;
	}

	public static enum Key4Json{
		COMBO_FOOD_ID("comboFoodId", "子菜编号"),
		COMBO_FOOD_AMOUNT("comboFoodAmount", "子菜数量");
		
		Key4Json(String key, String desc){
			this.key = key;
			this.desc = desc;
		}
		
		private final String key;
		private final String desc;
		
		@Override
		public String toString(){
			return "key = " + key + ",desc = " + desc;
		}
		
	}
	
	@Override
	public void fromJsonMap(JsonMap jm, int flag) {
		this.food = new Food(jm.getInt(Key4Json.COMBO_FOOD_ID.key));
		this.amount = jm.getInt(Key4Json.COMBO_FOOD_AMOUNT.key);
	}
	
	public static Jsonable.Creator<ComboFood> JSON_CREATOR = new Jsonable.Creator<ComboFood>() {
		@Override
		public ComboFood newInstance() {
			return new ComboFood();
		}
	};
}
