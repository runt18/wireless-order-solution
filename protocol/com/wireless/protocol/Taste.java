package com.wireless.protocol;

import com.wireless.pojo.util.NumericUtil;
import com.wireless.protocol.parcel.Parcel;
import com.wireless.protocol.parcel.Parcelable;

public class Taste implements Parcelable{
	
	public final static byte TASTE_PARCELABLE_COMPLEX = 0;
	public final static byte TASTE_PARCELABLE_SIMPLE = 1;
	
	private final static String NO_PREFERENCE = "无口味"; 
	
	public final static short CATE_ALL = Short.MIN_VALUE;	/* 全部 */
	public final static short CATE_TASTE = 0;				/* 口味 */
	public final static short CATE_STYLE = 1;				/* 做法 */
	public final static short CATE_SPEC = 2;				/* 规格 */
	
	public final static short CALC_PRICE = 0;				/* 按价格计算  */
	public final static short CALC_RATE = 1;				/* 按比例计算  */
	
	public final static short TYPE_NORMAL = 0;				/* 一般 */
	public final static short TYPE_RESERVED = 1;			/* 保留 */
	
	int restaurantId;

	int tasteId;
	
	int aliasId;
	
	short category = Taste.CATE_TASTE;
	
	short type = TYPE_NORMAL;
	
	short calc = Taste.CALC_PRICE;
	
	/**
	 * The rate to this taste preference
	 */
	int rate = 0;
	
	public Float getRate(){
		return NumericUtil.int2Float(rate);
	}
	
	public void setRate(Float rate){
		this.rate = NumericUtil.float2Int(rate);
	}
	
	public Taste(){

	}
	
	public Taste(int tasteID, int tasteAlias, int restaurantID){
		this.tasteId = tasteID;
		this.aliasId = tasteAlias;
		this.restaurantId = restaurantID;
	}
	
	public Taste(int tasteID, int tasteAlias, int restaurantID, String pref, 
				 short cate, short calcType, Float _rate, Float _price, short tasteType){
		this(tasteID, tasteAlias, restaurantID);
		preference = pref.trim();
		category = cate;
		calc = calcType;
		setRate(_rate);
		setPrice(_price);
		type = tasteType;
	}

	public Taste(Taste src){
		copyFrom(src);
	}
	
	public void copyFrom(Taste src){
		if(src != null && src != this){
			this.tasteId = src.tasteId;
			this.aliasId = src.aliasId;
			this.restaurantId = src.restaurantId;
			this.preference = src.preference;
			this.category = src.category;
			this.calc = src.calc;
			this.rate = src.rate;
			this.price = src.price;
			this.type = src.type;
		}
	}
	
	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Taste)){
			return false;
		}else{
			return aliasId == ((Taste)obj).aliasId && restaurantId == ((Taste)obj).restaurantId;
		}
	}
	
	public int hashCode(){
		return new Integer(aliasId).hashCode() ^ 
			   new Integer(restaurantId).hashCode();
	}
	
	/**
	 * The smaller the taste alias id, the more in front the taste stands.
	 * @param tasteToCompared
	 * @return
	 */
	public int compareTo(Taste tasteToCompared){
		if(this.aliasId > tasteToCompared.aliasId){
			return 1;
		}else if(this.aliasId < tasteToCompared.aliasId){
			return -1;
		}else{
			return 0;
		}
	}
	
	/**
	 * The price to this taste preference.
	 * Here we use an integer to represent the unit price of the food.
	 */
	int price = 0;		
	
	/**
	 * Set the price to this taste.
	 * @param price the price to this taste
	 */
	public void setPrice(Float price){
		this.price = NumericUtil.float2Int(price);
	}	

	public Float getPrice(){
		return NumericUtil.int2Float(price);
	}
	
	String preference;

	public void setPreference(String pref){
		preference = pref;
	}
	
	public String getPreference(){
		return preference == null ? NO_PREFERENCE : preference;
	}
	
	/**
	 * Invoke this method to get the price if the calculate type is for rate.
	 * @param foodPrice the food price
	 * @return the price to this taste
	 */
	public Float getPrice2(Float foodPrice){
		return NumericUtil.int2Float(NumericUtil.float2Int(foodPrice) * rate / 100);
	}
	
	/**
	 * Check if the taste belongs taste category.
	 * @return true if the taste belongs to taste, otherwise false
	 */
	public boolean isTaste(){
		return category == CATE_TASTE;
	}
	
	/**
	 * Check if the taste belongs taste specification.
	 * @return true if the taste belongs to specification, otherwise false
	 */
	public boolean isSpec(){
		return category == CATE_SPEC;
	}
	
	/**
	 * Check if the taste belongs taste style.
	 * @return true if the taste belongs to style, otherwise false
	 */
	public boolean isStyle(){ 
		return category == CATE_STYLE;
	}

	public int getRestaurantId() {
		return restaurantId;
	}

	public void setRestaurantId(int restaurantID) {
		this.restaurantId = restaurantID;
	}

	public int getTasteId() {
		return tasteId;
	}

	public void setTasteId(int tasteID) {
		this.tasteId = tasteID;
	}

	public int getAliasId() {
		return aliasId;
	}

	public void setAliasId(int aliasID) {
		this.aliasId = aliasID;
	}
	
	public short getCategory() {
		return category;
	}

	public void setCategory(short category) {
		this.category = category;
	}

	public short getType() {
		return type;
	}

	public void setType(short type) {
		this.type = type;
	}

	public short getCalc() {
		return calc;
	}

	public void setCalc(short calc) {
		this.calc = calc;
	}

	public boolean isCalcByPrice(){
		return calc == CALC_PRICE;
	}
	
	public boolean isCalcByRate(){
		return calc == CALC_RATE;
	}
	
	public void writeToParcel(Parcel dest, int flag) {
		dest.writeByte(flag);
		if(flag == TASTE_PARCELABLE_SIMPLE){
			dest.writeShort(this.aliasId);
			
		}else if(flag == TASTE_PARCELABLE_COMPLEX){
			dest.writeShort(this.aliasId);
			dest.writeByte(this.category);
			dest.writeByte(this.calc);
			dest.writeByte(this.type);
			dest.writeInt(this.price);
			dest.writeShort(this.rate);
			dest.writeString(this.preference);
		}
	}

	public void createFromParcel(Parcel source) {
		short flag = source.readByte();
		if(flag == TASTE_PARCELABLE_SIMPLE){
			this.aliasId = source.readShort();
			
		}else if(flag == TASTE_PARCELABLE_COMPLEX){
			this.aliasId = source.readShort();
			this.category = source.readByte();
			this.calc = source.readByte();
			this.type = source.readByte();
			this.price = source.readInt();
			this.rate = source.readShort();
			this.preference = source.readString();
		}
	}

	public final static Parcelable.Creator TASTE_CREATOR = new Parcelable.Creator() {
		
		public Parcelable[] newInstance(int size) {
			return new Taste[size];
		}
		
		public Parcelable newInstance() {
			return new Taste();
		}
	};
}
