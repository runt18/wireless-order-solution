package com.wireless.protocol;

public class Taste {
	
	public final static short NO_TASTE = 0;
	public final static String NO_PREFERENCE = "无口味"; 
	
	public final static short CATE_ALL = Short.MIN_VALUE;	/* 全部 */
	public final static short CATE_TASTE = 0;				/* 口味 */
	public final static short CATE_STYLE = 1;				/* 做法 */
	public final static short CATE_SPEC = 2;				/* 规格 */
	
	public final static short CALC_PRICE = 0;				/* 按价格计算  */
	public final static short CALC_RATE = 1;				/* 按比例计算  */
	
	public final static short TYPE_NORMAL = 0;				/* 一般 */
	public final static short TYPE_RESERVED = 1;			/* 保留 */
	
	public int restaurantID;
	public int tasteID;
	public int aliasID = Taste.NO_TASTE;
	public String preference = Taste.NO_PREFERENCE;
	public short category = Taste.CATE_TASTE;
	public short type = TYPE_NORMAL;
	public short calc = Taste.CALC_PRICE;
	
	/**
	 * The rate to this taste preference
	 */
	int rate = 0;
	
	public Float getRate(){
		return Util.int2Float(rate);
	}
	
	public void setRate(Float _rate){
		rate = Util.float2Int(_rate);
	}
	
	public Taste(){

	}
	
	public Taste(int tasteID, int tasteAlias, int restaurantID){
		this.tasteID = tasteID;
		this.aliasID = tasteAlias;
		this.restaurantID = restaurantID;
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

	public boolean equals(Object obj){
		if(obj == null || !(obj instanceof Taste)){
			return false;
		}else{
			return aliasID == ((Taste)obj).aliasID && restaurantID == ((Taste)obj).restaurantID;
		}
	}
	
	public int hashCode(){
		return new Integer(aliasID).hashCode() ^ 
			   new Integer(restaurantID).hashCode();
	}
	
	/**
	 * The rule to comparison is below.
	 * 1 - Put the value of no taste to the end.
	 * 2 - The smaller the taste alias id, the more the position in front.
	 * @param taste2
	 * @return
	 */
	public int compare(Taste taste2){
		if(this.aliasID == taste2.aliasID){
			return 0;
		}else if(this.aliasID == Taste.NO_TASTE){
			return 1;
		}else if(taste2.aliasID == Taste.NO_TASTE){
			return -1;
		}else if(this.aliasID > taste2.aliasID){
			return 1;
		}else if(this.aliasID < taste2.aliasID){
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
	 * Invoke this method if the calculate type is for price
	 * @param _price
	 */
	public void setPrice(Float _price){
		price = Util.float2Int(_price);
	}	

	public Float getPrice(){
		return Util.int2Float(price);
	}
	
	/**
	 * Invoke this method to get the price if the calculate type is for rate.
	 * @param foodPrice the food price
	 * @return the price to this taste
	 */
	public Float getPrice2(Float foodPrice){
		return Util.int2Float(Util.float2Int(foodPrice) * rate / 100);
	}
}
