package com.wireless.pojo.menuMgr;

import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;

public class FoodUnit implements Parcelable{
	
	public static class InsertBuilder{
		public final int foodId;
		public final float price;
		private String unit;
		
		public InsertBuilder(int foodId, float price, String unit){
			this.foodId = foodId;
			this.price = price;
			this.unit = unit;
		}
		
		public FoodUnit build(){
			return new FoodUnit(this);
		}
	}
	
	private int id;
	private int foodId;
	private float price;
	private String unit;
	
	private FoodUnit(InsertBuilder builder){
		this.foodId = builder.foodId;
		this.price = builder.price;
		this.unit = builder.unit;
	}

	FoodUnit(float price, String unit){
		this.price = price;
		this.unit = unit;
	}
	
	public FoodUnit(int id){
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getFoodId() {
		return foodId;
	}
	
	public void setFoodId(int foodId) {
		this.foodId = foodId;
	}
	
	public float getPrice() {
		return price;
	}
	
	public void setPrice(float price) {
		this.price = price;
	}
	
	public String getUnit() {
		if(unit == null){
			return "";
		}
		return unit;
	}
	
	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	@Override
	public int hashCode(){
		return id * 31 + 17;
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof FoodUnit)){
			return false;
		}else{
			return id == ((FoodUnit)obj).id;
		}
	}
	
	@Override
	public String toString(){
		return this.price + "/" + unit;
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeInt(this.id);
		dest.writeInt(this.foodId);
		dest.writeFloat(this.price);
		dest.writeString(this.unit);
	}

	@Override
	public void createFromParcel(Parcel source) {
		this.id = source.readInt();
		this.foodId = source.readInt();
		this.price = source.readFloat();
		this.unit = source.readString();
	}
	
	public final static Parcelable.Creator<FoodUnit> CREATOR = new Parcelable.Creator<FoodUnit>(){

		@Override
		public FoodUnit newInstance() {
			return new FoodUnit(0);
		}
		
		@Override
		public FoodUnit[] newInstance(int size){
			return new FoodUnit[size];
		}
		
	};
}
