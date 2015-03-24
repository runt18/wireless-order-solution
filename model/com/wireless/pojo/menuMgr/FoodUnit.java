package com.wireless.pojo.menuMgr;

import com.wireless.json.JsonMap;
import com.wireless.json.Jsonable;
import com.wireless.parcel.Parcel;
import com.wireless.parcel.Parcelable;

public class FoodUnit implements Parcelable, Jsonable{
	
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
	
	public static FoodUnit newInstance4CurPrice(float price){
		FoodUnit tmpUnit = new FoodUnit((int)Math.round(Math.random() * (99999999 - 50000000) + 50000000));
		tmpUnit.setUnit("时价");
		tmpUnit.setPrice(price);
		return tmpUnit;
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
	
	public static enum Key4Json{
		UNIT_ID("id", "单位id"),
		UNIT("unit", "单位"),
		PRICE("price", "价钱"),
		FOOD_ID("foodId", "foodId");
		
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
	
	public final static int CR_JSONABLE_4_COMMIT = 0; 
	
	
	public static Jsonable.Creator<FoodUnit> JSON_CREATOR = new Jsonable.Creator<FoodUnit>() {
		@Override
		public FoodUnit newInstance() {
			return new FoodUnit(0);
		}
	};

	@Override
	public JsonMap toJsonMap(int flag) {
		JsonMap jm = new JsonMap();
		jm.putFloat("price", getPrice());
		jm.putString("unit", getUnit());
		jm.putInt("id", getId());
		jm.putInt("foodId", getFoodId());
		return jm;
	}

	@Override
	public void fromJsonMap(JsonMap jsonMap, int flag) {
		if(flag == CR_JSONABLE_4_COMMIT){
			if(jsonMap.containsKey(Key4Json.UNIT_ID.key)){
				setId(jsonMap.getInt(Key4Json.UNIT_ID.key));
			}else{
				throw new IllegalStateException("提交的点菜数据缺少字段(" + Key4Json.UNIT_ID.desc + ")");
			}
			
			if(jsonMap.containsKey(Key4Json.UNIT.key)){
				setUnit(jsonMap.getString(Key4Json.UNIT.key));
			}else{
				throw new IllegalStateException("提交的点菜数据缺少字段(" + Key4Json.UNIT.desc + ")");
			}
			
			if(jsonMap.containsKey(Key4Json.PRICE.key)){
				setPrice(jsonMap.getFloat(Key4Json.PRICE.key));
			}else{
				throw new IllegalStateException("提交的点菜数据缺少字段(" + Key4Json.PRICE.desc + ")");
			}
		}		
	}
}
